package ca.digitalcave.buddi.live;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.mchange.v2.c3p0.ComboPooledDataSource;

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
import ca.digitalcave.buddi.live.security.BuddiLiveAuthenticationHelper;
import ca.digitalcave.buddi.live.security.BuddiVerifier;
import ca.digitalcave.buddi.live.service.BuddiStatusService;
import ca.digitalcave.moss.common.OperatingSystemUtil;
import ca.digitalcave.moss.crypto.Crypto;
import ca.digitalcave.moss.crypto.Crypto.Algorithm;
import ca.digitalcave.moss.crypto.ObfuscateUtil;
import ca.digitalcave.moss.restlet.CookieAuthenticator;
import ca.digitalcave.moss.restlet.plugin.AuthenticationHelper;
import ca.digitalcave.moss.restlet.plugin.ExtraFieldsDirective;
import ca.digitalcave.moss.restlet.router.AuthenticationRouter;
import ca.digitalcave.moss.restlet.util.PasswordChecker;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;


public class BuddiApplication extends Application{
	private Properties configProperties;
	private Configuration freemarkerConfiguration;
	private SqlSessionFactory sqlSessionFactory;
	private final JsonFactory jsonFactory = new JsonFactory();
	private ComboPooledDataSource ds;
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
		freemarker.template.Configuration freemarkerConfiguration = new freemarker.template.Configuration(new Version(2, 3, 31));
		final Object servletContext = getContext().getAttributes().get("org.restlet.ext.servlet.ServletContext");
		freemarkerConfiguration.setServletContextForTemplateLoading(servletContext, "/");
		freemarkerConfiguration.setDefaultEncoding("UTF-8");
		freemarkerConfiguration.setLocalizedLookup(false);
		freemarkerConfiguration.setLocale(Locale.ENGLISH);
		freemarkerConfiguration.setTemplateUpdateDelayMilliseconds(0);
		freemarkerConfiguration.setObjectWrapper(new BeansWrapper(new Version(2, 3, 31)));
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
		final BuddiApplication application = this;
		
		getMetadataService().setDefaultLanguage(Language.ENGLISH);
		getMetadataService().setEnabled(true);
		getTunnelService().setEnabled(true);
		getTunnelService().setExtensionsTunnel(true);

		final AuthenticationHelper authenticationHelper = new BuddiLiveAuthenticationHelper(this);
		
		authenticationHelper.getConfig().showCookieWarning = true;
		authenticationHelper.getConfig().showForgotUsername = false;
		authenticationHelper.getConfig().showForgotPassword = true;
		authenticationHelper.getConfig().showRegister = true;
		authenticationHelper.getConfig().showImpersonate = false;
		authenticationHelper.getConfig().showDisableIpLock = true;
		authenticationHelper.getConfig().i18nBaseCustom = "i18n";
		authenticationHelper.getConfig().totpIssuer = "Buddi Live";

		authenticationHelper.getConfig().applicationLoaderPaths = new HashMap<String, String>();
		authenticationHelper.getConfig().applicationLoaderPaths.put("BuddiLive", "buddilive");
		
		authenticationHelper.getConfig().applicationViews = new String[]{"BuddiLive.view.component.CurrenciesCombobox","BuddiLive.view.component.LocalesCombobox"};
		authenticationHelper.getConfig().applicationControllers = new String[]{"BuddiLive.controller.preferences.PreferencesEditor"};

		authenticationHelper.getConfig().extraRegisterStep1Fields = new ExtraFieldsDirective(){
			@Override
			public void writeFields(Writer out, ResourceBundle translation) {
				try {
					final JsonGenerator generator = application.getJsonFactory().createGenerator(out);
					generator.writeStartObject();
					generator.writeStringField("xtype", "selfdocumentingfield");
					generator.writeStringField("messageBody", translation.getString("HELP_LOCALE"));
					generator.writeStringField("type", "localescombobox");
					generator.writeStringField("name", "locale");
					generator.writeStringField("fieldLabel", translation.getString("LOCALE"));
					generator.writeStringField("value", "en_US");
					generator.writeEndObject();
					generator.writeRaw(",");
					generator.writeStartObject();
					generator.writeStringField("xtype", "selfdocumentingfield");
					generator.writeStringField("messageBody", translation.getString("HELP_CURRENCY"));
					generator.writeStringField("type", "currenciescombobox");
					generator.writeStringField("name", "currency");
					generator.writeStringField("fieldLabel", translation.getString("CURRENCY"));
					generator.writeStringField("value", "USD");
					generator.writeEndObject();
					generator.writeRaw(",");
					generator.writeStartObject();
					generator.writeStringField("xtype", "selfdocumentingfield");
					generator.writeStringField("messageBody", translation.getString("CREATE_USER_AGREEMENT_REQUIRED"));
					generator.writeStringField("type", "checkbox");
					generator.writeStringField("boxLabel", translation.getString("AGREE_TERMS_AND_CONDITIONS"));
					generator.writeStringField("name", "agree");
					generator.writeStringField("fieldLabel", " ");
					generator.writeStringField("labelSeparator", "");
					generator.writeEndObject();
					generator.writeRaw(",");
					generator.writeStartObject();
					generator.writeStringField("xtype", "label");
					generator.writeStringField("html", translation.getString("HELP_REGISTER"));
					generator.writeEndObject();
					generator.writeRaw(",");
					generator.flush();
				} catch (IOException e) {
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
				}
			}
		};
		authenticationHelper.getConfig().extraRegisterStep2Fields = new ExtraFieldsDirective(){
			@Override
			public void writeFields(Writer out, ResourceBundle translation) {
				try {
					final JsonGenerator generator = application.getJsonFactory().createGenerator(out);
					generator.writeStartObject();
					generator.writeStringField("xtype", "label");
					generator.writeStringField("html", translation.getString("HELP_REGISTER_2"));
					generator.writeEndObject();
					generator.writeRaw(",");
					generator.flush();
				} catch (IOException e) {
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
				}
			}
		};
		authenticationHelper.getConfig().extraforgotPasswordStep1PanelFields = new ExtraFieldsDirective(){
			@Override
			public void writeFields(Writer out, ResourceBundle translation) {
				try {
					final JsonGenerator generator = application.getJsonFactory().createGenerator(out);
					generator.writeStartObject();
					generator.writeStringField("xtype", "label");
					generator.writeStringField("html", translation.getString("HELP_RESET_PASSWORD"));
					generator.writeEndObject();
					generator.writeRaw(",");
					generator.flush();
				} catch (IOException e) {
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
				}
			}
		};
		authenticationHelper.getConfig().extraforgotPasswordStep2PanelFields = new ExtraFieldsDirective(){
			@Override
			public void writeFields(Writer out, ResourceBundle translation) {
				try {
					final JsonGenerator generator = application.getJsonFactory().createGenerator(out);
					generator.writeStartObject();
					generator.writeStringField("xtype", "label");
					generator.writeStringField("html", translation.getString("HELP_RESET_PASSWORD_2"));
					generator.writeEndObject();
					generator.writeRaw(",");
					generator.flush();
				} catch (IOException e) {
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
				}
			}
		};
		
