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
import ca.digitalcave.buddi.web.db.util.ConstraintsChecker;
import ca.digitalcave.buddi.web.db.util.DatabaseException;
import ca.digitalcave.buddi.web.model.Account;
import ca.digitalcave.buddi.web.model.AccountType;
import ca.digitalcave.buddi.web.model.User;
import ca.digitalcave.buddi.web.util.FormatUtil;

public class CategoriesResource extends ServerResource {

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
			final List<AccountType> accountsByType = sqlSession.getMapper(Sources.class).selectAccountTypes(user, true);
			
			final JSONArray data = new JSONArray();
			for (AccountType t : accountsByType) {
				final JSONObject type = new JSONObject();
				type.put("name", t.getAccountType());
				type.put("expanded", true);
				type.put("debit", "D".equals(t.getType()));
				type.put("nodeType", "type");
				type.put("icon", "img/folder-open-table.png");
				final JSONArray accounts = new JSONArray();
				for (Account a : t.getAccounts()) {
					final JSONObject account = new JSONObject();
					account.put("id", a.getId());
					account.put("name", a.getName());
					account.put("balance", FormatUtil.formatCurrency(a.getBalance()));
					account.put("type", a.getType());
					account.put("accountType", a.getAccountType());
					account.put("startBalance", FormatUtil.formatCurrency(a.getStartBalance()));
					account.put("debit", "D".equals(a.getType()));
					account.put("deleted", a.isDeleted());
					account.put("leaf", true);
					account.put("nodeType", "account");
					account.put("icon", "img/table.png");
					accounts.put(account);
				}
				type.put("children", accounts);
				data.put(type);
			}
			
			final JSONObject result = new JSONObject();
			result.put("children", data);
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
