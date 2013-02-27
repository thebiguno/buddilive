package ca.digitalcave.buddi.web.db.dao;

import org.apache.ibatis.session.SqlSessionFactory;

import ca.digitalcave.buddi.web.BuddiApplication;

public abstract class DAO {
	private final BuddiApplication application;
	private final SqlSessionFactory sqlSessionFactory;

	public DAO(BuddiApplication application, SqlSessionFactory sqlSessionFactory) {
		this.application = application;
		this.sqlSessionFactory = sqlSessionFactory;
	}
	
	public BuddiApplication getApplication() {
		return application;
	}
	
	public SqlSessionFactory getSqlSessionFactory() {
		return sqlSessionFactory;
	}
}
