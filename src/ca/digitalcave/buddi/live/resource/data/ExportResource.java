package ca.digitalcave.buddi.live.resource.data;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.ibatis.session.SqlSession;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ca.digitalcave.buddi.live.BuddiApplication;
import ca.digitalcave.buddi.live.db.Transactions;
import ca.digitalcave.buddi.live.model.Split;
import ca.digitalcave.buddi.live.model.Transaction;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.resource.buddilive.report.ReportHelper;
import ca.digitalcave.buddi.live.util.CryptoUtil;
import ca.digitalcave.buddi.live.util.FormatUtil;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;

public class ExportResource extends ServerResource {

	@Override
	protected void doInit() throws ResourceException {
		getVariants().add(new Variant(MediaType.TEXT_CSV));
	}

	@Override
	protected Representation get(Variant variant) throws ResourceException {
		final BuddiApplication application = (BuddiApplication) getApplication();
		final User user = (User) getRequest().getClientInfo().getUser();
		final Date[] dates = ReportHelper.processInterval(getQuery());
		final Representation result;
		
		if (!user.isPremium()){
			throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED);
		}
		
		if ("csv".equalsIgnoreCase(getQuery().getFirstValue("type"))){
			result = new OutputRepresentation(MediaType.TEXT_CSV) {
				@Override
				public void write(OutputStream os) throws IOException {
					final SqlSession sqlSession = application.getSqlSessionFactory().openSession(true);
					final CSVPrinter csvPrinter = new CSVPrinter(new OutputStreamWriter(os), CSVFormat.EXCEL);
					try {
						csvPrinter.printRecord(new Object[]{"Date", "Description", "Amount", "From", "To"});

						final List<Transaction> transactions = sqlSession.getMapper(Transactions.class).selectTransactions(user, dates[0], dates[1]);
						for (Transaction transaction : transactions) {
							if (transaction.getSplits() != null){
								for (Split split : transaction.getSplits()) {
									csvPrinter.printRecord(
											FormatUtil.formatDate(transaction.getDate(), user),
											CryptoUtil.decryptWrapper(transaction.getDescription(), user),
											CryptoUtil.decryptWrapperBigDecimal(split.getAmount(), user, true).toPlainString(),
											CryptoUtil.decryptWrapper(split.getFromSourceName(), user),
											CryptoUtil.decryptWrapper(split.getToSourceName(), user)
											);
								}
							}
						}
						csvPrinter.flush();
					}
					catch (CryptoException e){

					}
					finally {
						sqlSession.close();
						csvPrinter.close();
					}
				}
			};
			result.setMediaType(MediaType.TEXT_CSV);
			final Disposition disposition = new Disposition(Disposition.TYPE_ATTACHMENT);
			disposition.setFilename("Export (" + FormatUtil.formatDate(new Date(), user) + ").csv");
			result.setDisposition(disposition);
		}
		else {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}
		return result;
	}
}
