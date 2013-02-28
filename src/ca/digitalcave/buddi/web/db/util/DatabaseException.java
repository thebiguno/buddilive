package ca.digitalcave.buddi.web.db.util;

public class DatabaseException extends Exception {
	private static final long serialVersionUID = 1L;
	public DatabaseException(String message) {
		super(message);
	}
	public DatabaseException(Throwable e) {
		super(e);
	}
	public DatabaseException(String message, Throwable e) {
		super(message, e);
	}
}
