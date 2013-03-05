package ca.digitalcave.buddi.web.resource.gui;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import ca.digitalcave.buddi.web.BuddiApplication;
import ca.digitalcave.buddi.web.db.Sources;
import ca.digitalcave.buddi.web.db.Transactions;
import ca.digitalcave.buddi.web.db.util.ConstraintsChecker;
import ca.digitalcave.buddi.web.db.util.DatabaseException;
import ca.digitalcave.buddi.web.model.Source;
import ca.digitalcave.buddi.web.model.Split;
import ca.digitalcave.buddi.web.model.Transaction;
import ca.digitalcave.buddi.web.model.User;
import ca.digitalcave.buddi.web.util.FormatUtil;

public class TransactionsResource extends ServerResource {

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
			final Source source = new Source();
			try {
				source.setId(Integer.parseInt(getQuery().getFirstValue("source")));
			}
			catch (Throwable e){
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
			}
			final List<Transaction> transactions = sqlSession.getMapper(Transactions.class).selectTransactions(user, source);
			final List<Source> sources = sqlSession.getMapper(Sources.class).selectSources(user);
			final Map<Integer, Source> sourcesMap = new HashMap<Integer, Source>();
			for (Source s : sources) sourcesMap.put(s.getId(), s);
			
			final JSONArray data = new JSONArray();
			for (Transaction t : transactions) {
				final JSONObject transaction = new JSONObject();
				transaction.put("id", t.getId());
				transaction.put("date", FormatUtil.formatDate(t.getDate()));
				transaction.put("description", t.getDescription());
				transaction.put("number", t.getNumber());
				transaction.put("deleted", t.isDeleted());
				transaction.put("amount", FormatUtil.formatCurrency(t.getAmount()));
				transaction.put("from", t.getFrom(sourcesMap));
				transaction.put("to", t.getTo(sourcesMap));
				final JSONArray splits = new JSONArray();
				for (Split s : t.getSplits()) {
					final JSONObject split = new JSONObject();
					split.put("id", s.getId());
					split.put("amount", s.getAmount());
					split.put("fromId", s.getFromSource());
					split.put("toId", s.getToSource());
					split.put("from", s.getFromSourceName());
					split.put("to", s.getToSourceName());
					split.put("memo", s.getMemo());
					splits.put(split);
				}
				transaction.put("splits", splits);
				data.put(transaction);
			}
			
			final JSONObject result = new JSONObject();
			result.put("data", data);
			result.put("success", true);
			return new JsonRepresentation(result);
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
				final Transaction transaction = new Transaction(json);
				ConstraintsChecker.checkInsertTransaction(transaction, user, sqlSession);
				
				int count = sqlSession.getMapper(Transactions.class).insertTransaction(user, transaction);
				if (count != 1) throw new DatabaseException(String.format("Insert failed; expected 1 row, returned %s", count));

				for (Split split : transaction.getSplits()) {
					split.setTransactionId(transaction.getId());
					count = sqlSession.getMapper(Transactions.class).insertSplit(user, split);
					if (count != 1) throw new DatabaseException(String.format("Insert failed; expected 1 row, returned %s", count));
				}
			} 
			else if ("update".equals(action)){
				final Transaction transaction = new Transaction(json);
				ConstraintsChecker.checkUpdateTransaction(transaction, user, sqlSession);
				
				int count = sqlSession.getMapper(Transactions.class).insertTransaction(user, transaction);
				if (count != 1) throw new DatabaseException(String.format("Update failed; expected 1 row, returned %s", count));

				//To update, we delete all splits associated with the given transaction, and re-insert them according to the given data packet.
				count = sqlSession.getMapper(Transactions.class).deleteSplits(user, transaction);
				if (count == 0) throw new DatabaseException("Failed to delete splits; expected 1 or more rows, returned 0");

				for (Split split : transaction.getSplits()) {
					split.setTransactionId(transaction.getId());
					count = sqlSession.getMapper(Transactions.class).insertSplit(user, split);
					if (count != 1) throw new DatabaseException(String.format("Insert failed; expected 1 row, returned %s", count));
				}
			}
			else if ("delete".equals(action)){
				final Transaction transaction = new Transaction();
				transaction.setId(json.getLong("id"));
				int count = sqlSession.getMapper(Transactions.class).deleteTransaction(user, transaction);
				if (count != 1) throw new DatabaseException(String.format("Update failed; expected 1 row, returned %s", count));
			}
			else {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "An action parameter must be specified.");
			}
			
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
		finally {
			sqlSession.close();
		}
	}
}