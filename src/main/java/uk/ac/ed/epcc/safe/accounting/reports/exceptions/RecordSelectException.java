package uk.ac.ed.epcc.safe.accounting.reports.exceptions;

import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;

/** Exception for a bad {@link RecordSelector}
 * 
 * @author Stephen Booth
 *
 */
public class RecordSelectException extends ReportException {

	public RecordSelectException() {
	}

	public RecordSelectException(String message) {
		super(message);
	}

	public RecordSelectException(Throwable cause) {
		super(cause);
	}

	public RecordSelectException(String message, Throwable cause) {
		super(message, cause);
	}

}
