package ca.digitalcave.buddi.live.db.liquibase;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import liquibase.Liquibase;
import liquibase.resource.ResourceAccessor;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.ValidationFailedException;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.restlet.Context;
import org.restlet.resource.ClientResource;

public class Migration {

	public static void migrate(SqlSessionFactory sqlSessionFactory, Context context) throws Exception {
		final SqlSession sqlSession = sqlSessionFactory.openSession();
		try {
			final Connection conn = sqlSession.getConnection();
			final Liquibase liquibase = new Liquibase("liquibase/master.xml", new ContextResourceAccessor(context), new JdbcConnection(conn));
			try {
				liquibase.update(null);
			} catch (ValidationFailedException e) {
				Logger.getLogger(Migration.class.getName()).log(Level.INFO, "Validation Failed", e);
			}
		} finally {
			sqlSession.close();
		}
	}
	
	static class ContextResourceAccessor implements ResourceAccessor {
		private final Context context;
		public ContextResourceAccessor(Context context) {
			this.context = context;
		}
		
		public ClassLoader toClassLoader() {
			return context.getClass().getClassLoader();
		}
		
		public Enumeration<URL> getResources(String name) throws IOException {
			return toClassLoader().getResources(name);
		}
		
		public InputStream getResourceAsStream(String name) throws IOException {
			return new ClientResource(context, "war:///WEB-INF/" + name).get().getStream();
		}
	};
}
