/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.model;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.session.AppUserFactoryTestCase;

public class PersonTest extends AppUserFactoryTestCase {
	
	

	@Override
	public DataObjectFactory getFactory() {
		return new PropertyPersonFactory(ctx);
	}
	@Test
	public void testParser(){
		PropertyPersonFactory<PropertyPerson> fac = new PropertyPersonFactory<PropertyPerson>(ctx);
		
		assertNotNull(fac.getParser());
	}
	
	
}
