package ca.digitalcave.buddi.live.resource.buddilive.preferences;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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

public class TimezonesResource extends ServerResource {

	@Override
	protected void doInit() throws ResourceException {
		getVariants().add(new Variant(MediaType.APPLICATION_JSON));
	}

	final static Pattern p = Pattern.compile("^America/.*|^Pacific/.*|^Atlantic/.*|^Africa/.*|^Europe/.*|^Indian/.*|^Asia/.*", Pattern.MULTILINE);
	final static DateTimeFormatter offsetFormatter = DateTimeFormat.forPattern("ZZ");
	
	@Override
	protected Representation get(Variant variant) throws ResourceException {
		final User user = (User) getRequest().getClientInfo().getUser();
		final long instant = System.currentTimeMillis();
		final Set<DateTimeZone> allDateTimeZones = new TreeSet<DateTimeZone>(new Comparator<DateTimeZone>() {
			@Override
			public int compare(DateTimeZone o1, DateTimeZone o2) {
				if (o1 == null || o2 == null) return 0;
				int offset = new Integer(o1.getOffset(instant)).compareTo(o2.getOffset(instant));
				if (offset != 0) return offset;
				return o1.getName(instant, user.getLocale()).compareTo(o2.getName(instant, user.getLocale()));
			}
		});
		for(String id : DateTimeZone.getAvailableIDs()) {
			try {
				DateTimeZone timezone = DateTimeZone.forID(id);
				if (p.matcher(timezone.getID()).matches()) allDateTimeZones.add(timezone);
			}
			catch(Exception e) {}
		}
		
		try {
			final JSONObject result = new JSONObject();
			result.put("success", true);

			for (DateTimeZone timezone : allDateTimeZones) {
				final JSONObject entry = new JSONObject();
				entry.put("text", "(" + offsetFormatter.withZone(timezone).print(instant) + ") " + timezone.getName(instant, user.getLocale()));
				entry.put("value", timezone.getID());
				result.append("data", entry);
			}
			return new JsonRepresentation(result);
		}
		catch (JSONException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}
}
