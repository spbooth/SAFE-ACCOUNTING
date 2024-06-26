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
package uk.ac.ed.epcc.safe.accounting.servlet;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.ac.ed.epcc.safe.accounting.ErrorSet;
import uk.ac.ed.epcc.safe.accounting.reports.ReportBuilder;
import uk.ac.ed.epcc.safe.accounting.reports.ReportType;
import uk.ac.ed.epcc.safe.accounting.reports.ReportTypeRegistry;
import uk.ac.ed.epcc.safe.accounting.reports.forms.html.HTMLReportParametersForm;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Feature;
import uk.ac.ed.epcc.webapp.forms.html.HTMLForm;
import uk.ac.ed.epcc.webapp.forms.result.FormResult;
import uk.ac.ed.epcc.webapp.forms.result.ServeDataResult;
import uk.ac.ed.epcc.webapp.jdbc.DatabaseService;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.logging.buffer.BufferLoggerService;
import uk.ac.ed.epcc.webapp.model.data.stream.ByteArrayMimeStreamData;
import uk.ac.ed.epcc.webapp.model.serv.SettableServeDataProducer;
import uk.ac.ed.epcc.webapp.servlet.ServletService;
import uk.ac.ed.epcc.webapp.servlet.SessionServlet;
import uk.ac.ed.epcc.webapp.session.SessionDataProducer;
import uk.ac.ed.epcc.webapp.session.SessionService;

/** Servlet to generate simple reports under the control of an XML template
 * 
 * @author spb
 *
 */

@WebServlet(name="ReportServlet",urlPatterns="/ReportServlet/*")
public class ReportServlet extends SessionServlet {

	public static final String SERVE_DATA_DEFAULT_TAG = "ServeData";
	public static final Feature PRESELECT_REPORT_TYPE_FEATURE = new Feature("preselect_report_type",true,"report output format is selected on the report index page ");
	public static final Feature DEFAULT_REPORT_TYPE_FEATURE = new Feature("default_report_type",false,"default reports to html if not specified, otherwise an input is added to the parameter form");

