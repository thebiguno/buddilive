package ca.digitalcave.buddi.web.model;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class AccountType implements JsonSerialization {

	private long id;
	private String uuid;
	private String name;
	private boolean credit;
	private Date created;
	private Date modified;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isCredit() {
		return credit;
	}
	public void setCredit(boolean credit) {
		this.credit = credit;
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
	
	@Override
	public JSONObject toJson() throws JSONException {
		final JSONObject result = new JSONObject();
		result.put("id", id);
		result.put("uuid", uuid);
		result.put("name", name);
		result.put("credit", credit);
		result.put("created", created);
		result.put("modified", modified);
		return result;
	}
	
	@Override
	public Object fromJson(JSONObject serialized) throws JSONException {
		// TODO Auto-generated method stub
		return null;
	}
}
