// Copyright - The University of Edinburgh 2015
/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.reports;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.junit.Test;
import org.xml.sax.SAXException;

import uk.ac.ed.epcc.webapp.TestDataHelper;
import uk.ac.ed.epcc.webapp.exceptions.InvalidArgument;
import uk.ac.ed.epcc.webapp.forms.html.HTMLForm;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.junit4.DataBaseFixtures;
import uk.ac.ed.epcc.webapp.session.AppUser;
import uk.ac.ed.epcc.webapp.session.SessionService;
import uk.ac.ed.epcc.webapp.session.WebNameFinder;
public class RestrictExtensionTest extends ExtensionTestCase {

	
	
	
	@Test
	@DataBaseFixtures({"TestRestrict.xml"})
	public void testRestrict() throws Exception{		
		testRestrict("xml", new File(getOutputDir()+"Restrict.xml"));
	}

	protected void testRestrict(String reportType, File outputFile) throws Exception {
		testRestrict(reportType, TestDataHelper.readFileAsString(outputFile).replaceAll("<!--.*-->\\s*\n", ""));
		
	}
	
	protected void testRestrict(String type, String expectedOutput)
			throws Exception {
		
		String templateName = "testRestricted";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String, Object> params = new HashMap<String, Object>();
		
		ReportBuilderTest.setupParams(ctx, params);
		
		ReportBuilder reportBuilder = new ReportBuilder(ctx, templateName,
				"report.xsd");
		ReportType reportType = reportBuilder.getReportType(type);
		params.put("ReportType", reportType);
		reportBuilder.setupExtensions(params);
		reportBuilder.buildReportParametersForm(form, params);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
		
		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
		
		//System.out.println(out.toString());
		
		// Check it was correctly formatted.
		assertTrue("Report wasn't correctly formatted:\n"+
				TestDataHelper.diff(expectedOutput, out.toString()),
				out.toString().contains(expectedOutput));
	
	}
	@SuppressWarnings("unchecked")
	@Test
	@DataBaseFixtures({"TestRestrict.xml"})
	public void testGlobalRequired() throws URISyntaxException, SAXException, IOException, ParserConfigurationException, InvalidArgument, TransformerFactoryConfigurationError, TransformerException, DataException{
        
		String templateName = "testRestricted";
		ReportBuilder reportBuilder = new ReportBuilder(ctx, templateName,
		"report.xsd");
		SessionService s = ctx.getService(SessionService.class);
		AppUser target = s.getLoginFactory().findByEmail("spb@epcc.ed.ac.uk");
				
		s.setCurrentPerson(target);
		Map<String,Object> params=new HashMap<String, Object>();
		reportBuilder.setupExtensions(params);
		assertFalse(reportBuilder.canUse(s,params));
		target=s.getLoginFactory().getRealmFinder(WebNameFinder.WEB_NAME).findFromString("ngsadminuser");
		s.setCurrentPerson(target);
		assertTrue(s.hasRole("frog"));
		assertTrue(reportBuilder.canUse(s,params));
	}
	@SuppressWarnings("unchecked")
	@Test
	@DataBaseFixtures({"TestRestrict.xml"})
	public void testGlobalSufficient() throws URISyntaxException, SAXException, IOException, ParserConfigurationException, InvalidArgument, TransformerFactoryConfigurationError, TransformerException, DataException{
        
		String templateName = "testRestricted";
		ReportBuilder reportBuilder = new ReportBuilder(ctx, templateName,
		"report.xsd");
		SessionService s = ctx.getService(SessionService.class);
		AppUser target = s.getLoginFactory().findByEmail("spb@epcc.ed.ac.uk");
				
		s.setCurrentPerson(target);
		Map<String,Object> params=new HashMap<String, Object>();
		reportBuilder.setupExtensions(params);
		assertFalse(reportBuilder.canUse(s,params));
		
		target=s.getLoginFactory().getRealmFinder(WebNameFinder.WEB_NAME).findFromString("atlas015");
		s.setCurrentPerson(target);
		assertTrue(s.hasRole("earwig"));
		assertTrue(reportBuilder.canUse(s,params));
	}
	@SuppressWarnings("unchecked")
	@Test
	@DataBaseFixtures({"TestRestrict.xml"})
	public void testGlobalSufficientRelationship() throws URISyntaxException, SAXException, IOException, ParserConfigurationException, InvalidArgument, TransformerFactoryConfigurationError, TransformerException, DataException{
        
		String templateName = "testRestricted";
		ReportBuilder reportBuilder = new ReportBuilder(ctx, templateName,
		"report.xsd");
		SessionService s = ctx.getService(SessionService.class);
		AppUser target = s.getLoginFactory().findByEmail("spb@epcc.ed.ac.uk");
				
		s.setCurrentPerson(target);
		Map<String,Object> params=new HashMap<String, Object>();
		reportBuilder.setupExtensions(params);
		assertFalse(reportBuilder.canUse(s,params));
		
		target=s.getLoginFactory().getRealmFinder(WebNameFinder.WEB_NAME).findFromString("boris");
		s.setCurrentPerson(target);
		assertFalse(s.hasRole("frog"));
		assertFalse(s.hasRole("earwig"));
		assertTrue(reportBuilder.canUse(s,params));
	}
				
}