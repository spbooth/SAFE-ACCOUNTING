//| Copyright - The University of Edinburgh 2016                            |
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import uk.ac.ed.epcc.safe.accounting.reports.ReportBuilder;
import uk.ac.ed.epcc.safe.accounting.reports.ReportType;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.content.SimpleXMLBuilder;
import uk.ac.ed.epcc.webapp.content.Table;
import uk.ac.ed.epcc.webapp.forms.BaseForm;
import uk.ac.ed.epcc.webapp.forms.Field;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.MapForm;
import uk.ac.ed.epcc.webapp.forms.action.FormAction;
import uk.ac.ed.epcc.webapp.forms.exceptions.ActionException;
import uk.ac.ed.epcc.webapp.forms.exceptions.TransitionException;
import uk.ac.ed.epcc.webapp.forms.inputs.Input;
import uk.ac.ed.epcc.webapp.forms.inputs.ItemInput;
import uk.ac.ed.epcc.webapp.forms.inputs.MultiInput;
import uk.ac.ed.epcc.webapp.forms.result.FormResult;
import uk.ac.ed.epcc.webapp.forms.result.ServeDataResult;
import uk.ac.ed.epcc.webapp.forms.result.ViewTransitionResult;
import uk.ac.ed.epcc.webapp.forms.transition.AbstractDirectTransition;
import uk.ac.ed.epcc.webapp.forms.transition.AbstractFormTransition;
import uk.ac.ed.epcc.webapp.forms.transition.ExtraContent;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.logging.Logger;
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
	private static boolean canPreview(AppContext c, Report target) {
		SessionService sess = c.getService(SessionService.class);
		boolean result = true;
		return result;
	}

