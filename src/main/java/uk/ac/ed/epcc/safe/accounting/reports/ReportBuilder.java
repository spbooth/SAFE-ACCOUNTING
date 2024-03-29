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
package uk.ac.ed.epcc.safe.accounting.reports;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.*;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

import uk.ac.ed.epcc.safe.accounting.ErrorSet;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParserPolicy;
import uk.ac.ed.epcc.safe.accounting.reports.exceptions.ReportException;
import uk.ac.ed.epcc.safe.accounting.xml.ErrorSetErrorListener;
import uk.ac.ed.epcc.safe.accounting.xml.LSResolver;
import uk.ac.ed.epcc.webapp.*;
import uk.ac.ed.epcc.webapp.content.*;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
import uk.ac.ed.epcc.webapp.exceptions.InvalidArgument;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.inputs.*;
import uk.ac.ed.epcc.webapp.limits.LimitException;
import uk.ac.ed.epcc.webapp.limits.LimitService;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.model.TextFileOverlay;
import uk.ac.ed.epcc.webapp.model.TextFileOverlay.TextFile;
import uk.ac.ed.epcc.webapp.model.TextProvider;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.session.AppUser;
import uk.ac.ed.epcc.webapp.session.SessionService;
import uk.ac.ed.epcc.webapp.timer.TimerService;


/**
 * Class to Build Reports from an XML template
 * 
 * @author spb
 * 
 */


public class ReportBuilder extends AbstractContexed implements ContextCached , TemplateValidator {

	private static final String PARAMETER_TEXT_ELEMENT = "Text";
	public static final String AUTHENTICATED_USER_PARAMETER_NAME = "AuthenticatedUser";
	private static final String CURRENT_TIME_PARAMETER_NAME = "CurrentTime";
	private static final String RESTRICT_EXTENSION_TAG = "RestrictExtension";
	private static final String PARAMETER_EXTENSION_TAG = "ParameterExtension";
	public static final String DEFAULT_REPORT_SCHEMA = "report.xsd";
	public static final String REPORT_SCHEMA_CONFIG = "report.schema";
	
	public static final String SCHEMA_GROUP = "schema";
	public static final String REPORT_TEMPLATE_GROUP = "report-templates";
	public static final String STYLESHEET_GROUP = "stylesheets";

	public static final Feature CHECK_PARAMETER_NAMES = new Feature("reports.check_parameter_names",true,"Check that report parameter names come from the valid set");
	public static final Feature EMBEDDED_USE_REFERENCE = new Feature("reports.embedded.use_reference",true,"XMLGenerators (e.g. tables) are added directly to the final XMLBuilder to allow links etc");
	//public static final Feature ALWAYS_RUN_PARAMETER_TRANSFORM = new Feature("reports.always_run_parameter_transform",true,"Assume checking if transform is needed is as expensive as ruuning it");
	public class Resolver implements URIResolver {

		public Source resolve(String href, String base)
				throws TransformerException {
			try {
				getLogger().debug("URI resolve href="+href+" base="+base);
				URI base_uri = new URI(base);
				URI target = base_uri.resolve(href);
				URI style = STYLESHEET_URI.relativize(target);
				if (!style.isAbsolute()) {
					getLogger().debug("is stylesheet "+style.getRawPath());
					return getStyleSheet(style.getRawPath());
				}
				URI report = REPORT_URI.relativize(target);
				if (!report.isAbsolute()) {
					getLogger().debug("is report-template "+report.getRawPath());
					return getReport(report.getRawPath());
				}
				URI schema = SCHEMA_URI.relativize(target);
				if (!schema.isAbsolute()) {
					getLogger().debug("is schema "+schema.getRawPath());
					return getSchemaSource(report.getRawPath());

				}
				getLogger().debug("return null");
				return null;
			} catch (Exception e) {
				getLogger().warn("Error in Resolver",e);
				throw new TransformerException(e);
			}
		}

	}
	
	
	
	
	public static String getTemplateName(String templateFileName) {
		int dotLocation = templateFileName.lastIndexOf('.');
		if (dotLocation > 0) {
			return templateFileName.substring(0, dotLocation);

		} else {
			return templateFileName;

		}

	}

	
    public static final String BASE_LOC="http://safe.epcc.ed.ac.uk";
	public static final String PARAMETER_LOC = BASE_LOC+"/parameter";
	private final URI PARAMETER_URI;



	public static final String REPORT_LOC = BASE_LOC+"/report";
	private final URI REPORT_URI=new URI(REPORT_LOC);
	private static final String RESTRICT_LOC = BASE_LOC+"/restrict";
	private final URI RESTRICT_URI=new URI(RESTRICT_LOC);
	private static final String STYLESHEET_LOC = BASE_LOC+"/stylesheet";
	private final URI STYLESHEET_URI=new URI(STYLESHEET_LOC);

