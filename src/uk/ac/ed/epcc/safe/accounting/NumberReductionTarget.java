package uk.ac.ed.epcc.safe.accounting;

import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;

public abstract class NumberReductionTarget extends ReductionTarget<Number> {

	public NumberReductionTarget(Reduction op,
			PropExpression<? extends Number> tag)
			throws IllegalReductionException {
		super(Number.class, op, tag);
	}

	public static NumberReductionTarget getInstance(Reduction op, PropExpression<? extends Number> tag) throws IllegalReductionException{
		switch(op){
			case AVG: return new NumberAverageReductionTarget( tag);
			case SUM: return new NumberSumReductionTarget(tag);
			case MIN: return new NumberMinReductionTarget(tag);
			case MAX: return new NumberMaxReductionTarget(tag);
			default: throw new IllegalReductionException("Unsupported Number reduction "+op);
		}
	}

}
