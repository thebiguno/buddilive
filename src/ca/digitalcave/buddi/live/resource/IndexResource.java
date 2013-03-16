package ca.digitalcave.buddi.live.resource;

import org.json.JSONObject;
import org.restlet.data.CookieSetting;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ca.digitalcave.buddi.live.BuddiApplication;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.security.BuddiVerifier;
import ca.digitalcave.buddi.live.util.CryptoUtil;

public class IndexResource extends ServerResource {
	@Override
	protected void doInit() throws ResourceException {
		getVariants().add(new Variant(MediaType.TEXT_HTML));
		getVariants().add(new Variant(MediaType.APPLICATION_JSON));
	}
	
	@Override
	protected Representation get(Variant variant) throws ResourceException {
		
		//Handle Logout
		if (getQuery().getFirst("logout") != null) {
			final CookieSetting c = new CookieSetting(BuddiVerifier.COOKIE_NAME, "");
			c.setAccessRestricted(true);
			c.setMaxAge(0);
			getResponse().getCookieSettings().add(c);
			
			getResponse().redirectSeeOther(".");
			return new EmptyRepresentation();
		}
		
		final BuddiApplication application = (BuddiApplication) getApplication();
		final User user = (User) getRequest().getClientInfo().getUser();
		final TemplateRepresentation result = new TemplateRepresentation(user.isAuthenticated() ? "index.html" : "login.html", application.getFreemarkerConfiguration(), user, MediaType.TEXT_HTML);
		return result;
	}
	
	@Override
	protected Representation post(Representation entity, Variant variant) throws ResourceException {
		try {
			final JSONObject token = new JSONObject(entity.getText());
			token.put("clientIp", getRequest().getClientInfo().getAddress());
			
			final CookieSetting c = new CookieSetting(BuddiVerifier.COOKIE_NAME, CryptoUtil.encrypt(token.toString(), BuddiVerifier.COOKIE_PASSWORD));
			c.setAccessRestricted(true);
			c.setMaxAge(-1);	//Browser close
			
			getResponse().getCookieSettings().add(c);
			getResponse().redirectSeeOther(".");
			return new EmptyRepresentation();
		} catch (Exception e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}
}
