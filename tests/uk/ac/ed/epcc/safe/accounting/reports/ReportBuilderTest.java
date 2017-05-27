// Copyright - The University of Edinburgh 2015
/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.reports;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import uk.ac.ed.epcc.safe.accounting.ErrorSet;
import uk.ac.ed.epcc.safe.accounting.reports.forms.html.HTMLReportParametersForm;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.WebappTestBase;
import uk.ac.ed.epcc.webapp.exceptions.InvalidArgument;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.html.HTMLForm;
import uk.ac.ed.epcc.webapp.forms.inputs.Input;
import uk.ac.ed.epcc.webapp.forms.inputs.OptionalInput;
import uk.ac.ed.epcc.webapp.junit4.DataBaseFixtures;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.session.AppUser;
import uk.ac.ed.epcc.webapp.session.SessionService;
import uk.ac.ed.epcc.webapp.time.CalendarFieldSplitPeriod;
public class ReportBuilderTest extends WebappTestBase {
	
	
	public static void setupParams(AppContext ctx, 
			Map<String,Object> params)
		throws ParserConfigurationException 
	{
		ReportType reportType = (ReportType)params.get("ReportType");
		if (reportType == null) {
		  params.put("ReportType", "html");
				
		}
		SessionService sessionService = ctx.getService(SessionService.class);
		sessionService.setCurrentPerson(1);
		AppUser user = sessionService.getCurrentPerson();		
		params.put("User", user);
	}
	
	

	public static void checkErrors(Set<ErrorSet> eset) {
		int errors=0;
		for(ErrorSet es : eset){
			errors += es.size();
			es.traceback(System.err);
		}
		assertEquals(0,errors);
	}

	

	@SuppressWarnings({ "unchecked" })
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testPieTimeChartReport() throws Exception {		
		String templateName = "PieTimeChart";
	
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();
		setupParams(ctx, params);
		
	
		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");	
		reportBuilder.setupExtensions(params);
		reportBuilder.buildReportParametersForm(form, params);
	
		// Check the inputs...		
		Input input = form.getInput("Plot");	
		assertNotNull("Plot Input not found", input);
		((Input<String>)input).setValue("Count");
		
		input = form.getInput("Group");		
		assertTrue("Group is not of type OptionalInput", input instanceof OptionalInput);
		((Input<String>)input).setValue("User");
	
		input = form.getInput("Period");
		assertNotNull("Period Input not found", input);
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(0L);
		c.set(Calendar.YEAR, 2008);
		c.set(Calendar.MONTH,Calendar.SEPTEMBER);
		c.set(Calendar.DAY_OF_MONTH,26);
		c.set(Calendar.HOUR_OF_DAY,0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND,0);
		c.set(Calendar.MILLISECOND,0);
		CalendarFieldSplitPeriod p = new CalendarFieldSplitPeriod(c,Calendar.MONTH,1,1);
		input.setValue(p);
		
		assertTrue(form.validate());
		
		// Get the params values from the Form
		reportBuilder.parseReportParametersForm(form, params);
		
		// render the form
		params.put("ReportType", "html");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
		
		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
	
		// Check it was correctly formatted.
		assertTrue("Report wasn't correctly formatted "+out.toString()+" does not contain the plot image", 
				out.toString().contains("<img"));
	}

