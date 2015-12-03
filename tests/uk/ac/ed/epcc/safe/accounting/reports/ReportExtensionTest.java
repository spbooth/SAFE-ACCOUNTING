// Copyright - The University of Edinburgh 2015
/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.reports;


import static org.junit.Assert.*;

import java.text.ParseException;

import org.junit.Test;

public class ReportExtensionTest{

	@Test
	  public void testParseNumber() throws ParseException{
		  Number m = ReportExtension.parseNumberWithDef(null, "17,299,431,703");
		  assertNotNull(m);
		  assertEquals(m.doubleValue(), 17299431703.0,0.0);
	  }
}