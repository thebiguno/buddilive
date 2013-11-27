package ca.digitalcave.buddi.live.resource.buddilive;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
import ca.digitalcave.buddi.live.db.Entries;
import ca.digitalcave.buddi.live.db.Sources;
import ca.digitalcave.buddi.live.db.util.ConstraintsChecker;
import ca.digitalcave.buddi.live.db.util.DataUpdater;
import ca.digitalcave.buddi.live.db.util.DatabaseException;
import ca.digitalcave.buddi.live.model.Category;
import ca.digitalcave.buddi.live.model.CategoryPeriod;
import ca.digitalcave.buddi.live.model.CategoryPeriod.CategoryPeriods;
import ca.digitalcave.buddi.live.model.Entry;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.CryptoUtil;
import ca.digitalcave.buddi.live.util.FormatUtil;
import ca.digitalcave.buddi.live.util.LocaleUtil;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;

public class CategoriesResource extends ServerResource {

	private static final BigDecimal BIGDECIMAL_ZERO = FormatUtil.parseCurrency("0.00");
	
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
			CategoryPeriod cp = new CategoryPeriod(CategoryPeriods.valueOf(getQuery().getFirstValue("periodType")), FormatUtil.parseDateInternal(getQuery().getFirstValue("date")), Integer.parseInt(getQuery().getFirstValue("offset", "0")));
			final List<Category> categories = Category.getHierarchy(sqlSession.getMapper(Sources.class).selectCategories(user, cp, true));
			
			final JSONArray data = new JSONArray();
			for (Category c : categories) {
				final JSONObject category = getJsonObject(c, cp, user);
				if (category != null) data.put(category);
			}
			
