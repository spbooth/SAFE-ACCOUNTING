// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.apps;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Iterator;
import java.util.Date;

import java.io.File;
import java.io.FileWriter;




import uk.ac.ed.epcc.safe.accounting.AccountingService;
import uk.ac.ed.epcc.safe.accounting.UsageManager;
import uk.ac.ed.epcc.safe.accounting.UsageRecord;
import uk.ac.ed.epcc.safe.accounting.formatters.UsageRecordFormatter;
import uk.ac.ed.epcc.safe.accounting.formatters.UsageRecordWriter;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;


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
 * @author Malcolm
 * @see UsageRecordFormatter
 * @see UsageRecordWriter
 */
@Deprecated
@uk.ac.ed.epcc.webapp.Version("$Id: UsageRecordWriterFromDBApp.java,v 1.15 2014/09/15 14:32:30 spb Exp $")

public class UsageRecordWriterFromDBApp implements Command {

	/**
	 * Usage information to print when help is requieted
	 */
	private static final String USAGE = "\n\t"
			+ UsageRecordWriterFromDBApp.class.getSimpleName() + "[options]\n\n"
			+ "Reads usage records files from a configured Grid-SAFE instance and prints them in "
			+ "" + "OGF usage record format to standard output.  "
			+ "Records are selected from between two input dates, which must be specified."
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

	
	
	private static final Option OPT_OUTPUT_FILE = new Option(options, 'f',
			"output", true, "Specify a file to which the records will be output");

	private static final Option OPT_TEMPLATE = new Option(options, 't',
			"template", true, "The template file to use when formatting the output");
	
	private static final Option OPT_TEMPLATE_DIRECTORY = new Option(options, 'd',
			"template-directory", true, "The directory in which to find the template file");
	
	private static final Option OPT_START_DATE = new Option(options, 's',
			"start-date", true, "Specify a date to search from");
	
	private static final Option OPT_END_DATE = new Option(options, 'e',
			"end-date", true, "Specify a date to search to");
	
	private static final String desc = "Outputs records as formatted XML from a Grid-SAFE database";


	private Properties specifiedProperties = new Properties();
	private PrintStream summaryOutput = System.err;
	private Options.Instance opts;
	
	
	
	
	public String help() {
		StringBuilder sb = new StringBuilder();
		sb.append("Options:\n");
		sb.append(options.toString());
		return sb.toString();
	}
	
	public String description() {
		return desc;
	}
	
	
	 private AppContext conn;
	    public UsageRecordWriterFromDBApp(AppContext c){
	    	conn=c;
	    }


	

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
		
		// HELP option
		if (opts.containsOption(OPT_HELP)) {
			System.out.println(USAGE);
			System.out.println("\nOPTIONS:");
			System.out.println(options);
			return;
		}
		
		
		Date dateStart = null;
		Date dateEnd = null;
		
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
	    
		
		// check for a start date
		if (opts.containsOption(OPT_START_DATE)) {
			String startDateString = opts.getOption(OPT_START_DATE).getValue();
		
			try
			{
				dateStart = df.parse(startDateString);
			} catch (Exception e)
			{
				CommandLauncher.die(new Exception("Invalid date " + startDateString, e));
			}

		}else{
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.WEEK_OF_YEAR, -1);
			dateStart=cal.getTime();
		}
		
		// check for an end date, take today as the default if not supplied
		if (opts.containsOption(OPT_END_DATE)) {
			String endDateString = opts.getOption(OPT_END_DATE).getValue();
			try
			{
				dateEnd = df.parse(endDateString);
			} catch (Exception e)
			{
				CommandLauncher.die(new Exception("Invalid date " + endDateString, e));
			}
		} else
		{
			dateEnd = new Date(System.currentTimeMillis());
		}
		
		
		
		// get the output formatting template name. A match must exist in the database
		if (opts.containsOption(OPT_TEMPLATE)) {
			String templateLoc = opts.getOption(OPT_TEMPLATE).getValue();
			setTemplateLoc(templateLoc);
		}
		
		// get the real location of the template file
		// this is not needed if the template exists in text form inside the database
		if (opts.containsOption(OPT_TEMPLATE_DIRECTORY)) {
			String templateDirectory = opts.getOption(OPT_TEMPLATE_DIRECTORY).getValue();
			setTemplateDirectory(templateDirectory);
		}
		
		// if an output filename is specified, records are written to it.
		// if not, records are written to stdout
		String outputFileName = null;
		if (opts.containsOption(OPT_OUTPUT_FILE)) {
			outputFileName = opts.getOption(OPT_OUTPUT_FILE).getValue();
		}
		
			// Set the properties of the AppContext -----------------------------------
		if (specifiedProperties.size() > 0) {
			
			ConfigService newConfig = new OverrideConfigService(
					specifiedProperties, conn);
			conn.setService( newConfig);
		}
		
		// get the default accounting setup, we dont want to know the gory details
		AccountingService ac = conn.getService(AccountingService.class);
		@SuppressWarnings("unchecked")
		UsageManager<? extends UsageRecord> um = ac.getUsageManager();
		
		
		// construct a data-based query from the default accounting table
		AndRecordSelector sel = new AndRecordSelector();
		sel.add(new SelectClause<Date>(StandardProperties.ENDED_PROP,MatchCondition.GT,dateStart));
		sel.add(new SelectClause<Date>(StandardProperties.ENDED_PROP,MatchCondition.LE,dateEnd));
		
		
		PropertyFinder finder = um.getFinder();
		UsageRecordFormatter urf = new UsageRecordFormatter(conn);
		urf.init(conn, finder, null);
		urf.startFormatting(null);
		
		File outputRecordFile = null;
		FileWriter fw = null;
		
		if (outputFileName != null)
		{
			outputRecordFile = new File(outputFileName);
			
			try
			{
				outputRecordFile.createNewFile();
				fw = new FileWriter(outputRecordFile);
			} catch (Exception e)
			{
				CommandLauncher.die(e);
			}
			
			
		}
		
		
		try {
			for(Iterator<? extends UsageRecord> it = um.getIterator(sel); it.hasNext();){
				UsageRecord rec = it.next();
						
				String formattedRecord = urf.format(rec);
				
				// check if we are outputting to a file or to stdout
				if ( fw != null)
				{
					fw.write(formattedRecord);
				} else
				{
					System.out.println(formattedRecord);
				}
			}
		} catch (Exception e) {
			conn.error(e,"Error formatting records");
		}
		
		if( fw != null ){
		try
		{
			fw.close();
		} catch (Exception e)
		{
			conn.error(e,"Error closing file");
		}
		}
		// print any errors to stdout.
		String formattingErrors = urf.endFormatting();
		System.err.println(formattingErrors);
		
	}
	
	
	

	
	
	
	

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
	private void setTemplateLoc(String location) {
				
		this.specifiedProperties.setProperty("ur-template", location);
		
	}
	
	
	
	private void setTemplateDirectory(String location) {
		this.specifiedProperties.setProperty("ur-templates", location);
	}
	
	
	public AppContext getContext() {
		return conn;
	}
}