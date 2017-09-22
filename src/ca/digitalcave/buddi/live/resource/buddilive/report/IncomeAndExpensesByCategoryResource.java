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
import ca.digitalcave.buddi.live.model.Category;
import ca.digitalcave.buddi.live.model.Split;
import ca.digitalcave.buddi.live.model.Transaction;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.CryptoUtil;
import ca.digitalcave.buddi.live.util.FormatUtil;
import ca.digitalcave.buddi.live.util.LocaleUtil;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;

public class IncomeAndExpensesByCategoryResource extends ServerResource {

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
			
			final BigDecimal[] income = calculateCategories(result, true, user, sqlSession, sqlSession.getMapper(Sources.class).selectCategories(user, true), transactions, dates);
			final BigDecimal[] expenses = calculateCategories(result, false, user, sqlSession, sqlSession.getMapper(Sources.class).selectCategories(user, false), transactions, dates);
			
			final BigDecimal totalActual = income[0].add(expenses[0]);
			final BigDecimal totalBudgeted = income[1].add(expenses[1]);
			
			final JSONObject object = new JSONObject();
			object.put("source", LocaleUtil.getTranslation(getRequest()).getString("TOTAL"));
			object.put("sourceStyle", "font-weight: bold;");

			object.put("actual", FormatUtil.formatCurrency(totalActual, user));
			object.put("actualStyle", (FormatUtil.isRed(totalActual) ? FormatUtil.formatRed() : "") + " font-weight: bold; ");
			
			object.put("budgeted", FormatUtil.formatCurrency(totalBudgeted, user));
			object.put("budgetedStyle", (FormatUtil.isRed(totalBudgeted) ? FormatUtil.formatRed() : "") + " font-weight: bold; ");
			
			final BigDecimal difference = (totalActual.subtract(totalBudgeted != null ? totalBudgeted : BigDecimal.ZERO));
			object.put("difference", FormatUtil.formatCurrency(difference, user));
			object.put("differenceStyle", (FormatUtil.isRed(difference) ? FormatUtil.formatRed() : "") + " font-weight: bold; ");

			result.append("data", object);

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
	
