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

import org.junit.Before;
import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.OverlapHandler;
import uk.ac.ed.epcc.safe.accounting.db.DefaultAccountingService;
import uk.ac.ed.epcc.webapp.Feature;
import uk.ac.ed.epcc.webapp.WebappTestBase;
import uk.ac.ed.epcc.webapp.forms.html.HTMLForm;
import uk.ac.ed.epcc.webapp.junit4.DataBaseFixtures;
import uk.ac.ed.epcc.webapp.session.AbstractSessionService;
public class AtomExtensionTest extends WebappTestBase {
 
	@Before
	public void setRoleTable() {
		AbstractSessionService.setupRoleTable(ctx);
	}

	String countSummaryOutput = 
		"The user 's0565741' ran 25 jobs between 01-10-2008 and 02-10-2008.";
	
	String sumSummaryAtomOutput = 
		"The user 's0565741' used 13574 seconds between 01-10-2008 and 02-10-2008.";
	
	String averageSummaryAtomOutput = 
			"The user 's0565741' used an average of 542.96 seconds between 01-10-2008 and 02-10-2008.";
	
	String medianSummaryAtomOutput = 
			"The user 's0565741' used a median of 533 seconds between 01-10-2008 and 02-10-2008.";
		
	String distictSummaryAtomOutput = 
			"A total of 10 users ran jobs between 01-10-2008 and 02-10-2008.";
		
	String listAtomOutput = 
		"The user 's0565741' ran jobs against the following project: ecdf_baseline between 01-10-2008 and 02-10-2008.";
	
	String atomOutput = "The Job 'mert8-as'";
	
	String overlapOutput=
			"The user 's0565741' used 8935 seconds between 2008-10-01 04:30:00 and 2008-10-01 04:40:00.";	
	
	
	String overlapAverageOutput=
			"The user 's0565741' used an average of 14.892 cpus between 2008-10-01 04:30:00 and 2008-10-01 04:40:00.";	

	String atomatomOutput=
			"The user 's0565741' used 17870 seconds between 2008-10-01 04:30:00 and 2008-10-01 04:40:00.";			
 
	@Test
	@DataBaseFixtures({"AtomExtensionData.xml"})
	public void testCountSummaryAtom() throws Exception {		
		
		String templateName = "testAtom";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<>();
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
		Map<String,Object> params = new HashMap<>();
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
	public void testAverageSummaryAtom() throws Exception {
		Feature.setTempFeature(ctx, DefaultAccountingService.DEFAULT_COMPOSITE_FEATURE, true);
		innerAverageSummaryAtom();
	}
	@Test
	@DataBaseFixtures({"AtomExtensionData.xml"})
	public void testCompositeAverageSummaryAtom() throws Exception {
		Feature.setTempFeature(ctx, DefaultAccountingService.DEFAULT_COMPOSITE_FEATURE, false);
		innerAverageSummaryAtom();
	}
	public void innerAverageSummaryAtom() throws Exception {	
		String templateName = "testAverageAtom";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<>();
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
		assertTrue("AverageAtom wasn't correctly formatted: Got\n"+out.toString()+
				"Expected\n"+averageSummaryAtomOutput, 
				out.toString().contains(averageSummaryAtomOutput));
		
	}
	@Test
	@DataBaseFixtures({"AtomExtensionData.xml"})
	public void testMedianSummaryAtom() throws Exception {		
		
		String templateName = "testMedianAtom";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<>();
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
		assertTrue("MedianAtom wasn't correctly formatted: Got\n"+out.toString()+
				"Expected\n"+medianSummaryAtomOutput, 
				out.toString().contains(medianSummaryAtomOutput));
		
	}
	
	@Test
	@DataBaseFixtures({"AtomExtensionData.xml"})
	public void testDistictSummaryAtom() throws Exception {	
		Feature.setTempFeature(ctx, DefaultAccountingService.DEFAULT_COMPOSITE_FEATURE, true);
		innerDistictSummaryAtom();
	}
	@Test
	@DataBaseFixtures({"AtomExtensionData.xml"})
	public void testComositeDistictSummaryAtom() throws Exception {	
		Feature.setTempFeature(ctx, DefaultAccountingService.DEFAULT_COMPOSITE_FEATURE, false);
		innerDistictSummaryAtom();
	}
	public void innerDistictSummaryAtom() throws Exception {	
		String templateName = "testDistictAtom";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<>();
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
		assertTrue("DistinctAtom wasn't correctly formatted: Got\n"+out.toString()+
				"Expected\n"+distictSummaryAtomOutput, 
				out.toString().contains(distictSummaryAtomOutput));
		
	}
	
	@Test
	@DataBaseFixtures({"AtomExtensionData.xml"})
	public void testOverlapSumSummaryAtom() throws Exception {		
		
		String templateName = "testOverlapAtom";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<>();
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
	public void testOverlapAverageSummaryAtom() throws Exception {	
		Feature.setTempFeature(ctx, OverlapHandler.USE_QUERY_MAPPER_FEATURE, true);
		Feature.setTempFeature(ctx, OverlapHandler.USE_CASE_OVERLAP, true);
		String templateName = "testOverlapAverageAtom";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<>();
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
				"Expected\n"+overlapAverageOutput, 
				out.toString().contains(overlapAverageOutput));
		
	}
	
	@Test
	@DataBaseFixtures({"AtomExtensionData.xml"})
	public void testOverlapAverageSummaryAtom2() throws Exception {	
		Feature.setTempFeature(ctx, OverlapHandler.USE_QUERY_MAPPER_FEATURE, true);
		Feature.setTempFeature(ctx, OverlapHandler.USE_CASE_OVERLAP, false);
		String templateName = "testOverlapAverageAtom";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<>();
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
				"Expected\n"+overlapAverageOutput, 
				out.toString().contains(overlapAverageOutput));
		
	}
	
