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
import ca.digitalcave.buddi.live.db.Sources;
import ca.digitalcave.buddi.live.db.Transactions;
import ca.digitalcave.buddi.live.model.Account;
import ca.digitalcave.buddi.live.model.Split;
import ca.digitalcave.buddi.live.model.Transaction;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.CryptoUtil;
import ca.digitalcave.buddi.live.util.FormatUtil;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;

public class InflowAndOutflowByAccountResource extends ServerResource {

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
			
			calculateInflowOutflow(result, user, sqlSession, sqlSession.getMapper(Sources.class).selectAccounts(user), transactions, dates);

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
	
	private void calculateInflowOutflow(JSONObject result, final User user, SqlSession sqlSession, List<Account> accounts, List<Transaction> transactions, Date[] dates) throws JSONException, CryptoException, SQLException{
		//Sort collections by debit, name.  We can't do this in the DB due to encrypted names.
		Collections.sort(accounts, new Comparator<Account>() {
			@Override
			public int compare(Account o1, Account o2) {
				if (o1 == null || o2 == null) return 0;
				
				if (o1.isDebit() != o2.isDebit()) return o1.isDebit() ? -1 : 1;
				
				try {
					return CryptoUtil.decryptWrapper(o1.getName(), user).compareTo(CryptoUtil.decryptWrapper(o2.getName(), user));
				}
				catch (CryptoException e){
					return 0;
				}
			}
		});
		
		//Map all the transactions by source ID and sum them.  We could sum in the DB, but we do it this way elsewhere so we may as well just do this in memory.
		final Map<Integer, BigDecimal> totalInflowsBySource = new HashMap<Integer, BigDecimal>();
		final Map<Integer, BigDecimal> totalOutflowsBySource = new HashMap<Integer, BigDecimal>();
		final Map<Integer, List<Transaction>> transactionsBySource = new HashMap<Integer, List<Transaction>>();
		
		for (Transaction transaction : transactions) {
			for (Split split : transaction.getSplits()) {
				if ("D".equals(split.getFromType()) || "C".equals(split.getFromType())){
					//Ensure there is a list in the map already
					final int source = split.getFromSource();

					//Create a new transaction, containing only this one split.
					final Transaction t = new Transaction();
					t.setDate(transaction.getDate());
					t.setDescription(transaction.getDescription());
					t.setNumber(transaction.getNumber());
					t.setSplits(new ArrayList<Split>());
					t.getSplits().add(split);
					if (transactionsBySource.get(source) == null){
						transactionsBySource.put(source, new ArrayList<Transaction>());
					}
					transactionsBySource.get(source).add(t);

					//Sum the balances
					final BigDecimal splitAmount = CryptoUtil.decryptWrapperBigDecimal(split.getAmount(), user, true);
					if (splitAmount.compareTo(BigDecimal.ZERO) < 0){
						totalInflowsBySource.put(source, splitAmount.negate().add(totalInflowsBySource.get(source) == null ? BigDecimal.ZERO : totalInflowsBySource.get(source)));
					}
					else {
						totalOutflowsBySource.put(source, splitAmount.add(totalOutflowsBySource.get(source) == null ? BigDecimal.ZERO : totalOutflowsBySource.get(source)));
					}
				}
				
				if ("D".equals(split.getToType()) || "C".equals(split.getToType())){
					//Ensure there is a list in the map already
					final int source = split.getToSource();

					//Create a new transaction, containing only this one split.
					final Transaction t = new Transaction();
					t.setDate(transaction.getDate());
					t.setDescription(transaction.getDescription());
					t.setNumber(transaction.getNumber());
					t.setSplits(new ArrayList<Split>());
					t.getSplits().add(split);
					if (transactionsBySource.get(source) == null){
						transactionsBySource.put(source, new ArrayList<Transaction>());
					}
					transactionsBySource.get(source).add(t);

					//Sum the balances
					final BigDecimal splitAmount = CryptoUtil.decryptWrapperBigDecimal(split.getAmount(), user, true);
					if (splitAmount.compareTo(BigDecimal.ZERO) < 0){
						totalOutflowsBySource.put(source, splitAmount.negate().add(totalOutflowsBySource.get(source) == null ? BigDecimal.ZERO : totalOutflowsBySource.get(source)));
					}
					else {
						totalInflowsBySource.put(source, splitAmount.add(totalInflowsBySource.get(source) == null ? BigDecimal.ZERO : totalInflowsBySource.get(source)));
					}
				}
			}
		}
		
		for (Account account : accounts) {
			final BigDecimal inflow = totalInflowsBySource.get(account.getId()) == null ? BigDecimal.ZERO : totalInflowsBySource.get(account.getId());
			final BigDecimal outflow = totalOutflowsBySource.get(account.getId()) == null ? BigDecimal.ZERO : totalOutflowsBySource.get(account.getId());
			
			if (inflow.compareTo(BigDecimal.ZERO) != 0 || outflow.compareTo(BigDecimal.ZERO) != 0){
				final JSONObject object = new JSONObject();
				object.put("source", CryptoUtil.decryptWrapper(account.getName(), user));
				object.put("sourceStyle", (account.isDebit() ? "" : FormatUtil.formatRed()));
				
				object.put("inflow", FormatUtil.formatCurrency(inflow, user));
				object.put("inflowStyle", (FormatUtil.isRed(account, inflow) ? FormatUtil.formatRed() : ""));
				
				object.put("outflow", FormatUtil.formatCurrency(outflow, user));
				object.put("outflowStyle", (FormatUtil.isRed(account, outflow.negate()) ? FormatUtil.formatRed() : ""));
				
				final BigDecimal difference = (inflow.subtract(outflow));
				object.put("difference", FormatUtil.formatCurrency(difference, user));
				object.put("differenceStyle", (FormatUtil.isRed(account, difference) ? FormatUtil.formatRed() : ""));

				final List<Transaction> transactionsInCategory = transactionsBySource.get(account.getId());
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
						o.put("amountStyle", (FormatUtil.isRed(account, splitAmount) ? FormatUtil.formatRed() : ""));
						ts.put(o);
					}
					object.put("transactions", ts);
				}
				
				result.append("data", object);
			}
		}
	}
}
