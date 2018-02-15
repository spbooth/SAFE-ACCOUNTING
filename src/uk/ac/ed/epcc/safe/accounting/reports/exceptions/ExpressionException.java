package uk.ac.ed.epcc.safe.accounting.reports.exceptions;

import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;

/** Exception indicating a problem in a {@link PropExpression}
 * 
 * This should abort any report element using that set
 * 
 * @author spb
 *
 */
public class ExpressionException extends ReportException {

	public ExpressionException() {
		// TODO Auto-generated constructor stub
	}

	public ExpressionException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public ExpressionException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public ExpressionException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
