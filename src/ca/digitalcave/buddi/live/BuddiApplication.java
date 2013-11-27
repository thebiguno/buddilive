package ca.digitalcave.buddi.live;

import java.io.IOException;
import java.io.Writer;
import java.security.Key;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.crypto.spec.PBEKeySpec;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.data.Language;
import org.restlet.data.Status;
import org.restlet.engine.application.Encoder;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Redirector;
import org.restlet.routing.Router;
import org.restlet.routing.Template;

import ca.digitalcave.buddi.live.db.handler.BooleanHandler;
import ca.digitalcave.buddi.live.db.handler.CurrencyHandler;
import ca.digitalcave.buddi.live.db.handler.LocaleHandler;
import ca.digitalcave.buddi.live.db.liquibase.Migration;
import ca.digitalcave.buddi.live.resource.DefaultResource;
import ca.digitalcave.buddi.live.resource.IndexResource;
import ca.digitalcave.buddi.live.resource.buddilive.AccountsResource;
import ca.digitalcave.buddi.live.resource.buddilive.CategoriesResource;
import ca.digitalcave.buddi.live.resource.buddilive.ChangePasswordResource;
import ca.digitalcave.buddi.live.resource.buddilive.DescriptionsResource;
import ca.digitalcave.buddi.live.resource.buddilive.EntriesResource;
import ca.digitalcave.buddi.live.resource.buddilive.ParentsResource;
import ca.digitalcave.buddi.live.resource.buddilive.PeriodsResource;
import ca.digitalcave.buddi.live.resource.buddilive.ScheduledTransactionsResource;
import ca.digitalcave.buddi.live.resource.buddilive.ScheduledTransactionsRunnerResource;
import ca.digitalcave.buddi.live.resource.buddilive.SourcesResource;
import ca.digitalcave.buddi.live.resource.buddilive.TransactionsResource;
import ca.digitalcave.buddi.live.resource.buddilive.UserPreferencesResource;
import ca.digitalcave.buddi.live.resource.buddilive.preferences.CurrenciesResource;
import ca.digitalcave.buddi.live.resource.buddilive.preferences.LocalesResource;
import ca.digitalcave.buddi.live.resource.buddilive.report.IncomeAndExpensesByCategoryResource;
import ca.digitalcave.buddi.live.resource.buddilive.report.PieTotalsByCategoryResource;
import ca.digitalcave.buddi.live.resource.data.BackupResource;
import ca.digitalcave.buddi.live.resource.data.RestoreResource;
import ca.digitalcave.buddi.live.security.BuddiVerifier;
import ca.digitalcave.buddi.live.service.BuddiStatusService;
import ca.digitalcave.moss.crypto.Crypto;
import ca.digitalcave.moss.crypto.Crypto.Algorithm;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;
import ca.digitalcave.moss.restlet.CookieAuthenticator;
import ca.digitalcave.moss.restlet.login.ExtraFieldsDirective;
import ca.digitalcave.moss.restlet.login.LoginRouter;
import ca.digitalcave.moss.restlet.login.LoginRouterConfiguration;
import ca.digitalcave.moss.restlet.util.PasswordChecker;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;


public class BuddiApplication extends Application{
	private Configuration freemarkerConfiguration;
	private SqlSessionFactory sqlSessionFactory;
	private final JsonFactory jsonFactory = new JsonFactory();
	private ComboPooledDataSource ds;
	private BuddiVerifier verifier = new BuddiVerifier();
	private String cookieKey = null;
	private Properties systemProperties = new Properties();
	private PasswordChecker passwordChecker = new PasswordChecker().setHistoryEnforced(false);