//	public static final ReportTemplateKey BACK = new ReportTemplateKey("Back", "Return to the previous page")
//	{
//		
//		@Override
//		public boolean allow(AppContext c, Report target) 
//		{
//			return target != null;
//		}
//	};
//
	public static final ReportTemplateKey GENERATE = new ReportTemplateKey("Update", "Update report parameters")
	{
		
		@Override
		public boolean allow(AppContext c, Report target) 
		{
			return target != null && canPreview(c, target);
		}
	};
	
	public static final ReportTemplateKey EXPORT_TO_PDF = new ReportTemplateKey("PDF", "Generate PDF Report")
	{
		@Override
		public boolean allow(AppContext c, Report target) 
		{
			return target != null && target.getParameters() != null && !target.getParameters().isEmpty();
		}
	};
	
	public static final ReportTemplateKey EXPORT_TO_CSV = new ReportTemplateKey("CSV", "Generate CSV Report")
	{
		@Override
		public boolean allow(AppContext c, Report target) 
		{
			return target != null && target.getParameters() != null && !target.getParameters().isEmpty();
		}
	};
	
	public static final ReportTemplateKey EXPORT_TO_HTML = new ReportTemplateKey("HTML", "Generate HTML Report")
	{
		@Override
		public boolean allow(AppContext c, Report target) 
		{
			return target != null && target.getParameters() != null && !target.getParameters().isEmpty();
		}
	};

	public static final ReportTemplateKey EXPORT_TO_XML = new ReportTemplateKey("XML", "Generate XML Report")
	{
		
		@Override
		public boolean allow(AppContext c, Report target) 
		{
			return target != null && target.getParameters() != null && !target.getParameters().isEmpty();
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
				System.out.println("TARGET: PARAMETERS=" + params);
				if( ! builder.hasErrors()){
					builder.setupExtensions(reportType, params);
					params.put(ReportBuilder.REPORT_TYPE_PARAM, reportType);
					OutputStream out = new ByteArrayOutputStream();
					builder.renderXML(params, out);
					ByteArrayMimeStreamData msd = new ByteArrayMimeStreamData(((ByteArrayOutputStream)out).toByteArray());
					msd.setMimeType(reportType.getMimeType());
					msd.setName(target.getName() + "."+reportType.getExtension());
					SettableServeDataProducer producer = getContext().makeObjectWithDefault(SettableServeDataProducer.class, SessionDataProducer.class, "ServeData");
					return new ServeDataResult(producer, producer.setData(msd));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return new ViewTransitionResult<Report, ReportTemplateKey>(
					ReportTemplateTransitionProvider.this, target);
		}
		
	}
	
//	public class PreviewTransition extends AbstractDirectTransition<Report>{
//
//		@Override
//		public FormResult doTransition(Report target, AppContext c)
//				throws TransitionException {
//			return new ViewTransitionResult<Report, ReportTemplateKey>(
//					ReportTemplateTransitionProvider.this, target);
//		}
//		
//	}
	
	public class PreviewTransition 
	extends AbstractFormTransition<Report> 	implements ExtraContent<Report>
	{

		public class PreviewAction extends FormAction{
			private Report target;
			

			public PreviewAction(Report target) {
				super();
				this.target = target;
				
			}

			@Override
			public FormResult action(Form f) throws ActionException {
				return new ViewResult(getTargetReport(f,target));
			}
			
		}

		public class ExportAction extends FormAction{
			private Report target;
			private ReportBuilder builder;
			private ReportType reportType;

			public ExportAction(Report target, ReportBuilder builder, ReportType format) {
				super();
				this.target = target;
				this.builder = builder;
				this.reportType = format;
			}

			@Override
			public FormResult action(Form f) throws ActionException {
				Map<String, Object> reportParameters = target.getParameters(); 
				builder.parseReportParametersForm(f, reportParameters);
				System.out.println("EXPORT PARAMETERS : " + reportParameters);
				try {
					if( ! builder.hasErrors()){
						builder.setupExtensions(reportType, reportParameters);
						reportParameters.put(ReportBuilder.REPORT_TYPE_PARAM, reportType);
						OutputStream out = new ByteArrayOutputStream();
						builder.renderXML(reportParameters, out);
						ByteArrayMimeStreamData msd = new ByteArrayMimeStreamData(((ByteArrayOutputStream)out).toByteArray());
						msd.setMimeType(reportType.getMimeType());
						msd.setName(target.getName() + "."+reportType.getExtension());
						SettableServeDataProducer producer = getContext().makeObjectWithDefault(SettableServeDataProducer.class, SessionDataProducer.class, "ServeData");
						return new ServeDataResult(producer, producer.setData(msd));
					}
					else {
						return new ViewResult(getTargetReport(f,target));
					}
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
			
		}

		@Override
		public void buildForm(Form f, Report target, AppContext conn)
				throws TransitionException {
			try {
				ReportBuilder builder = ReportBuilder.getInstance(conn);
				ReportBuilder.setTemplate(conn, builder, target.getReportTemplate().getTemplateName());
				Map<String, Object> parameters = target.getParameters();
				builder.buildReportParametersForm(f, parameters);
				setMap(parameters, f, false);
				
				f.addAction("CSV", new ExportAction(target, builder, builder.getReportType("csv")));
				f.addAction("PDF", new ExportAction(target, builder, builder.getReportType("pdf")));
				f.addAction("HTML", new ExportAction(target, builder, builder.getReportType("html")));
				f.addAction("Preview", new PreviewAction(target));
			}
			catch (Exception e) {
//				e.printStackTrace();
			}
		}
		

		@Override
		public <X extends ContentBuilder> X getExtraHtml(X cb, SessionService<?> op, Report target) {
			ReportTemplate template = target.getReportTemplate();
			cb.addHeading(3, template.getReportName());
			cb.addText(template.getReportDescription());
			return cb;
		}
	}

	private ReportTemplateFactory fac;
	
	
	public ReportTemplateTransitionProvider(AppContext conn){
		super(conn);
		this.fac = new ReportTemplateFactory<ReportTemplate>(conn);
     	addTransition(EXPORT_TO_PDF, new ExportTransition("pdf"));
     	addTransition(EXPORT_TO_CSV, new ExportTransition("csv"));
     	addTransition(EXPORT_TO_HTML, new ExportTransition("html"));
//     	addTransition(EXPORT_TO_XML, new ExportTransition("xml"));
     	addTransition(GENERATE, new PreviewTransition());
//    	addTransition(BACK, new BackTransition());
    }

	@Override
	public Report getTarget(LinkedList<String> id) 
	{
		Logger logger = getLogger();
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
				String[] param = p.split(":");
				parameters.put(param[0], param[1]);
			}
			report = new Report(reportTemplate, parameters);
			report.setExtension(extension);
		} catch (DataException e) {
			getLogger().debug("Error retrieving template file name");
		}
		System.out.println("TARGET " + report);
		return report;
	}

	@Override
	public LinkedList<String> getID(Report target) {
		LinkedList<String> result = new LinkedList<String>();
		if (target.getParameters() != null) {
			for (Entry<String, Object> entry : target.getParameters().entrySet()) {
				Object value = entry.getValue();
				result.add(entry.getKey() + ":" + ParameterFormatter.format(value));
			}
		}
		if (target.getReportTemplate() != null) { 
			String name = target.getName();
			if (target.getExtension() != null) {
				name += "." + target.getExtension();
			}
			result.add(name);
		}
		System.out.println("ID = " + result);
		return result;
	}

	@Override
	public boolean allowTransition(AppContext c, Report target,
			ReportTemplateKey key) {
		return (key.allow(c, target));
	}

	@Override
	public <X extends ContentBuilder> X getSummaryContent(AppContext c, X cb,
			Report target) {
		Table<String,String> t = new Table<String,String>();
		return cb;
	}
	
	private Map<String, Object> getParameters(Map<String, Object> reportParameters, AppContext c, ReportBuilder builder) throws Exception 
	{
		Form form = new BaseForm(c);
		builder.buildReportParametersForm(form, reportParameters);
		Map<String, Object> params = new HashMap<String, Object>();
		Iterator<String> fields = form.getFieldIterator();
		while (fields.hasNext()) {
			String field = fields.next();
			Input input = form.getInput(field);
			Object value = setParameters(reportParameters, input);
			if (value != null) {
				params.put(input.getKey(), value);
			}
		}
		return params;
	}

	private Object setParameters(Map<String, Object> params, Input input) {
		Object data = params.get(input.getKey());
		if(input instanceof MultiInput)
		{
			MultiInput<?,?> multi=(MultiInput) input;
			for(String sub_key : multi.getSubKeys()){
				setParameters(params, multi.getInput(sub_key));
			}
		}
		else {
			if (data != null) {
				input.setValue(input.convert(data));
			}
		}
		if (input instanceof ItemInput) {
			return ((ItemInput) input).getItem();
		}
		return input.getValue();
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
			try {
				ReportTemplate template = target.getReportTemplate();
				cb.addHeading(3, template.getReportName());
				cb.addText(template.getReportDescription());
				ReportBuilder builder = ReportBuilder.getInstance(context);
				ReportBuilder.setTemplate(context, builder, target.getReportTemplate().getTemplateName());
				Map<String,Object> params = getParameters(target.getParameters(), context, builder);
				System.out.println("TARGET: PARAMETERS=" + target.getParameters());
				if (!target.getParameters().isEmpty()) {
					builder.renderContent(params, (SimpleXMLBuilder)cb);
				}
			} catch (Exception e) {
				e.printStackTrace();
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
	 * @throws Exception 
	 */
	public void setMap(Map<String,Object> p,Form f, boolean set_map) throws ActionException{
		SetParamsVisitor vis = new SetParamsVisitor(set_map, p);
		for(Field field : f){
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
}
