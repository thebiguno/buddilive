package ca.digitalcave.buddi.web.resource.gui;

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
					split.put("amount", s.getAmount() / 100.0);
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
}
