package ca.digitalcave.buddi.live.resource.buddilive.report;

import java.util.Date;

import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import ca.digitalcave.buddi.live.model.report.Interval;
import ca.digitalcave.buddi.live.util.FormatUtil;

public class ReportHelper {

	public static Date[] processInterval(Form query) throws ResourceException {
		final Interval interval = Interval.valueOf(query.getFirstValue("interval"));
		if (interval == null) throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "An interval parameter is required.");
		if (interval != Interval.PLUGIN_FILTER_OTHER){
			return new Date[]{interval.getStartDate(), interval.getEndDate()};
		}
		else {
			final Date startDate = FormatUtil.parseDateInternal(query.getFirstValue("startDate"));
			final Date endDate = FormatUtil.parseDateInternal(query.getFirstValue("endDate"));
			if (startDate == null || endDate == null) throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "For PLUGIN_FILTER_OTHER intervals, startDate and endDate parameters are required.");
			return new Date[]{startDate, endDate};
		}
	}
}
