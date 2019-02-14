package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;
/** Evaluate constant prop-expressions.
 * That is expressions that can be evaluated without a target object.
 * 
 * @author Stephen Booth
 *
 */
public class ConstEvaluatePropExpressionVisitor extends EvaluatePropExpressionVisitor {

	public ConstEvaluatePropExpressionVisitor(AppContext ctx) {
		super(ctx);
	}

	@Override
	public Object visitPropertyTag(PropertyTag<?> tag) throws Exception {
		throw new InvalidPropertyException(tag);
	}

	@Override
	protected boolean matches(RecordSelector sel) throws Exception {
		return false;
	}

}
