// Copyright - The University of Edinburgh 2015
/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.expr;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.expr.DummyDataObjectFactory.DummyObject;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.reference.ReferencePropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.update.BatchParser;
import uk.ac.ed.epcc.webapp.WebappTestBase;
import uk.ac.ed.epcc.webapp.junit4.ConfigFixtures;
import uk.ac.ed.epcc.webapp.junit4.DataBaseFixtures;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;

@DataBaseFixtures({"DummyTable.xml"})
public class AdvancedParserTest extends WebappTestBase{
	
	@ConfigFixtures("parser.properties")
	@Test
	public void testParser() throws ParseException, InvalidPropertyException, DataFault{
		
		MultiFinder finder = new MultiFinder();
		finder.addFinder(StandardProperties.time);
		finder.addFinder(StandardProperties.base);
		finder.addFinder(BatchParser.batch);
		finder.addFinder(ReferencePropertyRegistry.getInstance(ctx));
		
		Parser p = new Parser(ctx,finder);
		//p.setDebug(10);
		
		String tests[] = {
			"@LOCATE(\"hello\",\"hello\",1)",
			"@LOCATE(\"el\",\"hello\",1)",
			"@LOCATE(\"el\",\"hello\",2)",
			"@LOCATE(\"el\",\"hello\",3)",
			"@LOCATE(\"el\",\"hello\",5)",
			"@LOCATE(\"el\",\"hello\",6)",
			"@LOCATE(\"lo\",\"hello\",1)",
			"@LOCATE(\"zz\",\"hello\",1)"
		};
		  
		DummyDataObjectFactory fac = new DummyDataObjectFactory(ctx);
		DummyObject obj = fac.makeBDO();
				
		DummyPropertyFactory prop_fac = new DummyPropertyFactory(ctx, DummyDataObjectFactory.TABLE_NAME);
		DummyPropertyContainer prop_con = new DummyPropertyContainer(prop_fac, obj.getRecord());
		
		for( String t : tests){
			System.out.println(t);
			PropExpression expr = p.parse(t);
			
			try {
				Object res = expr.accept(prop_con.getEvaluator());
				System.out.println("  ->"+expr.toString()+"="+res.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}