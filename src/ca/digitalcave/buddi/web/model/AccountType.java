package ca.digitalcave.buddi.web.model;

import java.util.List;

public class AccountType {

	private String type;
	private List<Source> accounts;
	
	public List<Source> getAccounts() {
		return accounts;
	}
	public void setAccounts(List<Source> accounts) {
		this.accounts = accounts;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
