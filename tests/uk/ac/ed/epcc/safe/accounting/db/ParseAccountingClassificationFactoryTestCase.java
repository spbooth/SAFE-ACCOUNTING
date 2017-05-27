// Copyright - The University of Edinburgh 2015
/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.db;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.ErrorSet;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerPolicy;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactoryTestCase;
import uk.ac.ed.epcc.webapp.model.data.Duration;

public abstract class ParseAccountingClassificationFactoryTestCase<F extends ParseAccountingClassificationFactory<R,I>,R extends AccountingClassification,I>
		extends DataObjectFactoryTestCase<F, R>  {
	
	

	public String getUpdateText() throws IOException {
		return "";
	}

	
	public Map<String,Object> getUpdateMap(){
		return new HashMap<String, Object>();
	}
	

	
	@Test
	public void testGetParser() {
		F fac = getFactory();
		if (!fac.isValid()) {
			return;
		}
		PropertyContainerParser p = fac.getParser();

		assertNotNull(p);

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
		ClassificationUpdater<R,I> updater = new ClassificationUpdater<R,I>(ctx, fac);
		String result = updater.receiveData(getUpdateMap(), updateText,errors,skips);
		//TestDataHelper.saveDataSet("NGSRecord", "NGSRecord", "lsf");
		Assert.assertEquals(0,errors.size());
		Assert.assertEquals(0,skips.size());
		//System.out.println(result);
	}
@Test
	public void testGetPolicies() {
		F fac = getFactory();
		if (!fac.isValid()) {
			return;
		}
		Set<PropertyContainerPolicy> s = fac.getPolicies();

		assertNotNull(s);

	}
@Test
	public void testGetMapper() {
		F fac = getFactory();
		if (!fac.isValid()) {
			return;
		}
		AccessorMap map = fac.getAccessorMap();

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
   
	
}