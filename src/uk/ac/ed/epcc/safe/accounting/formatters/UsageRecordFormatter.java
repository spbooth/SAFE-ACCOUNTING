//| Copyright - The University of Edinburgh 2011                            |
//|                                                                         |
//| Licensed under the Apache License, Version 2.0 (the "License");         |
//| you may not use this file except in compliance with the License.        |
//| You may obtain a copy of the License at                                 |
//|                                                                         |
//|    http://www.apache.org/licenses/LICENSE-2.0                           |
//|                                                                         |
//| Unless required by applicable law or agreed to in writing, software     |
//| distributed under the License is distributed on an "AS IS" BASIS,       |
//| WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.|
//| See the License for the specific language governing permissions and     |
//| limitations under the License.                                          |
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.formatters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ed.epcc.safe.accounting.ErrorSet;
import uk.ac.ed.epcc.safe.accounting.formatters.value.DefaultFormatter;
import uk.ac.ed.epcc.safe.accounting.formatters.value.XMLDateTimeFormatter;
import uk.ac.ed.epcc.safe.accounting.formatters.value.XMLDurationFormatter;
import uk.ac.ed.epcc.safe.accounting.properties.FixedPropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.CurrentTimeService;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.TextFileOverlay;
import uk.ac.ed.epcc.webapp.model.TextFileOverlay.TextFile;
import uk.ac.ed.epcc.webapp.model.data.Duration;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;

/**
 * <p>
 * This formatter uses a template to construct usage records. The template may
 * contain variables which this formatter converts into values on a usage record
 * by usage record basis.
 * </p>
 * <p>
 * This object follows the same usage model as that used by parsers. The object
 * implements {@link Contexed}. It cannot be used until it's {@link #init}
 * method is called. To start formatting multiple usage records, the
 * {@link #startFormatting} method must be called. The {@link #format} method
 * can then be called multiple times. Each call returns a string representation
 * of the usage record whose values are specified by the input
 * <code>PropertyContainer</code> given to the {@link #format} method. The fomat
 * of the record is defined by the template this formatter uses.
 * </p>
 * <p>
 * The template should be located in a file. The location of the file should be
 * specified using a property in the <code>AppContext</code> of this formatter.
 * The property should have the name {@value #PROPERTY_BASE}.template or
 * {@value #PROPERTY_BASE}.<em>mode</em>.template where <em>mode</em> may be set
 * during initialisation of the formatter. The value of the property should be a
 * path to the template file. The path should either be relative to the Java
 * classpath, relative to the current working directory or an absolute path in
 * the file system (Java classpath is checked before the file system).
 * </p>
 * <p>
 * The template file may contain variables specified by
 * <code>${{prefix}prop_name{suffix}}</code> where <em>prop_name</em> is the name of a property
 * tag to have it's value replaced. Prefix and suffix are optional surrounding text that is only
 * inserted if the property exists.
 * Property tag names vary depending on the
 * parser used to parse the initial record. The standard rules for decoding
 * property tags apply (for example, derived property tags or specifying the
 * <code>PropertyRegistry</code> a tag belongs to using the colon -
 * ${reg:prop_name}). The variable cannot contain property expressions. If an
 * expression is required, a derived property must be defined using property
 * expressions. Any text that is not considered a variable is reproduced exactly
 * upon the output.
 * </p>
 * <p>
 * Properties are located by the <code>PropertyFinder</code> specified during
 * initialisation (when {@link #init} is called). In addition to the properties
 * the specified finder can find. Any PropertyTags used in this formatter's
 * {@link #REGISTRY PropertyRegistry} can be used. If there is a name clash
 * between registrys, the property names should be qualified by registry (using
 * the : as per usual).
 * 
 * <p>
 * The values of the properties may come from several sourses. A set of
 * <code>PropertyContainer</code>s are checked in the following order:
 * </p>
 * <ul>
 * <li>The container specified as an argument to {@link #format}</li>
 * <li>The container specified as an argument to {@link #startFormatting}</li>
 * <li>This formatter's internal container that contains properties that hold
 * values like the current date</li>
 * </ul>
 * <p>
 * Properties specified in this formatter's registry may be used. The registry
 * is called <em>formatter</code>.
 * Currently only one property is specified by formatter's container.  The property
 * <code>NOW</code> contains the date and time the formatter started parsing.  It
 * can be used by inserting the property <code>${NOW}</code> or by using it's qualified
 * name <code>${formatter:NOW}</code>
 * </p>
 * 
 * </p> <h4>Behaviour</h4>
 * <p>
 * Below are several characteristics of the formatter to bare in mind.
 * </p>
 * <p>
 * There is no escape character for variables. The character sequence
 * <em>${</em> (dollar open-brace) denote the start of a variable. However there
 * is one exception to this rule mentioned below.
 * </p>
 * <p>
 * Property names specified in variables must match a certain set of characters
 * to be identified as variables. If they don't match, the formatter will assume
 * the characters do not denote a variable. For example, assuming there is no
 * such property called <em>no_prop</em>. Using the variable
 * <code>${no_prop}</code> will result in an empty string replacing the variable
 * and a warning being logged during {@link #endFormatting} that the variable
 * couldn't be identified. However, the variable <code>${!no_prop}</code>
 * contains an exclamation mark. Property names don't contain exclamation marks
 * so this character sequence will not be considered a variable and will be
 * reproduced in the output with no warning. The same goes for variables that
 * span multiple lines because the new line character cannot be part of a
 * property name.
 * </p>
 * 
 * @author jgreen4
 * 
 */
