// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting;

import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.AverageValue;
import uk.ac.ed.epcc.webapp.NumberOp;

/** A {@link ReductionTarget} operating on numbers that generate an
 * average.
 * The result is always a {@link AverageValue} object.
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: NumberAverageReductionTarget.java,v 1.2 2014/09/15 14:32:17 spb Exp $")


public class NumberAverageReductionTarget extends NumberReductionTarget{
	public NumberAverageReductionTarget(
			PropExpression<? extends Number> tag) throws IllegalReductionException {
		super(Reduction.AVG, tag);
	}
	
	

	@Override
	public Number combine(Number a, Number b) {
		return NumberOp.average(a, b);
	}
	@Override
	public Number getDefault(){
		return new AverageValue(0.0, 0L);
		
	}

}