	private static final String SCHEMA_LOC = BASE_LOC;
	private final URI SCHEMA_URI=new URI(SCHEMA_LOC);

	private TextFileOverlay default_overlay, report_overlay,
			stylesheet_overlay, schema_overlay;
	private TextProvider template;
	private String template_name;
	
	// private String schema_name = null;
	private DocumentBuilder docBuilder;
	private TransformerFactory transformerFactory;
	private Set<ErrorSet> error_sets;
	ErrorSet general_error;
	private Set<TemplateValidator> validators;
	protected boolean log_source=false;
	public static final String REPORT_DEVELOPER = "ReportDeveloper";
    private final ReportTypeRegistry report_type_reg;
    private final LimitService limits;
	public ReportBuilder(AppContext conn) throws URISyntaxException, ParserConfigurationException {
		this(ReportTypeRegistry.getInstance(conn));
	}
	protected ReportBuilder(ReportTypeRegistry reg) throws URISyntaxException, ParserConfigurationException {	
		super(reg.getContext());
		PARAMETER_URI = new URI(PARAMETER_LOC);
		TimerService timer = conn.getService(TimerService.class);
		
		report_type_reg = reg;
		
		
		if( timer != null) {
			timer.startTimer("makeOverlays");
		}
		URL base_url=null;
		try {
			base_url = new URL(BASE_LOC);
		} catch (MalformedURLException e) {
			getLogger().error("Error making base URL",e);
		}
		default_overlay = new TextFileOverlay(conn);
		default_overlay.setBaseURL(base_url);
		
		String report_overlay_table = conn.getInitParameter("report.overlay");
		if (report_overlay_table != null) {
			report_overlay = conn.makeObject(TextFileOverlay.class,
					report_overlay_table);
			report_overlay.setBaseURL(base_url);
		} else {
			report_overlay = default_overlay;
		}
		if (!report_overlay.isValid()) {
			throw new ConsistencyError("Text file overlay (report) not valid");
		}
		String schema_overlay_table = conn.getInitParameter("schema.overlay");
		if (schema_overlay_table != null) {
			schema_overlay = conn.makeObject(TextFileOverlay.class,
					schema_overlay_table);
			schema_overlay.setBaseURL(base_url);
		} else {
			schema_overlay = default_overlay;
		}
		if (!schema_overlay.isValid()) {
			throw new ConsistencyError("Text file overlay (schema) not valid");
		}
		String stylesheet_overlay_table = conn
				.getInitParameter("stylesheet.overlay");
		if (stylesheet_overlay_table != null) {
			stylesheet_overlay = conn.makeObject(TextFileOverlay.class,
					stylesheet_overlay_table);
			stylesheet_overlay.setBaseURL(base_url);
		} else {
			stylesheet_overlay = default_overlay;
		}
		if (!stylesheet_overlay.isValid()) {
			throw new ConsistencyError(
					"Text file overlay (stylesheet) not valid");
		}
		if( timer != null) {
			timer.stopTimer("makeOverlays");
		}
		resetErrors();
		
		validators=new LinkedHashSet<>();
		if( timer != null) {
			timer.startTimer("docBuilder");
		}
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
		.newInstance();
		docBuilderFactory.setNamespaceAware(true);
		docBuilder = docBuilderFactory.newDocumentBuilder();
		if( timer != null) {
			timer.stopTimer("docBuilder");
		}
		if( timer != null) {
			timer.startTimer("ReportTypes");
		}
		// build ReportTypes from config.
		// make a ReportType for each tag defined in the config service.
		//To define a new ReportType MYTAG set
		// class.MYTAG=<fq-class-name of ReportType or subclass>
		// optionally set
		//     mime.MYTAG   extension.MYTAG description.MYTAG
		// to customise the other properties.
		Map<String,Class> map = conn.getClassMap(ReportType.class);
		for(String name : map.keySet()){
			Class<? extends ReportType> clazz = map.get(name);
			try{
				// assume standard constructor interface for ReportType
				conn.makeParamObject(clazz, 
						name,
						conn.getInitParameter("extension."+name,name.toLowerCase()),
						conn.getInitParameter("mime."+name,"text/plain"),
						conn.getInitParameter("description."+name,name+" ReportType"),
						conn.getInitParameter("help."+name),
						conn.getInitParameter("image."+name)
				);
				
			}catch(Exception t){
				getLogger().error("Error making ReportType "+name,t);
			}
		}
		if( timer != null) {
			timer.stopTimer("ReportTypes");
		}
		limits=conn.getService(LimitService.class);
		if( limits == null) {
			getLogger().debug("No limit service");
		}else {
			getLogger().debug("Limit service is "+limits.getClass().getCanonicalName());
		}
	}
	private TransformerFactory getTransformerFactory() {
		if( transformerFactory == null) {
			transformerFactory=TransformerFactory.newInstance();
			
			getLogger().debug("TransformerFactory="+transformerFactory.getClass().getCanonicalName());
			// Get the stylesheets to use the local URIs to find resources.
			transformerFactory.setURIResolver(new Resolver());
			
		}
		return transformerFactory;
	}
	/**
	 * 
	 */
	private void resetErrors() {
		error_sets=new HashSet<>();
		general_error=new ErrorSet();
		general_error.setName("general");
		general_error.setMaxDetails(16);
		general_error.setMaxEntry(16);
		error_sets.add(general_error);
	}

