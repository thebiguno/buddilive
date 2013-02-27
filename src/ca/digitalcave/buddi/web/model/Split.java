package ca.digitalcave.buddi.web.model;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import ca.digitalcave.buddi.web.util.FormatUtil;

public class Split {
	private Long id;
	private Long transaction_id;
	private int user_id;
	private long amount;
	private int from_source;
	private int to_source;
	private String memo;
	private Date created;
	private Date modified;
	
	public Split() {
	}
	public Split(JSONObject json) throws JSONException {
		this.setId(json.has("id") ? json.getLong("id") : null);
		this.setTransactionId(json.has("transactionId") ? json.getLong("transactionId") : null);
		this.setAmount(json.getLong("amount"));
		this.setFromSource(json.getInt("fromSource"));
		this.setToSource(json.getInt("toSource"));
		this.setMemo(json.has("memo") ? json.getString("memo") : null);
	}
	
	public JSONObject toJson() throws JSONException {
		final JSONObject result = new JSONObject();
		result.put("id", this.getId());
		result.put("transactionId", this.getTransactionId());
		result.put("userId", this.getUserId());
		result.put("amount", this.getAmount());
		result.put("fromSource", this.getFromSource());
		result.put("toSource", this.getToSource());
		result.put("memo", this.getMemo());
		result.put("created", FormatUtil.formatDateTime((Date) this.getCreated()));
		result.put("modified", FormatUtil.formatDateTime((Date) this.getModified()));
		return result;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getTransactionId() {
		return transaction_id;
	}
	public void setTransactionId(Long transactionId) {
		this.transaction_id = transactionId;
	}
	public int getUserId() {
		return user_id;
	}
	public void setUserId(int userId) {
		this.user_id = userId;
	}
	public long getAmount() {
		return amount;
	}
	public void setAmount(long amount) {
		this.amount = amount;
	}
	public int getFromSource() {
		return from_source;
	}
	public void setFromSource(int fromSource) {
		this.from_source = fromSource;
	}
	public int getToSource() {
		return to_source;
	}
	public void setToSource(int toSource) {
		this.to_source = toSource;
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
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
}
