package ca.digitalcave.buddi.live.resource.buddilive.report;

import java.util.Date;

import org.apache.ibatis.session.SqlSession;
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
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.FormatUtil;
import ca.digitalcave.moss.common.DateUtil;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;

public class NetWorthOverTimeResource extends ServerResource {

	@Override
	protected void doInit() throws ResourceException {
		getVariants().add(new Variant(MediaType.APPLICATION_JSON));
	}

	@Override
	protected Representation get(Variant variant) throws ResourceException {
		final BuddiApplication application = (BuddiApplication) getApplication();
		final SqlSession sqlSession = application.getSqlSessionFactory().openSession(true);
		final User user = (User) getRequest().getClientInfo().getUser();
		try {
			final Date[] dates = ReportHelper.processInterval(getQuery());
			
			int numberOfDaysBetween = DateUtil.getDaysBetween(dates[0], dates[1], false);
			int daysBetweenReport = numberOfDaysBetween / 12;

			final JSONObject result = new JSONObject();
			for (int i = 0; i < 12; i++){
				final Date date = DateUtil.addDays(dates[0], i * daysBetweenReport);
				if (date.before(dates[1])){
					final JSONObject object = new JSONObject();
					object.put("date", FormatUtil.formatDate(date, user));
					object.put("amount", Math.random() * 1000);
					result.append("data", object);
				}
			}

			result.put("success", true);
			return new JsonRepresentation(result);
		}
		catch (NumberFormatException e){
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}
//		catch (CryptoException e){
//			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
//		}
		catch (JSONException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		finally {
			sqlSession.close();
		}
	}
}