	/** Constructor for tests
	 * 
	 * @param ctx
	 * @param string
	 * @param string2
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws TransformerException 
	 * @throws TransformerFactoryConfigurationError 
	 * @throws InvalidArgument 
	 * @throws ParserConfigurationException 
	 * @throws DataFault 
	 * @throws URISyntaxException 
	 */
	public ReportBuilder(AppContext ctx, String string, String string2) throws DataFault, ParserConfigurationException, InvalidArgument, TransformerFactoryConfigurationError, TransformerException, SAXException, IOException, URISyntaxException {
		this(ctx);
		setTemplate(string, string2);
	}
	private Document param_document=null;
	public static final String REPORT_PARAMS_ATTR = "report.params";
	
	private Set<String> getParameterDefNames(){
		Set<String> names = new HashSet<>();
		Document pdoc = getParameterDocument();
		if( pdoc != null ) {
			NodeList paramNodes = pdoc.getElementsByTagNameNS(
					PARAMETER_LOC, "ParameterDef");
			for(int i=0 ; i < paramNodes.getLength(); i++){
				Element e = (Element) paramNodes.item(i);
				names.add(e.getAttribute("name"));
			}
		}
		return names;
	}
	protected void setTemplate(String template_name) throws DataFault,
			ParserConfigurationException, InvalidArgument,
			TransformerFactoryConfigurationError, TransformerException {
		TimerService timer = getContext().getService(TimerService.class);
		if( timer != null ) {
			timer.startTimer("ReportBuilder.setTemplate");
		}
		try {
		if( template != null){
			resetErrors();
			param_document=null;
		}
		if (template_name == null || template_name.trim().length() == 0) {
			throw new InvalidArgument("null template specified");
		}
		template_name = template_name.trim();
		String xml_name;
		// xml_bad extension used for tests to supress IDE schema warnings on known bad XML
		if (!template_name.endsWith(".xml") && ! template_name.endsWith(".xml_bad")) {
			xml_name = template_name + ".xml";
			this.template_name = template_name;
		} else {
			xml_name = template_name;
			this.template_name = template_name.substring(0, template_name
					.indexOf(".xml"));
		}
		template = report_overlay.find(REPORT_TEMPLATE_GROUP, xml_name);

		if (template == null || ! template.hasData()) {
			throw new InvalidArgument("template not found " + template_name);
		}

		
		
		// perform the initial parse to check for errors.
		getParameterDocument();
		}finally {
			if( timer != null ) {
				timer.stopTimer("ReportBuilder.setTemplate");
			}
		}
	}
	
	/** set the report template explicitly 
	 * 
	 * This is for use by sub-classes
	 * @param text_provider
	 */
	protected void setTemplate(TextProvider text_provider) {
			if( template != null){
				resetErrors();
				param_document=null;
			}
			template = text_provider;
	}
	/**
	 * @throws ParserConfigurationException
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerConfigurationException
	 * @throws DataFault
	 * @throws TransformerException
	 */
	private Document getParameterDocument()  {
		if( param_document == null ) {
			try {
				Document reportTemplateDocument = docBuilder.newDocument();
				assert(docBuilder.isNamespaceAware());


				// perform the initial transform before everything else.
				Map<String, Object> p = new HashMap<>();
				p.put(RESTRICT_EXTENSION_TAG, new RestrictExtension(conn, null));
				Transformer transformer = getXSLTransform("initial.xsl",
						p);

				DOMResult result = new DOMResult(reportTemplateDocument);
				transformer.transform(getTemplateSource(), result);

				if(log_source) {
					logSource("Initial ",new DOMSource(reportTemplateDocument));
				}
				param_document= reportTemplateDocument;
			}catch(Exception t) {
				general_error.add("initial_transform", "Failed to make parameter document", t);
				// prevent re-run
				param_document=docBuilder.newDocument();
			}
		}
		return param_document;
	}
	protected void setTemplate(String template_name,String schema_name) throws DataFault, ParserConfigurationException, InvalidArgument, TransformerFactoryConfigurationError, TransformerException, SAXException, IOException{
		log_source=true;
		setTemplate(template_name);
		getParameterDocument(); // Generate any errors 
		
		try{
			Schema s = getSchema(schema_name);
			if( s != null ){
				Validator val = s.newValidator();
				val.validate(getTemplateSource());
			}else{
				general_error.add("Bad schema", "Cannot validate schema "+schema_name+" not found");
			}
		}catch(Exception e){
			general_error.add("Bad schema", "Error validating schema",e);
		}
	}

