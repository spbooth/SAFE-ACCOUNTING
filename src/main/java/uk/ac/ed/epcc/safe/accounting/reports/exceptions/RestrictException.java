package uk.ac.ed.epcc.safe.accounting.reports.exceptions;

/** Exception thrown indicating a mis-configuration
 * of the access control
 * 
 * @author spb
 *
 */
public class RestrictException extends ReportException {

	public RestrictException() {
		
	}

	public RestrictException(String arg0) {
		super(arg0);
		
	}

	public RestrictException(Throwable cause) {
		super(cause);
		
	}

	public RestrictException(String message, Throwable cause) {
		super(message, cause);
		
	}


}
