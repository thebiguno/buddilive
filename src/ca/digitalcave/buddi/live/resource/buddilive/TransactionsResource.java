package ca.digitalcave.buddi.live.resource.buddilive;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;

import org.apache.commons.lang.mutable.MutableInt;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.SqlSession;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
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

import ca.digitalcave.buddi.live.BuddiApplication;
import ca.digitalcave.buddi.live.db.Sources;
import ca.digitalcave.buddi.live.db.Transactions;
import ca.digitalcave.buddi.live.db.util.ConstraintsChecker;
import ca.digitalcave.buddi.live.db.util.DataUpdater;
import ca.digitalcave.buddi.live.db.util.DatabaseException;
import ca.digitalcave.buddi.live.model.Source;
import ca.digitalcave.buddi.live.model.Split;
import ca.digitalcave.buddi.live.model.Transaction;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.CryptoUtil;
import ca.digitalcave.buddi.live.util.FormatUtil;
import ca.digitalcave.buddi.live.util.LocaleUtil;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;

public class TransactionsResource extends ServerResource {

	@Override
	protected void doInit() throws ResourceException {
		getVariants().add(new Variant(MediaType.APPLICATION_JSON));
	}

	@Override
	protected Representation get(Variant variant) throws ResourceException {
		final BuddiApplication application = (BuddiApplication) getApplication();
		final User user = (User) getRequest().getClientInfo().getUser();

		final WriterRepresentation result = new WriterRepresentation(MediaType.APPLICATION_JSON) {
			public void write(Writer writer) throws IOException {
				final JsonGenerator generator = application.getJsonFactory().createJsonGenerator(writer);
				final SqlSession sqlSession = application.getSqlSessionFactory().openSession(true);
				try {
					final Source source = sqlSession.getMapper(Sources.class).selectSource(user, Integer.parseInt(getQuery().getFirstValue("source")));
					final String search = (getQuery().getFirstValue("search") != null ? getQuery().getFirstValue("search").toLowerCase(user.getLocale()) : null);
					generator.writeStartObject();
					generator.writeBooleanField("success", true);
					generator.writeArrayFieldStart("data");
					final int start = Integer.parseInt(getQuery().getFirstValue("start"));
					final int limit = Integer.parseInt(getQuery().getFirstValue("limit"));
					final MutableInt total = new MutableInt(0);
					final MutableInt count = new MutableInt(0);
					sqlSession.getMapper(Transactions.class).selectTransactions(user, source, new ResultHandler() {
						public void handleResult(ResultContext context) {
							Transaction t = (Transaction) context.getResultObject();
							try {
								final String description = CryptoUtil.decryptWrapper(t.getDescription(), user);
								final String number = CryptoUtil.decryptWrapper(t.getNumber(), user);
								if (search != null){
									//See if the search terms are contained in description, number, or memo.  If so, proceed; otherwise, skip this row.
									if (!description.toLowerCase(user.getLocale()).contains(search)
											&& !number.toLowerCase(user.getLocale()).contains(search)){
										boolean match = false;
										for (Split s : t.getSplits()) {
											System.out.println(search + ": '" + CryptoUtil.decryptWrapper(s.getMemo(), user).toLowerCase(user.getLocale()) + "': " + search.contains(CryptoUtil.decryptWrapper(s.getMemo(), user).toLowerCase(user.getLocale())));
											if (CryptoUtil.decryptWrapper(s.getMemo(), user).toLowerCase(user.getLocale()).contains(search)){
												match = true;
												break;
											}
										}
										if (!match) return;	//If we are searching and it doesn't match, this line is not included in the totals
									}
								}
								
								total.increment();
								if (context.getResultCount() < start || count.intValue() >= limit) return;
								count.increment();
								
								generator.writeStartObject();
								generator.writeNumberField("id", t.getId());
								generator.writeStringField("date", FormatUtil.formatDate(t.getDate(), user));
								generator.writeStringField("dateIso", FormatUtil.formatDateInternal(t.getDate()));
								generator.writeStringField("description", description);
								generator.writeStringField("number", number);
								generator.writeBooleanField("deleted", t.isDeleted());
								generator.writeArrayFieldStart("splits");
								for (Split s : t.getSplits()) {
									generator.writeStartObject();
									generator.writeNumberField("id", s.getId());
									final BigDecimal amount = CryptoUtil.decryptWrapperBigDecimal(s.getAmount(), user, false);
									generator.writeStringField("amount", FormatUtil.formatCurrency(amount, user));
									generator.writeNumberField("amountNumber", amount);
									generator.writeBooleanField("amountInDebitColumn", s.isDebit(source));
									generator.writeStringField("amountStyle", (FormatUtil.isRed(source, user, s) ? FormatUtil.formatRed() : ""));
									generator.writeNumberField("fromId", s.getFromSource());
									generator.writeStringField("from", CryptoUtil.decryptWrapper(s.getFromSourceName(), user));
									generator.writeNumberField("toId", s.getToSource());
									generator.writeStringField("to", CryptoUtil.decryptWrapper(s.getToSourceName(), user));
									generator.writeBooleanField("debit", s.isDebit(source));
									final BigDecimal balance = CryptoUtil.decryptWrapperBigDecimal(s.getFromSource() == source.getId() ? s.getFromBalance() : s.getToBalance(), user, true);
									generator.writeStringField("balance", FormatUtil.formatCurrency(balance, user, source));
									generator.writeStringField("balanceStyle", (FormatUtil.isRed(source, balance) ? FormatUtil.formatRed() : ""));
									generator.writeStringField("memo", CryptoUtil.decryptWrapper(s.getMemo(), user));
									generator.writeEndObject();
								}
								generator.writeEndArray();
								generator.writeEndObject();
							}
							catch (JsonGenerationException e){
								e.printStackTrace();
								throw new RuntimeException(e);
							}
							catch (IOException e){
								throw new RuntimeException(e);
							}
							catch (CryptoException e){
								throw new RuntimeException(e);
							}
						}
					});
					generator.writeEndArray();
					generator.writeNumberField("total", total.intValue());
					generator.writeEndObject();
					generator.flush();
				}
				catch (Throwable e){
					e.printStackTrace();
					sqlSession.close();
				}
				finally {
					sqlSession.close();
				}
			}
		};

		return result;
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

				int count = sqlSession.getMapper(Transactions.class).updateTransaction(user, transaction);
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
