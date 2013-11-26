package ca.digitalcave.buddi.live.resource.buddilive;

import java.util.Date;

import org.apache.ibatis.session.SqlSession;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ca.digitalcave.buddi.live.BuddiApplication;
import ca.digitalcave.buddi.live.db.util.DataUpdater;
import ca.digitalcave.buddi.live.db.util.DatabaseException;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.FormatUtil;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;

public class ScheduledTransactionsRunnerResource extends ServerResource {

	@Override
	protected void doInit() throws ResourceException {
		getVariants().add(new Variant(MediaType.APPLICATION_JSON));
	}
	
	@Override
	protected Representation post(Representation entity, Variant variant) throws ResourceException {
		final BuddiApplication application = (BuddiApplication) getApplication();
		final SqlSession sqlSession = application.getSqlSessionFactory().openSession();
		try {
			final User user = (User) getRequest().getClientInfo().getUser();

			if (user != null){
				//Grab the date passed in, to be used for running scheduled transactions.  If it is 
				// not set or is invalid (more than 1 day off of server time), use server time.
				Date userDate = null;
				try { userDate = FormatUtil.parseDateInternal(entity.getText()); } catch (Throwable e){}
				if (userDate == null || Days.daysBetween(new DateTime(), new DateTime(userDate)).size() > 1) {
					userDate = new Date();
				}
				
				//Check for outstanding scheduled transactions
				final String messages = DataUpdater.updateScheduledTransactions(user, sqlSession, userDate);
				final JSONObject result = new JSONObject();
				result.put("success", true);
				result.put("messages", messages);
				sqlSession.commit();
				
				return new JsonRepresentation(result);
			}
		} catch (CryptoException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		} catch (DatabaseException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		} catch (JSONException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		} finally {
			sqlSession.close();
		}
		
		throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
	}
}
