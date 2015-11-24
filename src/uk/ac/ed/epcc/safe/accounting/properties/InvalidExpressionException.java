package uk.ac.ed.epcc.safe.accounting.properties;

/** Exception thrown when an expression cannot be evaluated.
 * 
 * @author spb
 * @see InvalidPropertyException
 *
 */
public class InvalidExpressionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidExpressionException() {
	}

	public InvalidExpressionException(String message) {
		super(message);
		
	}

	public InvalidExpressionException(Throwable cause) {
		super(cause);
		
	}

	public InvalidExpressionException(String message, Throwable cause) {
		super(message, cause);
		
	}

	public InvalidExpressionException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
