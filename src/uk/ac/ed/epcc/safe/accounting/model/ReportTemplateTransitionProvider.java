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

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import uk.ac.ed.epcc.safe.accounting.reports.ReportBuilder;
import uk.ac.ed.epcc.safe.accounting.reports.ReportType;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.content.SimpleXMLBuilder;
import uk.ac.ed.epcc.webapp.content.Table;
import uk.ac.ed.epcc.webapp.content.XMLGenerator;
import uk.ac.ed.epcc.webapp.forms.BaseForm;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.action.FormAction;
import uk.ac.ed.epcc.webapp.forms.exceptions.ActionException;
import uk.ac.ed.epcc.webapp.forms.exceptions.TransitionException;
import uk.ac.ed.epcc.webapp.forms.result.FormResult;
import uk.ac.ed.epcc.webapp.forms.result.ViewTransitionResult;
import uk.ac.ed.epcc.webapp.forms.transition.AbstractDirectTransition;
import uk.ac.ed.epcc.webapp.forms.transition.AbstractFormTransition;
import uk.ac.ed.epcc.webapp.forms.transition.ExtraContent;
import uk.ac.ed.epcc.webapp.forms.transition.ScriptTransitionFactory;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.model.data.transition.AbstractViewPathTransitionProvider;
import uk.ac.ed.epcc.webapp.session.SessionService;

/**
 * Publications 
 */
