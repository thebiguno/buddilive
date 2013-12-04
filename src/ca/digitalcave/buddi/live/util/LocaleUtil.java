package ca.digitalcave.buddi.live.util;

import java.util.ResourceBundle;

import org.restlet.Request;

import ca.digitalcave.buddi.live.model.User;

public class LocaleUtil {
	private static final String key = "buddilive-translations";
	
	public static ResourceBundle getTranslation(Request r){
		if (r.getAttributes().get(key) == null){
			final User user = (User) r.getClientInfo().getUser();
			ResourceBundle translations;
			if (user != null && user.getLocale() != null){
				translations = ResourceBundle.getBundle("i18n", user.getLocale());
			}
			else {
				translations = ResourceBundle.getBundle("i18n", ca.digitalcave.moss.restlet.util.LocaleUtil.parseLocales(r.getClientInfo().getAcceptedLanguages()));
			}
			r.getAttributes().put(key, translations);
		}
		return (ResourceBundle) r.getAttributes().get(key);
	}
}
