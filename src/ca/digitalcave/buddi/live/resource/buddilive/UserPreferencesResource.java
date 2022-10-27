package ca.digitalcave.buddi.live.resource.buddilive;

import java.io.IOException;
import java.util.Currency;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
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
import ca.digitalcave.buddi.live.db.ScheduledTransactions;
import ca.digitalcave.buddi.live.db.Sources;
import ca.digitalcave.buddi.live.db.Transactions;
import ca.digitalcave.buddi.live.db.Users;
import ca.digitalcave.buddi.live.db.util.ConstraintsChecker;
import ca.digitalcave.buddi.live.db.util.DataUpdater;
import ca.digitalcave.buddi.live.db.util.DatabaseException;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.LocaleUtil;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;
import ca.digitalcave.moss.crypto.DefaultHash;

public class UserPreferencesResource extends ServerResource {

	@Override
	protected void doInit() throws ResourceException {
		getVariants().add(new Variant(MediaType.APPLICATION_JSON));
	}

	@Override
	protected Representation get(Variant variant) throws ResourceException {
		final User user = (User) getRequest().getClientInfo().getUser();
		try {
			final JSONObject result = new JSONObject();
			result.put("encrypt", user.isEncrypted());
			result.put("storeEmail", StringUtils.isNotBlank(user.getEmail()));
			result.put("locale", user.getLocale().toString());
			result.put("currency", user.getCurrency().getCurrencyCode());
			result.put("dateFormat", user.getOverrideDateFormat());
			//result.put("currencyAfter", user.isCurrencyAfter());
			result.put("showDeleted", user.isShowDeleted());
//			result.put("showCleared", user.isShowCleared());
//			result.put("showReconciled", user.isShowReconciled());
			result.put("success", true);
			return new JsonRepresentation(result);
		}
		catch (JSONException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
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
				if (json.optBoolean("encrypt", false) != user.isEncrypted()){
					//First check that the password is correct
					final String encryptPassword = json.getString("encryptPassword");
					if (!DefaultHash.verify(new String(user.getSecret()), encryptPassword)) throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, LocaleUtil.getTranslation(getRequest()).getString("INCORRECT_PASSWORD"));

					if (user.isEncrypted()) DataUpdater.turnOffEncryption(user, sqlSession);
					else DataUpdater.turnOnEncryption(user, sqlSession);
				}
				user.setEmail(json.optBoolean("storeEmail", false) ? user.getPlaintextIdentifier() : null);
				user.setLocale(LocaleUtils.toLocale(json.optString("locale", "en_US")));
				user.setCurrency(Currency.getInstance(json.optString("currency", "USD")));
				user.setOverrideDateFormat(json.optString("dateFormat", null));
				//user.setCurrencyAfter(json.optBoolean("currencyAfter", false));
				user.setShowDeleted(json.optBoolean("showDeleted", true));
				//user.setShowCleared(json.optBoolean("showCleared", false));
				//user.setShowReconciled(json.optBoolean("showReconciled", false));
				
				ConstraintsChecker.checkUpdateUserPreferences(user, sqlSession);
				
				int count = sqlSession.getMapper(Users.class).updateUser(user);
				if (count != 1) throw new DatabaseException(String.format("Update failed; expected 1 row, returned %s", count));

				DataUpdater.updateBalances(user, sqlSession);
			}
			else if ("delete".equals(action)){
				//Everything in the system will cascade from sources, transactions, and users
				sqlSession.getMapper(Transactions.class).deleteAllTransactions(user);
				sqlSession.getMapper(ScheduledTransactions.class).deleteAllScheduledTransactions(user);
				sqlSession.getMapper(Sources.class).deleteAllSources(user);
				sqlSession.getMapper(Users.class).deleteUser(user);
			}
			else {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, LocaleUtil.getTranslation(getRequest()).getString("ACTION_PARAMETER_MUST_BE_SPECIFIED"));
			}
			
			sqlSession.commit();
			final JSONObject result = new JSONObject();
			result.put("success", true);
			return new JsonRepresentation(result);
		}
		catch (DatabaseException e){
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		}
		catch (CryptoException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		catch (IOException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		catch (JSONException e){
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		}
		finally {
			sqlSession.close();
		}
	}
}
