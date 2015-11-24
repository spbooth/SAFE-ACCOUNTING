// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.reports;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

import uk.ac.ed.epcc.safe.accounting.ErrorSet;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParserPolicy;
import uk.ac.ed.epcc.safe.accounting.xml.ErrorSetErrorListener;
import uk.ac.ed.epcc.safe.accounting.xml.LSResolver;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.CurrentTimeService;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.content.HtmlBuilder;
import uk.ac.ed.epcc.webapp.content.SimpleXMLBuilder;
import uk.ac.ed.epcc.webapp.content.XMLBuilderSaxHandler;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
import uk.ac.ed.epcc.webapp.exceptions.InvalidArgument;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.inputs.Input;
import uk.ac.ed.epcc.webapp.forms.inputs.InputVisitor;
import uk.ac.ed.epcc.webapp.forms.inputs.ItemInput;
import uk.ac.ed.epcc.webapp.forms.inputs.ListInput;
import uk.ac.ed.epcc.webapp.forms.inputs.MultiInput;
import uk.ac.ed.epcc.webapp.forms.inputs.TextInput;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.TextFileOverlay;
import uk.ac.ed.epcc.webapp.model.TextFileOverlay.TextFile;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.session.AppUser;
import uk.ac.ed.epcc.webapp.session.SessionService;

/**
 * Class to Build Reports from an XML template
 * 
 * @author spb
 * 
 */
@uk.ac.ed.epcc.webapp.Version("$Id: ReportBuilder.java,v 1.137 2015/08/19 18:05:18 spb Exp $")

public class ReportBuilder implements Contexed, TemplateValidator {

	private static final String RESTRICT_EXTENSION_TAG = "RestrictExtension";
	private static final String PARAMETER_EXTENSION_TAG = "ParameterExtension";
	public static final String DEFAULT_REPORT_SCHEMA = "report.xsd";
	public static final String REPORT_SCHEMA_CONFIG = "report.schema";
	public static final String REPORT_TYPE_PARAM = "ReportType";
	public static final String SCHEMA_GROUP = "schema";
	public static final String REPORT_TEMPLATE_GROUP = "report-templates";
	public static final String STYLESHEET_GROUP = "stylesheets";

	
	public class Resolver implements URIResolver {

		public Source resolve(String href, String base)
				throws TransformerException {
			try {
				log.debug("URI resolve href="+href+" base="+base);
				URI base_uri = new URI(base);
				URI target = base_uri.resolve(href);
				URI style = STYLESHEET_URI.relativize(target);
				if (!style.isAbsolute()) {
					log.debug("is stylesheet "+style.getRawPath());
					return getStyleSheet(style.getRawPath());
				}
				URI report = REPORT_URI.relativize(target);
				if (!report.isAbsolute()) {
					log.debug("is report-template "+report.getRawPath());
					return getReport(report.getRawPath());
				}
				URI schema = SCHEMA_URI.relativize(target);
				if (!schema.isAbsolute()) {
					log.debug("is schema "+schema.getRawPath());
					return getSchemaSource(report.getRawPath());

				}
				log.debug("return null");
				return null;
			} catch (Exception e) {
				log.warn("Error in Resolver",e);
				throw new TransformerException(e);
			}
		}

	}
	private Map<String,ReportType> report_type_reg = new LinkedHashMap<String, ReportType>();

	public class ReportTypeInput extends TextInput implements ListInput<String, ReportType>{

		public ReportType getItem() {
			return report_type_reg.get(getValue());
		}

		public void setItem(ReportType item) {
			setValue(item.toString());
		}

		public ReportType getItembyValue(String value) {
			return report_type_reg.get(value);
		}

