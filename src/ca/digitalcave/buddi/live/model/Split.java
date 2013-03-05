package ca.digitalcave.buddi.live.model;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import ca.digitalcave.buddi.live.util.FormatUtil;

public class Split {
	private Long id;
	private Long transactionId;
	private int userId;
	private long amount;
	private int fromSource;
	private int toSource;
	private String memo;
	private Date created;
	private Date modified;
	
	private String fromSourceName;
	private String toSourceName;
	
	public Split() {
	}
	public Split(JSONObject json) throws JSONException {
		this.setId(json.has("id") ? json.getLong("id") : null);
		this.setTransactionId(json.has("transactionId") ? json.getLong("transactionId") : null);
		this.setAmount(FormatUtil.parseCurrency(json.getString("amount")));
		this.setFromSource(json.getInt("fromSource"));
		this.setToSource(json.getInt("toSource"));
		this.setMemo(json.optString("memo", null));
	}
	
	public JSONObject toJson() throws JSONException {
		final JSONObject result = new JSONObject();
		result.put("id", this.getId());
		result.put("transactionId", this.getTransactionId());
		result.put("userId", this.getUserId());
		result.put("amount", FormatUtil.formatCurrency(this.getAmount()));
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
	public long getAmount() {
		return amount;
	}
	public void setAmount(long amount) {
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
}
