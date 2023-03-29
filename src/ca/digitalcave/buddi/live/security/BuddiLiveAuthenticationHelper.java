package ca.digitalcave.buddi.live.security;

import java.security.Key;
import java.util.Currency;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.SecretKey;
import javax.mail.internet.AddressException;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.ibatis.session.SqlSession;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import ca.digitalcave.buddi.live.BuddiApplication;
import ca.digitalcave.buddi.live.db.BuddiSystem;
import ca.digitalcave.buddi.live.db.Users;
import ca.digitalcave.buddi.live.db.util.ConstraintsChecker;
import ca.digitalcave.buddi.live.db.util.DatabaseException;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.LocaleUtil;
import ca.digitalcave.moss.crypto.Crypto;
import ca.digitalcave.moss.crypto.Crypto.Algorithm;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;
import ca.digitalcave.moss.crypto.DefaultHash;
import ca.digitalcave.moss.crypto.Hash;
import ca.digitalcave.moss.restlet.CookieAuthenticator;
import ca.digitalcave.moss.restlet.model.AuthUser;
import ca.digitalcave.moss.restlet.plugin.AuthenticationConfiguration;
import ca.digitalcave.moss.restlet.plugin.AuthenticationHelper;

public class BuddiLiveAuthenticationHelper extends AuthenticationHelper {
	
	private final BuddiApplication application;
	
	public BuddiLiveAuthenticationHelper(BuddiApplication application) {
		super(new AuthenticationConfiguration());
		this.application = application;
	}
	
	public BuddiApplication getApplication() {
		return application;
	}

	//******************* Authentication / User Section *******************//
	
	@Override
	public AuthUser authenticate(String applicationName, ChallengeResponse cr, Form form) {
		final String identifier = cr.getIdentifier();
		if (identifier == null){
			return null;
		}
//		cr.setIdentifier(identifier.toLowerCase());
		final String authenticator = CookieAuthenticator.getAuthenticator(cr);
		
		final String secret = new String(cr.getSecret());
		
		final User user = (User) selectUser(authenticator);
		if (user == null){
			//If the user was not found, we do not proceed.
			return null;
		}

		boolean authenticated = false;

		final String storedSecret = new String(user.getSecret());

		boolean legacy = false;

		if (storedSecret.startsWith("SHA-512:")){
			authenticated = DefaultHash.verify(storedSecret, secret);
			//if (!storedSecret.startsWith("SHA-512:4e20:")){
			//	legacy = true;		//If we need to adjust the iterations count, we can do so here.
			//}
		}
		else if (storedSecret.startsWith("SHA-256:")){
			authenticated = DefaultHash.verify(storedSecret, secret);
			legacy = true;
		}
		else {
			authenticated = false;	//We only accept SHA-512 passwords.  Plain text passwords no longer will work.
		}
		
		if (authenticated){
			//We update the DB for legacy hashing algorithms.  This ensures that everyone who logs in will have their password stored in a safe manner.
			if (legacy){
				final SqlSession sql2 = application.getSqlSessionFactory().openSession();
				try {
					sql2.getMapper(Users.class).updateUserSecret(user, getHash().generate(secret));
					sql2.commit();
				}
				catch (Exception e){
					sql2.rollback(true);
				}
				finally {
					sql2.close();
				}
			}

			user.setPlaintextSecret(secret);
			user.setPlaintextIdentifier(identifier);

			return user;
		}
		
		return null;
	}

	@Override
	public AuthUser selectUser(String username) {
		final SqlSession sql = application.getSqlSessionFactory().openSession();
		try {
			User user = sql.getMapper(Users.class).selectUser(getHashedUsername(username));
			
			sql.commit(true);
			
			return user;
		}
		finally {
			sql.close();
		}
	}
	
	@Override
	public List<AuthUser> selectUsers(String email) {
		throw new RuntimeException("Forgot Username not implemented");
	}

