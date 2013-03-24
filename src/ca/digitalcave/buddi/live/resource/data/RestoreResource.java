package ca.digitalcave.buddi.live.resource.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
import ca.digitalcave.buddi.live.db.ScheduledTransactions;
import ca.digitalcave.buddi.live.db.Sources;
import ca.digitalcave.buddi.live.db.Transactions;
import ca.digitalcave.buddi.live.db.util.ConstraintsChecker;
import ca.digitalcave.buddi.live.db.util.DataUpdater;
import ca.digitalcave.buddi.live.db.util.DatabaseException;
import ca.digitalcave.buddi.live.model.Account;
import ca.digitalcave.buddi.live.model.Category;
import ca.digitalcave.buddi.live.model.Entry;
import ca.digitalcave.buddi.live.model.ScheduledTransaction;
import ca.digitalcave.buddi.live.model.Split;
import ca.digitalcave.buddi.live.model.Transaction;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.CryptoUtil.CryptoException;
import ca.digitalcave.buddi.live.util.FormatUtil;

public class RestoreResource extends ServerResource {

	@Override
	protected void doInit() throws ResourceException {
		getVariants().add(new Variant(MediaType.APPLICATION_JSON));
	}

	@Override
	protected Representation post(Representation entity, Variant variant) throws ResourceException {
		final BuddiApplication application = (BuddiApplication) getApplication();
		final SqlSession sqlSession = application.getSqlSessionFactory().openSession();
		final User user = (User) getRequest().getClientInfo().getUser();
		final Map<String, Integer> sourceIDsByUUID = new HashMap<String, Integer>();

		try {
			final JSONObject request = new JSONObject(entity.getText());

			restoreAccounts(request, user, sqlSession, sourceIDsByUUID);

			restoreCategories(request, user, sqlSession, sourceIDsByUUID);

			restoreEntries(request, user, sqlSession, sourceIDsByUUID);

			restoreTransactions(request, user, sqlSession, sourceIDsByUUID);
			
			restoreScheduledTransactions(request, user, sqlSession, sourceIDsByUUID);

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
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		catch (JSONException e){
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		}
		catch (Throwable e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		finally {
			sqlSession.close();
		}
	}

	private void restoreAccounts(JSONObject jsonObject, User user, SqlSession sqlSession, Map<String, Integer> sourceIDsByUUID) throws DatabaseException, JSONException, CryptoException {
		final JSONArray accounts = jsonObject.getJSONArray("accounts");
		if (accounts != null){
			for (int i = 0; i < accounts.length(); i++){
				final JSONObject a = accounts.getJSONObject(i);
				//We only insert if the specified UUID is not already there.
				final Account existing = sqlSession.getMapper(Sources.class).selectAccount(user, a.getString("uuid"));
				if (existing == null){
					final Account account = new Account();
					account.setUuid(a.getString("uuid"));
					account.setName(a.getString("name"));
					account.setStartDate(FormatUtil.parseDateInternal(a.getString("startDate")));
					account.setDeleted(a.optBoolean("deleted", false));
					account.setType(a.getString("type"));
					account.setStartBalance(FormatUtil.parseCurrency(a.getString("startBalance")));
					account.setAccountType(a.getString("accountType"));

					ConstraintsChecker.checkInsertAccount(account, user, sqlSession);
					int count = sqlSession.getMapper(Sources.class).insertAccount(user, account);
					if (count != 1) throw new DatabaseException(String.format("Insert failed; expected 1 row, returned %s", count));
					sourceIDsByUUID.put(account.getUuid(), account.getId());
				}
				else {
					sourceIDsByUUID.put(existing.getUuid(), existing.getId());
				}
			}
		}
	}

	private void restoreCategories(JSONObject jsonObject, User user, SqlSession sqlSession, Map<String, Integer> sourceIDsByUUID) throws DatabaseException, JSONException, CryptoException {
		final JSONArray categories = jsonObject.getJSONArray("categories");
		if (categories != null){
			for (int i = 0; i < categories.length(); i++){
				final JSONObject c = categories.getJSONObject(i);
				//We only insert if the specified UUID is not already there.
				final Category existing = sqlSession.getMapper(Sources.class).selectCategory(user, c.getString("uuid"));
				if (existing == null){
					final Category category = new Category();
					category.setUuid(c.getString("uuid"));
					category.setName(c.getString("name"));
					category.setDeleted(c.optBoolean("deleted", false));
					category.setType(c.getString("type"));
					//We support either implied parentage, via recursive "categories" objects, or explicit
					// parentage, via the 'parent' entry.  Implied parentage is recommended, as it will 
					// ensure that parents are loaded before their children are.  If you use explicit
					// parentage, you need to make sure the parent category is loaded before its children are, 
					// or else the parentage for a given node may be lost.
					final Integer impliedParent = sourceIDsByUUID.get(jsonObject.optString("uuid"));
					final Integer explicitParent = sourceIDsByUUID.get(c.optString("parent"));
					category.setParent(impliedParent != null ? impliedParent : explicitParent);
					category.setPeriodType(c.getString("periodType"));

					ConstraintsChecker.checkInsertCategory(category, user, sqlSession);
					int count = sqlSession.getMapper(Sources.class).insertCategory(user, category);
					if (count != 1) throw new DatabaseException(String.format("Insert failed; expected 1 row, returned %s", count));
					sourceIDsByUUID.put(category.getUuid(), category.getId());
				}
				else {
					sourceIDsByUUID.put(existing.getUuid(), existing.getId());
				}
				if (c.has("categories")){
					restoreCategories(c, user, sqlSession, sourceIDsByUUID);
				}
			}
		}
	}

	private void restoreEntries(JSONObject jsonObject, User user, SqlSession sqlSession, Map<String, Integer> sourceIDsByUUID) throws DatabaseException, JSONException {
		final JSONArray entries = jsonObject.optJSONArray("entries");
		if (entries != null){
			for (int i = 0; i < entries.length(); i++){
				final JSONObject e = entries.getJSONObject(i);
				final Entry entry = new Entry();
				final Integer categoryId = sourceIDsByUUID.get(e.getString("category"));
				if (categoryId == null){
					throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Could not find category UUID " + e.getString("category") + " for budget entry " + e.getString("date") + " / " + e.getString("amount"));
				}
				entry.setCategoryId(categoryId);
				entry.setDate(FormatUtil.parseDateInternal(e.getString("date")));
				entry.setAmount(FormatUtil.parseCurrency(e.getString("amount")));
				final Entry existingEntry = sqlSession.getMapper(Entries.class).selectEntry(user, entry);
				if (existingEntry == null){
					//New entry
					ConstraintsChecker.checkInsertEntry(entry, user, sqlSession);
					int count = sqlSession.getMapper(Entries.class).insertEntry(user, entry);
					if (count != 1) throw new DatabaseException(String.format("Insert failed; expected 1 row, returned %s", count));
				}
				else {
					//Update entry
					ConstraintsChecker.checkUpdateEntry(entry, user, sqlSession);
					int count = sqlSession.getMapper(Entries.class).updateEntry(user, entry);
					if (count != 1) throw new DatabaseException(String.format("Update failed; expected 1 row, returned %s", count));
				}
			}
		}
	}

	private void restoreTransactions(JSONObject jsonObject, User user, SqlSession sqlSession, Map<String, Integer> sourceIDsByUUID) throws DatabaseException, JSONException, CryptoException {
		final JSONArray transactions = jsonObject.optJSONArray("transactions");
		if (transactions != null){
			for (int i = 0; i < transactions.length(); i++) {
				final JSONObject t = transactions.getJSONObject(i);
				//We only insert if the specified UUID is not already there.
				if (sqlSession.getMapper(Transactions.class).selectTransactionCount(user, t.getString("uuid")) == 0){
					final Transaction transaction = new Transaction();
					transaction.setUuid(t.getString("uuid"));
					transaction.setDescription(t.getString("description"));
					transaction.setNumber(t.optString("number", null));
					transaction.setDate(FormatUtil.parseDateInternal(t.getString("date")));
					transaction.setDeleted(t.optBoolean("deleted", false));
					transaction.setSplits(new ArrayList<Split>());
					final JSONArray splits = t.getJSONArray("splits");
					for (int j = 0; j < splits.length(); j++) {
						final JSONObject s = splits.getJSONObject(j);
						final Split split = new Split();
						final Integer fromSource = sourceIDsByUUID.get(s.getString("from"));
						final Integer toSource = sourceIDsByUUID.get(s.getString("to"));
						if (fromSource == null){
							throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Could not find transaction from source UUID " + s.getString("from") + " for split #" + j + " in transaction " + t.getString("uuid"));
						}
						split.setAmount(FormatUtil.parseCurrency(s.getString("amount")));
						split.setFromSource(fromSource);
						split.setToSource(toSource);
						split.setMemo(s.optString("memo", ""));
						transaction.getSplits().add(split);
					}

					ConstraintsChecker.checkInsertTransaction(transaction, user, sqlSession);
					int count = sqlSession.getMapper(Transactions.class).insertTransaction(user, transaction);
					if (count != 1) throw new DatabaseException(String.format("Insert failed; expected 1 row, returned %s", count));
					for (Split split : transaction.getSplits()) {
						split.setTransactionId(transaction.getId());
						count = sqlSession.getMapper(Transactions.class).insertSplit(user, split);
						if (count != 1) throw new DatabaseException(String.format("Insert failed; expected 1 row, returned %s", count));
					}
				}
			}
		}
	}
	
	private void restoreScheduledTransactions(JSONObject jsonObject, User user, SqlSession sqlSession, Map<String, Integer> sourceIDsByUUID) throws DatabaseException, JSONException, CryptoException {
		final JSONArray transactions = jsonObject.optJSONArray("scheduledTransactions");
		if (transactions != null){
			for (int i = 0; i < transactions.length(); i++) {
				final JSONObject t = transactions.getJSONObject(i);
				//We only insert if the specified UUID is not already there.
				if (sqlSession.getMapper(ScheduledTransactions.class).selectScheduledTransactionCount(user, t.getString("uuid")) == 0){
					final ScheduledTransaction transaction = new ScheduledTransaction();
					transaction.setUuid(t.getString("uuid"));
					transaction.setDescription(t.getString("description"));
					transaction.setNumber(t.optString("number", null));
					transaction.setScheduleName(t.getString("scheduleName"));
					transaction.setScheduleDay(t.getInt("scheduleDay"));
					transaction.setScheduleWeek(t.getInt("scheduleWeek"));
					transaction.setScheduleMonth(t.getInt("scheduleMonth"));
					transaction.setFrequencyType(t.getString("frequencyType"));
					transaction.setStartDate(FormatUtil.parseDateInternal(t.getString("startDate")));
					transaction.setEndDate(FormatUtil.parseDateInternal(t.optString("endDate", null)));
					transaction.setLastCreatedDate(FormatUtil.parseDateInternal(t.optString("lastCreatedDate", null)));
					transaction.setMessage(t.optString("message", null));
					transaction.setSplits(new ArrayList<Split>());
					final JSONArray splits = t.getJSONArray("splits");
					for (int j = 0; j < splits.length(); j++) {
						final JSONObject s = splits.getJSONObject(j);
						final Split split = new Split();
						final Integer fromSource = sourceIDsByUUID.get(s.getString("from"));
						if (fromSource == null){
							throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Could not find transaction from source UUID " + s.getString("from") + " for split #" + j + " in transaction " + t.getString("uuid"));
						}
						final Integer toSource = sourceIDsByUUID.get(s.getString("to"));
						if (toSource == null){
							throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Could not find transaction from source UUID " + s.getString("to") + " for split #" + j + " in transaction " + t.getString("uuid"));
						}
						split.setAmount(FormatUtil.parseCurrency(s.getString("amount")));
						split.setFromSource(fromSource);
						split.setToSource(toSource);
						split.setMemo(s.optString("memo", null));
						transaction.getSplits().add(split);
					}

					ConstraintsChecker.checkInsertScheduledTransaction(transaction, user, sqlSession);
					int count = sqlSession.getMapper(ScheduledTransactions.class).insertScheduledTransaction(user, transaction);
					if (count != 1) throw new DatabaseException(String.format("Insert failed; expected 1 row, returned %s", count));
					for (Split split : transaction.getSplits()) {
						split.setTransactionId(transaction.getId());
						count = sqlSession.getMapper(ScheduledTransactions.class).insertScheduledSplit(user, split);
						if (count != 1) throw new DatabaseException(String.format("Insert failed; expected 1 row, returned %s", count));
					}
				}
			}
		}
	}
}
