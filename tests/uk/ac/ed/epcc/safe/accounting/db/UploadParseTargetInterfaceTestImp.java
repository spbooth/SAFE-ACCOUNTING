package uk.ac.ed.epcc.safe.accounting.db;

import org.junit.Assert;

import uk.ac.ed.epcc.accounting.update.UploadContext;
import uk.ac.ed.epcc.junit.TargetProvider;
import uk.ac.ed.epcc.safe.accounting.ErrorSet;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.WebappTestBase;

public class UploadParseTargetInterfaceTestImp<R,X extends UploadParseTarget<R>> extends PropertyContainerParseTargetInterfaceTestImp<R, X>
		implements UploadParseTargetInterfaceTest<R, X> {

	private WebappTestBase base;
	public UploadParseTargetInterfaceTestImp(WebappTestBase base,TargetProvider<X> provider, TargetProvider<UploadContext> upload_prov, Contexed contexed) {
		super(provider, upload_prov, contexed);
		this.base=base;
	}



	@Override
	public X getUploadParseTarget() {
		return getPropertyContinerParseTarget();
	}

	@Override
	public void testUpload() throws Exception {

		String updateText = getUploadContext().getUpdateText();
		AppContext ctx = contexed.getContext();
		String expected = getUploadContext().getExpectedResource();
		ErrorSet errors = new ErrorSet();
		ErrorSet skips = new ErrorSet();
		if( expected != null) {
			base.takeBaseline();
		}
		UploadParseTargetUpdater<R> updater = new UploadParseTargetUpdater<>(ctx, getUploadParseTarget());
		String result = updater.receiveData(getUploadContext().getDefaultParams(), updateText,errors,skips);
		//TestDataHelper.saveDataSet("NGSRecord", "NGSRecord", "lsf");
		Assert.assertEquals(0,errors.size());
		Assert.assertEquals(0,skips.size());
		if( expected != null ) {
			base.checkDiff("/cleanup.xsl", expected);
		}
	}

}
