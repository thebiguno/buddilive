package ca.digitalcave.buddi.live.db.migrate;

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.ValidationFailedException;
import liquibase.resource.ClassLoaderResourceAccessor;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

public class Migration {

	public static void migrate(SqlSessionFactory sqlSessionFactory, String schema) throws Exception {
		final SqlSession sqlSession = sqlSessionFactory.openSession();
		try {
			final Connection conn = sqlSession.getConnection();
			final Liquibase liquibase = new Liquibase("ca/digitalcave/buddi/live/db/migrate/master.xml", new ClassLoaderResourceAccessor(), new JdbcConnection(conn));
			if (StringUtils.isNotBlank(schema)) {
				liquibase.getDatabase().setDefaultSchemaName(schema);
			}
			try {
				liquibase.update(null);
			} catch (ValidationFailedException e) {
				Logger.getLogger(Migration.class.getName()).log(Level.INFO, "Validation Failed", e);
			}
		} finally {
			sqlSession.close();
		}
	}
}
