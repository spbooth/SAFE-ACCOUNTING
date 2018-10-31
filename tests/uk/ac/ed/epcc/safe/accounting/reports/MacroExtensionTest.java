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
public class MacroExtensionTest extends ExtensionTestCase {

	
	

	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testMacro() throws Exception{		
		testMacro("xml", new File(getOutputDir()+"Macro.xml"));
	}

	protected void testMacro(String reportType, File outputFile) throws Exception {
		testMacro(reportType, TestDataHelper.readFileAsString(outputFile).replaceAll("<!--.*-->\\s*\n", ""));
		
	}
	
	protected void testMacro(String type, String expectedOutput)
			throws Exception {
		
		String templateName = "testMacros";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String, Object> params = new HashMap<>();
		
		ReportBuilderTest.setupParams(ctx, params);
		
		ReportBuilder reportBuilder = new ReportBuilder(ctx, templateName,
				"report.xsd");
		ReportType reportType = reportBuilder.getReportTypeReg().getReportType(type);
		params.put("ReportType", reportType);
		reportBuilder.setupExtensions(reportType,params);
		reportBuilder.buildReportParametersForm(form, params);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
		
		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
		
	
		String result = out.toString().replaceAll("<!--.*-->\\s*\n?", "");
		//System.out.println(result);
		
		//System.out.println("Expecting");
		//System.out.println(expectedOutput);
		// Check it was correctly formatted.
		assertTrue("Report wasn't correctly formatted:\n"+
				TestDataHelper.diff(expectedOutput, result),
				result.replaceAll("\r?\n", "\n").contains(expectedOutput.replaceAll("\r?\n", "\n")));
	
	}
		
}