		public Iterator<ReportType> getItems() {
			Set<ReportType> items = new LinkedHashSet<ReportType>();
			SessionService user = getContext().getService(SessionService.class);
			for(ReportType t : report_type_reg.values()){
				if( t.allowSelect(user)){
					items.add(t);
				}
			}
			return items.iterator();
		}
		public int getCount() {
			int count=0;
			SessionService user = getContext().getService(SessionService.class);
			for(ReportType t : report_type_reg.values()){
				if( t.allowSelect(user)){
					count++;
				}
			}
			return count;
		}

		public String getTagByItem(ReportType item) {
			return item.toString();
		}

		public String getTagByValue(String value) {
			return value;
		}

		public String getText(ReportType item) {
			return item.description;
		}

		@Override
		public <R> R accept(InputVisitor<R> vis) throws Exception {
			return vis.visitListInput(this);
		}
		
	}
	public static final String REPORT_TYPE_CONFIG_PREFIX="report_type";
	
	/** Get sub-classed {@link ReportType}s that are not declared via Config.
	 * 
	 * @return
	 */
	protected Set<ReportType> getSpecialReportTypes(){
		LinkedHashSet<ReportType> special = new LinkedHashSet<ReportType>();
		special.add(HTML);
		special.add(EHTML);
		return special;
	}
	private final void parseReportTypes(){
		for(ReportType t : getSpecialReportTypes()){
			report_type_reg.put(t.name(), t);
		}
		
		
		AppContext conn = getContext();		
		Logger log = conn.getService(LoggerService.class).getLogger(getClass());
		
		String list = conn.getInitParameter(REPORT_TYPE_CONFIG_PREFIX+".list", "");
		for(String name : list.split("\\s*,\\s*")){
			String extension = conn.getInitParameter(REPORT_TYPE_CONFIG_PREFIX+"."+name+".extension", name.toLowerCase());
			String mime = conn.getInitParameter(REPORT_TYPE_CONFIG_PREFIX+"."+name+".mime", "text/"+name.toLowerCase());
			String description = conn.getInitParameter(REPORT_TYPE_CONFIG_PREFIX+"."+name+".description", name.toLowerCase());
			Class<? extends ReportType> clazz = conn.getPropertyClass(ReportType.class, ReportType.class, name);
			try {
				ReportType type= conn.makeParamObject(clazz, name,extension,mime,description);
				if( type != null  ){
					report_type_reg.put(name,type);
				}
			} catch (Exception e) {
				log.debug("Error making report type "+name,e);
			}
			
		}
	}
	/** Look up a report type.
	 * If we can't find by name try looking up by extension.
	 * 
	 * @param text
	 * @return ReportType
	 */
	public ReportType getReportType(String text) {
		ReportType type = report_type_reg.get(text);
		if( type != null){
			return type;
		}
		for(ReportType t : report_type_reg.values()){
			if( t.getExtension().equalsIgnoreCase(text)){
				return t;
			}
		}
		return null;
	}
	public static String getTemplateName(String templateFileName) {
		int dotLocation = templateFileName.lastIndexOf('.');
		if (dotLocation > 0) {
			return templateFileName.substring(0, dotLocation);

		} else {
			return templateFileName;

		}

	}
	// Standard ReportTypes These can be extended from the config
    public static final ReportType	HTML = new ReportType("HTML","html", "text/html","HTML web page"); 
    public static final ReportType	EHTML = new DeveloperReportType("EHTML","html","application/xhtml+xml","embedded XHTML web page");
//    public static final ReportType	PDF = new PDFReportType("PDF","pdf", "application/pdf", "Portable Document Format"); 
//	public static final ReportType FOP = new DeveloperReportType("FOP","fop","text/xml","FOP formating lanuage XML"); 
//    public static final ReportType	CSV = new CSVReportType("CSV","csv", "text/csv", "Comma seperated values");
//    public static final ReportType	XML = new DeveloperReportType("XML","xml", "text/xml","XML"); 
//    public static final ReportType	RXML = new DeveloperReportType("RXML","rxml", "text/xml","Raw XML before final formatting");
//    public static final ReportType	XHTML = new DeveloperReportType("XHTML","xhtml", "application/xhtml+xml","XHTML web page");
//    
//    // This one might be better in the config
//    public static final ReportType OGFXML = new DeveloperReportType("OGFXML","ogfxml", "text/xml","OGF accounting records");
//    public static final ReportType	TXT = new ReportType("TXT","txt", "text/plain", "Plain text output");
	public ReportType getTemplateType(String templateFileName) {

		int dotLocation = templateFileName.lastIndexOf('.');
		if (dotLocation > 0) {
			String extension = templateFileName.substring(dotLocation + 1,
					templateFileName.length());
			return getReportType(extension);

		} else {
			return null;

		}

	}
    private static final String BASE_LOC="http://safe.epcc.ed.ac.uk";
	public static final String PARAMETER_LOC = BASE_LOC+"/parameter";
	private final URI PARAMETER_URI;