	public boolean insertTotpSecret(String username, String totpSharedSecret, ChallengeResponse cr){
		final SqlSession sql = application.getSqlSessionFactory().openSession();
		try {
			final User user = sql.getMapper(Users.class).selectUser(getHashedUsername(username));
			if (user != null) {
				sql.getMapper(Users.class).deleteUnusedBackupCodes(user);
				int count = sql.getMapper(Users.class).updateUserTotpSecret(user, totpSharedSecret);
				if (count == 1) {
					sql.commit(true);
					return true;
				}
			}

			sql.rollback(true);
			return false;
		}
		finally {
			sql.close();
		}
	}

	@Override
	public void insertTotpBackupCodes(String username, ChallengeResponse cr) {
		final SqlSession sql = application.getSqlSessionFactory().openSession();
		try {
			final User user = sql.getMapper(Users.class).selectUser(getHashedUsername(username));
			if (user != null) {
				sql.getMapper(Users.class).deleteUnusedBackupCodes(user);
				for (int i = 0; i < 10; i++) {
					final String backupCode = UUID.randomUUID().toString();
					sql.getMapper(Users.class).insertTotpBackupCode(user, backupCode);
				}
				
				sql.commit(true);
				return;
			}
			
			sql.rollback(true);
		}
		finally {
			sql.close();
		}
	}

	@Override
	public void updateTotpBackupCodeMarkUsed(String username, String backupCode, ChallengeResponse cr) {
		final SqlSession sql = application.getSqlSessionFactory().openSession();
		try {
			final User user = sql.getMapper(Users.class).selectUser(getHashedUsername(username));
			if (user != null) {
				int count = sql.getMapper(Users.class).updateUserTotpBackupCodeUsed(user, backupCode);
				if (count == 1) {
					sql.commit(true);
					return;
				}
			}
			
			sql.rollback(true);
		}
		finally {
			sql.close();
		}
	}
	
	@Override
	public void disableTotp(String username) {
		final SqlSession sql = application.getSqlSessionFactory().openSession();
		try {
			final User user = sql.getMapper(Users.class).selectUser(getHashedUsername(username));
			if (user != null && StringUtils.isBlank(user.getTwoFactorSecret())) {
				user.setTwoFactorRequired(false);
				int count = sql.getMapper(Users.class).updateUser(user);
				if (count == 1) {
					sql.getMapper(Users.class).updateUserTotpSecret(user, null);
					sql.getMapper(Users.class).deleteUnusedBackupCodes(user);
					
					sql.commit(true);
					return;
				}
			}
			
			sql.rollback(true);
		}
		finally {
			sql.close();
		}
	}

	@Override
	public String updateActivationKey(String username, String activationKey) throws Exception {
		final BuddiApplication application = (BuddiApplication) getApplication();
		final SqlSession sqlSession = application.getSqlSessionFactory().openSession();
		try {
			final String hashedIdentifier = getHashedUsername(username);
			final User user = sqlSession.getMapper(Users.class).selectUser(hashedIdentifier);
			if (user == null) throw new DatabaseException("Could not find user with hashed identifier" + hashedIdentifier);
			if (user.isEncrypted()) throw new DatabaseException("Users with encrypted data cannot reset passwords.");
			
			cleanupUsers(sqlSession, user);
			
			final Integer count = sqlSession.getMapper(Users.class).insertActivationKey(user, activationKey);
			if (count != 1) throw new DatabaseException(String.format("Insert failed; expected 1 row, returned %s", count));
			
			sqlSession.commit();

			return username;
		}
		catch (DatabaseException e){
			Logger.getLogger(this.getClass().getName()).log(Level.INFO, e.getMessage());
		}
		finally {
			sqlSession.close();
		}
		return null;
	}

