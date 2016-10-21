// Copyright - The University of Edinburgh 2015
/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.expr;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.expr.DummyDataObjectFactory.DummyObject;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.reference.ReferencePropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.update.BatchParser;
import uk.ac.ed.epcc.webapp.WebappTestBase;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLValue;
import uk.ac.ed.epcc.webapp.jdbc.filter.SQLFilter;
import uk.ac.ed.epcc.webapp.junit4.ConfigFixtures;
import uk.ac.ed.epcc.webapp.junit4.DataBaseFixtures;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.filter.SelfReferenceFilter;

@DataBaseFixtures({"DummyTable.xml"})
public class AdvancedParserTest extends WebappTestBase{
	public class LocateData{
		public LocateData(String search, String data, int offset, int expected) {
			super();
			this.search = search;
			this.data = data;
			this.offset = offset;
			this.expected = expected;
		}
		public final String search;
		public final String data;
		public final int offset;
		public final int expected;
		
		public String getConstExpression(){
			return "@LOCATE(\""+search+"\",\""+data+"\","+offset+")";
		}
	}
	
	LocateData data[] = {
			new LocateData("hello","hello",1,1),
			new LocateData("el","hello",1,2),
			new LocateData("el","hello",2,2),
			new LocateData("el","hello",3,0),
			new LocateData("el","hello",5,0),
			new LocateData("el","hello",6,0),
			new LocateData("lo","hello",1,4),
			new LocateData("zz","hello",1,0),
			
		};
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
	@ConfigFixtures("parser.properties")
	@Test
	public void testConstantExpressions() throws Exception{
		
		
		DummyPropertyFactory prop_fac = new DummyPropertyFactory(ctx);
		Parser p = new Parser(ctx,prop_fac.getFinder());
		
		
		DummyPropertyContainer prop_con = prop_fac.makeBDO();
		prop_con.setData("Boris");
		prop_con.commit(); // Need to have one record
		
		for( LocateData d : data){
			System.out.println(d.getConstExpression());
			PropExpression expr = p.parse(d.getConstExpression());
			Object res = prop_con.evaluateExpression(expr);
			assertEquals(d.expected,((Number)res).intValue());
			System.out.println("  ->"+d.getConstExpression()+"="+res.toString());
			
			SQLExpression sql_expr = prop_fac.getAccessorMap().getSQLExpression(expr);
			res = prop_fac.evaluate(null, sql_expr);
			assertNotNull("SQLSxpression returns null",res);
			assertEquals("Unexpected result from SQLExpression",d.expected,((Number)res).intValue());
			
			
			SQLValue sql_val = prop_fac.getAccessorMap().getSQLValue(expr);
			res = prop_fac.evaluate(null, sql_val);
			assertNotNull("SQLValue returns null",res);
			assertEquals("Unexpected result from SQLValue",d.expected,((Number)res).intValue());	
			
		}
		
	}
	
	@Test
	public void testFieldExpressions() throws Exception{
		
		
		DummyPropertyFactory prop_fac = new DummyPropertyFactory(ctx);
		Parser p = new Parser(ctx,prop_fac.getFinder());
		Map<Integer,LocateData> expected = new HashMap<>();
		
		for( LocateData d : data){
			DummyPropertyContainer prop_con = prop_fac.makeBDO();
			prop_con.setData(d.data);
			prop_con.setSearch(d.search);
			prop_con.setOffset(d.offset);
			prop_con.commit(); 
			expected.put(prop_con.getID(),d); // remember expected vaue
		}
		// Use field names these should map to the corresponding property names by default.
		String expr_text = "@LOCATE("+DummyPropertyFactory.SEARCH_FIELD+","+DummyPropertyFactory.DATA_FIELD+","+DummyPropertyFactory.OFFSET_FIELD+")";
		PropExpression expr = p.parse(expr_text);
		SQLExpression sql_expr = prop_fac.getAccessorMap().getSQLExpression(expr);
		SQLValue sql_val = prop_fac.getAccessorMap().getSQLValue(expr);
		for(Integer id : expected.keySet()){
			DummyPropertyContainer prop_con = prop_fac.find(id);
			LocateData d = expected.get(id);
			
			Object res = prop_con.evaluateExpression(expr);
			assertEquals(d.expected,((Number)res).intValue());
			System.out.println("  ->"+expr_text+"="+res.toString());
			
			// Filter to select the target record in SQL
			SQLFilter<DummyPropertyContainer> fil = prop_fac.getRefereceFilter(prop_fac.makeReference(prop_con));
			res = prop_fac.evaluate(fil, sql_expr);
			assertNotNull("SQLSxpression returns null",res);
			assertEquals("Unexpected result from SQLExpression",d.expected,((Number)res).intValue());
			
			
			res = prop_fac.evaluate(fil, sql_val);
			assertNotNull("SQLValue returns null",res);
			assertEquals("Unexpected result from SQLValue",d.expected,((Number)res).intValue());	
			
		}
		
	}
	
}