// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting;

import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;

/** A {@link ReductionTarget} that generates an index value similar to an SQL GROUP BY clause.
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: IndexReduction.java,v 1.8 2014/09/15 14:32:17 spb Exp $")


public class IndexReduction extends ReductionTarget<Object> {

	public IndexReduction(
			PropExpression<?> tag) throws IllegalReductionException {
		super(Object.class, Reduction.INDEX, tag);
		
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