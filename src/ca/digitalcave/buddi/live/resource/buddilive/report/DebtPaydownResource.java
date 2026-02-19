package ca.digitalcave.buddi.live.resource.buddilive.report;

import java.math.BigDecimal;
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
import ca.digitalcave.buddi.live.model.Account;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.CryptoUtil;
import ca.digitalcave.buddi.live.util.FormatUtil;
import ca.digitalcave.moss.common.DateUtil;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;

/**
 * Returns balance-over-time data for credit accounts only (debt paydown tracking).
 * Reuses the selectAccountBalances query which returns running balances per account per transaction date.
 */
public class DebtPaydownResource extends ServerResource {

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

			// Get all accounts with running balances (same as BalancesOverTime)
			final List<Account> accountBalances = sqlSession.getMapper(Sources.class).selectAccountBalances(user);
			if (accountBalances.isEmpty()) {
				final JSONObject result = new JSONObject();
				result.put("success", true);
				return new JsonRepresentation(result);
			}

			// Get credit accounts only
			final List<Account> allAccounts = sqlSession.getMapper(Sources.class).selectAccounts(user);
			final Map<Integer, String> creditAccountNames = new HashMap<Integer, String>();
			for (Account a : allAccounts) {
				if ("C".equals(a.getType()) && !a.isDeleted()) {
					creditAccountNames.put(a.getId(), CryptoUtil.decryptWrapper(a.getName(), user));
				}
			}

			if (creditAccountNames.isEmpty()) {
				final JSONObject result = new JSONObject();
				result.put("success", true);
				result.put("noData", true);
				return new JsonRepresentation(result);
			}

			// Emit series names
			final JSONObject result = new JSONObject();
			for (Map.Entry<Integer, String> e : creditAccountNames.entrySet()) {
				final JSONObject series = new JSONObject();
				series.put("id", e.getKey());
				series.put("name", e.getValue());
				result.append("series", series);
			}

			// Adjust start date to earliest transaction if needed
			if (accountBalances.get(0).getStartDate() != null && accountBalances.get(0).getStartDate().after(dates[0])) {
				dates[0] = accountBalances.get(0).getStartDate();
			}

			final int numberOfDays = DateUtil.getDaysBetween(dates[0], dates[1], false);
			final int step = Math.max(1, numberOfDays / 500);

			final Map<Integer, BigDecimal> balances = new HashMap<Integer, BigDecimal>();
			int idx = 0;

			while (dates[0].before(dates[1])) {
				while (idx < accountBalances.size()
						&& accountBalances.get(idx) != null
						&& accountBalances.get(idx).getStartDate() != null
						&& accountBalances.get(idx).getStartDate().before(dates[0])) {
					final BigDecimal bal = CryptoUtil.decryptWrapperBigDecimal(accountBalances.get(idx).getBalance(), user, true);
					balances.put(accountBalances.get(idx).getId(), bal);
					idx++;
				}

				final JSONObject point = new JSONObject();
				point.put("date", FormatUtil.formatDate(dates[0], user));
				for (Integer id : creditAccountNames.keySet()) {
					point.put("a" + id, balances.getOrDefault(id, BigDecimal.ZERO).doubleValue());
				}
				result.append("data", point);

				dates[0] = DateUtil.addDays(dates[0], step);
			}

			result.put("success", true);
			return new JsonRepresentation(result);
		} catch (NumberFormatException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		} catch (CryptoException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		} catch (JSONException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		} finally {
			sqlSession.close();
		}
	}
}
