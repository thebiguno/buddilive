package ca.digitalcave.buddi.live.resource.buddilive.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import ca.digitalcave.buddi.live.model.Account;
import ca.digitalcave.buddi.live.model.Split;
import ca.digitalcave.buddi.live.model.Transaction;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.CryptoUtil;
import ca.digitalcave.buddi.live.util.FormatUtil;
import ca.digitalcave.moss.common.DateUtil;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;

/**
 * Projects account balances forward using average monthly net change from the past 12 months.
 * Query params: days=N (default 90) — how many days to project forward.
 */
public class ProjectedBalanceResource extends ServerResource {

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
			final int days = Integer.parseInt(getQuery().getFirstValue("days", "90"));
			final Date today = DateUtil.getStartOfDay(new Date());
			final Date endDate = DateUtil.addDays(today, days);

			// Load current account balances and names
			final List<Account> accounts = sqlSession.getMapper(Sources.class).selectAccounts(user);
			final Map<Integer, BigDecimal> balances = new HashMap<Integer, BigDecimal>();
			final Map<Integer, String> accountNames = new HashMap<Integer, String>();
			for (Account a : accounts) {
				final String rawBal = a.getBalance();
				final BigDecimal bal = (rawBal == null || rawBal.isEmpty())
						? BigDecimal.ZERO
						: CryptoUtil.decryptWrapperBigDecimal(rawBal, user, false);
				balances.put(a.getId(), bal);
				accountNames.put(a.getId(), CryptoUtil.decryptWrapper(a.getName(), user));
			}

			// Load 12 months of historical transactions
			final Date histStart = DateUtil.addMonths(today, -12);
			final List<Transaction> transactions = sqlSession.getMapper(Transactions.class).selectTransactions(user, histStart, today);

			// Bucket net change per account per calendar month
			// monthlyNet: accountId -> (monthKey -> netChange)
			final Map<Integer, Map<String, BigDecimal>> monthlyNet = new HashMap<Integer, Map<String, BigDecimal>>();
			final Calendar cal = Calendar.getInstance();
			for (Transaction t : transactions) {
				cal.setTime(t.getDate());
				final String monthKey = String.format("%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
				for (Split s : t.getSplits()) {
					final BigDecimal amt = CryptoUtil.decryptWrapperBigDecimal(s.getAmount(), user, false);
					// from account: loses amt (positive stored amount = debit from account)
					if ("D".equals(s.getFromType()) || "C".equals(s.getFromType())) {
						final int id = s.getFromSource();
						monthlyNet.computeIfAbsent(id, k -> new HashMap<>());
						monthlyNet.get(id).merge(monthKey, amt.negate(), BigDecimal::add);
					}
					// to account: gains amt
					if ("D".equals(s.getToType()) || "C".equals(s.getToType())) {
						final int id = s.getToSource();
						monthlyNet.computeIfAbsent(id, k -> new HashMap<>());
						monthlyNet.get(id).merge(monthKey, amt, BigDecimal::add);
					}
				}
			}

			// Compute average monthly net change per account, only include accounts with activity
			final Map<Integer, BigDecimal> avgMonthlyDelta = new HashMap<Integer, BigDecimal>();
			final Set<Integer> activeAccountIds = new HashSet<Integer>();
			for (Map.Entry<Integer, Map<String, BigDecimal>> entry : monthlyNet.entrySet()) {
				final Map<String, BigDecimal> byMonth = entry.getValue();
				if (byMonth.isEmpty()) continue;
				BigDecimal sum = BigDecimal.ZERO;
				for (BigDecimal v : byMonth.values()) sum = sum.add(v);
				final BigDecimal avg = sum.divide(BigDecimal.valueOf(byMonth.size()), 10, RoundingMode.HALF_UP);
				// Only include accounts where average monthly change is non-trivial (> $1)
				if (avg.abs().compareTo(BigDecimal.ONE) > 0) {
					avgMonthlyDelta.put(entry.getKey(), avg);
					activeAccountIds.add(entry.getKey());
				}
			}

			// Build result
			final JSONObject result = new JSONObject();
			for (Account a : accounts) {
				if (activeAccountIds.contains(a.getId())) {
					result.append("accounts", accountNames.get(a.getId()));
				}
			}

			// Walk weekly, applying monthly deltas on the 1st of each month
			Date cursor = (Date) today.clone();
			int dayCount = 0;
			final Set<String> appliedMonths = new HashSet<String>();
			while (!cursor.after(endDate)) {
				cal.setTime(cursor);
				final String monthKey = String.format("%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);

				// Apply average delta on the 1st of each projected month
				if (cal.get(Calendar.DAY_OF_MONTH) == 1 && !cursor.equals(today) && !appliedMonths.contains(monthKey)) {
					appliedMonths.add(monthKey);
					for (Map.Entry<Integer, BigDecimal> e : avgMonthlyDelta.entrySet()) {
						balances.merge(e.getKey(), e.getValue(), BigDecimal::add);
					}
				}

				if (dayCount % 7 == 0 || cursor.equals(today) || cursor.equals(endDate)) {
					final JSONObject point = new JSONObject();
					point.put("date", FormatUtil.formatDate(cursor, user));
					BigDecimal netWorth = BigDecimal.ZERO;
					for (Account a : accounts) {
						final BigDecimal bal = balances.getOrDefault(a.getId(), BigDecimal.ZERO);
						netWorth = netWorth.add(bal);
						if (activeAccountIds.contains(a.getId())) {
							point.put("a" + a.getId(), bal.doubleValue());
						}
					}
					point.put("netWorth", netWorth.doubleValue());
					result.append("data", point);
				}

				cursor = DateUtil.addDays(cursor, 1);
				dayCount++;
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
