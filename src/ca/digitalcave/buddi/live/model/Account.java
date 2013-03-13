package ca.digitalcave.buddi.live.model;

import java.math.BigDecimal;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import ca.digitalcave.buddi.live.util.FormatUtil;

public class Account extends Source {
	private String accountType;
	private BigDecimal startBalance;
	private Date startDate;

	private BigDecimal balance;
	
	public Account() {}
	
	public Account(JSONObject json) throws JSONException {
		super(json);
		this.setAccountType(json.optString("accountType", null));
		this.setStartBalance(FormatUtil.parseCurrency(json.optString("startBalance", null)));
		this.setStartDate(FormatUtil.parseDate(json.optString("startDate", "1900-01-01")));
	}
	
	public JSONObject toJson() throws JSONException {
		JSONObject result = super.toJson();
		result.put("startBalance", FormatUtil.formatCurrency(this.getStartBalance()));
		result.put("balance", FormatUtil.formatCurrency(this.getBalance()));
		result.put("startDate", FormatUtil.formatDateTime((Date) this.getStartDate()));
		result.put("accountType", this.getAccountType());
		return result;
	}
	
	public String getAccountType() {
		return accountType;
	}
	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}
	public BigDecimal getStartBalance() {
		return startBalance;
	}
	public void setStartBalance(BigDecimal startBalance) {
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
	public BigDecimal getBalance() {
		return balance;
	}
	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}
}
