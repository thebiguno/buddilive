package ca.digitalcave.buddi.live.db.util;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.restlet.Application;

import ca.digitalcave.buddi.live.BuddiApplication;
import ca.digitalcave.buddi.live.db.Entries;
import ca.digitalcave.buddi.live.db.Sources;
import ca.digitalcave.buddi.live.db.Users;
import ca.digitalcave.buddi.live.model.Account;
import ca.digitalcave.buddi.live.model.Category;
import ca.digitalcave.buddi.live.model.Entry;
import ca.digitalcave.buddi.live.model.ScheduledTransaction;
import ca.digitalcave.buddi.live.model.Source;
import ca.digitalcave.buddi.live.model.Split;
import ca.digitalcave.buddi.live.model.Transaction;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.moss.crypto.Base64;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;

public class ConstraintsChecker {

	public static void checkInsertCategory(Category category, User user, SqlSession sqlSession) throws DatabaseException, CryptoException {
		//Perform some checks that we cannot do via Derby DB constraints
		if (category.getParent() != null){
			final Source parent = sqlSession.getMapper(Sources.class).selectSource(user, category.getParent());
			if (parent.isAccount()){
				throw new DatabaseException("The parent of a category cannot be an account");
			}
			if (parent.getUserId() != category.getUserId()){
				throw new DatabaseException("The userId of a parent category must match the userId of the child category");
			}
			//Check for loops / non matching types in the parentage
			final Map<Integer, Category> categories = sqlSession.getMapper(Sources.class).selectCategoriesMap(user);
			Category p = category;
			while (p.getParent() != null){
				p = categories.get(p.getParent());
				if (p.getId() == category.getId()) throw new DatabaseException("Loop detected in category parentage");
				if (!p.getType().equals(category.getType())) throw new DatabaseException("The type of a parent must match the type of the child");
				if (!p.getPeriodType().equals(category.getPeriodType())) throw new DatabaseException("The period of a parent must match the period of the child");
			}
		}

		if (user.isEncrypted() && !isEncryptedValue(category.getName())){
			final BuddiApplication application = (BuddiApplication) Application.getCurrent();
			category.setName(application.getCrypto().encrypt(user.getDecryptedEncryptionKey(), category.getName()));
		}
	}

	public static void checkUpdateCategory(Category category, User user, SqlSession sqlSession) throws DatabaseException, CryptoException {
		if (category.getId() == null) throw new DatabaseException("The id must be set to perform an update");
		checkInsertCategory(category, user, sqlSession);
	}

	public static void checkInsertAccount(Account account, User user, SqlSession sqlSession) throws DatabaseException, CryptoException {
		if (account.isAccount()){
			final List<Account> accounts = sqlSession.getMapper(Sources.class).selectAccounts(user, account.getAccountType());
			for (Account a : accounts) {
				if (!a.getType().equals(account.getType())){
					if ("D".equals(account.getType())) throw new DatabaseException(String.format("There is already an account type '%s' for a credit account.  Please change the account type, or set the type to credit.", account.getAccountType()));
					else throw new DatabaseException(String.format("There is already an account type '%s' for a debit account.  Please change the account type, or set the type to debit.", account.getAccountType()));
				}
			}
		}
		final BuddiApplication application = (BuddiApplication) Application.getCurrent();
		if (user.isEncrypted() && !isEncryptedValue(account.getName())){
			account.setName(application.getCrypto().encrypt(user.getDecryptedEncryptionKey(), account.getName()));
		}
		if (user.isEncrypted() && !isEncryptedValue(account.getAccountType())){
			account.setAccountType(application.getCrypto().encrypt(user.getDecryptedEncryptionKey(), account.getAccountType()));
		}
	}

	public static void checkUpdateAccount(Account account, User user, SqlSession sqlSession) throws DatabaseException, CryptoException {
		if (account.getId() == null) throw new DatabaseException("The id must be set to perform an update");
		checkInsertAccount(account, user, sqlSession);
	}

	public static void checkInsertTransaction(Transaction transaction, User user, SqlSession sqlSession) throws DatabaseException, CryptoException {
		//Perform integrity checks
		final BuddiApplication application = (BuddiApplication) Application.getCurrent();
		if (transaction.getSplits() == null || transaction.getSplits().size() == 0) throw new DatabaseException("A transaction must contain at least one split.");
		if (transaction.getDate() == null) throw new DatabaseException("The transaction date must be set");
		for (Split split : transaction.getSplits()) {
			if (split.getAmount().compareTo(BigDecimal.ZERO) == 0) throw new DatabaseException("Splits cannot have amounts equal to zero.");

			final Source fromSource = sqlSession.getMapper(Sources.class).selectSource(user, split.getFromSource());
			final Source toSource = sqlSession.getMapper(Sources.class).selectSource(user, split.getToSource());
			if (!fromSource.isAccount() && !toSource.isAccount()) throw new DatabaseException("From and To cannot both be categories");
			if (fromSource.getId() == toSource.getId()) throw new DatabaseException("From and To cannot be the same");

			if (user.isEncrypted() && !isEncryptedValue(split.getMemo())){
				split.setMemo(application.getCrypto().encrypt(user.getDecryptedEncryptionKey(), split.getMemo()));
			}
		}

		if (user.isEncrypted() && !isEncryptedValue(transaction.getDescription())){
			transaction.setDescription(application.getCrypto().encrypt(user.getDecryptedEncryptionKey(), transaction.getDescription()));
		}
		if (user.isEncrypted() && !isEncryptedValue(transaction.getNumber())){
			transaction.setNumber(application.getCrypto().encrypt(user.getDecryptedEncryptionKey(), transaction.getNumber()));
		}
	}

