// Copyright - The University of Edinburgh 2015
/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.reports;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import uk.ac.ed.epcc.webapp.TestDataHelper;
import uk.ac.ed.epcc.webapp.forms.html.HTMLForm;
import uk.ac.ed.epcc.webapp.junit4.DataBaseFixtures;
public class TableExtensionTest extends ExtensionTestCase {
 
	@Test
	@DataBaseFixtures ({"Eddie.xml"})
	public void testSummaryObjectTable() throws IOException, Exception{
		testTable("html","testSummaryObjectTable.xml",TestDataHelper.readFileAsString(
				new File(getOutputDir()+"SummaryObjectTable.html")));
	}
	
	
	//TODO break up template per test to make tests run quicker and debugging easier
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testHTMLTable() throws Exception {
		testTables("html", new File(getOutputDir()+"Table.html"));
		
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testPDFTable() throws Exception {
		testTables("fop", new File(getOutputDir()+"Table.fop"));
		
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testCSVTable() throws Exception {
		testTables("csv", new File(getOutputDir()+"Table.csv"));
		
	}
	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testXMLTable() throws Exception {
		testTables("xml", new File(getOutputDir()+"Table.xml"));
		
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testTableFormatting() throws Exception {	
		testTables("csv", "testTableWithFormatting.xml",new File(getOutputDir()+"TableWithFormatting.csv"));
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testHTMLSummaryTable() throws Exception {
		testTables("html","testSummaryTable.xml", new File(getOutputDir()+"SummaryTable.html"));
		
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testPDFSummaryTable() throws Exception {
		testTables("fop", "testSummaryTable.xml",new File(getOutputDir()+"SummaryTable.fop"));
		
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testCSVSummaryTable() throws Exception {
		testTables("csv", "testSummaryTable.xml",new File(getOutputDir()+"SummaryTable.csv"));
		
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testXMLSummaryTable() throws Exception {
		testTables("xml", "testSummaryTable.xml",new File(getOutputDir()+"SummaryTable.xml"));
		
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testSummaryTableExpressionIndex() throws Exception {
		testTables("csv", "testSummaryTableExpressionIndex.xml",new File(getOutputDir()+"SummaryTableExpressionIndex.csv"));
		
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testSummaryTableExpressionIndexLabeller() throws Exception {
		testTables("csv", "testSummaryTableExpressionIndexLabeller.xml",new File(getOutputDir()+"SummaryTableExpressionIndexLabeller.csv"));
		
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testTableWithDerviedProperty() throws Exception {	
		testTables("csv", "testSummaryTableWithDerivedProperty.xml",new File(getOutputDir()+"SummaryTableWithDerviedProperty.csv"));
			
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testTableWithDoubleDerviedProperty() throws Exception {	
		testTables("csv", "testSummaryTableWithDoubleDerivedProperty.xml",new File(getOutputDir()+"SummaryTableWithDoubleDerviedProperty.csv"));
		
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testSummaryTableStringData() throws Exception {	
		testTables("csv", "testSummaryTableWithStringProperty.xml",new File(getOutputDir()+"SummaryTableWithStringData.csv"));
		
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testSummaryTableWithDate() throws Exception {	
		testTables("csv", "testDateTable.xml",new File(getOutputDir()+"SummaryTableWithDate.csv"));
		
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testSummaryTableWithOverlap() throws Exception {	
		testTables("csv", "testDateTableWithOverlap.xml",new File(getOutputDir()+"SummaryTableWithOverlap.csv"));
		
	}	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testCompoundSimpleTable() throws Exception {	
		testTables("csv", "testCompoundSimpleTable",new File(getOutputDir()+"CompoundSimpleTable.csv"));
		
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testCompoundSummaryTable() throws Exception {	
		testTables("csv", "testCompoundSummaryTable.xml",new File(getOutputDir()+"CompoundSummaryTable.csv"));
		
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testObjectTable() throws Exception {	
		testTables("csv", "testObjectTable.xml",new File(getOutputDir()+"ObjectTable.csv"));
		
	}
	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testNameExpressionTable() throws Exception {	
		testTables("csv", "testSlowdownReport.xml",new File(getOutputDir()+"SlowdownReport.csv"));
		
	}
	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testMedianExpressionTable() throws Exception {	
		testTables("csv", "testMedianSlowdownReport.xml",new File(getOutputDir()+"MedianSlowdownReport.csv"));
		
	}
	protected void testTables(String reportType, File outputFile) throws Exception {
		testTables(reportType, "testTable",outputFile);
		
	}
	
	protected void testTables(String reportType, String templateName,File outputFile) throws Exception {
		//System.out.println("Type="+reportType+" template="+templateName+" expected output in "+outputFile);
		
		testTable(reportType,templateName ,TestDataHelper.readFileAsString(outputFile).replaceAll("<!--.*-->\\s*\n", ""));
		
	}
	
	protected void testTable(String type, String templateName, String expectedOutput) throws Exception {	
			
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<>();
		
		ReportBuilderTest.setupParams(ctx,params);
		
		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");
		ReportType reportType = reportBuilder.getReportTypeReg().getReportType(type);
		params.put("ReportType", reportType);
		reportBuilder.setupExtensions(params);
		reportBuilder.buildReportParametersForm(form, params);
		
		// render the form
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
	    String outstring=normalise(out.toString());
	    expectedOutput=normalise(expectedOutput);
//		PrintWriter tmp= new PrintWriter("/tmp/scratch");
//	    tmp.println(out.toString());
//	    tmp.close();
		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
		System.out.println(out.toString());
		for(String s : expectedOutput.replace("><",">\n<").split("\r?\n")){
			assertTrue("Output did not contain \n"+s.trim()+"\n",
					outstring.contains(s.trim()));
			assertTrue("Output has unexpected leading/trailing spaces \n"+s+"\n",
					outstring.contains(s));
		}
		// Check it was correctly formatted.
		assertTrue("Summary table was permuted\n-------\n"+out.toString()+
				"\nexpected\n-----\n"+expectedOutput+"\n---------\n"+
				TestDataHelper.diff(expectedOutput, out.toString()+"\n---------\n"), 
				outstring.contains(expectedOutput));
		
	}


	
	
	

}