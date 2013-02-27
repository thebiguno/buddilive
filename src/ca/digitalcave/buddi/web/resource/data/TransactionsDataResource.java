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
import ca.digitalcave.buddi.web.model.Transaction;
import ca.digitalcave.buddi.web.model.User;

public class TransactionsDataResource extends ServerResource {

	@Override
	protected void doInit() throws ResourceException {
		getVariants().add(new Variant(MediaType.APPLICATION_JSON));
	}

	@Override
	protected Representation get(Variant variant) throws ResourceException {
		final BuddiApplication application = (BuddiApplication) getApplication();
		final User user = (User) getRequest().getClientInfo().getUser();
		try {
			final List<Transaction> transactions = application.getTransactionsDAO().selectTransactions(user);
			final JSONArray result = new JSONArray();
			for (Transaction transaction : transactions) {
				result.put(transaction.toJson());
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
			for (int i = 0; i < request.length(); i++) {
				final JSONObject transaction = request.getJSONObject(i);
				transaction.put("userId", user.getId());
				final Integer count = application.getTransactionsDAO().insertTransaction(user, new Transaction(transaction));
				if (count == 1){
					total += count;
				}
				else {
					throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
				}
			}

			final JSONObject result = new JSONObject();
			result.put("success", true);
			result.put("added", total);
			return new JsonRepresentation(result);
		}
		catch (IOException e){
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		}
		catch (JSONException e){
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		}
	}
}
