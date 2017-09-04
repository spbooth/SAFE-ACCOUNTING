// Copyright - The University of Edinburgh 2015
/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.reports;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.TransformerFactoryConfigurationError;

import org.junit.Test;

import uk.ac.ed.epcc.webapp.WebappTestBase;
import uk.ac.ed.epcc.webapp.forms.html.HTMLForm;
import uk.ac.ed.epcc.webapp.junit4.DataBaseFixtures;
public class AtomExtensionTest extends WebappTestBase {
 
	

	String countSummaryOutput = 
		"The user 's0565741' ran 25 jobs between 01-10-2008 and 02-10-2008.";
	
	String sumSummaryAtomOutput = 
		"The user 's0565741' used 13574 seconds between 01-10-2008 and 02-10-2008.";
	
	String listAtomOutput = 
		"The user 's0565741' ran jobs against the following project: ecdf_baseline between 01-10-2008 and 02-10-2008.";
	
	String atomOutput = "The Job 'mert8-as'";
	
	String overlapOutput=
			"The user 's0565741' used 8935 seconds between 2008-10-01 04:30:00 and 2008-10-01 04:40:00.";	
	String atomatomOutput=
			"The user 's0565741' used 17870 seconds between 2008-10-01 04:30:00 and 2008-10-01 04:40:00.";			
 
	@Test
	@DataBaseFixtures({"AtomExtensionData.xml"})
	public void testCountSummaryAtom() throws Exception {		
		
		String templateName = "testAtom";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();
		ReportBuilderTest.setupParams(ctx,params);
		
		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");	
		reportBuilder.setupExtensions(reportBuilder.getReportTypeReg().getReportType("XML"),params);
		reportBuilder.buildReportParametersForm(form, params);
		
		// render the form
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
	
		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());

		// Check it was correctly formatted.		
		assertTrue("CountAtom wasn't correctly formatted: Got\n"+out.toString()+
				"Expected\n"+countSummaryOutput, 
				out.toString().contains(countSummaryOutput));
		
	}
	@Test
	@DataBaseFixtures({"AtomExtensionData.xml"})
	public void testSumSummaryAtom() throws Exception {		
		
		String templateName = "testAtom";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();
		ReportBuilderTest.setupParams(ctx,params);
		
		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");
		reportBuilder.setupExtensions(reportBuilder.getReportTypeReg().getReportType("XML"),params);
		reportBuilder.buildReportParametersForm(form, params);
		
		// render the form
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
	
		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
		
		// Check it was correctly formatted.		
		assertTrue("SumAtom wasn't correctly formatted: Got\n"+out.toString()+
				"Expected\n"+sumSummaryAtomOutput, 
				out.toString().contains(sumSummaryAtomOutput));
		
	}
	@Test
	@DataBaseFixtures({"AtomExtensionData.xml"})
	public void testOverlapSumSummaryAtom() throws Exception {		
		
		String templateName = "testOverlapAtom";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();
		ReportBuilderTest.setupParams(ctx,params);
		
		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");
		reportBuilder.setupExtensions(reportBuilder.getReportTypeReg().getReportType("XML"),params);
		reportBuilder.buildReportParametersForm(form, params);
		
		// render the form
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
	
		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
		
		// Check it was correctly formatted.		
		assertTrue("SumAtom wasn't correctly formatted: Got\n"+out.toString()+
				"Expected\n"+overlapOutput, 
				out.toString().contains(overlapOutput));
		
	}
	@Test
	@DataBaseFixtures({"AtomExtensionData.xml"})
	public void testAtomAtom() throws Exception {		
		
		String templateName = "testAtomAtom";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();
		ReportBuilderTest.setupParams(ctx,params);
		
		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");
		reportBuilder.setupExtensions(reportBuilder.getReportTypeReg().getReportType("XML"),params);
		reportBuilder.buildReportParametersForm(form, params);
		
		// render the form
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
	
		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
		
		// Check it was correctly formatted.		
		assertTrue("AtomAtom wasn't correctly formatted: Got\n"+out.toString()+
				"Expected\n"+atomatomOutput, 
				out.toString().contains(atomatomOutput));
		
	}
	@Test
	@DataBaseFixtures({"AtomExtensionData.xml"})
	public void testListAtom() throws Exception {		
		
		String templateName = "testAtom";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();
		ReportBuilderTest.setupParams(ctx,params);
		
		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");
		reportBuilder.setupExtensions(reportBuilder.getReportTypeReg().getReportType("XML"),params);
		reportBuilder.buildReportParametersForm(form, params);
		
		// render the form
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
	
		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
		
		// Check it was correctly formatted.		
		assertTrue("SumAtom wasn't correctly formatted: Got\n"+out.toString()+
				"Expected\n"+listAtomOutput, 
				out.toString().contains(listAtomOutput));
		
	}
	
	@Test
	@DataBaseFixtures({"AtomExtensionData.xml"})
	public void testAtom() throws Exception {		
		
		String templateName = "testAtom";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();
		ReportBuilderTest.setupParams(ctx,params);
		
		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");
		reportBuilder.setupExtensions(reportBuilder.getReportTypeReg().getReportType("XML"),params);
		reportBuilder.buildReportParametersForm(form, params);
		
		// render the form
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
	
		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
		
		// Check it was correctly formatted.		
		assertTrue("SumAtom wasn't correctly formatted: Got\n"+out.toString()+
				"Expected\n"+atomOutput, 
				out.toString().contains(atomOutput));
		
	}
	@Test
	public void testPercentage() throws TransformerFactoryConfigurationError, Exception{
		String templateName = "testPercentage";
		
		// Create a HTMLForm.
				HTMLForm form = new HTMLForm(ctx);
				
				// Get the params values from the Form
				Map<String,Object> params = new HashMap<String,Object>();
				ReportBuilderTest.setupParams(ctx,params);
				
				ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");
				reportBuilder.setupExtensions(reportBuilder.getReportTypeReg().getReportType("XML"),params);
				reportBuilder.buildReportParametersForm(form, params);
				
				// render the form
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				reportBuilder.renderXML(params, out);
			
				// Look for errors
				ReportBuilderTest.checkErrors(reportBuilder.getErrors());
				String percent_output="A basic percentage 54%";
				// Check it was correctly formatted.		
				assertTrue("Percent wasn't correctly formatted: Got\n"+out.toString()+
						"Expected\n"+percent_output,
						out.toString().contains(percent_output));
	}
	
	@Test
	public void testFormattedPercentage() throws TransformerFactoryConfigurationError, Exception{
		String templateName = "testPercentage";
		
		// Create a HTMLForm.
				HTMLForm form = new HTMLForm(ctx);
				
				// Get the params values from the Form
				Map<String,Object> params = new HashMap<String,Object>();
				ReportBuilderTest.setupParams(ctx,params);
				
				ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");
				reportBuilder.setupExtensions(reportBuilder.getReportTypeReg().getReportType("XML"),params);
				reportBuilder.buildReportParametersForm(form, params);
				
				// render the form
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				reportBuilder.renderXML(params, out);
			
				// Look for errors
				ReportBuilderTest.checkErrors(reportBuilder.getErrors());
				String percent_output="A formatted percentage 53.500%";
				// Check it was correctly formatted.		
				assertTrue("Percent wasn't correctly formatted: Got\n"+out.toString()+
						"Expected\n"+percent_output,
						out.toString().contains(percent_output));
	}
}