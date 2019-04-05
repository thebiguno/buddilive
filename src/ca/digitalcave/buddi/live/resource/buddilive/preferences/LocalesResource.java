package ca.digitalcave.buddi.live.resource.buddilive.preferences;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.FormatUtil;

public class LocalesResource extends ServerResource {

	@Override
	protected void doInit() throws ResourceException {
		getVariants().add(new Variant(MediaType.APPLICATION_JSON));
	}

	@Override
	protected Representation get(Variant variant) throws ResourceException {
		final User user = (User) getRequest().getClientInfo().getUser();
		final Locale[] commonLocales = new Locale[]{
				Locale.CANADA,
				Locale.US,
				Locale.UK,
				new Locale("es", "ES"),
				Locale.GERMANY,
				Locale.ITALY,
		};
		final Set<Locale> allLocales = new TreeSet<Locale>(new Comparator<Locale>() {
			@Override
			public int compare(Locale o1, Locale o2) {
				if (o1 == null || o2 == null) return 0;
				return o1.getDisplayName(user.getLocale()).compareTo(o2.getDisplayName(user.getLocale()));
			}
		});
		for(Locale locale : Locale.getAvailableLocales()) {
			try {
				if (StringUtils.isNotBlank(locale.getCountry())) allLocales.add(locale);
			}
			catch(Exception e) {}
		}
		
		allLocales.removeAll(Arrays.asList(commonLocales));
		
		try {
			final JSONObject result = new JSONObject();
			result.put("success", true);

			for (Locale locale : commonLocales) {
				final JSONObject entry = new JSONObject();
				entry.put("text", locale.getDisplayName(user != null && user.getLocale() != null ? user.getLocale() : Locale.ENGLISH));
				entry.put("value", locale.toString());
				result.append("data", entry);
			}
			
			final JSONObject separator = new JSONObject();
			separator.put("text", "---");
			separator.put("value", "");
			separator.put("style", "color: " + FormatUtil.HTML_GRAY + ";");
			result.append("data", separator);
			
			for (Locale locale : allLocales) {
				final JSONObject entry = new JSONObject();
				entry.put("text", locale.getDisplayName(user != null && user.getLocale() != null ? user.getLocale() : Locale.ENGLISH));
				entry.put("value", locale.toString());
				result.append("data", entry);
			}
			return new JsonRepresentation(result);
		}
		catch (JSONException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}
}