			final JSONObject result = new JSONObject();
			result.put("period", FormatUtil.formatDate(cp.getCurrentPeriodStartDate(), user) + " - " + FormatUtil.formatDate(cp.getCurrentPeriodEndDate(), user));
			result.put("date", FormatUtil.formatDateInternal(cp.getCurrentPeriodStartDate()));
			result.put("previousPeriod", FormatUtil.formatDate(cp.getPreviousPeriodStartDate(), user) + " - " + FormatUtil.formatDate(cp.getPreviousPeriodEndDate(), user));
			result.put("children", data);
			result.put("success", true);
			return new JsonRepresentation(result);
		}
		catch (NumberFormatException e){
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		}
		catch (CryptoException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		catch (JSONException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		finally {
			sqlSession.close();
		}
	}
	
	private JSONObject getJsonObject(Category category, CategoryPeriod categoryPeriod, final User user) throws JSONException, CryptoException {
		if (category.isDeleted() && !user.isShowDeleted()) return null;
		final JSONObject result = new JSONObject();
		result.put("id", category.getId());
		result.put("icon", "img/folder-open-table.png");
		result.put("dateIso", FormatUtil.formatDateInternal(categoryPeriod.getCurrentPeriodStartDate()));
		result.put("name", CryptoUtil.decryptWrapper(category.getName(), user));
		final StringBuilder sb = new StringBuilder();
		if (category.isDeleted()) sb.append(" text-decoration: line-through;");
		if (!category.isIncome()) sb.append(" color: " + FormatUtil.HTML_RED + ";");
		result.put("nameStyle", sb.toString());
		sb.setLength(0);
		

		final BigDecimal currentAmount = category.getCurrentEntry().getAmount() != null ? category.getCurrentEntry().getAmount() : BigDecimal.ZERO;
		if (currentAmount.compareTo(BigDecimal.ZERO) == 0){
			result.put("currentAmount", FormatUtil.formatCurrency(BIGDECIMAL_ZERO, user));
			result.put("currentAmountStyle", FormatUtil.formatGray());
		}
		else {
			result.put("currentAmount", FormatUtil.formatCurrency(currentAmount, user));
			result.put("currentAmountStyle", (FormatUtil.isRed(category, currentAmount) ? FormatUtil.formatRed() : ""));
		}
		
		final BigDecimal previousAmount = category.getPreviousEntry().getAmount() != null ? category.getPreviousEntry().getAmount() : BigDecimal.ZERO;
		if (previousAmount.compareTo(BigDecimal.ZERO) == 0){
			result.put("previousAmount", FormatUtil.formatCurrency(BIGDECIMAL_ZERO, user));
			result.put("previousAmountStyle", FormatUtil.formatGray());
		}
		else {
			result.put("previousAmount", FormatUtil.formatCurrency(previousAmount, user));
			result.put("previousAmountStyle", (FormatUtil.isRed(category, previousAmount) ? FormatUtil.formatRed() : ""));
		}
		
		final BigDecimal actualAmount = category.getPeriodBalance() == null ? BIGDECIMAL_ZERO : category.getPeriodBalance();
		result.put("actual", FormatUtil.formatCurrency(actualAmount, user));
		result.put("actualStyle", (FormatUtil.isRed(category, actualAmount) ? FormatUtil.formatRed() : ""));

		final BigDecimal difference = (actualAmount.subtract(currentAmount != null ? currentAmount : BigDecimal.ZERO));
		result.put("difference", FormatUtil.formatCurrency(category.isIncome() ? difference : difference.negate(), user));
		result.put("differenceStyle", (FormatUtil.isRed(category.isIncome() ? difference : difference.negate()) ? FormatUtil.formatRed() : ""));

		result.put("parent", category.getParent());
		result.put("deleted", category.isDeleted());

		final List<Category> children = category.getChildren();
		if (children != null){
			Collections.sort(children, new Comparator<Category>() {
				@Override
				public int compare(Category o1, Category o2) {
					if (o1 == null || o2 == null) return 0;
					try {
						return CryptoUtil.decryptWrapper(o1.getName(), user).compareTo(CryptoUtil.decryptWrapper(o2.getName(), user));
					}
					catch (CryptoException e){
						return 0;
					}
				}
			});
			for (Category child : children) {
				final JSONObject c = getJsonObject(child, categoryPeriod, user);
				if (c != null) result.append("children", c);
			}
		}
		if (result.has("children")){
			result.put("expanded", true);
		}
		else {
			result.put("leaf", true);
		}
		return result;
	}

	@Override
	protected Representation post(Representation entity, Variant variant) throws ResourceException {
		final BuddiApplication application = (BuddiApplication) getApplication();
		final SqlSession sqlSession = application.getSqlSessionFactory().openSession();
		final User user = (User) getRequest().getClientInfo().getUser();
		try {
			final JSONObject request = new JSONObject(entity.getText());
			final String action = request.optString("action");
			
			final Category category = new Category(request);
			
			if ("insert".equals(action)){
				ConstraintsChecker.checkInsertCategory(category, user, sqlSession);
				int count = sqlSession.getMapper(Sources.class).insertCategory(user, category);
				if (count != 1) throw new DatabaseException(String.format("Insert failed; expected 1 row, returned %s", count));
			} 
			else if ("delete".equals(action) || "undelete".equals(action)){
				if (sqlSession.getMapper(Sources.class).selectSourceAssociatedCount(user, category) == 0){
					int count = sqlSession.getMapper(Sources.class).deleteSource(user, category);
					if (count != 1) throw new DatabaseException(String.format("Delete failed; expected 1 row, returned %s", count));
				}
				else {
					category.setDeleted("delete".equals(action));
					int count = sqlSession.getMapper(Sources.class).updateSourceDeleted(user, category);
					if (count != 1) throw new DatabaseException(String.format("Delete / undelete failed; expected 1 row, returned %s", count));
				}
			}
			else if ("update".equals(action)){
				ConstraintsChecker.checkUpdateCategory(category, user, sqlSession);
				int count = sqlSession.getMapper(Sources.class).updateCategory(user, category);
				if (count != 1) throw new DatabaseException(String.format("Update failed; expected 1 row, returned %s", count));
			}
			else if ("copyFromPrevious".equals(action)){
				final CategoryPeriods period = CategoryPeriods.valueOf(request.getString("type"));
				final Date currentDate = period.getStartOfBudgetPeriod(FormatUtil.parseDateInternal(request.getString("date")));
				final Date previousDate = period.getBudgetPeriodOffset(currentDate, -1);
				
				final Map<Integer, Entry> previousEntries = sqlSession.getMapper(Entries.class).selectEntries(user, previousDate);
				final Map<Integer, Entry> currentEntries = sqlSession.getMapper(Entries.class).selectEntries(user, currentDate);
				for (Integer categoryId : previousEntries.keySet()) {
					if (previousEntries.get(categoryId).getAmount().compareTo(BigDecimal.ZERO) != 0){
						if (currentEntries.get(categoryId) == null){
							final Entry entry = previousEntries.get(categoryId);
							entry.setDate(currentDate);
							ConstraintsChecker.checkInsertEntry(entry, user, sqlSession);
							int count = sqlSession.getMapper(Entries.class).insertEntry(user, entry);
							if (count != 1) throw new DatabaseException(String.format("Insert failed; expected 1 row, returned %s", count));
						}
						else if (currentEntries.get(categoryId).getAmount().compareTo(BigDecimal.ZERO) == 0){
							final Entry entry = currentEntries.get(categoryId);
							entry.setAmount(previousEntries.get(categoryId).getAmount());
							ConstraintsChecker.checkUpdateEntry(entry, user, sqlSession);
							int count = sqlSession.getMapper(Entries.class).updateEntry(user, entry);
							if (count != 1) throw new DatabaseException(String.format("Update failed; expected 1 row, returned %s", count));
						}
					}
				}
			}
			else {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, LocaleUtil.getTranslation(getRequest()).getString("ACTION_PARAMETER_MUST_BE_SPECIFIED"));
			}
			
			DataUpdater.updateBalances(user, sqlSession, true);
			
			sqlSession.commit();
			final JSONObject result = new JSONObject();
			result.put("success", true);
			return new JsonRepresentation(result);
		}
		catch (DatabaseException e){
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		}
		catch (IOException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		catch (CryptoException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
		catch (JSONException e){
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		}
		finally {
			sqlSession.close();
		}
	}
}