		final Router privateRouter = new Router(getContext());
		
		final BuddiVerifier verifier = new BuddiVerifier(authenticationHelper);
		
		final CookieAuthenticator privateAuth = new CookieAuthenticator(getContext(), false, authenticationHelper, verifier);
		final CookieAuthenticator optionalAuth = new CookieAuthenticator(getContext(), true, authenticationHelper, verifier);;
		
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
		optionalAuth.setVerifier(verifier);
		optionalAuth.setNext(publicRouter);
		
		//Public data and binary data which should not be filtered through freemarker
		publicRouter.attach("", new Redirector(getContext(), "index.html", Redirector.MODE_CLIENT_TEMPORARY));
		publicRouter.attach("/", new Redirector(getContext(), "index.html", Redirector.MODE_CLIENT_TEMPORARY));
		publicRouter.attach("/authentication", new AuthenticationRouter(this, authenticationHelper, verifier));
		publicRouter.attach("/index", IndexResource.class);
		publicRouter.attach("/stores", comboStoreRouter);
		publicRouter.attach("/data", privateAuth);
		
		publicRouter.attach("/donation-completed", DonationResource.class);
		
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
	
	public HtmlEmail getEmail(String from, String replyTo, String to) throws EmailException, AddressException {
		final Properties c = getConfigProperties();
		final HtmlEmail htmlEmail = new HtmlEmail();
		if (StringUtils.isBlank(c.getProperty("mail.smtp.host"))){
			throw new EmailException("Parameter mail.smtp.host cannot be blank.");
		}
		htmlEmail.setHostName(c.getProperty("mail.smtp.host"));
		htmlEmail.setSmtpPort(Integer.parseInt(c.getProperty("mail.smtp.port", "25")));
		
		final InternetAddress[] fromAddresses = InternetAddress.parse(from, false);
		if (fromAddresses != null && fromAddresses.length > 0) {
			htmlEmail.setFrom(fromAddresses[0].getAddress(), fromAddresses[0].getPersonal());
		}
		
		final InternetAddress[] toAddresses = InternetAddress.parse(to, false);
		for (InternetAddress toAddress : toAddresses) {
			htmlEmail.addTo(toAddress.getAddress(), toAddress.getPersonal());
		}
		
		if (StringUtils.isNotBlank(replyTo)){
			final InternetAddress[] replyToAddresses = InternetAddress.parse(replyTo);
			for (InternetAddress replyToAddress : replyToAddresses) {
				try {
					htmlEmail.addReplyTo(replyToAddress.getAddress(), replyToAddress.getPersonal());
				}
				catch (Throwable e){
					getLogger().log(Level.WARNING, "Invalid replyToAddress " + replyToAddress, e);
				}
			}
		}
		
		final String emailUser = c.getProperty("mail.smtp.username");
		final String emailPassword = ObfuscateUtil.deobfuscate(c.getProperty("mail.smtp.password", ""));
		if (StringUtils.isNotBlank(emailUser)){
			htmlEmail.setAuthentication(emailUser, emailPassword);
		}		
		final boolean startTls = Boolean.parseBoolean(c.getProperty("mail.smtp.starttls.enable", "false"));
		htmlEmail.setStartTLSEnabled(startTls);
		htmlEmail.setStartTLSRequired(startTls);
		
		//final String ssl = getParameterProvider().get("config.smtp.ssl");
		//htmlEmail.setSSLOnConnect("ENABLED".equalsIgnoreCase(ssl));

		if (Boolean.parseBoolean(c.getProperty("mail.smtp.debug", "false"))){
			htmlEmail.setDebug(true);
		}
		
		return htmlEmail;
	}
}
