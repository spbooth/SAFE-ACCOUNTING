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
		// TODO Auto-generated constructor stub
	}

	public FilterException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public FilterException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public FilterException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
