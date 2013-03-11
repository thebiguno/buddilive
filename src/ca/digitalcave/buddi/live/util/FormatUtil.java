package ca.digitalcave.buddi.live.util;

import java.math.BigDecimal;
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
	
	/**
	 * Convert a currency string (in dollars + cents, i.e. "123.45") to the DB-friendly long
	 * value (i.e. 12345).
	 * @param value
	 * @return
	 */
	public static BigDecimal parseCurrency(String value){
		if (value == null || value.length() == 0) return null;
		return new BigDecimal(value);
	}
	
	/**
	 * Convert a long value from the DB (i.e. 12345) to a currency string (i.e. "123.45").
	 * @param value
	 * @return
	 */
	public static String formatCurrency(BigDecimal value){
		if (value == null) return null;
		return value.toPlainString();
	}
}
