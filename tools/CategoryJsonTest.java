import java.math.BigDecimal;

import org.json.JSONException;
import ca.digitalcave.buddi.live.util.InputValidationException;
import org.json.JSONObject;

import ca.digitalcave.buddi.live.model.Category;
import ca.digitalcave.buddi.live.model.Entry;
import ca.digitalcave.buddi.live.util.FormatUtil;

/** Run after compiling the application, with its classes and WEB-INF/lib on the classpath. */
public class CategoryJsonTest {
	public static void main(String[] args) {
		final String payload = "{\"action\":\"set\",\"categoryId\":48,\"amount\":8600.01,"
				+ "\"date\":\"2026-12-01\",\"periodType\":\"MONTH\"}";
		final JSONObject request = new JSONObject(payload);
		// The resource constructs both models before saving the budget entry.
		new Category(request);
		final Entry reported = new Entry(request);
		check(reported.getCategoryId() == 48, "Category ID");
		check(reported.getAmount().equals("8600.01"), "Numeric budget amount");
		check(FormatUtil.formatDateInternal(reported.getDate()).equals("2026-12-01"), "Budget date");
		check(reported.getId() == null, "New entry ID");

		for (Object amount : new Object[] { "8600.01", "$8,600.01", new BigDecimal("8600.01"),
				new BigDecimal("123456789012345678.90"), new BigDecimal("-0.01"), 0 }) {
			final JSONObject json = new JSONObject(payload);
			json.put("amount", amount);
			final BigDecimal expected = amount.equals("$8,600.01") ? new BigDecimal("8600.01") : new BigDecimal(amount.toString());
			check(new BigDecimal(new Entry(json).getAmount()).compareTo(expected) == 0, "Amount precision: " + amount);
		}

		for (Object amount : new Object[] { null, JSONObject.NULL, true, new JSONObject() }) {
			final JSONObject json = new JSONObject(payload);
			json.put("amount", amount);
			try {
				new Entry(json);
				throw new AssertionError("Invalid amount accepted: " + amount);
			}
			catch (JSONException | InputValidationException expected) {
				// Missing, null and non-currency JSON types must still be rejected.
			}
		}
		System.out.println("Category JSON regression checks passed.");
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
