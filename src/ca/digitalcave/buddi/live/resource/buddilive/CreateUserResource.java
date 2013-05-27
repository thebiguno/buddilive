package ca.digitalcave.buddi.live.resource.buddilive;

import java.io.IOException;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.ibatis.session.SqlSession;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ca.digitalcave.buddi.live.BuddiApplication;
import ca.digitalcave.buddi.live.db.Users;
import ca.digitalcave.buddi.live.db.util.ConstraintsChecker;
import ca.digitalcave.buddi.live.db.util.DataUpdater;
import ca.digitalcave.buddi.live.db.util.DatabaseException;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.security.BuddiVerifier;
import ca.digitalcave.buddi.live.util.CryptoUtil;
import ca.digitalcave.buddi.live.util.CryptoUtil.CryptoException;

public class CreateUserResource extends ServerResource {

	//Just re-use the cookie password.
	private static final String nonceSecret = BuddiVerifier.COOKIE_PASSWORD;

	@Override
	protected void doInit() throws ResourceException {
		getVariants().add(new Variant(MediaType.TEXT_HTML));
		getVariants().add(new Variant(MediaType.APPLICATION_JSON));
	}

	@Override
	protected Representation get(Variant variant) throws ResourceException {
		final BuddiApplication application = (BuddiApplication) getApplication();
		final SqlSession sqlSession = application.getSqlSessionFactory().openSession();
		try {
			if (getQuery().getFirst("nonce") != null) {
				final JSONObject request = new JSONObject(CryptoUtil.decrypt(getQuery().getFirstValue("nonce"), nonceSecret));
				final User user = new User(request);
				ConstraintsChecker.checkInsertUser(user, sqlSession);
				
				final Integer count = sqlSession.getMapper(Users.class).insertUser(user);
				if (count != 1) throw new DatabaseException(String.format("Insert failed; expected 1 row, returned %s", count));

				if (request.optBoolean("encrypt", false)) {
					DataUpdater.turnOnEncryption(user, request.getString("credentials"), sqlSession);
				}
				
				sqlSession.commit();

				getResponse().redirectSeeOther("..");
				return new EmptyRepresentation();
			}
			else {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
			}
		}
		catch (JSONException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		catch (DatabaseException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		catch (CryptoException e){
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
			final JSONObject result = new JSONObject();
			final User user = new User(request);
			final String identifier = request.getString("identifier");
			if ("insert".equals(request.getString("action"))){
				request.get("referrer");
//				final Reference ref = getRequest().getOriginalRef();
//				if (getRequestAttributes().get("X-Forwarded-For") != null){
//					getRequest().getHostRef()
//				}
				
				final Reference ref = new Reference(request.getString("referrer"));
				ref.setSegments(getRequest().getOriginalRef().getSegments());
				final String url = ref.addQueryParameter("nonce", CryptoUtil.encrypt(request.toString(), nonceSecret)).toString();
				final HtmlEmail email = new HtmlEmail();
				email.addTo(identifier);
				email.setSubject(user.getTranslation().getString("CREATE_USER_EMAIL_SUBJECT"));
				final String clientIp = getRequest().getClientInfo().getAddress();
				email.setMsg(String.format(user.getTranslation().getString("CREATE_USER_EMAIL_BODY"), clientIp, url));
				email.setFrom("no-reply@digitalcave.ca", "Buddi Live Account Creation");
				email.setHostName("localhost");
				email.send();

				result.put("success", true);
				result.put("title", user.getTranslation().getString("CREATE_USER_EMAIL_SENT_TITLE"));
				result.put("message", String.format(user.getTranslation().getString("CREATE_USER_EMAIL_SENT"), identifier));
				
				sqlSession.commit();
				return new JsonRepresentation(result);
			}
			else {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, user.getTranslation().getString("ACTION_PARAMETER_MUST_BE_SPECIFIED"));
			}
		}
		catch (EmailException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		catch (IOException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		catch (JSONException e){
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		}
		catch (CryptoException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		finally {
			sqlSession.close();
		}
	}
}
