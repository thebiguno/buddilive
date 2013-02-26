package ca.digitalcave.buddi.web.model;

import java.util.Date;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import ca.digitalcave.buddi.web.util.FormatUtil;

public class Source {
	private int id;
	private int userId;
	private String uuid;
	private String name;
	private Date startDate;
	private boolean deleted;
	private Date created;
	private Date modified;
	private String type;
	private Long startBalance;
	private String periodType;
	private Integer parent;
	
	public Source() {
	}
	public Source(JSONObject json) throws JSONException {
		this.setUserId(json.getInt("userId"));
		this.setUuid(json.has("uuid") ? json.getString("uuid") : UUID.randomUUID().toString());
		this.setName(json.getString("name"));
		this.setStartDate(json.has("startDate") ? FormatUtil.parseDate(json.getString("startDate")) : FormatUtil.parseDate("1900-01-01"));
		this.setDeleted(json.has("delete") ? json.getBoolean("deleted") : false);
		this.setType(json.getString("type"));
		this.setStartBalance(json.has("startBalance") ? json.getLong("startBalance") : null);
		this.setPeriodType(json.has("periodType") ? json.getString("periodType") : null);
		this.setParent(json.has("parent") ? json.getInt("parent") : null);

	}
	
	public JSONObject toJson() throws JSONException {
		final JSONObject result = new JSONObject();
		result.put("id", this.getId());
		result.put("userId", this.getUserId());
		result.put("uuid", this.getUuid());
		result.put("name", this.getName());
		result.put("startDate", FormatUtil.formatDateTime((Date) this.getStartDate()));
		result.put("deleted", this.isDeleted());
		result.put("type", this.getType());
		result.put("created", FormatUtil.formatDateTime((Date) this.getCreated()));
		result.put("modified", FormatUtil.formatDateTime((Date) this.getModified()));
		result.put("startBalance", this.getStartBalance());
		result.put("periodType", this.getPeriodType());
		result.put("parent", this.getParent());
		return result;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public boolean isDeleted() {
		return deleted;
	}
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
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
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Long getStartBalance() {
		return startBalance;
	}
	public void setStartBalance(Long startBalance) {
		this.startBalance = startBalance;
	}
	public String getPeriodType() {
		return periodType;
	}
	public void setPeriodType(String periodType) {
		this.periodType = periodType;
	}
	public Integer getParent() {
		return parent;
	}
	public void setParent(Integer parent) {
		this.parent = parent;
	}
}
