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
import java.util.*;
import java.util.Map.Entry;

import uk.ac.ed.epcc.safe.accounting.ErrorSet;
import uk.ac.ed.epcc.safe.accounting.model.ReportTemplateLog.ReportLogFactory;
import uk.ac.ed.epcc.safe.accounting.reports.ReportBuilder;
import uk.ac.ed.epcc.safe.accounting.reports.ReportType;
import uk.ac.ed.epcc.safe.accounting.reports.ReportTypeRegistry;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Feature;
import uk.ac.ed.epcc.webapp.content.*;
import uk.ac.ed.epcc.webapp.forms.BaseForm;
import uk.ac.ed.epcc.webapp.forms.Field;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.action.FormAction;
import uk.ac.ed.epcc.webapp.forms.exceptions.ActionException;
import uk.ac.ed.epcc.webapp.forms.exceptions.TransitionException;
import uk.ac.ed.epcc.webapp.forms.html.BaseHTMLForm;
import uk.ac.ed.epcc.webapp.forms.result.*;
import uk.ac.ed.epcc.webapp.forms.transition.*;
import uk.ac.ed.epcc.webapp.jdbc.DatabaseService;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.limits.LimitException;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.logging.buffer.BufferLoggerService;
import uk.ac.ed.epcc.webapp.messages.MessageBundleService;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.stream.ByteArrayMimeStreamData;
import uk.ac.ed.epcc.webapp.model.data.transition.AbstractPathTransitionProvider;
import uk.ac.ed.epcc.webapp.model.serv.ServeDataProducer;
import uk.ac.ed.epcc.webapp.model.serv.SettableServeDataProducer;
import uk.ac.ed.epcc.webapp.preferences.Preference;
import uk.ac.ed.epcc.webapp.servlet.ServletService;
import uk.ac.ed.epcc.webapp.servlet.ViewTransitionKey;
import uk.ac.ed.epcc.webapp.session.SessionDataProducer;
import uk.ac.ed.epcc.webapp.session.SessionService;
import uk.ac.ed.epcc.webapp.timer.DefaultTimerService;
import uk.ac.ed.epcc.webapp.timer.TimerService;

/**
 * Publications 
 */
