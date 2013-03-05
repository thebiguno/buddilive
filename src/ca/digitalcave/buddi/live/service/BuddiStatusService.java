package ca.digitalcave.buddi.live.service;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;

import org.json.JSONObject;
import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.service.StatusService;

import ca.digitalcave.buddi.live.db.util.DatabaseException;

public class BuddiStatusService extends StatusService {

	@Override
	public Status getStatus(Throwable e, Request request, Response response) {
		if (e instanceof DatabaseException){
			return new Status(Status.CLIENT_ERROR_BAD_REQUEST, e.getLocalizedMessage());
		}
		else if (e.getCause() != null && e.getCause() instanceof SQLIntegrityConstraintViolationException){
			return new Status(Status.CLIENT_ERROR_BAD_REQUEST, "A data constraint violation has occurred.  Please change the request and try again.");
		}
		return super.getStatus(e, request, response);
	}
	
	@Override
	public Representation getRepresentation(Status status, Request request, Response response) {
		final ArrayList<Variant> variants = new ArrayList<Variant>();
		variants.add(new Variant(MediaType.TEXT_HTML));
		variants.add(new Variant(MediaType.APPLICATION_JSON));
		variants.add(new Variant(MediaType.APPLICATION_JAVASCRIPT));
		
		final Variant variant = request.getClientInfo().getPreferredVariant(variants, Application.getCurrent().getMetadataService());
		
		if (variant.getMediaType().isCompatible(MediaType.APPLICATION_JSON) ||
				variant.getMediaType().isCompatible(MediaType.APPLICATION_JAVASCRIPT)) {
			response.setStatus(status);
			final Representation result = getJsonRepresentation(false, status.getName(), status.getDescription());
			return result;
		} else {
			response.setStatus(status);
			return super.getRepresentation(status, request, response);
		}
	}
	
	public static Representation getJsonRepresentation(boolean success, String title, String message) {
		try {
			final JSONObject object = new JSONObject();
			object.put("success", success);
			if (message != null) {
				object.put("msg", message);
			}
			return new JsonRepresentation(object);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
