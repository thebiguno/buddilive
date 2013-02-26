package ca.digitalcave.buddi.web.resource.data;

import java.io.IOException;
import java.util.UUID;

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
import ca.digitalcave.buddi.web.db.Users;
import ca.digitalcave.buddi.web.model.User;
import ca.digitalcave.buddi.web.security.BuddiUser;
import ca.digitalcave.buddi.web.util.CryptoUtil;
import ca.digitalcave.buddi.web.util.FormatUtil;

public class UsersDataResource extends ServerResource {

	@Override
	protected void doInit() throws ResourceException {
		getVariants().add(new Variant(MediaType.APPLICATION_JSON));
	}

	@Override
	protected Representation get(Variant variant) throws ResourceException {
		final BuddiApplication application = (BuddiApplication) getApplication();
		final SqlSession sqlSession = application.getSqlSessionFactory().openSession(true);
		final BuddiUser user = (BuddiUser) getRequest().getClientInfo().getUser();
		if (user == null){
			throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED);
		}
		try {
			final User userResult = sqlSession.getMapper(Users.class).selectUser(user.getIdentifier());
			final JSONObject result = new JSONObject();
			result.put("id", userResult.getId());
			result.put("uuid", userResult.getUuid());
			result.put("identifier", userResult.getIdentifier());
			result.put("credentials", userResult.getCredentials());
			result.put("email", userResult.getEmail());
			result.put("created", FormatUtil.formatDateTime(userResult.getCreated()));
			result.put("modified", FormatUtil.formatDateTime(userResult.getModified()));
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
			final JSONObject request = new JSONObject(entity.getText());
			
			final User value = new User();
			value.setIdentifier(request.getString("identifier").startsWith("SHA1:") ? request.getString("identifier") : CryptoUtil.getSha1Hash("", request.getString("identifier")));
			value.setCredentials(request.getString("credentials").startsWith("SHA1:") ? request.getString("credentials") : CryptoUtil.getSha1Hash(CryptoUtil.getRandomSalt(), request.getString("credentials")));
			value.setUuid(request.has("uuid") ? request.getString("uuid") : UUID.randomUUID().toString());
			value.setEmail(request.has("email") ? request.getString("email") : null);
			value.setPremium(false);
			
			final Integer count = sqlSession.getMapper(Users.class).insertUser(value);
			if (count == 1){
				sqlSession.commit();
			}
			else {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
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
