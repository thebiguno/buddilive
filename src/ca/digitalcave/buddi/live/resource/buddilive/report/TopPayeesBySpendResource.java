package ca.digitalcave.buddi.live.resource.buddilive.report;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import ca.digitalcave.moss.crypto.Crypto.CryptoException;

public class TopPayeesBySpendResource extends ServerResource {

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
			final int topN = Integer.parseInt(getQuery().getFirstValue("topN", "10"));
			final Date[] dates = ReportHelper.processInterval(getQuery());
			final List<Transaction> transactions = sqlSession.getMapper(Transactions.class).selectTransactions(user, dates[0], dates[1]);

			final Map<String, BigDecimal> spendByPayee = new HashMap<String, BigDecimal>();
			BigDecimal totalSpend = BigDecimal.ZERO;

			for (Transaction t : transactions) {
				final String payee = CryptoUtil.decryptWrapper(t.getDescription(), user);
				for (Split s : t.getSplits()) {
					// Only count expense outflows (positive amounts going to expense categories)
					if ("E".equals(s.getToType())) {
						final BigDecimal amt = CryptoUtil.decryptWrapperBigDecimal(s.getAmount(), user, true);
						if (amt.compareTo(BigDecimal.ZERO) > 0) {
							spendByPayee.put(payee, amt.add(spendByPayee.getOrDefault(payee, BigDecimal.ZERO)));
							totalSpend = totalSpend.add(amt);
						}
					}
				}
			}

			// Sort by spend descending
			final List<Map.Entry<String, BigDecimal>> sorted = new ArrayList<>(spendByPayee.entrySet());
			Collections.sort(sorted, (a, b) -> b.getValue().compareTo(a.getValue()));

			final JSONObject result = new JSONObject();
			final BigDecimal total = totalSpend;
			int count = 0;
			for (Map.Entry<String, BigDecimal> entry : sorted) {
				if (count++ >= topN) break;
				final BigDecimal spend = entry.getValue();
				final double pct = total.compareTo(BigDecimal.ZERO) == 0 ? 0
						: spend.doubleValue() / total.doubleValue() * 100.0;

				final JSONObject row = new JSONObject();
				row.put("payee", entry.getKey());
				row.put("spend", spend.doubleValue());
				row.put("spendFormatted", FormatUtil.formatCurrency(spend, user));
				row.put("percentOfTotal", Math.round(pct * 10.0) / 10.0);
				row.put("percentFormatted", String.format("%.1f%%", pct));
				result.append("data", row);
			}

			result.put("totalSpend", totalSpend.doubleValue());
			result.put("totalSpendFormatted", FormatUtil.formatCurrency(totalSpend, user));
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
