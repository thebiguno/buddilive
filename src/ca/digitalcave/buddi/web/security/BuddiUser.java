package ca.digitalcave.buddi.web.security;

import java.util.Date;

import ca.digitalcave.buddi.web.model.User;

public class BuddiUser extends org.restlet.security.User {
	private boolean donated = false;
	private Date created;
	private Date modified;
	
	public BuddiUser(User user) {
		super(user.getEmail(), user.getCredentials());
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
