package ca.digitalcave.buddi.web.db;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import ca.digitalcave.buddi.web.BuddiApplication;
import ca.digitalcave.buddi.web.db.dao.DAO;
import ca.digitalcave.buddi.web.db.dao.DataConstraintException;
import ca.digitalcave.buddi.web.model.Source;
import ca.digitalcave.buddi.web.model.User;

public class SourcesDAO extends DAO implements Sources{

	public SourcesDAO(BuddiApplication application, SqlSessionFactory sqlSessionFactory) {
		super(application, sqlSessionFactory);
	}
	
	@Override
	public List<Source> selectAccounts(User user) {
		final SqlSession s = getSqlSessionFactory().openSession(true);
		try {
			return s.getMapper(Sources.class).selectAccounts(user);
		}
		finally {
			s.close();
		}
	}
	
	@Override
	public List<Source> selectAccountsByAccountType(User user, String accountType) {
		final SqlSession s = getSqlSessionFactory().openSession(true);
		try {
			return s.getMapper(Sources.class).selectAccountsByAccountType(user, accountType);
		}
		finally {
			s.close();
		}
	}
	
	@Override
	public Integer insertSource(User user, Source source) throws DataConstraintException {
		final SqlSession s = getSqlSessionFactory().openSession();
		try {
			//Perform some checks that we cannot do via Derby DB constraints
			if (source.getParent() != null){
				final Source parent = selectSource(user, source.getParent());
				if (parent.isAccount()){
					throw new DataConstraintException("The parent of a category cannot be an account");
				}
				if (parent.getUserId() != source.getUserId()){
					throw new DataConstraintException("The userId of a parent category must match the userId of the child category");
				}
			}
			if (source.isAccount()){
				List<Source> accounts = selectAccountsByAccountType(user, source.getAccountType());
				for (Source account : accounts) {
					if (!account.getType().equals(source.getType())){
						if ("D".equals(source.getType())) throw new DataConstraintException(String.format("You cannot add a debit account to account type %s when there are already credit accounts assigned to the same account type.", source.getAccountType()));
						else throw new DataConstraintException(String.format("You cannot add a credit account to account type %s when there are already debit accounts assigned to the same account type.", source.getAccountType()));
					}
				}
			}
			
			Integer count = s.getMapper(Sources.class).insertSource(user, source);
			s.commit();
			return count;
		}
		finally {
			s.close();
		}
	}
	
	@Override
	public Source selectSource(User user, int id) {
		final SqlSession s = getSqlSessionFactory().openSession(true);
		try {
			return s.getMapper(Sources.class).selectSource(user, id);
		}
		finally {
			s.close();
		}
	}
	
	@Override
	public Source selectSource(User user, String uuid) {
		final SqlSession s = getSqlSessionFactory().openSession(true);
		try {
			return s.getMapper(Sources.class).selectSource(user, uuid);
		}
		finally {
			s.close();
		}
	}
	@Override
	public List<Source> selectSources(@Param("user") User user) {
		final SqlSession s = getSqlSessionFactory().openSession(true);
		try {
			return s.getMapper(Sources.class).selectSources(user);
		}
		finally {
			s.close();
		}
	}
}