public class ReportTemplateTransitionProvider 
extends AbstractPathTransitionProvider<Report, ReportTemplateKey> 
implements TitleTransitionFactory<ReportTemplateKey, Report>, DefaultingTransitionFactory<ReportTemplateKey, Report>
{
	private static final String REPORT_TIMER = "report_timer";
	private static final String FORM_PARAMETER_PREFIX = "__";
	public static final String PREVIEW_TAG="Preview";
	private static final int FORM_PARAMETER_OFFSET=FORM_PARAMETER_PREFIX.length();
	public static final Preference DOWNLOAD_FROM_NEW_TAB = new Preference("reports.download_from_new_tab",true,"Open a new tab when generating pdf/csv etc. reports");
	//private static final String SERVE_DATA_DEFAULT_TAG = "ServeData";
	public static final Feature LOG_REPORT_USE = new Feature("reports.log_report_use",
			false,
			"Log reporting: user, template and parameters are logged every time a report is built");

	private static boolean canView(AppContext c, Report target) {
		TimerService timer = c.getService(TimerService.class);
		if( timer!= null) {
			timer.startTimer("Reports.canView."+target.getName());
		}
		SessionService sess = c.getService(SessionService.class);
		try {
			ReportBuilder builder = ReportBuilder.getInstance(c);
			Map<String, Object> parameters = target.getParameters(); 
			ReportBuilder.setTemplate(c, builder, target.getName());
			builder.setupExtensions(parameters);
			return builder.canUse(sess, parameters);
		}
		catch (Exception e) {
			c.getService(LoggerService.class).getLogger(ReportTemplateTransitionProvider.class).error("Error checking access", e);
			return false;
		}finally{
			if( timer!= null) {
				timer.stopTimer("Reports.canView."+target.getName());
			}
		}
	}

	
	public static final ReportTemplateKey PREVIEW = new ReportTemplateViewKey("View", "Update report parameters");
	//public static final ReportTemplateKey HTML = new ReportTemplateViewKey("HTML", "Generate HTML");
	//public static final ReportTemplateKey PDF = new ReportTemplateViewKey("PDF", "Generate PDF");
	//public static final ReportTemplateKey CSV = new ReportTemplateViewKey("CSV", "Generate CSV");
	//public static final ReportTemplateKey FOP = new ReportDeveloperTemplateViewKey("FOP", "Generate FOP");

	private static final class ReportTemplateViewKey extends ReportTemplateKey implements ViewTransitionKey<Report>{
		private ReportTemplateViewKey(String name, String help) {
			super(name, help);
		}

		@Override
		public boolean allow(AppContext c, Report target) 
		{
			return target != null && canView(c, target);
		}

		@Override
		public boolean isNonModifying(Report target) {
			return true;
		}
	}
	public static final class ReportTypeKey extends ReportTemplateKey implements ViewTransitionKey<Report>{
		public ReportTypeKey(ReportType t) {
			super(t.name(), t.getHelp());
			this.type=t;
		}

		private final ReportType type;

		@Override
		public boolean allow(AppContext c, Report target) {
			return target != null && type.allowSelect(c.getService(SessionService.class)) && canView(c, target);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			ReportTypeKey other = (ReportTypeKey) obj;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			return true;
		}
	}
	
	private static final class ReportDeveloperTemplateViewKey extends ReportTemplateKey implements ViewTransitionKey<Report>{
		private ReportDeveloperTemplateViewKey(String name, String help) {
			super(name, help);
		}

		@Override
		public boolean allow(AppContext c, Report target) 
		{
			return target != null&& c.getService(SessionService.class).hasRole(ReportBuilder.REPORT_DEVELOPER) && canView(c, target);
		}

		@Override
		public boolean isNonModifying(Report target) {
			return true;
		}
	}

	public class ExportTransition extends AbstractDirectTransition<Report>{
		
		private ReportType type;

		public ExportTransition(ReportType reportType) {
			this.type = reportType;
		}

		@Override
		public FormResult doTransition(Report target, AppContext c)
				throws TransitionException 
		{
			ReportTemplateLog report_log=null;
			try {
				target.setExtension(type.getExtension());
				ReportBuilder builder = ReportBuilder.getInstance(c);
				ReportBuilder.setTemplate(c, builder, target.getName());
		
				Map<String, Object> params = getParameters(target, c, builder);
				ServletService ss = getContext().getService(ServletService.class);
				report_log=logReport(target);
				String css_path = getContext().getInitParameter("css.path","css/webapp.css");
				if( ss != null){
					css_path = ss.encodeURL("/"+css_path);
				}
				if( params != null && ! builder.hasErrors())
				{
					params.put("CssPath", css_path);
					builder.setupExtensions(type, params);
					params.put(ReportTypeRegistry.REPORT_TYPE_PARAM, type);
					OutputStream out = new ByteArrayOutputStream();
					builder.renderXML(params, out);
					ByteArrayMimeStreamData msd = new ByteArrayMimeStreamData(((ByteArrayOutputStream)out).toByteArray());
					msd.setMimeType(type.getMimeType());
					msd.setName(target.getName() + "."+type.getExtension());
					SettableServeDataProducer producer = getContext().makeObjectWithDefault(
							SettableServeDataProducer.class, SessionDataProducer.class, ServeDataProducer.DEFAULT_SERVE_DATA_TAG);
					return new ServeDataResult(producer, producer.setData(msd));
				}
			}catch(LimitException l) {
				DatabaseService db = c.getService(DatabaseService.class);
				db.closeRetainedClosables();
				getLogger().warn("Limits exceeded in report generation", l);
				return new WarningMessageResult("limits_exceeded", l.getMessage());
			} catch (Exception e) {
				getLogger().error("Error making report", e);
				return new InternalErrorResult();
			}finally {
				if( report_log != null ) {
					report_log.recordFinish();
				}
			}
			return new ChainedTransitionResult<>(ReportTemplateTransitionProvider.this, target, PREVIEW);
		}
		
	}
	
	public class PreviewTransition 
	extends AbstractFormTransition<Report> implements ExtraContent<Report>
	{
		public class NextAction extends FormAction{
			private Report target;
			private Object text;
			private ReportTemplateKey next_transition;
			private Map<String,Object> defaults;
			public NextAction(Object text,Report target,Map<String,Object> defaults,ReportTemplateKey next_transition,boolean new_window) {
				super();
				this.text=text;
				this.target = target;
				this.defaults=defaults;
				this.next_transition=next_transition;
				setNewWindow(new_window);
			}

			@Override
			public FormResult action(Form f) throws ActionException {
				Report next = getTargetReport(defaults,f,target);
				if( next.getParameters().isEmpty()) {
					// Force preview even if no parameters
					next.setPreview(next_transition.equals(PREVIEW));
				}
				return new RedirectChainedTransitionResult<>(
						ReportTemplateTransitionProvider.this, 
						next, 
						next_transition);
			}
			/* (non-Javadoc)
			 * @see uk.ac.ed.epcc.webapp.forms.action.FormAction#getText()
			 */
			@Override
			public Object getText() {
				return text;
			}

			/* (non-Javadoc)
			 * @see uk.ac.ed.epcc.webapp.forms.action.FormAction#getHelp()
			 */
			@Override
			public String getHelp() {
				return "Generate "+text;
			}
			
		}
		
		
		@Override
		public void buildForm(Form f, Report target, AppContext conn)
				throws TransitionException {
			try 
			{
				ReportBuilder builder = ReportBuilder.getInstance(conn);
				ReportBuilder.setTemplate(conn, builder, target.getName());
				Map<String, Object> parameters = target.getParameters();
				String param_stage = (String) parameters.get(BaseHTMLForm.FORM_STAGE_INPUT);
				if( param_stage != null) {
					int ps = Integer.parseInt(param_stage);
					if( ps > f.getTargetStage()) {
						f.setTargetStage(ps);
					}
				}
				Map<String,Object> defaults = new HashMap<String, Object>();
				if( builder.buildReportParametersForm(f, parameters,defaults)) {
					setMap(parameters, target.getContextParameters(),defaults, f, false);
					if( builder.hasReportParameters()){
						f.addAction("Preview", new NextAction(new Icon(conn,"Preview","/accounting/preview-file-48x48.png"),target,defaults,PREVIEW,false));
					}
					boolean new_window=DOWNLOAD_FROM_NEW_TAB.isEnabled(conn);
					ReportTypeRegistry reg = ReportTypeRegistry.getInstance(conn);
					SessionService sess = conn.getService(SessionService.class);
					for( ReportType t : reg.getReportTypes()) {
						if( t.allowSelect(sess)) {
							String image = t.getImage();
							if( image != null ) {
								f.addAction(t.name(), new NextAction(new Icon(conn,t.name(),image), target, defaults, new ReportTypeKey(t), new_window));
							}else {
								f.addAction(t.name(), new NextAction(t.name(), target, defaults, new ReportTypeKey(t), new_window));
							}
						}
					}
					
				}
			}
			catch (Exception e) {
				getLogger().error("Error creating report form", e);
			}
		}
		

		@Override
		public <X extends ContentBuilder> X getExtraHtml(X cb, SessionService<?> op, Report target,Form f) {
			ReportTemplate template = getReportTemplate(target);
			// name now shown in primary header
			//cb.addHeading(3, template.getReportName());
			if( template != null){
				cb.addText(template.getReportDescription());
			}
			
			
			ReportBuilder builder = null;
			try {
				builder = ReportBuilder.getInstance(conn);
				
				ReportBuilder.setTemplate(conn, builder, target.getName());
				builder.addParameterText(cb);
				if( f.isComplete()) {
					AppContext context = getContext();
					boolean isReportDev = isReportDeveloper(context.getService(SessionService.class));
					BufferLoggerService logService = null;
					if (isReportDev) 
					{
						logService = new BufferLoggerService(context);
						// maximum log length to keep
						int max_length = context.getIntegerParameter("reporting.buffer_logger.max_length", 1024*1024*8);
						logService.setMaxLength(max_length);
						context.setService(logService);
						// this may be a cached report builder
						builder.resetLogger();
						TimerService timer = context.getService(TimerService.class);
						if( timer == null ) {
							timer = new DefaultTimerService(context);
							context.setService(timer);
						}
						timer.startTimer(REPORT_TIMER);
						// set cached value of feature to ON
						// This applies for rest of AppContext life-time (request)
						context.setAttribute(DatabaseService.LOG_QUERY_FEATURE, Boolean.TRUE);
						
					}
				
					Map<String,Object> params = null;
					boolean hasErrors = false;
					ReportTemplateLog report_log=null;
					boolean has_form=false;
					try {
						
						
						has_form = builder.hasMandatoryReportParameters();
						params = getParameters(target, context, builder);
						// null params means invalid form
						// non-empty params always shows preview can also force a preview for the default param set
						if (! has_form || (params != null && (! target.getParameters().isEmpty() || target.isPreview()))) 
						{
							report_log=logReport(target);
							cb.addHeading(4, "Report Preview");
							ContentBuilder report = cb.getPanel("report_container");
							builder.renderContent(params, (SimpleXMLBuilder)report);
							report.addParent();
						}
					}catch(LimitException l) {
						DatabaseService db = context.getService(DatabaseService.class);
						db.closeRetainedClosables();
						// Show message in-line#
						ResourceBundle mess = context.getService(MessageBundleService.class).getBundle();
						PreDefinedContent title = new PreDefinedContent(context,mess, "limits_exceeded.title");
						ContentBuilder heading = cb.getHeading(4);
						heading.addObject(title);
						heading.addParent();
						PreDefinedContent text = new PreDefinedContent(context,mess, "limits_exceeded.text",l.getMessage());
						ExtendedXMLBuilder para = cb.getText();
						text.addContent(para);
						para.appendParent();
					} catch (Exception e) {
						getLogger().debug("Error making paramter form", e);
						hasErrors = true;
						cb.addText("An error ocurred when generating the report.");
					}finally {
						if( report_log != null ) {
							report_log.recordFinish();
						}
					}			
					// null params meand invalid 
					if ((! has_form || (params != null && (!target.getParameters().isEmpty() || target.isPreview()))) && isReportDev)
					{
						TimerService timer = context.getService(TimerService.class);
						if( timer != null ) {
							timer.stopTimer(REPORT_TIMER);
							timer.timerStats();
						}
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
			} catch (Exception e) {
				getLogger().error("Error creating preview", e);
			}
			if( builder != null &&  builder.hasReportParameters()) {
				cb.addHeading(4, "Report Parameters");
				cb.addText("Enter the parameters and click 'Preview' to view the report, or click on the report type to generate the report.");
			}
			return cb;
		}


		@Override
		public <X extends ContentBuilder> X getExtraHtml(X cb, SessionService<?> op, Report target) {
			cb.addText("Something looks wrong here!!!");
			return cb;
		}
	}

	private ReportTemplateFactory fac;
	private ReportLogFactory logFac;
	
	public ReportTemplateTransitionProvider(AppContext conn){
		super(conn);
		ReportTypeRegistry reg = ReportTypeRegistry.getInstance(conn);
		this.fac = new ReportTemplateFactory<>(conn);
		if (LOG_REPORT_USE.isEnabled(conn)) {
			this.logFac = new ReportLogFactory(conn, conn.getService(SessionService.class).getLoginFactory(), fac);
		}
     	addTransition(PREVIEW, new PreviewTransition());
     	for(ReportType t  : reg.getReportTypes()) {
     		addTransition(new ReportTypeKey(t), new ExportTransition(t));
     	}
//     	addTransition(HTML, new ExportTransition(ReportTypeRegistry.HTML));
//     	addTransition(PDF, new ExportTransition(reg.getReportType("pdf")));
//     	addTransition(CSV, new ExportTransition(reg.getReportType("csv")));
//    	addTransition(FOP, new ExportTransition(reg.getReportType("fop")));
    }

	@Override
	public Report getTarget(LinkedList<String> id) 
	{
		if( id == null || id.isEmpty()){
			return null;
		}
		String templateFileName = id.removeLast();
		int i = templateFileName.indexOf(".");
		String extension = null;
		if (i >= 0) {
			extension = templateFileName.substring(i+1);
			templateFileName = templateFileName.substring(0, i);
		}
		//templateFileName = templateFileName + ".xml";
		
		
			
			Map<String, Object> parameters = new HashMap<>();
			Set<String> contextParameters = new HashSet<>();
			boolean preview=false;
			for (String p : id) {
				if( p.equals(PREVIEW_TAG)) {
					preview=true;
					continue;
				}
				boolean isContextParam = false; 
				if (p.startsWith(FORM_PARAMETER_PREFIX)) {
					p = p.substring(FORM_PARAMETER_OFFSET);
					isContextParam = true;
				}
				int ind = p.indexOf(":");
				if (ind >= 0) {
					String key = p.substring(0, ind);
					String value = decodeParameter(key, p.substring(ind+1));
					parameters.put(key, value);
					if (isContextParam) {
						contextParameters.add(key);
					}
				}
			}
			Report report = new Report(templateFileName, parameters);
			report.setPreview(preview);
			report.setExtension(extension);
			report.setContextParameters(contextParameters);
			return report;
		
		
	}

	@Override
	public LinkedList<String> getID(Report target) {
		LinkedList<String> result = new LinkedList<>();
		Map<String, Object> parameters = target.getParameters();
		Collection<String> contextParameters = target.getContextParameters();
		if( target.isPreview()) {
			result.add(PREVIEW_TAG);
		}
		if (parameters != null) {
			for (Entry<String, Object> entry : parameters.entrySet()) {
				String value = encodeParameter(entry);
				String prefix = "";
				if (contextParameters != null && contextParameters.contains(entry.getKey())) {
					prefix = FORM_PARAMETER_PREFIX;
				}
				result.add(prefix + entry.getKey() + ":" + value);
			}
		}
		String name = target.getName();
		if (name != null) { 
			
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
	


	public String getTargetName() {
		return "ReportTemplate";
	}
	
	/** Add/sets form contents to a map
	 * 
	 * @param parameters parameters
	 * @param f form to populate or extract values
	 * @param set_map <code>true</code> to add the form contents to the map, or <code>false</code> to populate the form from the map values
	 * @throws ActionException 
	 */
	public void setMap(Map<String,Object> parameters, Form f, boolean set_map) throws ActionException{
		setMap(parameters, null,null, f, set_map);
	}

	/**
	 * Add/set form contents to a map
	 * @param p parameters
	 * @param locked keys of input forms that are locked
	 * @param optional map of form defaults
	 * @param f form
	 * @param set_map <code>true</code> to add the form contents to the map, or <code>false</code> to populate the form from the map values
	 * @return true if any mandatory inputs were missing
	 * @throws ActionException
	 */
	public boolean setMap(Map<String,Object> p, Collection<String> locked, Map<String,Object> defaults,Form f, boolean set_map) throws ActionException
	{
		boolean missing=false;
		SetParamsVisitor vis = new SetParamsVisitor(set_map, p);
		for(Field<?> field : f){
			if( set_map && defaults != null ) {
				Object value = field.getValue();
				if( value != null && value.equals(defaults.get(field.getKey()))) {
					// this is the current default so suppress the params to make url cleaner
					continue;
				}
				if( value == null && field.isOptional() && defaults.containsKey(field.getKey())) {
					// We have selected null where a default exists
					// use empty string to indicate this
					p.put(field.getKey(),""); 
					continue;
				}
			}
			try {
				vis.setMissing(false);
				field.getInput().accept(vis);
				if (locked != null && locked.contains(field.getKey())) {
					field.lock();
				}
				if( ! field.isOptional()) {
					if( defaults == null || !  defaults.containsKey(field.getKey())) {
						missing = missing || vis.getMissing();
					}
				}
			} catch (Exception e) {
				throw new ActionException("Error in setMap", e);
			}
		}
		return missing;
	}
	/** make an updated report object based on form params
	 * 
	 * @param f
	 * @param orig
	 * @return
	 * @throws ActionException
	 */
	public Report getTargetReport(Map<String,Object> defaults,Form f,Report orig) throws ActionException{
		LinkedHashMap<String,Object> new_param = new LinkedHashMap<>();
		Map<String, Object> param = orig.getParameters();
		Collection<String> contextParameters = orig.getContextParameters();
		setMap(new_param, contextParameters, defaults,f, true);
		// Overide with original values if locked.
		for (String c : contextParameters) {
			new_param.put(c, param.get(c));
		}
		int target = f.getTargetStage();
		if( target> 0) {
			// record form stage
			new_param.put(BaseHTMLForm.FORM_STAGE_INPUT, Integer.toString(target));
		}
		return new Report(orig.getName(), new_param, contextParameters);
	}
	/** convert the target into the equivalent of the parse. 
	 * 
	 * This returns a map of parameters including any form defaults and overridden by any parameters 
	 * set explicitly in the URL.
	 * 
	 * @param target
	 * @param c
	 * @param builder
	 * @return parameters or null if form will not validate
	 * @throws Exception
	 */
	private Map<String, Object> getParameters(Report target, AppContext c, ReportBuilder builder) throws Exception 
	{
		Map<String, Object> reportParameters = target.getParameters();
		Form form = new BaseForm(c);
		Map<String,Object> defaults = new HashMap<String, Object>();
		builder.buildReportParametersForm(form, reportParameters,defaults);
		
		// don't set any context parameters locking inputs in this form
		// because parseReportParameters below won't retrieve the values for locked input forms
		if (setMap(reportParameters, null,defaults, form, false)) {
			return null;
		}
		if( ! form.validate()){
			// important check the target params would validate otherwise we bypass
			// access control
			return null;
		}
		Map<String, Object> params = new HashMap<>(reportParameters);
		boolean isValid = ReportBuilder.extractReportParametersFromForm(form, params);
		if (!isValid) {
			return null;
		}
		return params;
	}

	/** Optionally log the report
	 * On error or if logging disabled this returns null.
	 * 
	 * @param target
	 * @return {@link ReportTemplateLog} or null
	 */
	private ReportTemplateLog logReport(Report target) {
		if (logFac != null) {
			try {
				return logFac.logReport(
						getContext().getService(SessionService.class).getCurrentPerson(), 
						getReportTemplate(target),
						getID(target));
			} catch (Exception e) {
				getLogger().error("Error logging report use", e);
			}
		}
		return null;
	}

	private DeveloperResults developerResults(
			AppContext conn, 
			BufferLoggerService logService, 
			ReportBuilder builder, 
			boolean hasErrors, 
			Map<String, Object> reportParameters) 
	{
		SettableServeDataProducer producer = conn.makeObjectWithDefault(SettableServeDataProducer.class, SessionDataProducer.class, ServeDataProducer.DEFAULT_SERVE_DATA_TAG);
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
				ReportType type = builder.getReportTypeReg().getReportType("RXML");
				builder.renderXML(type, reportParameters, type.getResult(conn,raw.getOutputStream()));
				result = new ServeDataResult(producer, producer.setData(raw));
			} catch(Exception t) {
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

	@Override
	public String getTitle(ReportTemplateKey key, Report target) {
		
		String operation="View";
		if( key != null){
			operation = key.toString();
		}
		if( target != null ){
			ReportTemplate reportTemplate = getReportTemplate(target);
			if( reportTemplate != null && reportTemplate.getReportName() != null ){
				return operation+" "+reportTemplate.getReportName();
			}
		}
		return operation+" Report";
	}
	/** Get the {@link ReportTemplate} corresponding to a report.
	 * 
	 * As reports CAN be run on unregistered reports this can return null for
	 * a valid report
	 * @param r
	 * @return
	 */
    public ReportTemplate getReportTemplate(Report r){
    	if( r == null ){
    		return null;
    	}
    	ReportTemplateFactory fac = new ReportTemplateFactory<>(getContext());
    	try {
			return fac.findByFileName(r.getName()+".xml");
		} catch (DataException e) {
			getLogger().error("Error looking up report template",e);
		}
    	return null;
    }
	@Override
	public String getHeading(ReportTemplateKey key, Report target) {
		return getTitle(key, target);
	}

	@Override
	public ReportTemplateKey getDefaultTransition(Report target) {
		return PREVIEW;
	}

	

}
