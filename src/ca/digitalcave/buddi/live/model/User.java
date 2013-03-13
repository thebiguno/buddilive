package ca.digitalcave.buddi.live.model;

import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import ca.digitalcave.buddi.live.util.CryptoUtil;
import ca.digitalcave.buddi.live.util.FormatUtil;

public class User extends org.restlet.security.User {
	private Integer id;
	private String identifier;	//Hashed, recovered from DB
	private String plaintextIdentifier;	//Not hashed, injected by BuddiVerifier
	private String credentials;
	private String email;
	private String uuid;
	private boolean premium = false;
	private Date created;
	private Date modified;
	private String locale;
	private boolean authenticated = false;
	
	public User() {
	}
	public User(String locale){
		this.locale = locale;
	}
	public User(JSONObject json) throws JSONException{
		if (json.optString("identifier", null) != null)this.setIdentifier(json.getString("identifier").startsWith("SHA1:") ? json.getString("identifier") : CryptoUtil.getSha256Hash(1, new byte[0], json.getString("identifier")));
		if (json.optString("credentials", null) != null) this.setCredentials(json.getString("credentials").startsWith("SHA1:") ? json.getString("credentials") : CryptoUtil.getSha256Hash(1, CryptoUtil.getRandomSalt(), json.getString("credentials")));
		this.setUuid(json.has("uuid") ? json.getString("uuid") : UUID.randomUUID().toString());
		//Prefer the email param, but if that is missing we can fill it in via the identifier if the storeEmail option is set.
		if (json.optString("email", null) != null) this.setEmail(json.getString("email"));
		else if (json.optBoolean("storeEmail", false)) this.setEmail(json.getString("identifier"));
		if (json.optString("locale", null) != null) this.setLocale(json.getString("locale"));
		this.setPremium(false);

	}
	public JSONObject toJson() throws JSONException {
		final JSONObject result = new JSONObject();
		result.put("id", this.getId());
		result.put("uuid", this.getUuid());
		result.put("identifier", this.getIdentifier());
		result.put("credentials", this.getCredentials());
		result.put("email", this.getEmail());
		result.put("locale", this.getLocale());
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
	public String getLocale() {
		return locale;
	}
	public void setLocale(String locale) {
		this.locale = locale;
	}
	public boolean isAuthenticated() {
		return authenticated;
	}
	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}
	public String getPlaintextIdentifier() {
		return plaintextIdentifier;
	}
	public void setPlaintextIdentifier(String plaintextIdentifier) {
		this.plaintextIdentifier = plaintextIdentifier;
	}
	
	public ResourceBundle getTranslation(){
		if (locale == null) return ResourceBundle.getBundle("buddi");
		
		final String[] splitLocale = locale.split("_");
		if (splitLocale.length == 0) return ResourceBundle.getBundle("buddi");
		else if (splitLocale.length == 1) return ResourceBundle.getBundle("buddi", new Locale(splitLocale[0]));
		else if (splitLocale.length == 2) return ResourceBundle.getBundle("buddi", new Locale(splitLocale[0], splitLocale[1]));
		else return ResourceBundle.getBundle("buddi", new Locale(splitLocale[0], splitLocale[1], splitLocale[2]));
	}
	
	@Override
	public String toString() {
		try {
			return toJson().toString();
		}
		catch (JSONException e){return "Error converting to JSON";}
	}
}