	private void logSource(String text,Document s) {
		logSource(text,new DOMSource(s));
	}
	private void logSource(String text, Source s){
		if( log_source) {
			getLogger().debug(() -> {
				try{

					ByteArrayOutputStream out = new ByteArrayOutputStream();
					Transformer identity = getXSLTransform(null, null);
					identity.transform(s, new
							StreamResult(out));
					return text+" source XML is:"+out.toString();

				}catch(Exception t){
					getLogger().error("Error in logSource",t);
					return "Error generating log";
				}
			});
		}
	}
	

	public void validate(String schema_name, String template_text)
	throws SAXException, IOException, DataFault {
		validate(schema_name,new StreamSource(new StringReader(template_text)));
	}
	public void validate(String schema_name, Source src)
	throws SAXException, IOException, DataFault {
		Schema s = getSchema(schema_name);
		if( s != null ) {
			Validator val = s.newValidator();
			val.validate(src);
		}
	}
	/**
	 * Get the title used for the parameter page
	 * 
	 * @return String title
	 */
	public String getTitle() {
		NodeList titleNodes = getParameterDocument().getElementsByTagNameNS(
				PARAMETER_LOC, "PageTitle");
		if (titleNodes.getLength() > 0) {
			HtmlBuilder hb = new HtmlBuilder();
			hb.clean(titleNodes.item(0).getTextContent());
			return hb.toString();
		}
		return conn.getInitParameter("service.name", "") + " Report parameters";
	}

	public ContentBuilder addParameterText(ContentBuilder hb) {
		NodeList paramNodes = getParameterDocument().getElementsByTagNameNS(
				PARAMETER_LOC, PARAMETER_TEXT_ELEMENT);
		for (int i = 0; i < paramNodes.getLength(); i++) {
			Node text = paramNodes.item(i);
			hb.addText(text.getTextContent());
		}
		return hb;
	}

	/** Evaluate the access control status
	 * 
	 * @param person
	 * @param params 
	 * @return boolean
	 */
	public boolean canUse( SessionService<?> person,Map<String, Object> params) {
		//Logger log =
		//getContext().getService(LoggerService.class).getLogger(getClass());
		if (person.hasRole(ReportBuilder.REPORT_DEVELOPER)) {
			// log.debug(template_name+" user "+person.getName()+" is report developer");
			return true;
		}
		//This has to go right back to the raw template source as the templateDocument
		//has been processed by the initial transform that removes the access control statements.
		Document doc;
		
		try {
			doc= getTemplateDocument();
		} catch (Exception e) {
			return false;
		}
		
		
		
		if( hasErrors()){
			// normally don't show erroneous reports.
			return false;
		}
		RestrictExtension restrict = (RestrictExtension) params.get(RESTRICT_EXTENSION_TAG);
		try {
			return restrict.canUse(doc);
		} catch (ReportException e) {
			getLogger().error("Error in access control", e);
			return false;
		}
	}
	
	public void setupUser(Map<String,Object> params) {
		// Add a user to the session for some of the tests
		SessionService sessionService = conn.getService(SessionService.class);
		sessionService.setCurrentPerson(1);
		AppUser user = sessionService.getCurrentPerson();		
		params.put("User", user);
	}
	public void register(ReportExtension re){
		ErrorSet errors = re.getErrors();
		if( errors != null){
			error_sets.add(errors);
		}else{
			getLogger().error("Adding null ErrorSet from "+re.getClass().getSimpleName());
		}
		validators.add(re);
	}
	
