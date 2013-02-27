package ca.digitalcave.buddi.web.resource.data;

import java.io.IOException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ca.digitalcave.buddi.web.BuddiApplication;
import ca.digitalcave.buddi.web.db.bd.DataConstraintException;
import ca.digitalcave.buddi.web.model.Source;
import ca.digitalcave.buddi.web.model.User;

public class SourcesDataResource extends ServerResource {

	@Override
	protected void doInit() throws ResourceException {
		getVariants().add(new Variant(MediaType.APPLICATION_JSON));
	}

	@Override
	protected Representation get(Variant variant) throws ResourceException {
		final BuddiApplication application = (BuddiApplication) getApplication();
		final User user = (User) getRequest().getClientInfo().getUser();
		try {
			final List<Source> sources = application.getSourcesBD().selectSources(user);
			final JSONArray result = new JSONArray();
			for (Source source : sources) {
				result.put(source.toJson());
			}
			return new JsonRepresentation(result);
		}
		catch (JSONException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}

	@Override
	protected Representation post(Representation entity, Variant variant) throws ResourceException {
		final BuddiApplication application = (BuddiApplication) getApplication();
		final User user = (User) getRequest().getClientInfo().getUser();
		try {
			final JSONArray request = new JSONArray(entity.getText());
			int total = 0;
			int error = 0;
			for (int i = 0; i < request.length(); i++) {
				final JSONObject source = request.getJSONObject(i);
				source.put("userId", user.getId());
				final Integer count = application.getSourcesBD().insertSource(user, new Source(source));
				if (count == 1){
					total ++;
				}
				else {
					error ++;
				}
			}
			
			final JSONObject result = new JSONObject();
			result.put("success", true);
			result.put("added", total);
			result.put("error", error);
			return new JsonRepresentation(result);
		}
		catch (DataConstraintException e){
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		}
		catch (IOException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		catch (JSONException e){
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		}
	}
}
