package ca.digitalcave.buddi.live.resource.buddilive.preferences;

import java.util.ResourceBundle;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ca.digitalcave.buddi.live.util.LocaleUtil;

public class TranslationsResource extends ServerResource {

	@Override
	protected void doInit() throws ResourceException {
		getVariants().add(new Variant(MediaType.APPLICATION_JSON));
	}

	@Override
	protected Representation get(Variant variant) throws ResourceException {
		final ResourceBundle translation = LocaleUtil.getTranslation(getRequest());
		try {
			final JSONObject result = new JSONObject();
			for (String key : translation.keySet()) {
				result.put(key, translation.getString(key));
			}
			result.put("success", true);
			return new JsonRepresentation(result);
		}
		catch (JSONException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
	}
}
