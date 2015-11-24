// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.formatters;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ErrorSet;
import uk.ac.ed.epcc.safe.accounting.db.AccountingUpdater;
import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.SkipRecord;
import uk.ac.ed.epcc.safe.accounting.update.UsageRecordPolicy;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.config.ConfigService;
import uk.ac.ed.epcc.webapp.config.FilteredProperties;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;

/**
 * <p>
 * This object is an end to end parse to format solution for processing usage
 * records. It reads and writes to input and output streams. It reads usage
 * records string form, parse them into <code>PropertyMap</code>s, processes any
 * policies that may be applied to the properties, and prints the resulting
 * properties to it's output in string form using a {@link UsageRecordFormatter}
 * . In addition to providing input stream, this object can also be used to
 * write usage records using a method, taking a string argument as input. Output
 * is always written to this object's output.
 * </p>
 * <p>
 * This object implements {@link Contexed} and relies heavily on properties set
 * within it's <code>AppContext</code> to determine which parser to use to parse
 * the input, properties to configure the parser, properties to configure any
 * policies, any derived property definitions and the format of the output.
 * </p>
 * <p>
 * This object was designed with reference to {@link AccountingUpdater},
 * particularly it's
 * {@link AccountingUpdater#receiveAccountingData(String, boolean)}
 * method which this object's {@link #formatAcountingData()} method is modelled
 * on. <code>AccountingUpdater</code> works within the context of a <code>UsageRecordParseTarget</code>.
 * This object emulates this design with the concept of a <em>mode</em>.
 * Properties may be specified by <em>mode</em> (which usually means specifying
 * the mode name somewhere in the property name). Properties set up to work
 * within a web application using <code>UsageRecordFactory</code> should work
 * with this object too.
 * </p>
 * <p>
 * This object uses property that sets the mode (see the {@link #PROP_MODE}
 * variable) as well as the <em>class.parser</em> to specify which parser class
 * to use for the input. Policies may optionally be used by specifying the
 * appropriate <em>policies</em> property as one would do for
 * <code>UsageRecordFactory</code>. If the parser selected required properties
 * to configure itself, these should be present (what they are depends on the
 * parser). Properties required by {@link UsageRecordFormatter} in order to
 * format the output correctly must also be present.
 * </p>
 * 
 * 
 * 
 * 
 * @author jgreen4
 * 
 */
@Deprecated
@uk.ac.ed.epcc.webapp.Version("$Id: UsageRecordWriter.java,v 1.15 2014/09/15 14:32:22 spb Exp $")

public class UsageRecordWriter implements Contexed {
	/**
	 * Convenience variable to define the system dependent new line character
	 */
	private static final String NEW_LINE = System.getProperty("line.separator");
	/**
	 * The mode in which this this writer will run. Determines what properties to
	 * fetch
	 */
	public static final String PROP_MODE = UsageRecordFormatter.PROPERTY_BASE
			+ ".mode";

	/**
	 * The input - usage records will be read in string format from this
	 */
	private Scanner in;
	/**
	 * The output - usage records will be written in string form to this
	 */
	private PrintStream out;
	/**
	 * The context under which this writer operates
	 */
	private AppContext context;

	/*
	 * Tweakable parameters to improve performance
	 */
	private final int INITIAL_INPUT_BUFFER_SIZE = 10000;

	/**
	 * Constructs a new <code>UsageRecordWriter</code> using <code>context</code>
	 * as the context under which this objects runs. Input is set to standard
	 * input (STDIN) and output s set to standard output (STDOUT)
	 * 
	 * @param context
	 *          The context under which this objects runs
	 */
	public UsageRecordWriter(AppContext context) {
		this.context = context;
		this.setInput(System.in);
		this.setOutput(System.out);
	}

	/**
	 * Allows the input for this writer to be changed
	 * 
	 * @param input
	 *          The new input
	 */
	public void setInput(InputStream input) {
		this.in = new Scanner(input);
	}