	private static final String REPORT_LOC = BASE_LOC+"/report";
	private final URI REPORT_URI=new URI(REPORT_LOC);
	private static final String RESTRICT_LOC = BASE_LOC+"/restrict";
	private final URI RESTRICT_URI=new URI(RESTRICT_LOC);
	private static final String STYLESHEET_LOC = BASE_LOC+"/stylesheet";
	private final URI STYLESHEET_URI=new URI(STYLESHEET_LOC);

	private static final String SCHEMA_LOC = BASE_LOC;
	private final URI SCHEMA_URI=new URI(SCHEMA_LOC);

	private AppContext conn;
	private TextFileOverlay default_overlay, report_overlay,
			stylesheet_overlay, schema_overlay;
	private TextFile template;
	private String template_name;
	
	// private String schema_name = null;
	private DocumentBuilder docBuilder;
	private TransformerFactory tFactory;
	private Set<ErrorSet> error_sets;
	ErrorSet general_error;
	private Set<TemplateValidator> validators;
	private Logger log;
	public static final String REPORT_DEVELOPER = "ReportDeveloper";

	public ReportBuilder(AppContext conn) throws URISyntaxException, ParserConfigurationException {
		PARAMETER_URI = new URI(PARAMETER_LOC);
		
		
		this.conn = conn;
		
		parseReportTypes();
		log = conn.getService(LoggerService.class).getLogger(getClass());
		
		tFactory = TransformerFactory.newInstance();
		
		log.debug("TransformerFactory="+tFactory.getClass().getCanonicalName());
		URL base_url=null;
		
		try {
			base_url = new URL(BASE_LOC);
		} catch (MalformedURLException e) {
			conn.error(e,"Error making base URL");
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
		// Get the stylesheets to use the local URIs to find resources.
		tFactory.setURIResolver(new Resolver());
		error_sets=new HashSet<ErrorSet>();
		general_error=new ErrorSet();
		error_sets.add(general_error);
		validators=new LinkedHashSet<TemplateValidator>();
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
		.newInstance();
		docBuilderFactory.setNamespaceAware(true);
		docBuilder = docBuilderFactory.newDocumentBuilder();
		
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
						conn.getInitParameter("description."+name,name+" ReportType")
				);
				
			}catch(Throwable t){
				conn.error(t,"Error making ReportType "+name);
			}
		}
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
	private Document getParameterDocument() {
		
		return param_document;
	}
	
	public void setTemplate(String template_name) throws DataFault,
			ParserConfigurationException, InvalidArgument,
			TransformerFactoryConfigurationError, TransformerException {

		if (template_name == null || template_name.trim().length() == 0) {
			throw new InvalidArgument("null template specified");
		}
		template_name = template_name.trim();
		String xml_name;
		if (!template_name.endsWith(".xml")) {
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
		Document reportTemplateDocument = docBuilder.newDocument();
		assert(docBuilder.isNamespaceAware());


		// perform the initial transform before everything else.
		Map<String, Object> p = new HashMap<String, Object>();
		p.put(RESTRICT_EXTENSION_TAG, new RestrictExtension(conn, null));
		Transformer transformer = getXSLTransform("initial.xsl",
				p);

		DOMResult result = new DOMResult(reportTemplateDocument);
		transformer.transform(getTemplateSource(), result);

		logSource("Initial ",new DOMSource(reportTemplateDocument));
		param_document= reportTemplateDocument;
		
	}
	public void setTemplate(String template_name,String schema_name) throws DataFault, ParserConfigurationException, InvalidArgument, TransformerFactoryConfigurationError, TransformerException, SAXException, IOException{
		setTemplate(template_name);
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

	private void logSource(String text, Source s){
		try{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Transformer identity = getXSLTransform(null, null);
		identity.transform(s, new
		   StreamResult(out));
		log.debug(text+" source XML is:"+out.toString());
		}catch(Throwable t){
			conn.error(t,"Error in logSource");
		}
	}
	

	public void validate(String schema_name, String template_text)
	throws SAXException, IOException, DataFault {
		validate(schema_name,new StreamSource(new StringReader(template_text)));
	}
	public void validate(String schema_name, Source src)
	throws SAXException, IOException, DataFault {
		Schema s = getSchema(schema_name);
		Validator val = s.newValidator();
		val.validate(src);
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

	public ContentBuilder addParameterText(HtmlBuilder hb) {
		NodeList paramNodes = getParameterDocument().getElementsByTagNameNS(
				PARAMETER_LOC, "Text");
		for (int i = 0; i < paramNodes.getLength(); i++) {
			Node text = paramNodes.item(i);
			hb.open("p");
			hb.clean(text.getTextContent());
			hb.close();
			hb.clean("\n");
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
		
		//This has to go right back to the raw template source as the templateDocument
		//has been processed by the initial transform that removes the access control statements.
		Document doc;
		
		try {
			doc= getTemplateDocument();
		} catch (Exception e) {
			return false;
		}
		
		
		if (person.hasRole(ReportBuilder.REPORT_DEVELOPER)) {
			// log.debug(template_name+" user "+person.getName()+" is report developer");
			return true;
		}
		if( hasErrors()){
			// normally don't show erroneous reports.
			return false;
		}
		RestrictExtension restrict = (RestrictExtension) params.get(RESTRICT_EXTENSION_TAG);
		return restrict.canUse(doc);
	}
	
	public void setupUser(Map<String,Object> params) {
		// Add a user to the session for some of the tests
		SessionService sessionService = conn.getService(SessionService.class);
		sessionService.setCurrentPerson(1);
		AppUser user = sessionService.getCurrentPerson();		
		params.put("User", user);
	}
	public void register(ReportExtension re){
		error_sets.add(re.getErrors());
		validators.add(re);
	}
	public void setupExtensions(Map<String,Object> params) throws ParserConfigurationException{
		setupExtensions(HTML, params);
	}
	public void setupExtensions(ReportType reportType,Map<String,Object> params)
		throws ParserConfigurationException 
	{
		
		
		// Note that reportType might be null when building
		// the parameter form
		NumberFormat nf = NumberFormat.getInstance();
		if( reportType != null){
			nf =	reportType.getNumberFormat(conn);
		}
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
				params.put("AuthenticatedUser", currentPerson);
			}
		}
		// This allows tests to set the time.
		CurrentTimeService current_time = conn.getService(CurrentTimeService.class);
		params.put("CurrentTime", current_time.getCurrentTime());
		// This is to allow custom classes created by reflection
		// like formatters or table transforms access to the parameters.
		conn.setAttribute(ReportBuilder.REPORT_PARAMS_ATTR, params);
		
		//always need parameter and restrict extension
		ReportExtension parme = new ParameterExtension(conn, nf);
		parme.setPolicy(pol);
		parme.setParams(params);
		register(parme);
		params.put(PARAMETER_EXTENSION_TAG, parme);
		RestrictExtension rest = new RestrictExtension(conn, nf);
		rest.setPolicy(pol);
		rest.setParams(params);
		register(rest);
		params.put(RESTRICT_EXTENSION_TAG,rest);
		
		if( reportType != null){
		// Now set the rest of the extensions from the config
		String extension_param_name = "ReportBuilder."+reportType.name()+".extension_list";
		String extension_list = conn.getInitParameter(extension_param_name, "");
		log.debug("extension list "+extension_param_name+"->"+extension_list);
		for(String extension_name : extension_list.split("\\s*,\\s*")){
			extension_name=extension_name.trim();
			if( extension_name.length() > 0){
				Class<? extends ReportExtension> clazz = conn.getPropertyClass(ReportExtension.class, extension_name);
				if( clazz == null ){
					conn.error("Extension "+extension_name+" not defined");
				}else{
					try {
						ReportExtension ext = conn.makeParamObject(clazz, conn,nf);
						String param_name = conn.getInitParameter("ReportBuilder."+extension_name+".name",extension_name);
						log.debug("Adding extension "+ext.getClass().getCanonicalName()+" as "+param_name);
						ext.setPolicy(pol);
						ext.setParams(params);
						register(ext);
						params.put(param_name, ext);
					} catch (Exception e) {
						conn.error(e,"Error making extension "+clazz.getCanonicalName());
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
				PARAMETER_LOC, "Parameter");
		return (paramNodes.getLength() > 0);

	}
	public void buildReportParametersForm(Form form, Map<String, Object> params)
			throws Exception {
		ParameterExtension pe = (ParameterExtension) params
				.get(PARAMETER_EXTENSION_TAG);
		if (pe == null) {
			pe = new ParameterExtension(getContext(), null);
		}
		pe.buildReportParametersForm(form, getParameterDocument());
		ErrorSet es = pe.getErrors();
		es.report(getContext());
		if (es.size() > 0) {
			throw new Exception("Error constructing parameter form");
		}
	}

	public boolean parseReportParametersForm(Form form,
			Map<String, Object> params) {
		if (form == null) {
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

	private void setInputValue(Map<String, Object> params, Input input) {
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
		ReportType type = getReportType(params);
		log.debug("Report type is "+type);
		renderXML(type, params, type.getResult(out));
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
		setupExtensions(EHTML,params);
		for(Object o : params.values()){
			if( o instanceof ReportExtension){
				((ReportExtension)o).setUseReference(true);
			}
		}
		// We pass the params to allow extensions to store XMLGenereator objects and
		// refer to them by reference.
		XMLBuilderSaxHandler handler = new XMLBuilderSaxHandler(builder,params);
		SAXResult result = new SAXResult(handler);
		renderXML(EHTML, params, result);
	}

	public void renderXML(ReportType type, Map<String, Object> params,
			Result out) throws Exception {

		// Get the XML input document
		Source xmlSource = new DOMSource(getParameterDocument());

		// always try parameters.
		xmlSource = runParametersTransform( xmlSource,params);
		// Get the report type
		
		
		
		String transform_list = conn.getInitParameter("ReportBuilder."+type.name()+".transform_list", "identity.xsl");
		log.debug("Transform list ReportBuilder."+type.name()+".transform_list is "+transform_list);
		String transform_names[] = transform_list.split("\\s*,\\s*");
		String name="";
		try{
			for( int i=0; i< (transform_names.length-1) ; i++){
				name = transform_names[i];
				name = name.trim();
				if( name.length() > 0){
					Transformer transformer = getXSLTransform(name,
							params);
					logSource(name, xmlSource);
					// Perform the transformation, sending the output to the response.
					DOMResult result = new DOMResult();
					transformer.transform(xmlSource, result);
					xmlSource = new DOMSource(result.getNode());
				}
			}
			name = transform_names[transform_names.length-1];
			logSource(name, xmlSource);
			Transformer transformer = getXSLTransform(name, params);
			transformer.transform(xmlSource,out );
		}catch(Throwable t){
			getContext().error(t,"Error in transform "+name);
			throw new Exception(t);
		}
	}

	
	protected Source getStyleSheet(String name) throws DataFault {
		TextFile sheet = stylesheet_overlay.find(STYLESHEET_GROUP, name);
		return new StreamSource(sheet.getDataReader(), STYLESHEET_LOC + "/"
				+ name);
	}

	protected Source getReport(String name) throws DataFault {
		TextFile sheet = report_overlay.find(REPORT_TEMPLATE_GROUP, name);
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
		Transformer tf = tFactory.newTransformer();
		tf.transform(src, res);
		return (Document) res.getNode();
	}
	public Schema getSchema(String name) throws DataFault, SAXException {
		

		SchemaFactory factory = SchemaFactory
				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		LSResourceResolver parent = factory.getResourceResolver();
		if( ! (parent instanceof LSResolver)){
			try {
				factory.setResourceResolver(new LSResolver(docBuilder.getDOMImplementation(), schema_overlay, SCHEMA_GROUP, parent));
			} catch (ParserConfigurationException e) {
				conn.error(e,"Error setting schema resolver");
			}
		}
		Source source = getSchemaSource(name);
		if(source == null ){
			return null;
		}
		return factory.newSchema(source);
		

	}

	public Source getTemplateSource() {
		return new StreamSource(template.getDataReader(), REPORT_LOC + "/"
				+ template_name + ".xml");
	}
	

	protected ReportType getReportType(Map<String, Object> params) {
		ReportType type = null;

		// See if there's a param
		Object reportTypeParam = params.get(REPORT_TYPE_PARAM);
		if (reportTypeParam instanceof ReportType) {
			return (ReportType) reportTypeParam;

		}else if( reportTypeParam instanceof String){
			type = report_type_reg.get(reportTypeParam.toString());
		}

		if (type == null) {
			type = HTML;
		}
		return type;
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
		Logger log =getContext().getService(LoggerService.class).getLogger(getClass());
		ErrorSet error_set = new ErrorSet();
		ErrorSetErrorListener listener = new ErrorSetErrorListener(name!=null?name:"identity", log, error_set);
		error_sets.add(error_set);
		tFactory.setErrorListener(listener);
		// Generate the transformer.
		Transformer transformer;
		if( name != null ){
			transformer = tFactory.newTransformer(getStyleSheet(name));
		}else{
			transformer = tFactory.newTransformer();
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
	// The routine that generates the html:
	protected Source runParametersTransform(Source xmlSource,
			Map<String, Object> params) throws Exception {
		// If the report has parameters the first pass will substitute them.
		// Note we look for Parameter not ParameterDef as these may be static
		// values not driven by the form
		if (hasReportParameters()) {

			Transformer transformer = getXSLTransform(
					"parameters.xsl", params);
			logSource("parameters.xsl", xmlSource);
			// Perform the transformation, sending the output to the response.
			DOMResult result = new DOMResult();
			transformer.transform(xmlSource, result);
			xmlSource = new DOMSource(result.getNode());

		}
		return xmlSource;
	}

	

	
	

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

	public AppContext getContext() {
		return conn;
	}

	public static ReportBuilder getInstance(AppContext conn) throws Exception{
		
		// allow ReportBuilder to be overridden using param 
		ReportBuilder builder=conn.makeContexedObject(ReportBuilder.class, "ReportBuilder");
		
		
		
		return builder;
	}
    public static void setTemplate(AppContext conn,ReportBuilder builder,String templateName) throws Exception{
    	SessionService person = conn.getService(SessionService.class);
    	boolean isDeveloper = person.hasRole(ReportBuilder.REPORT_DEVELOPER);
		if( person != null && isDeveloper){
			builder.setTemplate(templateName,conn.getInitParameter(REPORT_SCHEMA_CONFIG, DEFAULT_REPORT_SCHEMA));
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
	
}