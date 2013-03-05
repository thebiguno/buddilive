package ca.digitalcave.buddi.web.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class Category extends Source {
	private String periodType;
	private Integer parent;
	
	//The following are used in Java, but are not populated from the DB
	private List<Category> children;

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
		if (this.getChildren() != null){
			for (Category c : getChildren()) {
				result.accumulate("children", c.toJson());
			}
		}
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
	public List<Category> getChildren() {
		return children;
	}
	public void setChildren(List<Category> children) {
		this.children = children;
	}
	
	/**
	 * Creates a hierarchy of categories, with the correct parentage.  The resulting list will
	 * be all categories without parents, with children fields set accordingly.
	 * @param categories
	 * @return
	 */
	public static List<Category> getHierarchy(List<Category> categories){
		final Map<Integer, Category> categoryMap = new HashMap<Integer, Category>();
		final List<Category> result = new ArrayList<Category>();
		final List<Category> remaining = new ArrayList<Category>();
		for (Category category : categories) {
			categoryMap.put(category.getId(), category);
			if (category.getParent() == null) result.add(category);
			else remaining.add(category);
		}
		
		while (remaining.size() > 0){
			final Category category = remaining.remove(0);
			final Category parent = categoryMap.get(category.getParent());
			if (parent != null){
				if (parent.getChildren() == null) parent.setChildren(new ArrayList<Category>());
				parent.getChildren().add(category);
			}
		}
		
		return result;
	}
	
	public boolean isIncome(){
		return "I".equals(getType());
	}

}
