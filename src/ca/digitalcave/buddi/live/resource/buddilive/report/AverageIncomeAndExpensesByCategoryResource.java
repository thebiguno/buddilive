package ca.digitalcave.buddi.live.resource.buddilive.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import ca.digitalcave.buddi.live.model.CategoryPeriod.CategoryPeriods;
import ca.digitalcave.buddi.live.model.Split;
import ca.digitalcave.buddi.live.model.Transaction;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.CryptoUtil;
import ca.digitalcave.buddi.live.util.FormatUtil;
import ca.digitalcave.buddi.live.util.LocaleUtil;
import ca.digitalcave.moss.common.DateUtil;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;

public class AverageIncomeAndExpensesByCategoryResource extends ServerResource {

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
			
			calculateCategories(result, true, user, sqlSession, sqlSession.getMapper(Sources.class).selectCategories(user, true), transactions, dates);
			calculateCategories(result, false, user, sqlSession, sqlSession.getMapper(Sources.class).selectCategories(user, false), transactions, dates);

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
	
	private void calculateCategories(JSONObject result, boolean income, final User user, SqlSession sqlSession, List<Category> categories, List<Transaction> transactions, Date[] dates) throws JSONException, CryptoException, SQLException{
		//Sort collections by income, name.  We can't do this in the DB due to encrypted names.
		Collections.sort(categories, new Comparator<Category>() {
			@Override
			public int compare(Category o1, Category o2) {
				if (o1 == null || o2 == null) return 0;
				
				if (o1.isIncome() != o2.isIncome()) return o1.isIncome() ? -1 : 1;
				
				try {
					return CryptoUtil.decryptWrapper(o1.getName(), user).compareTo(CryptoUtil.decryptWrapper(o2.getName(), user));
				}
				catch (CryptoException e){
					return 0;
				}
			}
		});
		
		//Map all the transactions by source ID and sum them.  We could sum in the DB, but we do it this way elsewhere so we may as well just do this in memory.
		final Map<Integer, BigDecimal> totalsBySource = new HashMap<Integer, BigDecimal>();
		for (Transaction transaction : transactions) {
			for (Split split : transaction.getSplits()) {
				//A split can only be either from or to a category; not both.  Find which it is, and
				// add it to that category in the map.
				if ("I".equals(split.getFromType()) || "E".equals(split.getFromType())){
					//Ensure there is a list in the map already
					final int source = split.getFromSource();

					//Create a new transaction, containing only this one split.
					final Transaction t = new Transaction();
					t.setDate(transaction.getDate());
					t.setDescription(transaction.getDescription());
					t.setNumber(transaction.getNumber());
					t.setSplits(new ArrayList<Split>());
					t.getSplits().add(split);

					//Sum the balances
					final BigDecimal splitAmount = CryptoUtil.decryptWrapperBigDecimal(split.getAmount(), user, true);
					totalsBySource.put(source, splitAmount.add(totalsBySource.get(source) == null ? BigDecimal.ZERO : totalsBySource.get(source)));
				}
				else if ("I".equals(split.getToType()) || "E".equals(split.getToType())){
					//Ensure there is a list in the map already
					final int source = split.getToSource();

					//Create a new transaction, containing only this one split.
					final Transaction t = new Transaction();
					t.setDate(transaction.getDate());
					t.setDescription(transaction.getDescription());
					t.setNumber(transaction.getNumber());
					t.setSplits(new ArrayList<Split>());
					t.getSplits().add(split);

					//Sum the balances
					final BigDecimal splitAmount = CryptoUtil.decryptWrapperBigDecimal(split.getAmount(), user, true);
					totalsBySource.put(source, splitAmount.add(totalsBySource.get(source) == null ? BigDecimal.ZERO : totalsBySource.get(source)));
				}
			}
		}
		
		final Map<String, BigDecimal> totalActualByPeriod = new TreeMap<String, BigDecimal>();
		final Map<String, BigDecimal> totalBudgetedByPeriod = new TreeMap<String, BigDecimal>();
		final int totalDaysInRange = DateUtil.getDaysBetween(dates[0], dates[1], true);
		
		for (Category category : categories) {
			final BigDecimal budgetedAmount = category.getAmount(user, sqlSession, dates[0], dates[1]);
			final BigDecimal actualAmount = totalsBySource.get(category.getId()) == null ? BigDecimal.ZERO : totalsBySource.get(category.getId());
			
			if (budgetedAmount.compareTo(BigDecimal.ZERO) != 0 || actualAmount.compareTo(BigDecimal.ZERO) != 0){
				final JSONObject object = new JSONObject();
				object.put("source", CryptoUtil.decryptWrapper(category.getName(), user));
				
				final BigDecimal daysInPeriod = new BigDecimal(CategoryPeriods.valueOf(category.getPeriodType()).getDaysInPeriod(dates[0]));
				final BigDecimal averageAmount = actualAmount.divide(new BigDecimal(totalDaysInRange), RoundingMode.HALF_UP).multiply(daysInPeriod);
				object.put("average", FormatUtil.formatCurrency(averageAmount, user));
				object.put("averageStyle", (FormatUtil.isRed(category, averageAmount) ? FormatUtil.formatRed() : ""));
				
				final BigDecimal averageBudgeted = budgetedAmount.divide(new BigDecimal(totalDaysInRange), RoundingMode.HALF_UP).multiply(daysInPeriod);
				object.put("averageBudgeted", FormatUtil.formatCurrency(averageBudgeted, user));
				object.put("averageBudgetedStyle", (FormatUtil.isRed(category, averageBudgeted) ? FormatUtil.formatRed() : ""));
				
				final BigDecimal difference = (averageAmount.subtract(averageBudgeted));
				object.put("difference", FormatUtil.formatCurrency(difference, user));
				object.put("differenceStyle", (FormatUtil.isRed(category, difference) ? FormatUtil.formatRed() : ""));

				object.put("period", LocaleUtil.getTranslation(getRequest()).getString("BUDGET_CATEGORY_TYPE_" + category.getPeriodType()));
				object.put("periodStyle", (FormatUtil.isRed(category, actualAmount) ? FormatUtil.formatRed() : ""));
				
				totalActualByPeriod.put(category.getPeriodType(), (totalActualByPeriod.get(category.getPeriodType()) == null ? BigDecimal.ZERO : totalActualByPeriod.get(category.getPeriodType())).add(averageAmount));
				totalBudgetedByPeriod.put(category.getPeriodType(), (totalBudgetedByPeriod.get(category.getPeriodType()) == null ? BigDecimal.ZERO : totalBudgetedByPeriod.get(category.getPeriodType())).add(averageBudgeted));

				result.append("data", object);
			}
		}
		
		for (String period : totalActualByPeriod.keySet()) {
			final JSONObject object = new JSONObject();
			object.put("source", LocaleUtil.getTranslation(getRequest()).getString(income ? "AVERAGE_INCOME" : "AVERAGE_EXPENSES") + " / " + LocaleUtil.getTranslation(getRequest()).getString("BUDGET_CATEGORY_TYPE_" + period));
			object.put("sourceStyle", "font-weight: bold;");
			
			object.put("average", FormatUtil.formatCurrency(totalActualByPeriod.get(period), user));
			object.put("averageStyle", "font-weight: bold;" + (totalActualByPeriod.get(period).compareTo(BigDecimal.ZERO) >= 0 ? FormatUtil.formatRed() : ""));
			
			object.put("averageBudgeted", FormatUtil.formatCurrency(totalBudgetedByPeriod.get(period), user));
			object.put("averageBudgetedStyle", "font-weight: bold;" + (totalBudgetedByPeriod.get(period).compareTo(BigDecimal.ZERO) >= 0 ? FormatUtil.formatRed() : ""));
			
			final BigDecimal difference = (totalActualByPeriod.get(period).subtract(totalBudgetedByPeriod.get(period)));
			object.put("difference", FormatUtil.formatCurrency(difference, user));
			object.put("differenceStyle", "font-weight: bold;" + (FormatUtil.isRed(difference) ? FormatUtil.formatRed() : ""));

			object.put("period", LocaleUtil.getTranslation(getRequest()).getString("BUDGET_CATEGORY_TYPE_" + period));
			object.put("periodStyle", "font-weight: bold;" + FormatUtil.formatRed());

			result.append("data", object);
		}
	}
}
