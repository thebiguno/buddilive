package ca.digitalcave.buddi.live.resource.buddilive;

import java.util.HashSet;
import java.util.Set;

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
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.model.CategoryPeriod.CategoryPeriods;

public class PeriodsResource extends ServerResource {

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
			final Set<String> categoryPeriods = new HashSet<String>(sqlSession.getMapper(Sources.class).selectCategoryPeriods(user));
			
			final JSONArray data = new JSONArray();
			for (CategoryPeriods cp : CategoryPeriods.values()) {
				JSONObject item = new JSONObject();
				item.put("value", cp.toString());
				item.put("visible", categoryPeriods.contains(cp.toString()));
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
		finally {
			sqlSession.close();
		}
	}
}
