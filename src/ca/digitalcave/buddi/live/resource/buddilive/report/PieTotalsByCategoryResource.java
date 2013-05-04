package ca.digitalcave.buddi.live.resource.buddilive.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import ca.digitalcave.buddi.live.model.report.Pie;
import ca.digitalcave.buddi.live.util.CryptoUtil;
import ca.digitalcave.buddi.live.util.CryptoUtil.CryptoException;
import ca.digitalcave.buddi.live.util.FormatUtil;

public class PieTotalsByCategoryResource extends ServerResource {

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
			final String type = getQuery().getFirstValue("type");
			if (!"E".equals(type) && !"I".equals(type)) throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "type parameter (I or E) is required");
			
			final Date[] dates = ReportHelper.processInterval(getQuery());
			final List<Pie> data = sqlSession.getMapper(Reports.class).selectPieIncomeOrExpensesByCategory(user, type, dates[0], dates[1]);
			BigDecimal total = BigDecimal.ZERO;
			final BigDecimal ONE_HUNDRED = new BigDecimal(100);
			for (Pie pie : data) { total = total.add(pie.getAmount()); }
			
			final JSONObject result = new JSONObject();
			for (Pie pie : data){
				final JSONObject object = new JSONObject();
				object.put("label", CryptoUtil.decryptWrapper(pie.getLabel(), user));
				object.put("amount", pie.getAmount());
				object.put("formattedAmount", FormatUtil.formatCurrency(pie.getAmount(), user));
				object.put("percent", pie.getAmount().divide(total, RoundingMode.HALF_UP).multiply(ONE_HUNDRED));
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
