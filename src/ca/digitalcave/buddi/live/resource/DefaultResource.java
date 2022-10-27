package ca.digitalcave.buddi.live.resource;

import java.util.Date;
import java.util.HashMap;

import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ca.digitalcave.buddi.live.BuddiApplication;
import ca.digitalcave.buddi.live.util.LocaleUtil;
import ca.digitalcave.moss.restlet.model.AuthUser;
import ca.digitalcave.moss.restlet.util.LocalizationUtil;
import ca.digitalcave.moss.restlet.util.RequestUtil;
import freemarker.template.Configuration;

public class DefaultResource extends ServerResource {
	protected void doInit() throws ResourceException {
		LocalizationUtil.addVariants(getVariants(), MediaType.TEXT_HTML);
		LocalizationUtil.addVariants(getVariants(), MediaType.TEXT_CSS);
		LocalizationUtil.addVariants(getVariants(), MediaType.APPLICATION_JAVASCRIPT);
		LocalizationUtil.addVariants(getVariants(), MediaType.IMAGE_ALL);
	}
	
	public Representation get(Variant variant) throws ResourceException {
		final String path = RequestUtil.getPath(getRequest());
		
		if (path.contains("WEB-INF")){
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		}
		
		final boolean forceDoNotModify = path.startsWith("media/lib/extjs");
		
		if (!forceDoNotModify && (variant.getMediaType().equals(MediaType.TEXT_HTML) || variant.getMediaType().equals(MediaType.APPLICATION_JAVASCRIPT))) {
			final HashMap<String, Object> dataModel = new HashMap<String, Object>();
			final AuthUser user = (AuthUser) getClientInfo().getUser();
			dataModel.put("user", user);
			dataModel.put("requestAttributes", getRequestAttributes());
			dataModel.put("translation", LocaleUtil.getTranslation(getRequest()));

			final Configuration freeMarkerConfig = ((BuddiApplication) getApplication()).getFreemarkerConfiguration();
			final TemplateRepresentation entity = new TemplateRepresentation(path, freeMarkerConfig, dataModel, variant.getMediaType());
			if (entity.getTemplate() == null) throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
			entity.setModificationDate(new Date());
			return entity;
		}
		else {
			final Request request = new Request(Method.GET, new Reference("war://" + (path.startsWith("/") ? "" : "/") + path));
			request.getConditions().setUnmodifiedSince(getRequest().getConditions().getUnmodifiedSince());
			getContext().getClientDispatcher().handle(request, getResponse());
			return getResponseEntity();
		}
	}
}