@Deprecated


public class UsageRecordFormatter implements Contexed {

	// STATIC FIELDS ############################################################

	// DEVELOPERS: if REGISTRY's name changes, change this class' javadoc above
	/**
	 * Registry that holds property tags specific to formatting a document. For
	 * example, the date property tag NOW which holds the date and time formatting
	 * starts
	 */
	public static final PropertyRegistry REGISTRY = new PropertyRegistry(
			"formater","UsageRecordFormatter properties");
	/*
	 * DEVELOPERS: if more properties are added, describe them this class' javadoc
	 * above
	 */
	/**
	 * Holds the date/time when writing a record started
	 */
	public static final PropertyTag<Date> PROP_NOW = new PropertyTag<Date>(REGISTRY, "NOW",Date.class);
	/**
	 * The base of properties that set configuration parameters for this object
	 * start with
	 */
	public static final String PROPERTY_BASE = "ur";

	/** 
	 * The Constant REPORT_TEMPLATE_GROUP. 
	 */
	public static final String UR_TEMPLATE_GROUP = "ur-templates";
	
	/**
	 * Convenient holder for the system dependent line separator
	 */
	private static final String NEW_LINE = System.getProperty("line.separator");
	/**
	 * Regular expression that matches a property tag name. The name may, or may
	 * not be qualified
	 */
	private static final String PROPERTY_TAG_REGEXP = "(?:"
			+ FixedPropertyFinder.PROPERTY_FINDER_PREFIX_REGEXP + ")?"
			+ PropertyTag.PROPERT_TAG_NAME_PATTERN;
	/**
	 * The pattern that matches variables to be replaced with actual values in a
	 * usage record template
	 */
	private static final Pattern VAR_PATTERN = Pattern.compile("\\$\\{(?:\\{([^\\}]*)\\})?("
			+ PROPERTY_TAG_REGEXP + ")(?:\\{([^\\}]*)\\})?\\}");
	
	// INSTANCE FIELDS ##########################################################

