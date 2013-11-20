package ca.digitalcave.buddi.live.util;

import java.util.Locale;
import java.util.ResourceBundle;

import org.restlet.Request;

import ca.digitalcave.buddi.live.model.User;

public class LocaleUtil {
	private static final String key = "buddilive-translations";
	
	public static ResourceBundle getTranslation(Request r){
		if (r.getAttributes().get(key) == null){
			final User user = (User) r.getClientInfo().getUser();
			final ResourceBundle translations = ResourceBundle.getBundle("i18n", user == null || user.getLocale() == null ? Locale.ENGLISH : user.getLocale());	//TODO Pick browser locale
			r.getAttributes().put(key, translations);
		}
		return (ResourceBundle) r.getAttributes().get(key);
	}
}