	public void setupExtensions(Map<String,Object> params) throws ParserConfigurationException{
		setupExtensions(ReportTypeRegistry.getInstance(getContext()).getReportType("HTML"), params);
	}
	public void setupExtensions(ReportType reportType,Map<String,Object> params)
		throws ParserConfigurationException 
	{
		
		
		// Note that reportType might be null when building
		// the parameter form
		
		ValueParserPolicy pol = new ValueParserPolicy(getContext());
		if( reportType != null){
			String mimeType = reportType.getMimeType();
			pol.setXML(mimeType != null && mimeType.equalsIgnoreCase("text/xml"));
		}
		// Add in the AuthenticatedUser and CurrentTime
		SessionService<?> person = conn.getService(SessionService.class);
		if( person != null ){
			final AppUser currentPerson = person.getCurrentPerson();
			if( currentPerson != null ){
				params.put(AUTHENTICATED_USER_PARAMETER_NAME, currentPerson);
			}
		}
		// This allows tests to set the time.
		CurrentTimeService current_time = conn.getService(CurrentTimeService.class);
		params.put(CURRENT_TIME_PARAMETER_NAME, current_time.getCurrentTime());
		// This is to allow custom classes created by reflection
		// like formatters or table transforms access to the parameters.
		conn.setAttribute(ReportBuilder.REPORT_PARAMS_ATTR, params);

		Set<String> parameter_names=null;
		if( CHECK_PARAMETER_NAMES.isEnabled(conn)){
			// Make the set of legal parameter names.
			parameter_names = getParameterDefNames();
			parameter_names.add(CURRENT_TIME_PARAMETER_NAME);
			parameter_names.add(AUTHENTICATED_USER_PARAMETER_NAME);
		}
		
		//always need parameter and restrict extension
		ReportExtension parme = new ParameterExtension(conn, reportType);
		parme.setPolicy(pol);
		parme.setParams(reportType,parameter_names,params);
		register(parme);
		params.put(PARAMETER_EXTENSION_TAG, parme);
		RestrictExtension rest = new RestrictExtension(conn, reportType);
		rest.setPolicy(pol);
		rest.setParams(reportType,parameter_names,params);
		register(rest);
		params.put(RESTRICT_EXTENSION_TAG,rest);
		
		if( reportType != null){
		// Now set the rest of the extensions from the config
		String extension_param_name = "ReportBuilder."+reportType.name()+".extension_list";
		String extension_list = conn.getInitParameter(extension_param_name, "");
		getLogger().debug("extension list "+extension_param_name+"->"+extension_list);
		for(String extension_name : extension_list.split("\\s*,\\s*")){
			extension_name=extension_name.trim();
			if( extension_name.length() > 0){
				Class<? extends ReportExtension> clazz = conn.getPropertyClass(ReportExtension.class, extension_name);
				if( clazz == null ){
					getLogger().error("Extension "+extension_name+" not defined");
				}else{
					try {
						ReportExtension ext = conn.makeParamObject(clazz, conn,reportType);
						String param_name = conn.getInitParameter("ReportBuilder."+extension_name+".name",extension_name);
						getLogger().debug("Adding extension "+ext.getClass().getCanonicalName()+" as "+param_name);
						ext.setPolicy(pol);
						ext.setParams(reportType,parameter_names,params);
						register(ext);
						params.put(param_name, ext);
					} catch (Exception e) {
						getLogger().error("Error making extension "+clazz.getCanonicalName(),e);
					}
				}
			}
		}
		
		}
	}

	public boolean hasReportParameterDefs() {
		// Find the parameters which have been defined
		NodeList paramNodes = getParameterDocument().getElementsByTagNameNS(
				PARAMETER_LOC, "ParameterDef");
		return (paramNodes.getLength() > 0);

	}
	public boolean hasReportParameters() {
		// Find the parameters which have been defined
		NodeList paramNodes = getParameterDocument().getElementsByTagNameNS(
				PARAMETER_LOC, "ParameterDef");
		return (paramNodes.getLength() > 0);

	}
	public boolean hasMandatoryReportParameters() {
		// Find the parameters which have been defined
		NodeList paramNodes = getParameterDocument().getElementsByTagNameNS(
				PARAMETER_LOC, "ParameterDef");
		if( paramNodes.getLength()==0) {
			return false;
		}
		for(int i=0 ; i< paramNodes.getLength(); i++) {
			Node n = paramNodes.item(i);
			Element e = (Element)n;
			String optional = e.getAttribute("optional");
			if( optional == null || optional.trim().isEmpty() || ! Boolean.parseBoolean(optional.trim())) {
				return true;
			}
		}
		return false;

	}
	public boolean needParameterTransform() {
		// Default behaviour is to run transform if any param syntax found
		// some elements are exceptions and don't need the transform run
		NodeList paramNodes = getParameterDocument().getElementsByTagNameNS(
				PARAMETER_LOC, "*");
		for(int i=0; i< paramNodes.getLength(); i++) {
			Node n = paramNodes.item(i);
			switch(n.getLocalName()){
			case	ParameterExtension.PARAMETER_DEF_ELEMENT: break;
			case    ParameterExtension.PARAMETER_REF_ELEMENT: break;
			default: return true;
			}
		}
		return false;

	}
	
	public boolean buildReportParametersForm(Form form, Map<String, Object> params)
			throws Exception {
		return buildReportParametersForm(form, params, null);
	}
	public boolean buildReportParametersForm(Form form, Map<String, Object> params,Map<String,Object> defaults)
			throws Exception {
		ParameterExtension pe = (ParameterExtension) params
				.get(PARAMETER_EXTENSION_TAG);
		if (pe == null) {
			pe = new ParameterExtension(getContext(), null);
			pe.setParams(null,params.keySet(), params);
		}
		boolean cont = pe.buildReportParametersForm(defaults,form, getParameterDocument());
		ErrorSet es = pe.getErrors();
		es.report(getContext());
		if (es.size() > 0) {
			throw new Exception("Error constructing parameter form");
		}
		return cont;
	}

