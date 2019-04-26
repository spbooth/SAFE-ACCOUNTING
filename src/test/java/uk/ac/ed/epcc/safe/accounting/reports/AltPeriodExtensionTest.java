// Copyright - The University of Edinburgh 2015
/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.reports;

import java.io.File;
/** This duplicates all the period tests using templates with
 * an alternative date format.
 * 
 * @author spb
 *
 */
public class AltPeriodExtensionTest extends PeriodExtensionTest {

	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.reports.PeriodExtensionTest#testPeriod(java.lang.String, java.lang.String, java.io.File)
	 */
	@Override
	protected void testPeriod(String templateName, String reportType, File outputFile) throws Exception {
		super.testPeriod(templateName+"Alt", reportType, outputFile);
	}

	
	
	
}