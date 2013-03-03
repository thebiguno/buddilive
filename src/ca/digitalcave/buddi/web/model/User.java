package ca.digitalcave.buddi.web.model;

import java.util.Date;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import ca.digitalcave.buddi.web.util.CryptoUtil;
import ca.digitalcave.buddi.web.util.FormatUtil;

public class User extends org.restlet.security.User {
	private Integer id;
	private String identifier;
	private String credentials;
	private String email;
	private String uuid;
	private boolean premium = false;
	private Date created;
	private Date modified;
	
	public User() {
	}
	public User(JSONObject json) throws JSONException{
		this.setIdentifier(json.getString("identifier").startsWith("SHA1:") ? json.getString("identifier") : CryptoUtil.getSha256Hash(1, new byte[0], json.getString("identifier")));
		this.setCredentials(json.getString("credentials").startsWith("SHA1:") ? json.getString("credentials") : CryptoUtil.getSha256Hash(1, CryptoUtil.getRandomSalt(), json.getString("credentials")));
		this.setUuid(json.has("uuid") ? json.getString("uuid") : UUID.randomUUID().toString());
		this.setEmail(json.has("email") ? json.getString("email") : null);
		this.setPremium(false);

	}
	public JSONObject toJson() throws JSONException {
		final JSONObject result = new JSONObject();
		result.put("id", this.getId());
		result.put("uuid", this.getUuid());
		result.put("identifier", this.getIdentifier());
		result.put("credentials", this.getCredentials());
		result.put("email", this.getEmail());
		result.put("created", FormatUtil.formatDateTime(this.getCreated()));
		result.put("modified", FormatUtil.formatDateTime(this.getModified()));
		
		return result;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
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
	
	public boolean isPremium() {
		return premium;
	}
	public void setPremium(boolean premium) {
		this.premium = premium;
	}
	
	@Override
	public String toString() {
		try {
			return toJson().toString();
		}
		catch (JSONException e){return "Error converting to JSON";}
	}
}
