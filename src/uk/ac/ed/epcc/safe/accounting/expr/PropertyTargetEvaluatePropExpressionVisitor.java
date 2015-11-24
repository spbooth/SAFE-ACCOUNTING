package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.db.MatchSelectVisitor;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTarget;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;

/** visitor to evaluate an expression on a simple {@link PropertyTarget}
 * 
 * @author spb
 *
 */
public class PropertyTargetEvaluatePropExpressionVisitor extends
		EvaluatePropExpressionVisitor  {

	public PropertyTargetEvaluatePropExpressionVisitor(AppContext ctx, PropertyTarget target) {
		super(ctx);
		this.target=target;
	}

	private final PropertyTarget target;
	
	public Object visitPropertyTag(PropertyTag<?> tag) throws Exception {
		return target.getProperty(tag,null);
	}

	@Override
	protected boolean matches(RecordSelector sel) throws Exception {
		if( target instanceof ExpressionTarget){
			MatchSelectVisitor<ExpressionTarget> vis = new MatchSelectVisitor<ExpressionTarget>((ExpressionTarget)target);
			return sel.visit(vis).booleanValue();
		}
		throw new CannotFilterException("Cannot apply RecordSelector to PropertyTarget");
	}


	
}
