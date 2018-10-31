// Copyright - The University of Edinburgh 2015
/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.reports;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import uk.ac.ed.epcc.webapp.TestDataHelper;
import uk.ac.ed.epcc.webapp.forms.html.HTMLForm;
/** Tests for the final formatting stylesheet
 * 
 * @author spb
 *
 */
public class StyleSheetExtensionTest extends ExtensionTestCase {

	
	@Test
	public void testXML() throws Exception {
		testFormat("xml", new File(getOutputDir()+"Stylesheet.xml"));
	}
	@Test
	public void testHTML() throws Exception {
		testFormat("html", new File(getOutputDir()+"Stylesheet.html"));
	}
	@Test
	public void testXHTML() throws Exception {
		testFormat("xhtml", new File(getOutputDir()+"Stylesheet.xhtml"));
	}
@Test
	public void testCSV() throws Exception {
		testFormat("csv", new File(getOutputDir()+"Stylesheet.csv"));
	}

	

	protected void testFormat(String reportType, File outputFile) throws Exception {
		testFormat(reportType, TestDataHelper.readFileAsString(outputFile).replaceAll("<!--.*-->\\s*\n?", ""));
	}	
	
	protected void testFormat(String type, String expectedOutput)
			throws Exception {

		String templateName = "testStylesheet";

		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);

		// Get the params values from the Form
		Map<String, Object> params = new HashMap<>();
		
		ReportBuilderTest.setupParams(ctx, params);

		ReportBuilder reportBuilder = new ReportBuilder(ctx, templateName,
				"report.xsd");
		ReportType reportType=reportBuilder.getReportTypeReg().getReportType(type);
		assertNotNull(reportType);
		params.put(ReportTypeRegistry.REPORT_TYPE_PARAM, reportType);
		reportBuilder.setupExtensions(reportType,params);
		reportBuilder.buildReportParametersForm(form, params);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);

		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());

		String result = out.toString().replaceAll("<!--.*-->\\s*\n?", "");
		//System.out.println(result);

		// Check it was correctly formatted.
		String output = result.replace(ctx.getInitParameter("java.io.tmpdir","/tmp"), "/tmp");
		assertTrue("Report wasn't correctly formatted:\n"+
				TestDataHelper.diff(expectedOutput, output),
				output.replaceAll("\r?\n", "\n").contains(expectedOutput.replaceAll("\r?\n", "\n")));

	}

	
}