	public static boolean extractReportParametersFromForm(Form form,
			Map<String, Object> params) {
		if (form == null || ! form.validate()) {
			return false;
		}

		// next we move all the values form the Inputs to the params list.
		Iterator<String> fields = form.getFieldIterator();
		while (fields.hasNext()) {
			String field = fields.next();
			Input input = form.getInput(field);
			setInputValue(params, input);
		}
		return true;

	}

	static void setInputValue(Map<String, Object> params, Input input) {
		if( input instanceof LockedInput) {
			input = ((LockedInput)input).getNested();
		}
		Object data = null;
		if (input instanceof ItemInput ) {
			data = ((ItemInput) input).getItem();
		} else {
			data = input.getValue();
		}
		// do explicit remove for null value as this might
		// be the result of a parse of a non-null string.
		if( data == null ){
			params.remove(input.getKey());
		}else{
			params.put(input.getKey(), data);
		}
		if(input instanceof MultiInput){
			MultiInput<?,?> multi=(MultiInput) input;
			for(String sub_key : multi.getSubKeys()){
				setInputValue(params, multi.getInput(sub_key));
			}
		}
	}

	

	public void renderXML(Map<String, Object> params, OutputStream out)
			throws Exception {
		ReportType type = getReportTypeReg().getReportType(params);
		getLogger().debug("Report type is "+type);
		renderXML(type, params, type.getResult(getContext(),out));
	}
	/** Forward HTML output to a {@link SimpleXMLBuilder}.
	 * 
	 * This is intended to embed micro-reports in application pages. In particular in 
	 * transition pages.
	 * 
	 * 
	 * @param params report parameters
	 * @param builder SimpleXMLBuilder
	 * @throws Exception
	 */
	public void renderContent(Map<String,Object> params,SimpleXMLBuilder builder) throws Exception{
		ReportType ehtml = ReportTypeRegistry.getInstance(getContext()).getReportType("EHTML");
		setupExtensions(ehtml,params);
		//setupExtensions(HTML,params);
		for(Object o : params.values()){
			if( o instanceof ReportExtension){
				// XMLGenerators are added to parameters and added to the output as processing instructions
				// the generators are then added directly to the builder in the XMLBuilderSAXHandler
				// This allows domain objects to add navication links
				((ReportExtension)o).setUseReference(EMBEDDED_USE_REFERENCE.isEnabled(getContext()));
			}
		}
		// We pass the params to allow extensions to store XMLGenereator objects and
		// refer to them by reference.
		XMLBuilderSaxHandler handler = new XMLBuilderSaxHandler(builder,params);
		SAXResult result = new SAXResult(handler);
		renderXML(ehtml, params, result);
	}
	private IdentityDomTransform findTransform(Map<String, Object> params,String name) {
		if( name.contains("+")) {
			// create a MultiDomTransform
			MultiDomTransform mdt = new MultiDomTransform();
			for(String sub : name.split("\\+")) {
				IdentityDomTransform st = findTransform(params,sub);
				if( st != null) {
					mdt.addTransform(st);
				}
			}
			return mdt;
		}
		Object x = params.get(name);
		if( x != null && x instanceof IdentityDomTransform) {
			return (IdentityDomTransform) x;
		}
		return null;
	}
	private Document transformList(Document xmlSource,String transform_names[],int count,Map<String, Object> params) throws DataFault, TransformerFactoryConfigurationError, TransformerException {
		TimerService timer = conn.getService(TimerService.class);
		String name="";

		for( int i=0; i< count ; i++){
			name = transform_names[i];
			name = name.trim();
			if( name.length() > 0){
				logSource(name, xmlSource);
				if( name.endsWith(".xsl")) {
					if( timer != null){
						timer.startTimer("xml-transform "+name);
					}
					checkLimits();
					Transformer transformer = getXSLTransform(name,
							params);
					
					// Perform the transformation, sending the output to the response.
					DOMResult result = new DOMResult(docBuilder.newDocument());
					transformer.transform(new DOMSource(xmlSource), result);
					xmlSource = (Document) result.getNode();
					if( timer != null){
						timer.stopTimer("xml-transform "+name);
					}
				}else {
					Object x = findTransform(params, name);
					if( x != null && x instanceof DomTransform) {
						DomTransform t = (DomTransform)x;

						if( timer != null){
							timer.startTimer("dom-transform "+name);
						}
						Document new_doc = t.transform(docBuilder,xmlSource);
						xmlSource=new_doc;
						if( timer != null){
							timer.stopTimer("dom-transform "+name);
						}
					}else {
						getLogger().error("Bad DOMTransform "+name);
					}
				}
			}
		}
		return xmlSource;
	}

