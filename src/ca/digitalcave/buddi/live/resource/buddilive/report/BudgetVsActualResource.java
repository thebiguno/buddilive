package ca.digitalcave.buddi.live.resource.buddilive.report;

import java.math.BigDecimal;
import java.sql.SQLException;
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
import ca.digitalcave.buddi.live.db.Sources;
import ca.digitalcave.buddi.live.db.Transactions;
import ca.digitalcave.buddi.live.model.Category;
import ca.digitalcave.buddi.live.model.Split;
import ca.digitalcave.buddi.live.model.Transaction;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.CryptoUtil;
import ca.digitalcave.buddi.live.util.FormatUtil;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;

public class BudgetVsActualResource extends ServerResource {

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
			final List<Transaction> transactions = sqlSession.getMapper(Transactions.class).selectTransactions(user, dates[0], dates[1]);
			final List<Category> categories = sqlSession.getMapper(Sources.class).selectCategories(user);

			final Map<Integer, BigDecimal> actualByCategory = new HashMap<Integer, BigDecimal>();
			for (Transaction t : transactions) {
				for (Split s : t.getSplits()) {
					if ("I".equals(s.getFromType()) || "E".equals(s.getFromType())) {
						final int id = s.getFromSource();
						final BigDecimal amt = CryptoUtil.decryptWrapperBigDecimal(s.getAmount(), user, true);
						actualByCategory.put(id, amt.add(actualByCategory.getOrDefault(id, BigDecimal.ZERO)));
					} else if ("I".equals(s.getToType()) || "E".equals(s.getToType())) {
						final int id = s.getToSource();
						final BigDecimal amt = CryptoUtil.decryptWrapperBigDecimal(s.getAmount(), user, true);
						actualByCategory.put(id, amt.add(actualByCategory.getOrDefault(id, BigDecimal.ZERO)));
					}
				}
			}

			Collections.sort(categories, new Comparator<Category>() {
				@Override
				public int compare(Category o1, Category o2) {
					if (o1 == null || o2 == null) return 0;
					if (o1.isIncome() != o2.isIncome()) return o1.isIncome() ? -1 : 1;
					try {
						return CryptoUtil.decryptWrapper(o1.getName(), user).compareTo(CryptoUtil.decryptWrapper(o2.getName(), user));
					} catch (CryptoException e) { return 0; }
				}
			});

			final JSONObject result = new JSONObject();
			for (Category c : categories) {
				if (c.isDeleted()) continue;
				final BigDecimal budgeted = c.getAmount(user, sqlSession, dates[0], dates[1]);
				final BigDecimal actual = actualByCategory.getOrDefault(c.getId(), BigDecimal.ZERO);
				if (budgeted.compareTo(BigDecimal.ZERO) == 0 && actual.compareTo(BigDecimal.ZERO) == 0) continue;

				final JSONObject row = new JSONObject();
				row.put("category", CryptoUtil.decryptWrapper(c.getName(), user));
				row.put("budgeted", budgeted.doubleValue());
				row.put("actual", actual.doubleValue());
				row.put("budgetedFormatted", FormatUtil.formatCurrency(budgeted, user));
				row.put("actualFormatted", FormatUtil.formatCurrency(actual, user));
				row.put("income", c.isIncome());
				result.append("data", row);
			}

			result.put("success", true);
			return new JsonRepresentation(result);
		} catch (NumberFormatException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		} catch (CryptoException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		} catch (JSONException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		} catch (SQLException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		} finally {
			sqlSession.close();
		}
	}
}
