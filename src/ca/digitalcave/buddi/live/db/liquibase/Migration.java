package ca.digitalcave.buddi.live.db.liquibase;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import liquibase.resource.AbstractResource;
import liquibase.resource.AbstractResourceAccessor;
import liquibase.resource.Resource;

public class Migration {

	public static void migrate(SqlSessionFactory sqlSessionFactory, Context context) throws Exception {
		final SqlSession sqlSession = sqlSessionFactory.openSession();
		try {
			final Connection conn = sqlSession.getConnection();
			final Liquibase liquibase = new Liquibase("liquibase/master.xml", new ContextResourceAccessor(context), new JdbcConnection(conn));
			try {
				liquibase.update(new Contexts(), new LabelExpression());
			}
			catch (ValidationFailedException e) {
				Logger.getLogger(Migration.class.getName()).log(Level.INFO, "Validation Failed", e);
			}
			finally {
				liquibase.close();
			}
		}
		finally {
			sqlSession.close();
		}
	}
	
	static class ContextResourceAccessor extends AbstractResourceAccessor {
		private final Context context;
		public ContextResourceAccessor(Context context) {
			this.context = context;
		}
		@Override
		public void close() throws Exception {
			;
		}
		@Override
		public List<String> describeLocations() {
			return new ArrayList<String>();
		}
		@Override
		public List<Resource> getAll(String path) throws IOException {
			try {
				return Collections.singletonList(new ContextPathResource(path, new URI("war:///WEB-INF/"), context));
			}
			catch (URISyntaxException e) {
				throw new IOException(e);
			}
		}
		@Override
		public List<Resource> search(String path, boolean recursive) throws IOException {
			return null;
		}
	}
	
	static class ContextPathResource extends AbstractResource {
		private final Context context;
		public ContextPathResource(String path, URI uri, Context context) {
			super(path, uri);
			this.context = context;
		}
		
		@Override
		public boolean exists() {
			return new ClientResource(context, "war:///WEB-INF/" + getPath()).get() != null;
		}

		@Override
		public InputStream openInputStream() throws IOException {
			return new ClientResource(context, "war:///WEB-INF/" + getPath()).get().getStream();
		}

		@Override
		public Resource resolve(String arg0) {
			return null;
		}

		@Override
		public Resource resolveSibling(String arg0) {
			return null;
		}
	}
}
