package uk.ac.ed.epcc.safe.accounting.allocations;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.db.PropertyContainerParseTargetComposite;
import uk.ac.ed.epcc.safe.accounting.db.UploadParseTarget;
import uk.ac.ed.epcc.safe.accounting.db.UploadParseTargetInterfaceTest;
import uk.ac.ed.epcc.safe.accounting.db.UploadParseTargetInterfaceTestImp;
import uk.ac.ed.epcc.safe.accounting.db.UsageRecordFactoryTestCase;
import uk.ac.ed.epcc.safe.accounting.update.PlugInOwner;
import uk.ac.ed.epcc.safe.accounting.update.UploadContext;
/** Abstract test case for {@link AllocationFactory}s
 * 
 * @author Stephen Booth
 *
 * @param <R>
 * @param <F>
 * @param <T>
 */
public abstract class AllocationFactoryTestCase<R,F extends AllocationFactory<T, R>,T extends AllocationFactory.AllocationRecord> extends UsageRecordFactoryTestCase<F,T> 
implements UploadParseTargetInterfaceTest<R, UploadParseTarget<R>>{

	//private PluginOwnerTestCase<R, PlugInOwner<R>> plugin_owner_test = new PluginOwnerTestCaseImpl<>(()->getPluginOwner());

	private UploadParseTargetInterfaceTest<R, UploadParseTarget<R>> plugin_owner_test = new UploadParseTargetInterfaceTestImp<>(this, ()->getPluginOwner(), ()->getUploadContext(), ()->ctx);
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
	public final  UploadParseTarget<R> getPluginOwner() {
		F factory = getFactory();
		if( factory instanceof PlugInOwner) {
			return (UploadParseTarget<R>) factory;
		}
		return (UploadParseTarget<R>) factory.getComposite(PropertyContainerParseTargetComposite.class);
	}

	@Override
	public abstract UploadContext getUploadContext();

	@Override
	@Test
	public void testSkipLines() throws Exception {
		plugin_owner_test.testSkipLines();
		
	}

	@Override
	@Test
	public void testExceptionLines() throws Exception {
		plugin_owner_test.testExceptionLines();
		
	}

	@Override
	@Test
	public void testGoodLines() throws Exception {
		plugin_owner_test.testGoodLines();
		
	}

	@Override
	@Test
	public void testUpload() throws Exception {
		plugin_owner_test.testUpload();
		
	}

}