	private static String SET_REPORT_PARAMETERS = "SET_REPORT_PARAMETERS";
	private static String GENERATE_REPORT = "GENERATE_REPORT";
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse res,
			AppContext conn, SessionService person) throws Exception
			{
		Logger log = conn.getService(LoggerService.class).getLogger(getClass());
		boolean isReportDeveloper = person != null && person.hasRole(ReportBuilder.REPORT_DEVELOPER);
		
		ErrorSet global_error = new ErrorSet();
		ReportBuilder builder=null;
		ReportType reportType=null;
		String templateName="Report";
		
		
		
		
		BufferLoggerService logService=null;
		if( isReportDeveloper){
			logService = new BufferLoggerService(conn);
			conn.setService(logService);
			// set cached value of feature to ON
			// This applies for rest of AppContext life-time (request)
			conn.setAttribute(DatabaseService.LOG_QUERY_FEATURE, Boolean.TRUE);
		}
		// Get the arguments and parameters
		List<String> args=conn.getService(ServletService.class).getArgs();
		Map<String,Object> report_params = conn.getService(ServletService.class).getParams();
		
		OutputStream out = new ByteArrayOutputStream();
		boolean has_errors=false;
		try{
			
			String web_root = req.getContextPath();
			String css_path = conn.getInitParameter("css.path","css/webapp.css");
			String css_path2 = conn.getInitParameter("css.path2","css/inline_report.css");
			report_params.put("WebRoot",web_root);
			report_params.put("CssPath", web_root+"/"+css_path);
			report_params.put("CssPath2", web_root+"/"+css_path2);
			// Check we have the template name as a argument and remove it from 
			// there and add it as a parameter.
			if( args.size() < 1){
				res.sendError(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
			int pos = args.size()-1;
			String templateFileName = args.get(pos);
			args.remove(pos);			
   
			ReportTypeRegistry reg = ReportTypeRegistry.getInstance(conn);
			ReportType html = reg.getReportType("HTML");
			
			templateName =  ReportBuilder.getTemplateName(templateFileName);
			builder = ReportBuilder.getInstance(conn);
			report_params.put("TemplateName", templateName);
//			Object type = report_params.get(ReportBuilder.REPORT_TYPE_PARAM);
			Object type = report_params.get("submit");
			if (type instanceof String) {
				if (((String)type).startsWith("Preview")) {
					reportType = html;
				}
				else {
					reportType = builder.getReportTypeReg().getReportType(((String)type).replace("Export as ", ""));
				}
			}
//			if( type != null ){
//				if( type instanceof ReportType ){
//					reportType=(ReportType)type;
//				}else{
//					reportType=builder.getReportType(type.toString());
//				}
//			}

			if( reportType == null ){
				reportType = builder.getReportTypeReg().getTemplateType(templateFileName);
			}
			if (reportType == null && DEFAULT_REPORT_TYPE_FEATURE.isEnabled(conn)) {
				reportType=html;
			}
			if( reportType != null){
				report_params.put("ReportType", reportType);
				log.debug("Template name is "+templateName+" and type is "+reportType);
			}
			ReportBuilder.setTemplate(conn, builder, templateName);
			
			if( ! builder.hasErrors()){
				builder.setupExtensions(reportType,report_params);
				if( ! builder.canUse(person,report_params)){
					log.debug("access denied");
					message(conn, req, res, "access_denied");
					return;
				}
				log.debug("access ok");
				
				if (builder.hasReportParameterDefs() || reportType == null) {
					log.debug("has report parameters or type unknown");
					String action = (String) report_params.get("action");	
					if (action == null) {
						// might be better not to show html and pdf etc via the same url
						// when type is not encoded in the url
						log.debug("display parameter page");
						if( reportType != null  ){
							conn.getService(ServletService.class).forward("/accounting/report_parameters.jsp");
						}else{
							conn.getService(ServletService.class).redirect("/accounting/report_parameters.jsp?TemplateName="+templateName+"&ReportType="+reportType);
						}
						return;

					} else {
						log.debug("parsing form");
						// We parse the for for hidden parameter
						HTMLForm.setFormUrl(req, "/accounting/report_parameters.jsp");
						HTMLReportParametersForm form = 
							new HTMLReportParametersForm(builder, report_params,reportType);		
						boolean valid = form.parseForm(req);

						if (!valid) {
							log.debug("Error parsing parameters form");
							HTMLForm.doFormError(conn,req,res);
							return;
						}
					}
				}else{
					log.debug("No parameters needed");
				}
				log.debug("past parameter check");
				boolean direct=false; // do we want to serve data directly
				if( report_params.containsKey("direct_output")){
					out= res.getOutputStream();
					res.setContentType(reportType.getMimeType());
					direct=true;
				}
				builder.renderXML(report_params, out);
				if( direct ){
					out.close();
					return;
				}
			}
		}catch(Exception t){
			getLogger(conn).error("Cannot build report",t);
			global_error.add("Global Report error", "Cannot build report", t);
			has_errors=true;
		}
		SettableServeDataProducer producer = conn.makeObjectWithDefault(SettableServeDataProducer.class, SessionDataProducer.class, SERVE_DATA_DEFAULT_TAG);
        FormResult result=null;
        FormResult logs=null;
        if( isReportDeveloper){
        	// store the logs
        	ByteArrayMimeStreamData logdata= new ByteArrayMimeStreamData(logService.getBuffer().toString().getBytes());
        	logdata.setMimeType("text/plain");
        	logdata.setName("logdata.txt");
        	logs = new ServeDataResult(producer, producer.setData(logdata));
        }
        Set<ErrorSet> errors=null;
		if( builder ==  null || has_errors){
			has_errors=true;
		}else{
			has_errors=builder.hasErrors();

			// errors from the builder
			errors = builder.getErrors();
			for(ErrorSet es : errors){
				es.report(conn);
				if(es.size() > 0){
					has_errors=true;
				}
				es.report(10, getLogger(conn));
			}
		}
		if( has_errors){
			if( person != null && person.hasRole(ReportBuilder.REPORT_DEVELOPER)){
				if( builder != null ){
					try{
						
						ByteArrayMimeStreamData raw = new ByteArrayMimeStreamData();
						raw.setMimeType("text/xml");
						ReportType type = builder.getReportTypeReg().getReportType("RXML");
						builder.renderXML(type, report_params, type.getResult(conn,raw.getOutputStream()));
						result = new ServeDataResult(producer, producer.setData(raw));
					}catch(Exception t){
						getLogger(conn).error("Error generating raw XML",t);
					}
				}
				result = new DeveloperResult(result, logs, errors, true);
				handleFormResult(conn, req, res, result);
				return;
			}else{
				log.debug("error in report");
				message(conn, req, res, "report_error","");
			}
			return;
		}
		
		ByteArrayMimeStreamData msd = new ByteArrayMimeStreamData(((ByteArrayOutputStream)out).toByteArray());
		if( reportType != null){
			log.debug("Report type is "+reportType.getMimeType());
			msd.setMimeType(reportType.getMimeType());
			msd.setName(templateName+"."+reportType.getExtension());
		}else{
			msd.setName(templateName);
			log.debug("No report type set");
		}
		result = new ServeDataResult(producer, producer.setData(msd));
		if( isReportDeveloper){
			result = new DeveloperResult(result, logs, null, false);
		}
		handleFormResult(conn, req, res, result);


	}

}