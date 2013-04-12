package ca.digitalcave.buddi.live.resource;

import java.util.Date;

import org.apache.ibatis.session.SqlSession;
import org.json.JSONObject;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.CookieSetting;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.restlet.security.Verifier;

import ca.digitalcave.buddi.live.BuddiApplication;
import ca.digitalcave.buddi.live.db.Users;
import ca.digitalcave.buddi.live.db.util.DataUpdater;
import ca.digitalcave.buddi.live.db.util.DatabaseException;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.security.BuddiVerifier;
import ca.digitalcave.buddi.live.util.CryptoUtil;
import ca.digitalcave.buddi.live.util.CryptoUtil.CryptoException;

public class IndexResource extends ServerResource {
	@Override
	protected void doInit() throws ResourceException {
		getVariants().add(new Variant(MediaType.TEXT_HTML));
		getVariants().add(new Variant(MediaType.APPLICATION_JSON));
	}
	
	@Override
	protected Representation get(Variant variant) throws ResourceException {
		
		//Handle Logout
		if (getQuery().getFirst("logout") != null) {
			final CookieSetting c = new CookieSetting(BuddiVerifier.COOKIE_NAME, "");
			c.setAccessRestricted(true);
			c.setMaxAge(0);
			getResponse().getCookieSettings().add(c);
			
			getResponse().redirectSeeOther(".");
			return new EmptyRepresentation();
		}
		
		final BuddiApplication application = (BuddiApplication) getApplication();
		final SqlSession sqlSession = application.getSqlSessionFactory().openSession();
		try {
			final User user = (User) getRequest().getClientInfo().getUser();

			if (user.isAuthenticated()){
				//Check for outstanding scheduled transactions
				final Date userDate = (Date) getRequest().getAttributes().get("date");
				final String messages = DataUpdater.updateScheduledTransactions(user, sqlSession, userDate);
				user.getData().put("messages", messages);
				
				//Update the user's last login date
				sqlSession.getMapper(Users.class).updateUserLoginTime(user);
				
				sqlSession.commit();
				return new TemplateRepresentation("index.html", application.getFreemarkerConfiguration(), user, MediaType.TEXT_HTML);
			}
			else {
				return new TemplateRepresentation("login.html", application.getFreemarkerConfiguration(), user, MediaType.TEXT_HTML);
			}
		}
		catch (CryptoException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		catch (DatabaseException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		finally {
			sqlSession.close();
		}
	}
	
	@Override
	protected Representation post(Representation entity, Variant variant) throws ResourceException {
		try {
			final JSONObject token = new JSONObject(entity.getText());
			token.put("clientIp", getRequest().getClientInfo().getAddress());
			
			//Check the authentication right now, so that we can return an error if it is not valid.  This is somewhat
			// awkward, but it makes for a better user experience.
			getRequest().setChallengeResponse(new ChallengeResponse(ChallengeScheme.CUSTOM, token.getString("identifier"), token.getString("credentials")));
			if (new BuddiVerifier((BuddiApplication) getApplication()).verify(getRequest(), getResponse()) != Verifier.RESULT_VALID){
				throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED);	
			}
			
			final CookieSetting c = new CookieSetting(BuddiVerifier.COOKIE_NAME, CryptoUtil.encrypt(token.toString(), BuddiVerifier.COOKIE_PASSWORD));
			c.setAccessRestricted(true);
			c.setMaxAge(-1);	//Delete on browser close
			
			getResponse().getCookieSettings().add(c);
			getResponse().redirectSeeOther(".");
			return new EmptyRepresentation();
		}
		catch (Exception e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}
}
