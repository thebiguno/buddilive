package ca.digitalcave.buddi.web.db;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import ca.digitalcave.buddi.web.BuddiApplication;
import ca.digitalcave.buddi.web.db.dao.DAO;
import ca.digitalcave.buddi.web.model.Transaction;
import ca.digitalcave.buddi.web.model.User;

public class TransactionsDAO extends DAO implements Transactions {

	public TransactionsDAO(BuddiApplication application, SqlSessionFactory sqlSessionFactory) {
		super(application, sqlSessionFactory);
	}
	
	@Override
	public Integer insertTransaction(User user, Transaction transaction) {
		final SqlSession s = getSqlSessionFactory().openSession();
		try {
			Integer count = s.getMapper(Transactions.class).insertTransaction(user, transaction);
			s.commit();
			return count;
		}
		finally {
			s.close();
		}
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
