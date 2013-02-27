package ca.digitalcave.buddi.web.db.bd;

import org.apache.ibatis.session.SqlSessionFactory;

import ca.digitalcave.buddi.web.BuddiApplication;

public abstract class BusinessDelegate {
	private final BuddiApplication application;
	private final SqlSessionFactory sqlSessionFactory;

	public BusinessDelegate(BuddiApplication application, SqlSessionFactory sqlSessionFactory) {
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
