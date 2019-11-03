// Copyright - The University of Edinburgh 2015
/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.expr;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.db.AccountingClassification;
import uk.ac.ed.epcc.safe.accounting.db.AccountingClassificationFactory;
import uk.ac.ed.epcc.safe.accounting.expr.parse.FormatVisitor;
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
  public void testParser() throws Exception{
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
		"@REF(thing,"+boris.getID()+")["+AccountingClassificationFactory.DESCRIPTION_PROP.getName()+"]",
		"@NAME(@REF(thing,"+boris.getID()+"))",
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
		"@DURATION("+StandardProperties.STARTED_PROP.toString()+","+StandardProperties.ENDED_PROP.toString()+")",
		"@GREATEST("+StandardProperties.STARTED_PROP.toString()+","+StandardProperties.ENDED_PROP.toString()+")",
		"@LEAST("+StandardProperties.STARTED_PROP.toString()+","+StandardProperties.ENDED_PROP.toString()+")",
		"{"+StandardProperties.STARTED_PROP.toString()+","+StandardProperties.ENDED_PROP.toString()+"}",
		 
		"@DATE(@MILLIS("+StandardProperties.STARTED_PROP.toString()+"))",
	    "@STRING(99)",
		"\"hello world\"",
		"@DURATION_CAST(300)",
        "@DOUBLE_CAST(300)",
        "@LONG_CAST(300)",
		"99",
		"0 == 1",
		"true",
		"False"
		
	  };
	  FormatVisitor vis = new FormatVisitor();
	  for( String t : tests){
		  //System.out.println(t);
		  PropExpression<?> e = p.parse(t);
		  //System.out.println("  ->"+e.toString());
		  
		  // brackets and spacesmay not match onserialise but at least
		  // check rount trip works
		  
		  String formatted = e.accept(vis);
		  System.out.println(t+" -> "+formatted);
		  //assertEquals(normalise(t), normalise(formatted));
		  assertEquals(e, p.parse(e.accept(vis)));
	  }
  }
	
  public String normalise(String in) {
	  return in.replaceAll("\\s|\\(|\\)", "");
  }
}