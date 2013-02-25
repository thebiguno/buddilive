package ca.digitalcave.buddi.web.model;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class User implements JsonSerialization {
	private String email;
	private String credentials;
	private String uuid;
	private boolean donated = false;
	private Date created;
	private Date modified;
	
	public User() {
	}
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getCredentials() {
		return credentials;
	}
	public void setCredentials(String credentials) {
		this.credentials = credentials;
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
	
	public boolean isDonated() {
		return donated;
	}
	public void setDonated(boolean donated) {
		this.donated = donated;
	}
	
	@Override
	public JSONObject toJson() throws JSONException {
		final JSONObject result = new JSONObject();
		result.put("id", email);
		result.put("uuid", uuid);
		result.put("credentials", credentials);
		result.put("donated", donated);
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
