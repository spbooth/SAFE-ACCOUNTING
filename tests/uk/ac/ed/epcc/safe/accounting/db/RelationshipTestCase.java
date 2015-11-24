/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.db;

import org.junit.Test;

import uk.ac.ed.epcc.webapp.WebappTestBase;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.relationship.Relationship;


public class RelationshipTestCase extends WebappTestBase {
	
	
	 @Test
	public void testCreate() throws DataFault{
		 Relationship.makeTable(ctx, "Relationship", "School");
	 }
	 
	 
}
