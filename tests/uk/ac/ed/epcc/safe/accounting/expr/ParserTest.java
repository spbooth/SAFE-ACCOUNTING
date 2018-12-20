// Copyright - The University of Edinburgh 2015
/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.expr;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.db.AccountingClassification;
import uk.ac.ed.epcc.safe.accounting.db.AccountingClassificationFactory;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.reference.ReferencePropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.update.BatchParser;
import uk.ac.ed.epcc.webapp.WebappTestBase;
import uk.ac.ed.epcc.webapp.junit4.ConfigFixtures;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedProducer;


public class ParserTest extends WebappTestBase{

	@ConfigFixtures("parser.properties")
	@Test
  public void testParser() throws ParseException, InvalidPropertyException, DataFault{
	  // ensure test props are loaded
	  MultiFinder finder = new MultiFinder();
	  finder.addFinder(StandardProperties.time);
	  finder.addFinder(StandardProperties.base);
	  finder.addFinder(BatchParser.batch);
	  finder.addFinder(ReferencePropertyRegistry.getInstance(ctx));
	  AccountingClassificationFactory thing_fac = (AccountingClassificationFactory) ctx.makeObject(IndexedProducer.class,"thing");
	  AccountingClassification boris = (AccountingClassification) thing_fac.makeFromString("boris");
	  boris.commit();
	  
	  Parser p = new Parser(ctx,finder);
	  //p.setDebug(10);
	  String tests[] = {
		BatchParser.NODE_COUNT_PROP+" > 7",	  
		"@REF( thing , boris  )",
		"@REF(thing,"+boris.getID()+")",
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
		
		"99",
		"0 == 1",
		"true",
		"False"
		
	  };
	  for( String t : tests){
		  //System.out.println(t);
		  PropExpression e = p.parse(t);
		  //System.out.println("  ->"+e.toString());

	  }
  }
}