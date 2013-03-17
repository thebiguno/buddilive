package ca.digitalcave.buddi.live.model;

import java.util.Date;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import ca.digitalcave.buddi.live.util.FormatUtil;

public class Source {
	private Integer id;
	private int userId;
	private String uuid;
	private String name;
	private boolean deleted;
	private Date created;
	private Date modified;
	private String type;
	
	public Source() {}
	
	public Source(JSONObject json) throws JSONException {
		this.setId(json.has("id") ? json.getInt("id") : null);
		this.setUuid(json.optString("uuid", UUID.randomUUID().toString()));
		this.setName(json.optString("name", null));
		this.setDeleted(json.optBoolean("deleted", false));
		this.setType(json.optString("type", null));
	}
	
	public JSONObject toJson() throws JSONException {
		final JSONObject result = new JSONObject();
		result.put("id", this.getId());
		result.put("userId", this.getUserId());
		result.put("uuid", this.getUuid());
		result.put("name", this.getName());
		result.put("deleted", this.isDeleted());
		result.put("type", this.getType());
		result.put("created", FormatUtil.formatDateTimeInternal((Date) this.getCreated()));
		result.put("modified", FormatUtil.formatDateTimeInternal((Date) this.getModified()));
		return result;
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
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
	
	@Override
	public String toString() {
		try {
			return toJson().toString();
		}
		catch (JSONException e){return "Error converting to JSON";}
	}
	
	//Convenience methods
	public boolean isAccount(){
		return "D".equals(getType()) || "C".equals(getType());
	}
}
