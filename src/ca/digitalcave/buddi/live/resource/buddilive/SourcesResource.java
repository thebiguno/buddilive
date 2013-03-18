package ca.digitalcave.buddi.live.resource.buddilive;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
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
import ca.digitalcave.buddi.live.model.Account;
import ca.digitalcave.buddi.live.model.AccountType;
import ca.digitalcave.buddi.live.model.Category;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.CryptoUtil;
import ca.digitalcave.buddi.live.util.CryptoUtil.CryptoException;
import ca.digitalcave.buddi.live.util.FormatUtil;

public class SourcesResource extends ServerResource {

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
			final boolean isIncome = getRequest().getResourceRef().getBaseRef().toString().endsWith("from");
			final List<AccountType> accountsByType = sqlSession.getMapper(Sources.class).selectAccountTypes(user);
			//If the data is encrypted, we cannot rely on the DB to sort the account types properly.  Thus we have to.
			final Map<String, AccountType> accountTypeMap = new TreeMap<String, AccountType>();
			for (AccountType accountType : accountsByType) {
				final String key = (accountType.isDebit() ? "1" : "2") + CryptoUtil.decryptWrapper(accountType.getAccountType(), user);
				if (!accountTypeMap.containsKey(key)) {
					accountTypeMap.put(key, accountType);
				}
				else {
					accountTypeMap.get(key).getAccounts().addAll(accountType.getAccounts());
				}
			}
			final List<Category> categories = Category.getHierarchy(sqlSession.getMapper(Sources.class).selectCategories(user, isIncome));
			final JSONObject result = new JSONObject();

			final StringBuilder sb = new StringBuilder();
			
			JSONObject separator = new JSONObject();
			separator.put("text", "--- Accounts ---");
			separator.put("value", "");
			separator.put("style", "color: " + FormatUtil.HTML_GRAY + ";");
			result.append("data", separator);
			
			for (String key : accountTypeMap.keySet()) {
				AccountType at = accountTypeMap.get(key);
				if (!at.isDeleted() || user.isShowDeleted()){
					final JSONObject accountType = new JSONObject();
					accountType.put("value", "");
					accountType.put("text", CryptoUtil.decryptWrapper(at.getAccountType(), user));
					if (at.isDeleted()) sb.append(" text-decoration: line-through;");
					sb.append(" color: " + (at.isDebit() ? FormatUtil.HTML_GRAY : FormatUtil.HTML_DISABLED_RED) + ";");
					accountType.put("style", sb.toString());
					sb.setLength(0);
					result.append("data", accountType);

					for (Account a : at.getAccounts() != null ? at.getAccounts() : new ArrayList<Account>()) {
						if (!a.isDeleted() || user.isShowDeleted()){
							final JSONObject account = new JSONObject();
							account.put("value", a.getId());
							account.put("text", StringUtils.repeat("\u00a0", 2) + CryptoUtil.decryptWrapper(a.getName(), user));
							account.put("search", CryptoUtil.decryptWrapper(a.getName(), user));
							if (a.isDeleted()) sb.append(" text-decoration: line-through;");
							if (!a.isDebit()) sb.append(" color: " + FormatUtil.HTML_RED + ";");
							account.put("style", sb.toString());
							sb.setLength(0);
							result.append("data", account);
						}
					}
				}
			}
			
			separator = new JSONObject();
			separator.put("text", "--- Budget Categories ---");
			separator.put("value", "");
			separator.put("style", "color: " + FormatUtil.HTML_GRAY + ";");
			result.append("data", separator);
			
			insertCategories(result, categories, user, 0);
			
			result.put("success", true);
			return new JsonRepresentation(result);
		}
		catch (JSONException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		catch (CryptoException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		finally {
			sqlSession.close();
		}
	}
	
	private void insertCategories(JSONObject result, List<Category> categories, User user, int depth) throws JSONException, CryptoException {
		final StringBuilder sb = new StringBuilder();
		for (Category c : categories) {
			if (!c.isDeleted() || user.isShowDeleted()){
				final JSONObject category = new JSONObject();
				category.put("value", c.getId());
				category.put("text", StringUtils.repeat("\u00a0", depth * 2) + CryptoUtil.decryptWrapper(c.getName(), user));
				category.put("search", CryptoUtil.decryptWrapper(c.getName(), user));
				if (c.isDeleted()) sb.append(" text-decoration: line-through;");
				if (!c.isIncome()) sb.append(" color: " + FormatUtil.HTML_RED + ";");
				category.put("style", sb.toString());
				sb.setLength(0);
				result.append("data", category);
				if (c.getChildren() != null) insertCategories(result, c.getChildren(), user, depth + 1);
			}
		}
	}
}
