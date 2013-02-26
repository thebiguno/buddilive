package ca.digitalcave.buddi.web.resource.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import ca.digitalcave.buddi.web.security.BuddiUser;

public class SourcesDataResource extends ServerResource {

	@Override
	protected void doInit() throws ResourceException {
		getVariants().add(new Variant(MediaType.APPLICATION_JSON));
	}

	@Override
	protected Representation get(Variant variant) throws ResourceException {
		final BuddiApplication application = (BuddiApplication) getApplication();
		final SqlSession sqlSession = application.getSqlSessionFactory().openSession(true);
		final BuddiUser user = (BuddiUser) getRequest().getClientInfo().getUser();
		try {
			final List<Map<String, Object>> sources = sqlSession.getMapper(Sources.class).selectSource(user.getId(), (Long) null);
			final JSONArray result = new JSONArray();
			for (Map<String, Object> source : sources) {
				result.put(new JSONObject(source));
			}
			return new JsonRepresentation(result);
		}
		finally {
			sqlSession.close();
		}
	}

	@Override
	protected Representation post(Representation entity, Variant variant) throws ResourceException {
		final BuddiApplication application = (BuddiApplication) getApplication();
		final SqlSession sqlSession = application.getSqlSessionFactory().openSession();
		final BuddiUser user = (BuddiUser) getRequest().getClientInfo().getUser();
		try {
			final JSONArray request = new JSONArray(entity.getText());
			for (int i = 0; i < request.length(); i++) {
				final JSONObject source = request.getJSONObject(i);
				final Map<String, Object> values = new HashMap<String, Object>();
				values.put("userId", user.getId());
				values.put("uuid", source.has("uuid") ? source.getString("uuid") : UUID.randomUUID().toString());
				values.put("name", source.getString("name"));
				values.put("startDate", source.get("startDate"));
				values.put("deleted", source.has("delete") ? source.getString("deleted") : "N");
				values.put("type", source.getString("type"));
				values.put("startBalance", source.getInt("startBalance"));
				values.put("periodType", source.optString("periodType"));
				Integer count = sqlSession.getMapper(Sources.class).insertSource(values);
				if (count == 1){
					sqlSession.commit();
				}
				else {
					throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
				}
			}
			
			final JSONObject result = new JSONObject();
			result.put("success", true);
			return new JsonRepresentation(result);
		}
		catch (IOException e){
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		}
		catch (JSONException e){
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		}
		finally {
			sqlSession.close();
		}
	}
}
