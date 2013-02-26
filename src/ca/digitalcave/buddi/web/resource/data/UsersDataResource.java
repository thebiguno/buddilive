package ca.digitalcave.buddi.web.resource.data;

import java.io.IOException;

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
import ca.digitalcave.buddi.web.db.Users;
import ca.digitalcave.buddi.web.model.User;
import ca.digitalcave.buddi.web.security.BuddiUser;

public class UsersDataResource extends ServerResource {

	@Override
	protected void doInit() throws ResourceException {
		getVariants().add(new Variant(MediaType.APPLICATION_JSON));
	}

	@Override
	protected Representation get(Variant variant) throws ResourceException {
		final BuddiApplication application = (BuddiApplication) getApplication();
		final SqlSession sqlSession = application.getSqlSessionFactory().openSession(true);
		final BuddiUser buddiUser = (BuddiUser) getRequest().getClientInfo().getUser();
		if (buddiUser == null){
			throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED);
		}
		try {
			final User user = sqlSession.getMapper(Users.class).selectUser(buddiUser.getIdentifier());
			final JSONArray result = new JSONArray();
			result.put(user.toJson());
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
		try {
			final JSONArray request = new JSONArray(entity.getText());
			int total = 0;
			for (int i = 0; i < request.length(); i++) {
				final JSONObject user = request.getJSONObject(i);

				final Integer count = sqlSession.getMapper(Users.class).insertUser(new User(user));
				if (count == 1){
					sqlSession.commit();
					total += count;
				}
				else {
					throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
				}
			}
			
			final JSONObject result = new JSONObject();
			result.put("success", true);
			result.put("added", total);
			return new JsonRepresentation(result);
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
