package ca.digitalcave.buddi.live.resource.buddilive.preferences;

import java.util.Arrays;
import java.util.Currency;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ca.digitalcave.buddi.live.util.FormatUtil;

public class CurrenciesResource extends ServerResource {

	@Override
	protected void doInit() throws ResourceException {
		getVariants().add(new Variant(MediaType.APPLICATION_JSON));
	}

	@Override
	protected Representation get(Variant variant) throws ResourceException {
		final String[] commonCurrencies = new String[]{
				Currency.getInstance("CAD").getCurrencyCode(),
				Currency.getInstance("USD").getCurrencyCode(),
				Currency.getInstance("EUR").getCurrencyCode(),
				Currency.getInstance("GBP").getCurrencyCode(),
				Currency.getInstance("AUD").getCurrencyCode(),
		};
		final Set<String> allCurrencies = new TreeSet<String>();
		for(Locale locale : Locale.getAvailableLocales()) {
			try {
				allCurrencies.add(Currency.getInstance(locale).getCurrencyCode());
			}
			catch(Exception e) {}
		}
		
		allCurrencies.removeAll(Arrays.asList(commonCurrencies));
		
		try {
			final JSONObject result = new JSONObject();
			result.put("success", true);

			for (String currency : commonCurrencies) {
				final JSONObject entry = new JSONObject();
				entry.put("text", currency);	//TODO Change the display string to getDisplayName(locale) when I upgrade my server to JVM 7
				entry.put("value", currency);
				result.append("data", entry);
			}
			
			final JSONObject separator = new JSONObject();
			separator.put("text", "---");
			separator.put("value", "");
			separator.put("style", "color: " + FormatUtil.HTML_GRAY + ";");
			result.append("data", separator);
			
			for (String currency : allCurrencies) {
				final JSONObject entry = new JSONObject();
				entry.put("text", currency);
				entry.put("value", currency);
				result.append("data", entry);
			}
			return new JsonRepresentation(result);
		}
		catch (JSONException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}
}
