package ca.digitalcave.buddi.live.db.liquibase;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.restlet.Context;
import org.restlet.resource.ClientResource;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.ValidationFailedException;
import liquibase.resource.ResourceAccessor;

public class Migration {

	public static void migrate(SqlSessionFactory sqlSessionFactory, Context context) throws Exception {
		final SqlSession sqlSession = sqlSessionFactory.openSession();
		try {
			final Connection conn = sqlSession.getConnection();
			final Liquibase liquibase = new Liquibase("liquibase/master.xml", new ContextResourceAccessor(context), new JdbcConnection(conn));
			try {
				liquibase.update(new Contexts(), new LabelExpression());
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
			return Migration.class.getClassLoader();
		}
		
		public Enumeration<URL> getResources(String name) throws IOException {
			return toClassLoader().getResources(name);
		}
		
		public InputStream getResourceAsStream(String name) throws IOException {
			return new ClientResource(context, "war:///WEB-INF/" + name).get().getStream();
		}
		
		@Override
		public Set<InputStream> getResourcesAsStream(String name) throws IOException {
			return new HashSet<InputStream>(Collections.singletonList(new ClientResource(context, "war:///WEB-INF/" + name).get().getStream()));
		}
		
		@Override
		public Set<String> list(String arg0, String arg1, boolean arg2, boolean arg3, boolean arg4) throws IOException {
			throw new RuntimeException("Not implemented");
		}
	};
}
