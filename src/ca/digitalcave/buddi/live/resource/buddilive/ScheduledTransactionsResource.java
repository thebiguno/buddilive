package ca.digitalcave.buddi.live.resource.buddilive;

import java.util.List;

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
import ca.digitalcave.buddi.live.db.ScheduledTransactions;
import ca.digitalcave.buddi.live.model.ScheduledTransaction;
import ca.digitalcave.buddi.live.model.Split;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.CryptoUtil;
import ca.digitalcave.buddi.live.util.CryptoUtil.CryptoException;
import ca.digitalcave.buddi.live.util.FormatUtil;

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
			
			final JSONArray data = new JSONArray();
			for (ScheduledTransaction t : scheduledTransactions) {
				final JSONObject scheduledTransaction = new JSONObject();
				scheduledTransaction.put("id", t.getId());
				scheduledTransaction.put("name", CryptoUtil.decryptWrapper(t.getDescription(), user));
				scheduledTransaction.put("number", CryptoUtil.decryptWrapper(t.getNumber(), user));
				for (Split s : t.getSplits()) {
					final JSONObject split = new JSONObject();
					split.put("id", s.getId());
					split.put("amount", FormatUtil.formatCurrency(s.getAmount()));
					split.put("fromId", s.getFromSource());
					split.put("from", CryptoUtil.decryptWrapper(s.getFromSourceName(), user));
					split.put("toId", s.getToSource());
					split.put("to", CryptoUtil.decryptWrapper(s.getToSourceName(), user));
					split.put("memo", CryptoUtil.decryptWrapper(s.getMemo(), user));
					scheduledTransaction.append("splits", split);
				}
				data.put(scheduledTransaction);
			}
			
			final JSONObject result = new JSONObject();
			result.put("data", data);
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
	
//	@Override
//	protected Representation post(Representation entity, Variant variant) throws ResourceException {
//		final BuddiApplication application = (BuddiApplication) getApplication();
//		final SqlSession sqlSession = application.getSqlSessionFactory().openSession();
//		final User user = (User) getRequest().getClientInfo().getUser();
//		try {
//			final JSONObject json = new JSONObject(entity.getText());
//			final String action = json.optString("action");
//			
//			if ("insert".equals(action)){
//				final Transaction transaction = new Transaction(json);
//				ConstraintsChecker.checkInsertTransaction(transaction, user, sqlSession);
//				
//				int count = sqlSession.getMapper(Transactions.class).insertTransaction(user, transaction);
//				if (count != 1) throw new DatabaseException(String.format("Insert failed; expected 1 row, returned %s", count));
//
//				for (Split split : transaction.getSplits()) {
//					split.setTransactionId(transaction.getId());
//					
//					count = sqlSession.getMapper(Transactions.class).insertSplit(user, split);
//					if (count != 1) throw new DatabaseException(String.format("Insert failed; expected 1 row, returned %s", count));
//				}
//			} 
//			else if ("update".equals(action)){
//				final Transaction transaction = new Transaction(json);
//				ConstraintsChecker.checkUpdateTransaction(transaction, user, sqlSession);
//				
//				int count = sqlSession.getMapper(Transactions.class).updateTransaction(user, transaction);
//				if (count != 1) throw new DatabaseException(String.format("Update failed; expected 1 row, returned %s", count));
//
//				//To update, we delete all splits associated with the given transaction, and re-insert them according to the given data packet.
//				count = sqlSession.getMapper(Transactions.class).deleteSplits(user, transaction);
//				if (count == 0) throw new DatabaseException("Failed to delete splits; expected 1 or more rows, returned 0");
//
//				for (Split split : transaction.getSplits()) {
//					split.setTransactionId(transaction.getId());
//					count = sqlSession.getMapper(Transactions.class).insertSplit(user, split);
//					if (count != 1) throw new DatabaseException(String.format("Insert failed; expected 1 row, returned %s", count));
//				}
//			}
//			else if ("delete".equals(action)){
//				final Transaction transaction = new Transaction();
//				transaction.setId(json.getLong("id"));
//				int count = sqlSession.getMapper(Transactions.class).deleteTransaction(user, transaction);
//				if (count != 1) throw new DatabaseException(String.format("Update failed; expected 1 row, returned %s", count));
//			}
//			else {
//				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, user.getTranslation().getString("ACTION_PARAMETER_MUST_BE_SPECIFIED"));
//			}
//			
//			DataUpdater.updateBalances(user, sqlSession, false);
//			
//			sqlSession.commit();
//			final JSONObject result = new JSONObject();
//			result.put("success", true);
//			return new JsonRepresentation(result);
//		}
//		catch (DatabaseException e){
//			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
//		}
//		catch (IOException e){
//			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
//		}
//		catch (CryptoException e){
//			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
//		}
//		catch (JSONException e){
//			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
//		}
//		finally {
//			sqlSession.close();
//		}
//	}
}
