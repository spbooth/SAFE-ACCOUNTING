package uk.ac.ed.epcc.safe.accounting.db;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.util.Iterator;

import uk.ac.ed.epcc.junit.TargetProvider;
import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.PluginOwnerTestCaseImpl;
import uk.ac.ed.epcc.safe.accounting.update.UploadContext;
import uk.ac.ed.epcc.webapp.Contexed;

public class PropertyContainerParseTargetInterfaceTestImp<R,X extends PropertyContainerParseTarget<R>> extends PluginOwnerTestCaseImpl<R, X> implements PropertyContainerParseTargetInterfaceTest<R, X>  {
	protected final Contexed contexed;
    private final TargetProvider<UploadContext> upload;
	public PropertyContainerParseTargetInterfaceTestImp(TargetProvider<X> provider, TargetProvider<UploadContext> upload, Contexed contexed) {
		super(provider);
		this.upload=upload;
		this.contexed=contexed;
	}

	@Override
	public UploadContext getUploadContext() {
		return upload.getTarget();
	}

	@Override
	public void testSkipLines() throws Exception {
		X target = getPropertyContinerParseTarget();
		UploadContext uploadContext = getUploadContext();
		target.startParse(uploadContext.getDefaults());
		String skipText = uploadContext.getSkipText();
		if( skipText==null ||skipText.isEmpty()) {
			return;
		}
		for( Iterator<R> it = target.getParser().splitRecords(new ByteArrayInputStream(skipText.getBytes())); it.hasNext() ;) {
			R current_line = it.next();
			DerivedPropertyMap map = new DerivedPropertyMap(contexed.getContext());
			assertFalse(target.parse(map, current_line));
		}
		
	}

	@Override
	public void testExceptionLines() throws Exception {
		X target = getPropertyContinerParseTarget();
		UploadContext uploadContext = getUploadContext();
		target.startParse(uploadContext.getDefaults());
		String exceptionText = uploadContext.getExceptionText();
		if( exceptionText == null || exceptionText.isEmpty()) {
			return;
		}
		for( Iterator<R> it = target.getParser().splitRecords(new ByteArrayInputStream( exceptionText.getBytes())); it.hasNext() ;) {
			R current_line = it.next();
			DerivedPropertyMap map = new DerivedPropertyMap(contexed.getContext());
			try {
				target.parse(map, current_line);
				target.lateParse(map);
				fail("Expecting exception");
			}catch(AccountingParseException e) {
				// ok expecting this
			}
		}
		
	}

	@Override
	public void testGoodLines() throws Exception{
		X target = getPropertyContinerParseTarget();
		UploadContext uploadContext = getUploadContext();
		target.startParse(uploadContext.getDefaults());
		String updateText = uploadContext.getUpdateText();
		if( updateText == null || updateText.isEmpty()) {
			return;
		}
		for( Iterator<R> it = target.getParser().splitRecords(new ByteArrayInputStream( updateText.getBytes())); it.hasNext() ;) {
			R current_line = it.next();
			DerivedPropertyMap map = new DerivedPropertyMap(contexed.getContext());
			assertTrue(target.parse(map, current_line));
		}
	}

}
