package ca.digitalcave.buddi.live.resource.data;

import java.io.IOException;

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

import ca.digitalcave.buddi.live.BuddiApplication;
import ca.digitalcave.buddi.live.db.Users;
import ca.digitalcave.buddi.live.db.util.ConstraintsChecker;
import ca.digitalcave.buddi.live.db.util.DatabaseException;
import ca.digitalcave.buddi.live.model.User;

public class UsersDataResource extends ServerResource {

	@Override
	protected void doInit() throws ResourceException {
		getVariants().add(new Variant(MediaType.APPLICATION_JSON));
	}

	@Override
	protected Representation get(Variant variant) throws ResourceException {
		final User user = (User) getRequest().getClientInfo().getUser();
		if (user == null){
			throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED);
		}
		try {
			return new JsonRepresentation(user.toJson());
		}
		catch (JSONException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}
	
	@Override
	protected Representation post(Representation entity, Variant variant) throws ResourceException {
		final BuddiApplication application = (BuddiApplication) getApplication();
		final SqlSession sqlSession = application.getSqlSessionFactory().openSession();
		try {
			final JSONObject request = new JSONObject(entity.getText());
			final User user = new User(request);
			if ("insert".equals(request.getString("action"))){
				ConstraintsChecker.checkInsertUser(user, sqlSession);
				final Integer count = sqlSession.getMapper(Users.class).insertUser(user);
				if (count != 1) throw new DatabaseException(String.format("Insert failed; expected 1 row, returned %s", count));
			}
			else {
				final User secureUser = (User) getClientInfo().getUser();
				if (!secureUser.isAuthenticated()) throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED);
				if ("update".equals(request.getString("action"))){
					secureUser.setCredentials(user.getCredentials());
					secureUser.setEmail(user.getEmail());
					secureUser.setLocale(user.getLocale());
					secureUser.setPremium(user.isPremium());
					final Integer count = sqlSession.getMapper(Users.class).updateUser(secureUser);
					if (count != 1) throw new DatabaseException(String.format("Update failed; expected 1 row, returned %s", count));
				}
				else if ("delete".equals(request.getString("action"))){
					final Integer count = sqlSession.getMapper(Users.class).deleteUser(secureUser);
					if (count != 1) throw new DatabaseException(String.format("Delete failed; expected 1 row, returned %s", count));
				}
				else {
					throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "An action parameter must be specified.");
				}
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