	public synchronized void start() throws Exception {
		try { systemProperties.load(new ClientResource(getContext(), "war:///WEB-INF/classes/version.properties").get().getStream()); } catch (Throwable e){}
		
		final Properties p = new Properties();
		p.load(new ClientResource(getContext(), "war:///WEB-INF/classes/config.properties").get().getStream());
		cookieKey = p.getProperty("verifier.encryptionKey", new String(new Crypto().setSaltLength(64).getRandomSalt()));
		
		ds = new ComboPooledDataSource();
		ds.setDriverClass(p.getProperty("db.driver"));
		ds.setJdbcUrl(p.getProperty("db.url"));
		ds.setUser(p.getProperty("db.user"));
		ds.setPassword(p.getProperty("db.password"));
		ds.setPreferredTestQuery(p.getProperty("db.query"));
		ds.setTestConnectionOnCheckin(true);
		ds.setIdleConnectionTestPeriod(30 * 60);	//value in seconds
		ds.setTestConnectionOnCheckout(false);
		ds.setDebugUnreturnedConnectionStackTraces(true);
		ds.setUnreturnedConnectionTimeout(10 * 60); //value in seconds.  this needs to be long enough to handle restore + encryption on my slow server

		org.apache.ibatis.logging.LogFactory.useJdkLogging();
		final SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
		final Environment environment = new Environment("prod", new JdbcTransactionFactory(), ds);
		final org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration(environment);
		configuration.getTypeHandlerRegistry().register(BooleanHandler.class);
		configuration.getTypeHandlerRegistry().register(CurrencyHandler.class);
		configuration.getTypeHandlerRegistry().register(LocaleHandler.class);
		
		configuration.addMappers("ca.digitalcave.buddi.live.db");
		
		sqlSessionFactory = sqlSessionFactoryBuilder.build(configuration);
		
		//***** Freemarker Configuration *****
		freemarker.template.Configuration freemarkerConfiguration = new freemarker.template.Configuration();
		final Object servletContext = getContext().getAttributes().get("org.restlet.ext.servlet.ServletContext");
		freemarkerConfiguration.setServletContextForTemplateLoading(servletContext, "/");
		freemarkerConfiguration.setDefaultEncoding("UTF-8");
		freemarkerConfiguration.setLocalizedLookup(false);
		freemarkerConfiguration.setLocale(Locale.ENGLISH);
		freemarkerConfiguration.setTemplateUpdateDelay(0);
		freemarkerConfiguration.setObjectWrapper(new BeansWrapper());
		freemarkerConfiguration.setDateFormat("yyyy'-'MM'-'dd");
		freemarkerConfiguration.setDateTimeFormat("yyyy'-'MM'-'dd' 'HH:mm");
		freemarkerConfiguration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		this.freemarkerConfiguration = freemarkerConfiguration;

		setStatusService(new BuddiStatusService());

		Migration.migrate(sqlSessionFactory, getContext());

		super.start();
	}
	
