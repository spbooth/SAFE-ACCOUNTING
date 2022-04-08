package uk.ac.ed.epcc.safe.accounting.db;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ed.epcc.junit.TargetProvider;
import uk.ac.ed.epcc.safe.accounting.ErrorSet;
import uk.ac.ed.epcc.safe.accounting.update.UploadContext;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.WebappTestBase;

public class UsageRecordParseTargetInterfaceTestImp<R,X extends UsageRecordParseTarget<R>> extends PropertyContainerParseTargetInterfaceTestImp<R, X>
		implements UsageRecordParseTargetInterfaceTest<R, X> {

	private WebappTestBase base;
	public UsageRecordParseTargetInterfaceTestImp(WebappTestBase base,TargetProvider<X> provider, TargetProvider<UploadContext> upload_prov, Contexed contexed) {
		super(provider, upload_prov, contexed);
		this.base=base;
	}



	@Override
	public X getUsageRecordParseTarget() {
		return getPropertyContinerParseTarget();
	}

	
	public void testReceiveAccounting() throws Exception {
		UploadContext uploadContext = getUploadContext();
		String updateText = uploadContext.getUpdateText();
		String expect = uploadContext.getExpectedResource();
		getUsageRecordParseTarget(); // make sure factory is constructed before baseline
		if( expect != null) {
			base.takeBaseline();
		}
		receiveAccounting(updateText);
		//save("tests",getClass().getSimpleName(),getFactory());
		
		if( expect != null) {
			//base.saveDiff("scratch.xml");
			base.checkDiff("/cleanup.xsl", expect);
		}
	}

    
    public void receiveAccounting(String updateText) throws Exception {
	
    	UploadContext uploadContext = getUploadContext();
	//System.out.println(updateText);
	AccountingUpdater u = new AccountingUpdater(contexed.getContext(),uploadContext.getDefaults(),getUsageRecordParseTarget(), false,false,false);
	String result = u.receiveAccountingData(new ByteArrayInputStream( updateText.getBytes()));
	
	Assert.assertFalse(result.contains("Error in accounting parse"));
}

	

	

}
