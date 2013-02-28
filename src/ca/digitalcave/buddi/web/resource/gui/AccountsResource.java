package ca.digitalcave.buddi.web.resource.gui;

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
import ca.digitalcave.buddi.web.model.AccountType;
import ca.digitalcave.buddi.web.model.Source;
import ca.digitalcave.buddi.web.model.User;

public class AccountsResource extends ServerResource {

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
			final List<AccountType> accountsByType = sqlSession.getMapper(Sources.class).selectAccountsWithBalancesByType(user);
			
			final JSONArray data = new JSONArray();
			for (AccountType t : accountsByType) {
				final JSONObject type = new JSONObject();
				type.put("name", t.getType());
				type.put("expanded", true);
				type.put("type", "type");
				type.put("icon", "img/folder-open-table.png");
				final JSONArray accounts = new JSONArray();
				for (Source a : t.getAccounts()) {
					final JSONObject account = new JSONObject();
					account.put("name", a.getName());
					account.put("balance", a.getBalance());
					account.put("leaf", true);
					account.put("type", "account");
					account.put("debit", "D".equals(a.getType()));
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
}