	@Test
	@DataBaseFixtures({"AtomExtensionData.xml"})
	public void testOverlapAverageSummaryAtom3() throws Exception {		
		Feature.setTempFeature(ctx, OverlapHandler.USE_QUERY_MAPPER_FEATURE, false);
		String templateName = "testOverlapAverageAtom";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<>();
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
				"Expected\n"+overlapAverageOutput, 
				out.toString().contains(overlapAverageOutput));
		
	}
	@Test
	@DataBaseFixtures({"AtomExtensionData.xml"})
	public void testAtomAtom() throws Exception {		
		
		String templateName = "testAtomAtom";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<>();
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
		Map<String,Object> params = new HashMap<>();
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
		Map<String,Object> params = new HashMap<>();
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
				Map<String,Object> params = new HashMap<>();
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
	public void testIntPercentage() throws TransformerFactoryConfigurationError, Exception{
		String templateName = "testPercentage";
		
		// Create a HTMLForm.
				HTMLForm form = new HTMLForm(ctx);
				
				// Get the params values from the Form
				Map<String,Object> params = new HashMap<>();
				ReportBuilderTest.setupParams(ctx,params);
				
				ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");
				reportBuilder.setupExtensions(reportBuilder.getReportTypeReg().getReportType("XML"),params);
				reportBuilder.buildReportParametersForm(form, params);
				
				// render the form
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				reportBuilder.renderXML(params, out);
			
				// Look for errors
				ReportBuilderTest.checkErrors(reportBuilder.getErrors());
				String percent_output="A int percentage 67%";
				// Check it was correctly formatted.		
				assertTrue("Percent wasn't correctly formatted: Got\n"+out.toString()+
						"Expected\n"+percent_output,
						out.toString().contains(percent_output));
	}
	@Test
	public void testPlugin() throws TransformerFactoryConfigurationError, Exception{
		String templateName = "testPlugin";
		
		// Create a HTMLForm.
				HTMLForm form = new HTMLForm(ctx);
				
				// Get the params values from the Form
				Map<String,Object> params = new HashMap<>();
				ReportBuilderTest.setupParams(ctx,params);
				
				ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");
				reportBuilder.setupExtensions(reportBuilder.getReportTypeReg().getReportType("XML"),params);
				reportBuilder.buildReportParametersForm(form, params);
				
				// render the form
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				reportBuilder.renderXML(params, out);
			
				// Look for errors
				ReportBuilderTest.checkErrors(reportBuilder.getErrors());
				String output="PI=3.142";
				// Check it was correctly formatted.		
				assertTrue("Plugin wasn't correctly formatted: Got\n"+out.toString()+
						"Expected\n"+output,
						out.toString().contains(output));
	}
	@Test
	public void testPeriodDuration() throws TransformerFactoryConfigurationError, Exception{
		String templateName = "testPeriodDuration";
		
		// Create a HTMLForm.
				HTMLForm form = new HTMLForm(ctx);
				
				// Get the params values from the Form
				Map<String,Object> params = new HashMap<>();
				ReportBuilderTest.setupParams(ctx,params);
				
				ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");
				reportBuilder.setupExtensions(reportBuilder.getReportTypeReg().getReportType("XML"),params);
				reportBuilder.buildReportParametersForm(form, params);
				
				// render the form
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				reportBuilder.renderXML(params, out);
			
				// Look for errors
				ReportBuilderTest.checkErrors(reportBuilder.getErrors());
				String output="Period is 48:00:00";
				// Check it was correctly formatted.		
				assertTrue("Plugin wasn't correctly formatted: Got\n"+out.toString()+
						"Expected\n"+output,
						out.toString().contains(output));
				String output2="Period/15 is 3:12:00";
				// Check it was correctly formatted.		
				assertTrue("Plugin wasn't correctly formatted: Got\n"+out.toString()+
						"Expected\n"+output2,
						out.toString().contains(output2));
				String output3="Period/17 is 2:49:24";
				// Check it was correctly formatted.		
				assertTrue("Plugin wasn't correctly formatted: Got\n"+out.toString()+
						"Expected\n"+output3,
						out.toString().contains(output3));
	}
	@Test
	public void testFormattedPercentage() throws TransformerFactoryConfigurationError, Exception{
		String templateName = "testPercentage";
		
		// Create a HTMLForm.
				HTMLForm form = new HTMLForm(ctx);
				
				// Get the params values from the Form
				Map<String,Object> params = new HashMap<>();
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
	
	@Test
	public void testDefine() throws TransformerFactoryConfigurationError, Exception{
		String templateName = "testDefine";
		
		// Create a HTMLForm.
				HTMLForm form = new HTMLForm(ctx);
				
				// Get the params values from the Form
				Map<String,Object> params = new HashMap<>();
				ReportBuilderTest.setupParams(ctx,params);
				
				ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");
				reportBuilder.setupExtensions(reportBuilder.getReportTypeReg().getReportType("XML"),params);
				reportBuilder.buildReportParametersForm(form, params);
				
				// render the form
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				reportBuilder.renderXML(params, out);
			
				// Look for errors
				ReportBuilderTest.checkErrors(reportBuilder.getErrors());
				String define_output="Value is 378.667";
				// Check it was correctly formatted.		
				assertTrue("Percent wasn't correctly formatted: Got\n"+out.toString()+
						"Expected\n"+define_output,
						out.toString().contains(define_output));
	}
}