package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.time.TimePeriod;
/** An {@link ExpressionTarget} that provides custom overlap calculations for some properties
 * 
 * @author spb
 *
 */
public interface TimePeriodExpressionTarget extends ExpressionTarget {

	/** 
	 * 
	 * @param period
	 * @param prop
	 * @return overlap
	 * @throws InvalidExpressionException
	 */
		public abstract <T> T evaluateOverlapProperty(TimePeriod period, PropExpression<T> prop)
				throws InvalidExpressionException;
}
