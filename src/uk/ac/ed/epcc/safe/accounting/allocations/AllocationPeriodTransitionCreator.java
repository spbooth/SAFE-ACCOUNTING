package uk.ac.ed.epcc.safe.accounting.allocations;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.forms.transition.TransitionFactoryCreator;

public class AllocationPeriodTransitionCreator implements
		Contexed, TransitionFactoryCreator<AllocationPeriodTransitionProvider> {
	private final AppContext c;
	public AllocationPeriodTransitionCreator(AppContext c) {
		this.c=c;
	}

	@SuppressWarnings("unchecked")
	public AllocationPeriodTransitionProvider getTransitionProvider(String tag) {
		
			try {
				AllocationManager man = c.makeObject(AllocationManager.class, tag);
				if(man != null){
					return new AllocationPeriodTransitionProvider(man);
				}
			} catch (Exception e) {
				c.error(e,"Error making AllocationPeriodTransitionProvider");
			}
			
		
		return null;
	}

	public AppContext getContext() {

		return c;
	}

}
