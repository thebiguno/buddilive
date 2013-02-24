package ca.digitalcave.buddi.web.security;

import java.text.SimpleDateFormat;
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
import ca.digitalcave.buddi.web.util.CryptoUtil;

public class BuddiVerifier implements Verifier {
	
	public final static String COOKIE_NAME = "buddi";
	
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
				final JSONObject token = new JSONObject(new String(CryptoUtil.decrypt(Base64.decode(cookieUser.getValue()))));
				
				if (token.has("clientIp")){
					if (!token.get("clientIp").equals(request.getClientInfo().getAddress())){
						throw new Exception("Stored IP address does not match client IP; denying login");
					}
				}
				
				Date expiry = null;
				try {
					expiry = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(token.getString("expiry"));
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
		} else {
			//if (true) return RESULT_INVALID;
			final String email = request.getChallengeResponse().getIdentifier();
			final String secret = new String(request.getChallengeResponse().getSecret());
			
			SqlSession sqlSession = application.getSqlSessionFactory().openSession(true);
			try {
				final String storedSecret = sqlSession.getMapper(Users.class).selectHashedPassword(email);
				if (storedSecret.startsWith("SHA1:")) {
					final String storedSalt = CryptoUtil.extractSalt(storedSecret);
					if (CryptoUtil.saltedSha1(storedSalt, secret).equals(storedSecret)){
						return RESULT_VALID;
					}
				}
				//We currently don't support anything other than SHA1 hashed + salted passwords.  If we add further methods in the future, add them here.
			} finally {
				sqlSession.close();
			}
			return RESULT_INVALID;
		}
	}
}
