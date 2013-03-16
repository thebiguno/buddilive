package ca.digitalcave.buddi.live.model;

import java.util.List;

public class AccountType {

	private String accountType;
	private String type;
	private List<Account> accounts;
	
	public List<Account> getAccounts() {
		return accounts;
	}
	public void setAccounts(List<Account> accounts) {
		this.accounts = accounts;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getAccountType() {
		return accountType;
	}
	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}
	
	public boolean isDebit(){
		return "D".equals(getType());
	}
	public boolean isDeleted(){
		//return true if all accounts are deleted; return false if there is at least one valid account.
		if (getAccounts() == null) return false;
		for (Account a : getAccounts()) {
			if (!a.isDeleted()) return false;
		}
		return true;
	}
}
