package uk.ac.ed.epcc.safe.accounting.allocations;

import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.webapp.servlet.ViewTransitionKey;

public class ViewAllocationKey<T extends ExpressionTargetContainer> extends AllocationKey<T> implements ViewTransitionKey<T> {

	public ViewAllocationKey(Class<? super T> t, String name, String help) {
		super(t, name, help);
	}

	public ViewAllocationKey(Class<? super T> t, String name) {
		super(t, name);
	}

}
