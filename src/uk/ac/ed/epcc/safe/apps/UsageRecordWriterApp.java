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
package uk.ac.ed.epcc.safe.apps;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import uk.ac.ed.epcc.safe.accounting.formatters.AccountingFormattingException;
import uk.ac.ed.epcc.safe.accounting.formatters.UsageRecordFormatter;
import uk.ac.ed.epcc.safe.accounting.formatters.UsageRecordWriter;
import uk.ac.ed.epcc.safe.accounting.parsers.OGFXMLRecordParser;
import uk.ac.ed.epcc.safe.accounting.parsers.PbsParser;
import uk.ac.ed.epcc.safe.accounting.parsers.SgeParser;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.webapp.AppContext;

import uk.ac.ed.epcc.webapp.apps.Command;
import uk.ac.ed.epcc.webapp.apps.CommandLauncher;
import uk.ac.ed.epcc.webapp.apps.Options;
import uk.ac.ed.epcc.webapp.apps.Option;
import uk.ac.ed.epcc.webapp.config.ConfigService;
import uk.ac.ed.epcc.webapp.config.OverrideConfigService;

/**
 * Front end command line application for parsing usage records and formatting
 * them using the specified formatting template. The application may be
 * configured via command line options. As these options may be added over time,
 * the best documentation on what options are available can be found by running
 * this application with the <em>-h</em> or <em>--help</em> flag
 * 
 * @author jgreen4
 * @see UsageRecordFormatter
 * @see UsageRecordWriter
 */
@Deprecated


public class UsageRecordWriterApp implements Command {

	/**
	 * Usage information to print when help is requieted
	 */
	private static final String USAGE = "\n\t"
			+ UsageRecordWriterApp.class.getSimpleName() + "[options]\n\n"
			+ "Reads usage records files from standard input and prints them in "
			+ "" + "OGF usage record format to standard output.  "
			+ "The type of input record must be specified.\n\n"
			+ "Configuration propertes be specified either via a properties "
			+ "file (see class documentation for necessary contents) and/or with "
			+ "command line options\n\n"
			+ "Properties in the properties file may be specified by 'mode' by "
			+ "adding a dot(.) and the mode name to the property key.  "
			+ "This allows multiple sets of options to be stored in one file.  "
			+ "Properties without a mode are used as the default for all modes."
			+ "The mode may be set with the appropriate option.  "
			+ "I a mode isn't set, default properties only are used";

	/*
	 * ##########################################################################
	 * COMMAND LINE OPTIONS
	 * ##########################################################################
	 */

	private static final Options options = new Options();

	private static final Option OPT_HELP = new Option(options, 'h', "help",
			"Print usage information and exit");

	private static final Option OPT_MODE = new Option(options, 'm', "mode", true,
			"the mode in which to operate - default is no mode");

	private static final Option OPT_PROP = new Option(options, 'P', true,
			"Specifiy a property value.  e.g. -Pprop=val").setMultipleArgs();

	private static final Option OPT_PROPS_FILE = new Option(options, 'p',
			"properties", true, "Specify a properties file to load");

	private static final Option OPT_TYPE = new Option(options, 'r',
			"record-type",
			"The format of the input records ('pbs', 'sge' or 'ogf-ur')");

	private static final Option OPT_TEMPLATE = new Option(options, 't',
			"template", "The template file to use when formatting the output");
	
	
	private static final String desc = 
		"Converts xml records from a file to another format using a template file";

	/*
	 * ##########################################################################
	 * STATIC FIELDS USED IN main
	 * ##########################################################################
	 */

	private String mode = null;
	private Properties specifiedProperties = new Properties();
	private PrintStream summaryOutput = System.err;
	private Options.Instance opts;
	
	
	 private AppContext conn;
	    public UsageRecordWriterApp(AppContext c){
	    	conn=c;
	    }

	/**
	 * 
	 * Constructs a new <code>UsageRecordWriterApp</code> to hold application
	 * data. The constructor parses the command line arguments and builds an
	 * instance of all options that were specified
	 * 
	 * @param args
	 *          The command line arguments to parse
	 * @throws IllegalArgumentException
	 *           If there was a problem with one of the options specified in the
	 *           command line arguments
	 * @throws IllegalStateException
	 *           If an value was assigned to an option that couldn't take more
	 *           values or couldn't take values at all
	 */
	private UsageRecordWriterApp(String... args) throws IllegalArgumentException,
			IllegalStateException {
		opts = options.newInstance(args);
		opts.validate();
	}

	/*
	 * ##########################################################################
	 * main
	 * ##########################################################################
	 */

