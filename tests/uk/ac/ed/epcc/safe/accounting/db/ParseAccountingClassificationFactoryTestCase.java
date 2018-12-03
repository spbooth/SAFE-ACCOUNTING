// Copyright - The University of Edinburgh 2015
/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.db;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ed.epcc.accounting.update.UploadContext;
import uk.ac.ed.epcc.safe.accounting.ErrorSet;
import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionCast;
import uk.ac.ed.epcc.webapp.WebappTestBase;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactoryTestCase;
import uk.ac.ed.epcc.webapp.model.data.Duration;

public abstract class ParseAccountingClassificationFactoryTestCase<F extends ParseAccountingClassificationFactory<R,I>,R extends AccountingClassification,I>
		extends DataObjectFactoryTestCase<F, R>  implements UploadParseTargetInterfaceTest<I, UploadParseTarget<I>>{
	
	
   // private PluginOwnerTestCase<R, PlugInOwner<R>> plugin_owner_test = new PluginOwnerTestCaseImpl<>(()->getPluginOwner());
    private UploadParseTargetInterfaceTest<I, UploadParseTarget<I>> plugin_owner_test = new UploadParseTargetInterfaceTestImp<>((WebappTestBase)this, ()->getPluginOwner(),()->getUploadContext(), ()->ctx);
	public String getUpdateText() throws IOException {
		return "";
	}

	
	public Map<String,Object> getUpdateMap(){
		return new HashMap<>();
	}
	
	public abstract UploadContext getUploadContext();

	
	@Test
	public void testGetParser() {
		plugin_owner_test.testGetParser();
	}

	public String info(Object o) {
		if (o == null) {
			return "null";
		}
		return "<"+o.toString() + "> [" + o.getClass().getCanonicalName() + "]";
	}

	

@Test
	public void testRecieveData() throws Exception {

		F fac = getFactory();
		if (!fac.isValid()) {
			return;
		}
		String updateText = getUpdateText();
		//System.out.println(updateText);
		ErrorSet errors = new ErrorSet();
		ErrorSet skips = new ErrorSet();
		UploadParseTargetUpdater<R> updater = new UploadParseTargetUpdater<>(ctx, (UploadParseTarget<R>) fac);
		String result = updater.receiveData(getUpdateMap(), updateText,errors,skips);
		//TestDataHelper.saveDataSet("NGSRecord", "NGSRecord", "lsf");
		Assert.assertEquals(0,errors.size());
		Assert.assertEquals(0,skips.size());
		//System.out.println(result);
	}
@Test
	public void testGetPolicies() {
		plugin_owner_test.testGetPolicies();
	}
@Test
	public void testGetMapper() {
		F fac = getFactory();
		if (!fac.isValid()) {
			return;
		}
		ExpressionTargetFactory<R> etf = ExpressionCast.getExpressionTargetFactory(fac);
		AccessorMap map = etf.getAccessorMap();

		assertNotNull(map);

	}

	public boolean compare(Object a, Object b) {
		if (a instanceof Duration) {
			a = Long.valueOf(((Duration) a).getMilliseconds());
		}
		if (b instanceof Duration) {
			b = Long.valueOf(((Duration) b).getMilliseconds());
		}
		// System.out.println(" "+a.getClass().getCanonicalName()+" "+b.getClass().getCanonicalName()+" "+a.toString()+" "+b.toString());
		
		if( (a instanceof Integer || a instanceof Long) &&( b instanceof Integer || b instanceof Long)){
			// compare as integer
			return ((Number) a).longValue() == ((Number) b).longValue();
		}else if ( a instanceof Number && b instanceof Number ){
			// compare as floating
			return ((Number) a).floatValue() == ((Number) b).floatValue();
		}
		return a.equals(b);
	}


	@Override
	public UploadParseTarget<I> getPluginOwner() {
		F fac = getFactory();
		return fac.getComposite(PropertyContainerParseTargetComposite.class);
	}


	@Override
	public void testGetDerivedProperties() {
		plugin_owner_test.testGetDerivedProperties();
		
	}


	@Override
	public void testGetFinder() {
		plugin_owner_test.testGetFinder();
		
	}


	


	@Override
	public void testSkipLines() throws Exception {
		plugin_owner_test.testSkipLines();
		
	}


	@Override
	public void testExceptionLines() throws Exception {
		plugin_owner_test.testExceptionLines();
		
	}


	@Override
	public void testGoodLines() throws Exception {
		plugin_owner_test.testGoodLines();
		
	}


	@Override
	public void testUpload() throws Exception {
		plugin_owner_test.testUpload();
		
	}
   
	
}