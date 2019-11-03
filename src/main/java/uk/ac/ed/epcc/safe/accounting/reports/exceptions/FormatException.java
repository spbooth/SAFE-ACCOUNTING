package uk.ac.ed.epcc.safe.accounting.reports.exceptions;

public class FormatException extends ReportException {

	/**
	 * 
	 */
	public FormatException() {
		super();
		
	}

	/**
	 * @param message
	 * @param cause
	 */
	public FormatException(String message, Throwable cause) {
		super(message, cause);
		
	}

	/**
	 * @param message
	 */
	public FormatException(String message) {
		super(message);
		
	}

	/**
	 * @param cause
	 */
	public FormatException(Throwable cause) {
		super(cause);
		
	}

}
