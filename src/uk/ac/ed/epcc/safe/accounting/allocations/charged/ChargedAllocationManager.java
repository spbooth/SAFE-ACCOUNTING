// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.allocations.charged;

import uk.ac.ed.epcc.safe.accounting.UsageRecordListener;
import uk.ac.ed.epcc.safe.accounting.allocations.Allocation;
import uk.ac.ed.epcc.safe.accounting.allocations.AllocationManager;
/** AllocationManager where charges are accumulated.
 * 
 * @author spb
 * @param <K> transition key
 *
 * @param <T> Allocation usage record type
 */
public interface ChargedAllocationManager<K,T extends Allocation> extends AllocationManager<K,T>,
		UsageRecordListener {

}