	public static void checkUpdateTransaction(Transaction transaction, User user, SqlSession sqlSession) throws DatabaseException, CryptoException {
		if (transaction.getId() == null) throw new DatabaseException("The id must be set to perform an update");
		checkInsertTransaction(transaction, user, sqlSession);
	}

	public static void checkInsertScheduledTransaction(ScheduledTransaction scheduledTransaction, User user, SqlSession sqlSession) throws DatabaseException, CryptoException {
		//Perform integrity checks
		final BuddiApplication application = (BuddiApplication) Application.getCurrent();
		if (scheduledTransaction.getSplits() == null || scheduledTransaction.getSplits().size() == 0) throw new DatabaseException("A transaction must contain at least one split.");
		for (Split split : scheduledTransaction.getSplits()) {
			if (split.getAmount().compareTo(BigDecimal.ZERO) == 0) throw new DatabaseException("Splits cannot have amounts equal to zero.");

			final Source fromSource = sqlSession.getMapper(Sources.class).selectSource(user, split.getFromSource());
			final Source toSource = sqlSession.getMapper(Sources.class).selectSource(user, split.getToSource());
			if (!fromSource.isAccount() && !toSource.isAccount()) throw new DatabaseException("From and To cannot both be categories");
			if (fromSource.getId() == toSource.getId()) throw new DatabaseException("From and To cannot be the same");

			if (user.isEncrypted() && !isEncryptedValue(split.getMemo())){
				split.setMemo(application.getCrypto().encrypt(user.getDecryptedEncryptionKey(), split.getMemo()));
			}
		}

		if (user.isEncrypted() && !isEncryptedValue(scheduledTransaction.getScheduleName())){
			scheduledTransaction.setScheduleName(application.getCrypto().encrypt(user.getDecryptedEncryptionKey(), scheduledTransaction.getScheduleName()));
		}
		if (user.isEncrypted() && !isEncryptedValue(scheduledTransaction.getDescription())){
			scheduledTransaction.setDescription(application.getCrypto().encrypt(user.getDecryptedEncryptionKey(), scheduledTransaction.getDescription()));
		}
		if (user.isEncrypted() && !isEncryptedValue(scheduledTransaction.getNumber())){
			scheduledTransaction.setNumber(application.getCrypto().encrypt(user.getDecryptedEncryptionKey(), scheduledTransaction.getNumber()));
		}
		if (user.isEncrypted() && !isEncryptedValue(scheduledTransaction.getScheduleName())){
			scheduledTransaction.setScheduleName(application.getCrypto().encrypt(user.getDecryptedEncryptionKey(), scheduledTransaction.getScheduleName()));
		}
		if (user.isEncrypted() && !isEncryptedValue(scheduledTransaction.getMessage())){
			scheduledTransaction.setMessage(application.getCrypto().encrypt(user.getDecryptedEncryptionKey(), scheduledTransaction.getMessage()));
		}
	}

	public static void checkUpdateScheduledTransaction(ScheduledTransaction scheduledTransaction, User user, SqlSession sqlSession) throws DatabaseException, CryptoException {
		if (scheduledTransaction.getId() == null) throw new DatabaseException("The id must be set to perform an update");
		checkInsertScheduledTransaction(scheduledTransaction, user, sqlSession);
	}

	public static void checkInsertEntry(Entry entry, User user, SqlSession sqlSession) throws DatabaseException {
		if (entry.getAmount() == null) entry.setAmount(BigDecimal.ZERO);
		if (entry.getCategoryId() == 0) throw new DatabaseException("The category id must be set");
		if (entry.getDate() == null) throw new DatabaseException("The date must be set");

		if (sqlSession.getMapper(Sources.class).selectCategory(user, entry.getCategoryId()) == null) throw new DatabaseException("The specified category is not valid");
	}
	public static void checkUpdateEntry(Entry entry, User user, SqlSession sqlSession) throws DatabaseException {
		if (sqlSession.getMapper(Entries.class).selectEntry(user, entry) == null) throw new DatabaseException("Could not find an entry to update");
		checkInsertEntry(entry, user, sqlSession);
	}

	public static void checkInsertUser(User user, SqlSession sqlSession) throws DatabaseException {
		if (sqlSession.getMapper(Users.class).selectUser(user.getIdentifier()) != null) throw new DatabaseException("The user name already exists");
	}

	public static void checkUpdateUserPreferences(User user, SqlSession sqlSession) throws DatabaseException {
		if (!user.isPremium()) user.setShowCleared(false);
		if (!user.isPremium()) user.setShowReconciled(false);
	}
	
	private static boolean isEncryptedValue(String value){
		if (StringUtils.isBlank(value)) return false;
		final String[] split = value.split(":");
		//Depending on the encryption scheme used, the length will be one of 3, 4, or 5.
		if (split.length != 3 && split.length != 4 && split.length != 5) {
			return false;
		}
		
		try {
			//Nothing is really special about 4 here... it is just a small value which is smaller than any valid value I have seen.  We could probably calculate what the actual value is, but this is fine for now.
			//Try decoding the last segment; this is always going to be the message.
			if (Base64.decode(split[split.length - 1]).length <= 4) return false;	
		}
		catch (Throwable e){
			return false;
		}
		
		return true;
	}
}
