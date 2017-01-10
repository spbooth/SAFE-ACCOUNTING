//| Copyright - The University of Edinburgh 2017                            |
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
package uk.ac.ed.epcc.safe.accounting.model;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ErrorSet;
import uk.ac.ed.epcc.safe.accounting.reports.ReportBuilder;
import uk.ac.ed.epcc.safe.accounting.reports.ReportType;
import uk.ac.ed.epcc.safe.accounting.servlet.DeveloperResult;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.config.OverrideConfigService;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.content.SimpleXMLBuilder;
import uk.ac.ed.epcc.webapp.forms.BaseForm;
import uk.ac.ed.epcc.webapp.forms.Field;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.action.FormAction;
import uk.ac.ed.epcc.webapp.forms.exceptions.ActionException;
import uk.ac.ed.epcc.webapp.forms.exceptions.TransitionException;
import uk.ac.ed.epcc.webapp.forms.result.ChainedTransitionResult;
import uk.ac.ed.epcc.webapp.forms.result.FormResult;
import uk.ac.ed.epcc.webapp.forms.result.ServeDataResult;
import uk.ac.ed.epcc.webapp.forms.result.ViewTransitionResult;
import uk.ac.ed.epcc.webapp.forms.transition.AbstractDirectTransition;
import uk.ac.ed.epcc.webapp.forms.transition.AbstractFormTransition;
import uk.ac.ed.epcc.webapp.forms.transition.ExtraContent;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.logging.buffer.BufferLoggerService;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.stream.ByteArrayMimeStreamData;
import uk.ac.ed.epcc.webapp.model.data.transition.AbstractViewPathTransitionProvider;
import uk.ac.ed.epcc.webapp.model.serv.SettableServeDataProducer;
import uk.ac.ed.epcc.webapp.session.SessionDataProducer;
import uk.ac.ed.epcc.webapp.session.SessionService;

/**
 * Publications 
 */
