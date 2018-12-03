package uk.ac.ed.epcc.safe.accounting.db;

import org.junit.Test;

public interface UsageRecordParseTargetInterfaceTest<R,X extends UsageRecordParseTarget<R>> extends PropertyContainerParseTargetInterfaceTest<R, X> {

	public default X getUsageRecordParseTarget() {
		return getPropertyContinerParseTarget();
	}
	
	@Test
	public void testReceiveAccounting() throws Exception;
}
