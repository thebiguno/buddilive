package ca.digitalcave.buddi.web.resource.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
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

public class SourcesResource extends ServerResource {

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
			final JSONObject result = new JSONObject();
			for (Map<String, Object> source : sources) {
				result.accumulate("data", new JSONObject(source));
			}
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
		final BuddiUser user = (BuddiUser) getRequest().getClientInfo().getUser();
		try {
			final JSONObject request = new JSONObject(entity.getText());
			@SuppressWarnings("unchecked")
			final Iterator<String> keys = request.keys();
			Map<String, Object> values = new HashMap<String, Object>();
			while(keys.hasNext()) {
				final String key = keys.next();
				values.put(key, request.get(key));
			}
			values.put("user", user.getId());
			Long result = sqlSession.getMapper(Sources.class).insertSource(values);
			System.out.println(result);
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
		return null;
	}
}
