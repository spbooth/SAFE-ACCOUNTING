// Copyright - The University of Edinburgh 2015
/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.db;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionCast;
import uk.ac.ed.epcc.safe.accounting.update.UploadContext;
import uk.ac.ed.epcc.webapp.WebappTestBase;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactoryTestCase;

public abstract class ParseAccountingClassificationFactoryTestCase<F extends ParseAccountingClassificationFactory<R,I>,R extends AccountingClassification,I>
		extends DataObjectFactoryTestCase<F, R>  implements UploadParseTargetInterfaceTest<I, UploadParseTarget<I>>{
	
	
   // private PluginOwnerTestCase<R, PlugInOwner<R>> plugin_owner_test = new PluginOwnerTestCaseImpl<>(()->getPluginOwner());
    private UploadParseTargetInterfaceTest<I, UploadParseTarget<I>> plugin_owner_test = new UploadParseTargetInterfaceTestImp<>((WebappTestBase)this, ()->getPluginOwner(),()->getUploadContext(), ()->ctx);
	

	
	
	public abstract UploadContext getUploadContext();

	
	@Test
	public void testGetParser() {
		plugin_owner_test.testGetParser();
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

	


	@Override
	public UploadParseTarget<I> getPluginOwner() {
		F fac = getFactory();
		return fac.getComposite(PropertyContainerParseTargetComposite.class);
	}


	@Override
	@Test
	public void testGetDerivedProperties() {
		plugin_owner_test.testGetDerivedProperties();
		
	}


	@Override
	@Test
	public void testGetFinder() {
		plugin_owner_test.testGetFinder();
		
	}


	


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