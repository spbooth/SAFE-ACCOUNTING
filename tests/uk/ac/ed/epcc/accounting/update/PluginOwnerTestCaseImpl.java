package uk.ac.ed.epcc.accounting.update;

import static org.junit.Assert.assertNotNull;

import java.util.Set;

import uk.ac.ed.epcc.junit.TargetProvider;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.update.PlugInOwner;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerPolicy;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;

public class PluginOwnerTestCaseImpl<R,X extends PlugInOwner<R>> implements PluginOwnerTestCase<R, X> {

	private final TargetProvider<X> provider;
	public PluginOwnerTestCaseImpl(TargetProvider<X> provider) {
		this.provider=provider;
	}

	@Override
	public void testGetFinder() {
		assertNotNull(getPluginOwner().getFinder());
		
	}

	

	@Override
	public void testGetParser() {
		PropertyContainerParser<R> p = getPluginOwner().getParser();

		assertNotNull(p);
		
	}

	@Override
	public void testGetPolicies() {
		Set<PropertyContainerPolicy> s = getPluginOwner().getPolicies();

		assertNotNull(s);
		
	}

	@Override
	public void testGetDerivedProperties() {
		PropExpressionMap derived = getPluginOwner().getDerivedProperties();
		assertNotNull(derived);
		
		
	}

	@Override
	public X getPluginOwner() {
		return provider.getTarget();
	}

}
