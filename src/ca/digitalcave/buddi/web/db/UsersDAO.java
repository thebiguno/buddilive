package ca.digitalcave.buddi.web.db;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import ca.digitalcave.buddi.web.BuddiApplication;
import ca.digitalcave.buddi.web.db.dao.DAO;
import ca.digitalcave.buddi.web.model.User;

public class UsersDAO extends DAO implements Users{

	public UsersDAO(BuddiApplication application, SqlSessionFactory sqlSessionFactory) {
		super(application, sqlSessionFactory);
	}
	
	@Override
	public Integer insertUser(User user) {
		final SqlSession s = getSqlSessionFactory().openSession();
		try {
			Integer count = s.getMapper(Users.class).insertUser(user);
			s.commit();
			return count;
		}
		finally {
			s.close();
		}
	}
	
	@Override
	public User selectUser(String identifier) {
		final SqlSession s = getSqlSessionFactory().openSession(true);
		try {
			return s.getMapper(Users.class).selectUser(identifier);
		}
		finally {
			s.close();
		}
	}
}
