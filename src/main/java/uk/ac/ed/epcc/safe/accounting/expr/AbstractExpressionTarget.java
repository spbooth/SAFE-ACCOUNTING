package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.db.MatchSelectVisitor;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.MethodPropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;


public abstract class AbstractExpressionTarget extends EvaluatePropExpressionVisitor implements ExpressionTarget {

	public AbstractExpressionTarget(AppContext ctx) {
		super(ctx);
	}

	

	@Override
	public Object visitMethodPropExpression(MethodPropExpression<?> method) throws Exception {
		return method.evaluate(this);
	}

	

	

	@Override
	public <T> T evaluateExpression(PropExpression<T> expr) throws InvalidExpressionException {
		
		try {
			return (T) expr.accept(this);
		}catch(InvalidExpressionException ie) {
			throw ie;
		} catch (Exception e) {
			String str_expr = expr.toString();
			getLogger().error("Unexpected exception evaluating expression "+str_expr,e);
			
			throw new InvalidExpressionException("Error evaluating expression "+str_expr, e);

		}
	}

	public <R> R evaluateExpression(PropExpression<R> expr, R def){
		try {
			R val = evaluateExpression(expr);
			if( val == null ) {
				return def;
			}
			return val;
		} catch (InvalidExpressionException e) {
			return def;
		}
	}

	private MatchSelectVisitor<AbstractExpressionTarget> match_visitor=null;
	@Override
	protected boolean matches(RecordSelector sel) throws Exception {
		if(match_visitor == null){
			match_visitor=new MatchSelectVisitor<>(this);
		}
	
		return sel.visit(match_visitor).booleanValue();
		
	}
	

}
