package ca.digitalcave.buddi.live.resource.gui;

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

import ca.digitalcave.buddi.live.BuddiApplication;
import ca.digitalcave.buddi.live.db.Sources;
import ca.digitalcave.buddi.live.model.Account;
import ca.digitalcave.buddi.live.model.Category;
import ca.digitalcave.buddi.live.model.User;
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
			final List<Account> accounts = sqlSession.getMapper(Sources.class).selectAccounts(user, false);
			final List<Category> categories = sqlSession.getMapper(Sources.class).selectCategories(user, isIncome);
			
			final JSONArray data = new JSONArray();
			final StringBuilder sb = new StringBuilder();
			
			JSONObject separator = new JSONObject();
			separator.put("text", "--- Accounts ---");
			separator.put("value", "");
			separator.put("style", "color: " + FormatUtil.HTML_GRAY + ";");
			data.put(separator);
			
			for (Account a : accounts) {
				final JSONObject account = new JSONObject();
				account.put("value", a.getId());
				account.put("text", a.getName());
				if (a.isDeleted()) sb.append(" text-decoration: line-through;");
				if (!a.isDebit()) sb.append(" color: " + FormatUtil.HTML_RED + ";");
				account.put("style", sb.toString());
				sb.setLength(0);
				data.put(account);
			}
			
			separator = new JSONObject();
			separator.put("text", "--- Budget Categories ---");
			separator.put("value", "");
			separator.put("style", "color: " + FormatUtil.HTML_GRAY + ";");
			data.put(separator);
			
			for (Category c : categories) {
				final JSONObject category = new JSONObject();
				category.put("value", c.getId());
				category.put("text", c.getName());
				if (c.isDeleted()) sb.append(" text-decoration: line-through;");
				if (!c.isIncome()) sb.append(" color: " + FormatUtil.HTML_RED + ";");
				category.put("style", sb.toString());
				sb.setLength(0);
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
