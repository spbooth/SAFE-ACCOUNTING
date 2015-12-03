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
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.db.AccountingClassificationFactory;
import uk.ac.ed.epcc.webapp.TestDataHelper;
import uk.ac.ed.epcc.webapp.WebappTestBase;
import uk.ac.ed.epcc.webapp.forms.html.HTMLForm;
import uk.ac.ed.epcc.webapp.forms.inputs.BooleanInput;
import uk.ac.ed.epcc.webapp.forms.inputs.DoubleInput;
import uk.ac.ed.epcc.webapp.forms.inputs.Input;
import uk.ac.ed.epcc.webapp.forms.inputs.IntegerInput;
import uk.ac.ed.epcc.webapp.forms.inputs.LengthInput;
import uk.ac.ed.epcc.webapp.forms.inputs.ListInput;
import uk.ac.ed.epcc.webapp.forms.inputs.LongInput;
import uk.ac.ed.epcc.webapp.forms.inputs.OptionalInput;
import uk.ac.ed.epcc.webapp.forms.inputs.ParseInput;
import uk.ac.ed.epcc.webapp.forms.inputs.RealInput;
import uk.ac.ed.epcc.webapp.forms.inputs.TextInput;
import uk.ac.ed.epcc.webapp.forms.inputs.TimeStampInput;
import uk.ac.ed.epcc.webapp.forms.inputs.UnmodifiableInput;
import uk.ac.ed.epcc.webapp.junit4.DataBaseFixtures;
import uk.ac.ed.epcc.webapp.model.Classification;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.forms.inputs.DataObjectItemInput;

