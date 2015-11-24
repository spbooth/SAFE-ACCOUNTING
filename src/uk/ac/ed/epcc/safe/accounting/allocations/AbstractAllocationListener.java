package uk.ac.ed.epcc.safe.accounting.allocations;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;

public class AbstractAllocationListener<T extends AllocationFactory.AllocationRecord> implements AllocationListener<T>{

	public void deleted(T rec) {
	
		
	}

	public void canCreate(PropertyContainer values) throws ListenerObjection {
		
		
	}

	public void created(T rec) {
	
		
	}

	public void canModify(T rec, PropertyContainer values)
			throws ListenerObjection {
		
		
	}

	public void modified(T rec, String details) {
		
		
	}

	public void split(T before, T after) {
		
		
	}

	public void canMerge(T before, T after) throws ListenerObjection {
	
		
	}

	public void merge(T before, T after) {
		
		
	}

	public void aggregate(T rec, PropertyContainer props, boolean add) {
		
	}

}
