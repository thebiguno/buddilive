package ca.digitalcave.buddi.live;

import java.util.Locale;
import java.util.Properties;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.data.Language;
import org.restlet.engine.application.Encoder;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.routing.TemplateRoute;

import ca.digitalcave.buddi.live.db.liquibase.Migration;
import ca.digitalcave.buddi.live.resource.FreemarkerResource;
import ca.digitalcave.buddi.live.resource.IndexResource;
import ca.digitalcave.buddi.live.resource.buddilive.AccountsResource;
import ca.digitalcave.buddi.live.resource.buddilive.CategoriesResource;
import ca.digitalcave.buddi.live.resource.buddilive.CreateUserResource;
import ca.digitalcave.buddi.live.resource.buddilive.DescriptionsResource;
import ca.digitalcave.buddi.live.resource.buddilive.EntriesResource;
import ca.digitalcave.buddi.live.resource.buddilive.ParentsResource;
import ca.digitalcave.buddi.live.resource.buddilive.PeriodsResource;
import ca.digitalcave.buddi.live.resource.buddilive.ScheduledTransactionsResource;
import ca.digitalcave.buddi.live.resource.buddilive.SourcesResource;
import ca.digitalcave.buddi.live.resource.buddilive.TransactionsResource;
import ca.digitalcave.buddi.live.resource.buddilive.UserPreferencesResource;
import ca.digitalcave.buddi.live.resource.data.BackupResource;
import ca.digitalcave.buddi.live.resource.data.RestoreResource;
import ca.digitalcave.buddi.live.resource.data.UsersDataResource;
import ca.digitalcave.buddi.live.security.AddressFilter;
import ca.digitalcave.buddi.live.security.BuddiAuthenticator;
import ca.digitalcave.buddi.live.security.BuddiVerifier;
import ca.digitalcave.buddi.live.service.BuddiStatusService;
import ca.digitalcave.buddi.live.util.CryptoUtil;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;


public class BuddiApplication extends Application{
	private Configuration freemarkerConfiguration;
	private SqlSessionFactory sqlSessionFactory;


	@Override  
	public synchronized Restlet createInboundRoot() {  
		getMetadataService().setDefaultLanguage(Language.ENGLISH);
		getMetadataService().setEnabled(true);
		getTunnelService().setEnabled(true);
		getTunnelService().setExtensionsTunnel(true);

		final Router router = new Router(getContext());
		//Handles main page + login / logout
		router.attach("/", new BuddiAuthenticator(this, getContext(), true, IndexResource.class));
		
		//Handles the desktop GUI stuff
		router.attach("/buddilive/createaccount", new BuddiAuthenticator(this, getContext(), true, CreateUserResource.class));
		router.attach("/buddilive/accounts", new BuddiAuthenticator(this, getContext(), false, AccountsResource.class));
		router.attach("/buddilive/categories", new BuddiAuthenticator(this, getContext(), false, CategoriesResource.class));
		router.attach("/buddilive/categories/periods", new BuddiAuthenticator(this, getContext(), false, PeriodsResource.class));
		router.attach("/buddilive/categories/entries", new BuddiAuthenticator(this, getContext(), false, EntriesResource.class));
		router.attach("/buddilive/categories/parents", new BuddiAuthenticator(this, getContext(), false, ParentsResource.class));
		router.attach("/buddilive/transactions", new BuddiAuthenticator(this, getContext(), false, TransactionsResource.class));
		router.attach("/buddilive/transactions/descriptions", new BuddiAuthenticator(this, getContext(), false, DescriptionsResource.class));
		router.attach("/buddilive/scheduledtransactions", new BuddiAuthenticator(this, getContext(), false, ScheduledTransactionsResource.class));
		router.attach("/buddilive/sources/from", new BuddiAuthenticator(this, getContext(), false, SourcesResource.class));
		router.attach("/buddilive/sources/to", new BuddiAuthenticator(this, getContext(), false, SourcesResource.class));
		router.attach("/buddilive/userpreferences", new BuddiAuthenticator(this, getContext(), false, UserPreferencesResource.class));
		
		//Handles non-GUI data import / export
		router.attach("/data/backup", new BuddiAuthenticator(this, getContext(), false, BackupResource.class));
		router.attach("/data/restore", new BuddiAuthenticator(this, getContext(), false, RestoreResource.class));
		router.attach("/data/users", new BuddiAuthenticator(this, getContext(), true, UsersDataResource.class));
		
		//Public data and binary data which should not be filtered through freemarker
		router.attach("/img", new Directory(getContext(), "war:///img"));
		router.attach("/extjs", new Directory(getContext(), "war:///extjs"));
		router.attach("/touch", new Directory(getContext(), "war:///touch"));
		
		//Everything else is filtered through Freemarker, passing in the user object (locales + preferences)
		final TemplateRoute route = router.attach("/", new BuddiAuthenticator(this, getContext(), true, FreemarkerResource.class));
		route.setMatchingMode(Template.MODE_STARTS_WITH);

		final Encoder encoder = new Encoder(getContext());
		encoder.setNext(router);
		
		final AddressFilter addressFilter = new AddressFilter(getContext());
		addressFilter.setNext(encoder);
		
		return addressFilter;
	}

	public synchronized void start() throws Exception {
		final Properties p = new Properties();
		p.load(new ClientResource(getContext(), "war:///WEB-INF/classes/config.properties").get().getStream());
		BuddiVerifier.COOKIE_PASSWORD = p.getProperty("verifier.encryptionKey", new String(CryptoUtil.getSecureRandom(64)));
		final ComboPooledDataSource ds;

		ds = new ComboPooledDataSource();
		ds.setDriverClass(p.getProperty("db.driver"));
		ds.setJdbcUrl(p.getProperty("db.url"));
		ds.setUser(p.getProperty("db.user"));
		ds.setPassword(p.getProperty("db.password"));
		ds.setPreferredTestQuery(p.getProperty("db.query"));
		ds.setTestConnectionOnCheckin(false);
		ds.setTestConnectionOnCheckout(true);
		ds.setDebugUnreturnedConnectionStackTraces(true);
		ds.setUnreturnedConnectionTimeout(2 * 60); // 2 minutes

		org.apache.ibatis.logging.LogFactory.useJdkLogging();
		final SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
		final Environment environment = new Environment("prod", new JdbcTransactionFactory(), ds);
		final org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration(environment);
		configuration.addMappers("ca.digitalcave.buddi.live.db");
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
		freemarkerConfiguration.setServletContextForTemplateLoading(getContext().getServerDispatcher().getContext().getAttributes().get("org.restlet.ext.servlet.ServletContext"), "/");
		this.freemarkerConfiguration = freemarkerConfiguration;

		setStatusService(new BuddiStatusService());

		Migration.migrate(sqlSessionFactory, getContext());

		super.start();
	}
	
	@Override
	public synchronized void stop() throws Exception {
		super.stop();
	}
	
	public SqlSessionFactory getSqlSessionFactory() {
		return sqlSessionFactory;
	}
	
	public Configuration getFreemarkerConfiguration() {
		return freemarkerConfiguration;
	}
}
