package ca.digitalcave.buddi.web.resource;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.ibatis.session.SqlSession;
import org.json.JSONObject;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.util.Base64;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ca.digitalcave.buddi.web.BuddiApplication;
import ca.digitalcave.buddi.web.db.Schema;
import ca.digitalcave.buddi.web.security.BuddiVerifier;
import ca.digitalcave.buddi.web.util.CryptoUtil;

public class IndexResource extends ServerResource {
	private static final int DAYS = 7;

	@Override
	protected void doInit() throws ResourceException {
		getVariants().add(new Variant(MediaType.TEXT_HTML));
	}
	
	@Override
	protected Representation post(Representation entity, Variant variant) throws ResourceException {
		try {
			final Form form = new Form(entity);
			
			boolean secure = "on".equals(form.getFirstValue("secure"));
						
			final JSONObject token = new JSONObject();
			token.put("identifier", form.getFirstValue("user"));
			token.put("password", form.getFirstValue("password"));

			if (secure){
				token.put("clientIp", getRequest().getClientInfo().getAddress());
				
				final Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DAY_OF_MONTH, DAYS);
				token.put("expiry", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(cal.getTime()));
			}
			
			final CookieSetting c = new CookieSetting(BuddiVerifier.COOKIE_NAME, Base64.encode(CryptoUtil.encrypt(token.toString().getBytes()), false));
			c.setAccessRestricted(true);
			if (secure) {
				c.setMaxAge(-1);					//Clear on browser close
			} else {
				c.setMaxAge(60 * 60 * 24 * 365);	//One year
			}
			
			getResponse().getCookieSettings().add(c);
			getResponse().redirectSeeOther(".");
			return new EmptyRepresentation();
		} catch (Exception e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}
	
	@Override
	protected Representation get(Variant variant) throws ResourceException {
		
		SqlSession sql = ((BuddiApplication) getApplication()).getSqlSessionFactory().openSession();
//		sql.getMapper(Schema.class).selectAccounts(1);
		sql.close();
		//Handle Logout
		if (getQuery().getFirst("logout") != null) {
			final CookieSetting c = new CookieSetting(BuddiVerifier.COOKIE_NAME, "");
			c.setAccessRestricted(true);
			c.setMaxAge(0);
			getResponse().getCookieSettings().add(c);
			
			getResponse().redirectSeeOther(".");
			return new EmptyRepresentation();
		}
		
		if (getClientInfo().getUser() == null){
			final BuddiApplication application = (BuddiApplication) getApplication();
			final TemplateRepresentation result = new TemplateRepresentation("login.html.ftl", application.getFreemarkerConfiguration(), null, MediaType.TEXT_HTML);
			return result;
		}
		else {
			final BuddiApplication application = (BuddiApplication) getApplication();
			final TemplateRepresentation result = new TemplateRepresentation("index.html.ftl", application.getFreemarkerConfiguration(), null, MediaType.TEXT_HTML);
			return result;
		}
	}
}
