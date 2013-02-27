package ca.digitalcave.buddi.web.model;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import ca.digitalcave.buddi.web.util.FormatUtil;

public class Transaction {
	private Integer id;
	private String uuid;
	private int user_id;
	private String description;
	private String number;
	private boolean deleted;
	private Date created;
	private Date modified;
	
	public Transaction() {
	}
	public Transaction(JSONObject json) throws JSONException {
		this.setId(json.getInt("id"));
		this.setUuid(json.getString("uuid"));
		this.setUserId(json.getInt("userId"));
		this.setDescription(json.getString("description"));
		this.setNumber(json.getString("number"));
		this.setDeleted(json.getBoolean("deleted"));
	}
	
	public JSONObject toJson() throws JSONException {
		final JSONObject result = new JSONObject();
		result.put("id", this.getId());
		result.put("userId", this.getUserId());
		result.put("uuid", this.getUuid());
		result.put("description", this.getDescription());
		result.put("number", this.getNumber());
		result.put("deleted", this.isDeleted());
		result.put("created", FormatUtil.formatDateTime((Date) this.getCreated()));
		result.put("modified", FormatUtil.formatDateTime((Date) this.getModified()));
		return result;
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public int getUserId() {
		return user_id;
	}
	public void setUserId(int userId) {
		this.user_id = userId;
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
}
