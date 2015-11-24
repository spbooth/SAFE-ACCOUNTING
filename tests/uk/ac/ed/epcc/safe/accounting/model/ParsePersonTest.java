/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.model;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.ErrorSet;
import uk.ac.ed.epcc.safe.accounting.db.ClassificationUpdater;
import uk.ac.ed.epcc.safe.accounting.parsers.PasswdParser;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
public class ParsePersonTest extends PersonTest {
	

	/** This is the test for the data upload to Person.
	 * We subclass PersonTest as this test is not applicable to
	 * CertificateTest that also sub-classes PersonTest
	 * 
	 * @throws InvalidPropertyException
	 * @throws DataException
	 */
	@Test
	public void testUpload() throws InvalidExpressionException, DataException{
		PropertyPersonFactory<PropertyPerson> fac = new PropertyPersonFactory<PropertyPerson>(ctx);
		ClassificationUpdater<PropertyPerson> updater = new ClassificationUpdater<PropertyPerson>(ctx, fac);
		
		ErrorSet errors = new ErrorSet();
    	ErrorSet skip_list = new ErrorSet();
    	Map<String,Object>  params = new HashMap<String,Object>();
    	String update = "sbooth:x:73964:4047:Stephen Booth:/exports/home/sbooth:/bin/bash\n" +
    			"millingw:x:309546:4047:Malcolm Illingworth:/exports/home/millingw:/bin/bash\n";
    	String result = updater.receiveData(params, update, errors, skip_list);
    	System.out.println(result);
    	assertEquals(2, updater.getLineCount());
    	assertEquals(2, updater.getUpdateCount());
    	
    	PropertyPerson p = fac.makeFromString("sbooth");
    	assertEquals(p.getProperty(PasswdParser.GECOS), "Stephen Booth");
	}

}
