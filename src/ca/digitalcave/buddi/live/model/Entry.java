package ca.digitalcave.buddi.live.model;

import java.math.BigDecimal;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import ca.digitalcave.buddi.live.util.FormatUtil;

public class Entry {
	private Long id;
	private int categoryId;
	private BigDecimal amount;
	private Date date;
	private Date created;
	private Date modified;
	private CategoryPeriod period;
	
	public Entry(){}
	public Entry(JSONObject json) throws JSONException {
		this.setId(json.has("id") ? json.getLong("id") : null);
		this.setCategoryId(json.getInt("categoryId"));	//This field is required
		this.setAmount(FormatUtil.parseCurrency(json.getString("amount")));	//This field is required, but can be zero
		this.setDate(FormatUtil.parseDate(json.getString("date")));	//This field is required
	}
	
	public JSONObject toJson() throws JSONException {
		final JSONObject result = new JSONObject();
		result.put("id", this.getId());
		result.put("categoryId", this.getCategoryId());
		result.put("amount", this.getAmount());
		result.put("date", FormatUtil.formatDate((Date) this.getDate()));
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
	public int getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
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
	public CategoryPeriod getPeriod() {
		return period;
	}
	public void setPeriod(CategoryPeriod period) {
		this.period = period;
	}
	
	@Override
	public String toString() {
		try {
			return toJson().toString();
		}
		catch (JSONException e){return "Error converting to JSON";}
	}
}
