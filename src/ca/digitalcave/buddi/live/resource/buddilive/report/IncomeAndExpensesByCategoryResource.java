package ca.digitalcave.buddi.live.resource.buddilive.report;

import java.util.Date;
import java.util.List;

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
import ca.digitalcave.buddi.live.db.Reports;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.model.report.Summary;
import ca.digitalcave.buddi.live.util.CryptoUtil;
import ca.digitalcave.buddi.live.util.CryptoUtil.CryptoException;
import ca.digitalcave.buddi.live.util.FormatUtil;

public class IncomeAndExpensesByCategoryResource extends ServerResource {

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
			final List<Summary> data = sqlSession.getMapper(Reports.class).selectActualIncomeAndExpensesByCategory(user, dates[0], dates[1]);
			
			final JSONObject result = new JSONObject();
			for (Summary summary : data){
				final JSONObject object = new JSONObject();
				object.put("source", CryptoUtil.decryptWrapper(summary.getSource().getName(), user));
				object.put("actual", FormatUtil.formatCurrency(summary.getActual(), user, summary.getSource()));
				object.put("budgeted", FormatUtil.formatCurrency(summary.getBudgeted(), user, summary.getSource()));
				object.put("difference", FormatUtil.formatCurrency(summary.getDifference(), user, summary.getSource()));
				result.append("data", object);
			}

			result.put("success", true);
			return new JsonRepresentation(result);
		}
		catch (NumberFormatException e){
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}
		catch (CryptoException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		catch (JSONException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		finally {
			sqlSession.close();
		}
	}
}
