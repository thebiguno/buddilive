package ca.digitalcave.buddi.live.util;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ca.digitalcave.buddi.live.model.Account;
import ca.digitalcave.buddi.live.model.AccountType;
import ca.digitalcave.buddi.live.model.Category;
import ca.digitalcave.buddi.live.model.Source;
import ca.digitalcave.buddi.live.model.Split;
import ca.digitalcave.buddi.live.model.User;

public class FormatUtil {
	public static String HTML_RED = "#dd2222";
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
		return new SimpleDateFormat(user.getDateFormat() == null ? "yyyy-MM-dd" : user.getDateFormat()).format(date);
	}

	public static BigDecimal parseCurrency(String value){
		if (value == null || value.length() == 0) return null;
		return new BigDecimal(value);
	}
	
	public static String formatCurrency(BigDecimal value){
	if (value == null) return null;
	return value.toPlainString();
}

	
//	public static String formatCurrency(BigDecimal value, Source s){
//		if ("C".equals(s.getType())){
//			return formatCurrency(value, true);
//		}
//		return formatCurrency(value, false);
//	}
//	public static String formatCurrency(BigDecimal value, boolean negate){
//		if (value == null) return null;
//		if (negate){
//			return value.negate().toPlainString();
//		}
//		return value.toPlainString();
//	}
//	
//	public static String formatStyle(BigDecimal value, Category c){
//		if (c.isIncome() && c.)
//	}
//	
//	public static String formatStyle(BigDecimal value, Source s){
//		if (value == null) value = BigDecimal.ZERO;
//		if (BigDecimal.ZERO.compareTo(value) > 0 && ("C".equals(s.getType()) || "E".equals(s.getType()))
//				|| BigDecimal.ZERO.compareTo(value) >= 0 && ("D".equals(s.getType()) || "I".equals(s.getType()))){
//			return formatRed();
//		}
//		return "";
//	}
	
	public static String formatRed(){
		return "color: " + FormatUtil.HTML_RED + ";";
	}
	
	public static boolean isRed(Source s){
		if (s instanceof Account){
			return !((Account) s).isDebit();
		}
		else if (s instanceof Category){
			return !((Category) s).isIncome();
		}
		else
			return false;
	}
	
	public static boolean isRed(Source s, BigDecimal value){
		if (s instanceof Account){
			return isRed((Account) s, value);
		}
		else if (s instanceof Category){
			return isRed((Category) s, value);
		}
		else
			return false;
	}

	public static boolean isRed(Account a, BigDecimal value){
		if (a == null || value == null) return false;
		return value.compareTo(BigDecimal.ZERO) < 0;
	}
	
	public static boolean isRed(Category c, BigDecimal value){
		if (value == null || c == null) return false;
		if (c.isIncome())
			return value.compareTo(BigDecimal.ZERO) < 0;
		else
			return value.compareTo(BigDecimal.ZERO) >= 0;
	}

	public static boolean isRed(AccountType t){
		return !t.isDebit();
	}

	public static boolean isRed(AccountType t, BigDecimal value){
		if (value == null || t == null) return false;
		return value.compareTo(BigDecimal.ZERO) < 0;
	}
	
	public static boolean isRed(Split s){
		return !s.isInflow();
	}
	
	public static boolean isRed(Split s, boolean toSelectedAccount){
		if (!toSelectedAccount && s.getAmount().compareTo(BigDecimal.ZERO) >= 0
				|| toSelectedAccount && s.getAmount().compareTo(BigDecimal.ZERO) < 0)
			return true;
		else
			return false;
	}

	public static boolean isRed(BigDecimal value){
		if (value == null || value.compareTo(BigDecimal.ZERO) >= 0)
			return false;
		return true;
	}
}
