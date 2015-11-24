// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.allocations;

import uk.ac.ed.epcc.safe.accounting.UsageRecord;
import uk.ac.ed.epcc.webapp.model.data.transition.TransitionKey;

/** Key object for AllocationFactory transitions
 * 
 * @author spb
 *
 * @param <T>
 */
@uk.ac.ed.epcc.webapp.Version("$Id: AllocationKey.java,v 1.11 2014/09/15 14:32:18 spb Exp $")

public class AllocationKey<T extends UsageRecord> extends TransitionKey<T> {

	public AllocationKey(Class<? super T> t, String name, String help) {
		super(t, name, help);
	}

	public AllocationKey(Class<? super T> t, String name) {
		super(t, name);
	}
	

}