public class ReportTemplateTransitionProvider 
extends AbstractViewPathTransitionProvider<Report, ReportTemplateKey> 
{
	private static final String SERVE_DATA_DEFAULT_TAG = "ServeData";

	private static boolean canView(AppContext c, Report target) {
		SessionService sess = c.getService(SessionService.class);
		try {
			ReportBuilder builder = ReportBuilder.getInstance(c);
			Map<String, Object> parameters = target.getParameters(); 
			ReportBuilder.setTemplate(c, builder, target.getReportTemplate().getTemplateName());
			builder.setupExtensions(parameters);
			return builder.canUse(sess, parameters);
		}
		catch (Exception e) {
			return false;
		}
	}

	public static final ReportTemplateKey PREVIEW = new ReportTemplateKey("Update", "Update report parameters")
	{
		
		@Override
		public boolean allow(AppContext c, Report target) 
		{
			return target != null && canView(c, target);
		}
	};

	public class ExportTransition extends AbstractDirectTransition<Report>{
		
		private String extension;

		public ExportTransition(String reportType) {
			this.extension = reportType;
		}

		@Override
		public FormResult doTransition(Report target, AppContext c)
				throws TransitionException 
		{
			try {
				target.setExtension(extension);
				ReportBuilder builder = ReportBuilder.getInstance(c);
				ReportBuilder.setTemplate(c, builder, target.getReportTemplate().getTemplateName());
				ReportType reportType = builder.getReportType(extension);
				Map<String, Object> params = getParameters(target.getParameters(), c, builder);

				if( params != null && ! builder.hasErrors())
				{
					builder.setupExtensions(reportType, params);
					params.put(ReportBuilder.REPORT_TYPE_PARAM, reportType);
					OutputStream out = new ByteArrayOutputStream();
					builder.renderXML(params, out);
					ByteArrayMimeStreamData msd = new ByteArrayMimeStreamData(((ByteArrayOutputStream)out).toByteArray());
					msd.setMimeType(reportType.getMimeType());
					msd.setName(target.getName() + "."+reportType.getExtension());
					SettableServeDataProducer producer = getContext().makeObjectWithDefault(
							SettableServeDataProducer.class, SessionDataProducer.class, SERVE_DATA_DEFAULT_TAG);
					return new ServeDataResult(producer, producer.setData(msd));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return new ViewTransitionResult<Report, ReportTemplateKey>(
					ReportTemplateTransitionProvider.this, target);
		}
		
	}
	
	public class PreviewTransition 
	extends AbstractFormTransition<Report> implements ExtraContent<Report>
	{

		public class PreviewAction extends FormAction{
			private Report target;

			public PreviewAction(Report target) {
				super();
				this.target = target;
			}

			@Override
			public FormResult action(Form f) throws ActionException {
				return new ChainedTransitionResult<Report, ReportTemplateKey>(
						ReportTemplateTransitionProvider.this, 
						getTargetReport(f,target), 
						ReportTemplateTransitionProvider.PREVIEW) {
					@Override
					public boolean useURL() {
						return true;
					}
				};
			}
			
		}

		public class ExportAction extends FormAction 
		{
			private final Report target;
			private final ReportBuilder builder;
			private final ReportType reportType;
			private final AppContext context;

			public ExportAction(AppContext context, Report target, ReportBuilder builder, ReportType format) {
				super();
				this.target = target;
				this.builder = builder;
				this.reportType = format;
				this.context = context;
			}

			@Override
			public FormResult action(Form f) throws ActionException 
			{
				boolean isReportDev = isReportDeveloper(context.getService(SessionService.class));
				boolean hasErrors = false;
				BufferLoggerService logService = null;
				FormResult result = null;
				if (isReportDev) 
				{
					logService = new BufferLoggerService(context);
					context.setService(logService);
					Properties props = new Properties();
					props.setProperty("service.feature.log_query", "on");
					context.setService(new OverrideConfigService(props, context));
				}

				Map<String, Object> reportParameters = target.getParameters(); 
				builder.parseReportParametersForm(f, reportParameters);
				try {
					if( ! builder.hasErrors()){
						builder.setupExtensions(reportType, reportParameters);
						reportParameters.put(ReportBuilder.REPORT_TYPE_PARAM, reportType);
						OutputStream out = new ByteArrayOutputStream();
						builder.renderXML(reportParameters, out);
						ByteArrayMimeStreamData msd = new ByteArrayMimeStreamData(((ByteArrayOutputStream)out).toByteArray());
						msd.setMimeType(reportType.getMimeType());
						msd.setName(target.getName() + "."+reportType.getExtension());
						SettableServeDataProducer producer = getContext().makeObjectWithDefault(
								SettableServeDataProducer.class, SessionDataProducer.class, SERVE_DATA_DEFAULT_TAG);
						result = new ServeDataResult(producer, producer.setData(msd));
					}
					else {
						hasErrors = true;
					}
				} catch (Exception e) {
					getLogger().error("Error generating report for type " + reportType, e);
					hasErrors = true;
				}
				if (isReportDev) {
					DeveloperResults devResults = developerResults(getContext(), logService, builder, hasErrors, reportParameters);
					result = new DeveloperResult(devResults.getResult(), devResults.getLogs(), devResults.getErrors(), hasErrors);
				}
				return result;
			}
			
		}
		
		@Override
		public void buildForm(Form f, Report target, AppContext conn)
				throws TransitionException {
			try 
			{
				ReportBuilder builder = ReportBuilder.getInstance(conn);
				ReportBuilder.setTemplate(conn, builder, target.getReportTemplate().getTemplateName());
				Map<String, Object> parameters = target.getParameters();
				builder.buildReportParametersForm(f, parameters);
				setMap(parameters, f, false);
				
				f.addAction("CSV", new ExportAction(conn, target, builder, builder.getReportType("csv")));
				f.addAction("PDF", new ExportAction(conn, target, builder, builder.getReportType("pdf")));
				f.addAction("HTML", new ExportAction(conn, target, builder, builder.getReportType("html")));
				f.addAction("Preview", new PreviewAction(target));
			}
			catch (Exception e) {
				getLogger().error("Error creating report form", e);
			}
		}
		

		@Override
		public <X extends ContentBuilder> X getExtraHtml(X cb, SessionService<?> op, Report target) {
			ReportTemplate template = target.getReportTemplate();
			cb.addHeading(3, template.getReportName());
			cb.addText(template.getReportDescription());
			try {
				addPreview(getContext(), cb, target);
			} catch (Exception e) {
				getLogger().error("Error creating preview", e);
			}
			cb.addHeading(4, "Report Parameters");
			cb.addText("Enter the parameters and click 'Preview' to view the report, or click on the report type to generate the report.");
			
			return cb;
		}
	}

	private ReportTemplateFactory fac;
	
	
	public ReportTemplateTransitionProvider(AppContext conn){
		super(conn);
		this.fac = new ReportTemplateFactory<ReportTemplate>(conn);
     	addTransition(PREVIEW, new PreviewTransition());
    }

	@Override
	public Report getTarget(LinkedList<String> id) 
	{
		String templateFileName = id.removeLast();
		int i = templateFileName.indexOf(".");
		String extension = null;
		if (i >= 0) {
			extension = templateFileName.substring(i+1);
			templateFileName = templateFileName.substring(0, i);
		}
		templateFileName = templateFileName + ".xml";
		Report report = new Report(null);
		try {
			ReportTemplate reportTemplate = fac.findByFileName(templateFileName);
			Map<String, Object> parameters = new HashMap<String, Object>();
			for (String p : id) {
				int ind = p.indexOf(":");
				if (ind >= 0) {
					String key = p.substring(0, ind);
					String value = decodeParameter(key, p.substring(ind+1));
					parameters.put(key, value);
				}
			}
			report = new Report(reportTemplate, parameters);
			report.setExtension(extension);
		} catch (DataException e) {
			getLogger().debug("Error retrieving template file name");
		}
		return report;
	}

	@Override
	public LinkedList<String> getID(Report target) {
		LinkedList<String> result = new LinkedList<String>();
		if (target.getParameters() != null) {
			for (Entry<String, Object> entry : target.getParameters().entrySet()) {
				String value = encodeParameter(entry);
				result.add(entry.getKey() + ":" + value);
			}
		}
		if (target.getReportTemplate() != null) { 
			String name = target.getName();
			if (target.getExtension() != null) {
				name += "." + target.getExtension();
			}
			result.add(name);
		}
		return result;
	}

	@Override
	public boolean allowTransition(AppContext c, Report target,
			ReportTemplateKey key) {
		return (key.allow(c, target));
	}

	@Override
	public <X extends ContentBuilder> X getSummaryContent(AppContext c, X cb, Report target) {
		return cb;
	}
	
	@Override
	public <X extends ContentBuilder> X getLogContent(X cb, Report target,
			SessionService<?> sess) 
	{
		X result = super.getLogContent(cb, target, sess);
		AppContext context = getContext();

		if (target.getReportTemplate() == null)
		{
			cb.addHeading(3, "Report Templates");
			cb.addText("Unknown report template");
		}
		else {
			ReportTemplate template = target.getReportTemplate();
			cb.addHeading(3, template.getReportName());
			cb.addText(template.getReportDescription());
			try {
				addPreview(context, cb, target);
			} catch (Exception e) {
				getLogger().error("Error creating preview", e);
			}
		}
		return result;
	}

	@Override
	public boolean canView(Report target, SessionService<?> sess) {
		return true;
	}

	public String getTargetName() {
		return "ReportTemplate";
	}
	
	/** Add/sets form contents to a map
	 * 
	 * @param p
	 * @param f
	 * @param set_map
	 * @throws ActionException 
	 */
	public void setMap(Map<String,Object> p,Form f, boolean set_map) throws ActionException{
		SetParamsVisitor vis = new SetParamsVisitor(set_map, p);
		for(Field<?> field : f){
			try {
				field.getInput().accept(vis);
			} catch (Exception e) {
				throw new ActionException("Error in setMap", e);
			}
		}
	}
	public Report getTargetReport(Form f,Report orig) throws ActionException{
		LinkedHashMap<String,Object> new_param = new LinkedHashMap<String, Object>();
		setMap(new_param,f,true);
		
		return new Report(orig.getReportTemplate(),new_param);
	}
	
	private Map<String, Object> getParameters(Map<String, Object> reportParameters, AppContext c, ReportBuilder builder) throws Exception 
	{
		Form form = new BaseForm(c);
		builder.buildReportParametersForm(form, reportParameters);
		setMap(reportParameters, form, false);
		Map<String, Object> params = new HashMap<String, Object>(reportParameters);
		boolean isValid = builder.parseReportParametersForm(form, params);
		if (!isValid) {
			return null;
		}
		return params;
	}

	
	private <X extends ContentBuilder> void addPreview(AppContext context, X cb, Report target) 
	{
		boolean isReportDev = isReportDeveloper(context.getService(SessionService.class));
		BufferLoggerService logService = null;
		if (isReportDev) 
		{
			logService = new BufferLoggerService(context);
			context.setService(logService);
			Properties props = new Properties();
			props.setProperty("service.feature.log_query", "on");
			context.setService(new OverrideConfigService(props, context));
		}

		ReportBuilder builder = null;
		Map<String,Object> params = null;
		boolean hasErrors = false;
		try {
			builder = ReportBuilder.getInstance(context);
			ReportBuilder.setTemplate(context, builder, target.getReportTemplate().getTemplateName());
			params = getParameters(target.getParameters(), context, builder);
			if (params != null && !target.getParameters().isEmpty()) {
				cb.addHeading(4, "Report Preview");
				builder.renderContent(params, (SimpleXMLBuilder)cb);
			}
		} catch (Exception e) {
			hasErrors = true;
			cb.addText("An error ocurred when generating the report.");
		}			
			
		if (params != null && !target.getParameters().isEmpty() && isReportDev)
		{
			DeveloperResults devResults = developerResults(context, logService, builder, hasErrors, params);
			cb.addHeading(4, "Report Developer Information");
			cb.addText("You have the ReportDeveloper role active. The links below give access to additional information to aid in debugging the reports.");
			if( devResults.hasErrors() )
			{
				cb.addText("This report resulted in an error.");
				if( devResults.getResult() != null){
					cb.addLink(context, "Processed template", devResults.getResult());
				}
				if( devResults.getErrors() != null){
					for(ErrorSet error : devResults.getErrors()){
						error.addContent(cb, -1);
					}
				}
			}
			else {
				cb.addText("This report completed without error.");
			}
			if( devResults.getLogs() != null ){
				cb.addLink(context, "Logs from report generation", devResults.getLogs());
			}
		}
	}
	
	private DeveloperResults developerResults(
			AppContext conn, 
			BufferLoggerService logService, 
			ReportBuilder builder, 
			boolean hasErrors, 
			Map<String, Object> reportParameters) 
	{
		SettableServeDataProducer producer = conn.makeObjectWithDefault(SettableServeDataProducer.class, SessionDataProducer.class, SERVE_DATA_DEFAULT_TAG);
        FormResult result=null;
        FormResult logs=null;
    	// store the logs
    	try {
        	ByteArrayMimeStreamData logdata = new ByteArrayMimeStreamData(logService.getBuffer().toString().getBytes());
        	logdata.setMimeType("text/plain");
        	logdata.setName("logdata.txt");
        	logs = new ServeDataResult(producer, producer.setData(logdata));
    	}
    	catch (DataFault e) {
    		getLogger().error("Error creating log data", e);
    	}
        Set<ErrorSet> errors=null;

		hasErrors = hasErrors || builder.hasErrors();

		// errors from the builder
		errors = builder.getErrors();
		for(ErrorSet es : errors){
			es.report(conn);
			if(es.size() > 0){
				hasErrors=true;
			}
		}
		if( hasErrors) {
			try {
				ByteArrayMimeStreamData raw = new ByteArrayMimeStreamData();
				raw.setMimeType("text/xml");
				ReportType type = builder.getReportType("RXML");
				builder.renderXML(type, reportParameters, type.getResult(conn,raw.getOutputStream()));
				result = new ServeDataResult(producer, producer.setData(raw));
			} catch(Throwable t) {
				getLogger().error("Error generating raw XML", t);
			}
		}
		return new DeveloperResults(result, logs, errors, hasErrors);
	}
	
	private boolean isReportDeveloper(SessionService<?> person) {
		return person != null && person.hasRole(ReportBuilder.REPORT_DEVELOPER);
	}
	
	private String encodeParameter(Entry<String, Object> parameter) {
		String value = String.valueOf(parameter.getValue());
		try {
			value = URLEncoder.encode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			getLogger().debug("Failed to encode parameter: " + parameter.getKey() + "=" + parameter.getValue(), e);
		}
		return value;
	}
	
	private String decodeParameter(String key, String value) {
		try {
			return URLDecoder.decode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			getLogger().debug("Failed to decode parameter: " + key + "=" + value, e);
			return value;
		}
	}

	private static class DeveloperResults 
	{

		private FormResult result;
		private FormResult logs;
		private Set<ErrorSet> errors;
		private boolean hasErrors;

		public DeveloperResults(FormResult result, FormResult logs,
				Set<ErrorSet> errors, boolean hasErrors) {
			this.result = result;
			this.logs = logs;
			this.errors = errors;
			this.hasErrors = hasErrors;
		}
		
		public FormResult getResult() {
			return result;
		}
		
		public FormResult getLogs() {
			return logs;
		}
		
		public Set<ErrorSet> getErrors() {
			return errors;
		}
		
		public boolean hasErrors() {
			return hasErrors;
		}
	}

}