public class ParameterExtensionTest extends WebappTestBase {
	
	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testLongParameter() throws Exception{		

		String templateName = "testParameters";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();
		ReportBuilderTest.setupParams(ctx, params);
		

		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");	
		reportBuilder.setupExtensions(params);
		reportBuilder.buildReportParametersForm(form, params);	
		
		((LongInput)form.getInput("MyLong")).setValue(123456789l);
	
		reportBuilder.parseReportParametersForm(form, params);
		
		// render the form
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
		System.out.println(out.toString());
		// Check it was correctly formatted.
		assertTrue("Report wasn't correctly formatted", 
				out.toString().contains("My Long is 123456789."));

		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testIntegerParameter() throws Exception{
		
		String templateName = "testParameters";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();
		ReportBuilderTest.setupParams(ctx, params);
		
		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");
		reportBuilder.setupExtensions(params);
		reportBuilder.buildReportParametersForm(form, params);
		
		((IntegerInput)form.getInput("MyInteger")).setValue(1234);

		// Get the params values from the Form
		reportBuilder.parseReportParametersForm(form, params);
	
		// render the form
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
		
		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
		
		// Check it was correctly formatted.
		assertTrue("Report wasn't correctly formatted", 
				out.toString().contains("My Integer is 1234."));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testFloatParameter() throws Exception {				
	    
		String templateName = "testParameters";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();
		ReportBuilderTest.setupParams(ctx,params);
		
		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");
		reportBuilder.setupExtensions(params);
		reportBuilder.buildReportParametersForm(form, params);
		
		((RealInput)form.getInput("MyFloat")).setValue(12.34f);

		// Get the params values from the Form
		reportBuilder.parseReportParametersForm(form, params);

		// render the form
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
		
		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
		
		// Check it was correctly formatted.
		assertTrue("Report wasn't correctly formatted", 
				out.toString().contains("My Float is 12.34."));

	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testDoubleParameter() throws Exception {				
		
		String templateName = "testParameters";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();
		ReportBuilderTest.setupParams(ctx,params);
		
		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");	
		reportBuilder.setupExtensions(params);
		reportBuilder.buildReportParametersForm(form, params);
		
		((DoubleInput)form.getInput("MyDouble")).setValue(1.234E103);

		// Get the params values from the Form
		reportBuilder.parseReportParametersForm(form, params);

		// render the form
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
		
		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
		
		// Check it was correctly formatted.
		assertTrue("Report wasn't correctly formatted "+out.toString()+" does not contain string "+"My Double is 12.34E102.", 
				out.toString().contains("My Double is 1.234E103."));

	}
	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testStringParameter() throws Exception {				
		
		String templateName = "testParameters";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();
		ReportBuilderTest.setupParams(ctx,params);
		
		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");	
		reportBuilder.setupExtensions(params);
		reportBuilder.buildReportParametersForm(form, params);
		
		((TextInput)form.getInput("MyString")).setValue("Mary had a little lamb");

		// Get the params values from the Form
		reportBuilder.parseReportParametersForm(form, params);
		
		// render the form
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
		
		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
		
		// Check it was correctly formatted.
		assertTrue("Report wasn't correctly formatted "+out.toString()+" does not contain string "+"My String is Mary had a little lamb.", 
				out.toString().contains("My String is Mary had a little lamb."));

	}

	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testDateParameter() throws Exception {				
		
		String templateName = "testParameters";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();
		ReportBuilderTest.setupParams(ctx,params);
		
		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");	
		reportBuilder.setupExtensions(params);
		reportBuilder.buildReportParametersForm(form, params);
		
		((ParseInput)form.getInput("MyDate")).parse("2008-09-27");

		// Get the params values from the Form
		reportBuilder.parseReportParametersForm(form, params);
		
		// render the form
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
		
		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
		
		// Check it was correctly formatted.
		assertTrue("Report wasn't correctly formatted", 
				out.toString().contains("My Date is 2008-09-27."));

	}
	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testTimeStampParameter() throws Exception {				
		
		String templateName = "testParameters";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();
		ReportBuilderTest.setupParams(ctx,params);
		
		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");
		reportBuilder.setupExtensions(params);
		reportBuilder.buildReportParametersForm(form, params);
		
		((TimeStampInput)form.getInput("MyTimeStamp")).parse("2008-09-27 14:00:07");

		// Get the params values from the Form
		reportBuilder.parseReportParametersForm(form, params);

		// render the form
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
		
		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
		
		// Check it was correctly formatted.
		assertTrue("Report wasn't correctly formatted", 
				out.toString().contains("My TimeStamp is 2008-09-27 14:00:07."));

	}
	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testBooleanParameter() throws Exception {				
		
		String templateName = "testParameters";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();
		ReportBuilderTest.setupParams(ctx,params);
		
		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");	
		reportBuilder.setupExtensions(params);
		reportBuilder.buildReportParametersForm(form, params);
		
		((BooleanInput)form.getInput("MyBoolean")).setValue(true);

		// Get the params values from the Form
		reportBuilder.parseReportParametersForm(form, params);

		// render the form
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
		
		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
		
		// Check it was correctly formatted.
		assertTrue("Report wasn't correctly formatted", 
				out.toString().contains("My Boolean is true."));

	}
	
	@SuppressWarnings("unchecked")
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testListParameter() throws Exception {				
		
		String templateName = "testParameters";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();
		ReportBuilderTest.setupParams(ctx,params);
		
		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");
		reportBuilder.setupExtensions(params);
		reportBuilder.buildReportParametersForm(form, params);
		
		((ListInput)form.getInput("ListString")).setValue("bar");

		// Get the params values from the Form
		reportBuilder.parseReportParametersForm(form, params);

		// render the form
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
		
		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
		
		// Check it was correctly formatted.
		assertTrue("Report wasn't correctly formatted", 
				out.toString().contains("My List is bar."));

	}


	@SuppressWarnings("unchecked")
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testDataObjectParameter() throws Exception {				
		
		String templateName = "testParameters";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();
		ReportBuilderTest.setupParams(ctx,params);
		
		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");
		reportBuilder.setupExtensions(params);
		reportBuilder.buildReportParametersForm(form, params);
		
		((DataObjectItemInput<? extends DataObject>)form.getInput("MyMachine")).setValue(1);
		
		// Get the params values from the Form
		reportBuilder.parseReportParametersForm(form, params);

		// render the form
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
		
		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
		
		System.out.println(out.toString());
		// Check it was correctly formatted.
		assertTrue("Report wasn't correctly formatted", 
				out.toString().contains("My Machine is Eddie."));

	}
	
	@SuppressWarnings("unchecked")
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testDerivedPropertyParameter() throws Exception {				
		
		String templateName = "testParameters";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();
		ReportBuilderTest.setupParams(ctx,params);
		
		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");
		reportBuilder.setupExtensions(params);
		reportBuilder.buildReportParametersForm(form, params);
		AccountingClassificationFactory school_fac = new AccountingClassificationFactory(ctx, "Institute");
		Classification target = school_fac.findFromString("Infrastructure and Environment");
		
		((DataObjectItemInput<? extends DataObject>)form.getInput("Derived")).setValue(target.getID());
		
		// Get the params values from the Form
		reportBuilder.parseReportParametersForm(form, params);

		// render the form
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
		
		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
		
		// Check it was correctly formatted.
		assertTrue("Report wasn't correctly formatted"+
				TestDataHelper.diff(out.toString(), "My Institute is Infrastructure and Environment."), 
				out.toString().contains("My Institute is Infrastructure and Environment."));

	}
	
	@SuppressWarnings("unchecked")
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testDoubleDerivedPropertyParameter() throws Exception {				
		
		String templateName = "testParameters";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();
		ReportBuilderTest.setupParams(ctx,params);
		
		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");
		reportBuilder.setupExtensions(params);
		reportBuilder.buildReportParametersForm(form, params);
		AccountingClassificationFactory school_fac = new AccountingClassificationFactory(ctx, "School");
		Classification target = school_fac.findFromString("Biological Sciences");
		
		((DataObjectItemInput<? extends DataObject>)form.getInput("DoubleDerived")).setValue(target.getID());
		
		// Get the params values from the Form
		reportBuilder.parseReportParametersForm(form, params);

		// render the form
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
		
		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
		
		// Check it was correctly formatted.
		assertTrue("Report wasn't correctly formatted"+
				TestDataHelper.diff(out.toString(), "My School is Biological Sciences."), 
				out.toString().contains("My School is Biological Sciences."));

	}
	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testParameterLabel() throws Exception {				
		
		String templateName = "testParameters";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();
		ReportBuilderTest.setupParams(ctx,params);
		
		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");	
		reportBuilder.setupExtensions(params);
		reportBuilder.buildReportParametersForm(form, params);
		
		// Check the label value
		assertEquals("Invalid Label", form.getField("MyLabel").getLabel(), "My Label");

		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
		
		
	}
	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testParameterOptional() throws Exception {				
		
		String templateName = "testParameters";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();
		ReportBuilderTest.setupParams(ctx,params);
		
		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");
		reportBuilder.setupExtensions(params);
		reportBuilder.buildReportParametersForm(form, params);
		
		// Check is optional
		Input input = form.getInput("Optional");		
		assertTrue("Input is not of type OptionalInput", input instanceof OptionalInput);
		
		OptionalInput optionalInput = (OptionalInput)input;
		assertTrue("Input is not optional", optionalInput.isOptional());

		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
		
		
	}
	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testParameterWithDefault() throws Exception {				
		
		String templateName = "testParameters";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();
		ReportBuilderTest.setupParams(ctx,params);
		
		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");
		reportBuilder.setupExtensions(params);
		reportBuilder.buildReportParametersForm(form, params);
		
		// Check the default
		assertNotNull("Default value not set", form.getInput("Default").getValue());
		assertEquals("Wrong default value", form.getInput("Default").getValue(),"Bob");
		
		// Get the params values from the Form
		reportBuilder.parseReportParametersForm(form, params);	

		// render the form
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);

		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
		
		// Check it was correctly formatted.
		assertTrue("Report wasn't correctly formatted", 
				out.toString().contains("My name is Bob."));
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testReadOnlyParameter() throws Exception {				
		
		String templateName = "testParameters";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();
		ReportBuilderTest.setupParams(ctx,params);
		
		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");
		reportBuilder.setupExtensions(params);
		reportBuilder.buildReportParametersForm(form, params);
		
		// Check for read only
		Input input = form.getInput("ReadOnly");		
		assertTrue("Input is not of type UnmodifiableInput", input instanceof UnmodifiableInput);
		
		// Set the value and then make sure we still have to original value
		input.setValue("Jane");
		
		// Get the params values from the Form
		reportBuilder.parseReportParametersForm(form, params);	

		// render the form
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);

		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
		
		// Check it was correctly formatted.
		assertTrue("Readonly value was changed.", 
				out.toString().contains("My name is Bob."));

	}

	@SuppressWarnings("unchecked")
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testParameterLength() throws Exception {				
		
		String templateName = "testParameters";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();
		ReportBuilderTest.setupParams(ctx,params);
		
		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");
		reportBuilder.setupExtensions(params);
		reportBuilder.buildReportParametersForm(form, params);
		
		// Check the length of the field
		Input input = form.getInput("LongString");		
		assertTrue("Input is not of type LengthInput", input instanceof LengthInput);

		LengthInput lengthInput = (LengthInput)input;
		assertEquals("Input is not the right length", lengthInput.getMaxResultLength(),256);
		
		// Set the value and then make sure we still have to original value
		input.setValue("123456789 123456789 123456789 123456789 123456789 " +
				"123456789 123456789 123456789 123456789 123456789 " +
				"123456789 123456789 123456789 123456789 123456789 " +
				"123456789 123456789 123456789 123456789 123456789 " +
				"123456789 123456789 123456789 123456789 123456789 ");
		
		// Get the params values from the Form
		reportBuilder.parseReportParametersForm(form, params);

		// render the form
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);

		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
		
		// Check it was correctly formatted.
		assertTrue("Readonly value was changed.", 
				out.toString().contains("My String is " +
						"123456789 123456789 123456789 123456789 123456789 " +
						"123456789 123456789 123456789 123456789 123456789 " +
						"123456789 123456789 123456789 123456789 123456789 " +
						"123456789 123456789 123456789 123456789 123456789 " +
						"123456789 123456789 123456789 123456789 123456789 "));
		
	}
	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testInvalidParameters() throws Exception {		
		
		String templateName = "testParameters";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();
		ReportBuilderTest.setupParams(ctx,params);
		
		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");
		reportBuilder.setupExtensions(params);
		reportBuilder.buildReportParametersForm(form, params);

		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
		
		// Here I'm deliberately not setting a value
		((TextInput)form.getInput("MyString")).setValue(null);
		
		assertFalse("No error were found in the form", form.validate());
		
	}	


	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testParameterUserUsername() throws Exception {		
		
		String templateName = "testParameters";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();
		ReportBuilderTest.setupParams(ctx,params);
		
		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");	
		reportBuilder.setupExtensions(params);
		reportBuilder.buildReportParametersForm(form, params);

		// Get the params values from the Form
		reportBuilder.parseReportParametersForm(form, params);	
		
		// render the form
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
		
		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
		
		// Check it was correctly formatted.
		assertTrue("Report wasn't correctly formatted:\n"+out, 
				out.toString().contains("My Username is sbooth."));
		
	}	
	
	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testParameterUserEmail() throws Exception {		
		
		String templateName = "testParameters";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();
		ReportBuilderTest.setupParams(ctx,params);
		
		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");	
		reportBuilder.setupExtensions(params);
		reportBuilder.buildReportParametersForm(form, params);

		// Get the params values from the Form
		reportBuilder.parseReportParametersForm(form, params);	
		
		// render the form
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
		
		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
		
		// Check it was correctly formatted.
		assertTrue("Report wasn't correctly formatted", 
				out.toString().contains("My E-mail is spb@epcc.ed.ac.uk."));
		
	}	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
public void testParameterUserExpression() throws Exception {		
		
		String templateName = "testParameters";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();
		ReportBuilderTest.setupParams(ctx,params);
		
		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");	
		reportBuilder.setupExtensions(params);
		reportBuilder.buildReportParametersForm(form, params);

		// Get the params values from the Form
		reportBuilder.parseReportParametersForm(form, params);	
		
		// render the form
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
		
		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
		
		// Check it was correctly formatted.
		assertTrue("Report wasn't correctly formatted", 
				out.toString().contains("My Expression is spb@epcc.ed.ac.uk."));
		
		
	}	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testParameterReportType() throws Exception {		
		
		String templateName = "testParameters";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();
		ReportBuilderTest.setupParams(ctx,params);
		
		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");
		reportBuilder.setupExtensions(params);
		reportBuilder.buildReportParametersForm(form, params);

		// Get the params values from the Form
		reportBuilder.parseReportParametersForm(form, params);	
		params.put("ReportType", reportBuilder.HTML);
		// render the form
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
		
		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
		
		// Check it was correctly formatted.
		assertTrue("Report wasn't correctly formatted:\n"+out, 
				out.toString().contains("Report type is HTML"));
		assertFalse("Report wasn't correctly formatted:\n"+out, 
				out.toString().contains("This is XML"));
		
	}	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
public void testParameterReportType2() throws Exception {		
		
		String templateName = "testParameters";
		
		// Create a HTMLForm.
		HTMLForm form = new HTMLForm(ctx);
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();
		ReportBuilderTest.setupParams(ctx,params);
		
		ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");
		reportBuilder.setupExtensions(params);
		reportBuilder.buildReportParametersForm(form, params);

		// Get the params values from the Form
		reportBuilder.parseReportParametersForm(form, params);	
		params.put("ReportType", reportBuilder.getReportType("XML"));
		// render the form
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		reportBuilder.renderXML(params, out);
		
		// Look for errors
		ReportBuilderTest.checkErrors(reportBuilder.getErrors());
		
		// Check it was correctly formatted.
		assertFalse("Report wasn't correctly formatted:\n"+out, 
				out.toString().contains("Report type is HTML"));
		assertFalse("Report wasn't correctly formatted:\n"+out, 
				out.toString().contains("Report type is XML"));
		assertTrue("Report wasn't correctly formatted:\n"+out, 
				out.toString().contains("This is XML"));
		
	}	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
public void testParameterReportType3() throws Exception {		
	
	String templateName = "testParameters";
	
	// Create a HTMLForm.
	HTMLForm form = new HTMLForm(ctx);
	
	// Get the params values from the Form
	Map<String,Object> params = new HashMap<String,Object>();
	ReportBuilderTest.setupParams(ctx,params);
	
	ReportBuilder reportBuilder = new ReportBuilder(ctx,templateName,"report.xsd");
	reportBuilder.setupExtensions(params);
	reportBuilder.buildReportParametersForm(form, params);

	// Get the params values from the Form
	reportBuilder.parseReportParametersForm(form, params);	
	params.put("ReportType", reportBuilder.getReportType("CSV"));
	// render the form
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	reportBuilder.renderXML(params, out);
	
	// Look for errors
	ReportBuilderTest.checkErrors(reportBuilder.getErrors());
	
	// Check it was correctly formatted.
	assertTrue("Report wasn't correctly formatted:\n"+out, 
			out.toString().contains("Report type is CSV"));
	assertFalse("Report wasn't correctly formatted:\n"+out, 
			out.toString().contains("Report type is HTML"));
	assertFalse("Report wasn't correctly formatted:\n"+out, 
			out.toString().contains("This is XML"));
	
}	
}