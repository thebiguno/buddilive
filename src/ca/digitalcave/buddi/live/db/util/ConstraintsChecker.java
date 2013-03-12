package ca.digitalcave.buddi.live.db.util;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import ca.digitalcave.buddi.live.db.Entries;
import ca.digitalcave.buddi.live.db.Sources;
import ca.digitalcave.buddi.live.db.Users;
import ca.digitalcave.buddi.live.model.Account;
import ca.digitalcave.buddi.live.model.Category;
import ca.digitalcave.buddi.live.model.Entry;
import ca.digitalcave.buddi.live.model.Source;
import ca.digitalcave.buddi.live.model.Split;
import ca.digitalcave.buddi.live.model.Transaction;
import ca.digitalcave.buddi.live.model.User;

public class ConstraintsChecker {

	public static void checkInsertCategory(Category category, User user, SqlSession sqlSession) throws DatabaseException {
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
			final Map<Integer, Category> categories = sqlSession.getMapper(Sources.class).selectCategoriesByParentId(user);
			Category p = category;
			while (p.getParent() != null){
				p = categories.get(p.getParent());
				if (p.getId() == category.getId()) throw new DatabaseException("Loop detected in category parentage");
				if (!p.getType().equals(category.getType())) throw new DatabaseException("The type of a parent must match the type of the child");
				if (!p.getPeriodType().equals(category.getPeriodType())) throw new DatabaseException("The period of a parent must match the period of the child");
			}
		}
	}

	public static void checkUpdateCategory(Category category, User user, SqlSession sqlSession) throws DatabaseException {
		if (category.getId() == null) throw new DatabaseException("The id must be set to perform an update");
		checkInsertCategory(category, user, sqlSession);
	}
	
	public static void checkInsertAccount(Account account, User user, SqlSession sqlSession) throws DatabaseException {
		if (account.isAccount()){
			final List<Account> accounts = sqlSession.getMapper(Sources.class).selectAccounts(user, account.getAccountType());
			for (Account a : accounts) {
				if (!a.getType().equals(account.getType())){
					if ("D".equals(account.getType())) throw new DatabaseException(String.format("There is already an account type '%s' for a credit account.  Please change the account type, or set the type to credit.", account.getAccountType()));
					else throw new DatabaseException(String.format("There is already an account type '%s' for a debit account.  Please change the account type, or set the type to debit.", account.getAccountType()));
				}
			}
		}
	}
	
	public static void checkUpdateAccount(Account account, User user, SqlSession sqlSession) throws DatabaseException {
		if (account.getId() == null) throw new DatabaseException("The id must be set to perform an update");
		checkInsertAccount(account, user, sqlSession);
	}
	
	public static void checkInsertTransaction(Transaction transaction, User user, SqlSession sqlSession) throws DatabaseException {
		//Perform integrity checks
		if (transaction.getSplits() == null || transaction.getSplits().size() == 0) throw new DatabaseException("A transaction must contain at least one split.");
		if (transaction.getDate() == null) throw new DatabaseException("The transaction date must be set");
		for (Split split : transaction.getSplits()) {
			if (split.getAmount().compareTo(BigDecimal.ZERO) == 0) throw new DatabaseException("Splits cannot have amounts equal to zero.");
			
			final Source fromSource = sqlSession.getMapper(Sources.class).selectSource(user, split.getFromSource());
			final Source toSource = sqlSession.getMapper(Sources.class).selectSource(user, split.getToSource());
			if (!fromSource.isAccount() && !toSource.isAccount()) throw new DatabaseException("From and To cannot both be categories");
			if (fromSource.getId() == toSource.getId()) throw new DatabaseException("From and To cannot be the same");
			
		}
	}
	
	public static void checkUpdateTransaction(Transaction transaction, User user, SqlSession sqlSession) throws DatabaseException {
		if (transaction.getId() == null) throw new DatabaseException("The id must be set to perform an update");
		checkInsertTransaction(transaction, user, sqlSession);
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
}
