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
import uk.ac.ed.epcc.webapp.TestTimeService;
import uk.ac.ed.epcc.webapp.forms.html.HTMLForm;
public class PeriodExtensionTest extends ExtensionTestCase {

	@Test
	public void testDefaultPeriod() throws Exception {
		
		// Has to resolve data in period
		TestTimeService serv = new TestTimeService();
		Calendar start = Calendar.getInstance();
		start.set(Calendar.YEAR, 2008);
		start.set(Calendar.MONTH,Calendar.NOVEMBER);
		serv.setResult(start.getTime());
		ctx.setService(serv);
		
		String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
				"Aug", "Sep", "Oct", "Nov", "Dec"};
		//Calendar start = Calendar.getInstance();	
		
		start.add(Calendar.MONTH, -1);
		String defaultPeriod = 
				"Default Period" + System.getProperty("line.separator") +
				"01-"+months[start.get(Calendar.MONTH)]+"-"+start.get(Calendar.YEAR);
		
		testPeriod("testDefaultPeriod","csv", defaultPeriod);
		
	}
	@Test
	public void testDefaultEndTimePeriod() throws Exception {	
		testPeriod("testDefaultEndPeriod","csv", new File(getOutputDir()+"DefaultEndTimePeriod.csv"));
		
	}
	@Test
	public void testDefaultNumberOfSplitsPeriod() throws Exception {	
		testPeriod("testDefaultNumSplitPeriod","csv", new File(getOutputDir()+"DefaultNumberOfSplitsPeriod.csv"));
		
	}
	@Test
	public void testNumberOfSplitsPeriod() throws Exception {	
		testPeriod("testNumSplitPeriod","csv", new File(getOutputDir()+"NumberOfSplitsPeriod.csv"));
		
	}
	@Test
	public void testTimeStampPeriod() throws Exception {	
		testPeriod("testTimestampPeriod","csv", new File(getOutputDir()+"TimeStampPeriod.csv"));
		
	}
	@Test
	public void testMonthPeriod() throws Exception {	
		testPeriod("testMonthPeriod","csv", new File(getOutputDir()+"MonthPeriod.csv"));

	}
	@Test
	public void testYearInQuartersPeriod() throws Exception {	
		testPeriod("testYearInQuartersPeriod","csv", new File(getOutputDir()+"YearInQuatersPeriod.csv"));

	}	
	@Test
	public void testSixOneMonthSplits() throws Exception {	
		testPeriod("testSixOneMonthSplitsPeriod","csv", new File(getOutputDir()+"SixOneMonthSplits.csv"));

	}
	@Test
	public void testOneSixMonthSplit() throws Exception {	
		testPeriod("testOneSixMonthSplitPeriod","csv", new File(getOutputDir()+"OneSixMonthSplit.csv"));

	}
	@Test
	public void testOneHourPeriod() throws Exception {	
		testPeriod("testOneHourPeriod","csv", new File(getOutputDir()+"OneHourPeriod.csv"));

	}
	@Test
	public void testFormatPeriod() throws Exception {	
		testPeriod("testFormatPeriod","csv", new File(getOutputDir()+"FormatPeriod.csv"));

	}
	
	protected void testPeriod(String templateName,String reportType, File outputFile) throws Exception {
		testPeriod(templateName,reportType, TestDataHelper.readFileAsString(outputFile));
		
	}
	private void testPeriod(String templateName,String type, String expectedOutput) throws Exception {		
		
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();
		
		ReportBuilderTest.setupParams(ctx, params);
		
		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");
		ReportType reportType = reportBuilder.getReportTypeReg().getReportType(type);
		params.put("ReportType", reportType);
		reportBuilder.setupExtensions(reportType,params);
		reportBuilder.buildReportParametersForm(form, params);
		
		// render the form
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
	
		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());

		//System.out.println(out.toString());
		
		// Check it was correctly formatted.		
		assertTrue("Table wasn't correctly formatted:\n"+
				TestDataHelper.diff(expectedOutput, out.toString()),
				out.toString().replaceAll("\r?\n", "\n").contains(expectedOutput.replaceAll("\r?\n", "\n")));
		
	}

}