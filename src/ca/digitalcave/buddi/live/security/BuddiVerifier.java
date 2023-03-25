package ca.digitalcave.buddi.live.security;

import ca.digitalcave.moss.restlet.plugin.AuthenticationHelper;
import ca.digitalcave.moss.restlet.security.CookieVerifier;

public class BuddiVerifier extends CookieVerifier {
	
	public BuddiVerifier(AuthenticationHelper helper) {
		super(helper);
	}

//	@Override
//	public int verify(Request request, Response response) {
//		final ChallengeResponse cr = request.getChallengeResponse();
//		if (cr == null || cr.getSecret() == null) return RESULT_MISSING;
//
//		final BuddiApplication application = (BuddiApplication) Application.getCurrent();
//
//		final SqlSession sql = application.getSqlSessionFactory().openSession(true);
//		final String identifier = request.getChallengeResponse().getIdentifier();
//		try {
//			final String hashedIdentifier = new DefaultHash().setSaltLength(0).setIterations(1).generate(identifier);	//We don't salt the identifier, as the user supplies this and we have no way of looking up salt.
//			final User user = sql.getMapper(Users.class).selectUser(hashedIdentifier);
//			if (user == null) return RESULT_UNKNOWN;
//			
//			final String secret = new String(cr.getSecret());
//			if (checkSecret(secret, user) == false) return RESULT_INVALID;
//			if (user.getLocale() == null) user.setLocale(ServletUtils.getRequest(request).getLocale());
//			user.setPlaintextSecret(secret);
//			user.setPlaintextIdentifier(identifier);
//			request.getClientInfo().setUser(user);
//			return RESULT_VALID;
//		} finally {
//			sql.close();
//		}
//	}
//	
//	private boolean checkSecret(String secret, User user) {
//		if (secret == null || user.getSecret() == null) return false;
//		
//		final String storedSecret = new String(user.getSecret());
//		if (DefaultHash.verify(storedSecret, secret)){
//			return true;
//		}
//		
//		return false;
//	}
}
