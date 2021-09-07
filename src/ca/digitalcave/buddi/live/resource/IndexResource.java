package ca.digitalcave.buddi.live.resource;

import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.ibatis.session.SqlSession;
import org.restlet.Application;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

import ca.digitalcave.buddi.live.BuddiApplication;
import ca.digitalcave.buddi.live.db.Sources;
import ca.digitalcave.buddi.live.db.Users;
import ca.digitalcave.buddi.live.db.util.ConstraintsChecker;
import ca.digitalcave.buddi.live.db.util.DataUpdater;
import ca.digitalcave.buddi.live.db.util.DatabaseException;
import ca.digitalcave.buddi.live.model.Account;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.LocaleUtil;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;
import ca.digitalcave.moss.crypto.MossHash;
import ca.digitalcave.moss.restlet.CookieAuthInterceptResource;

public class IndexResource extends CookieAuthInterceptResource {

	@Override
	protected Representation get(Variant variant) throws ResourceException {
		final HashMap<String, Object> dataModel = new HashMap<String, Object>();
		final BuddiApplication application = (BuddiApplication) getApplication();
		final SqlSession sqlSession = application.getSqlSessionFactory().openSession();
		try {
			final User user = (User) getRequest().getClientInfo().getUser();
			if (user != null){
				//Check user's encryption level, and upgrade as needed
				final int encryptionVersion = sqlSession.getMapper(Users.class).selectEncryptionVersion(user);

				if (encryptionVersion == 0){
					//Upgrade from 0 to latest
					DataUpdater.upgradeEncryptionFrom0(user, sqlSession);
				}
				else if (encryptionVersion == 1){
					//Upgrade from 1 to latest
					DataUpdater.upgradeEncryptionFrom1(user, sqlSession);
				}

				final List<Account> accounts = sqlSession.getMapper(Sources.class).selectAccounts(user);
				if (accounts.size() == 0) dataModel.put("newUser", "true");

				//Update the user's last login date
				sqlSession.getMapper(Users.class).updateUserLoginTime(user);

				sqlSession.commit();
			}
		} catch (CryptoException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		} catch (DatabaseException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		} finally {
			sqlSession.close();
		}
		
		dataModel.put("user", getClientInfo().getUser());
		dataModel.put("requestAttributes", getRequestAttributes());
		dataModel.put("systemProperties", ((BuddiApplication) getApplication()).getSystemProperties());
		dataModel.put("translation", LocaleUtil.getTranslation(getRequest()));

		return new TemplateRepresentation("/index.html", ((BuddiApplication) getApplication()).getFreemarkerConfiguration(), dataModel, variant.getMediaType());
	}

	@Override
	protected String insertUser(org.restlet.security.User user, String activationKey) {
		final BuddiApplication application = (BuddiApplication) getApplication();
		final SqlSession sqlSession = application.getSqlSessionFactory().openSession();
		try {
			final Form form = (Form) getRequest().getAttributes().get("form");
			
			if (!"on".equals(form.getFirstValue("agree", "off"))) throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, LocaleUtil.getTranslation(getRequest()).getString("CREATE_USER_AGREEMENT_REQUIRED"));
			
			final User newUser = new User();
			newUser.setIdentifier(new MossHash().setSaltLength(0).setIterations(1).generate(user.getIdentifier()));
			newUser.setUuid(UUID.randomUUID().toString());
			newUser.setCurrency(Currency.getInstance(form.getFirstValue("currency", "USD")));
			newUser.setLocale(LocaleUtils.toLocale(form.getFirstValue("locale", "en_US")));
			ConstraintsChecker.checkInsertUser(newUser, sqlSession);

			cleanupUsers(sqlSession, null);
			
			final Integer insertUserCount = sqlSession.getMapper(Users.class).insertUser(newUser);
			if (insertUserCount != 1) throw new DatabaseException(String.format("User insert failed; expected 1 row, returned %s", insertUserCount));
			final Integer insertActivationCount = sqlSession.getMapper(Users.class).insertActivationKey(newUser, activationKey);
			if (insertActivationCount != 1) throw new DatabaseException(String.format("Activation key insert failed; expected 1 row, returned %s", insertActivationCount));
			
			sqlSession.commit();

			return user.getIdentifier();
		}
		catch (DatabaseException e){
			getLogger().log(Level.INFO, e.getMessage());
		}
		finally {
			sqlSession.close();
		}
		return null;
	}
	
	@Override
	protected String updateActivationKey(String identifier, String activationKey) {
		final BuddiApplication application = (BuddiApplication) getApplication();
		final SqlSession sqlSession = application.getSqlSessionFactory().openSession();
		try {
			final String hashedIdentifier = new MossHash().setSaltLength(0).setIterations(1).generate(identifier);
			final User user = sqlSession.getMapper(Users.class).selectUser(hashedIdentifier);
			if (user == null) throw new DatabaseException("Could not find user with hashed identifier" + hashedIdentifier);
			if (user.isEncrypted()) throw new DatabaseException("Users with encrypted data cannot reset passwords.");
			
			cleanupUsers(sqlSession, user);
			
			final Integer count = sqlSession.getMapper(Users.class).insertActivationKey(user, activationKey);
			if (count != 1) throw new DatabaseException(String.format("Insert failed; expected 1 row, returned %s", count));
			
			sqlSession.commit();

			return identifier;
		}
		catch (DatabaseException e){
			getLogger().log(Level.INFO, e.getMessage());
		}
		finally {
			sqlSession.close();
		}
		return null;
	}
	
	@Override
	protected void updateSecret(String activationKey, String hash) {
		final BuddiApplication application = (BuddiApplication) getApplication();
		final SqlSession sqlSession = application.getSqlSessionFactory().openSession();
		try {
			final User user = sqlSession.getMapper(Users.class).selectUserByActivationKey(activationKey);
			if (user == null) throw new DatabaseException("Activation key is not valid");
			final Integer count = sqlSession.getMapper(Users.class).updateUserSecret(user, hash);
			if (count != 1) throw new DatabaseException(String.format("Update failed; expected 1 row, returned %s", count));
			
			cleanupUsers(sqlSession, user);
			
			sqlSession.commit();
		} catch (DatabaseException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		} finally {
			sqlSession.close();
		}
	}
	
	@Override
	protected boolean isAllowRegister() {
		return true;
	}
	
	@Override
	protected boolean isAllowReset() {
		return true;
	}
	
	@Override
	protected Properties getConfig() {
		return ((BuddiApplication) Application.getCurrent()).getConfigProperties();
	}
	
	private void cleanupUsers(SqlSession sqlSession, User user) throws DatabaseException {
		if (user == null || user.getId() == null) sqlSession.getMapper(Users.class).deleteActivationKey();	//Delete keys older than one day
		else  sqlSession.getMapper(Users.class).deleteActivationKey(user);	//Delete any existing keys for user, and all keys older than one day
		sqlSession.getMapper(Users.class).deleteInactiveUsers();	//Delete users who never set a password and whose activation keys have already expired (i.e. they never completed registration and it is too late to do so now)
	}
}
