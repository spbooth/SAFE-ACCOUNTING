package uk.ac.ed.epcc.safe.accounting.allocations;

import uk.ac.ed.epcc.webapp.AppContext;


public class ImportedAllocation<T extends AllocationFactory.AllocationRecord,R> extends AllocationFactory<T,R> {

	public ImportedAllocation(AppContext c, String table) {
		super(c, table);
	}

	@Override
	protected boolean editEnds() {
		return false;
	}

	@Override
	public boolean canMerge(T first, T last) {
		return false;
	}

	@Override
	public boolean canSplit() {
		return false;
	}

	

	
}
