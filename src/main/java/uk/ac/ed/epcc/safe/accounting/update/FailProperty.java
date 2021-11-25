package uk.ac.ed.epcc.safe.accounting.update;

import uk.ac.ed.epcc.safe.accounting.charts.FailLabeller;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.content.FormatProvider;
import uk.ac.ed.epcc.webapp.content.Labeller;

public class FailProperty extends PropertyTag<Boolean> implements FormatProvider<Boolean, String> {

	public FailProperty(PropertyRegistry registry, String name) {
		super(registry, name, Boolean.class);
	}

	public FailProperty(PropertyRegistry registry, String name, String description) {
		super(registry, name, Boolean.class, description);
	}

	@Override
	public Labeller<Boolean, String> getLabeller() {
		return new FailLabeller();
	}

}
