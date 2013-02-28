package ca.digitalcave.buddi.web.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Category extends Source {
	private String periodType;
	private Integer parent;

	public Category() {}
	
	public Category(JSONObject json) throws JSONException {
		super(json);
		this.setPeriodType(json.optString("periodType", null));
		this.setParent(json.optInt("parent") == 0 ? null : json.optInt("parent"));
	}
	
	public JSONObject toJson() throws JSONException {
		JSONObject result = super.toJson();
		result.put("periodType", this.getPeriodType());
		result.put("parent", this.getParent());
		return result;
	}
	
	public String getPeriodType() {
		return periodType;
	}
	public void setPeriodType(String periodType) {
		this.periodType = periodType;
	}
	public Integer getParent() {
		return parent;
	}
	public void setParent(Integer parent) {
		this.parent = parent;
	}
}
