package uk.ac.ed.epcc.safe.accounting;

import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.Reduction;

public class SelectReduction extends ReductionTarget<Object> {

	public SelectReduction(PropExpression<?> tag) throws IllegalReductionException {
		super(Object.class, Reduction.SELECT, tag);
	}

	@Override
	public Object combine(Object a, Object b){
		if( a != null ){
			return  a;
		}else{
			return  b;
		}
	}

}
