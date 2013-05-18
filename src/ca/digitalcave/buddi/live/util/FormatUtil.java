package ca.digitalcave.buddi.live.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ca.digitalcave.buddi.live.model.Source;
import ca.digitalcave.buddi.live.model.Split;
import ca.digitalcave.buddi.live.model.User;

public class FormatUtil {
	public static String HTML_RED = "#dd2222";
	public static String HTML_DISABLED_RED = "#886666";
	public static String HTML_GRAY = "#bbbbbb";
	
	public static String formatDateTimeInternal(Date date){
		if (date == null) return null;
		return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(date);
	}
	public static String formatDateInternal(Date date){
		if (date == null) return null;
		return new SimpleDateFormat("yyyy-MM-dd").format(date);
	}
	public static Date parseDateTimeInternal(String date){
		if (date == null) return null;
		try {
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(date);
		}
		catch (ParseException e){
			return null;
		}
	}
	public static Date parseDateInternal(String date){
		if (date == null) return null;
		try {
			return new SimpleDateFormat("yyyy-MM-dd").parse(date);
		}
		catch (ParseException e){
			return null;
		}
	}
	
	public static String formatDate(Date date, User user){
		if (date == null) return null;
		return new SimpleDateFormat(user.getDateFormat()).format(date);
	}

	public static BigDecimal parseCurrency(String value){
		if (value == null || value.length() == 0) return null;
		return new BigDecimal(value);
	}
	
	public static String formatCurrency(BigDecimal value, User user){
		if (value == null) return null;
		
		final DecimalFormat format = (DecimalFormat) DecimalFormat.getCurrencyInstance(user.getLocale());
		format.setCurrency(user.getCurrency());
		format.setMaximumFractionDigits(user.getCurrency().getDefaultFractionDigits());
		format.setMinimumFractionDigits(user.getCurrency().getDefaultFractionDigits());
		return format.format(value);
	}
	
	public static String formatCurrency(BigDecimal value, User user, Source source){
		if ("C".equals(source.getType()) || "E".equals(source.getType())){
			return formatCurrency(value == null ? null : value.negate(), user);
		}
		return formatCurrency(value, user);
	}
	
	public static String formatRed(){
		return "color: " + FormatUtil.HTML_RED + ";";
	}
	public static String formatGray(){
		return "color: " + FormatUtil.HTML_GRAY + ";";
	}
	
	public static boolean isRed(Source selected, Split split){
		boolean toSelected = split.getToSource() == selected.getId();
		boolean positive = split.getAmount().compareTo(BigDecimal.ZERO) >= 0;
		if ((!toSelected && positive) || (toSelected && !positive)){
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean isRed(Source s, BigDecimal value){
		if (s == null){
			return false;
		}
		if ("D".equals(s.getType()) || "C".equals(s.getType())){
			return value.compareTo(BigDecimal.ZERO) < 0;
		}
		else if ("I".equals(s.getType())){
			return value.compareTo(BigDecimal.ZERO) < 0;
		}
		else if ("E".equals(s.getType())){
			return value.compareTo(BigDecimal.ZERO) >= 0;
		}
		else {
			throw new RuntimeException("Unknown source type '" + s.getType() + "'");
		}
	}
	public static boolean isRed(BigDecimal value){
		if (value == null || value.compareTo(BigDecimal.ZERO) >= 0)
			return false;
		return true;
	}
}
