package ca.digitalcave.buddi.live.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
	
	public static Long parseCurrency(String value){
		if (value == null) return null;
		final String numbers = value.replaceAll("[^0-9]", "");
		if (numbers.length() == 0) return null;
		try {
			return Long.parseLong(numbers);
		}
		catch (NumberFormatException e){
			return null;
		}
	}
	
	public static String formatCurrency(Long value){
		if (value == null) return null;
		final NumberFormat f = DecimalFormat.getInstance();
		f.setMaximumFractionDigits(2);
		f.setMinimumFractionDigits(2);
		return f.format((double) value / 100.0);
	}
}
