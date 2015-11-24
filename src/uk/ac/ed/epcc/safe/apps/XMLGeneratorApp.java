// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.apps;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import uk.ac.ed.epcc.safe.accounting.ErrorSet;
import uk.ac.ed.epcc.safe.accounting.ogf.ur.XMLSplitter;
import uk.ac.ed.epcc.safe.accounting.reports.ReportBuilder;
import uk.ac.ed.epcc.safe.accounting.reports.ReportType;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.apps.Command;
import uk.ac.ed.epcc.webapp.apps.CommandLauncher;
import uk.ac.ed.epcc.webapp.apps.Option;
import uk.ac.ed.epcc.webapp.apps.Options;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
@uk.ac.ed.epcc.webapp.Version("$Id: XMLGeneratorApp.java,v 1.16 2014/09/15 14:32:30 spb Exp $")



public class XMLGeneratorApp implements Command {

	/**
	 * Usage information to print when help is requieted
	 */
	private static final String USAGE = "\n" +
			"\t" + XMLGeneratorApp.class.getSimpleName() + "\n" +
			"\n" +
			"\tGenerates XML records from a report template file.\n";
	
	private static final String desc = "Generates XML formatted records";

	private static final Options options = new Options();
	
	private static final Option OPT_HELP = 
		new Option(options, 'h', "help", "Print usage information and exit");

	private static final Option OPT_REPORT_TEMPLATE_FILE = 
		new Option(options, 'r', "report-template", true, "Specifiy a report template file");
	
	private static final Option OPT_REPORT_PARAM = new Option(options, 'S', true,
			"Specifiy a report parameter value.  e.g. -Sparam=val").setMultipleArgs();
	private static final Option OPT_START_DATE = new Option(options, 's',
			"start-date", true, "Specify a date to search from");
	
	private static final Option OPT_END_DATE = new Option(options, 'e',
			"end-date", true, "Specify a date to search to");
	private static final Option OPT_COUNT = new Option(options, 'n',
			"record-count",true,"Maximum number of record to generate");
	private static final Option OPT_OUTPUT_FILE = 
		new Option(options, 'f', "output", true, 
				"Specifiy the report file name");

	private AppContext conn;

	public XMLGeneratorApp(AppContext c) {
		conn = c;
	}
	
	public AppContext getContext() {
		return conn;
	}
	
	public String description() {
		return desc;
	}

	public String help() {
		StringBuilder sb = new StringBuilder();
		sb.append("Options:\n");
		sb.append(options.toString());
		return sb.toString();
	}

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
		
		// Process the options ----------------------------------------------------

		// HELP option
		if (opts.containsOption(OPT_HELP)) {
			System.out.println(USAGE);
			System.out.println("\nOPTIONS:");
			System.out.println(options);
			System.out.flush();
			CommandLauncher.exit(conn,0);
			return;
		}
		
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();
		
		String reportTemplateFileName = "format.xml";	
		File outputFile = null;
		
		// load data from an individual file
		if (opts.containsOption(OPT_REPORT_TEMPLATE_FILE)) {
			reportTemplateFileName = 
				opts.getOption(OPT_REPORT_TEMPLATE_FILE).getValue();
			
		}
		
		if (opts.containsOption(OPT_OUTPUT_FILE)) {
			outputFile = new File(opts.getOption(OPT_OUTPUT_FILE).getValue());
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
			cal.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY);
			cal.set(Calendar.HOUR_OF_DAY,0);
			cal.set(Calendar.MINUTE,0);
			cal.set(Calendar.SECOND,0);
			cal.set(Calendar.MILLISECOND,0);
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
		int max_records=100;
		if( opts.containsOption(OPT_COUNT)){
			try{
			max_records = Integer.parseInt(opts.getOption(OPT_COUNT).getValue());
			}catch(Exception e){
				CommandLauncher.die(e);
			}
		}
		params.put("StartDate", dateStart);
		params.put("EndDate", dateEnd);
		
		
		
		// Set individual report parameters option
		if (opts.containsOption(OPT_REPORT_PARAM)) {
			Option.Instance optReportParams = opts.getOption(OPT_REPORT_PARAM);
			setReportParams(optReportParams.getValues(), params);
		}
		
		try {
			Logger log = conn.getService(LoggerService.class).getLogger(getClass());
			ReportBuilder reportBuilder = ReportBuilder.getInstance(conn);
			ReportBuilder.setTemplate(conn, reportBuilder, reportTemplateFileName);
			ReportType reportType =reportBuilder.getReportType("OGFXML");
			params.put(ReportBuilder.REPORT_TYPE_PARAM, reportType);
			
			int skip=0;
			int count=100;
			boolean running = true;
			while(running && skip < max_records){
				running=false;
				if( (max_records -skip) < count){
					count=max_records-skip;
				}
				params.put("Skip",skip);
				params.put("Count",count);
				reportBuilder.setupExtensions(reportType,params);

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				reportBuilder.renderXML(params, out);
				if( reportBuilder.hasErrors()){
					Set<ErrorSet> err = reportBuilder.getErrors();
					for( ErrorSet es : err){
						es.report(conn);
					}
					break;
				}else{
					// records are split into seperate files
					XMLSplitter handler = new XMLSplitter();
					log.debug(out.toString());
					try {
						XMLReader parser = XMLReaderFactory.createXMLReader();
						parser.setContentHandler(handler);
						parser.parse(new InputSource(new StringReader(out.toString())));
					} catch (SAXException e) {
						throw new AccountingParseException(
								"Problem while separating OGF-UR usage records", e);
					}
					if (outputFile != null) {
						Iterator<String> iter = handler.iterator();
						while( iter.hasNext() ){
							running=true;
							FileWriter writer = new FileWriter(outputFile+"."+(skip++)+".xml");
							writer.append(iter.next());
							writer.close();
						}
					} else {
						Iterator<String> iter = handler.iterator();
						while( iter.hasNext() ){
							running=true;
							System.out.println(iter.next());
							skip++;
						}
					}
				}
			}
		} catch (Exception e) {
			conn.error(e,"Error generating report");
			CommandLauncher.die(e);
		}

	}

	private static String readFileAsString(String filePath)
			throws java.io.IOException {
		byte[] buffer = new byte[(int) new java.io.File(filePath).length()];
		java.io.BufferedInputStream f = new java.io.BufferedInputStream(
				new java.io.FileInputStream(filePath));
		f.read(buffer);
		f.close();
		return new String(buffer);
	}
	
	/**
	 * Sets the specified property
	 * 
	 * @param propKeyVals
	 *            The property key and value (expected format is 'key=value')
	 */
	static void setReportParams(List<String> propKeyVals, Map<String,Object> params) {

		for (String propKeyVal : propKeyVals) {
			int sepIndex = propKeyVal.indexOf('=');
			if (sepIndex < 0) {
				// This either means the last valus was a 
				CommandLauncher.die("attempt to set value of report parameter '"
						+ propKeyVal
						+ "' failed.  "
						+ "Couldn't find '=' character separating property key "
						+ "and value");
			}

			String key = propKeyVal.substring(0, sepIndex);
			try {
				String value = propKeyVal.substring(sepIndex + 1);
				params.put(key, value);
				
			} catch (ArrayIndexOutOfBoundsException e) {
				CommandLauncher.die("Unable to extract value of report " +
						"parameter '" + key + "'");
			}
		}

	}
}