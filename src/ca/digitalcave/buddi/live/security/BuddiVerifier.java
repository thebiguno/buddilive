package ca.digitalcave.buddi.live.security;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.json.JSONObject;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.security.Verifier;

import ca.digitalcave.buddi.live.BuddiApplication;
import ca.digitalcave.buddi.live.db.Users;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.CryptoUtil;
import ca.digitalcave.buddi.live.util.CryptoUtil.CryptoException;
import ca.digitalcave.buddi.live.util.FormatUtil;

public class BuddiVerifier implements Verifier {
	
	public final static String COOKIE_NAME = "buddi";
	public final static String COOKIE_PASSWORD = "changeme";
	public final static ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);
	
	private BuddiApplication application;
	
	public BuddiVerifier(BuddiApplication application) {
		this.application = application;
	}
	
	protected BuddiApplication getApplication() {
		return application;
	}
	
	public int verify(Request request, Response response) {
		final Cookie cookieUser = request.getCookies().getFirst(COOKIE_NAME);
		final String requestLocale = ServletUtils.getRequest(request).getLocale().toString();
		
		if (cookieUser != null){
			try {
				ScheduledFuture<JSONObject> decryptCookie = executor.schedule(new Callable<JSONObject>() {
					@Override
					public JSONObject call() throws Exception {
						return new JSONObject(CryptoUtil.decrypt(cookieUser.getValue(), COOKIE_PASSWORD));
					}
				}, 0, TimeUnit.MILLISECONDS);
				final JSONObject token = decryptCookie.get(1, TimeUnit.SECONDS);
				
				if (token.has("clientIp")){
					if (!token.get("clientIp").equals(request.getClientInfo().getAddress())){
						throw new Exception("Stored IP address does not match client IP; denying login");
					}
				}
				
				Date expiry = null;
				try {
					expiry = FormatUtil.parseDateTime(token.getString("expiry"));
				}
				catch (Throwable e){}
				
				if (expiry == null || expiry.after(new Date())) {
					request.setChallengeResponse(new ChallengeResponse(ChallengeScheme.CUSTOM, token.getString("identifier"), token.getString("credentials")));
				}
			}
			catch (Throwable t){
				//If we failed to decrypt it, delete the cookie so we don't have to bother again
				final CookieSetting c = new CookieSetting(BuddiVerifier.COOKIE_NAME, "");
				c.setAccessRestricted(true);
				c.setMaxAge(0);
				response.getCookieSettings().add(c);
			}
		}

		if (request.getChallengeResponse() == null) {
			request.getClientInfo().setUser(new User(requestLocale));
			return RESULT_MISSING;
		} 
		else {
			final String identifier = request.getChallengeResponse().getIdentifier();	//This is the non-hashed identifier from the request
			final String secret = new String(request.getChallengeResponse().getSecret());
			
			
			final String hashedIdentifier = CryptoUtil.getSha256Hash(1, new byte[0], identifier);
			final SqlSession sqlSession = application.getSqlSessionFactory().openSession(true);
			try {
				final User user = sqlSession.getMapper(Users.class).selectUser(hashedIdentifier);
				if (user != null){
					final String storedSecret = user.getCredentials();
					if (CryptoUtil.verify(storedSecret, secret)){
						user.setAuthenticated(true);
						if (user.isEncrypted()){
							//This decryption procedure must be the same as is used in DataUpdater.  If one of these changes, be sure to update them both.
							user.setDecryptedEncryptionKey(CryptoUtil.decrypt(user.getEncryptionKey(), secret));
						}
						//Fallback to browser locale if it is not set in the user object
						if (StringUtils.isBlank(user.getLocale())) user.setLocale(requestLocale);
						user.setPlaintextIdentifier(identifier);
						request.getClientInfo().setUser(user);
						return RESULT_VALID;
					}
				}
			}
			catch (CryptoException e){
				//TODO Remove this
				e.printStackTrace();
			}
			finally {
				sqlSession.close();
			}
			
			request.getClientInfo().setUser(new User(requestLocale));
			return RESULT_INVALID;
		}
	}
}
