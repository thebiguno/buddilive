package ca.digitalcave.buddi.live.resource.data;

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
import ca.digitalcave.buddi.live.db.Entries;
import ca.digitalcave.buddi.live.db.Sources;
import ca.digitalcave.buddi.live.db.Transactions;
import ca.digitalcave.buddi.live.model.Account;
import ca.digitalcave.buddi.live.model.Category;
import ca.digitalcave.buddi.live.model.Entry;
import ca.digitalcave.buddi.live.model.Split;
import ca.digitalcave.buddi.live.model.Transaction;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.FormatUtil;

public class BackupResource extends ServerResource {

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
			final JSONObject result = new JSONObject();
			final List<Account> accounts = sqlSession.getMapper(Sources.class).selectAccounts(user, false);
			final List<Category> categories = Category.getHierarchy(sqlSession.getMapper(Sources.class).selectCategories(user));
			final List<Transaction> transactions = sqlSession.getMapper(Transactions.class).selectTransactions(user);
			final List<Entry> entries = sqlSession.getMapper(Entries.class).selectEntries(user);
			final Map<Integer, String> sourceUUIDsById = new HashMap<Integer, String>();
			
			for (Account account : accounts) {
				addAccount(result, account, sourceUUIDsById);
			}
			for (Category category : categories) {
				addCategory(result, category, sourceUUIDsById);
			}
			for (Entry entry : entries) {
				addEntry(result, entry, sourceUUIDsById);
			}
			for (Transaction transaction : transactions) {
				addTransaction(result, transaction, sourceUUIDsById);
			}
			
			return new JsonRepresentation(result);
		}
		catch (JSONException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		finally {
			sqlSession.close();
		}
	}
	
	private void addAccount(JSONObject result, Account account, Map<Integer, String> sourceUUIDsById) throws JSONException {
		sourceUUIDsById.put(account.getId(), account.getUuid());
		final JSONObject a = new JSONObject();
		a.put("uuid", account.getUuid());
		a.put("name", account.getName());
		a.put("startDate", FormatUtil.formatDate((Date) account.getStartDate()));
		if (account.isDeleted()) a.put("deleted", account.isDeleted());
		a.put("type", account.getType());
		a.put("startBalance", FormatUtil.formatCurrency(account.getStartBalance()));
		a.put("accountType", account.getAccountType());
		result.append("accounts", a);
	}
	
	private void addCategory(JSONObject result, Category category, Map<Integer, String> sourceUUIDsById) throws JSONException {
		sourceUUIDsById.put(category.getId(), category.getUuid());
		final JSONObject c = new JSONObject();
		c.put("uuid", category.getUuid());
		c.put("name", category.getName());
		if (category.isDeleted()) c.put("deleted", category.isDeleted());
		c.put("type", category.getType());
		c.put("parent", sourceUUIDsById.get(category.getParent()));
		c.put("periodType", category.getPeriodType());
		if (category.getChildren() != null){
			for (Category child : category.getChildren()) {
				addCategory(c, child, sourceUUIDsById);
			}
		}
		result.append("categories", c);
	}
	
	private void addEntry(JSONObject result, Entry entry, Map<Integer, String> sourceUUIDsById) throws JSONException {
		final JSONObject e = new JSONObject();
		e.put("date", FormatUtil.formatDate((Date) entry.getDate()));
		e.put("category", sourceUUIDsById.get(entry.getCategoryId()));
		e.put("amount", FormatUtil.formatCurrency(entry.getAmount()));
		result.append("entries", e);
	}
	
	private void addTransaction(JSONObject result, Transaction transaction, Map<Integer, String> sourceUUIDsById) throws JSONException {
		final JSONObject t = new JSONObject();
		t.put("uuid", transaction.getUuid());
		t.put("description", transaction.getDescription());
		t.put("number", transaction.getNumber());
		t.put("date", FormatUtil.formatDate((Date) transaction.getDate()));
		if (transaction.isDeleted()) t.put("deleted", transaction.isDeleted());
		if (transaction.getSplits() != null){
			for (Split split : transaction.getSplits()) {
				final JSONObject s = new JSONObject();
				s.put("amount", FormatUtil.formatCurrency(split.getAmount()));
				s.put("from", sourceUUIDsById.get(split.getFromSource()));
				s.put("to", sourceUUIDsById.get(split.getToSource()));
				s.put("memo", split.getMemo());
				t.append("splits", s);
			}
		}
		result.append("transactions", t);
	}
}
