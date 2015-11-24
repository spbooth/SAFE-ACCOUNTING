// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting;

import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.NumberOp;

/** A {@link ReductionTarget} operating on numbers. 
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: NumberMaxReductionTarget.java,v 1.2 2014/09/15 14:32:18 spb Exp $")


public class NumberMaxReductionTarget extends NumberReductionTarget{
	
	public NumberMaxReductionTarget( 
			PropExpression<? extends Number> tag) throws IllegalReductionException {
		super( Reduction.MAX, tag);
	}
	

	@Override
	public Number combine(Number a, Number b) {
		return NumberOp.max(a, b);
		
	}

}