	@SuppressWarnings("unchecked")
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testTimeChartReport() throws Exception {		
		String templateName = "TimeChart";
	
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();	
		setupParams(ctx, params);
	
		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");	
		reportBuilder.setupExtensions(params);
		reportBuilder.buildReportParametersForm(form, params);
	
		// Check the inputs...		
		Input input = form.getInput("Plot");	
		assertNotNull("Plot Input not found", input);
		((Input<String>)input).setValue("Wall");
		
		input = form.getInput("Group");		
		assertTrue("Group is not of type OptionalInput", input instanceof OptionalInput);
		((Input<String>)input).setValue("User");
	
		input = form.getInput("Period");
		assertNotNull("Period Input not found", input);
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(0L);
		c.set(Calendar.YEAR, 2008);
		c.set(Calendar.MONTH,Calendar.SEPTEMBER);
		c.set(Calendar.DAY_OF_MONTH,26);
		c.set(Calendar.HOUR_OF_DAY,0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND,0);
		c.set(Calendar.MILLISECOND,0);
		CalendarFieldSplitPeriod p = new CalendarFieldSplitPeriod(c,Calendar.MONTH,1,1);
		input.setValue(p);
		
		
		assertTrue(form.validate());
		// Get the params values from the Form
		reportBuilder.parseReportParametersForm(form, params);
		
		// render the form
		params.put("ReportType", "html");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
		
		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
	
		// Check it was correctly formatted.
		assertTrue("Report wasn't correctly formatted "+out.toString()+" does not contain the plot image", 
				out.toString().contains("<img"));
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testHTMLReport() throws Exception {		
		
		String templateName = "testReport";

		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();	
		setupParams(ctx, params);

		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");	
		reportBuilder.setupExtensions(params);
		reportBuilder.buildReportParametersForm(form, params);
		
		// render the form
		params.put(ReportBuilder.REPORT_TYPE_PARAM, reportBuilder.getReportType("html"));
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
		//System.out.println(out.toString());

		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());

		// Check it was correctly formatted.
		assertTrue("<Title> wasn't correctly formatted "+out.toString(), 
				out.toString().contains("<h1>The Title</h1>"));
		
		assertTrue("<Header> wasn't correctly formatted "+out.toString(), 
				out.toString().contains("<h2>The Heading</h2>"));
		
		assertTrue("<Text> wasn't correctly formatted "+out.toString(), 
				out.toString().contains("<p>This is some text.</p>"));
		assertTrue("<P> wasn't correctly formatted "+out.toString(), 
				out.toString().contains("<p>This is a paragraph.</p>"));
		assertTrue("<Para> wasn't correctly formatted "+out.toString(), 
				out.toString().contains("<p>And another paragraph.</p>"));
		
		assertTrue("<Section> wasn't correctly formatted "+out.toString(), 
				out.toString().contains("<h3>Section 1</h3>"));
		assertTrue("<Section> wasn't correctly formatted "+out.toString(), 
				out.toString().contains("This is a bit section text."));
		
		assertTrue("<Subsection> wasn't correctly formatted "+out.toString(), 
				out.toString().contains("<h4>Subsection 1</h4>"));
		assertTrue("<Subsection> wasn't correctly formatted "+out.toString(), 
				out.toString().contains("This is a subsection."));
		
		assertTrue("<Table> wasn't correctly formatted "+out.toString(), 
				out.toString().contains("<table>"));
		assertTrue("<Table> headers weren't correctly formatted "+out.toString(), 
				out.toString().contains("<th>Heading 1</th><th>Heading 2</th>"));
		assertTrue("<Table> rows weren't correctly formatted "+out.toString(), 
				out.toString().contains("<td>Row 1</td><td>Row 1</td>"));
		
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testPDFReport() throws Exception {		
		String templateName = "testReport";

		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();			
		setupParams(ctx, params);
		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");
		reportBuilder.setupExtensions(params);
		
		SVGChartExtension svg = new SVGChartExtension(ctx,NumberFormat.getInstance());
		params.put("ChartExtension", svg);
		reportBuilder.register(svg);
		params.put("ParameterExtension", new ParameterExtension(ctx, null));

			
		reportBuilder.buildReportParametersForm(form, params);

		// render the form
		params.put(ReportBuilder.REPORT_TYPE_PARAM, reportBuilder.getReportType("pdf"));
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params,out);
		
		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
		
		// Check it's a pdf file
		assertTrue("Did not generate a pdf file "+out.toString(), 
				out.toString().startsWith("%PDF-1.4"));
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testCVSReport() throws Exception {		
		String templateName = "testReport";

		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();		
		setupParams(ctx, params);

		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");	
		reportBuilder.setupExtensions(params);
		reportBuilder.buildReportParametersForm(form, params);

		// render the form
	
		params.put(ReportBuilder.REPORT_TYPE_PARAM, reportBuilder.getReportType("csv"));
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
		
		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());

		// Check it's a pdf file
		assertTrue("Did not generate a csv file "+out.toString(), 
				out.toString().startsWith("The Title"));
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testComplex() throws Exception {		
		
		ReportBuilder builder = new ReportBuilder(ctx,"complex","report.xsd");
		Map<String,Object> params=new HashMap<String,Object>();	
	    setupParams(ctx, params);
	    builder.setupExtensions(params);
	    
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		builder.renderXML(params, out);
		assertTrue(out.size()>0);
		assertFalse(builder.hasErrors());
		//System.out.println(out.toString());
		ReportBuilderTest.checkErrors(builder.getErrors());
		
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testValidate() throws URISyntaxException, DataFault, IOException, SAXException, ParserConfigurationException, InvalidArgument, TransformerFactoryConfigurationError, TransformerException {
        try{
		  ReportBuilder rb = new ReportBuilder(ctx,"badTemplate.xml","report.xsd");
		  assertTrue(rb.hasErrors());
		  //assertFalse("illegal template validated", true);
		}catch(SAXException e){
			e.printStackTrace(System.out);
			
		}
	
	}
	
	@Test
	@Ignore
	public void testTypeSelect() throws Exception{
        // think this has been refactored away
		ReportBuilder builder = new ReportBuilder(ctx,"testParameters","report.xsd");
		Map<String,Object> params=new HashMap<String,Object>();	
	    setupParams(ctx, params);
	    // no report type selected
	    params.remove("ReportType");
	    builder.setupExtensions(null,params);
	    HTMLReportParametersForm form = new HTMLReportParametersForm(builder, params, null);
	    Form f = form.getForm();
	    
	    assertNotNull(f.getInput("ReportType"));
		
	}
	
	
}