	/**
	 * Records whether or not this formatter has started formatting. This is
	 * defined as the period between a call to {@link #startFormatting} and a call
	 * to {@link #endFormatting}
	 */
	private boolean isFormatting = false;
	/**
	 * Records whether or not init(...) has been called
	 */
	private boolean isInitialised = false;
	/**
	 * The template that will be used when formatting usage records
	 */
	private String template;
	/**
	 * The matcher that spans usage record template and is used for find-replace
	 * operations for variables
	 */
	private Matcher templateVariableMatcher;
	/**
	 * The context in which this object operates
	 */
	private AppContext context;
	/**
	 * For recording errors during writing. The errors are reported as the return
	 * argument of the {@link #endFormatting()} method
	 */
	private ErrorSet errors;
	
	/**
	 * The defaults specified when {@link #startFormatting(PropertyContainer)} is
	 * called
	 * 
	 */
	private PropertyContainer defaults;
	/**
	 * Contains entries from this formatter's registry
	 */
	private PropertyMap internalDefaults;
	/**
	 * The finder specified during {@link #init}. This is the main finder used for
	 * locating <code>PropertyTag</code>s found in the template
	 */
	private PropertyFinder finder;

	/**
	 * Constructs a new <code>UsageRecordFormatter</code>. Note that the this
	 * object shouldn't be used until it's {@link #init} method has been called.
	 * The context specified in the constructor is not really used - the one
	 * specified in {@link #init} takes presidence. However, this object
	 * implements {@linkplain Contexed} <code>context</code> so provides a
	 * constructor that takes an <code>AppContext</code> as {@linkplain Contexed}
	 * requires
	 * 
	 * @param context
	 *          The context under which this object operates
	 */
	public UsageRecordFormatter(AppContext context) {
		/*
		 * This should be reset during init(...) but this class implements Contexed
		 * (because it does rely on an AppContext) so we make sure the AppContext is
		 * always present, even if the object isn't valid until the init(...) method
		 * is called
		 */
		this.context = context;
	}

	/**
	 * Informs this object that formatting has finished. Any non-fatal errors or
	 * warnings that occurred during formatting are returned as a message in
	 * <code>String</code> format.
	 * 
	 * @return Any error messages that have accumulated during formatting
	 * @throws IllegalStateException
	 * @see #isFormatting
	 */
	public String endFormatting() throws IllegalStateException {
		/*
		 * we don't need to call checkState() here it doesn't really matter if the
		 * formatter has been initialised or not
		 */

		if (this.isFormatting == false) {
			// startFormatting(...) needs to be called first
			throw new IllegalStateException(
					"Cannot end formatting.  Formatting has not been started yes");
		}

		this.isFormatting = false;
		return this.errors.toString();
	}

	/**
	 * Tests to see if this formatter is in the process of formatting.
	 * 
	 * @return <code>true</code> if {@link #startFormatting} has already been
	 *         called but {@link #endFormatting()} has not been called yet.
	 *         <code>false</code> otherwise
	 */
	public boolean isFormatting() {
		return this.isFormatting;
	}

