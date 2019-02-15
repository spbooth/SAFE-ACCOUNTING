// Copyright - The University of Edinburgh 2015
/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.reports;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import uk.ac.ed.epcc.webapp.TestDataHelper;
import uk.ac.ed.epcc.webapp.forms.html.HTMLForm;
import uk.ac.ed.epcc.webapp.junit4.DataBaseFixtures;
public class FilterExtensionTest extends ExtensionTestCase {

	
	
	

	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testFilterNameParse() throws Exception {			
		testFilter("csv", "testNameParseFilters.xml",
				new File(getOutputDir()+"FilterNameParse.csv"));
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testFilterIdParse() throws Exception {			
		testFilter("csv", "testIdParseFilters.xml",
				new File(getOutputDir()+"FilterIdParse.csv"));
	}
	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testFilterEQ() throws Exception {			
		testFilter("csv", "testEQFilters.xml",
				new File(getOutputDir()+"FilterEQ.csv"));
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testFilterNE() throws Exception {			
		testFilter("csv", "testNEFilters.xml",
				new File(getOutputDir()+"FilterNE.csv"));
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testFilterGT() throws Exception {			
		testFilter("csv", "testGTFilters.xml",
				new File(getOutputDir()+"FilterGT.csv"));
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testFilterGE() throws Exception {			
		testFilter("csv", "testGEFilters.xml",
				new File(getOutputDir()+"FilterGE.csv"));
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testFilterLT() throws Exception {			
		testFilter("csv", "testLTFilters.xml",
				new File(getOutputDir()+"FilterLT.csv"));
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testFilterLE() throws Exception {			
		testFilter("csv", "testLEFilters.xml",
				new File(getOutputDir()+"FilterLE.csv"));
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testFilterAnd() throws Exception {			
		testFilter("csv", "testAndFilters.xml",
				new File(getOutputDir()+"FilterAnd.csv"));
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testFilterOr() throws Exception {			
		testFilter("csv", "testOrFilters.xml",
				new File(getOutputDir()+"FilterOr.csv"));
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testFilterWithDerivedProperty() throws Exception {			
		testFilter("csv", "testDerivedPropertyFilters",
				new File(getOutputDir()+"FilterWithDerivedProperty.csv"));
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testFilterWithDoubleDerivedProperty() throws Exception {			
		testFilter("csv", "testDoubleDerivedFilters.xml",
				new File(getOutputDir()+"FilterWithDoubleDerivedProperty.csv"));
	}
	
	

	protected void testFilter(String reportType, String templateName,File outputFile) 
	throws Exception 
	{		
		testFlter(reportType, templateName,TestDataHelper.readFileAsString(outputFile));
	
	}
	protected void testFlter(String type, String templateName,String expectedOutput)
		throws Exception 
	{
	
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String, Object> params = new HashMap<>();
		
		ReportBuilderTest.setupParams(ctx, params);
		
		ReportBuilder reportBuilder = 
			new ReportBuilder(ctx, templateName,"report.xsd");
		ReportType reportType=reportBuilder.getReportTypeReg().getReportType(type);
		params.put(ReportTypeRegistry.REPORT_TYPE_PARAM, reportType);
		reportBuilder.setupExtensions(reportType,params);
		reportBuilder.buildReportParametersForm(form, params);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
		
		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
		//System.out.println(out.toString());
		// Check it was correctly formatted.
		String outstring = normalise(out.toString());
		expectedOutput=normalise(expectedOutput);
		assertTrue("Report wasn't correctly formatted:\n"+
				TestDataHelper.diff(expectedOutput, outstring),
				TestDataHelper.compareUnordered(outstring, expectedOutput));

	}

}