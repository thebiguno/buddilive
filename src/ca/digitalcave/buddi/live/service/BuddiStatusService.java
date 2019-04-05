package ca.digitalcave.buddi.live.service;

import java.util.ArrayList;

import org.json.JSONObject;
import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.service.StatusService;

public class BuddiStatusService extends StatusService {

	@Override
	public Representation getRepresentation(Status status, Request request, Response response) {
		final ArrayList<Variant> variants = new ArrayList<Variant>();
		variants.add(new Variant(MediaType.TEXT_HTML));
		variants.add(new Variant(MediaType.APPLICATION_JSON));
		variants.add(new Variant(MediaType.APPLICATION_JAVASCRIPT));
		
		final Variant variant = Application.getCurrent().getConnegService().getPreferredVariant(variants, request, Application.getCurrent().getMetadataService());
		
		if (variant.getMediaType().isCompatible(MediaType.APPLICATION_JSON) || variant.getMediaType().isCompatible(MediaType.APPLICATION_JAVASCRIPT)) {
			response.setStatus(status);
			return getJsonRepresentation(false, status.getReasonPhrase(), status.getDescription());
		}
		else if (variant.getMediaType().isCompatible(MediaType.TEXT_HTML)){
			return new StringRepresentation(String.format("<html><head><title>%s</title><body><h1>%s</h1><p>%s</p></body></html>", status.getDescription(), status.getCode(), status.getDescription()), MediaType.TEXT_HTML);
		}
		else {
			response.setStatus(status);
			return new StringRepresentation(status.getCode() + ": " + status.getDescription());
		}
	}
	
	public static Representation getJsonRepresentation(boolean success, String title, String message) {
		try {
			final JSONObject object = new JSONObject();
			object.put("success", success);
			if (title != null) {
				object.put("title", title);
			}
			if (message != null) {
				object.put("msg", message);
			}
			return new JsonRepresentation(object);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
