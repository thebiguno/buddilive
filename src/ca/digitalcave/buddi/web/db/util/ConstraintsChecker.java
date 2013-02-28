package ca.digitalcave.buddi.web.db.util;

import java.util.List;

import org.apache.ibatis.session.SqlSession;

import ca.digitalcave.buddi.web.db.Sources;
import ca.digitalcave.buddi.web.model.Account;
import ca.digitalcave.buddi.web.model.Category;
import ca.digitalcave.buddi.web.model.Source;
import ca.digitalcave.buddi.web.model.Transaction;
import ca.digitalcave.buddi.web.model.User;

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
		}
	}
	
	public static void checkInsertAccount(Account account, User user, SqlSession sqlSession) throws DatabaseException {
		if (account.isAccount()){
			List<Account> accounts = sqlSession.getMapper(Sources.class).selectAccountsByAccountType(user, account.getAccountType());
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
	
	public static void checkInsertTransaction(Transaction transaction, SqlSession sqlSession) throws DatabaseException {
		//Perform integrity checks
		if (transaction.getSplits() == null || transaction.getSplits().size() == 0) throw new DatabaseException("A transaction must contain at least one split.");
		if (transaction.getDate() == null) throw new DatabaseException("The transaction date must be set");

	}
}
