package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;

/** Exception thrown if an expression is unsupported
 * 
 * @author spb
 *
 */
public class UnsupportedExpressionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4445691984890652620L;
	PropExpression e;
	public UnsupportedExpressionException(PropExpression e) {
		super("Unsupported expression "+e.toString());
		this.e=e;
	}

	public PropExpression geExpression(){
		return e;
	}
	

}
