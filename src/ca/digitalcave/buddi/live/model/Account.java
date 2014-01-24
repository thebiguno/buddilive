package ca.digitalcave.buddi.live.model;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import ca.digitalcave.buddi.live.util.FormatUtil;

public class Account extends Source {
	private String accountType;
	private String startBalance;
	private Date startDate;

	private String balance;
	
	public Account() {}
	
	public Account(JSONObject json) throws JSONException {
		super(json);
		this.setAccountType(json.optString("accountType", null));
		this.setStartBalance(FormatUtil.parseCurrency(json.optString("startBalance", null)).toPlainString());
		this.setStartDate(FormatUtil.parseDateInternal(json.optString("startDate", "1900-01-01")));
	}
	
//	public JSONObject toJson() throws JSONException {
//		JSONObject result = super.toJson();
//		result.put("startBalance", this.getStartBalance().toPlainString());
//		result.put("balance", this.getBalance().toPlainString());
//		result.put("startDate", FormatUtil.formatDateTimeInternal((Date) this.getStartDate()));
//		result.put("accountType", this.getAccountType());
//		return result;
//	}
	
	public String getAccountType() {
		return accountType;
	}
	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}
	public String getStartBalance() {
		return startBalance;
	}
	public void setStartBalance(String startBalance) {
		this.startBalance = startBalance;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	public boolean isDebit(){
		return "D".equals(getType());
	}
	public String getBalance() {
		return balance;
	}
	public void setBalance(String balance) {
		this.balance = balance;
	}
}
