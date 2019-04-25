package uk.ac.ed.epcc.safe.accounting;

import java.util.Set;

import uk.ac.ed.epcc.webapp.AppContext;

public class ListUsageManager<UR> extends UsageManager<UR> {
	
	public ListUsageManager(AppContext c, String tag,Set<UsageProducer<UR>> facs) {
		super(c, tag, facs);
	}
	@Override
	protected void populate(String tag) {
	}

}
