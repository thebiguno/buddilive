package ca.digitalcave.buddi.web.resource.gui;

import java.io.IOException;
import java.util.List;

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

import ca.digitalcave.buddi.web.BuddiApplication;
import ca.digitalcave.buddi.web.db.Sources;
import ca.digitalcave.buddi.web.db.Transactions;
import ca.digitalcave.buddi.web.db.util.ConstraintsChecker;
import ca.digitalcave.buddi.web.db.util.DatabaseException;
import ca.digitalcave.buddi.web.model.Account;
import ca.digitalcave.buddi.web.model.Split;
import ca.digitalcave.buddi.web.model.Transaction;
import ca.digitalcave.buddi.web.model.User;

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
			final List<String> descriptions = sqlSession.getMapper(Transactions.class).selectDescriptions(user);
			
			final JSONArray data = new JSONArray();
//			for (Transaction t : transactions) {
//				final JSONObject transaction = new JSONObject();
//				transaction.put("id", t.getId());
//				transaction.put("date", t.getDate());
//				transaction.put("description", t.getDescription());
//				transaction.put("number", t.getNumber());
//				transaction.put("deleted", t.isDeleted());
//				final JSONArray splits = new JSONArray();
//				for (Split s : t.getSplits()) {
//					final JSONObject split = new JSONObject();
//					split.put("id", s.getId());
//					split.put("name", s.getAmount());
//					split.put("from", s.getFromSource());
//					split.put("to", s.getToSource());
//					split.put("memo", s.getMemo());
//					splits.put(split);
//				}
//				transaction.put("splits", splits);
//				data.put(transaction);
//			}
			
			final JSONObject result = new JSONObject();
			result.put("data", data);
			result.put("success", true);
			return new JsonRepresentation(result);
		}
		catch (JSONException e){
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
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "An action parameter must be specified.");
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
		catch (JSONException e){
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		}
		finally {
			sqlSession.close();
		}
	}
}
