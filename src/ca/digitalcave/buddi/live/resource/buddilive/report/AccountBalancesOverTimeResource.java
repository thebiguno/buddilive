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

public class AccountBalancesOverTimeResource extends ServerResource {

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
			final List<Account> accountBalances = sqlSession.getMapper(Sources.class).selectAccountBalances(user);
			//Running balances for each account; initialized with starting balances.
			final Map<Integer, BigDecimal> balances = new HashMap<Integer, BigDecimal>();
//			for (Account account : accounts) {
//				final BigDecimal balance = CryptoUtil.decryptWrapperBigDecimal(account.getStartBalance(), user, true);
//				balances.put(account.getId(), "C".equals(account.getType()) ? balance.negate() : balance);	//TODO This makes credit accounts appear positive.  Not sure if this is right or not...
//				balances.put(account.getId(), balance);
//			}
			
			if (accountBalances.get(0).getStartDate().after(dates[0])) dates[0] = accountBalances.get(0).getStartDate();
			int numberOfDaysBetween = DateUtil.getDaysBetween(dates[0], dates[1], false);
			int daysBetweenReport = Math.max(1, numberOfDaysBetween / 500);	//Try for 50 items in the list; if there are less than this, no worries.
			
			final JSONObject result = new JSONObject();
			int accountBalancesIndex = 0;
			
			while (dates[0].before(dates[1])){
				while (accountBalancesIndex < accountBalances.size() && accountBalances.get(accountBalancesIndex).getStartDate().before(dates[0])){
					//Accumulate new values until we are up to the next required date
					final BigDecimal balance = CryptoUtil.decryptWrapperBigDecimal(accountBalances.get(accountBalancesIndex).getBalance(), user, true);
					balances.put(accountBalances.get(accountBalancesIndex).getId(), balance);
					accountBalancesIndex++;
				}
				
				final JSONObject dataItem = new JSONObject();
				dataItem.put("date", FormatUtil.formatDate(dates[0], user));
				for (int j : balances.keySet()) {
					dataItem.put("a" + j, balances.get(j));
				}
				dates[0] = DateUtil.addDays(dates[0], daysBetweenReport);
				result.append("data", dataItem);
			}
			
//			for (int i = 0; i < accountBalances.size(); i++) {
//				final Account account = accountBalances.get(i);
//				//If we have progressed beyond the next date to print, show accumulated values so far.
//				// We (ab)use the account.startDate field to hold the actual transaction date, rather 
//				// than the account start date itself, for this report; see SQL for details. 
//				if (account.getStartDate().after(dates[0]) || (i == accountBalances.size() - 1 && account.getStartDate().after(dates[0]))){
//					final JSONObject dataItem = new JSONObject();
//					dataItem.put("date", FormatUtil.formatDate(account.getStartDate(), user));
//					for (int j : balances.keySet()) {
//						dataItem.put("a" + j, balances.get(j));
//					}
//					dates[0] = DateUtil.addDays(account.getStartDate(), daysBetweenReport);
//					result.append("data", dataItem);
//				}
//				
//				//Accumulate new values
//				final BigDecimal balance = CryptoUtil.decryptWrapperBigDecimal(account.getBalance(), user, true);
//				balances.put(account.getId(), balance);
//			}
			
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