public class ReportTemplateTransitionProvider 
extends AbstractViewPathTransitionProvider<Report, ReportTemplateKey> 
implements ScriptTransitionFactory<ReportTemplateKey>
{
	private static boolean canAdd(AppContext c, Report target) {
		SessionService sess = c.getService(SessionService.class);
		boolean result = true;
		return result;
	}

	private static boolean canUpdateDelete(AppContext c, Report target) {
		SessionService sess = c.getService(SessionService.class);
		boolean result = true;
		return result;
	}

	public static final ReportTemplateKey BACK = new ReportTemplateKey("Back","Return to the previous page")
	{
		
		@Override
		public boolean allow(AppContext c, Report target) 
		{
			return target != null;
		}
	};

	public static final ReportTemplateKey GENERATE = new ReportTemplateKey("Generate","Generate report")
	{
		
		@Override
		public boolean allow(AppContext c, Report target) 
		{
			return target != null && canAdd(c, target);
		}
	};
	
	public static final ReportTemplateKey UPDATE = new ReportTemplateKey("Update","Fetch and update the publication metadata")
	{
		
		@Override
		public boolean allow(AppContext c, Report target) 
		{
			return target != null && canUpdateDelete(c, target);
		}
	};
	
	public static final ReportTemplateKey DELETE = new ReportTemplateKey("Delete","Remove the publication from the project")
	{
		
		@Override
		public boolean allow(AppContext c, Report target) 
		{
			return target != null && canUpdateDelete(c, target);
		}
	};

	public static final ReportTemplateKey DEFAULT = new ReportTemplateKey("List","Select a report template")
	{
		
		@Override
		public boolean allow(AppContext c, Report target) 
		{
			return target == null || target.getReportTemplate() == null;
		}
	};
	
//	public class GenerateReportTemplateTransition 
//	extends AbstractFormTransition<Report> implements ExtraContent<Report>
//	{
//
//		public class CreateAction extends FormAction{
//			private Report target;
//
//			public CreateAction(Report target) {
//				super();
//				this.target = target;
//			}
//			
//			@Override
//			public FormResult action(Form f) throws ActionException {
//				return new ViewTransitionResult<Report, ReportTemplateKey>(
//						ReportTemplateTransitionProvider.this, new Report(target.getReportTemplate(), f.getContents()));
//			}
//			
//		}
//
//		@Override
//		public void buildForm(Form f, Report target, AppContext conn)
//				throws TransitionException {
//			try {
//			ReportBuilder builder = ReportBuilder.getInstance(conn);
//			Map<String,Object> report_params = new HashMap<String, Object>();
//			builder.setTemplate(target.getReportTemplate().getTemplateName());
//			builder.buildReportParametersForm(f, report_params);
////			HTMLReportParametersForm form = 
////					new HTMLReportParametersForm(builder, report_params, null);	
////			boolean valid = form.parseForm(req);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//
//			f.addAction("Generate", new CreateAction(target));
//		}
//
//		@Override
//		public <X extends ContentBuilder> X getExtraHtml(X cb, SessionService<?> op, Report target) {
//	 		cb.addHeading(3, target.getReportTemplate().getReportName());
//	 		cb.addText(target.getReportTemplate().getReportDescription());
//			return cb;
//		}
//
//		
//
//	}
//
	public class GeneratePreviewTransition extends AbstractDirectTransition<Report>{

		@Override
		public FormResult doTransition(Report target, AppContext c)
				throws TransitionException {
			return new ViewTransitionResult<Report, ReportTemplateKey>(
					ReportTemplateTransitionProvider.this, target);
		}
		
	}

	public class BackTransition extends AbstractDirectTransition<Report>{

		@Override
		public FormResult doTransition(Report target, AppContext c)
				throws TransitionException 
		{
			return new ViewTransitionResult<Report, ReportTemplateKey>(
					ReportTemplateTransitionProvider.this, new Report(null));
		}
		
	}
	public class UpdateReportTemplateTransition extends AbstractDirectTransition<Report>{

		@Override
		public FormResult doTransition(Report target, AppContext c)
				throws TransitionException {
			return new ViewTransitionResult<Report, ReportTemplateKey>(
					ReportTemplateTransitionProvider.this, target);
		}
		
	}
	public class PreviewTransition 
	extends AbstractFormTransition<Report> 	implements ExtraContent<Report>
	{

		public class CreateAction extends FormAction{
			private Report target;
			private ReportBuilder builder;

			public CreateAction(Report target, ReportBuilder builder) {
				super();
				this.target = target;
				this.builder = builder;
			}

			@Override
			public FormResult action(Form f) throws ActionException {
				System.out.println("GENERATE WITH PARAMETERS: ");
				return new ViewResult(target);
			}
			
		}

		@Override
		public void buildForm(Form f, Report target, AppContext conn)
				throws TransitionException {
			try {
				ReportBuilder builder = ReportBuilder.getInstance(conn);
				Map<String,Object> report_params = target.getParameters();
				ReportBuilder.setTemplate(conn, builder, target.getReportTemplate().getTemplateName());
				builder.buildReportParametersForm(f, new HashMap<String, Object>());
				f.addAction("Generate", new CreateAction(target, builder));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public <X extends ContentBuilder> X getExtraHtml(X cb, SessionService<?> op, Report target) {
			cb.addText("Generate a report"); 
			return cb;
		}
	}

	private ReportTemplateFactory fac;
	
	
	public ReportTemplateTransitionProvider(AppContext conn){
		super(conn);
		this.fac = new ReportTemplateFactory<ReportTemplate>(conn);
    	addTransition(GENERATE, new GeneratePreviewTransition());
    	addTransition(BACK, new BackTransition());
    }

	@Override
	public Report getTarget(LinkedList<String> id) 
	{
		Logger logger = getLogger();
		String templateFileName = id.removeLast();
		String templateName =  ReportBuilder.getTemplateName(templateFileName);
		Report report = new Report(null);
		try {
			ReportTemplate reportTemplate = fac.findByFileName(templateFileName);
			Map<String, Object> parameters = new HashMap<String, Object>();
			for (String p : id) {
				String[] param = p.split("=");
				parameters.put(param[0], param[1]);
			}
			report = new Report(reportTemplate, parameters);
		} catch (DataException e) {
			getLogger().debug("Error retrieving template file name");
		}
		System.out.println("TARGET " + report.getReportTemplate());
		return report;
	}

	@Override
	public LinkedList<String> getID(Report target) {
		LinkedList<String> result = new LinkedList<String>();
		if (target.getParameters() != null) {
			for (Entry<String, Object> entry : target.getParameters().entrySet()) {
				Object value = entry.getValue();
				if (value instanceof Date) {
					value = ((Date)value).getTime();
				}
				result.add(entry.getKey() + "=" + value);
			}
		}
		if (target.getReportTemplate() != null) { 
			result.add(target.getReportTemplate().getTemplateName());
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
				cb.addHeading(3, target.getReportTemplate().getReportName());
				ReportBuilder builder = ReportBuilder.getInstance(context);
				Map<String,Object> report_params = target.getParameters();
				String templateName = target.getReportTemplate().getTemplateName();
				ReportBuilder.setTemplate(context, builder, target.getReportTemplate().getTemplateName());
				ReportType reportType = ReportBuilder.HTML;
				Form form = new BaseForm(context);
				builder.buildReportParametersForm(form, new HashMap<String, Object>());
				System.out.println("PARAMETERS: " + report_params);
//				builder.parseReportParametersForm(form, report_params);
				if (!report_params.isEmpty()) {
					builder.renderContent(report_params, (SimpleXMLBuilder)cb);
				}
				cb.addFormTable(context, form);
						
//				OutputStream out = new ByteArrayOutputStream();
//				builder.renderXML(report_params, out);
//				ByteArrayMimeStreamData msd = new ByteArrayMimeStreamData(((ByteArrayOutputStream)out).toByteArray());
//				if( reportType != null){
//					msd.setMimeType(reportType.getMimeType());
//					msd.setName(templateName+"."+reportType.getExtension());
//				}else{
//					msd.setName(templateName);
//				}


	//			boolean valid = form.parseForm(req);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public class PreformattedTextGenerator implements XMLGenerator {

		private String text;

		public PreformattedTextGenerator(String text) {
			this.text = text;
		}
		
		@Override
		public SimpleXMLBuilder addContent(SimpleXMLBuilder builder) 
		{
			builder.open("pre");
			builder.clean(text);
			builder.close();
			return builder;
		}
		
	}

	@Override
	public boolean canView(Report target, SessionService<?> sess) {
		return true;
	}

	public String getTargetName() {
		return "ReportTemplate";
	}

	@Override
	public String getAdditionalCSS(ReportTemplateKey key) {
		return "//cdn.datatables.net/1.10.5/css/jquery.dataTables.css,//cdn.datatables.net/colvis/1.1.1/css/dataTables.colVis.css,//cdn.datatables.net/colreorder/1.1.2/css/dataTables.colReorder.css";
		//return "//cdn.datatables.net/1.10.5/css/jquery.dataTables.css";
	}

	@Override
	public String getAdditionalScript(ReportTemplateKey key) {
		//return "//code.jquery.com/jquery-1.10.2.min.js,//cdn.datatables.net/1.10.5/js/jquery.dataTables.min.js,"+
		//		"$(document).ready( function(){ $('#datatable').DataTable();";

		return "//code.jquery.com/jquery-1.10.2.min.js,//cdn.datatables.net/1.10.5/js/jquery.dataTables.min.js,//cdn.datatables.net/colvis/1.1.1/js/dataTables.colVis.min.js,//cdn.datatables.net/colreorder/1.1.2/js/dataTables.colReorder.min.js,"+
		"$(document).ready( function(){ $('#datatable').DataTable({ stateSave: true \\, stateDuration: 3600\\, pageLength: 50 \\, lengthMenu: [[ 10\\, 25\\, 50\\, 100\\, -1 ]\\,[ 10\\, 25\\, 50\\, 100\\, 'All'] ] \\, paging: true \\,  order: [[ 0\\, 'desc' ]] \\, dom: 'C<\"clear\">Rlfrtip'   });});";
	}
	
}