	/** Generate a report result.
	 * 
	 * This proceeds as a series of transformation that can either be XLST stylesheets
	 * or a {@link DomTransform}. The last transform in the chain must be a XLST transform
	 * and may be generating a non XML result.
	 * 
	 * @param type
	 * @param params
	 * @param out
	 * @throws Exception
	 */
	public void renderXML(ReportType type, Map<String, Object> params,
			Result out) throws Exception {

		// Get the XML input document
		Document xmlSource = getParameterDocument();

		// always try parameters.
		//xmlSource = runParametersTransform( xmlSource,params);
		// Get the report type
		
		
		String parameter_transform_list = conn.getExpandedProperty("ReportBuilder."+type.name()+".parameter_transform_list", "RestrictExtension,ParameterExtension");
		String transform_list = conn.getExpandedProperty("ReportBuilder."+type.name()+".transform_list", "identity.xsl");
		String parameter_transform_names[] = parameter_transform_list.split("\\s*,\\s*");
		String transform_names[] = transform_list.split("\\s*,\\s*");
		TimerService timer = conn.getService(TimerService.class);
		String name="";
		try{
			// parameter/restrict transform
			xmlSource = transformList(xmlSource, parameter_transform_names, parameter_transform_names.length , params);
			
			getLogger().debug("Transform list ReportBuilder."+type.name()+".transform_list is "+transform_list);

			xmlSource = transformList(xmlSource, transform_names, transform_names.length -1, params);
			
			name = transform_names[transform_names.length-1];
			//name=null;
			if( timer != null){
				timer.startTimer("xml-transform "+name);
			}
			logSource(name, xmlSource);
			Transformer transformer = getXSLTransform(name, params);
			
			transformer.transform(new DOMSource(xmlSource),out );
			if( timer != null){
				timer.stopTimer("xml-transform "+name);
			}
		}catch(Exception t){
			if( t instanceof LimitException) {
				throw t;
			}
			if( t instanceof TransformerException && t.getCause() instanceof LimitException) {
				throw (LimitException) t.getCause();
			}
			getLogger().error("Error in transform "+name,t);
			throw new Exception(t);
		}
	}

	
	protected Source getStyleSheet(String name) throws DataFault {
		TextFile sheet = stylesheet_overlay.find(STYLESHEET_GROUP, name);
		if( ! sheet.hasData()){
			throw new DataFault("No stylesheet "+name);
		}
		return new StreamSource(sheet.getDataReader(), STYLESHEET_LOC + "/"
				+ name);
	}

	protected Source getReport(String name) throws DataFault {
		TextFile sheet = report_overlay.find(REPORT_TEMPLATE_GROUP, name);
		if( ! sheet.hasData()){
			throw new DataFault("No report "+name);
		}
		return new StreamSource(sheet.getDataReader(), REPORT_LOC + "/"
				+ name);
	}

	public Source getSchemaSource(String name) throws DataFault,
			SAXException {
		TextFile sheet = schema_overlay.find(SCHEMA_GROUP, name);
		if (sheet == null || !sheet.hasData()) {
			return null;
		}

		
		return new StreamSource(sheet.getDataReader(), SCHEMA_LOC + "/"
				+ name);

	}
	
	public Document getSchemaDocument(String name) throws DataFault, SAXException, TransformerException{
		Source src = getSchemaSource(name);
		DOMResult res = new DOMResult();
		Transformer tf = getTransformerFactory().newTransformer();
		tf.transform(src, res);
		return (Document) res.getNode();
	}
	public Schema getSchema(String name) throws DataFault, SAXException {
		try {

			SchemaFactory factory = SchemaFactory
					.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			LSResourceResolver parent = factory.getResourceResolver();
			if( ! (parent instanceof LSResolver)){
				try {
					factory.setResourceResolver(new LSResolver(docBuilder.getDOMImplementation(), schema_overlay, SCHEMA_GROUP, parent));
				} catch (ParserConfigurationException e) {
					getLogger().error("Error setting schema resolver",e);
				}
			}
			Source source = getSchemaSource(name);
			if(source == null ){
				return null;
			}
			return factory.newSchema(source);
		
		}catch(Exception e) {
			getLogger().error("Error getting schema",e);
			return null;
		}
	}

	public Source getTemplateSource() {
		return new StreamSource(template.getDataReader(), REPORT_LOC + "/"
				+ template_name + ".xml");
	}
	

	
	
