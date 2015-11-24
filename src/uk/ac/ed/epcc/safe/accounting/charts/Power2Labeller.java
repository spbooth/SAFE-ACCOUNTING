// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.charts;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.content.Labeller;
@uk.ac.ed.epcc.webapp.Version("$Id: Power2Labeller.java,v 1.7 2014/09/15 14:32:19 spb Exp $")

/** Labeller to map a number to the next highest power of two.
 * It returns an integer so the results sort numerically.
 * @author spb
 *
 * @param <T>
 */
public class Power2Labeller<T extends Number> implements Labeller<T,Integer> {

	public Integer getLabel(AppContext conn, T key) {
		int i=key.intValue();
		int j=1;
		while(j< i){
			j*=2;
		}
		return j;
	}
	
	public Class<? super Integer> getTarget(){
		return Integer.class;
	}

	public boolean accepts(Object o) {
		if( o != null && o instanceof Number){
			return true;
		}
		return false;
	}

}