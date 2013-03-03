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
import ca.digitalcave.buddi.web.model.Account;
import ca.digitalcave.buddi.web.model.Category;
import ca.digitalcave.buddi.web.model.User;

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
			final List<Account> accounts = sqlSession.getMapper(Sources.class).selectAccounts(user, false);
			final List<Category> categories = sqlSession.getMapper(Sources.class).selectCategories(user, isIncome);
			
			final JSONArray data = new JSONArray();
			
			for (Account a : accounts) {
				final JSONObject account = new JSONObject();
				account.put("value", a.getId());
				account.put("text", a.getName());
				account.put("deleted", a.isDeleted());
				account.put("red", "C".equals(a.getType()));
				data.put(account);
			}
			
			JSONObject separator = new JSONObject();
			separator.put("text", "-----");
			data.put(separator);
			
			for (Category c : categories) {
				final JSONObject category = new JSONObject();
				category.put("value", c.getId());
				category.put("text", c.getName());
				category.put("deleted", c.isDeleted());
				category.put("red", "E".equals(c.getType()));
				data.put(category);
			}
			
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
}
