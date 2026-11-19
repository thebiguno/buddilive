import java.math.BigDecimal;

import org.json.JSONObject;

import ca.digitalcave.buddi.live.model.ScheduledTransaction;
import ca.digitalcave.buddi.live.model.Transaction;

/** Run after compiling the application, with its classes and WEB-INF/lib on the classpath. */
public class TransactionJsonTest {
	public static void main(String[] args) {
		final String payload = "{\"action\":\"update\",\"id\":17939,\"date\":\"2026-11-17\","
				+ "\"description\":\"TD Auto Insurance\",\"number\":\"\","
				+ "\"splits\":[{\"amount\":1356.8,\"fromId\":6,\"toId\":94,\"memo\":\"\"}]}";
		final Transaction reported = new Transaction(new JSONObject(payload));
		check(reported.getId() == 17939L, "Numeric transaction ID");
		check(reported.getSplits().get(0).getAmount().equals("1356.8"), "Numeric split amount");
		check(reported.getSplits().get(0).getFromSource() == 6, "From source");
		check(reported.getSplits().get(0).getToSource() == 94, "To source");

		for (Object amount : new Object[] { "1356.80", "$1,356.80", new BigDecimal("1356.80"),
				new BigDecimal("123456789012345678.90"), new BigDecimal("-0.01"), 0 }) {
			final JSONObject json = new JSONObject(payload);
			json.put("id", "17939");
			json.getJSONArray("splits").getJSONObject(0).put("amount", amount);
			final Transaction parsed = new Transaction(json);
			final BigDecimal expected = amount.equals("$1,356.80") ? new BigDecimal("1356.80") : new BigDecimal(amount.toString());
			check(parsed.getId() == 17939L, "String transaction ID");
			check(new BigDecimal(parsed.getSplits().get(0).getAmount()).compareTo(expected) == 0, "Amount precision: " + amount);
		}

		for (Object id : new Object[] { null, JSONObject.NULL, "" }) {
			final JSONObject json = new JSONObject(payload);
			json.put("id", id);
			check(new Transaction(json).getId() == null, "Insert without ID");
		}

		for (Object id : new Object[] { 17939, "17939" }) {
			final JSONObject scheduled = new JSONObject();
			scheduled.put("id", id);
			scheduled.put("name", "Insurance");
			scheduled.put("scheduleDay", 17);
			scheduled.put("scheduleWeek", 0);
			scheduled.put("scheduleMonth", 0);
			scheduled.put("repeat", "SCHEDULE_FREQUENCY_MONTHLY_BY_DATE");
			scheduled.put("start", "2026-11-17");
			scheduled.put("end", JSONObject.NULL);
			scheduled.put("lastCreatedDate", JSONObject.NULL);
			scheduled.put("transaction", new JSONObject(payload));
			check(new ScheduledTransaction(scheduled).getId() == 17939L, "Scheduled transaction ID");
			check(new ScheduledTransaction(scheduled).getEndDate() == null, "Optional scheduled end date");
			check(new ScheduledTransaction(scheduled).getLastCreatedDate() == null, "Optional last-created date");
		}
		System.out.println("Transaction JSON regression checks passed.");
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
