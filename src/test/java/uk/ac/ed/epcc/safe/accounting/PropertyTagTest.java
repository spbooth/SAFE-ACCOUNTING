// Copyright - The University of Edinburgh 2015
/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;

public class PropertyTagTest  {

	@Test
	public void testPattern(){
		PropertyRegistry reg = new PropertyRegistry("test","Test properties");
		
		new PropertyTag<>( reg,"good",String.class);
		try{
			new PropertyTag<>( reg,"bad.value",String.class);
			assertTrue(" Bad value fails",false);
		}catch(ConsistencyError e){
			
		}
	}
}