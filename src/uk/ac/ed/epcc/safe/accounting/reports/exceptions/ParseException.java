package uk.ac.ed.epcc.safe.accounting.reports.exceptions;
/** Part of the template document cannot be parsed.
 * usually because required elements are missing.
 * 
 * @author spb
 *
 */
public class ParseException extends ReportException {

	public ParseException() {
	}

	public ParseException(String message) {
		super(message);
	}

	public ParseException(Throwable cause) {
		super(cause);
	}

	public ParseException(String message, Throwable cause) {
		super(message, cause);
	}

}
