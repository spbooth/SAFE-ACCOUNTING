package uk.ac.ed.epcc.safe.accounting;

import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.Reduction;
/** A {@link ReductionTarget} for a SELECT {@link Reduction} that just evaluates the expression.
 * 
 * 
 * @author Stephen Booth
 *
 */
public class SelectReduction extends ReductionTarget<Object,Object> {

	public SelectReduction(PropExpression<?> tag) throws IllegalReductionException {
		super(Object.class, Object.class,Reduction.SELECT, tag);
	}

}
