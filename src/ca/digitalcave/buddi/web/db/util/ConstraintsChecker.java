package ca.digitalcave.buddi.web.db.util;

import java.util.List;

import org.apache.ibatis.session.SqlSession;

import ca.digitalcave.buddi.web.db.Sources;
import ca.digitalcave.buddi.web.model.Source;
import ca.digitalcave.buddi.web.model.Transaction;
import ca.digitalcave.buddi.web.model.User;

public class ConstraintsChecker {

	public static void checkInsertSource(Source source, User user, SqlSession sqlSession) throws DatabaseException {
		//Perform some checks that we cannot do via Derby DB constraints
		if (source.getParent() != null){
			final Source parent = sqlSession.getMapper(Sources.class).selectSource(user, source.getParent());
			if (parent.isAccount()){
				throw new DatabaseException("The parent of a category cannot be an account");
			}
			if (parent.getUserId() != source.getUserId()){
				throw new DatabaseException("The userId of a parent category must match the userId of the child category");
			}
		}
		if (source.isAccount()){
			List<Source> accounts = sqlSession.getMapper(Sources.class).selectAccountsByAccountType(user, source.getAccountType());
			for (Source account : accounts) {
				if (!account.getType().equals(source.getType())){
					if ("D".equals(source.getType())) throw new DatabaseException(String.format("There is already an account type '%s' for a credit account.  Please change the account type, or set the type to credit.", source.getAccountType()));
					else throw new DatabaseException(String.format("There is already an account type '%s' for a debit account.  Please change the account type, or set the type to debit.", source.getAccountType()));
				}
			}
		}
	}
	
	public static void checkInsertTransaction(Transaction transaction, SqlSession sqlSession) throws DatabaseException {
		//Perform integrity checks
		if (transaction.getSplits() == null || transaction.getSplits().size() == 0) throw new DatabaseException("A transaction must contain at least one split.");
		if (transaction.getDate() == null) throw new DatabaseException("The transaction date must be set");

	}
}
