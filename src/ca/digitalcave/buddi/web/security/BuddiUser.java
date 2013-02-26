package ca.digitalcave.buddi.web.security;

import java.util.Date;

import ca.digitalcave.buddi.web.model.User;

public class BuddiUser extends org.restlet.security.User {
	private int id;
	private boolean donated = false;
	private Date created;
	private Date modified;
	
	public BuddiUser(User user) {
		super(user.getIdentifier(), user.getCredentials());
		this.setId(user.getId());
		this.setDonated(user.isPremium());
		this.setEmail(user.getEmail());
		this.setModified(user.getModified());
		this.setCreated(user.getCreated());
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
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
}
