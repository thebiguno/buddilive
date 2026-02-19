package ca.digitalcave.buddi.live.resource.buddilive.report;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
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
import ca.digitalcave.buddi.live.db.Sources;
import ca.digitalcave.buddi.live.db.Transactions;
import ca.digitalcave.buddi.live.model.Category;
import ca.digitalcave.buddi.live.model.Split;
import ca.digitalcave.buddi.live.model.Transaction;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.CryptoUtil;
import ca.digitalcave.buddi.live.util.FormatUtil;
import ca.digitalcave.moss.common.DateUtil;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;

/**
 * Returns per-category spending for two periods: the selected interval ("current")
 * and the automatically-derived prior period of the same length ("previous").
 *
 * Query params:
 *   interval=PLUGIN_FILTER_*  — uses the named interval; prior period is derived by type
 *   interval=PLUGIN_FILTER_OTHER&startDate=YYYY-MM-DD&endDate=YYYY-MM-DD
 *                             — custom range; prior period is the same number of days going back
 */
public class YearOverYearResource extends ServerResource {

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
			final Date[] current = ReportHelper.processInterval(getQuery());
			final Date[] previous = derivePreviousPeriod(getQuery().getFirstValue("interval"), current);

			final List<Transaction> currentTx = sqlSession.getMapper(Transactions.class).selectTransactions(user, current[0], current[1]);
			final List<Transaction> previousTx = sqlSession.getMapper(Transactions.class).selectTransactions(user, previous[0], previous[1]);
			final List<Category> categories = sqlSession.getMapper(Sources.class).selectCategories(user);

			final Map<Integer, BigDecimal> currentByCategory = sumByCategory(currentTx, user);
			final Map<Integer, BigDecimal> previousByCategory = sumByCategory(previousTx, user);

			Collections.sort(categories, new Comparator<Category>() {
				@Override
				public int compare(Category o1, Category o2) {
					if (o1 == null || o2 == null) return 0;
					if (o1.isIncome() != o2.isIncome()) return o1.isIncome() ? -1 : 1;
					try {
						return CryptoUtil.decryptWrapper(o1.getName(), user).compareTo(CryptoUtil.decryptWrapper(o2.getName(), user));
					} catch (CryptoException e) { return 0; }
				}
			});

			final JSONObject result = new JSONObject();
			result.put("currentPeriod", FormatUtil.formatDate(current[0], user) + " – " + FormatUtil.formatDate(current[1], user));
			result.put("previousPeriod", FormatUtil.formatDate(previous[0], user) + " – " + FormatUtil.formatDate(previous[1], user));

			for (Category c : categories) {
				if (c.isDeleted()) continue;
				final BigDecimal cur = currentByCategory.getOrDefault(c.getId(), BigDecimal.ZERO);
				final BigDecimal prev = previousByCategory.getOrDefault(c.getId(), BigDecimal.ZERO);
				if (cur.compareTo(BigDecimal.ZERO) == 0 && prev.compareTo(BigDecimal.ZERO) == 0) continue;

				final JSONObject row = new JSONObject();
				row.put("category", CryptoUtil.decryptWrapper(c.getName(), user));
				row.put("current", cur.doubleValue());
				row.put("previous", prev.doubleValue());
				row.put("currentFormatted", FormatUtil.formatCurrency(cur, user));
				row.put("previousFormatted", FormatUtil.formatCurrency(prev, user));
				row.put("income", c.isIncome());
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

	private Map<Integer, BigDecimal> sumByCategory(List<Transaction> transactions, User user) throws CryptoException {
		final Map<Integer, BigDecimal> map = new HashMap<Integer, BigDecimal>();
		for (Transaction t : transactions) {
			for (Split s : t.getSplits()) {
				if ("I".equals(s.getFromType()) || "E".equals(s.getFromType())) {
					final int id = s.getFromSource();
					final BigDecimal amt = CryptoUtil.decryptWrapperBigDecimal(s.getAmount(), user, true);
					map.put(id, amt.add(map.getOrDefault(id, BigDecimal.ZERO)));
				} else if ("I".equals(s.getToType()) || "E".equals(s.getToType())) {
					final int id = s.getToSource();
					final BigDecimal amt = CryptoUtil.decryptWrapperBigDecimal(s.getAmount(), user, true);
					map.put(id, amt.add(map.getOrDefault(id, BigDecimal.ZERO)));
				}
			}
		}
		return map;
	}

	private Date[] derivePreviousPeriod(String intervalName, Date[] current) {
		if (intervalName == null) intervalName = "";
		switch (intervalName) {
			case "PLUGIN_FILTER_THIS_WEEK":
			case "PLUGIN_FILTER_LAST_WEEK":
				return new Date[]{ DateUtil.addDays(current[0], -7), DateUtil.addDays(current[1], -7) };
			case "PLUGIN_FILTER_THIS_SEMI_MONTH":
			case "PLUGIN_FILTER_LAST_SEMI_MONTH":
				return new Date[]{ DateUtil.addDays(current[0], -15), DateUtil.addDays(current[1], -15) };
			case "PLUGIN_FILTER_THIS_MONTH":
			case "PLUGIN_FILTER_LAST_MONTH":
				return new Date[]{ DateUtil.addMonths(current[0], -1), DateUtil.getEndOfMonth(DateUtil.addMonths(current[1], -1)) };
			case "PLUGIN_FILTER_THIS_QUARTER":
			case "PLUGIN_FILTER_LAST_QUARTER":
				return new Date[]{ DateUtil.addQuarters(current[0], -1), DateUtil.addQuarters(current[1], -1) };
			case "PLUGIN_FILTER_THIS_YEAR":
			case "PLUGIN_FILTER_THIS_YEAR_TO_DATE":
			case "PLUGIN_FILTER_LAST_YEAR":
				return new Date[]{ DateUtil.addYears(current[0], -1), DateUtil.addYears(current[1], -1) };
			default:
				// Custom or unknown: shift back by the same number of days
				final int days = DateUtil.getDaysBetween(current[0], current[1], true);
				return new Date[]{ DateUtil.addDays(current[0], -days - 1), DateUtil.addDays(current[1], -days - 1) };
		}
	}
}
