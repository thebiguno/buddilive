package ca.digitalcave.buddi.web.db;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import ca.digitalcave.buddi.web.BuddiApplication;
import ca.digitalcave.buddi.web.db.bd.BusinessDelegate;
import ca.digitalcave.buddi.web.db.bd.DataConstraintException;
import ca.digitalcave.buddi.web.model.Split;
import ca.digitalcave.buddi.web.model.Transaction;
import ca.digitalcave.buddi.web.model.User;

public class TransactionsBD extends BusinessDelegate implements Transactions {

	public TransactionsBD(BuddiApplication application, SqlSessionFactory sqlSessionFactory) {
		super(application, sqlSessionFactory);
	}
	
	@Override
	public Integer insertTransaction(User user, Transaction transaction) throws DataConstraintException {
		final SqlSession s = getSqlSessionFactory().openSession();
		try {
			//Perform integrity checks
			if (transaction.getSplits() == null || transaction.getSplits().size() == 0) throw new DataConstraintException("A transaction must contain at least one split.");
			if (transaction.getDate() == null) throw new DataConstraintException("The transaction date must be set");
			
			Integer count = s.getMapper(Transactions.class).insertTransaction(user, transaction);
			for (Split split : transaction.getSplits()) {
				split.setTransactionId(transaction.getId());
				s.getMapper(Transactions.class).insertSplit(user, split);
			}
			s.commit();
			return count;
		}
		finally {
			s.close();
		}
	}
	
	@Override
	public Integer insertSplit(User user, Split split) throws DataConstraintException {
		throw new RuntimeException("Not implemented");
	}
	
	@Override
	public Transaction selectTransaction(User user, Long id) {
		final SqlSession s = getSqlSessionFactory().openSession(true);
		try {
			return s.getMapper(Transactions.class).selectTransaction(user, id);
		}
		finally {
			s.close();
		}
	}
	
	@Override
	public Transaction selectTransaction(User user, String uuid) {
		final SqlSession s = getSqlSessionFactory().openSession(true);
		try {
			return s.getMapper(Transactions.class).selectTransaction(user, uuid);
		}
		finally {
			s.close();
		}
	}
	
	@Override
	public List<Transaction> selectTransactions(@Param("user") User user) {
		final SqlSession s = getSqlSessionFactory().openSession(true);
		try {
			return s.getMapper(Transactions.class).selectTransactions(user);
		}
		finally {
			s.close();
		}
	}
}
