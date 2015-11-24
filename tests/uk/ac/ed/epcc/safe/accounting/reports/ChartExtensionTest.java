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

public class ChartExtensionTest extends ExtensionTestCase {

	

	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testHTMLTimeChartPlot() throws Exception {
		testChart("html", new File(getOutputDir()+"TimeChart.html"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testPDFTimeChartPlot() throws Exception {
		testChart("fop", new File(getOutputDir()+"TimeChart.fop"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testCSVTimeChartPlot() throws Exception {
		testChart("csv", new File(getOutputDir()+"TimeChart.csv"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testXMLTimeChartPlot() throws Exception {
		testChart("xml", new File(getOutputDir()+"TimeChart.xml"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testHTMLTimeChartNoGroupPlot() throws Exception {
		testChart("html", new File(getOutputDir()+"TimeChartNoGroup.html"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testPDFTimeChartNoGroupPlot() throws Exception {
		testChart("fop", new File(getOutputDir()+"TimeChartNoGroup.fop"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testCSVTimeChartNoGroupPlot() throws Exception {
		testChart("csv", new File(getOutputDir()+"TimeChartNoGroup.csv"));
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testXMLTimeChartNoGroupPlot() throws Exception {
		testChart("xml", new File(getOutputDir()+"TimeChartNoGroup.xml"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testHTMLPieTimeChartPlot() throws Exception {
		testChart("html", new File(getOutputDir()+"PieTimeChart.html"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testCSVTimeChartCumilative() throws Exception {
		testChart("csv", new File(getOutputDir()+"TimeChartCumulative.csv"));
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testPDFPieTimeChartPlot() throws Exception {
		testChart("fop", new File(getOutputDir()+"PieTimeChart.fop"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testCVSPieTimeChartPlot() throws Exception {
		testChart("csv", new File(getOutputDir()+"PieTimeChart.csv"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testXMLPieTimeChartPlot() throws Exception {
		testChart("xml", new File(getOutputDir()+"PieTimeChart.xml"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testHTMLBarTimeChartPlot() throws Exception {
		testChart("html", new File(getOutputDir()+"BarTimeChart.html"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testPDFBarTimeChartPlot() throws Exception {
		testChart("fop", new File(getOutputDir()+"BarTimeChart.fop"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testCVSBarTimeChartPlot() throws Exception {
		testChart("csv", new File(getOutputDir()+"BarTimeChart.csv"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testXMLBarTimeChartPlot() throws Exception {
		testChart("xml", new File(getOutputDir()+"BarTimeChart.xml"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testPlotWithDerviedProperty() throws Exception {
		testChart("csv", new File(getOutputDir()+"TimeChartWithDerviedProperty.csv"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testPlotWithDoubleDerviedProperty() throws Exception {
		testChart("csv", new File(getOutputDir()+"TimeChartWithDoubleDerviedProperty.csv"));

	}
	
//	public void testPlotOfAllocation() throws Exception {
//		testChart(ReportType.CSV, new File(dir+"AllocationPlot.csv"));
//
//	}
	
	protected void testChart(String reportType, File outputFile) throws Exception {
		testChart(reportType, TestDataHelper.readFileAsString(outputFile));
		
	}	
	
	protected void testChart(String type, String expectedOutput)
			throws Exception {

		String templateName = "testCharts";

		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);

		// Get the params values from the Form
		Map<String, Object> params = new HashMap<String, Object>();
		
		ReportBuilderTest.setupParams(ctx, params);

		ReportBuilder reportBuilder = new ReportBuilder(ctx, templateName,
				"report.xsd");
		ReportType reportType=reportBuilder.getReportType(type);
		params.put(ReportBuilder.REPORT_TYPE_PARAM, reportType);
		reportBuilder.setupExtensions(reportType,params);
		reportBuilder.buildReportParametersForm(form, params);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);

		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());

		System.out.println(out.toString());

		// Check it was correctly formatted.
		String output = out.toString().replace(ctx.getInitParameter("java.io.tmpdir","/tmp"), "/tmp");
		assertTrue("Report wasn't correctly formatted:\n"+
				TestDataHelper.diff(expectedOutput, output),
				output.contains(expectedOutput));

	}

	
}
