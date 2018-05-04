// Copyright - The University of Edinburgh 2015
/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.reports;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.junit.Test;
import org.xml.sax.SAXException;

import uk.ac.ed.epcc.webapp.TestDataHelper;
import uk.ac.ed.epcc.webapp.exceptions.InvalidArgument;
import uk.ac.ed.epcc.webapp.forms.html.HTMLForm;
import uk.ac.ed.epcc.webapp.junit4.DataBaseFixtures;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;

public class ChartExtensionTest extends ExtensionTestCase {

	

	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testHTMLTimeChartPlot() throws Exception {
		testChart("testTimeCharts.xml","html", new File(getOutputDir()+"TimeChart.html"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testPDFTimeChartPlot() throws Exception {
		testChart("testTimeCharts.xml","fop", new File(getOutputDir()+"TimeChart.fop"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testCSVTimeChartPlot() throws Exception {
		testChart("testTimeCharts.xml","csv", new File(getOutputDir()+"TimeChart.csv"));

	}
	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testBadTimeChartPlot() throws Exception {
		String template = "testBadTimeCharts.xml";
		ReportBuilder reportBuilder = runBadTemplate(template);
		
		// Look for errors
		Set<String> errors = ReportBuilderTest.expectErrors(2, reportBuilder.getErrors());
		assertTrue(errors.contains("Bad PlotEntry"));
		assertTrue(errors.contains("Error parsing plot expression"));
		//System.out.println(out.toString());
		
	}
	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testBadTimeChartGroup() throws Exception {
		String template = "testBadTimeCharts2.xml";
		ReportBuilder reportBuilder = runBadTemplate(template);
		
		// Look for errors
		Set<String> errors = ReportBuilderTest.expectErrors(1, reportBuilder.getErrors());
		assertTrue(errors.contains("Error parsing group tag/expression"));
		//System.out.println(out.toString());
		
	}
	/**
	 * @param template
	 * @return
	 * @throws ParserConfigurationException
	 * @throws DataFault
	 * @throws InvalidArgument
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 * @throws SAXException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws Exception
	 */
	private ReportBuilder runBadTemplate(String template)
			throws ParserConfigurationException, DataFault, InvalidArgument, TransformerFactoryConfigurationError,
			TransformerException, SAXException, IOException, URISyntaxException, Exception {
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String, Object> params = new HashMap<String, Object>();
		
		ReportBuilderTest.setupParams(ctx, params);
		
		
		ReportBuilder reportBuilder = new ReportBuilder(ctx, template,
				"report.xsd");
		ReportType reportType=reportBuilder.getReportTypeReg().getReportType("csv");
		params.put(ReportTypeRegistry.REPORT_TYPE_PARAM, reportType);
		reportBuilder.setupExtensions(reportType,params);
		reportBuilder.buildReportParametersForm(form, params);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
		return reportBuilder;
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testXMLTimeChartPlot() throws Exception {
		testChart("testTimeCharts.xml","xml", new File(getOutputDir()+"TimeChart.xml"));

	}
	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testNestedXMLTimeChartPlot() throws Exception {
		testChart("testChartsNested.xml","xml", new File(getOutputDir()+"NestedTimeChart.xml"));

	}
	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testAddDataXMLTimeChartPlot() throws Exception {
		testChart("testChartsAddData.xml","xml", new File(getOutputDir()+"AddDataTimeChart.xml"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testHTMLTimeChartNoGroupPlot() throws Exception {
		testChart("testTimeChartsNoGroup.xml","html", new File(getOutputDir()+"TimeChartNoGroup.html"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testPDFTimeChartNoGroupPlot() throws Exception {
		testChart("testTimeChartsNoGroup.xml","fop", new File(getOutputDir()+"TimeChartNoGroup.fop"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testCSVTimeChartNoGroupPlot() throws Exception {
		testChart("testTimeChartsNoGroup.xml","csv", new File(getOutputDir()+"TimeChartNoGroup.csv"));
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testXMLTimeChartNoGroupPlot() throws Exception {
		testChart("testTimeChartsNoGroup.xml","xml", new File(getOutputDir()+"TimeChartNoGroup.xml"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testHTMLPieTimeChartPlot() throws Exception {
		testChart("testPieTimeCharts.xml","html", new File(getOutputDir()+"PieTimeChart.html"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testCSVTimeChartCumulative() throws Exception {
		testChart("testTimeChartCummulative.xml","csv", new File(getOutputDir()+"TimeChartCumulative.csv"));
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testPDFPieTimeChartPlot() throws Exception {
		testChart("testPieTimeCharts.xml","fop", new File(getOutputDir()+"PieTimeChart.fop"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testCVSPieTimeChartPlot() throws Exception {
		testChart("testPieTimeCharts.xml","csv", new File(getOutputDir()+"PieTimeChart.csv"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testXMLPieTimeChartPlot() throws Exception {
		testChart("testPieTimeCharts.xml","xml", new File(getOutputDir()+"PieTimeChart.xml"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testAddDataXMLPieTimeChartPlot() throws Exception {
		testChart("testAddDataPieTimeCharts.xml","xml", new File(getOutputDir()+"AddDataPieTimeChart.xml"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testHTMLBarTimeChartPlot() throws Exception {
		testChart("testBarTimeCharts.xml","html", new File(getOutputDir()+"BarTimeChart.html"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testPDFBarTimeChartPlot() throws Exception {
		testChart("testBarTimeCharts.xml","fop", new File(getOutputDir()+"BarTimeChart.fop"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testCVSBarTimeChartPlot() throws Exception {
		testChart("testBarTimeCharts.xml","csv", new File(getOutputDir()+"BarTimeChart.csv"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testXMLBarTimeChartPlot() throws Exception {
		testChart("testBarTimeCharts.xml","xml", new File(getOutputDir()+"BarTimeChart.xml"));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testPlotWithDerviedProperty() throws Exception {
		testChart("testChartsDerived.xml","csv", new File(getOutputDir()+"TimeChartWithDerviedProperty.csv"));

	}
	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testPlotWithDoubleDerviedProperty() throws Exception {
		testChart("testChartsDoubleDerived.xml","csv", new File(getOutputDir()+"TimeChartWithDoubleDerviedProperty.csv"));

	}
	
//	public void testPlotOfAllocation() throws Exception {
//		testChart(ReportType.CSV, new File(dir+"AllocationPlot.csv"));
//
//	}
	
	
	protected void testChart(String template,String reportType, File outputFile) throws Exception {
		testChart(template,reportType, TestDataHelper.readFileAsString(outputFile).replaceAll("<!--.*-->\\s*\n", ""));
		
	}	
	
	protected void testChart(String templateName,String type, String expectedOutput)
				throws Exception {
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);

		// Get the params values from the Form
		Map<String, Object> params = new HashMap<String, Object>();
		
		ReportBuilderTest.setupParams(ctx, params);

		ReportBuilder reportBuilder = new ReportBuilder(ctx, templateName,
				"report.xsd");
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
		String output = out.toString().replace(ctx.getInitParameter("java.io.tmpdir","/tmp"), "/tmp");
		assertTrue("Report wasn't correctly formatted:\n"+
				TestDataHelper.diff(expectedOutput, output),
				output.replaceAll("\r?\n", "\n").contains(expectedOutput.replaceAll("\r?\n", "\n")));

	}

	
}