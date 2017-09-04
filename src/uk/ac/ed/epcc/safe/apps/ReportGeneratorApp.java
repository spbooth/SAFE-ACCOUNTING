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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;

import uk.ac.ed.epcc.safe.accounting.ErrorSet;
import uk.ac.ed.epcc.safe.accounting.reports.ReportBuilder;
import uk.ac.ed.epcc.safe.accounting.reports.ReportType;
import uk.ac.ed.epcc.safe.accounting.reports.ReportTypeRegistry;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.apps.Command;
import uk.ac.ed.epcc.webapp.apps.CommandLauncher;
import uk.ac.ed.epcc.webapp.apps.Option;
import uk.ac.ed.epcc.webapp.apps.Options;
import uk.ac.ed.epcc.webapp.forms.BaseForm;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.action.FormAction;
import uk.ac.ed.epcc.webapp.forms.exceptions.ActionException;
import uk.ac.ed.epcc.webapp.forms.result.FormResult;
import uk.ac.ed.epcc.webapp.forms.result.MessageResult;
import uk.ac.ed.epcc.webapp.forms.result.ServeDataResult;
import uk.ac.ed.epcc.webapp.forms.swing.JFormDialog;
import uk.ac.ed.epcc.webapp.forms.swing.SwingContentBuilder;
import uk.ac.ed.epcc.webapp.forms.swing.SwingFormComponentListener;
import uk.ac.ed.epcc.webapp.forms.swing.SwingTransitionHandler;
import uk.ac.ed.epcc.webapp.forms.text.CommandLineForm;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.stream.ByteArrayMimeStreamData;
import uk.ac.ed.epcc.webapp.model.serv.SettableServeDataProducer;
import uk.ac.ed.epcc.webapp.session.SessionDataProducer;
import uk.ac.ed.epcc.webapp.session.SessionService;




public class ReportGeneratorApp implements Command {

	/**
	 * Usage information to print when help is requieted
	 */
	private static final String USAGE = "\n" +
			"\t" + ReportGeneratorApp.class.getSimpleName() + "\n" +
			"\n" +
			"\tGenerates a report from a report template file.\n";
	
	private static final String desc = "Generates reports";

	private static final Options options = new Options();
	
	private static final Option OPT_HELP = 
		new Option(options, 'h', "help", "Print usage information and exit");

	private static final Option OPT_VERB = 
		new Option(options, 'v', "verbose", "Show additional information");

	private static final Option OPT_REPORT_TEMPLATE_FILE = 
		new Option(options, 'r', "report-template", true, "Specifiy a report template file").setRequired(true);
	
	private static final Option OPT_REPORT_PARAM = new Option(options, 'S', true,
			"Specifiy a report parameter value.  e.g. -Sparam=val").setMultipleArgs();
	
	private static final Option OPT_REPORT_TYPE = 
		new Option(options, 't', "type", true, 
				"Specifiy the type of report, 'html' (default), 'pdf', 'xml' or 'csv'").setRequired(true);
	
	private static final Option OPT_OUTPUT_FILE = 
		new Option(options, 'f', "output", true, 
				"Specifiy the report file name");
	private static final Option OPT_GUI = new Option(options,'g',"gui",false,"Use graphical user interface");

	private AppContext conn;

