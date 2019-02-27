package uk.ac.ed.epcc.safe.accounting;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.webapp.AppContext;

public class ListUsageManager<UR> extends UsageManager<UR> {
	private final Set<UsageProducer<UR>> external_factories;
	public ListUsageManager(AppContext c, String tag,Set<UsageProducer<UR>> facs) {
		super(c, tag);
		this.external_factories=facs;
	}
	@Override
	protected void populate(String tag) {
		for(UsageProducer<UR> prod : external_factories) {
			addProducer(prod.getTag(), prod);
		}
		
	}

}