	@Override
	public boolean updatePasswordByActivationKey(String activationKey, String hashedPassword) {
		final BuddiApplication application = (BuddiApplication) getApplication();
		final SqlSession sqlSession = application.getSqlSessionFactory().openSession();
		try {
			final User user = sqlSession.getMapper(Users.class).selectUserByActivationKey(activationKey);
			if (user == null) {
				throw new DatabaseException("Activation key is not valid");
			}
			final Integer count = sqlSession.getMapper(Users.class).updateUserSecret(user, hashedPassword);
			if (count != 1) {
				throw new DatabaseException(String.format("Update failed; expected 1 row, returned %s", count));
			}
			
			cleanupUsers(sqlSession, user);
			
			sqlSession.commit();
			return true;
		}
		catch (DatabaseException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		finally {
			sqlSession.close();
		}
	}

	@Override
	public void insertUser(String email, String activationKey, Form form) throws Exception {
		final BuddiApplication application = (BuddiApplication) getApplication();
		final SqlSession sqlSession = application.getSqlSessionFactory().openSession();
		try {
			if (!"on".equals(form.getFirstValue("agree", "off"))){
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, LocaleUtil.getTranslation().getString("CREATE_USER_AGREEMENT_REQUIRED"));
			}
			
			final User newUser = new User();
			newUser.setIdentifier(getHashedUsername(email));	//This is a simple SHA-256 hash of the username.  We store username hashed in the DB for extra privacy.
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
		}
		finally {
			sqlSession.close();
		}
	}

	@Override
	public boolean updatePassword(String username, String hashedPassword) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void sendEmail(String toEmail, String subject, String body) {
		final Properties config = application.getConfigProperties();
		final String fromEmail = config.getProperty("mail.smtp.from");
		
		try {
			final HtmlEmail email = getApplication().getEmail(fromEmail, null, toEmail);
			email.setSubject(subject);
			email.setTextMsg(body);
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						email.send();
					}
					catch (EmailException e){
						Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Error sending email", e);
					}
				}
			}).start();
		}
		catch (AddressException e){
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Error parsing email address", e);
		}
		catch (EmailException e){
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Error preparing email", e);
		}
	}
	
	private void cleanupUsers(SqlSession sqlSession, User user) throws DatabaseException {
		if (user == null){
			sqlSession.getMapper(Users.class).deleteActivationKey();	//Delete keys older than one day
		}
		else {
			sqlSession.getMapper(Users.class).deleteActivationKey(user);	//Delete any existing keys for user, and all keys older than one day
		}
		sqlSession.getMapper(Users.class).deleteInactiveUsers();	//Delete users who never set a password and whose activation keys have already expired (i.e. they never completed registration and it is too late to do so now)
	}
	
	@Override
	public Key getKey() {
		final SqlSession sql = application.getSqlSessionFactory().openSession();
		try {
			SecretKey key;
			try {
				String keyEncoded = sql.getMapper(BuddiSystem.class).selectCookieEncryptionKey();
				if (keyEncoded == null){
					key = new Crypto().setAlgorithm(Algorithm.AES_256).generateSecretKey();
					keyEncoded = Crypto.encodeSecretKey(key);
					sql.getMapper(BuddiSystem.class).deleteCookieEncryptionKey();
					sql.getMapper(BuddiSystem.class).insertCookieEncryptionKey(keyEncoded);
					sql.commit();
				}
				key = Crypto.recoverSecretKey(keyEncoded);
			}
			catch (CryptoException e) {
				key = new Crypto().setAlgorithm(Algorithm.AES_256).generateSecretKey();
				String keyEncoded = Crypto.encodeSecretKey(key);
				sql.getMapper(BuddiSystem.class).updateCookieEncryptionKey(keyEncoded);
				sql.commit();
			}
			finally {
				sql.close();
			}
			return key;
		}
		catch (CryptoException e){
			throw new RuntimeException(e);
		}
		finally {
			sql.close();
		}
	}
	
	@Override
	public Hash getHash() {
		return new DefaultHash().setAlgorithm("SHA-512").setIterations(20000).setSaltLength(96);
	}
	
	private String getHashedUsername(String username) {
		return new DefaultHash().setSaltLength(0).setIterations(1).generate(username);
	}
}