	public ReportGeneratorApp(AppContext c) {
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
		boolean verbose = opts.containsOption(OPT_VERB);
		// Get the params values from the Form
		Map<String,Object> params = new HashMap<String,Object>();
		
		String reportTemplateFileName = null;	
		File outputFile = null;
		
		// load data from an individual file
		if (opts.containsOption(OPT_REPORT_TEMPLATE_FILE)) {
			reportTemplateFileName = 
				opts.getOption(OPT_REPORT_TEMPLATE_FILE).getValue();
			
		}else{
			System.out.println("No report template specified");
			CommandLauncher.exit(conn,0);
		}
		
		if (opts.containsOption(OPT_OUTPUT_FILE)) {
			outputFile = new File(opts.getOption(OPT_OUTPUT_FILE).getValue());
		}
		String type = "pdf";
		if (opts.containsOption(OPT_REPORT_TYPE)) {
			type= opts.getOption(OPT_REPORT_TYPE).getValue();
			
			
		}
		
		
		// Set individual report parameters option
		if (opts.containsOption(OPT_REPORT_PARAM)) {
			Option.Instance optReportParams = opts.getOption(OPT_REPORT_PARAM);
			setReportParams(optReportParams.getValues(), params);
		}
		params.put("CurrentTime", new Date(System.currentTimeMillis()));
		SessionService sess = conn.getService(SessionService.class);
		if( sess != null && sess.haveCurrentUser()){
			params.put("AuthenticatedUser", sess.getCurrentPerson());
		}
		boolean gui = opts.containsOption(OPT_GUI);
		JFrame frame=null;
		try {			
			ReportBuilder reportBuilder = ReportBuilder.getInstance(conn);
			ReportBuilder.setTemplate(conn, reportBuilder, reportTemplateFileName);
			ReportType reportType =reportBuilder.getReportTypeReg().getReportType(type);
			params.put(ReportTypeRegistry.REPORT_TYPE_PARAM, reportType);
			reportBuilder.setupExtensions(reportType,params);
			if( reportBuilder.hasReportParameterDefs()){
				if( gui){
					frame = new JFrame("ReportGenerator");
					SwingFormComponentListener listener = new SwingFormComponentListener(getContext());
					Form f = new BaseForm(conn);
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					reportBuilder.buildReportParametersForm(f, params);
					f.addAction("Submit", new FormAction(){

						@Override
						public FormResult action(Form f) throws ActionException {
							return new MessageResult("OK");
						}
						
					});
					if( reportBuilder.hasErrors()){
						Set<ErrorSet> err = reportBuilder.getErrors();
						for( ErrorSet es : err){
							es.report(conn);
						}
						CommandLauncher.die("Form build failed");
					}
					JFormDialog dialog = new JFormDialog(conn,frame);
					SwingContentBuilder builder = dialog.getContentBuilder();
					builder.addFormTable(conn, f);
					builder.addActionButtons(f);
					FormResult result = dialog.showForm(f);
					if( result == null ){
						CommandLauncher.die("Form parse failed");
					}
					reportBuilder.extractReportParametersFromForm(f, params);
					frame.dispose();
				}else{
					CommandLineForm f = new CommandLineForm(getContext());
					reportBuilder.buildReportParametersForm(f, params);
					if( reportBuilder.hasErrors()){
						Set<ErrorSet> err = reportBuilder.getErrors();
						for( ErrorSet es : err){
							es.report(conn);
						}
						CommandLauncher.die("Form build failed");
					}
					if(! f.parseParams(params)){
						CommandLauncher.die("Form parse failed");
					}
					if( verbose ){
						System.err.println(f.showTable(null, null));
					}
					reportBuilder.extractReportParametersFromForm(f, params);
				}
			}
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			reportBuilder.renderXML(params, out);
			if( reportBuilder.hasErrors()){
				Set<ErrorSet> err = reportBuilder.getErrors();
				for( ErrorSet es : err){
					es.report(conn);
				}
			}else{
                if( gui ){
                	SettableServeDataProducer producer = conn.makeObjectWithDefault(SettableServeDataProducer.class, SessionDataProducer.class, "ServeData");

            		ByteArrayMimeStreamData msd = new ByteArrayMimeStreamData(out.toByteArray());
            		if( reportType != null){
            			msd.setMimeType(reportType.getMimeType());
            			msd.setName(reportTemplateFileName+"."+reportType.getExtension());
            		}else{
            			msd.setName(reportTemplateFileName);
            		}
            		ServeDataResult res = new ServeDataResult(producer, producer.setData(msd));
            		SwingTransitionHandler handler=new SwingTransitionHandler(frame, conn);
            		handler.process(res);
            		frame.dispose();
                }else{
				if (outputFile != null) {
					FileOutputStream stream = new FileOutputStream(outputFile);
					stream.write(out.toByteArray());
					stream.close();
					//FileWriter writer = new FileWriter(outputFile);
					//writer.append(out.toString());
					//writer.close();

				} else {
					System.out.print(out.toString());

				}
                }
			}
		} catch (Exception e) {
			conn.getService(LoggerService.class).getLogger(getClass()).error("Error generating report "+e.getMessage(),e);
			CommandLauncher.die(e);
		}
		System.out.println("All done");
		

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