package ca.digitalcave.buddi.live.security;

import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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
	
	public final static String COOKIE_NAME = "buddi-live";
	public static String COOKIE_PASSWORD;	//Set from BuddiApplication.start()
	public final static ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);
	private final static Map<String, Semaphore> loginAttempts = Collections.synchronizedMap(new WeakHashMap<String, Semaphore>());
	
	private BuddiApplication application;
	
	public BuddiVerifier(BuddiApplication application) {
		this.application = application;
	}
	
	protected BuddiApplication getApplication() {
		return application;
	}
	
	public int verify(Request request, Response response) {
		final Cookie cookieUser = request.getCookies().getFirst(COOKIE_NAME);
		final Locale browserLocale = ServletUtils.getRequest(request).getLocale();
		
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
					expiry = FormatUtil.parseDateTimeInternal(token.getString("expiry"));
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
			request.getClientInfo().setUser(new User(browserLocale));
			return RESULT_MISSING;
		} 
		else {
			final String identifier = request.getChallengeResponse().getIdentifier();	//This is the non-hashed identifier from the request
			final String secret = new String(request.getChallengeResponse().getSecret());
			
			final String hashedIdentifier = CryptoUtil.getSha256Hash(1, new byte[0], identifier);	//We don't salt the identifier, as the user supplies this and we have no way of looking up salt.
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
						if (user.getLocale() == null) user.setLocale(browserLocale);
						user.setPlaintextIdentifier(identifier);
						request.getClientInfo().setUser(user);
						return RESULT_VALID;
					}
					else {
						delay(identifier, response);
					}
				}
				else if (identifier != null){
					//This will happen if the identifier is set, but is not in the DB.  By delaying equally for bad password and
					// bad username, we prevent user enumeration attacks.
					delay(identifier, response);
				}
			}
			catch (CryptoException e){
				//TODO Remove this
				e.printStackTrace();
			}
			finally {
				sqlSession.close();
			}
			
			request.getClientInfo().setUser(new User(browserLocale));
			return RESULT_INVALID;
		}
	}
	
	private void delay(String identifier, Response response) {
		try {
			//If the authentication is wrong, wait for 5 seconds.  This, combined with the semaphore to gate the concurrent 
			// number of attempts, will limit brute force attacks on the system.
			if (loginAttempts.get(identifier) == null) loginAttempts.put(identifier, new Semaphore(1));
			final Semaphore semaphore = loginAttempts.get(identifier);
			semaphore.acquire();
			Thread.sleep(1500);
			semaphore.release();

			//Clear the bad cookie (if it was a cookie) so that we don't delay again
			final CookieSetting c = new CookieSetting(BuddiVerifier.COOKIE_NAME, "");
			c.setAccessRestricted(true);
			c.setMaxAge(0);
			response.getCookieSettings().add(c);
		}
		catch (InterruptedException e){
			;
		}
	}
}
