package uk.ac.ed.epcc.safe.accounting.reports.exceptions;
/** superclass of exceptions generated in ReportExtensions.
 * 
 * @author spb
 *
 */
public class ReportException extends Exception {

	public ReportException() {
	}

	public ReportException(String message) {
		super(message);
	}

	public ReportException(Throwable cause) {
		super(cause);
	}

	public ReportException(String message, Throwable cause) {
		super(message, cause);
	}

}