	/**
	 * Formats a usage record. The template for the usage record will have been
	 * obtained from the <code>AppContext</code>'s configuration properties.
	 * 
	 * @param container
	 *          The container holding all the values to be used to replace the
	 *          variables in the template.
	 * @return The usage record template with all variables replaced with values
	 *         contained in <code>container</code> or the default
	 *         <code>PropertyContainer</code> provided during
	 *         {@link #startFormatting}
	 * @throws IllegalStateException
	 *           If {@link #startFormatting} has not been called yet
	 */
	public String format(PropertyContainer container)
			throws IllegalStateException {
		this.checkState();

		if (this.isFormatting == false) {
			// startFormatting(...) needs to be called first
			throw new IllegalStateException(
					"Cannot formatting.  Formatting has not been started yes");
		}

		this.templateVariableMatcher.reset();
		StringBuffer recordText = new StringBuffer(this.template.length());

		while (this.templateVariableMatcher.find()) {
			String prefix = this.templateVariableMatcher.group(1);
			String variable = this.templateVariableMatcher.group(2);
            String suffix = this.templateVariableMatcher.group(3);
            if( prefix == null ){
            	prefix="";
            }
            if( suffix == null ){
            	suffix="";
            }
			String value = this.findValue(variable, container);
			if (value == null){
				value = "";
			}else{
				value = prefix+value+suffix;
			}
			this.templateVariableMatcher.appendReplacement(recordText, value);
		}
		// Append the rest of the record to the text
		this.templateVariableMatcher.appendTail(recordText);

		return recordText.toString();
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
	 * Initialises this formatter. Properties used to configure the formatter will
	 * be fetched from <code>context</code>. Most importantly, the template to use
	 * must be specified in the configuration properties of <code>context</code>.
	 * 
	 * @param context
	 *          The context under which this finder operates
	 * @param finder
	 *          The <code>PropertyFinder</code> this formatter will use to find
	 *          properties specified in the variables of the template.
	 * @param mode
	 *          The mode in which this formatter operates (can be
	 *          <code>null</code>)
	 * @throws IllegalStateException
	 *           If the formatter is in the process of formatting
	 * @see #isFormatting
	 */
	public void init(AppContext context, PropertyFinder finder, String mode)
			throws IllegalStateException {
		if (this.isFormatting)
			throw new IllegalStateException(
					"Cannot initialise formatter.  Formatting is already in progress");

		assert context != null : "The AppContext should not be null";
		assert finder != null : "The PropertyFinder should not be null";

		this.context = context;
		// this should occur in startWrite(...) but added here too just to be safe
		this.errors = new ErrorSet();

		// Add the internal property finder to the finder provided
		MultiFinder mFinder = new MultiFinder();
		mFinder.addFinder(REGISTRY);
		mFinder.addFinder(finder);
		this.finder = mFinder;

		
		this.template = this.getTemplate();
		this.templateVariableMatcher = VAR_PATTERN.matcher(this.template);
		this.isInitialised = true;
		this.isFormatting = false;
	}

	/**
	 * Prepares this formatter for formatting one or more usage records. The
	 * values provided in <code>defaults</code> will be used if a required
	 * property is not present in the <code>PropertyContainer</code> provided to
	 * the {@link #format} method when formatting a particular usage record
	 * 
	 * @param defaults
	 *          property-value pairs to use if required properties are not present
	 *          during the formatting of a particular usage record. Can be
	 *          <code>null</code> if no defaults are required
	 * @throws IllegalStateException
	 *           If this formatter hasn't been initialised yet
	 * @see #init
	 */
	public void startFormatting(PropertyContainer defaults)
			throws IllegalStateException {
		this.checkState();

		if (this.isFormatting) {
			throw new IllegalStateException(
					"Cannot start formatting.  Formatting is already in progress");
		}

		this.defaults = defaults;

		this.internalDefaults = new PropertyMap();
		CurrentTimeService serv = getContext().getService(CurrentTimeService.class);
		this.internalDefaults.setProperty(PROP_NOW, serv.getCurrentTime());

		this.errors = new ErrorSet();
		this.isFormatting = true;
	}

	/*
	 * ##########################################################################
	 * PRIVATE METHODS
	 * ##########################################################################
	 */

	/**
	 * Make sure init has been called. Throw IllegalStateException if it hasn't
	 */
	private void checkState() throws IllegalStateException {
		if (this.isInitialised == false)
			throw new IllegalStateException("formatter has not been initialised yet");
	}

	/**
	 * <p>
	 * Finds the value of a property in one of the many
	 * <code>PropertyContainer</code>s this formatter uses. The containers are
	 * checked in the following order.
	 * </p>
	 * <ul>
	 * <li><code>container</code> - the <code>PropertyContainer</code> provided as
	 * an argument to this method</li>
	 * <li>The default <code>PropertyContainer</code> if one was provided during
	 * {@link #startFormatting}</li>
	 * <li>The internal <code>PropertyContainer</code> which holds special
	 * properties that vary depending on when/how the formatter is used</li>
	 * <li></li>
	 * </ul>
	 * <p>
	 * If a property is not found in one, the next one is checked. If a value is
	 * not found at all, the error is logged in the internal <code>ErrorSet</code>
	 * and an empty string is returned.
	 * </p>
	 * 
	 * @param variableName
	 *          The variable to find the value for. The format expected is
	 *          ${prop-name} where prop-name is the name of the property whose
	 *          value should be returned
	 * @param container
	 * @return The value of the variable specified, or an empty string if no
	 *         variable could be found with the specified name
	 */
	@SuppressWarnings("unchecked")
	private String findValue(String propertyTagName, PropertyContainer container) {
		// Strip off the '${' and '}' bits that surround the variable
		PropertyTag tag = this.finder.find(propertyTagName);
		if (tag == null) {
			this.errors.add("unable to identify property '" + propertyTagName + "'",
					this.template);
			return null;
		}

		// Get the value
		Object value = null;
		try {
			if (container.supports(tag))
				value = container.getProperty(tag);
			if (value == null && this.defaults != null && defaults.supports(tag))
				value = this.defaults.getProperty(tag);
			if (value == null && this.internalDefaults.supports(tag))
				value = this.internalDefaults.getProperty(tag);
		} catch (InvalidExpressionException e2) {
			/*
			 * Should never happen as we checked each container supported 'tag' before
			 * trying to get it. Nevertheless, behaviour is the same as if value isn't
			 * found so leaving value=null and doing nothing here is fine
			 */
		}

		if (value == null) {
			return null;
		}

		if (value instanceof Date)
			return XMLDateTimeFormatter.FORMATTER.format((Date) value);
		else if (value instanceof Duration)
			return XMLDurationFormatter.FORMATTER.format((Duration) value);
		else
			return DefaultFormatter.FORMATTER.format(value);
	}

	/**
	 * Fetches the template to use for usage records. The template location should
	 * be specified in the configuration properties of this formatter's
	 * <code>AppContext</code>. The path to the template should either be relative
	 * to the current Java classpath, relative to the current working directory or
	 * and absolute path in the file system.
	 * 
	 * @return The template to use when constructing usage records
	 * @throws IOException 
	 */
	
	private String getTemplate() {
		Reader reader = null;
		BufferedReader bufferedReader = null;
		StringBuilder sb = null;
		
		// TODO let this be able to define it's own overlay table
		// See ReportBuilder constructor in webacct.
		String urTemplateName = context.getInitParameter("ur-template");	
		
		TextFileOverlay urTemplateOverlay = new TextFileOverlay(context);			
		try {
			TextFile templateTextFile = 
				urTemplateOverlay.find(UR_TEMPLATE_GROUP, urTemplateName);
			
			if ( templateTextFile == null)
			{
				throw new DataFault("Couldnt find template " + urTemplateName + " in database");
			}
			
			reader = templateTextFile.getDataReader();			
			if (reader == null) {
				throw new IOException("Couldn't find template on the classpath");
			}
			
			bufferedReader = new BufferedReader(reader);
			sb = new StringBuilder(1000);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				sb.append(line).append(NEW_LINE);
			}
			return sb.toString();
			
		} catch (DataFault e1) {
			e1.printStackTrace();
			
		} catch (IOException e) {
			getLogger().error("Unable to obtain the OGF usage record template '"
					+ urTemplateName
					+ "'.  An empty string will be used for the template",e);
			sb = new StringBuilder("");
			
		} finally {
			// Tidy up by closing the reader (if we actually got round to opening it)
			if (reader != null) {
				try {
					reader.close();
					if( bufferedReader != null ){
						bufferedReader.close();
					}
				} catch (IOException e) {
					getLogger().error("Unable to close the input stream "
							+ "used to read the OGF usage record template");
				}
			}
			
		}		
		return "";

	}
	private Logger getLogger(){
		return context.getService(LoggerService.class).getLogger(getClass());
	}
}