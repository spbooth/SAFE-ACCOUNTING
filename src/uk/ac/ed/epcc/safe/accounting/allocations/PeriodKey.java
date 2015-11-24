package uk.ac.ed.epcc.safe.accounting.allocations;

import uk.ac.ed.epcc.webapp.model.data.transition.TransitionKey;

public class PeriodKey extends TransitionKey<AllocationPeriod> {

	public PeriodKey(String name) {
		super(AllocationPeriod.class, name);
	}

	public PeriodKey(String name, String help){
		super(AllocationPeriod.class,name,help);
	}

	public boolean allow(AllocationPeriod target){
		return true;
	}
}
