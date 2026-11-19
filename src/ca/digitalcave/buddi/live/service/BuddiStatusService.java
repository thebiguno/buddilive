package ca.digitalcave.buddi.live.service;

import java.util.ArrayList;

import org.json.JSONObject;
import org.json.JSONException;
import ca.digitalcave.buddi.live.db.util.DatabaseException;
import ca.digitalcave.buddi.live.util.InputValidationException;
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
	public Status toStatus(Throwable throwable, Request request, Response response) {
		for (Throwable cause = throwable; cause != null; cause = cause.getCause()) {
			if (cause instanceof InputValidationException || cause instanceof JSONException
					|| cause instanceof IllegalArgumentException) {
				return new Status(Status.CLIENT_ERROR_BAD_REQUEST, throwable);
			}
		}
		return super.toStatus(throwable, request, response);
	}

	public static String validationMessage(Status status) {
		if (status.getCode() != 400) {
			return status.getDescription();
		}
		for (Throwable cause = status.getThrowable(); cause != null; cause = cause.getCause()) {
			if (cause instanceof InputValidationException || cause instanceof DatabaseException) {
				return cause.getMessage();
			}
			if (cause instanceof JSONException) {
				final java.util.regex.Matcher field = java.util.regex.Pattern.compile("JSONObject\\[\"([^\"]+)\"\\]").matcher(cause.getMessage() == null ? "" : cause.getMessage());
				if (field.find()) {
					return "The field '" + field.group(1) + "' is missing or invalid. Please check its value.";
				}
				return "The submitted data is not valid JSON. Please check the required fields and try again.";
			}
			if (cause instanceof NumberFormatException) {
				return "A numeric field contains an invalid value. Please check amounts and numbers.";
			}
			if (cause instanceof IllegalArgumentException) {
				return "A selected option or date is invalid. Please check your selections and try again.";
			}
		}
		if (status.getCode() == 400 && Status.CLIENT_ERROR_BAD_REQUEST.getDescription().equals(status.getDescription())) {
			return "Please check that all required fields are filled in and all amounts, dates and selections are valid.";
		}
		return status.getDescription();
	}

	@Override
	public Representation toRepresentation(Status status, Request request, Response response) {
		final ArrayList<Variant> variants = new ArrayList<Variant>();
		variants.add(new Variant(MediaType.TEXT_HTML));
		variants.add(new Variant(MediaType.APPLICATION_JSON));
		variants.add(new Variant(MediaType.APPLICATION_JAVASCRIPT));
		
		final Variant variant = Application.getCurrent().getConnegService().getPreferredVariant(variants, request, Application.getCurrent().getMetadataService());
		
		if (variant.getMediaType().isCompatible(MediaType.APPLICATION_JSON) || variant.getMediaType().isCompatible(MediaType.APPLICATION_JAVASCRIPT)) {
			response.setStatus(status);
			return getJsonRepresentation(false, status.getReasonPhrase(), validationMessage(status));
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
