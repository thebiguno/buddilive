package ca.digitalcave.buddi.live.resource;

import java.util.HashMap;

import ca.digitalcave.buddi.live.BuddiApplication;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.LocaleUtil;
import ca.digitalcave.moss.restlet.AbstractFreemarkerResource;
import freemarker.template.Configuration;

public class DefaultResource extends AbstractFreemarkerResource {
	@Override
	protected Configuration getFreemarkerConfig() {
		return ((BuddiApplication) getApplication()).getFreemarkerConfiguration();
	}
	
	@Override
	protected Object getDataModel() {
		final HashMap<String, Object> dataModel = new HashMap<String, Object>();
		final User user = (User) getClientInfo().getUser();
		dataModel.put("user", user);
		dataModel.put("requestAttributes", getRequestAttributes());
		dataModel.put("translation", LocaleUtil.getTranslation(getRequest()));
		return dataModel;
	}
}
