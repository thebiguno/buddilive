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

public class BalancesOverTimeResource extends ServerResource {

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
			final boolean netWorthOnly = Boolean.parseBoolean(getQuery().getFirstValue("netWorthOnly", "false"));
			final Date[] dates = ReportHelper.processInterval(getQuery());
			final List<Account> accountBalances = sqlSession.getMapper(Sources.class).selectAccountBalances(user);
			//Running balances for each account, populated by the transactions in accountBalances.
			final Map<Integer, BigDecimal> balances = new HashMap<Integer, BigDecimal>();

			//Figure out proper starting range; it doesn't matter if they asked for data from 1900, if 
			// we only started entering transactions yesterday, that is the earliest we will get.
			if (accountBalances.get(0).getStartDate().after(dates[0])) dates[0] = accountBalances.get(0).getStartDate();
			int numberOfDaysBetween = DateUtil.getDaysBetween(dates[0], dates[1], false);
			int daysBetweenReport = Math.max(1, numberOfDaysBetween / 500);	//Try for 50 items in the list; if there are less than this, no worries.
			
			final JSONObject result = new JSONObject();
			int accountBalancesIndex = 0;
			
			//Iterate over dates, counting by numberOfDaysBetween, until we get to the end date.
			while (dates[0].before(dates[1])){
				//While iterating over dates, loop through transaction balances up to the given day.  Record
				// running balances in the balances map.
				while (accountBalancesIndex < accountBalances.size() 
						&& accountBalances.get(accountBalancesIndex) != null
						&& accountBalances.get(accountBalancesIndex).getStartDate() != null
						&& accountBalances.get(accountBalancesIndex).getStartDate().before(dates[0])){
					//Accumulate new values until we are up to the next required date
					final BigDecimal balance = CryptoUtil.decryptWrapperBigDecimal(accountBalances.get(accountBalancesIndex).getBalance(), user, true);
					balances.put(accountBalances.get(accountBalancesIndex).getId(), balance);
					accountBalancesIndex++;
				}
				
				//For each interval date, show a data point, even if there has been no activity in the given account since last time.
				final JSONObject dataItem = new JSONObject();
				dataItem.put("date", FormatUtil.formatDate(dates[0], user));
				BigDecimal netWorth = BigDecimal.ZERO;
				for (int j : balances.keySet()) {
					if (!netWorthOnly) dataItem.put("a" + j, balances.get(j));
					netWorth = netWorth.add(balances.get(j));
				}
				dates[0] = DateUtil.addDays(dates[0], daysBetweenReport);
				dataItem.put("netWorth", netWorth);
				result.append("data", dataItem);
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
