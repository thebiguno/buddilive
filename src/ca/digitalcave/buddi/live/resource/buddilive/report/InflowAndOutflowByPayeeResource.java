package ca.digitalcave.buddi.live.resource.buddilive.report;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ca.digitalcave.buddi.live.BuddiApplication;
import ca.digitalcave.buddi.live.db.Transactions;
import ca.digitalcave.buddi.live.model.Split;
import ca.digitalcave.buddi.live.model.Transaction;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.CryptoUtil;
import ca.digitalcave.buddi.live.util.FormatUtil;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;

public class InflowAndOutflowByPayeeResource extends ServerResource {

	@Override
	protected void doInit() throws ResourceException {
		getVariants().add(new Variant(MediaType.APPLICATION_JSON));
	}

	@Override
	protected Representation get(Variant variant) throws ResourceException {
		final BuddiApplication application = (BuddiApplication) getApplication();
		final SqlSession sqlSession = application.getSqlSessionFactory().openSession(true);
		final User user = (User) getRequest().getClientInfo().getUser();
		try {
			final Date[] dates = ReportHelper.processInterval(getQuery());
			final List<Transaction> transactions = sqlSession.getMapper(Transactions.class).selectTransactions(user, dates[0], dates[1]);
			
			final JSONObject result = new JSONObject();
			
			calculateInflowOutflow(result, user, sqlSession, transactions, dates);

			result.put("success", true);
			return new JsonRepresentation(result);
		}
		catch (NumberFormatException e){
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}
		catch (CryptoException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		catch (JSONException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		catch (SQLException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		finally {
			sqlSession.close();
		}
	}
	
	private void calculateInflowOutflow(JSONObject result, final User user, SqlSession sqlSession, List<Transaction> transactions, Date[] dates) throws JSONException, CryptoException, SQLException{
		//Map all the transactions by source ID and sum them.  We could sum in the DB, but we do it this way elsewhere so we may as well just do this in memory.
		final Map<String, BigDecimal> totalInflowsByPayee = new HashMap<String, BigDecimal>();
		final Map<String, BigDecimal> totalOutflowsByPayee = new HashMap<String, BigDecimal>();
		final Map<String, List<Transaction>> transactionsByPayee = new HashMap<String, List<Transaction>>();
		
		for (Transaction transaction : transactions) {
			for (Split split : transaction.getSplits()) {
				final String payee = CryptoUtil.decryptWrapper(transaction.getDescription(), user);
				
				//Create a new transaction, containing only this one split.
				final Transaction t = new Transaction();
				t.setDate(transaction.getDate());
				t.setDescription(transaction.getDescription());
				t.setNumber(transaction.getNumber());
				t.setSplits(new ArrayList<Split>());
				t.getSplits().add(split);
				//Ensure there is a list in the map already
				if (transactionsByPayee.get(payee) == null){
					transactionsByPayee.put(payee, new ArrayList<Transaction>());
				}
				transactionsByPayee.get(payee).add(t);

				//Sum the balances
				final BigDecimal splitAmount = CryptoUtil.decryptWrapperBigDecimal(split.getAmount(), user, true);
				if (splitAmount.compareTo(BigDecimal.ZERO) < 0){
					totalInflowsByPayee.put(payee, splitAmount.negate().add(totalInflowsByPayee.get(payee) == null ? BigDecimal.ZERO : totalInflowsByPayee.get(payee)));
				}
				else {
					totalOutflowsByPayee.put(payee, splitAmount.add(totalOutflowsByPayee.get(payee) == null ? BigDecimal.ZERO : totalOutflowsByPayee.get(payee)));
				}
			}
		}
		
		List<String> payees = new ArrayList<String>(transactionsByPayee.keySet());
		
		//Sort collections by debit, name.  We can't do this in the DB due to encrypted names.
		Collections.sort(payees);
		
		for (String payee : payees) {
			final BigDecimal inflow = totalInflowsByPayee.get(payee) == null ? BigDecimal.ZERO : totalInflowsByPayee.get(payee);
			final BigDecimal outflow = totalOutflowsByPayee.get(payee) == null ? BigDecimal.ZERO : totalOutflowsByPayee.get(payee);
			
			if (inflow.compareTo(BigDecimal.ZERO) != 0 || outflow.compareTo(BigDecimal.ZERO) != 0){
				final JSONObject object = new JSONObject();
				object.put("source", payee);
				
				object.put("inflow", FormatUtil.formatCurrency(inflow, user));
				object.put("inflowStyle", (FormatUtil.isRed(inflow) ? FormatUtil.formatRed() : ""));
				
				object.put("outflow", FormatUtil.formatCurrency(outflow, user));
				object.put("outflowStyle", (FormatUtil.isRed(outflow.negate()) ? FormatUtil.formatRed() : ""));
				
				final BigDecimal difference = (inflow.subtract(outflow));
				object.put("difference", FormatUtil.formatCurrency(difference, user));
				object.put("differenceStyle", (FormatUtil.isRed(difference) ? FormatUtil.formatRed() : ""));

				final List<Transaction> transactionsInCategory = transactionsByPayee.get(payee);
				if (transactionsInCategory != null){
					Collections.sort(transactionsInCategory, new Comparator<Transaction>() {
						@Override
						public int compare(Transaction o1, Transaction o2) {
							if (o1 == null || o2 == null) return 0;
							return o1.getDate().compareTo(o2.getDate());
						}
					});
					final JSONArray ts = new JSONArray();
					for (Transaction t : transactionsInCategory) {
						final JSONObject o = new JSONObject();
						o.put("date", FormatUtil.formatDate(t.getDate(), user));
						o.put("description", CryptoUtil.decryptWrapper(t.getDescription(), user));
						o.put("number", CryptoUtil.decryptWrapper(t.getNumber(), user));
						final Split split = t.getSplits().get(0);
						o.put("from", CryptoUtil.decryptWrapper(split.getFromSourceName(), user));
						o.put("to", CryptoUtil.decryptWrapper(split.getToSourceName(), user));
						final BigDecimal splitAmount = CryptoUtil.decryptWrapperBigDecimal(split.getAmount(), user, true);
						o.put("amount", FormatUtil.formatCurrency(splitAmount, user));
						o.put("amountStyle", (FormatUtil.isRed(splitAmount) ? FormatUtil.formatRed() : ""));
						ts.put(o);
					}
					object.put("transactions", ts);
				}
				
				result.append("data", object);
			}
		}
	}
}
