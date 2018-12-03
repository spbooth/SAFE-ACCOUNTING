package uk.ac.ed.epcc.safe.accounting.db;


import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.update.PluginOwnerTestCase;
import uk.ac.ed.epcc.safe.accounting.update.UploadContext;

/** Interface test for {@link PropertyContainerParseTarget}
 * 
 * @author Stephen Booth
 *
 */
public interface PropertyContainerParseTargetInterfaceTest<R,X extends PropertyContainerParseTarget<R>> extends PluginOwnerTestCase<R, X> {

	public UploadContext getUploadContext();
	
	
	@Test
	public void testSkipLines() throws Exception;
	
	@Test
	public void testExceptionLines() throws Exception;
	
	@Test
	public void testGoodLines() throws Exception;
	
	default X getPropertyContinerParseTarget(){
		return getPluginOwner();
	}
}
