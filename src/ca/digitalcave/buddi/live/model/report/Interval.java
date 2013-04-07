package ca.digitalcave.buddi.live.model.report;

import java.util.Date;

import ca.digitalcave.buddi.live.model.CategoryPeriod.CategoryPeriods;
import ca.digitalcave.moss.common.DateUtil;

public enum Interval {
	PLUGIN_FILTER_THIS_WEEK,
	PLUGIN_FILTER_LAST_WEEK,
	PLUGIN_FILTER_THIS_SEMI_MONTH,
	PLUGIN_FILTER_LAST_SEMI_MONTH,
	PLUGIN_FILTER_THIS_MONTH,
	PLUGIN_FILTER_LAST_MONTH,
	PLUGIN_FILTER_THIS_QUARTER,
	PLUGIN_FILTER_LAST_QUARTER,
	PLUGIN_FILTER_THIS_YEAR,
	PLUGIN_FILTER_THIS_YEAR_TO_DATE,
	PLUGIN_FILTER_LAST_YEAR,
	PLUGIN_FILTER_ALL_TIME,
	PLUGIN_FILTER_OTHER;
	
	public Date getStartDate() {
		switch(this){
		case PLUGIN_FILTER_THIS_WEEK: return DateUtil.getStartOfWeek(new Date());
		case PLUGIN_FILTER_LAST_WEEK: return DateUtil.getStartOfWeek(DateUtil.addDays(new Date(), -7));
		case PLUGIN_FILTER_THIS_SEMI_MONTH: return CategoryPeriods.SEMI_MONTH.getStartOfBudgetPeriod(new Date());
		case PLUGIN_FILTER_LAST_SEMI_MONTH: return CategoryPeriods.SEMI_MONTH.getStartOfBudgetPeriod(CategoryPeriods.SEMI_MONTH.getBudgetPeriodOffset(new Date(), -1));
		case PLUGIN_FILTER_THIS_MONTH: return DateUtil.getStartOfMonth(new Date());
		case PLUGIN_FILTER_LAST_MONTH: return DateUtil.getStartOfMonth(DateUtil.addMonths(new Date(), -1));
		case PLUGIN_FILTER_THIS_QUARTER: return DateUtil.getStartOfQuarter(new Date());
		case PLUGIN_FILTER_LAST_QUARTER: return DateUtil.addQuarters(DateUtil.getStartOfQuarter(new Date()), -1);
		case PLUGIN_FILTER_THIS_YEAR: return DateUtil.getStartOfYear(new Date());
		case PLUGIN_FILTER_THIS_YEAR_TO_DATE: return DateUtil.getStartOfYear(new Date());
		case PLUGIN_FILTER_LAST_YEAR: return DateUtil.addYears(DateUtil.getStartOfYear(new Date()), -1);
		case PLUGIN_FILTER_ALL_TIME: return DateUtil.getDate(1900);
		default: throw new RuntimeException("Invalid interval enum " + this);
		}
	}
	
	public Date getEndDate() {
		switch(this){
		case PLUGIN_FILTER_THIS_WEEK: return DateUtil.getEndOfWeek(new Date());
		case PLUGIN_FILTER_LAST_WEEK: return DateUtil.getEndOfWeek(DateUtil.addDays(new Date(), -7));
		case PLUGIN_FILTER_THIS_SEMI_MONTH: return CategoryPeriods.SEMI_MONTH.getEndOfBudgetPeriod(new Date());
		case PLUGIN_FILTER_LAST_SEMI_MONTH: return CategoryPeriods.SEMI_MONTH.getEndOfBudgetPeriod(CategoryPeriods.SEMI_MONTH.getBudgetPeriodOffset(new Date(), -1));
		case PLUGIN_FILTER_THIS_MONTH: return DateUtil.getEndOfMonth(new Date());
		case PLUGIN_FILTER_LAST_MONTH: return DateUtil.getEndOfMonth(DateUtil.addMonths(new Date(), -1));
		case PLUGIN_FILTER_THIS_QUARTER: return DateUtil.getEndOfQuarter(new Date());
		case PLUGIN_FILTER_LAST_QUARTER: return DateUtil.addQuarters(DateUtil.getEndOfQuarter(new Date()), -1);
		case PLUGIN_FILTER_THIS_YEAR: return DateUtil.getEndOfYear(new Date());
		case PLUGIN_FILTER_THIS_YEAR_TO_DATE: return DateUtil.getEndOfDay(new Date());
		case PLUGIN_FILTER_LAST_YEAR: return DateUtil.addYears(DateUtil.getEndOfYear(new Date()), -1);
		case PLUGIN_FILTER_ALL_TIME: return DateUtil.getEndOfMonth(new Date());
		default: throw new RuntimeException("Invalid interval enum " + this);
		}
	}
}
