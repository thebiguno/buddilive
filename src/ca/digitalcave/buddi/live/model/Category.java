package ca.digitalcave.buddi.live.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.json.JSONException;
import org.json.JSONObject;

import ca.digitalcave.buddi.live.db.Entries;
import ca.digitalcave.buddi.live.model.CategoryPeriod.CategoryPeriods;
import ca.digitalcave.moss.common.DateUtil;

public class Category extends Source {
	private String periodType;
	private Integer parent;
	
	//The following are used in Java, but are not directly mapped to the DB
	private List<Category> children;
	
	private BigDecimal periodBalance;

	private Entry previousEntry;
	private Entry currentEntry;

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
	public BigDecimal getPeriodBalance() {
		return periodBalance;
	}
	public void setPeriodBalance(BigDecimal periodBalance) {
		this.periodBalance = periodBalance;
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
	
	/**
	 * Returns the budgeted amount associated with this category, between the given dates.  If the start and end dates
	 * match the boundaries of a single period, then we just return the amount.  Otherwise, the amount is calculated
	 * based on the percentage of the category in the range.
	 * @param user
	 * @param sql
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws SQLException
	 */
	public BigDecimal getAmount(User user, SqlSession sql, Date startDate, Date endDate) throws SQLException {
		final CategoryPeriods categoryPeriod = CategoryPeriods.valueOf(getPeriodType());
		
		//If the start date and end date are in the same period, then our job is easy: find the entry, 
		// and return the amount * percent ofhow many days were used in the period. 
		if (categoryPeriod.getStartOfBudgetPeriod(startDate).equals(categoryPeriod.getStartOfBudgetPeriod(endDate))){
			final Entry entry = sql.getMapper(Entries.class).selectEntry(user, categoryPeriod.getStartOfBudgetPeriod(startDate), getId());
			if (entry == null) return BigDecimal.ZERO;
			final BigDecimal totalAmount = entry.getAmount();
			final double totalDays = categoryPeriod.getDaysInPeriod(startDate);
			final double daysBetween = DateUtil.getDaysBetween(startDate, endDate, true);
			final BigDecimal result = new BigDecimal(totalAmount.doubleValue() * (daysBetween / totalDays));
			result.setScale(2, RoundingMode.HALF_EVEN);
			return result;
		}
		//If the start date and end date are different, then we need to add each period separately, using the same
		// rules as the simple case above.
		else {
			Date periodStartDate = startDate;
			Date periodEndDate = categoryPeriod.getEndOfBudgetPeriod(endDate);
			BigDecimal total = BigDecimal.ZERO;
			while (categoryPeriod.getEndOfBudgetPeriod(endDate).before(endDate)){
				total = total.add(getAmount(user, sql, periodStartDate, periodEndDate));
				
				//The next start date is the first day in the next period
				periodStartDate = categoryPeriod.getBudgetPeriodOffset(periodStartDate, 1);
				//The next end date is the overall end date, or the end date in the next period, whichever is earlier
				periodEndDate = categoryPeriod.getEndOfBudgetPeriod(periodStartDate).before(endDate) ? categoryPeriod.getEndOfBudgetPeriod(periodStartDate) : endDate;
 			}
			total = total.add(getAmount(user, sql, periodStartDate, periodEndDate));
			
			return total;
		}
	}
	
	public boolean isIncome(){
		return "I".equals(getType());
	}

	public Entry getCurrentEntry() {
		return currentEntry;
	}
	public void setCurrentEntry(Entry currentEntry) {
		this.currentEntry = currentEntry;
	}
	public Entry getPreviousEntry() {
		return previousEntry;
	}
	public void setPreviousEntry(Entry previousEntry) {
		this.previousEntry = previousEntry;
	}
}
