package ca.digitalcave.buddi.web.security;

import java.util.Date;

import org.apache.ibatis.session.SqlSession;
import org.json.JSONObject;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Cookie;
import org.restlet.engine.util.Base64;
import org.restlet.security.Verifier;

import ca.digitalcave.buddi.web.BuddiApplication;
import ca.digitalcave.buddi.web.db.Users;
import ca.digitalcave.buddi.web.model.User;
import ca.digitalcave.buddi.web.util.CryptoUtil;
import ca.digitalcave.buddi.web.util.FormatUtil;

public class BuddiVerifier implements Verifier {
	
	public final static String COOKIE_NAME = "buddi";
	public final static String COOKIE_PASSWORD = "changeme";
	
	private BuddiApplication application;
	
	public BuddiVerifier(BuddiApplication application) {
		this.application = application;
	}
	
	protected BuddiApplication getApplication() {
		return application;
	}
	
	public int verify(Request request, Response response) {
		final Cookie cookieUser = request.getCookies().getFirst(COOKIE_NAME);
		if (cookieUser != null){
			try {
				final JSONObject token = new JSONObject(new String(CryptoUtil.decrypt(Base64.decode(cookieUser.getValue()), COOKIE_PASSWORD.toCharArray())));
				
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
					request.setChallengeResponse(new ChallengeResponse(ChallengeScheme.CUSTOM, token.getString("identifier"), token.getString("password")));
				}
			}
			catch (Throwable t){}
		}
		
		if (request.getChallengeResponse() == null) {
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
						request.getClientInfo().setUser(user);
						return RESULT_VALID;
					}
				}
			}
			finally {
				sqlSession.close();
			}
			return RESULT_INVALID;
		}
	}
}
