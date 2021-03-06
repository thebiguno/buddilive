package ca.digitalcave.buddi.live.model;

import java.math.BigDecimal;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import ca.digitalcave.buddi.live.util.FormatUtil;

public class Split {
	private Long id;
	private Long transactionId;
	private int userId;
	private String amount;
	private int fromSource;
	private int toSource;
	private String fromType;
	private String toType;
	private String memo;
	private Date created;
	private Date modified;
	private String fromBalance;
	private String toBalance;

	private String fromSourceName;
	private String toSourceName;
	
	public Split() {
	}
	public Split(JSONObject json) throws JSONException {
		this.setId(json.has("id") ? json.getLong("id") : null);
		this.setTransactionId(json.has("transactionId") ? json.getLong("transactionId") : null);
		this.setAmount(FormatUtil.parseCurrency(json.getString("amount")).toPlainString());
		this.setFromSource(json.getInt("fromId"));
		this.setToSource(json.getInt("toId"));
		this.setMemo(json.optString("memo", null));
	}
	
	public JSONObject toJson() throws JSONException {
		final JSONObject result = new JSONObject();
		result.put("id", this.getId());
		result.put("transactionId", this.getTransactionId());
		result.put("userId", this.getUserId());
		result.put("amount", new BigDecimal(this.getAmount()).toPlainString());
		result.put("fromId", this.getFromSource());
		result.put("toId", this.getToSource());
		result.put("memo", this.getMemo());
		result.put("created", FormatUtil.formatDateTimeInternal((Date) this.getCreated()));
		result.put("modified", FormatUtil.formatDateTimeInternal((Date) this.getModified()));
		return result;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(Long transactionId) {
		this.transactionId = transactionId;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public int getFromSource() {
		return fromSource;
	}
	public void setFromSource(int fromSource) {
		this.fromSource = fromSource;
	}
	public int getToSource() {
		return toSource;
	}
	public void setToSource(int toSource) {
		this.toSource = toSource;
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
	public String getFromBalance() {
		return fromBalance;
	}
	public void setFromBalance(String fromBalance) {
		this.fromBalance = fromBalance;
	}
	public String getToBalance() {
		return toBalance;
	}
	public void setToBalance(String toBalance) {
		this.toBalance = toBalance;
	}
	public String getFromType() {
		return fromType;
	}
	public void setFromType(String fromType) {
		this.fromType = fromType;
	}
	public String getToType() {
		return toType;
	}
	public void setToType(String toType) {
		this.toType = toType;
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
	
//	public boolean isInflow(){
//		if ("I".equals(getFromType())){
//			return this.getAmount().compareTo(BigDecimal.ZERO) >= 0;
//		}
//		if ("E".equals(getToType())){
//			return this.getAmount().compareTo(BigDecimal.ZERO) < 0;
//		}
//
//		//If neither sources are BudgetCategory, this is not an inflow.
//		return false;
//	}
	
	@Override
	public String toString() {
		try {
			return toJson().toString();
		}
		catch (JSONException e){return "Error converting to JSON";}
	}
	
	public String getFromSourceName() {
		return fromSourceName;
	}
	public String getToSourceName() {
		return toSourceName;
	}
	
	/**
	 * Should the amount appear on the debit or credit side in the display?
	 * @param source
	 * @return
	 */
	public boolean isDebit(Source source){
		return (this.getFromSource() == source.getId() && "D".equals(source.getType())) 
				|| (this.getFromSource() == source.getId() && "C".equals(source.getType()));
	}
}
