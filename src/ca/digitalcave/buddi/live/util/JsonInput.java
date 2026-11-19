package ca.digitalcave.buddi.live.util;

import java.math.BigDecimal;
import org.json.JSONObject;

public final class JsonInput {
	private JsonInput() {}

	public static String currency(JSONObject json, String field, String label, boolean required) {
		final Object value = json.opt(field);
		if (value == null || value == JSONObject.NULL || value.toString().trim().isEmpty()) {
			if (required) {
				throw new InputValidationException(label + " is required. Enter a valid amount.");
			}
			return null;
		}
		if (!(value instanceof Number) && !(value instanceof String)) {
			throw new InputValidationException(label + " must be a valid amount.");
		}
		try {
			final BigDecimal amount = FormatUtil.parseCurrency(value.toString());
			return amount.toPlainString();
		}
		catch (NumberFormatException e) {
			throw new InputValidationException(label + " must be a valid amount.");
		}
	}
}