	public Transformer getXSLTransform(String name,
			Map<String, Object> params)
			throws TransformerFactoryConfigurationError,
			TransformerConfigurationException, DataFault {

		// XML transforms capture all exceptions and forward them to
		// an ErrorListener
		// errors from parsing the stylesheet go to the TransformerFactory where
		// errors during the transform go to the Transform listener.
		// We capture these to an ErrorSet 
		// arguably we could just re-throw to prevent the errors being captured
		// however this does allow us to capture multple errors and display them together
		Logger log =getLogger();
		
		String tag = name!=null?name:"identity";
		ErrorListener listener = makeErrorListener(log, tag);
		if( listener != null ) {
			getTransformerFactory().setErrorListener(listener);
		}
		// Generate the transformer.
		Transformer transformer;
		if( name != null ){
			transformer = getTransformerFactory().newTransformer(getStyleSheet(name));
		}else{
			transformer = getTransformerFactory().newTransformer();
		}
		transformer.setErrorListener(listener);
		// Push all the parameters form the request into the transformer
		if (params != null) {
			for (String key : params.keySet()) {
				if (params.get(key) != null) {
					transformer.setParameter(key, params.get(key));
				}
			}
		}
		return transformer;
	}
	protected ErrorListener makeErrorListener(Logger log, String tag) {
		ErrorSet error_set = new ErrorSet();
		error_set.setName(tag);
		error_set.setMaxDetails(16);
		error_set.setMaxEntry(16);
		
		ErrorSetErrorListener listener = new ErrorSetErrorListener(tag, log, error_set);
		error_sets.add(error_set);
		return listener;
	}
    public boolean hasErrors(){
    	for( ErrorSet e : error_sets){
    		if( e.size() > 0 ){
    			return true;
    		}
    	}
    	return false;
    }
    public Set<ErrorSet> getErrors(){
    	return error_sets;
    }
//	// The routine that generates the html:
//	protected Document runParametersTransform(Document xmlSource,
//			Map<String, Object> params) throws Exception {
//		// If the report has parameter content the first pass will substitute them.
//		// Is the test as expensive as the transform ??
//		if (ALWAYS_RUN_PARAMETER_TRANSFORM.isEnabled(getContext()) || needParameterTransform()) {
//
//			Transformer transformer = getXSLTransform(
//					"parameters.xsl", params);
//			logSource("parameters.xsl", xmlSource);
//			// Perform the transformation, sending the output to the response.
//			DOMResult result = new DOMResult();
//			transformer.transform(new DOMSource(xmlSource), result);
//			xmlSource = (Document) result.getNode();
//
//		}
//		return xmlSource;
//	}

	

	
	

	/**
	 * Get the text body of the Element The Element is assumed to be a leaf node
	 * with only text content.
	 * 
	 * 
	 * @param e
	 *            Element
	 * @return String
	 */
	protected final String getText(Element e) {
		Node n = e.getFirstChild();
		if (n != null) {
			if (n.getNodeType() == Node.TEXT_NODE) {
				return n.getNodeValue().trim();
			}
		}
		return null;
	}

	
	public static ReportBuilder getInstance(AppContext conn) throws Exception{
		
		// allow ReportBuilder to be overridden using param 
		// Note that as ReportBuilder implements ContextCached
		// this will be cached in the AppContext
		return  conn.makeObjectWithDefault(ReportBuilder.class,ReportBuilder.class, "ReportBuilder");
	}
    public static void setTemplate(AppContext conn,ReportBuilder builder,String templateName) throws Exception{
    	SessionService person = conn.getService(SessionService.class);
    	boolean isDeveloper = person.hasRole(ReportBuilder.REPORT_DEVELOPER);
		if( person != null && isDeveloper){
			builder.setTemplate(templateName,conn.getInitParameter(REPORT_SCHEMA_CONFIG, DEFAULT_REPORT_SCHEMA));
			builder.log_source=true;
		}else{
			builder.setTemplate(templateName);
		}
    }
	

	
	private Document getTemplateDocument() throws DataFault, TransformerFactoryConfigurationError, TransformerException{
		Document reportTemplateDocument = docBuilder.newDocument();
		assert(docBuilder.isNamespaceAware());
		DOMResult result = new DOMResult(reportTemplateDocument);
		Transformer t = getXSLTransform(null, null);
		t.transform(getTemplateSource(), result);
		return reportTemplateDocument;
	}
	public boolean checkNode(Element e) throws TemplateValidateException {
		for( TemplateValidator v : validators){
			if( v.checkNode(e)){
				return true;
			}
		}
		return false;
	}
	public TextFileOverlay getSchemaOverlay(){
		return schema_overlay;
	}
	public TextFileOverlay getReportOverlay(){
		return report_overlay;
	}

	/**
	 * @return the report_type_reg
	 */
	public ReportTypeRegistry getReportTypeReg() {
		return report_type_reg;
	}
	
	protected void checkLimits() {
		if( limits != null) {
			limits.checkLimit();
		}
	}
	
}