// Copyright - The University of Edinburgh 2015
/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.reports;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import uk.ac.ed.epcc.webapp.TestDataHelper;
import uk.ac.ed.epcc.webapp.forms.html.HTMLForm;
import uk.ac.ed.epcc.webapp.junit4.DataBaseFixtures;
public class PeriodExtensionTest extends ExtensionTestCase {

	
	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testDefaultPeriod() throws Exception {	
		String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
				"Aug", "Sep", "Oct", "Nov", "Dec"};
		Calendar start = Calendar.getInstance();	
		
		start.add(Calendar.MONTH, -1);
		String defaultPeriod = 
				"Default Period" + System.getProperty("line.separator") +
				"01-"+months[start.get(Calendar.MONTH)]+"-"+start.get(Calendar.YEAR);
		
		testPeriod("csv", defaultPeriod);
		
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testDefaultEndTimePeriod() throws Exception {	
		testPeriod("csv", new File(getOutputDir()+"DefaultEndTimePeriod.csv"));
		
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testDefaultNumberOfSplitsPeriod() throws Exception {	
		testPeriod("csv", new File(getOutputDir()+"DefaultNumberOfSplitsPeriod.csv"));
		
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testNumberOfSplitsPeriod() throws Exception {	
		testPeriod("csv", new File(getOutputDir()+"NumberOfSplitsPeriod.csv"));
		
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testTimeStampPeriod() throws Exception {	
		testPeriod("csv", new File(getOutputDir()+"TimeStampPeriod.csv"));
		
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testMonthPeriod() throws Exception {	
		testPeriod("csv", new File(getOutputDir()+"MonthPeriod.csv"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testYearInQuatersPeriod() throws Exception {	
		testPeriod("csv", new File(getOutputDir()+"YearInQuatersPeriod.csv"));

	}	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testSixOneMonthSplits() throws Exception {	
		testPeriod("csv", new File(getOutputDir()+"SixOneMonthSplits.csv"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testOneSixMonthSplit() throws Exception {	
		testPeriod("csv", new File(getOutputDir()+"OneSixMonthSplit.csv"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testOneHourPeriod() throws Exception {	
		testPeriod("csv", new File(getOutputDir()+"OneHourPeriod.csv"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testFormatPeriod() throws Exception {	
		testPeriod("csv", new File(getOutputDir()+"FormatPeriod.csv"));

	}
	protected void testPeriod(String reportType, File outputFile) throws Exception {
		testPeriod(reportType, TestDataHelper.readFileAsString(outputFile));
		
	}

	private void testPeriod(String type, String expectedOutput) throws Exception {		
		
		String templateName = "testPeriod";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();
		
		ReportBuilderTest.setupParams(ctx, params);
		
		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");
		ReportType reportType = reportBuilder.getReportType(type);
		params.put("ReportType", reportType);
		reportBuilder.setupExtensions(reportType,params);
		reportBuilder.buildReportParametersForm(form, params);
		
		// render the form
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
	
		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());

		System.out.println(out.toString());
		
		// Check it was correctly formatted.		
		assertTrue("Table wasn't correctly formatted:\n"+
				TestDataHelper.diff(expectedOutput, out.toString()),
				out.toString().contains(expectedOutput));
		
	}

}