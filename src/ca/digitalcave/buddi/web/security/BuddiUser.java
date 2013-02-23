package ca.digitalcave.buddi.web.security;

import org.restlet.security.User;

public class BuddiUser extends User {

	private String commonName;
	private String client;
	private String companyName;
	private String telephone;

	public BuddiUser(String identifier, char[] secret) {
		super(identifier, secret);
	}
	
	public String getCommonName() {
		return commonName;
	}
	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}
	public String getClient() {
		return client;
	}
	public void setClient(String client) {
		this.client = client;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public String getTelephone() {
		return telephone;
	}
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
}
