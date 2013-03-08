package ca.digitalcave.buddi.live.model;

import java.util.Calendar;
import java.util.Date;

import ca.digitalcave.moss.common.DateUtil;

public class CategoryPeriod {

	private final CategoryPeriods categoryPeriods;
	private final Date date;
	
	public CategoryPeriod(CategoryPeriods categoryPeriods, Date date, int offset) {
		this.categoryPeriods = categoryPeriods;
		this.date = categoryPeriods.getBudgetPeriodOffset(date == null ? new Date() : date, offset);
	}
	
	public Date getCurrentPeriodStartDate(){
		return categoryPeriods.getStartOfBudgetPeriod(date);
	}
	public Date getCurrentPeriodEndDate(){
		return categoryPeriods.getEndOfBudgetPeriod(date);
	}
	
	public Date getPreviousPeriodStartDate(){
		return categoryPeriods.getBudgetPeriodOffset(date, -1);
	}
	
	public CategoryPeriods getCategoryPeriods(){
		return categoryPeriods;
	}

	public String getPeriodType(){
		return categoryPeriods.toString();
	}
	
	public static enum CategoryPeriods {
		MONTH,
		WEEK,
		QUARTER,
		SEMI_MONTH,
		SEMI_YEAR,
		YEAR;
		
		public Date getStartOfBudgetPeriod(Date date) {
			switch(this){
			case WEEK:
				return DateUtil.getStartOfWeek(date);
			case SEMI_MONTH:
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				if (cal.get(Calendar.DAY_OF_MONTH) <= 15) {
					cal.set(Calendar.DAY_OF_MONTH, 1);
				} else {
					cal.set(Calendar.DAY_OF_MONTH, 16);
				}
				return DateUtil.getStartOfDay(cal.getTime());
			case MONTH:
				return DateUtil.getStartOfMonth(date);
			case QUARTER:
				return DateUtil.getStartOfQuarter(date);
			case SEMI_YEAR:
				if (DateUtil.getMonth(date) <= Calendar.JUNE)
					return DateUtil.getStartOfYear(date);
				else
					return DateUtil.getDate(DateUtil.getYear(date), Calendar.JULY);
			case YEAR:
				return DateUtil.getStartOfYear(date);
			default:
				return null;
			}
		}
		
		public Date getEndOfBudgetPeriod(Date date) {
			switch(this){
			case WEEK:
				return DateUtil.getEndOfWeek(date);
			case SEMI_MONTH:
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				if (cal.get(Calendar.DAY_OF_MONTH) <= 15) {
					cal.set(Calendar.DAY_OF_MONTH, 15);
				} else {
					cal.set(Calendar.DAY_OF_MONTH, cal
							.getActualMaximum(Calendar.DAY_OF_MONTH));
				}
				return DateUtil.getEndOfDay(cal.getTime());
			case MONTH:
				return DateUtil.getEndOfMonth(date);
			case QUARTER:
				return DateUtil.getEndOfQuarter(date);
			case SEMI_YEAR:
				if (DateUtil.getMonth(date) <= Calendar.JUNE)
					return DateUtil.getEndOfMonth(DateUtil.getDate(DateUtil.getYear(date), Calendar.JUNE));
				else
					return DateUtil.getEndOfYear(date);
			case YEAR:
				return DateUtil.getEndOfYear(date);
			default:
				return null;
			}
		}
		
		public Date getBudgetPeriodOffset(Date date, int offset) {
			switch(this){
			case WEEK:
				return getStartOfBudgetPeriod(DateUtil.addDays(DateUtil.getStartOfWeek(date), 7 * offset));
			case SEMI_MONTH:
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				cal.add(Calendar.MONTH, offset / 2);
				if (offset % 2 != 0) {
					if (cal.get(Calendar.DAY_OF_MONTH) <= 15) {
						cal.set(Calendar.DAY_OF_MONTH, 16);
						if (offset < 0) {
							cal.add(Calendar.MONTH, -1);
						}
					} else {
						cal.set(Calendar.DAY_OF_MONTH, 1);
						if (offset > 0) {
							cal.add(Calendar.MONTH, 1);
						}
					}
				}
				return getStartOfBudgetPeriod(cal.getTime());
			case MONTH:
				return getStartOfBudgetPeriod(DateUtil.addMonths(DateUtil.getStartOfMonth(date), 1 * offset));
			case QUARTER:
				return getStartOfBudgetPeriod(DateUtil.addQuarters(date, offset));
			case SEMI_YEAR:
				return getStartOfBudgetPeriod(DateUtil.addMonths(date, offset * 6));
			case YEAR:
				return getStartOfBudgetPeriod(DateUtil.addYears(date, offset));
			default:
				return null;
			}
		}
		
		public long getDaysInPeriod(Date date) {
			switch(this){
			case WEEK:
				return 7;
			case SEMI_MONTH:
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				if (cal.get(Calendar.DAY_OF_MONTH) <= 15) {
					return 15;
				}
				return cal.getActualMaximum(Calendar.DAY_OF_MONTH) - 15;
			case MONTH:
				return DateUtil.getDaysInMonth(date);
			case QUARTER:
				return DateUtil.getDaysBetween(getStartOfBudgetPeriod(date), getEndOfBudgetPeriod(date), true);
			case SEMI_YEAR:
				return DateUtil.getDaysBetween(getStartOfBudgetPeriod(date), getEndOfBudgetPeriod(date), true);
			case YEAR:
				return DateUtil.getDaysBetween(getStartOfBudgetPeriod(date), getEndOfBudgetPeriod(date), true);
			default:
				return 0;
			}
		}
		
		public String getDateFormat() {
			switch(this){
			case WEEK:
			case SEMI_MONTH:
				return "dd MMM yyyy";
			case MONTH:
			case QUARTER:
			case SEMI_YEAR:
				return "MMM yyyy";
			case YEAR:
				return "yyyy";
			default:
				return null;
			}
		}
	}
}
