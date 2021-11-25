package uk.ac.ed.epcc.safe.accounting.charts;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.content.Labeller;

public class FailLabeller implements Labeller<Boolean, String> {

	public FailLabeller() {
	}

	@Override
	public Class<String> getTarget() {
		return String.class;
	}

	@Override
	public String getLabel(AppContext conn, Boolean key) {
		if( key instanceof Boolean && ((Boolean)key).booleanValue()) {
			return "Failed";
		}
		return "OK";
	}

	@Override
	public boolean accepts(Object o) {
		
		return o instanceof Boolean;
	}

}
