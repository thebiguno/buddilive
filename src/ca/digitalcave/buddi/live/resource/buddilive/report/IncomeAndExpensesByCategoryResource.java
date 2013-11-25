package ca.digitalcave.buddi.live.resource.buddilive.report;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
import ca.digitalcave.buddi.live.db.Sources;
import ca.digitalcave.buddi.live.model.Category;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.CryptoUtil;
import ca.digitalcave.buddi.live.util.FormatUtil;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;

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
			final Map<Integer, Map<String, Object>> data = sqlSession.getMapper(Reports.class).selectActualByCategory(user, dates[0], dates[1]);
			final List<Category> categories = sqlSession.getMapper(Sources.class).selectCategories(user);
			
			final JSONObject result = new JSONObject();
			for (Category category : categories) {
				final BigDecimal budgetedAmount = category.getAmount(user, sqlSession, dates[0], dates[1]);
				final BigDecimal actualAmount = data.get(category.getId()) == null ? BigDecimal.ZERO : (BigDecimal) data.get(category.getId()).get("actual");
				if (budgetedAmount.compareTo(BigDecimal.ZERO) != 0 || actualAmount.compareTo(BigDecimal.ZERO) != 0){
					final JSONObject object = new JSONObject();
					object.put("category", CryptoUtil.decryptWrapper(category.getName(), user));

					object.put("actual", FormatUtil.formatCurrency(actualAmount, user));
					object.put("actualStyle", (FormatUtil.isRed(category, actualAmount) ? FormatUtil.formatRed() : ""));
					
					object.put("currentAmount", FormatUtil.formatCurrency(budgetedAmount, user));
					object.put("currentAmountStyle", (FormatUtil.isRed(category, budgetedAmount) ? FormatUtil.formatRed() : ""));
					
					final BigDecimal difference = (actualAmount.subtract(budgetedAmount != null ? budgetedAmount : BigDecimal.ZERO));
					object.put("difference", FormatUtil.formatCurrency(category.isIncome() ? difference : difference.negate(), user));
					object.put("differenceStyle", (FormatUtil.isRed(category.isIncome() ? difference : difference.negate()) ? FormatUtil.formatRed() : ""));

					result.append("data", object);
				}
				
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
		catch (SQLException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		finally {
			sqlSession.close();
		}
	}
}
