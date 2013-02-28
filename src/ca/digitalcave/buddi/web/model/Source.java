package ca.digitalcave.buddi.web.model;

import java.util.Date;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import ca.digitalcave.buddi.web.util.FormatUtil;

public class Source {
	private Integer id;
	private int user_id;
	private String uuid;
	private String name;
	private Date start_date;
	private boolean deleted;
	private Date created;
	private Date modified;
	private String type;
	private String account_type;
	private Long start_balance;
	private String periodType;
	private Integer parent;
	
	//The following are used for temporary results from the DB, but are not persisted.
	private Long balance;
	
	public Source() {
	}
	public Source(JSONObject json) throws JSONException {
		this.setId(json.has("id") ? json.getInt("id") : null);
		this.setUuid(json.optString("uuid", UUID.randomUUID().toString()));
		this.setName(json.optString("name", null));
		this.setStartDate(FormatUtil.parseDate(json.optString("startDate", "1900-01-01")));
		this.setDeleted(json.optBoolean("deleted", false));
		this.setType(json.optString("type", null));
		this.setAccountType(json.optString("accountType", null));
		this.setStartBalance(json.optLong("startBalance") == 0 ? null : json.optLong("startBalance"));
		this.setPeriodType(json.optString("periodType", null));
		this.setParent(json.optInt("parent") == 0 ? null : json.optInt("parent"));
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
		result.put("balance", this.getBalance());
		result.put("accountType", this.getAccountType());
		result.put("periodType", this.getPeriodType());
		result.put("parent", this.getParent());
		return result;
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public int getUserId() {
		return user_id;
	}
	public void setUserId(int userId) {
		this.user_id = userId;
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
		return start_date;
	}
	public void setStartDate(Date startDate) {
		this.start_date = startDate;
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
	public String getAccountType() {
		return account_type;
	}
	public void setAccountType(String accountType) {
		this.account_type = accountType;
	}
	public Long getStartBalance() {
		return start_balance;
	}
	public void setStartBalance(Long startBalance) {
		this.start_balance = startBalance;
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
	
	//Convenience methods
	public boolean isAccount(){
		return "D".equals(getType()) || "C".equals(getType());
	}
	public Long getBalance() {
		return balance;
	}
	public void setBalance(Long balance) {
		this.balance = balance;
	}
}
