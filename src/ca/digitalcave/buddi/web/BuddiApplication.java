package ca.digitalcave.buddi.web;

import java.util.Locale;
import java.util.Properties;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.codehaus.jackson.JsonFactory;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.data.Language;
import org.restlet.engine.application.Encoder;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;

import ca.digitalcave.buddi.web.db.migrate.Migration;
import ca.digitalcave.buddi.web.resource.IndexResource;
import ca.digitalcave.buddi.web.security.AddressFilter;
import ca.digitalcave.buddi.web.security.BuddiAuthenticator;
import ca.digitalcave.buddi.web.service.BuddiStatusService;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;


public class BuddiApplication extends Application{
	private SqlSessionFactory sqlSessionFactory;
	private final JsonFactory jsonFactory = new JsonFactory();
	private ComboPooledDataSource ds;
	private Configuration freemarkerConfiguration;

	@Override  
	public synchronized Restlet createInboundRoot() {  
		getMetadataService().setDefaultLanguage(Language.ENGLISH);
		getMetadataService().setEnabled(true);
		getTunnelService().setEnabled(true);
		getTunnelService().setExtensionsTunnel(true);

		final Router router = new Router(getContext());
//		router.attach("/scripts", new BuddiAuthenticator(this, getContext(), false, ScriptsResource.class));
//		router.attach("/script/{name}", new BuddiAuthenticator(this, getContext(), false, ScriptResource.class));
//		router.attach("/parameters", new BuddiAuthenticator(this, getContext(), false, ParametersResource.class));

		final Directory directory = new Directory(getContext(), "war:///");
		directory.setListingAllowed(true);
		
		router.attach("/", new BuddiAuthenticator(this, getContext(), true, IndexResource.class));
		router.attachDefault(directory);
		
		final Encoder encoder = new Encoder(getContext());
		encoder.setNext(router);
		
		final AddressFilter addressFilter = new AddressFilter(getContext());
		addressFilter.setNext(encoder);
		
		return addressFilter;
	}

	public synchronized void start() throws Exception {
		final Properties p = new Properties();
		p.load(new ClientResource(getContext(), "war:///WEB-INF/buddi.properties").get().getStream());

		ds = new ComboPooledDataSource();
		ds.setDriverClass(p.getProperty("db.driver"));
		ds.setJdbcUrl(p.getProperty("db.url"));
//		ds.setUser(p.getProperty("db.user"));
//		ds.setPassword(p.getProperty("db.password"));
		ds.setPreferredTestQuery(p.getProperty("db.query"));
		ds.setTestConnectionOnCheckin(false);
		ds.setTestConnectionOnCheckout(true);
		ds.setDebugUnreturnedConnectionStackTraces(true);
		ds.setUnreturnedConnectionTimeout(2 * 60); // 2 minutes

		org.apache.ibatis.logging.LogFactory.useJdkLogging();
		final SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
		final Environment environment = new Environment("prod", new JdbcTransactionFactory(), ds);
		final org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration(environment);
		configuration.addMappers("ca.digitalcave.buddi.web.db");
		sqlSessionFactory = sqlSessionFactoryBuilder.build(configuration);

		
		//***** Freemarker Configuration *****
		freemarker.template.Configuration freemarkerConfiguration = new freemarker.template.Configuration();
		freemarkerConfiguration.setDefaultEncoding("UTF-8");
		freemarkerConfiguration.setLocalizedLookup(false);
		freemarkerConfiguration.setLocale(Locale.ENGLISH);
		freemarkerConfiguration.setTemplateUpdateDelay(0);
		freemarkerConfiguration.setObjectWrapper(new BeansWrapper());
		freemarkerConfiguration.setDateFormat("yyyy'-'MM'-'dd");
		freemarkerConfiguration.setDateTimeFormat("yyyy'-'MM'-'dd' 'HH:mm");
		freemarkerConfiguration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		freemarkerConfiguration.setServletContextForTemplateLoading(getContext().getServerDispatcher().getContext().getAttributes().get("org.restlet.ext.servlet.ServletContext"), "/WEB-INF/ftl/");
		this.freemarkerConfiguration = freemarkerConfiguration;

		setStatusService(new BuddiStatusService());

		Migration.migrate(sqlSessionFactory, p.getProperty("db.schema", "buddi"));

		super.start();
	}
	
	@Override
	public synchronized void stop() throws Exception {
		super.stop();
	}
	
	public SqlSessionFactory getSqlSessionFactory() {
		return sqlSessionFactory;
	}

	public JsonFactory getJsonFactory() {
		return jsonFactory;
	}
	public Configuration getFreemarkerConfiguration() {
		return freemarkerConfiguration;
	}
}
