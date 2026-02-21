package ca.digitalcave.buddi.live.resource.buddilive;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.ibatis.session.SqlSession;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.fasterxml.jackson.core.JsonGenerator;

import ca.digitalcave.buddi.live.BuddiApplication;
import ca.digitalcave.buddi.live.db.Sources;
import ca.digitalcave.buddi.live.db.Transactions;
import ca.digitalcave.buddi.live.db.util.ConstraintsChecker;
import ca.digitalcave.buddi.live.db.util.DatabaseException;
import ca.digitalcave.buddi.live.model.Account;
import ca.digitalcave.buddi.live.model.Split;
import ca.digitalcave.buddi.live.model.Transaction;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.CryptoUtil;
import ca.digitalcave.buddi.live.util.LocaleUtil;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;

public class DescriptionsResource extends ServerResource {
	private static final int DESCRIPTION_LOOKBACK_MONTHS = 18;

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
			final Calendar lookback = Calendar.getInstance();
			lookback.add(Calendar.MONTH, -DESCRIPTION_LOOKBACK_MONTHS);
			final Date minDate = lookback.getTime();

			final Map<String, Transaction> mostRecentByDescription = new LinkedHashMap<String, Transaction>();
			final List<Transaction> transactions = sqlSession.getMapper(Transactions.class).selectDescriptions(user, minDate);
				for (Transaction transaction : transactions) {
					final String description = CryptoUtil.decryptWrapper(transaction.getDescription(), user);
					if (mostRecentByDescription.get(description) == null){
						mostRecentByDescription.put(description, transaction);
					}
				}
			final Map<String, Transaction> transactionsByDescription = new TreeMap<String, Transaction>(mostRecentByDescription);
			
			return new WriterRepresentation(MediaType.APPLICATION_JSON) {
				@Override
				public void write(Writer writer) throws IOException {
					final JsonGenerator generator = application.getJsonFactory().createGenerator(writer);
					try {
						generator.writeStartObject();
						generator.writeBooleanField("success", true);
						generator.writeArrayFieldStart("data");
						for (String description : transactionsByDescription.keySet()) {
							final Transaction t = transactionsByDescription.get(description);
							generator.writeStartObject();
							generator.writeStringField("value", description);
							generator.writeObjectFieldStart("transaction");
							generator.writeStringField("description", description);
							generator.writeStringField("number", CryptoUtil.decryptWrapper(t.getNumber(), user));
							generator.writeNumberField("dateEpoch", t.getDate() == null ? 0L : t.getDate().getTime());
							generator.writeArrayFieldStart("splits");
							for (Split s : t.getSplits() != null ? t.getSplits() : new ArrayList<Split>()) {
								generator.writeStartObject();
								final BigDecimal amount = CryptoUtil.decryptWrapperBigDecimal(s.getAmount(), user, false);
								generator.writeStringField("amount", amount.toPlainString());
								generator.writeNumberField("amountNumber", amount);
								generator.writeNumberField("fromId", s.getFromSource());
								generator.writeNumberField("toId", s.getToSource());
								generator.writeStringField("fromType", s.getFromType());
								generator.writeStringField("toType", s.getToType());
								generator.writeEndObject();
							}
							generator.writeEndArray();	//splits
							generator.writeEndObject();	//transaction
							generator.writeEndObject();	//array entry object
						}
						generator.writeEndArray();	//data
						generator.writeEndObject();	//entire object
						generator.flush();
					}
					catch (CryptoException e){
						throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
					}
				}
			};
		}
		catch (CryptoException e){
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
				account.setDeleted("delete".equals(action));
				int count = sqlSession.getMapper(Sources.class).updateSourceDeleted(user, account);
				if (count != 1) throw new DatabaseException(String.format("Delete / undelete failed; expected 1 row, returned %s", count));
			}
			else if ("update".equals(action)){
				ConstraintsChecker.checkUpdateAccount(account, user, sqlSession);
				int count = sqlSession.getMapper(Sources.class).updateAccount(user, account);
				if (count != 1) throw new DatabaseException(String.format("Update failed; expected 1 row, returned %s", count));
			}
			else {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, LocaleUtil.getTranslation(getRequest()).getString("ACTION_PARAMETER_MUST_BE_SPECIFIED"));
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
