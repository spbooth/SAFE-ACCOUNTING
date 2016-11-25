package uk.ac.ed.epcc.safe.accounting;

import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;

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
