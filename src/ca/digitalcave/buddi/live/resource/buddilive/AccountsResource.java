package ca.digitalcave.buddi.live.resource.buddilive;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.ibatis.session.SqlSession;
import org.json.JSONArray;
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
import ca.digitalcave.buddi.live.db.util.ConstraintsChecker;
import ca.digitalcave.buddi.live.db.util.DataUpdater;
import ca.digitalcave.buddi.live.db.util.DatabaseException;
import ca.digitalcave.buddi.live.model.Account;
import ca.digitalcave.buddi.live.model.AccountType;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.CryptoUtil;
import ca.digitalcave.buddi.live.util.FormatUtil;
import ca.digitalcave.buddi.live.util.LocaleUtil;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;

public class AccountsResource extends ServerResource {

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
			final List<AccountType> accountsByType = sqlSession.getMapper(Sources.class).selectAccountTypes(user);
			//If the data is encrypted, we cannot rely on the DB to sort the account types properly.  Thus we have to.
			final Map<String, AccountType> accountTypeMap = new TreeMap<String, AccountType>();
			for (AccountType accountType : accountsByType) {
				final String key = (accountType.isDebit() ? "1" : "2") + CryptoUtil.decryptWrapper(accountType.getAccountType(), user);
				if (!accountTypeMap.containsKey(key)) {
					accountTypeMap.put(key, accountType);
				}
				else {
					accountTypeMap.get(key).getAccounts().addAll(accountType.getAccounts());
				}
			}
			
			final JSONArray data = new JSONArray();
			final StringBuilder sb = new StringBuilder();
			BigDecimal netWorth = BigDecimal.ZERO;
			for (String key : accountTypeMap.keySet()) {
				final JSONObject type = new JSONObject();
				AccountType t = accountTypeMap.get(key);
				type.put("name", CryptoUtil.decryptWrapper(t.getAccountType(), user));
				type.put("expanded", true);
				type.put("debit", t.isDebit());
				if (!t.isDebit()){
					sb.append(" color: " + FormatUtil.HTML_RED + ";");
				}
				sb.append(FormatUtil.formatBold());
				type.put("style", sb.toString());
				sb.setLength(0);
				type.put("nodeType", "type");
				type.put("icon", "img/folder-open-table.png");
				List<Account> accounts = t.getAccounts();
				Collections.sort(accounts, new Comparator<Account>() {
					@Override
					public int compare(Account o1, Account o2) {
						if (o1 == null || o2 == null) return 0;
						try {
							return CryptoUtil.decryptWrapper(o1.getName(), user).compareTo(CryptoUtil.decryptWrapper(o2.getName(), user));
						}
						catch (CryptoException e){
							return 0;
						}
					}
				});
				BigDecimal total = BigDecimal.ZERO;
				for (Account a : accounts) {
					if (!a.isDeleted() || user.isShowDeleted()){
						final JSONObject account = new JSONObject();
						account.put("id", a.getId());
						account.put("name", CryptoUtil.decryptWrapper(a.getName(), user));
						final BigDecimal balance = CryptoUtil.decryptWrapperBigDecimal(a.getBalance(), user, true);
						account.put("balance", FormatUtil.formatCurrency(a.isDebit() ? balance : balance.negate(), user));
						account.put("balanceStyle", (FormatUtil.isRed(a, balance) ? FormatUtil.formatRed() : ""));
						account.put("type", a.getType());
						account.put("accountType", CryptoUtil.decryptWrapper(a.getAccountType(), user));
						final BigDecimal startBalance = CryptoUtil.decryptWrapperBigDecimal(a.getStartBalance(), user, true);
						account.put("startBalance", startBalance);
						account.put("debit", a.isDebit());
						account.put("deleted", a.isDeleted());
						if (a.isDeleted()) sb.append(" text-decoration: line-through;");
						if (!a.isDebit()) sb.append(" color: " + FormatUtil.HTML_RED + ";");
						account.put("style", sb.toString());
						sb.setLength(0);
						account.put("leaf", true);
						account.put("nodeType", "account");
						account.put("icon", "img/table-money.png");
						type.append("children", account);
						total = total.add(a.isDebit() ? balance : balance.negate());
					}
				}
				type.put("balance", FormatUtil.formatCurrency(total, user));
				type.put("balanceStyle", FormatUtil.formatBold() + (FormatUtil.isRed(t.isDebit() ? total : total.negate()) ? FormatUtil.formatRed() : ""));
				netWorth = netWorth.add(t.isDebit() ? total : total.negate());
				if (type.has("children")){
					data.put(type);
				}
			}
			
			final JSONObject netWorthNode = new JSONObject();
			netWorthNode.put("name", LocaleUtil.getTranslation(getRequest()).getString("NET_WORTH"));
			netWorthNode.put("style", FormatUtil.formatBold());
			netWorthNode.put("leaf", true);
			sb.append(FormatUtil.formatBold());
			netWorthNode.put("icon", "img/table-sum.png");
			netWorthNode.put("balance", FormatUtil.formatCurrency(netWorth, user));
			netWorthNode.put("balanceStyle", FormatUtil.formatBold() + (FormatUtil.isRed(netWorth) ? FormatUtil.formatRed() : ""));
			data.put(netWorthNode);

			final JSONObject result = new JSONObject();
			result.put("children", data);
			result.put("success", true);
			return new JsonRepresentation(result);
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

	@Override
	protected Representation post(Representation entity, Variant variant) throws ResourceException {
		final BuddiApplication application = (BuddiApplication) getApplication();
		final SqlSession sqlSession = application.getSqlSessionFactory().openSession();
		final User user = (User) getRequest().getClientInfo().getUser();
		try {
			final JSONObject request = new JSONObject(entity.getText());
			final String action = request.optString("action");
			
			final Account account = new Account(request);
			
			if ("insert".equals(action)){
				ConstraintsChecker.checkInsertAccount(account, user, sqlSession);
				int count = sqlSession.getMapper(Sources.class).insertAccount(user, account);
				if (count != 1) throw new DatabaseException(String.format("Insert failed; expected 1 row, returned %s", count));
			} 
			else if ("delete".equals(action) || "undelete".equals(action)){
				if (sqlSession.getMapper(Sources.class).selectSourceAssociatedCount(user, account) == 0){
					int count = sqlSession.getMapper(Sources.class).deleteSource(user, account);
					if (count != 1) throw new DatabaseException(String.format("Delete failed; expected 1 row, returned %s", count));
				}
				else {
					account.setDeleted("delete".equals(action));
					int count = sqlSession.getMapper(Sources.class).updateSourceDeleted(user, account);
					if (count != 1) throw new DatabaseException(String.format("Delete / undelete failed; expected 1 row, returned %s", count));
				}
			}
			else if ("update".equals(action)){
				ConstraintsChecker.checkUpdateAccount(account, user, sqlSession);
				int count = sqlSession.getMapper(Sources.class).updateAccount(user, account);
				if (count != 1) throw new DatabaseException(String.format("Update failed; expected 1 row, returned %s", count));
			}
			else {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, LocaleUtil.getTranslation(getRequest()).getString("ACTION_PARAMETER_MUST_BE_SPECIFIED"));
			}
			
			DataUpdater.updateBalances(user, sqlSession);
			
			sqlSession.commit();
			final JSONObject result = new JSONObject();
			result.put("success", true);
			return new JsonRepresentation(result);
		}
		catch (DatabaseException e){
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		}
		catch (IOException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		catch (JSONException e){
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		}
		catch (CryptoException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		finally {
			sqlSession.close();
		}
	}
}
