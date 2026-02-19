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
import ca.digitalcave.buddi.live.db.Sources;
import ca.digitalcave.buddi.live.db.Transactions;
import ca.digitalcave.buddi.live.model.Category;
import ca.digitalcave.buddi.live.model.Split;
import ca.digitalcave.buddi.live.model.Transaction;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.CryptoUtil;
import ca.digitalcave.buddi.live.util.FormatUtil;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;

/**
 * Returns monthly spending trend for a single category over the selected interval.
 * Query params: interval=..., categoryId=N
 */
public class CategoryDrillDownResource extends ServerResource {

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
			final String categoryIdStr = getQuery().getFirstValue("categoryId");
			if (categoryIdStr == null) throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "categoryId is required");
			final int categoryId = Integer.parseInt(categoryIdStr);

			final Date[] dates = ReportHelper.processInterval(getQuery());
			final List<Transaction> transactions = sqlSession.getMapper(Transactions.class).selectTransactions(user, dates[0], dates[1]);

			// Find the category name
			final List<Category> categories = sqlSession.getMapper(Sources.class).selectCategories(user);
			String categoryName = "Category " + categoryId;
			for (Category c : categories) {
				if (c.getId() == categoryId) {
					categoryName = CryptoUtil.decryptWrapper(c.getName(), user);
					break;
				}
			}

			// Aggregate by month
			final TreeMap<String, BigDecimal> monthly = new TreeMap<String, BigDecimal>();
			for (Transaction t : transactions) {
				for (Split s : t.getSplits()) {
					if (s.getFromSource() == categoryId || s.getToSource() == categoryId) {
						final Calendar cal = Calendar.getInstance();
						cal.setTime(t.getDate());
						final String key = String.format("%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
						final BigDecimal amt = CryptoUtil.decryptWrapperBigDecimal(s.getAmount(), user, true);
						monthly.put(key, amt.add(monthly.getOrDefault(key, BigDecimal.ZERO)));
					}
				}
			}

			final JSONObject result = new JSONObject();
			result.put("categoryName", categoryName);
			for (String key : monthly.keySet()) {
				final BigDecimal amt = monthly.get(key);
				final JSONObject row = new JSONObject();
				row.put("month", key);
				row.put("amount", amt.doubleValue());
				row.put("amountFormatted", FormatUtil.formatCurrency(amt, user));
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
