package uk.ac.ed.epcc.safe.accounting.db;

import org.junit.Test;

public interface UploadParseTargetInterfaceTest<R,X extends UploadParseTarget<R>> extends PropertyContainerParseTargetInterfaceTest<R, X> {

	public default X getUploadParseTarget() {
		return getPropertyContinerParseTarget();
	}
	
	@Test
	public void testUpload() throws Exception;
}