	/**
	 * Allows the output for this writer to be changed
	 * 
	 * @param output
	 *          The new output
	 */
	public void setOutput(PrintStream output) {
		this.out = output;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ed.epcc.webapp.Contexed#getContext()
	 */
	public AppContext getContext() {
		return this.context;
	}

	/**
	 * Reads usage records from this writer's input, parses it using the parser
	 * specified in this writer's <code>AppContext</code> and formats the
	 * resulting information, sending it to we writer's output.
	 * 
	 * @return A summary of the operation in human readable format
	 * @throws AccountingFormattingException
	 *           If a problem occurred while configuring the formatter that
	 *           formats the parsed usage records
	 * @throws AccountingParseException
	 *           If a problem occurs while parsing the usage records that come in
	 *           through the input stream
	 * @see #formatAcountingData(PropertyMap)
	 * @see #formatAcountingData(PropertyMap, String)
	 */
	public String formatAcountingData() throws AccountingFormattingException,
			AccountingParseException {
		return this.formatAcountingData(null);

	}

	/**
	 * Reads usage records from this writer's input, parses it using the parser
	 * specified in this writer's <code>AppContext</code> and formats the
	 * resulting information, sending it to we writer's output. When values
	 * required for formatting are not included in the usage records received in
	 * the intput, the default values provided are used.
	 * 
	 * @param defaults
	 *          The default values provided when appropriate values are not
	 *          provided by the incoming usage records. Can be <code>null</code>
	 *          if no defaults are to be used
	 * @return A summary of the operation in human readable format
	 * @throws AccountingFormattingException
	 *           If a problem occurred while configuring the formatter that
	 *           formats the parsed usage records
	 * @throws AccountingParseException
	 *           If a problem occurs while parsing the usage records that come in
	 *           through the input stream
	 * @see #formatAcountingData()
	 * @see #formatAcountingData(PropertyMap, String)
	 */
	public String formatAcountingData(PropertyMap defaults)
			throws AccountingFormattingException, AccountingParseException {

		StringBuilder inputBuffer = new StringBuilder(INITIAL_INPUT_BUFFER_SIZE);
		try {
			while (true)
				inputBuffer.append(this.in.nextLine()).append(NEW_LINE);
		} catch (NoSuchElementException e) {
			/*
			 * For large input, it's cheaper to read until we hit an exception than to
			 * check for a new line every time
			 */
		}

		// Make sure we finished reading the input and didn't just get an
		// IOException
		IOException ioException = this.in.ioException();
		if (ioException != null) {
			throw new AccountingFormattingException("Unable to read input",
					ioException);
		}

		return this.formatAcountingData(defaults, inputBuffer.toString());
	}

	/**
	 * Reads usage records defined in the <code>usageRecords</code> string, parses
	 * them using the parser specified in this writer's <code>AppContext</code>
	 * and formats the resulting information, sending it to we writer's output.
	 * When values required for formatting are not included in the supplied usage
	 * records, the default values provided are used.
	 * 
	 * @param defaults
	 *          The default values provided when appropriate values are not
	 *          provided by the incoming usage records. Can be <code>null</code>
	 *          if no defaults are to be used
	 * @param usageRecords
	 *          The recored to parse.
	 * @return A summary of the operation in human readable format
	 * @throws AccountingFormattingException
	 *           If a problem occurred while configuring the formatter that
	 *           formats the parsed usage records
	 * @throws AccountingParseException
	 *           If a problem occurs while parsing the usage records that come in
	 *           through the input stream
	 */
	public String formatAcountingData(PropertyMap defaults, String usageRecords)
			throws AccountingFormattingException, AccountingParseException {
		// If mode is null, default properties will be used
		String mode = this.context.getInitParameter(PROP_MODE);

		Date parseStartDate = new Date();
		PropExpressionMap derivedProps = new PropExpressionMap();

		// Get the parser and policies we'll need
		PropertyContainerParser parser = this.getParser(mode);
		Set<UsageRecordPolicy> policies = this.getPolicies(mode);
		UsageRecordFormatter urWriter = new UsageRecordFormatter(this.context);

		PropertyFinder finder = parser.initFinder(this.context, null, mode);
		urWriter.init(this.context, finder, mode);

		Iterator<String> recordIterator = parser.splitRecords(usageRecords
				.toString());

		// Start everything going
		try {
			derivedProps=parser.getDerivedProperties(derivedProps);
			parser.startParse(defaults);
			urWriter.startFormatting(defaults);
			for (UsageRecordPolicy pol : policies) {
				derivedProps=pol.getDerivedProperties(derivedProps);
				pol.startParse(defaults);
			}
			

			
		} catch (Exception e) {
			throw new AccountingParseException("Unable to start parsing the records",
					e);
		}

		// Parse the records, one at a time
		ErrorSet errors = new ErrorSet();
		ErrorSet skip_list = new ErrorSet();
		int recordCounter = 0;
		while (recordIterator.hasNext()) {
			recordCounter++;
			String record = recordIterator.next();
			try {

				DerivedPropertyMap map = new DerivedPropertyMap(this.context);
				map.addDerived(derivedProps);
				if (defaults != null) {
					defaults.setContainer(map);
				}

				// Parse the record into a PropertyMap
				if (parser.parse(map, record))
					// Apply policies
					for (UsageRecordPolicy policy : policies) {
						policy.parse(map);
					}

				// add date and text
				map.setProperty(StandardProperties.INSERTED_PROP, parseStartDate);
				map.setProperty(StandardProperties.TEXT_PROP, record);

				// Write the result to this writer's output
				this.out.print(urWriter.format(map));

			} catch (SkipRecord s) {
				skip_list.add(s.getMessage(), record);
			} catch (AccountingParseException pe) {
				errors.add(pe.getMessage(), record, pe);
			} catch (Exception e) {
				errors.add("Unexpected parse error", record, e);
			}
		}

		// Generate and return a summary of parsing/formatting
		StringBuilder summary = new StringBuilder(recordCounter + " records read\n");
		summary.append(skip_list.toString());
		summary.append(parser.endParse());
		summary.append(errors.toString());
		for (UsageRecordPolicy pol : policies) {
			summary.append(pol.endParse());
		}
		summary.append(urWriter.endFormatting());

		String summaryString = summary.toString();

		return summaryString;
	}

	/*
	 * ##########################################################################
	 * PRIVATE METHODS
	 * ##########################################################################
	 */

	/**
	 * Fetches the parser specified in this writer's <code>AppContext</code>
	 * 
	 * @param mode
	 *          The mode this writer is working in (can be null)
	 * @return The parser to use
	 * @throws AccountingParseException
	 *           If there was a problem finding or constructing the parser
	 */
	@SuppressWarnings("unchecked")
	private PropertyContainerParser getParser(String mode)
			throws AccountingParseException {

		Properties ctxProperties = this.context.getService(ConfigService.class)
				.getServiceProperties();

		/*
		 * First try getting the parser for the specified mode. If that doesn't
		 * work, try getting a general parser for all modes
		 */
		String parserClassName = FilteredProperties.getProperty(ctxProperties,
				"class.parser", mode);
		if (parserClassName == null)
			throw new AccountingParseException("No parser specified");

		Class parserClass;
		try {
			parserClass = Class.forName(parserClassName);
		} catch (ClassNotFoundException e) {
			throw new AccountingParseException(
					"unable to find parser class on the classpath", e);
		}

		try {
			return (PropertyContainerParser) this.context.makeObject(parserClass);
		} catch (Exception e) {
			throw new AccountingParseException("Error making parser", e);
		}
	}

	/**
	 * Fetches the any policies specified in this writer's <code>AppContext</code>
	 * . Policies are not crucial so if something goes wrong while getting them,
	 * we just report an error to the <code>AppContext</code>. We don't throw an
	 * exception
	 * 
	 * @param mode
	 *          The mode this writer is working in (can be null)
	 * @return A set of policies to apply to parsed usage records
	 */
	@SuppressWarnings("unchecked")
	private Set<UsageRecordPolicy> getPolicies(String mode) {
		Logger logger = this.context.getService(LoggerService.class).getLogger(
				getClass());

		Properties ctxProperties = this.context.getService(ConfigService.class)
				.getServiceProperties();

		Set<UsageRecordPolicy> policies = new LinkedHashSet<UsageRecordPolicy>();
		String policyList = FilteredProperties.getProperty(ctxProperties,
				"policies", mode);
		logger.debug("policy list = " + policyList);

		if (policyList == null)
			return policies;

		for (String pol : policyList.trim().split(",")) {
			try {
				logger.debug("consider " + pol);
				Class polClass = Class.forName(pol);

				if (UsageRecordPolicy.class.isAssignableFrom(polClass)) {
					policies.add((UsageRecordPolicy) this.context.makeObject(polClass));
				} else {
					logger.debug("Bad policy: " + polClass);
					this.context.error("Bad Policy class " + pol);
				}
			} catch (Exception e) {
				this.context.error(e, "Error making policy ");
			}
		}
		return policies;
	}
}