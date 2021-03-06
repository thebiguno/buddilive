package ca.digitalcave.buddi.live;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.security.Key;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

import javax.crypto.SecretKey;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
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

import com.mchange.v2.c3p0.ComboPooledDataSource;

import ca.digitalcave.buddi.live.db.BuddiSystem;
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
import ca.digitalcave.buddi.live.resource.buddilive.DonationResource;
import ca.digitalcave.buddi.live.resource.buddilive.ParentsResource;
import ca.digitalcave.buddi.live.resource.buddilive.PeriodsResource;
import ca.digitalcave.buddi.live.resource.buddilive.ScheduledTransactionsResource;
import ca.digitalcave.buddi.live.resource.buddilive.ScheduledTransactionsRunnerResource;
import ca.digitalcave.buddi.live.resource.buddilive.SourcesResource;
import ca.digitalcave.buddi.live.resource.buddilive.TransactionsResource;
import ca.digitalcave.buddi.live.resource.buddilive.UserPreferencesResource;
import ca.digitalcave.buddi.live.resource.buddilive.preferences.CurrenciesResource;
import ca.digitalcave.buddi.live.resource.buddilive.preferences.LocalesResource;
import ca.digitalcave.buddi.live.resource.buddilive.report.AverageIncomeAndExpensesByCategoryResource;
import ca.digitalcave.buddi.live.resource.buddilive.report.BalancesOverTimeResource;
import ca.digitalcave.buddi.live.resource.buddilive.report.IncomeAndExpensesByCategoryResource;
import ca.digitalcave.buddi.live.resource.buddilive.report.InflowAndOutflowByAccountResource;
import ca.digitalcave.buddi.live.resource.buddilive.report.InflowAndOutflowByPayeeResource;
import ca.digitalcave.buddi.live.resource.buddilive.report.PieTotalsByCategoryResource;
import ca.digitalcave.buddi.live.resource.data.BackupResource;
import ca.digitalcave.buddi.live.resource.data.ExportResource;
import ca.digitalcave.buddi.live.resource.data.RestoreResource;
import ca.digitalcave.buddi.live.security.BuddiVerifier;
import ca.digitalcave.buddi.live.service.BuddiStatusService;
import ca.digitalcave.moss.common.OperatingSystemUtil;
import ca.digitalcave.moss.crypto.Crypto;
import ca.digitalcave.moss.crypto.Crypto.Algorithm;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;
import ca.digitalcave.moss.restlet.CookieAuthenticator;
import ca.digitalcave.moss.restlet.login.ExtraFieldsDirective;
import ca.digitalcave.moss.restlet.login.LoginRouter;
import ca.digitalcave.moss.restlet.login.LoginRouterConfiguration;
import ca.digitalcave.moss.restlet.util.PasswordChecker;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;


public class BuddiApplication extends Application{
	private Properties configProperties;
	private Configuration freemarkerConfiguration;
	private SqlSessionFactory sqlSessionFactory;
	private final JsonFactory jsonFactory = new JsonFactory();
	private ComboPooledDataSource ds;
	private BuddiVerifier verifier = new BuddiVerifier();
	private Properties systemProperties = new Properties();
	private PasswordChecker passwordChecker = new PasswordChecker().setHistoryEnforced(false);
	private Crypto crypto = new Crypto().setAlgorithm(Algorithm.AES_256).setSaltLength(32).setKeyIterations(1);

