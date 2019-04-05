package ca.digitalcave.buddi.live.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import ca.digitalcave.buddi.live.util.FormatUtil;

public class ScheduledTransaction {
	private Long id;
	private String uuid;
	private int userId;
	private String description;
	private String number;
	private String scheduleName;
	private int scheduleDay;
	private int scheduleWeek;
	private int scheduleMonth;
	private String frequencyType;
	private Date startDate;
	private Date endDate;
	private Date lastCreatedDate;
	private String message;
	private Date created;
	private Date modified;
	private List<Split> splits = new ArrayList<Split>();

	public static enum ScheduleFrequency {
		SCHEDULE_FREQUENCY_MONTHLY_BY_DATE,
		SCHEDULE_FREQUENCY_MONTHLY_BY_DAY_OF_WEEK,
		SCHEDULE_FREQUENCY_WEEKLY,
		SCHEDULE_FREQUENCY_BIWEEKLY,
		SCHEDULE_FREQUENCY_EVERY_DAY,
		SCHEDULE_FREQUENCY_EVERY_X_DAYS,
		SCHEDULE_FREQUENCY_EVERY_WEEKDAY,
		SCHEDULE_FREQUENCY_MULTIPLE_WEEKS_EVERY_MONTH,
		SCHEDULE_FREQUENCY_MULTIPLE_MONTHS_EVERY_YEAR,
	}

	public ScheduledTransaction() {
	}
	public ScheduledTransaction(JSONObject json) throws JSONException {
		this.setId(StringUtils.isNotBlank(json.optString("id", null)) ? Long.parseLong(json.getString("id")) : null);
		this.setUuid(json.has("uuid") ? json.getString("uuid") : UUID.randomUUID().toString());
		this.setScheduleName(json.getString("name"));
		this.setScheduleDay(json.getInt("scheduleDay"));
		this.setScheduleWeek(json.getInt("scheduleWeek"));
		this.setScheduleMonth(json.getInt("scheduleMonth"));
		this.setFrequencyType(json.getString("repeat"));
		this.setStartDate(FormatUtil.parseDateInternal(json.getString("start")));
		this.setEndDate(FormatUtil.parseDateInternal(json.getString("end")));
		this.setLastCreatedDate(FormatUtil.parseDateInternal(json.getString("lastCreatedDate")));
		this.setMessage(json.optString("message", null));

		final JSONObject transaction = json.getJSONObject("transaction");
		this.setDescription(transaction.getString("description"));
		this.setNumber(transaction.has("number") ? transaction.getString("number") : null);
		for (int i = 0; i < transaction.getJSONArray("splits").length(); i++){
			splits.add(new Split(transaction.getJSONArray("splits").getJSONObject(i)));
		}
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getScheduleName() {
		return scheduleName;
	}
	public void setScheduleName(String scheduleName) {
		this.scheduleName = scheduleName;
	}
	public int getScheduleDay() {
		return scheduleDay;
	}
	public void setScheduleDay(int scheduleDay) {
		this.scheduleDay = scheduleDay;
	}
	public int getScheduleWeek() {
		return scheduleWeek;
	}
	public void setScheduleWeek(int scheduleWeek) {
		this.scheduleWeek = scheduleWeek;
	}
	public int getScheduleMonth() {
		return scheduleMonth;
	}
	public void setScheduleMonth(int scheduleMonth) {
		this.scheduleMonth = scheduleMonth;
	}
	public String getFrequencyType() {
		return frequencyType;
	}
	public void setFrequencyType(String frequencyType) {
		this.frequencyType = frequencyType;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public Date getLastCreatedDate() {
		return lastCreatedDate;
	}
	public void setLastCreatedDate(Date lastCreatedDate) {
		this.lastCreatedDate = lastCreatedDate;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public Date getModified() {
		return modified;
	}
	public void setModified(Date modified) {
		this.modified = modified;
	}
	public List<Split> getSplits() {
		return splits;
	}
	public void setSplits(List<Split> splits) {
		this.splits = splits;
	}
}
