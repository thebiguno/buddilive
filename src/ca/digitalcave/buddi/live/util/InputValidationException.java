package ca.digitalcave.buddi.live.util;

/** A message safe to show to the user when a request field is invalid. */
public class InputValidationException extends IllegalArgumentException {
	private static final long serialVersionUID = 1L;

	public InputValidationException(String message) {
		super(message);
	}
}
