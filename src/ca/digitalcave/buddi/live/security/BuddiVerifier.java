package ca.digitalcave.buddi.live.security;

import org.apache.ibatis.session.SqlSession;
import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.Status;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.resource.ResourceException;
import org.restlet.security.Verifier;

import ca.digitalcave.buddi.live.BuddiApplication;
import ca.digitalcave.buddi.live.db.Users;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.CryptoUtil;
import ca.digitalcave.buddi.live.util.CryptoUtil.CryptoException;
import ca.digitalcave.moss.crypto.MossHash;

public class BuddiVerifier implements Verifier {

	@Override
	public int verify(Request request, Response response) {
		final ChallengeResponse cr = request.getChallengeResponse();
		if (cr == null) return RESULT_MISSING;

		final BuddiApplication application = (BuddiApplication) Application.getCurrent();

		final SqlSession sql = application.getSqlSessionFactory().openSession(true);
		final String identifier = request.getChallengeResponse().getIdentifier();
		try {
			final String hashedIdentifier = new MossHash().setSaltLength(0).setIterations(1).generate(identifier);	//We don't salt the identifier, as the user supplies this and we have no way of looking up salt.
			final User user = sql.getMapper(Users.class).selectUser(hashedIdentifier);
			if (user == null) return RESULT_UNKNOWN;
			
//			cr.setIdentifier(user.getIdentifier()); // the identifier could be an activation key so replace it
			if (checkSecret(cr, user) == false) return RESULT_INVALID;
			
			if (user.getLocale() == null) user.setLocale(ServletUtils.getRequest(request).getLocale());
			request.getClientInfo().setUser(user);
			return RESULT_VALID;
		} catch (CryptoException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		} finally {
			sql.close();
		}
	}
	
	private boolean checkSecret(ChallengeResponse cr, User user) throws CryptoException {
		if ((cr.getSecret() == null) || user.getSecret() == null) return false;
		
		final String challengeSecret = new String(cr.getSecret());
		final String storedSecret = new String(user.getSecret());
		if (CryptoUtil.verify(storedSecret, challengeSecret)){
			if (user.isEncrypted()){
				//This decryption procedure must be the same as is used in DataUpdater.  If one of these changes, be sure to update them both.
				user.setDecryptedEncryptionKey(CryptoUtil.decrypt(user.getEncryptionKey(), challengeSecret));
			}
			return true;
		}
		
		return false;
	}
}
