package ca.digitalcave.buddi.live.resource.buddilive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
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
import ca.digitalcave.buddi.live.util.FormatUtil;
import ca.digitalcave.buddi.live.util.LocaleUtil;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;

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
			for (AccountType accountType : accountTypeMap.values()) {
				sortAccountsByName(accountType.getAccounts(), user);
			}
			sortCategoriesByName(categories, user);
			final JSONObject result = new JSONObject();

			final StringBuilder sb = new StringBuilder();
			
			JSONObject separator = new JSONObject();
			separator.put("text", "--- " + LocaleUtil.getTranslation(getRequest()).getString("SOURCE_COMBOBOX_SECTION_ACCOUNTS") + " ---");
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
							account.put("text", StringUtils.repeat("\u00a0", 2) + CryptoUtil.decryptWrapper(a.getName(), user).replaceAll(" ", "\u00a0"));
							if (a.isDeleted()) sb.append(" text-decoration: line-through;");
							if (!a.isDebit()) sb.append(" color: " + FormatUtil.HTML_RED + ";");
							account.put("style", sb.toString());
							sb.setLength(0);
							account.put("type", a.getType());
							result.append("data", account);
						}
					}
				}
			}
			
			separator = new JSONObject();
			separator.put("text", "--- " + LocaleUtil.getTranslation(getRequest()).getString("SOURCE_COMBOBOX_SECTION_BUDGET_CATEGORIES") + " ---");
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

	private void sortAccountsByName(List<Account> accounts, User user) {
		if (accounts == null || accounts.size() < 2) return;
		Collections.sort(accounts, Comparator.comparing(a -> getSortName(a.getName(), user), String.CASE_INSENSITIVE_ORDER));
	}

	private void sortCategoriesByName(List<Category> categories, User user) {
		if (categories == null || categories.isEmpty()) return;
		Collections.sort(categories, Comparator.comparing(c -> getSortName(c.getName(), user), String.CASE_INSENSITIVE_ORDER));
		for (Category category : categories) {
			sortCategoriesByName(category.getChildren(), user);
		}
	}

	private String getSortName(String encryptedValue, User user) {
		try {
			final String decrypted = CryptoUtil.decryptWrapper(encryptedValue, user);
			return decrypted == null ? "" : decrypted;
		}
		catch (CryptoException e) {
			return encryptedValue == null ? "" : encryptedValue;
		}
	}
	
	private void insertCategories(JSONObject result, List<Category> categories, User user, int depth) throws JSONException, CryptoException {
		final StringBuilder sb = new StringBuilder();
		for (Category c : categories) {
			if (!c.isDeleted() || user.isShowDeleted()){
				final JSONObject category = new JSONObject();
				category.put("value", c.getId());
				category.put("type", c.getType());
				category.put("text", StringUtils.repeat("\u00a0", depth * 2) + CryptoUtil.decryptWrapper(c.getName(), user).replaceAll(" ", "\u00a0"));
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