	@Override  
	public synchronized Restlet createInboundRoot() {  
		getMetadataService().setDefaultLanguage(Language.ENGLISH);
		getMetadataService().setEnabled(true);
		getTunnelService().setEnabled(true);
		getTunnelService().setExtensionsTunnel(true);

		final Router privateRouter = new Router(getContext());
		Key key;
		try {
			key = Crypto.recoverKey(Algorithm.AES_128, new PBEKeySpec(cookieKey.toCharArray(), new byte[]{0x00, 0x01}, 1, 128));
		} catch (CryptoException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		final CookieAuthenticator privateAuth = new CookieAuthenticator(getContext(), false, key);
		privateAuth.setVerifier(verifier);
		privateAuth.setNext(privateRouter);
		
		//Handles the desktop GUI stuff
		privateRouter.attach("/accounts", AccountsResource.class);
		privateRouter.attach("/categories", CategoriesResource.class);
		privateRouter.attach("/categories/periods", PeriodsResource.class);
		privateRouter.attach("/categories/entries", EntriesResource.class);
		privateRouter.attach("/categories/parents", ParentsResource.class);
		privateRouter.attach("/changepassword", ChangePasswordResource.class);
		privateRouter.attach("/transactions", TransactionsResource.class);
		privateRouter.attach("/transactions/descriptions", DescriptionsResource.class);
		privateRouter.attach("/scheduledtransactions", ScheduledTransactionsResource.class);
		privateRouter.attach("/scheduledtransactions/execute", ScheduledTransactionsRunnerResource.class);
		privateRouter.attach("/sources/from", SourcesResource.class);
		privateRouter.attach("/sources/to", SourcesResource.class);
		privateRouter.attach("/userpreferences", UserPreferencesResource.class);
		
		privateRouter.attach("/preferences/currencies", CurrenciesResource.class);
		privateRouter.attach("/preferences/locales", LocalesResource.class);

		privateRouter.attach("/report/pietotalsbycategory", PieTotalsByCategoryResource.class);
		privateRouter.attach("/report/incomeandexpensesbycategory", IncomeAndExpensesByCategoryResource.class);
		
		privateRouter.attach("/backup", BackupResource.class);
		privateRouter.attach("/restore", RestoreResource.class);
		
		final Router publicRouter = new Router(getContext());
		final CookieAuthenticator optionalAuth = new CookieAuthenticator(getContext(), true, key);
		optionalAuth.setVerifier(verifier);
		optionalAuth.setNext(publicRouter);
		
		//Public data and binary data which should not be filtered through freemarker
		publicRouter.attach("", new Redirector(getContext(), "index.html", Redirector.MODE_CLIENT_TEMPORARY));
		publicRouter.attach("/", new Redirector(getContext(), "index.html", Redirector.MODE_CLIENT_TEMPORARY));
		publicRouter.attach("/index", IndexResource.class);
		publicRouter.attach("/data", privateRouter);
		final LoginRouterConfiguration loginConfig = new LoginRouterConfiguration();
		final BuddiApplication application = this;
		loginConfig.extraRegisterStep1Fields = new ExtraFieldsDirective(){
			@Override
			public void writeFields(Writer out, ResourceBundle translation, ResourceBundle customTranslation) {
				try {
					final JsonGenerator generator = application.getJsonFactory().createJsonGenerator(out);
					generator.writeStartObject();
					generator.writeStringField("xtype", "selfdocumentingfield");
					generator.writeStringField("messageBody", customTranslation.getString("HELP_LOCALE"));
					generator.writeStringField("type", "localescombobox");
					generator.writeStringField("name", "locale");
					generator.writeStringField("fieldLabel", customTranslation.getString("LOCALE"));
					generator.writeStringField("value", "en_US");
					generator.writeEndObject();
					generator.writeRaw(",");
					generator.writeStartObject();
					generator.writeStringField("xtype", "selfdocumentingfield");
					generator.writeStringField("messageBody", customTranslation.getString("HELP_CURRENCY"));
					generator.writeStringField("type", "currenciescombobox");
					generator.writeStringField("name", "currency");
					generator.writeStringField("fieldLabel", customTranslation.getString("CURRENCY"));
					generator.writeStringField("value", "USD");
					generator.writeEndObject();
					generator.writeRaw(",");
					generator.writeStartObject();
					generator.writeStringField("xtype", "selfdocumentingfield");
					generator.writeStringField("messageBody", customTranslation.getString("CREATE_USER_AGREEMENT_REQUIRED"));
					generator.writeStringField("type", "checkbox");
					generator.writeStringField("boxLabel", customTranslation.getString("AGREE_TERMS_AND_CONDITIONS"));
					generator.writeStringField("name", "agree");
					generator.writeStringField("fieldLabel", " ");
					generator.writeStringField("labelSeparator", "");
					generator.writeEndObject();
					generator.writeRaw(",");
					generator.flush();
				} catch (IOException e) {
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
				}
			}
		};
		final HashMap<String, String> applicationLoaderPaths = new HashMap<String, String>();
		applicationLoaderPaths.put("BuddiLive", "buddilive");
		loginConfig.applicationLoaderPaths = applicationLoaderPaths;
		loginConfig.applicationControllers = new String[]{"BuddiLive.controller.preferences.PreferencesEditor"};	//This will load the stores for currencies and locales combos; we could load them manually, but this works and is easier...
		loginConfig.applicationViews = new String[]{"BuddiLive.view.component.SelfDocumentingField", "BuddiLive.view.component.CurrenciesCombobox", "BuddiLive.view.component.LocalesCombobox"};
		loginConfig.identifierLabelKey = "EMAIL_LABEL";
		loginConfig.showRegister = true;
		loginConfig.i18nBaseCustom = "i18n";
		loginConfig.passwordChecker = passwordChecker;
		publicRouter.attach("/login", new LoginRouter(this, loginConfig));
		publicRouter.attachDefault(DefaultResource.class).setMatchingMode(Template.MODE_STARTS_WITH);
		
		final Encoder encoder = new Encoder(getContext(), false, true, getEncoderService());
		encoder.setNext(optionalAuth);

		return encoder;
	}
	
	@Override
	public synchronized void stop() throws Exception {
		ds.close();
		
		super.stop();
	}
	
	public SqlSessionFactory getSqlSessionFactory() {
//		return sqlSessionFactory;
		final SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
		final Environment environment = new Environment("prod", new JdbcTransactionFactory(), ds);
		final org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration(environment);
		configuration.getTypeHandlerRegistry().register(BooleanHandler.class);
		configuration.getTypeHandlerRegistry().register(CurrencyHandler.class);
		configuration.getTypeHandlerRegistry().register(LocaleHandler.class);
		
		configuration.addMappers("ca.digitalcave.buddi.live.db");
		
		return sqlSessionFactoryBuilder.build(configuration);
	}
	
	public Configuration getFreemarkerConfiguration() {
		return freemarkerConfiguration;
	}
	
	public JsonFactory getJsonFactory() {
		return jsonFactory;
	}
	public Properties getSystemProperties(){
		return systemProperties;
	}
	public PasswordChecker getPasswordChecker() {
		return passwordChecker;
	}
}
