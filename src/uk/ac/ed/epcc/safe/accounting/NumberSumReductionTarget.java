// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting;

import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.AverageValue;
import uk.ac.ed.epcc.webapp.NumberOp;

/** A {@link ReductionTarget} operating on numbers. 
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: NumberSumReductionTarget.java,v 1.2 2014/09/15 14:32:18 spb Exp $")


public class NumberSumReductionTarget extends NumberReductionTarget{
	private final Number def;
	public NumberSumReductionTarget(
			PropExpression<? extends Number> tag) throws IllegalReductionException {
		super( Reduction.SUM, tag);
		Class clazz = tag.getTarget();
		if( clazz == AverageValue.class){
			def = new AverageValue(0.0, 0L);
		}else if( clazz == Double.class || clazz == Number.class){
			def = Double.valueOf(0.0);
		}else if( clazz == Float.class){
			def = Float.valueOf(0.0F);
		}else if( clazz == Long.class){
			def = Long.valueOf(0L);
		}else if( clazz == Integer.class){
			def =  Integer.valueOf(0);
		}else{
		    def = Double.valueOf(0.0);
		}
	}
	public NumberSumReductionTarget( 
			PropExpression<? extends Number> tag,Number def) throws IllegalReductionException {
		super(Reduction.SUM, tag);
		this.def=def;
	}
	

	@Override
	public Number combine(Number a, Number b) {
		return NumberOp.add(a, b);
	}
	@Override
	public Number getDefault(){
		return def;
		
	}

}