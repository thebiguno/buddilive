package ca.digitalcave.buddi.live.resource.buddilive.report;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.apache.ibatis.session.SqlSession;
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
import ca.digitalcave.moss.common.DateUtil;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;

public class MonthlyCashFlowResource extends ServerResource {

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

			// month key -> [income, expenses]
			final TreeMap<String, BigDecimal[]> monthly = new TreeMap<String, BigDecimal[]>();

			for (Transaction t : transactions) {
				final Calendar cal = Calendar.getInstance();
				cal.setTime(t.getDate());
				final String key = String.format("%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
				if (!monthly.containsKey(key)) monthly.put(key, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});

				for (Split s : t.getSplits()) {
					final BigDecimal amt = CryptoUtil.decryptWrapperBigDecimal(s.getAmount(), user, true);
					// income: category is on the "to" side (money flows into account from income category)
					if ("I".equals(s.getFromType())) {
						monthly.get(key)[0] = monthly.get(key)[0].add(amt);
					} else if ("E".equals(s.getToType())) {
						monthly.get(key)[1] = monthly.get(key)[1].add(amt);
					}
				}
			}

			final JSONObject result = new JSONObject();
			for (String key : monthly.keySet()) {
				final BigDecimal income = monthly.get(key)[0];
				final BigDecimal expenses = monthly.get(key)[1];
				final BigDecimal net = income.subtract(expenses);

				final JSONObject row = new JSONObject();
				row.put("month", key);
				row.put("income", income.doubleValue());
				row.put("expenses", expenses.doubleValue());
				row.put("net", net.doubleValue());
				row.put("incomeFormatted", FormatUtil.formatCurrency(income, user));
				row.put("expensesFormatted", FormatUtil.formatCurrency(expenses, user));
				row.put("netFormatted", FormatUtil.formatCurrency(net, user));
				result.append("data", row);
			}

			result.put("success", true);
			return new JsonRepresentation(result);
		} catch (NumberFormatException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		} catch (CryptoException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		} catch (JSONException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		} finally {
			sqlSession.close();
		}
	}
}
