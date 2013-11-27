package ca.digitalcave.buddi.live.resource.buddilive;

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
import ca.digitalcave.buddi.live.db.util.DatabaseException;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.LocaleUtil;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;
import ca.digitalcave.moss.crypto.MossHash;

public class ChangePasswordResource extends ServerResource {

	@Override
	protected void doInit() throws ResourceException {
		getVariants().add(new Variant(MediaType.APPLICATION_JSON));
	}

	@Override
	protected Representation post(Representation entity, Variant variant) throws ResourceException {
		final BuddiApplication application = (BuddiApplication) getApplication();
		final SqlSession sqlSession = application.getSqlSessionFactory().openSession();
		final User user = (User) getRequest().getClientInfo().getUser();
		try {
			final JSONObject json = new JSONObject(entity.getText());
			final String action = json.optString("action");
			
			if ("update".equals(action)){
				final String currentPassword = json.getString("currentPassword");
				final String newPassword = json.getString("newPassword");

				if (MossHash.verify(user.getSecretString(), currentPassword)){
					if (application.getPasswordChecker().isValid("", newPassword)){	//We don't care about password history, so the identifier is not used.
						user.setSecretString(new MossHash().generate(newPassword));
						int count = sqlSession.getMapper(Users.class).updateUser(user);
						if (count != 1) throw new DatabaseException(String.format("Update failed; expected 1 row, returned %s", count));
						if (user.isEncrypted()){
							final String encryptionKey = user.getDecryptedEncryptionKey();
							user.setEncryptionKey(application.getCrypto().encrypt(encryptionKey, newPassword));
							count = sqlSession.getMapper(Users.class).updateUserEncryptionKey(user);
							if (count != 1) throw new DatabaseException(String.format("Encryption key update failed; expected 1 row, returned %s", count));
						}
						
						sqlSession.commit();
						final JSONObject result = new JSONObject();
						result.put("success", true);
						return new JsonRepresentation(result);
					}
					throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, LocaleUtil.getTranslation(getRequest()).getString("PASSWORD_CHECK_FAILED"));
				}
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, LocaleUtil.getTranslation(getRequest()).getString("INCORRECT_PASSWORD"));
			}
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, LocaleUtil.getTranslation(getRequest()).getString("ACTION_PARAMETER_MUST_BE_SPECIFIED"));
		} catch (DatabaseException e){
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		} catch (IOException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		} catch (JSONException e){
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		} catch (CryptoException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		} finally {
			sqlSession.close();
		}
	}
}
