// Copyright - The University of Edinburgh 2015
/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.expr;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.update.BatchParser;
import uk.ac.ed.epcc.webapp.WebappTestBase;

public class ParserTest extends WebappTestBase{

	
	@Test
  public void testParser() throws ParseException, InvalidPropertyException{
	  // ensure test props are loaded
	  MultiFinder finder = new MultiFinder();
	  finder.addFinder(StandardProperties.time);
	  finder.addFinder(StandardProperties.base);
	  finder.addFinder(BatchParser.batch);
	  Parser p = new Parser(ctx,finder);
	  String tests[] = {
		StandardProperties.CPU_TIME_PROP.toString(),
		StandardProperties.CPU_TIME_PROP.toString()+"*3",
		StandardProperties.ENDED_PROP.toString()+"-"+StandardProperties.STARTED_PROP.toString(),
		"("+BatchParser.REQUESTED_CPUS_PROP.toString()+"*("+
		StandardProperties.ENDED_PROP.toString()+"-"+StandardProperties.STARTED_PROP.toString()+
		"))/1000",
		"1000*"+StandardProperties.ENDED_PROP.toString()+"-"+"1000*"+StandardProperties.STARTED_PROP.toString(),
		
		"{"+BatchParser.NODE_COUNT_PROP.toString()+","+BatchParser.PROC_COUNT_PROP.toString()+"}",
		"@INT(30/7)",
		"@INT("+StandardProperties.ENDED_PROP.toString()+"-"+StandardProperties.STARTED_PROP.toString()+")",
		BatchParser.NODE_COUNT_PROP+" > 7"
	  };
	  for( String t : tests){
		  System.out.println(t);
		  PropExpression e = p.parse(t);
		  System.out.println("  ->"+e.toString());

	  }
  }
}