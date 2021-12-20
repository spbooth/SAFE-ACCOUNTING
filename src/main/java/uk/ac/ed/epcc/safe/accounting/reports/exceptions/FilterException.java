package uk.ac.ed.epcc.safe.accounting.reports.exceptions;

import uk.ac.ed.epcc.safe.accounting.reports.RecordSet;

/** Exception indicating a problem in the {@link RecordSet}
 * 
 * This should abort any report element using that set
 * 
 * @author spb
 *
 */
public class FilterException extends ReportException {

	public FilterException() {
		
	}

	public FilterException(String message) {
		super(message);
		
	}

	public FilterException(Throwable cause) {
		super(cause);
		
	}

	public FilterException(String message, Throwable cause) {
		super(message, cause);
		
	}

}
