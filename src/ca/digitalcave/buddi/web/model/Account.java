package ca.digitalcave.buddi.web.model;

import org.json.JSONException;
import org.json.JSONObject;

import ca.digitalcave.buddi.web.util.FormatUtil;

public class Account extends Source {
	private String accountType;
	private Long startBalance;

	//The following are used for temporary results from the DB, but are not persisted.
	private Long balance;
	
	public Account() {}
	
	public Account(JSONObject json) throws JSONException {
		super(json);
		this.setAccountType(json.optString("accountType", null));
		this.setStartBalance(json.optLong("startBalance") == 0 ? null : json.optLong("startBalance"));
	}
	
	public JSONObject toJson() throws JSONException {
		JSONObject result = super.toJson();
		result.put("startBalance", this.getStartBalance());
		result.put("balance", FormatUtil.formatCurrency(this.getBalance()));
		result.put("accountType", this.getAccountType());
		return result;
	}
	
	public String getAccountType() {
		return accountType;
	}
	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}
	public Long getStartBalance() {
		return startBalance;
	}
	public void setStartBalance(Long startBalance) {
		this.startBalance = startBalance;
	}
	public Long getBalance() {
		return balance;
	}
	
	public boolean isDebit(){
		return "D".equals(getType());
	}
}