	private BigDecimal[] calculateCategories(JSONObject result, boolean income, final User user, SqlSession sqlSession, List<Category> categories, List<Transaction> transactions, Date[] dates) throws JSONException, CryptoException, SQLException{
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
		
		//Map all the transactions by source ID and sum them.  We could sum in the DB, but we want the actual transactions too,
		// so we may as well just do this in memory.
		final Map<Integer, List<Transaction>> transactionsBySource = new HashMap<Integer, List<Transaction>>();
		final Map<Integer, BigDecimal> totalsBySource = new HashMap<Integer, BigDecimal>();
		for (Transaction transaction : transactions) {
			for (Split split : transaction.getSplits()) {
				//A split can only be either from or to a category; not both.  Find which it is, and
				// add it to that category in the map.
				if ("I".equals(split.getFromType()) || "E".equals(split.getFromType())){
					//Ensure there is a list in the map already
					final int source = split.getFromSource();
					if (transactionsBySource.get(source) == null) transactionsBySource.put(source, new ArrayList<Transaction>());

					//Create a new transaction, containing only this one split.
					final Transaction t = new Transaction();
					t.setDate(transaction.getDate());
					t.setDescription(transaction.getDescription());
					t.setNumber(transaction.getNumber());
					t.setSplits(new ArrayList<Split>());
					t.getSplits().add(split);

					//Add this new transaction to the map
					transactionsBySource.get(source).add(t);

					//Sum the balances
					final BigDecimal splitAmount = CryptoUtil.decryptWrapperBigDecimal(split.getAmount(), user, true);
					totalsBySource.put(source, splitAmount.add(totalsBySource.get(source) == null ? BigDecimal.ZERO : totalsBySource.get(source)));
				}
				else if ("I".equals(split.getToType()) || "E".equals(split.getToType())){
					//Ensure there is a list in the map already
					final int source = split.getToSource();
					if (transactionsBySource.get(source) == null) transactionsBySource.put(source, new ArrayList<Transaction>());

					//Create a new transaction, containing only this one split.
					final Transaction t = new Transaction();
					t.setDate(transaction.getDate());
					t.setDescription(transaction.getDescription());
					t.setNumber(transaction.getNumber());
					t.setSplits(new ArrayList<Split>());
					t.getSplits().add(split);

					//Add this new transaction to the map
					transactionsBySource.get(source).add(t);

					//Sum the balances
					final BigDecimal splitAmount = CryptoUtil.decryptWrapperBigDecimal(split.getAmount(), user, true);
					totalsBySource.put(source, splitAmount.add(totalsBySource.get(source) == null ? BigDecimal.ZERO : totalsBySource.get(source)));
				}
			}
		}
		
		BigDecimal totalActual = BigDecimal.ZERO;
		BigDecimal totalBudgeted = BigDecimal.ZERO;
		for (Category category : categories) {
			final BigDecimal budgetedAmount = category.getAmount(user, sqlSession, dates[0], dates[1]);
			final BigDecimal actualAmount = totalsBySource.get(category.getId()) == null ? BigDecimal.ZERO : totalsBySource.get(category.getId());
			
			totalActual = totalActual.add(category.isIncome() ? actualAmount : actualAmount.negate());
			totalBudgeted = totalBudgeted.add(category.isIncome() ? budgetedAmount : budgetedAmount.negate());
			if (budgetedAmount.compareTo(BigDecimal.ZERO) != 0 || actualAmount.compareTo(BigDecimal.ZERO) != 0){
				final JSONObject object = new JSONObject();
				object.put("source", CryptoUtil.decryptWrapper(category.getName(), user));

				object.put("actual", FormatUtil.formatCurrency(actualAmount, user));
				object.put("actualStyle", (FormatUtil.isRed(category, actualAmount) ? FormatUtil.formatRed() : ""));
				
				object.put("budgeted", FormatUtil.formatCurrency(budgetedAmount, user));
				object.put("budgetedStyle", (FormatUtil.isRed(category, budgetedAmount) ? FormatUtil.formatRed() : ""));
				
				final BigDecimal difference = (actualAmount.subtract(budgetedAmount != null ? budgetedAmount : BigDecimal.ZERO));
				object.put("difference", FormatUtil.formatCurrency(category.isIncome() ? difference : difference.negate(), user));
				object.put("differenceStyle", (FormatUtil.isRed(category.isIncome() ? difference : difference.negate()) ? FormatUtil.formatRed() : ""));
				
				final List<Transaction> transactionsInCategory = transactionsBySource.get(category.getId());
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
						o.put("amountStyle", (FormatUtil.isRed(category, splitAmount) ? FormatUtil.formatRed() : ""));
						ts.put(o);
					}
					object.put("transactions", ts);
				}

				result.append("data", object);
			}
		}
		
		final JSONObject object = new JSONObject();
		object.put("source", LocaleUtil.getTranslation(getRequest()).getString(income ? "TOTAL_INCOME" : "TOTAL_EXPENSES"));
		object.put("sourceStyle", "font-weight: bold;");

		object.put("actual", FormatUtil.formatCurrency(totalActual, user));
		object.put("actualStyle", (FormatUtil.isRed(totalActual) ? FormatUtil.formatRed() : "") + " font-weight: bold; ");
		
		object.put("budgeted", FormatUtil.formatCurrency(totalBudgeted, user));
		object.put("budgetedStyle", (FormatUtil.isRed(totalBudgeted) ? FormatUtil.formatRed() : "") + " font-weight: bold; ");
		
		final BigDecimal difference = (totalActual.subtract(totalBudgeted != null ? totalBudgeted : BigDecimal.ZERO));
		object.put("difference", FormatUtil.formatCurrency(difference, user));
		object.put("differenceStyle", (FormatUtil.isRed(difference) ? FormatUtil.formatRed() : "") + " font-weight: bold; ");

		result.append("data", object);
		
		return new BigDecimal[]{totalActual, totalBudgeted};
	}
}
