package ca.digitalcave.buddi.live.resource.buddilive.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
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
import ca.digitalcave.buddi.live.db.Transactions;
import ca.digitalcave.buddi.live.model.Split;
import ca.digitalcave.buddi.live.model.Transaction;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.CryptoUtil;
import ca.digitalcave.buddi.live.util.FormatUtil;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;

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
			final Map<Integer, BigDecimal> totalsByCategory = new HashMap<Integer, BigDecimal>();
			final Map<Integer, String> labelsByCategory = new HashMap<Integer, String>();
			final List<Transaction> data = sqlSession.getMapper(Transactions.class).selectTransactions(user, type, dates[0], dates[1]);
			for (Transaction transaction : data) {
				for (Split split : transaction.getSplits()) {
					//The query will already have filtered the splits to only those which have a from or to source type
					// of I or E; since both from and to sources will not be the same (you can't have a split with both
					// sources being a category), we can safely say that if the from type is not the requested type, then
					// the to type must be.
					final Integer categoryId = type.equals(split.getFromType()) ? split.getFromSource() : split.getToSource();
					final BigDecimal amount = CryptoUtil.decryptWrapperBigDecimal(split.getAmount(), user, true);
					totalsByCategory.put(categoryId, totalsByCategory.get(categoryId) == null ? amount : totalsByCategory.get(categoryId).add(amount));
					
					//Keep track of the label for the given source
					if (labelsByCategory.get(categoryId) == null) {
						labelsByCategory.put(categoryId, type.equals(split.getFromType()) ? split.getFromSourceName() : split.getToSourceName());
					}
				}
			}
			BigDecimal total = BigDecimal.ZERO;
			for (BigDecimal subtotal : totalsByCategory.values()) {
				total = total.add(subtotal);
			}
			total = total.divide(new BigDecimal(100));  //The total is only used for calculating percents; divide by 100 now instead of multiplying after every calculation
			
			//Sort categories by total
			final List<Integer> categories = new ArrayList<Integer>(totalsByCategory.keySet());
			Collections.sort(categories, new Comparator<Integer>() {
				@Override
				public int compare(Integer o1, Integer o2) {
					return -1 * totalsByCategory.get(o1).compareTo(totalsByCategory.get(o2));
				}
			});
			
			final JSONObject result = new JSONObject();
			for (Integer categoryId : categories){
				final JSONObject object = new JSONObject();
				final BigDecimal amount = totalsByCategory.get(categoryId);
				object.put("label", CryptoUtil.decryptWrapper(labelsByCategory.get(categoryId), user) + " - " + FormatUtil.formatCurrency(amount, user));
				object.put("amount", amount);
				object.put("formattedAmount", FormatUtil.formatCurrency(amount, user));
				object.put("percent", amount.divide(total, RoundingMode.HALF_UP));
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
