package uk.ac.ed.epcc.safe.accounting.allocations;

import uk.ac.ed.epcc.webapp.session.SessionService;

public class ViewPeriodKey extends PeriodKey {

	public ViewPeriodKey(String name) {
		super(name);
	}

	public ViewPeriodKey(String name, String help) {
		super(name, help);
	}

	@Override
	public boolean allow(SessionService sess, AllocationPeriod target) {
		return target.getPeriod() instanceof ViewPeriod;
	}

}
