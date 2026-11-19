import org.json.JSONObject;
import org.restlet.data.Status;
import ca.digitalcave.buddi.live.model.Split;
import ca.digitalcave.buddi.live.model.Entry;
import ca.digitalcave.buddi.live.service.BuddiStatusService;
import ca.digitalcave.buddi.live.util.InputValidationException;
import ca.digitalcave.buddi.live.db.util.DatabaseException;

public class InputValidationTest {
	public static void main(String[] args) {
		for (Object amount : new Object[] { "", " ", "abc", JSONObject.NULL, true }) {
			final JSONObject json = new JSONObject().put("amount", amount).put("fromId", 6).put("toId", 94)
					.put("categoryId", 48).put("date", "2026-12-01");
			expectInvalid(() -> new Split(json), "Split amount");
			expectInvalid(() -> new Entry(json), "Budget amount");
		}
		final String message = BuddiStatusService.validationMessage(new Status(Status.CLIENT_ERROR_BAD_REQUEST,
				new DatabaseException("From and To cannot be the same")));
		check(message.equals("From and To cannot be the same"), "Constraint message was lost");
		final String internal = BuddiStatusService.validationMessage(new Status(Status.SERVER_ERROR_INTERNAL,
				new IllegalArgumentException("private server detail")));
		check(!internal.contains("private server detail"), "Internal error detail exposed");
		System.out.println("Input validation regression checks passed.");
	}

	private static void expectInvalid(Runnable action, String label) {
		try {
			action.run();
			throw new AssertionError("Invalid amount accepted");
		}
		catch (InputValidationException e) {
			final Status status = new BuddiStatusService().toStatus(e, null, null);
			check(status.getCode() == 400, "Expected HTTP 400");
			check(BuddiStatusService.validationMessage(status).contains(label), "Missing field explanation");
		}
	}

	private static void check(boolean value, String message) {
		if (!value) {
			throw new AssertionError(message);
		}
	}
}
