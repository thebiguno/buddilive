package ca.digitalcave.buddi.web.resource.gui;

import java.util.ArrayList;
import java.util.Collections;
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

import ca.digitalcave.buddi.web.BuddiApplication;
import ca.digitalcave.buddi.web.db.Sources;
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
			final List<Source> allAccounts = sqlSession.getMapper(Sources.class).selectAccounts(user);
			final Map<String, List<Source>> accountsByType = new HashMap<String, List<Source>>();
			for (Source account : allAccounts) {
				if (accountsByType.get(account.getAccountType()) == null) accountsByType.put(account.getAccountType(), new ArrayList<Source>());
				accountsByType.get(account.getAccountType()).add(account);
			}
			
			final JSONArray data = new JSONArray();
			final List<String> types = new ArrayList<String>(accountsByType.keySet());
			Collections.sort(types);
			for (String t : types) {
				final JSONObject type = new JSONObject();
				type.put("name", t);
				type.put("expanded", true);
				type.put("type", "type");
				type.put("icon", "img/folder-open-table.png");
				final JSONArray accounts = new JSONArray();
				for (Source a : accountsByType.get(t)) {
					final JSONObject account = new JSONObject();
					account.put("name", a.getName());
					account.put("balance", 0);
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
