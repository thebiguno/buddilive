package ca.digitalcave.buddi.live.resource.buddilive.preferences;

import java.util.Arrays;
import java.util.Comparator;
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

import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.FormatUtil;

public class CurrenciesResource extends ServerResource {

	@Override
	protected void doInit() throws ResourceException {
		getVariants().add(new Variant(MediaType.APPLICATION_JSON));
	}

	@Override
	protected Representation get(Variant variant) throws ResourceException {
		final User user = (User) getRequest().getClientInfo().getUser();
		final Currency[] commonCurrencies = new Currency[]{
				Currency.getInstance("CAD"),
				Currency.getInstance("USD"),
				Currency.getInstance("EUR"),
				Currency.getInstance("GBP"),
				Currency.getInstance("AUD"),
		};
		final Set<Currency> allCurrencies = new TreeSet<Currency>(new Comparator<Currency>() {
			@Override
			public int compare(Currency o1, Currency o2) {
				if (o1 == null || o2 == null) return 0;
				return o1.getDisplayName(user.getLocale()).compareTo(o2.getDisplayName(user.getLocale()));
			}
		});
		for(Locale locale : Locale.getAvailableLocales()) {
			try {
				allCurrencies.add(Currency.getInstance(locale));
			}
			catch(Exception e) {}
		}
		
		allCurrencies.removeAll(Arrays.asList(commonCurrencies));
		
		try {
			final JSONObject result = new JSONObject();
			result.put("success", true);

			for (Currency currency : commonCurrencies) {
				final JSONObject entry = new JSONObject();
				entry.put("text", currency.getDisplayName(user.getLocale()));
				entry.put("value", currency.getCurrencyCode());
				result.append("data", entry);
			}
			
			final JSONObject separator = new JSONObject();
			separator.put("text", "---");
			separator.put("value", "");
			separator.put("style", "color: " + FormatUtil.HTML_GRAY + ";");
			result.append("data", separator);
			
			for (Currency currency : allCurrencies) {
				final JSONObject entry = new JSONObject();
				entry.put("text", currency.getDisplayName(user.getLocale()));
				entry.put("value", currency.getCurrencyCode());
				result.append("data", entry);
			}
			return new JsonRepresentation(result);
		}
		catch (JSONException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}
}
