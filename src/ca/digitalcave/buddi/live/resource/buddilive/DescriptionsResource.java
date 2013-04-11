package ca.digitalcave.buddi.live.resource.buddilive;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import ca.digitalcave.buddi.live.db.util.ConstraintsChecker;
import ca.digitalcave.buddi.live.db.util.DatabaseException;
import ca.digitalcave.buddi.live.model.Account;
import ca.digitalcave.buddi.live.model.Split;
import ca.digitalcave.buddi.live.model.Transaction;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.CryptoUtil;
import ca.digitalcave.buddi.live.util.CryptoUtil.CryptoException;

public class DescriptionsResource extends ServerResource {

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
			final Map<String, Transaction> transactionsByDescription = new TreeMap<String, Transaction>();
			final List<Transaction> transactions = sqlSession.getMapper(Transactions.class).selectDescriptions(user);
			for (Transaction transaction : transactions) {
				final String description = CryptoUtil.decryptWrapper(transaction.getDescription(), user);
				if (transactionsByDescription.get(description) == null){
					transactionsByDescription.put(description, transaction);
				}
			}
			
			final JSONArray data = new JSONArray();
			for (String description : transactionsByDescription.keySet()) {
				final JSONObject item = new JSONObject();
				final Transaction t = transactionsByDescription.get(description);
				item.put("value", description);
				final JSONObject transaction = new JSONObject();
				transaction.put("description", description);
				transaction.put("number", CryptoUtil.decryptWrapper(t.getNumber(), user));
				for (Split s : t.getSplits() != null ? t.getSplits() : new ArrayList<Split>()) {
					final JSONObject split = new JSONObject();
					split.put("amount", s.getAmount().toPlainString());
					split.put("fromId", s.getFromSource());
					split.put("toId", s.getToSource());
					transaction.append("splits", split);
				}
				item.put("transaction", transaction);
				data.put(item);
			}
			
			final JSONObject result = new JSONObject();
			result.put("data", data);
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

	@Override
	protected Representation post(Representation entity, Variant variant) throws ResourceException {
		final BuddiApplication application = (BuddiApplication) getApplication();
		final SqlSession sqlSession = application.getSqlSessionFactory().openSession();
		final User user = (User) getRequest().getClientInfo().getUser();
		try {
			final JSONObject request = new JSONObject(entity.getText());
			final String action = request.optString("action");
			
			final Account account = new Account(request);
			
			if ("insert".equals(action)){
				ConstraintsChecker.checkInsertAccount(account, user, sqlSession);
				int count = sqlSession.getMapper(Sources.class).insertAccount(user, account);
				if (count != 1) throw new DatabaseException(String.format("Insert failed; expected 1 row, returned %s", count));
			} 
			else if ("delete".equals(action) || "undelete".equals(action)){
				account.setDeleted("delete".equals(action));
				int count = sqlSession.getMapper(Sources.class).updateSourceDeleted(user, account);
				if (count != 1) throw new DatabaseException(String.format("Delete / undelete failed; expected 1 row, returned %s", count));
			}
			else if ("update".equals(action)){
				ConstraintsChecker.checkUpdateAccount(account, user, sqlSession);
				int count = sqlSession.getMapper(Sources.class).updateAccount(user, account);
				if (count != 1) throw new DatabaseException(String.format("Update failed; expected 1 row, returned %s", count));
			}
			else {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, user.getTranslation().getString("ACTION_PARAMETER_MUST_BE_SPECIFIED"));
			}
			
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
		finally {
			sqlSession.close();
		}
	}
}
