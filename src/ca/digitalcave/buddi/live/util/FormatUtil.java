package ca.digitalcave.buddi.live.util;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ca.digitalcave.buddi.live.model.Source;

public class FormatUtil {
	public static String HTML_RED = "#dd2222";
	public static String HTML_GRAY = "#bbbbbb";
	
	public static String formatDateTime(Date date){
		if (date == null) return null;
		return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(date);
	}
	public static String formatDate(Date date){
		if (date == null) return null;
		return new SimpleDateFormat("yyyy-MM-dd").format(date);
	}
	public static Date parseDateTime(String date){
		if (date == null) return null;
		try {
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(date);
		}
		catch (ParseException e){
			return null;
		}
	}
	public static Date parseDate(String date){
		if (date == null) return null;
		try {
			return new SimpleDateFormat("yyyy-MM-dd").parse(date);
		}
		catch (ParseException e){
			return null;
		}
	}
	

	public static BigDecimal parseCurrency(String value){
		if (value == null || value.length() == 0) return null;
		return new BigDecimal(value);
	}
	
	public static String formatCurrency(BigDecimal value, Source s){
		if (value == null) return null;
		if ("C".equals(s.getType()) || "I".equals(s.getType())){
			return value.negate().toPlainString();
		}
		return value.toPlainString();
	}
	
	public static String formatStyle(BigDecimal value, Source s){
		if (value.compareTo(BigDecimal.ZERO) <= 0 && "C".equals(s.getType())
				|| value.compareTo(BigDecimal.ZERO) < 0 && "D".equals(s.getType())){
			return "color: " + FormatUtil.HTML_RED + ";";
		}
		return "";
	}
}
