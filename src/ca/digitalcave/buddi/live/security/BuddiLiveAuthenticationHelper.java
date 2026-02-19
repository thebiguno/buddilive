package ca.digitalcave.buddi.live.security;

import java.security.Key;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
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

	private static final long MAX_BACKOFF_MS = 5_000L;
	private final ConcurrentHashMap<String, AtomicInteger> failedAttempts = new ConcurrentHashMap<>();

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

		// Incremental backoff: delay = min(2^(failures-1), 5) seconds
		final AtomicInteger failures = failedAttempts.get(authenticator);
		if (failures != null && failures.get() > 0) {
			final long delayMs = Math.min((1L << (failures.get() - 1)) * 1000L, MAX_BACKOFF_MS);
			try { Thread.sleep(delayMs); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
		}

		final String secret = new String(cr.getSecret());
		
		final User user = (User) selectUser(authenticator);
		if (user == null){
			//If the user was not found, we do not proceed.
			failedAttempts.computeIfAbsent(authenticator, k -> new AtomicInteger(0)).incrementAndGet();
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
			failedAttempts.remove(authenticator);
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
		
		failedAttempts.computeIfAbsent(authenticator, k -> new AtomicInteger(0)).incrementAndGet();
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
			application.getEmailExecutor().submit(() -> {
				try {
					email.send();
				}
				catch (EmailException e){
					Logger.getLogger(BuddiLiveAuthenticationHelper.class.getName()).log(Level.WARNING, "Error sending email", e);
				}
			});
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
	public String getCookiePath() {
		return application.getConfigProperties().getProperty("cookie.path", super.getCookiePath());
	}
	
	public String getCookieName(){
		return "buddi_auth";
	}
	
	@Override
	public String getRegisterFields() {
		final ResourceBundle t = LocaleUtil.getTranslation();
		final StringBuilder sb = new StringBuilder();
		sb.append("[");

		// Locale select
		sb.append("{\"type\":\"select\",\"name\":\"locale\",\"label\":").append(jsonString(t.getString("LOCALE")))
		  .append(",\"defaultValue\":\"en_US\",\"help\":").append(jsonString(t.getString("HELP_LOCALE")))
		  .append(",\"options\":[");
		final Locale[] commonLocales = new Locale[]{ Locale.CANADA, Locale.US, Locale.UK, new Locale("es","ES"), Locale.GERMANY, Locale.ITALY };
		for (int i = 0; i < commonLocales.length; i++) {
			if (i > 0) sb.append(",");
			sb.append("{\"text\":").append(jsonString(commonLocales[i].getDisplayName(Locale.ENGLISH)))
			  .append(",\"value\":").append(jsonString(commonLocales[i].toString())).append("}");
		}
		sb.append(",{\"text\":\"---\",\"value\":\"\",\"disabled\":true}");
		final java.util.Set<Locale> allLocales = new java.util.TreeSet<>(new java.util.Comparator<Locale>() {
			public int compare(Locale a, Locale b) { return a.getDisplayName(Locale.ENGLISH).compareTo(b.getDisplayName(Locale.ENGLISH)); }
		});
		for (Locale l : Locale.getAvailableLocales()) {
			if (l.getCountry() != null && !l.getCountry().isEmpty()) allLocales.add(l);
		}
		allLocales.removeAll(java.util.Arrays.asList(commonLocales));
		for (Locale l : allLocales) {
			sb.append(",{\"text\":").append(jsonString(l.getDisplayName(Locale.ENGLISH)))
			  .append(",\"value\":").append(jsonString(l.toString())).append("}");
		}
		sb.append("]},");

		// Currency select
		sb.append("{\"type\":\"select\",\"name\":\"currency\",\"label\":").append(jsonString(t.getString("CURRENCY")))
		  .append(",\"defaultValue\":\"USD\",\"help\":").append(jsonString(t.getString("HELP_CURRENCY")))
		  .append(",\"options\":[");
		final String[] commonCurrencies = new String[]{ "CAD", "USD", "EUR", "GBP", "AUD" };
		for (int i = 0; i < commonCurrencies.length; i++) {
			if (i > 0) sb.append(",");
			sb.append("{\"text\":").append(jsonString(commonCurrencies[i]))
			  .append(",\"value\":").append(jsonString(commonCurrencies[i])).append("}");
		}
		sb.append(",{\"text\":\"---\",\"value\":\"\",\"disabled\":true}");
		final java.util.Set<String> allCurrencies = new java.util.TreeSet<>();
		for (Locale l : Locale.getAvailableLocales()) {
			try { allCurrencies.add(java.util.Currency.getInstance(l).getCurrencyCode()); } catch (Exception e2) {}
		}
		allCurrencies.removeAll(java.util.Arrays.asList(commonCurrencies));
		for (String c : allCurrencies) {
			sb.append(",{\"text\":").append(jsonString(c)).append(",\"value\":").append(jsonString(c)).append("}");
		}
		sb.append("]},");

		// Terms checkbox
		sb.append("{\"type\":\"checkbox\",\"name\":\"agree\",\"checkboxLabel\":").append(jsonString(t.getString("AGREE_TERMS_AND_CONDITIONS")))
		  .append(",\"help\":").append(jsonString(t.getString("CREATE_USER_AGREEMENT_REQUIRED"))).append("},");

		// Help text
		sb.append("{\"type\":\"html\",\"html\":").append(jsonString(t.getString("HELP_REGISTER"))).append("}");

		sb.append("]");
		return sb.toString();
	}

	private static String jsonString(String s) {
		if (s == null) return "null";
		return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") + "\"";
	}

	@Override
	public Hash getHash() {
		return new DefaultHash().setAlgorithm("SHA-512").setIterations(20000).setSaltLength(96);
	}
	
	private String getHashedUsername(String username) {
		return new DefaultHash().setSaltLength(0).setIterations(1).generate(username);
	}
}