	public synchronized void start() throws Exception {
		try { systemProperties.load(new ClientResource(getContext(), "war:///WEB-INF/classes/version.properties").get().getStream()); } catch (Throwable e){}
		
		configProperties = new Properties();
		try {
			configProperties.load(new ClientResource(getContext(), "war:///WEB-INF/classes/config.properties").get().getStream());
			configProperties.setProperty("mail.subject", "BuddiLive Account Activation");
		}
		catch (Exception e){
			getLogger().severe("There was an error loading the config file from WEB-INF/classes/config.properties.  Please ensure that this file exists and is readable.");
			throw e;
		}
		
		//If we are using Derby, and the user does not have a connection URL, set one up in a sane location.
		if ("org.apache.derby.jdbc.EmbeddedDriver".equals(configProperties.getProperty("db.driver")) && configProperties.getProperty("db.url") == null){
			final File buddiLiveFolder = OperatingSystemUtil.getUserFolder("BuddiLive");
			
			//Try to create directory if needed
			if (!buddiLiveFolder.exists()) buddiLiveFolder.mkdirs();
			
			//Check if the directory exists
			if (!buddiLiveFolder.exists()){
				throw new RuntimeException("Unable to access folder '" + buddiLiveFolder.getAbsolutePath() + "'; please ensure this folder exists and is writable.");
			}
			else if (buddiLiveFolder.isFile()){
				throw new RuntimeException("There is already a file at path '" + buddiLiveFolder.getAbsolutePath() + "'; cannot create directory.");
			}

			//Set up the URL; if the folder does not already exist, use the 'create' option on the URL.
			final File database = new File(buddiLiveFolder, "derby");
			configProperties.setProperty("db.url", "jdbc:derby:directory:" + database.getAbsolutePath() + (database.exists() ? "" : ";create=true"));
		}
		
		ds = new ComboPooledDataSource();
		ds.setDriverClass(configProperties.getProperty("db.driver"));
		ds.setJdbcUrl(configProperties.getProperty("db.url"));
		ds.setUser(configProperties.getProperty("db.user"));
		ds.setPassword(configProperties.getProperty("db.password"));
		ds.setPreferredTestQuery(configProperties.getProperty("db.query"));
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
		final Callable<Key> key = new Callable<Key>() {
			@Override
			public Key call() throws Exception {
				SecretKey key;
				final SqlSession sql = sqlSessionFactory.openSession();
				try {
					String keyEncoded = sql.getMapper(BuddiSystem.class).selectCookieEncryptionKey();
					if (keyEncoded == null){
						key = new Crypto().setAlgorithm(Algorithm.AES_256).generateSecretKey();
						keyEncoded = Crypto.encodeSecretKey(key);
						sql.getMapper(BuddiSystem.class).deleteCookieEncryptionKey();
						sql.getMapper(BuddiSystem.class).insertCookieEncryptionKey(keyEncoded);
						sql.commit();
					}
					key = Crypto.recoverSecretKey(keyEncoded);
				} catch (CryptoException e) {
					key = new Crypto().setAlgorithm(Algorithm.AES_256).generateSecretKey();
					String keyEncoded = Crypto.encodeSecretKey(key);
					sql.getMapper(BuddiSystem.class).updateCookieEncryptionKey(keyEncoded);
					sql.commit();
				} finally {
					sql.close();
				}
				return key;
			}
		};
		
		final CookieAuthenticator privateAuth = new CookieAuthenticator(getContext(), false, key);
		privateAuth.setMaxCookieAge(60 * 15);	//Invalidate credentials and auto logout after this period (value in seconds)
		privateAuth.setAllowRemember(false);
		privateAuth.setVerifier(verifier);
		privateAuth.setNext(privateRouter);
		
		//Handles the desktop GUI stuff
		privateRouter.attach("/accounts", AccountsResource.class);
		privateRouter.attach("/categories", CategoriesResource.class);
		privateRouter.attach("/categories/periods", PeriodsResource.class);
		privateRouter.attach("/categories/parents", ParentsResource.class);
		privateRouter.attach("/changepassword", ChangePasswordResource.class);
		privateRouter.attach("/transactions", TransactionsResource.class);
		privateRouter.attach("/transactions/descriptions", DescriptionsResource.class);
		privateRouter.attach("/scheduledtransactions", ScheduledTransactionsResource.class);
		privateRouter.attach("/scheduledtransactions/execute", ScheduledTransactionsRunnerResource.class);
		privateRouter.attach("/sources/from", SourcesResource.class);
		privateRouter.attach("/sources/to", SourcesResource.class);
		privateRouter.attach("/userpreferences", UserPreferencesResource.class);
		
		privateRouter.attach("/report/pietotalsbycategory", PieTotalsByCategoryResource.class);
		privateRouter.attach("/report/incomeandexpensesbycategory", IncomeAndExpensesByCategoryResource.class);
		privateRouter.attach("/report/averageincomeandexpensesbycategory", AverageIncomeAndExpensesByCategoryResource.class);
		privateRouter.attach("/report/inflowandoutflowbyaccount", InflowAndOutflowByAccountResource.class);
		privateRouter.attach("/report/inflowandoutflowbypayee", InflowAndOutflowByPayeeResource.class);
		privateRouter.attach("/report/balancesovertime", BalancesOverTimeResource.class);
		
		privateRouter.attach("/backup", BackupResource.class);
		privateRouter.attach("/export", ExportResource.class);
		privateRouter.attach("/restore", RestoreResource.class);
		
		final Router comboStoreRouter = new Router(getContext());
		comboStoreRouter.attach("/currencies", CurrenciesResource.class);
		comboStoreRouter.attach("/locales", LocalesResource.class);

		
		final Router publicRouter = new Router(getContext());
		final CookieAuthenticator optionalAuth = new CookieAuthenticator(getContext(), true, key);
		optionalAuth.setVerifier(verifier);
		optionalAuth.setNext(publicRouter);
		
		//Public data and binary data which should not be filtered through freemarker
		publicRouter.attach("", new Redirector(getContext(), "index.html", Redirector.MODE_CLIENT_TEMPORARY));
		publicRouter.attach("/", new Redirector(getContext(), "index.html", Redirector.MODE_CLIENT_TEMPORARY));
		publicRouter.attach("/index", IndexResource.class);
		publicRouter.attach("/stores", comboStoreRouter);
		publicRouter.attach("/data", privateAuth);
		
		publicRouter.attach("/donation-completed", DonationResource.class);
		
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
					generator.writeStartObject();
					generator.writeStringField("xtype", "label");
					generator.writeStringField("html", customTranslation.getString("HELP_REGISTER"));
					generator.writeEndObject();
					generator.writeRaw(",");
					generator.flush();
				} catch (IOException e) {
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
				}
			}
		};
		loginConfig.extraRegisterStep2Fields = new ExtraFieldsDirective(){
			@Override
			public void writeFields(Writer out, ResourceBundle translation, ResourceBundle customTranslation) {
				try {
					final JsonGenerator generator = application.getJsonFactory().createJsonGenerator(out);
					generator.writeStartObject();
					generator.writeStringField("xtype", "label");
					generator.writeStringField("html", customTranslation.getString("HELP_REGISTER_2"));
					generator.writeEndObject();
					generator.writeRaw(",");
					generator.flush();
				} catch (IOException e) {
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
				}
			}
		};
		loginConfig.extraResetStep1PanelFields = new ExtraFieldsDirective(){
			@Override
			public void writeFields(Writer out, ResourceBundle translation, ResourceBundle customTranslation) {
				try {
					final JsonGenerator generator = application.getJsonFactory().createJsonGenerator(out);
					generator.writeStartObject();
					generator.writeStringField("xtype", "label");
					generator.writeStringField("html", customTranslation.getString("HELP_RESET_PASSWORD"));
					generator.writeEndObject();
					generator.writeRaw(",");
					generator.flush();
				} catch (IOException e) {
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
				}
			}
		};
		loginConfig.extraResetStep2PanelFields = new ExtraFieldsDirective(){
			@Override
			public void writeFields(Writer out, ResourceBundle translation, ResourceBundle customTranslation) {
				try {
					final JsonGenerator generator = application.getJsonFactory().createJsonGenerator(out);
					generator.writeStartObject();
					generator.writeStringField("xtype", "label");
					generator.writeStringField("html", customTranslation.getString("HELP_RESET_PASSWORD_2"));
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
		loginConfig.i18nBaseCustom = "i18n";
		loginConfig.passwordChecker = passwordChecker;
		loginConfig.formTitle = "";
		loginConfig.showRegister = !Boolean.parseBoolean(configProperties.getProperty("server.private", "true"));
		loginConfig.showRemember = false;
		loginConfig.tabBarBackgroundInvisible = true;
		loginConfig.tabPackAlignment = "end";
		loginConfig.tabPosition = "top";
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
	
	public Properties getConfigProperties() {
		return configProperties;
	}
	
	public SqlSessionFactory getSqlSessionFactory() {
		return sqlSessionFactory;
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
	public Crypto getCrypto() {
		return crypto;
	}
}
