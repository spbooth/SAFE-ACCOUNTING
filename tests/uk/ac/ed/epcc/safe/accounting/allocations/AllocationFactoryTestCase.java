package uk.ac.ed.epcc.safe.accounting.allocations;

import uk.ac.ed.epcc.accounting.update.PluginOwnerTestCase;
import uk.ac.ed.epcc.accounting.update.PluginOwnerTestCaseImpl;
import uk.ac.ed.epcc.safe.accounting.db.PropertyContainerParseTargetComposite;
import uk.ac.ed.epcc.safe.accounting.db.UsageRecordFactoryTestCase;
import uk.ac.ed.epcc.safe.accounting.update.PlugInOwner;
/** Abstract test case for {@link AllocationFactory}s
 * 
 * @author Stephen Booth
 *
 * @param <R>
 * @param <F>
 * @param <T>
 */
public abstract class AllocationFactoryTestCase<R,F extends AllocationFactory<T, R>,T extends AllocationFactory.AllocationRecord> extends UsageRecordFactoryTestCase<F,T> 
implements PluginOwnerTestCase<R, PlugInOwner<R>>{

	private PluginOwnerTestCase<R, PlugInOwner<R>> plugin_owner_test = new PluginOwnerTestCaseImpl<>(()->getPluginOwner());

	@Override
	public void testGetFinder() {
		plugin_owner_test.testGetFinder();
	}

	@Override
	public void testGetParser() {
		plugin_owner_test.testGetParser();
		
	}

	@Override
	public void testGetPolicies() {
		plugin_owner_test.testGetPolicies();
		
	}

	@Override
	public void testGetDerivedProperties() {
		plugin_owner_test.testGetDerivedProperties();
		
	}

	@Override
	public final  PlugInOwner<R> getPluginOwner() {
		F factory = getFactory();
		if( factory instanceof PlugInOwner) {
			return (PlugInOwner<R>) factory;
		}
		return factory.getComposite(PropertyContainerParseTargetComposite.class).getPlugInOwner();
	}

}
