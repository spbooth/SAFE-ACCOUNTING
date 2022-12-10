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
import uk.ac.ed.epcc.webapp.model.data.stream.ByteArrayStreamData;

public class FormatExtensionTest extends ExtensionTestCase {

	@Override
	public String normalise(String output) throws Exception {
		String s1 = output.replaceAll("</?fragment>\\s*\r?\n?\\s*", "");
		String s2 = s1.replaceAll("<!--.*-->\\s*\r?\n", "");
		return super.normalise(s2);
	}

	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testFormat() throws Exception {
		testFormatExtension("testFormatBasic","xml", "Format.xml");

	}	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testFormatWithIfDef() throws Exception {
		testFormatExtension("testFormatIfDef","xml", "FormatIfDef.xml");

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testFormatWithIf() throws Exception {
		testFormatExtension("testFormatIf","xml", "FormatIf.xml");

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testFormatLimit() throws Exception {
		testFormatExtension("testFormatLimit","xml", "LimitFormat.xml");

	}
	protected void testFormatExtension(String templateName,String type, File outputFile) throws Exception {
		testChart(templateName,type, TestDataHelper.readFileAsString(outputFile).replaceAll("<!--.*-->\\s*\n", ""));
		
	}	
//	protected void testFormatExtension(String type, String name) throws Exception {
//		testFormatExtension("testFormat",type, name);
//	}	
	protected void testFormatExtension(String templateName,String reportType, String name) throws Exception {
		
		
		ByteArrayStreamData data = new ByteArrayStreamData();
		data.read(getClass().getResourceAsStream("output/"+name));
		testChart(templateName,reportType, data.toString());
		
	}
	protected void testChart(String templateName,String type, String expectedOutput)
			throws Exception {

		

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

		//System.out.println(out.toString());
		expectedOutput = normalise(expectedOutput);
		String outs = normalise(out.toString());
		System.out.println("##Expected");
		System.out.println(expectedOutput);
		System.out.println("##Got");
		System.out.println(outs);
		System.out.println("##Raw");
		System.out.println(out.toString());
		// Check it was correctly formatted.
		assertTrue("Report wasn't correctly formatted:\n"+
				TestDataHelper.diff(expectedOutput, outs),
				outs.contains(expectedOutput));

	}
}