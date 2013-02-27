package ca.digitalcave.buddi.web.db.util;

import java.util.List;

import org.apache.ibatis.session.SqlSession;

import ca.digitalcave.buddi.web.db.Sources;
import ca.digitalcave.buddi.web.model.Source;
import ca.digitalcave.buddi.web.model.Transaction;
import ca.digitalcave.buddi.web.model.User;

public class ConstraintsChecker {

	public static void checkInsertSource(Source source, User user, SqlSession sqlSession) throws DataConstraintException {
		//Perform some checks that we cannot do via Derby DB constraints
		if (source.getParent() != null){
			final Source parent = sqlSession.getMapper(Sources.class).selectSource(user, source.getParent());
			if (parent.isAccount()){
				throw new DataConstraintException("The parent of a category cannot be an account");
			}
			if (parent.getUserId() != source.getUserId()){
				throw new DataConstraintException("The userId of a parent category must match the userId of the child category");
			}
		}
		if (source.isAccount()){
			List<Source> accounts = sqlSession.getMapper(Sources.class).selectAccountsByAccountType(user, source.getAccountType());
			for (Source account : accounts) {
				if (!account.getType().equals(source.getType())){
					if ("D".equals(source.getType())) throw new DataConstraintException(String.format("You cannot add a debit account to account type %s when there are already credit accounts assigned to the same account type.", source.getAccountType()));
					else throw new DataConstraintException(String.format("You cannot add a credit account to account type %s when there are already debit accounts assigned to the same account type.", source.getAccountType()));
				}
			}
		}
	}
	
	public static void checkInsertTransaction(Transaction transaction, SqlSession sqlSession) throws DataConstraintException {
		//Perform integrity checks
		if (transaction.getSplits() == null || transaction.getSplits().size() == 0) throw new DataConstraintException("A transaction must contain at least one split.");
		if (transaction.getDate() == null) throw new DataConstraintException("The transaction date must be set");

	}
}