	/**
	 * The application
	 * 
	 * @param args
	 *          The command line arguments
	 */
	public void run(LinkedList<String> args) {
		
		Options.Instance opts = options.newInstance();
		
		try {
			
			opts.parse(args);
		} catch (IllegalArgumentException e) {
			CommandLauncher.die(e);
			return; // This will never happen but java can't spot that.
		} catch (IllegalStateException e) {
			CommandLauncher.die(e);
			return; // This will never happen but java can't spot that.
		}

		
		// We will need to know the default mode for some of our options.
		// It's ok if it's not specified and we get null back. The rest of the code
		// will handle that
		mode = conn.getInitParameter(UsageRecordWriter.PROP_MODE);

		// Process the options ----------------------------------------------------

		// HELP option
		if (opts.containsOption(OPT_HELP)) {
			System.out.println(USAGE);
			System.out.println("\nOPTIONS:");
			System.out.println(options);
			System.exit(0);
		}

		// PROPERTIES option
		if (opts.containsOption(OPT_PROPS_FILE)) {
			String propFileName = opts.getOption(OPT_PROPS_FILE).getValue();
			loadProps(propFileName);
		}

		// Set individual properties option
		if (opts.containsOption(OPT_PROP)) {
			Option.Instance optProp = opts.getOption(OPT_PROP);
			setProp(optProp.getValues());
		}

		// Set the mode of operation
		// WARNING: this has to be done before OPT_TYPE is checked
		if (opts.containsOption(OPT_MODE)) {
			mode = opts.getOption(OPT_MODE).getValue();
			specifiedProperties
					.setProperty(UsageRecordWriter.PROP_MODE, mode);
		}

		if (opts.containsOption(OPT_TYPE)) {
			String type = opts.getOption(OPT_TYPE).getValue();
			setInputRecordType(mode, type);
		}

		if (opts.containsOption(OPT_TEMPLATE)) {
			String templateLoc = opts.getOption(OPT_TYPE).getValue();
			setTemplateLoc(mode, templateLoc);
		}

		// Set the properties of the AppContext -----------------------------------
		if (specifiedProperties.size() > 0) {
			
			ConfigService newConfig = new OverrideConfigService(
					specifiedProperties, conn);
			conn.setService(newConfig);
		}

		// Run the formatter ------------------------------------------------------
		UsageRecordWriter writer = new UsageRecordWriter(conn);
		writer.setInput(System.in);
		writer.setOutput(System.out);

		try {
			String result = writer.formatAcountingData();
			summaryOutput.println(result);
		} catch (AccountingFormattingException e) {
			CommandLauncher.die(e);
		} catch (AccountingParseException e) {
			CommandLauncher.die(e);
		}
	}

	/*
	 * ##########################################################################
	 * PRIVATE METHODS
	 * ##########################################################################
	 */

	

	/**
	 * Loads properties from the specified file
	 * 
	 * @param fileName
	 *          The location and name of the file where the application's
	 *          properties are
	 */
	private void loadProps(String fileName) {
		try {
			this.specifiedProperties.load(new FileInputStream(fileName));
		} catch (IOException e) {
			CommandLauncher.die(e);
		}
	}

	/**
	 * Sets the type of record this application will parse. The appropriate parser
	 * is selected using this method
	 * 
	 * @param mode
	 *          The mode set - if any
	 * @param type
	 *          The type of record that will be parsed
	 */
	private void setInputRecordType(String mode, String type) {
		String key = "class.parser";
		if (mode != null)
			key += "." + mode;

		/*
		 * DEVELOPERS: if another record type is added here, add it to the list of
		 * record types in the OPT_TYPE property description (at the top of this
		 * class) as well.
		 */
		if (type.equals("pbs"))
			this.specifiedProperties.setProperty(key, PbsParser.class.getName());
		else if (type.equals("sge"))
			this.specifiedProperties.setProperty(key, SgeParser.class.getName());
		else if (type.equals("ogfur") || type.equals("ogf-ur")) {
			this.specifiedProperties.setProperty(key, OGFXMLRecordParser.class
					.getName());
		} else
			CommandLauncher.die("unknown record type '" + type + "'");
	}

	/**
	 * Sets the specified property
	 * 
	 * @param propKeyVals
	 *          The property key and vlaue (expected format is 'key=value')
	 */
	private void setProp(List<String> propKeyVals) {

		for (String propKeyVal : propKeyVals) {
			int sepIndex = propKeyVal.indexOf('=');
			if (sepIndex < 0) {
				CommandLauncher.die("attempt to set value of property '" + propKeyVal + "' failed.  "
						+ "Couldn't find '=' character separating property key and value");
			}

			String key = propKeyVal.substring(0, sepIndex);
			try {
				String value = propKeyVal.substring(sepIndex + 1);
				this.specifiedProperties.setProperty(key, value);
			} catch (ArrayIndexOutOfBoundsException e) {
				CommandLauncher.die("Unable to extract value of property '" + key + "'");
			}
		}
	}

	/**
	 * Sets the location of the template the formatter will use to format the
	 * output data
	 * @param mode
	 *          The mode set - if any
	 * @param location
	 *          The location and name of the template file
	 */
	private void setTemplateLoc(String mode, String location) {
		String key = UsageRecordFormatter.PROPERTY_BASE;
		if (mode != null)
			key += key + "." + mode;
		key += ".template";

		this.specifiedProperties.setProperty(key, location);
	}

	public String description() {
		return desc;
	}

	public String help()
	{
			StringBuilder sb = new StringBuilder();
			sb.append("Options:\n");
			sb.append(options.toString());
			return sb.toString();
	}


	public AppContext getContext() {
		return conn;
	}
}