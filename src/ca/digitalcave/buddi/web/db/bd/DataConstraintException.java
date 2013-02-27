package ca.digitalcave.buddi.web.db.bd;

public class DataConstraintException extends Exception {
	private static final long serialVersionUID = 1L;
	public DataConstraintException(String message) {
		super(message);
	}
	public DataConstraintException(Throwable e) {
		super(e);
	}
	public DataConstraintException(String message, Throwable e) {
		super(message, e);
	}
}
