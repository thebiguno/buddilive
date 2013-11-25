package ca.digitalcave.buddi.live.resource.buddilive;

import java.io.IOException;
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
import ca.digitalcave.buddi.live.db.ScheduledTransactions;
import ca.digitalcave.buddi.live.db.util.ConstraintsChecker;
import ca.digitalcave.buddi.live.db.util.DataUpdater;
import ca.digitalcave.buddi.live.db.util.DatabaseException;
import ca.digitalcave.buddi.live.model.ScheduledTransaction;
import ca.digitalcave.buddi.live.model.Split;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.CryptoUtil;
import ca.digitalcave.buddi.live.util.FormatUtil;
import ca.digitalcave.buddi.live.util.LocaleUtil;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;

public class ScheduledTransactionsResource extends ServerResource {

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
			final List<ScheduledTransaction> scheduledTransactions = sqlSession.getMapper(ScheduledTransactions.class).selectScheduledTransactions(user);
			final JSONObject result = new JSONObject();
			
			for (ScheduledTransaction t : scheduledTransactions) {
				final JSONObject scheduledTransaction = new JSONObject();
				scheduledTransaction.put("id", t.getId());
				scheduledTransaction.put("name", CryptoUtil.decryptWrapper(t.getScheduleName(), user));
				scheduledTransaction.put("description", CryptoUtil.decryptWrapper(t.getDescription(), user));
				scheduledTransaction.put("number", CryptoUtil.decryptWrapper(t.getNumber(), user));
				scheduledTransaction.put("scheduleDay", t.getScheduleDay());
				scheduledTransaction.put("scheduleWeek", t.getScheduleWeek());
				scheduledTransaction.put("scheduleMonth", t.getScheduleMonth());
				scheduledTransaction.put("start", FormatUtil.formatDateInternal(t.getStartDate()));
				scheduledTransaction.put("end", FormatUtil.formatDateInternal(t.getEndDate()));
				scheduledTransaction.put("repeat", t.getFrequencyType());
				scheduledTransaction.put("message", CryptoUtil.decryptWrapper(t.getMessage(), user));
				for (Split s : t.getSplits()) {
					final JSONObject split = new JSONObject();
					split.put("id", s.getId());
					split.put("amount", FormatUtil.formatCurrency(s.getAmount(), user));
					split.put("amountNumber", s.getAmount());
					split.put("fromId", s.getFromSource());
					split.put("toId", s.getToSource());
					split.put("memo", CryptoUtil.decryptWrapper(s.getMemo(), user));
					scheduledTransaction.append("splits", split);
				}
				result.append("data", scheduledTransaction);
			}
			
			result.put("total", scheduledTransactions.size());
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
			final JSONObject json = new JSONObject(entity.getText());
			final String action = json.optString("action");
			
			if ("insert".equals(action)){
				final ScheduledTransaction scheduledTransaction = new ScheduledTransaction(json);
				ConstraintsChecker.checkInsertScheduledTransaction(scheduledTransaction, user, sqlSession);
				
				int count = sqlSession.getMapper(ScheduledTransactions.class).insertScheduledTransaction(user, scheduledTransaction);
				if (count != 1) throw new DatabaseException(String.format("Insert failed; expected 1 row, returned %s", count));

				for (Split split : scheduledTransaction.getSplits()) {
					split.setTransactionId(scheduledTransaction.getId());
					
					count = sqlSession.getMapper(ScheduledTransactions.class).insertScheduledSplit(user, split);
					if (count != 1) throw new DatabaseException(String.format("Insert failed; expected 1 row, returned %s", count));
				}
			} 
			else if ("update".equals(action)){
				final ScheduledTransaction scheduledTransaction = new ScheduledTransaction(json);
				ConstraintsChecker.checkUpdateScheduledTransaction(scheduledTransaction, user, sqlSession);
				
				int count = sqlSession.getMapper(ScheduledTransactions.class).updateScheduledTransaction(user, scheduledTransaction);
				if (count != 1) throw new DatabaseException(String.format("Update failed; expected 1 row, returned %s", count));

				count = sqlSession.getMapper(ScheduledTransactions.class).deleteScheduledSplits(user, scheduledTransaction);
				if (count == 0) throw new DatabaseException(String.format("Delete scheduled splits failed; expected 1 or more rows, returned %s", count));
				for (Split split : scheduledTransaction.getSplits()) {
					split.setTransactionId(scheduledTransaction.getId());
					
					count = sqlSession.getMapper(ScheduledTransactions.class).insertScheduledSplit(user, split);
					if (count != 1) throw new DatabaseException(String.format("Insert failed; expected 1 row, returned %s", count));
				}
			}
			else if ("delete".equals(action)){
				final ScheduledTransaction scheduledTransaction = new ScheduledTransaction();
				scheduledTransaction.setId(json.getLong("id"));
				int count = sqlSession.getMapper(ScheduledTransactions.class).deleteScheduledTransaction(user, scheduledTransaction);
				if (count != 1) throw new DatabaseException(String.format("Update failed; expected 1 row, returned %s", count));
				//We rely on delete cascading to clean up scheduled splits.
			}
			else {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, LocaleUtil.getTranslation(getRequest()).getString("ACTION_PARAMETER_MUST_BE_SPECIFIED"));
			}
			
			DataUpdater.updateBalances(user, sqlSession, false);
			
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
		catch (CryptoException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		catch (JSONException e){
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		}
		finally {
			sqlSession.close();
		}
	}
}
