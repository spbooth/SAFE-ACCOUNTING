// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting;

import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;

/** A {@link ReductionTarget} operating on Dates.
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: DateReductionTarget.java,v 1.9 2014/09/15 14:32:17 spb Exp $")


public class DateReductionTarget extends ReductionTarget<Date> {

	public DateReductionTarget(Reduction op,
			PropExpression<? extends Date> tag) throws IllegalReductionException {
		super(Date.class, op, tag);
		if(  op == Reduction.SUM || op == Reduction.AVG){
			throw new IllegalReductionException("Bad operation on date "+op);
		}
	}
	@Override
	public Date combine(Date a, Date b){
	
		if(a != null && b != null){
			switch(getReduction()){
			case MIN: if( a.before(b) ){ return  a; }else{ return  b; }
			case MAX: if( a.after(b) ){ return  a; }else{ return b; }
			}
		}
		if( a != null ){
			return  a;
		}else{
			return  b;
		}
	}
}