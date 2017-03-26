// Copyright - The University of Edinburgh 2015
/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import uk.ac.ed.epcc.webapp.forms.exceptions.ParseException;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;

public class CertificatePersonTest extends PersonTest {
	

	@Override
	public DataObjectFactory getFactory() {
		return new CertificatePersonFactory(ctx,"CertificatePerson");
	}

	@Test
	public void testGetName() throws DataException, ParseException{
		CertificatePersonFactory<?> fac = (CertificatePersonFactory) getFactory();
		
		CertificatePerson p = fac.makeFromString("/C=UK/O=eScience/OU=Edinburgh/L=NeSC/CN=stephen booth");
		assertEquals( "stephen booth", p.getName());
		
	}
	@Test
	public void testNormalise() throws DataException, ParseException{
	CertificatePersonFactory<?> fac = (CertificatePersonFactory) getFactory();
		
		CertificatePerson p = fac.makeFromString("/C=UK/O=eScience/OU=Edinburgh/L=NeSC/CN=stephen booth");
	     p.commit();
	     
	     CertificatePerson q = fac.findFromString("CN=stephen booth,L=NeSC,OU=Edinburgh,O=eScience,C=UK");
	     assertNotNull(q);
	     assertTrue(p.equals(q));
		
	}
	@Test
	public void testReversible(){
		CertificatePersonFactory<?> fac = (CertificatePersonFactory) getFactory();
		String web[]={
				"/C=UK/O=eScience/OU=Edinburgh/L=NeSC/CN=stephen booth",
				"/C=PL/O=GRID/O=PSNC/CN=Bartosz Bosak"
		};
		String ldap[] = {
				"CN=stephen booth,L=NeSC,OU=Edinburgh,O=eScience,C=UK",
				"CN=Bartosz Bosak,O=PSNC,O=GRID,C=PL"
		};
		
		for( int i = 0 ; i< web.length ; i++){
			assertEquals(web[i], DNNameFinder.toWebFormat(ldap[i]));
			assertEquals(web[i], DNNameFinder.toWebFormat(web[i]));
			assertEquals(ldap[i], DNNameFinder.toLdapFormat(ldap[i]));
			assertEquals(ldap[i], DNNameFinder.toLdapFormat(web[i]));
		}
		
	}
}