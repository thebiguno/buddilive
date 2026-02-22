package ca.digitalcave.buddi.live.resource.buddilive.report;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
			final boolean incomeRollup = "__income__".equals(categoryIdStr);
			final boolean expensesRollup = "__expenses__".equals(categoryIdStr);
			final boolean superCategoryRollup = incomeRollup || expensesRollup;
			final int categoryId = superCategoryRollup ? -1 : Integer.parseInt(categoryIdStr);

			final Date[] dates = ReportHelper.processInterval(getQuery());
			final List<Transaction> transactions = sqlSession.getMapper(Transactions.class).selectTransactions(user, dates[0], dates[1]);

			// Find the category name
			final List<Category> categories = sqlSession.getMapper(Sources.class).selectCategories(user);
			String categoryName;
			final Set<Integer> selectedCategoryIds = new HashSet<Integer>();
			if (superCategoryRollup) {
				final String categoryType = incomeRollup ? "I" : "E";
				categoryName = incomeRollup ? "Income" : "Expenses";
				for (Category c : categories) {
					if (categoryType.equals(c.getType())) {
						selectedCategoryIds.add(c.getId());
					}
				}
			}
			else {
				categoryName = "Category " + categoryId;
				selectedCategoryIds.add(categoryId);
				for (Category c : categories) {
					if (c.getId() == categoryId) {
						categoryName = CryptoUtil.decryptWrapper(c.getName(), user);
						break;
					}
				}
			}
			final Set<Integer> childCategoryIds = superCategoryRollup ? new HashSet<Integer>() : getDescendantCategoryIds(categoryId, categories);

			// Aggregate by month:
			// - amount: selected category only
			// - childRollupAmount: all descendant child categories
			final TreeMap<String, BigDecimal> selectedCategoryMonthly = new TreeMap<String, BigDecimal>();
			final TreeMap<String, BigDecimal> childRollupMonthly = new TreeMap<String, BigDecimal>();
			for (Transaction t : transactions) {
				for (Split s : t.getSplits()) {
					final boolean matchesSelectedCategory = (selectedCategoryIds.contains(s.getFromSource()) || selectedCategoryIds.contains(s.getToSource()));
					final boolean matchesChildCategory = (childCategoryIds.contains(s.getFromSource()) || childCategoryIds.contains(s.getToSource()));
					if (matchesSelectedCategory || matchesChildCategory) {
						final String month = getMonthKey(t.getDate());
						final BigDecimal amount = CryptoUtil.decryptWrapperBigDecimal(s.getAmount(), user, true);
						if (matchesSelectedCategory) {
							selectedCategoryMonthly.put(month, amount.add(selectedCategoryMonthly.getOrDefault(month, BigDecimal.ZERO)));
						}
						else if (matchesChildCategory) {
							childRollupMonthly.put(month, amount.add(childRollupMonthly.getOrDefault(month, BigDecimal.ZERO)));
						}
					}
				}
			}

			final JSONObject result = new JSONObject();
			result.put("categoryName", categoryName);
			result.put("hasChildCategories", !superCategoryRollup && !childCategoryIds.isEmpty());

			final TreeMap<String, BigDecimal> allMonths = new TreeMap<String, BigDecimal>();
			allMonths.putAll(selectedCategoryMonthly);
			allMonths.putAll(childRollupMonthly);
			for (String key : allMonths.keySet()) {
				final BigDecimal selectedAmount = selectedCategoryMonthly.getOrDefault(key, BigDecimal.ZERO);
				final BigDecimal childRollupAmount = childRollupMonthly.getOrDefault(key, BigDecimal.ZERO);
				final BigDecimal totalAmount = selectedAmount.add(childRollupAmount);
				final JSONObject row = new JSONObject();
				row.put("month", key);
				row.put("amount", selectedAmount.doubleValue());
				row.put("amountFormatted", FormatUtil.formatCurrency(selectedAmount, user));
				row.put("childRollupAmount", childRollupAmount.doubleValue());
				row.put("childRollupAmountFormatted", FormatUtil.formatCurrency(childRollupAmount, user));
				row.put("totalAmount", totalAmount.doubleValue());
				row.put("totalAmountFormatted", FormatUtil.formatCurrency(totalAmount, user));
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

	private static String getMonthKey(Date date) {
		final Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return String.format("%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
	}

	private static Set<Integer> getDescendantCategoryIds(int parentCategoryId, List<Category> categories) {
		final Map<Integer, Set<Integer>> childrenByParent = new HashMap<Integer, Set<Integer>>();
		for (Category category : categories) {
			if (category.getParent() == null) continue;
			if (childrenByParent.get(category.getParent()) == null) {
				childrenByParent.put(category.getParent(), new HashSet<Integer>());
			}
			childrenByParent.get(category.getParent()).add(category.getId());
		}

		final Set<Integer> descendants = new HashSet<Integer>();
		final Deque<Integer> stack = new ArrayDeque<Integer>();
		final Set<Integer> directChildren = childrenByParent.get(parentCategoryId);
		if (directChildren != null) stack.addAll(directChildren);

		while (!stack.isEmpty()) {
			final Integer current = stack.removeLast();
			if (!descendants.add(current)) continue;
			final Set<Integer> children = childrenByParent.get(current);
			if (children != null) stack.addAll(children);
		}

		return descendants;
	}
}
