package ca.digitalcave.buddi.web.model;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import ca.digitalcave.buddi.web.util.FormatUtil;

public class Split {
	private Integer id;
	private long parent_transaction;
	private int user_id;
	private long amount;
	private int from_source;
	private int to_source;
	private String memo;
	private boolean deleted;
	private Date created;
	private Date modified;
	
	public Split() {
	}
	public Split(JSONObject json) throws JSONException {
		this.setId(json.getInt("id"));
		this.setParentTransaction(json.getLong("parentTransaction"));
		this.setUserId(json.getInt("userId"));
		this.setAmount(json.getLong("amount"));
		this.setFromSource(json.getInt("fromSource"));
		this.setToSource(json.getInt("toSource"));
		this.setMemo(json.getString("memo"));
		this.setDeleted(json.getBoolean("deleted"));
	}
	
	public JSONObject toJson() throws JSONException {
		final JSONObject result = new JSONObject();
		result.put("id", this.getId());
		result.put("parentTransaction", this.getParentTransaction());
		result.put("userId", this.getUserId());
		result.put("amount", this.getAmount());
		result.put("fromSource", this.getFromSource());
		result.put("toSource", this.getToSource());
		result.put("memo", this.getMemo());
		result.put("deleted", this.isDeleted());
		result.put("created", FormatUtil.formatDateTime((Date) this.getCreated()));
		result.put("modified", FormatUtil.formatDateTime((Date) this.getModified()));
		return result;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public long getParentTransaction() {
		return parent_transaction;
	}
	public void setParentTransaction(long parentTransaction) {
		this.parent_transaction = parentTransaction;
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
	public boolean isDeleted() {
		return deleted;
	}
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
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
