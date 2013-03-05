package ca.digitalcave.buddi.live.resource.gui;

import org.restlet.data.MediaType;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ca.digitalcave.buddi.live.BuddiApplication;
import ca.digitalcave.buddi.live.model.User;

public class JavascriptI18nResource extends ServerResource {
	@Override
	protected void doInit() throws ResourceException {
		getVariants().add(new Variant(MediaType.APPLICATION_JAVASCRIPT));
	}

	@Override
	protected Representation get(Variant variant) throws ResourceException {
		final BuddiApplication application = (BuddiApplication) getApplication();
		final User user = (User) getRequest().getClientInfo().getUser();
		
		return new TemplateRepresentation(getRequest().getOriginalRef().getPath(), application.getFreemarkerConfiguration(), user, MediaType.APPLICATION_JAVASCRIPT);
	}
}
