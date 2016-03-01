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

import java.io.ByteArrayOutputStream;
import java.security.Principal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.ac.ed.epcc.safe.accounting.ErrorSet;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
//import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.expr.Parser;
import uk.ac.ed.epcc.safe.accounting.formatters.value.DomFormatter;
import uk.ac.ed.epcc.safe.accounting.formatters.value.DomValueFormatter;
import uk.ac.ed.epcc.safe.accounting.formatters.value.ValueFormatter;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTargetFactory;
import uk.ac.ed.epcc.safe.accounting.properties.UnresolvedNameException;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceExpression;
import uk.ac.ed.epcc.safe.accounting.reports.exceptions.ReportException;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.Indexed;
import uk.ac.ed.epcc.webapp.content.FormatProvider;
import uk.ac.ed.epcc.webapp.content.Labeller;
import uk.ac.ed.epcc.webapp.content.Table;
import uk.ac.ed.epcc.webapp.content.TableFormatPolicy;
import uk.ac.ed.epcc.webapp.content.TableXMLFormatter;
import uk.ac.ed.epcc.webapp.content.TableXMLGenerator;
import uk.ac.ed.epcc.webapp.content.XMLBuilderSaxHandler;
import uk.ac.ed.epcc.webapp.content.XMLDomBuilder;
import uk.ac.ed.epcc.webapp.content.XMLGenerator;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedProducer;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
import uk.ac.ed.epcc.webapp.session.SessionService;
import uk.ac.ed.epcc.webapp.time.CalendarFieldSplitPeriod;
import uk.ac.ed.epcc.webapp.time.Period;
import uk.ac.ed.epcc.webapp.time.RegularSplitPeriod;

/** Abstract superclass for Reporting Extensions
 * holds methods useful in multiple extensions. 
 * 
 * Each extension also implements {@link TemplateValidator} and should perform
 * additional checks for the Elements that they support.
 * @author spb
 *
 */
public abstract class ReportExtension extends SelectBuilder implements Contexed, TemplateValidator{
	
	private static final String MAXIMUM_INTEGER_DIGITS = "max_integer";
	private static final String MINIMUM_INTEGER_DIGITS = "min_integer";
	private static final String MAXIMUM_FRACTIONAL_DIGITS = "max_fraction";
	private static final String MINIMUM_FRACTIONAL_DIGITS = "min_fraction";
	public static final String FORMATTER_PREFIX = "Formatter";
	protected static final String EXPRESSION_PREFIX = "expression:";
	protected static final String PRODUCER_ELEMENT = FILTER_PRODUCER_ELEMENT;
	protected static final String FILTER_ELEMENT = "Filter";
	public static final String FILTER_LOC = "http://safe.epcc.ed.ac.uk/filter";
	protected static final String PERIOD_ELEMENT = "Period";
	protected static final String NUMBER_OF_SPLIT_UNITS = "NumberOfSplitUnits";
	protected static final String SPLIT_UNIT = "SplitUnit";
	protected static final String YEAR = "Year";
	protected static final String MONTH = "Month";
	protected static final String WEEK = "Week";
	protected static final String HOUR = "Hour";
	protected static final String MINUTE = "Minute";
	protected static final String SECOND = "Second";
	protected static final String DAY = "Day";
	protected static final String NUMBER_OF_SPLITS = "NumberOfSplits";
	protected static final String START_TIME = "StartTime";
	protected static final String END_TIME = "EndTime";
	public static final String PERIOD_NS = "http://safe.epcc.ed.ac.uk/period";
	protected static SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");
	private static SimpleDateFormat altTimestampFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	private static SimpleDateFormat altDateFormat = new SimpleDateFormat("dd-MM-yyyy");
	private static SimpleDateFormat altMonthFormat = new SimpleDateFormat("MM-yyyy");
	private static SimpleDateFormat fmts[] = {altTimestampFormat,timestampFormat,altDateFormat,dateFormat,altMonthFormat,monthFormat};
	private final Document doc;
	private final ErrorSet errors;
	final Logger log;
	protected final NumberFormat nf; // number format used for final user display
	protected DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	protected Map<String,Object> params=null;
	private boolean use_reference = false;
	
	public ReportExtension(AppContext conn,NumberFormat nf) throws ParserConfigurationException{
		super(conn);
		
		
		this.nf=nf;
		
		DocumentBuilderFactory fac= DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = fac.newDocumentBuilder();
		doc = builder.newDocument();
		errors=new ErrorSet();
		errors.setName(getClass().getName());
		LoggerService loggin_serv = conn.getService(LoggerService.class);
		if( loggin_serv == null){
			log=null;
		}else{
			log=loggin_serv.getLogger(getClass());
		}
	
	}
	protected final Logger getLogger(){
		return log;
	}
	public void setParams(Map<String,Object> p){
		this.params=p;
	}
	
	public final void addError(String type,String details){
		log.debug("add error "+type+" "+details);
		errors.add(type, details);
	}
	/** Add an error only if the current user is a report developer
	 * 
	 * @param type
	 * @param details
	 */
	public final void addDeveloperError(String type,String details){
		if( getContext().getService(SessionService.class).hasRole(ReportBuilder.REPORT_DEVELOPER)){
			addError(type, details);
		}
		log.debug(details);
	}
	public final void addError(String type, String details, Node e){
		addError(type, details+makeString(e));
	}
	/** A debugging method to generate a String version of a Node
	 * 
	 * @param e
	 * @return
	 */
	public String makeString(Node e){
		Transformer t;
		try {
			t = TransformerFactory.newInstance().newTransformer();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			t.transform(new DOMSource(e), new StreamResult(out));
			return out.toString();
		} catch (Exception e1) {
			log.error("Error performing transform",e1);
		}
		return "";
	}
	public final void addError(String type,String details,Throwable t){
		log.warn("extension error:"+type+":"+details, t);
		errors.add(type, details,t);
	}
	public ErrorSet getErrors(){
		return errors;
	}
	public String debug(String text){
		if( log != null){
			log.debug(text);
		}
		return text;
	}
	/** Get a form parameter by name
	 * 
	 * @param name
	 * @return parameter
	 */
	public Object getFormParameter(String name){
		if( params != null ){
			return params.get(name);
		}
		return null;
	}
	
	/** Get a Document object needed to create result Nodes
	 * 
	 * @return Document
	 */
	protected final Document getDocument(){
		return doc;
	}
	/** Parse a {@link PropExpression} in a nested element
	 * the value can also be provided as a ParameterRef element
	 * @param expr
	 * @param name
	 * @param elem
	 * @return
	 * @throws Exception
	 */
	protected <T> T getParamExpressionValue(PropExpression<T> expr, String name,
			Element elem) throws Exception {
				Element v = getParamElementNS(elem.getNamespaceURI(), name, elem);
				if( v == null ){
					return null;
				}
				if( hasParameterRef(v)){
					Object ref_data = getParameterRef(v);
					if( ref_data != null ){
						IndexedProducer prod = null;
						if(expr instanceof ReferenceExpression){
							prod = ((ReferenceExpression)expr).getFactory(getContext());
						}
						@SuppressWarnings("unchecked")
						T val = (T) convert(prod,expr.getTarget(),ref_data);
						if( val != null ){
							return val;
						}
					}
					return null;
				}else{
					final String text = getText(v);
					
					if( text == null || text.trim().length()==0){
						return null;
					}
					final String format = getAttribute(FORMAT_ATTR, v);
					return parse(expr,format,text);
				}
			}
	@Override
	protected <T> SelectClause<T> getSelectClause(PropExpression<T> expr, MatchCondition cond, Element e)
			throws Exception {
				T value = getParamExpressionValue(expr, VALUE_ELEMENT, e);
				if( value == null ){
					// assume optional form input if no value
					return null;
				}
				return new SelectClause<T>(expr,cond,value);
			}
	/** Tests that the element has a parameter node of the specified name and that that
	 * node has non-trivial content. Use {@link #hasChild(String, Element)} to test
	 * for a child element without content.
	 * 
	 * @param name
	 * @param elem
	 * @return
	 * @throws ReportException
	 */
	protected final boolean hasParam(String name, Element elem) throws ReportException{
		String param = getParam(name, elem);
		return param != null && param.trim().length() > 0; 
	}
	/** Get the parameter referenced by a nested ParameterRef element
	 * 
	 * @param e
	 * @return
	 */
	protected Object getParameterRef(Element e){
		NodeList list = e.getElementsByTagNameNS(ReportBuilder.PARAMETER_LOC, "ParameterRef");
		if( list.getLength()==1){
			Element ref = (Element) list.item(0);
			return getFormParameter(ref.getAttribute("name"));
		}
		return null;
	}
	/** Test for existance of a ParameterRef
	 * 
	 * @param e
	 * @return
	 */
	protected boolean hasParameterRef(Element e){
		//String s = makeString(e);
		NodeList elems = e.getElementsByTagNameNS(ReportBuilder.PARAMETER_LOC, "ParameterRef");
		return elems.getLength()==1;
	}
	
	protected final int getIntParam(String name, int def, Element elem) throws Exception{
		String s = getParam(name,elem);
		if( s == null || s.trim().length() == 0){
			return def;
		}
		try{
			return Integer.parseInt(s.trim());
		}catch(Exception e){
		  addError("Error parsing Integer param","Param "+name+" value"+s,e);
		  return def;
		}
	}
	protected final boolean getBooleanParam(String name, boolean def, Element elem) throws Exception{
		String s = getParam(name,elem);
		if( s == null || s.trim().length() == 0){
			return def;
		}
		try{
			return Boolean.parseBoolean(s.trim());
		}catch(Exception e){
		  addError("Error parsing Boolean param","Param "+name+" value"+s,e);
		  return def;
		}
	}
	/** Get a numeric value from document.
	 * This will resolve ParameterRef elements.
	 * 
	 * @param name Name of sub-element to query (null to query elem)
	 * @param def  default value to return
	 * @param elem 
	 * @return Number
	 * @throws Exception 
	 */
	protected final Number getNumberParam(String name, Number def, Element elem) throws Exception{
		Element v;
		if( name == null ){
			v = elem;
		}else{
			v = getParamElementNS(elem.getNamespaceURI(), name, elem);
		}
		if( v == null ){
			return def;
		}
		if( hasParameterRef(v)){
			Object ref_data = getParameterRef(v);
			if( ref_data != null ){
				Number val =  convert(null,Number.class,ref_data);
				if( val != null ){
					return val;
				}
			}
			return null;
		}else{
			final String text = getText(v);
		return parseNumberWithDef( def, text);
		}
	}
	protected static Number parseNumberWithDef(Number def, String s) throws ParseException {
		if( s == null || s.trim().length() == 0){
			return def;
		}
		s = s.trim();
		NumberFormat nf = NumberFormat.getInstance();
		try {
			return nf.parse(s);
		} catch (ParseException e1) {
			if( def == null){
				throw e1;
			}
		}
		return def;
	}
	
	protected final int getIntParamNS(String namespace,String name, int def, Element elem) throws Exception{
		String s = getParamNS(namespace,name,elem);
		if( s == null || s.trim().length() == 0){
			return def;
		}
		try{
			return Integer.parseInt(s.trim());
		}catch(Exception e){
		  addError("Error parsing Integer param","Param "+name+" value"+s,e);
		  return def;
		}
	}
	
	
	
	
	public PropertyTag getTag(PropertyTargetFactory up, Element element) throws Exception{
		String data_str = getText(element);
		if( data_str == null || data_str.trim().length() == 0){
			addError("Bad property", "No property specified",element);
			return null;
		}
		return getTag(up,data_str);
	}
	public PropertyTag getTag(PropertyTargetFactory up, String name){
		return up.getFinder().find(name.trim());
	}
	protected PropExpression getExpression(PropertyTargetFactory up, String expr){
		Parser p = new Parser(conn, up.getFinder());
		try {
			return p.parse(expr.trim());
		} catch (UnresolvedNameException e) {
			addError("Bad Property",expr+" Cannot resolve:"+e.getUnresolvedName());
			debug("Cannot resolve:"+e.getUnresolvedName());
			debug("PropertyFinder is "+e.getFinder());
			for(PropertyTag tag : e.getFinder().getProperties()){
				debug(" "+tag.getName());
			}
		} catch (uk.ac.ed.epcc.safe.accounting.expr.ParseException e) {
			addError("BadExpression", expr,e);
		}
		return null;
	}
	
	
	public <T> T getValue(PropExpression<T> tag, Element element) throws  Exception{
		return parse(tag,getAttribute(FORMAT_ATTR, element),getText(element));
	}
	
	public <T> PropertyTag<? extends T> getTag(PropertyTargetFactory up,Class<? extends T> target, String name) {
		return getTag(up.getFinder(),target,name);
	}
	public <T> PropertyTag<? extends T> getTag(PropertyFinder finder,Class<? extends T> target, String name) {
		return finder.find(target,name);
	}
	
	
	/** Perform known conversions to a desired target type.
	 * @param fac IndexedProducer (only needed if converting to an IndexedReference)
	 * @param target
	 * @param dat
	 * @return converted value
	 */
	@SuppressWarnings("unchecked")
	public <T> T convert(IndexedProducer fac, Class<T> target, Object dat){
		if( dat == null ){
			return null;
		}
		if( target.isAssignableFrom(dat.getClass())){
			return (T) dat;
		}
		if( target ==  String.class){
			return (T) dat.toString();
		}
		if( dat instanceof IndexedReference && Indexed.class.isAssignableFrom(target)){
			IndexedReference ref = (IndexedReference) dat;
			Indexed i = ref.getIndexed(getContext());
			if( target.isAssignableFrom(i.getClass())){
				return (T)i;
			}
		}
		if( fac != null && target == IndexedReference.class){
			if( dat instanceof Indexed){
				return (T) fac.makeReference((Indexed)dat);
			}
			if( dat instanceof Number){
				return (T) fac.makeReference(((Number)dat).intValue());
			}
		}
		addError("bad type conversion", "Cannot convert "+dat.getClass().getCanonicalName()+" to "+target.getCanonicalName());
		return null;
	}
	
	
	public static Number parseNumber(NumberFormat nf,Class target, String value) throws ParseException{
		if( target == Integer.class){
			return  Integer.parseInt(value);
		}else if( target == Long.class){
			return  Long.parseLong(value);
		}else if( target == Float.class){
			return Float.parseFloat(value);
		}else if( target == Double.class){
			return Double.parseDouble(value);
		}
		if( nf != null ){
			return nf.parse(value);
		}
		return NumberFormat.getInstance().parse(value);
	}
	
	/** Format a property value in a way that may be re-parsed in a later processing
	 * stage ie compatible with the parse method
	 * @param tag 
	 * @param value 
	 * @param <T> 
	 * @return String
	 * @throws Exception 
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <T> String format(PropExpression<T> tag, T value) throws Exception {
		if( value == null ){
			return "";
		}
		if (tag != null) {
			ValueParser vp = tag.accept(parse_vis);
			return vp.format(value);
		}
		return value.toString();
	}
	
	
	public <T> String formatByClass(Class<? extends T> target, T value) throws Exception{	
			if( value == null ){
				return "";
			}
			ValueParser<T> vp = getValueParser(target);
			
			if( vp != null){
				String s = vp.format(value);
				if( s != null ){
					return s;
				}
			}
			// Now fallbacks for types where we don't have enough info to parse
			if ( Principal.class.isAssignableFrom(target)){
				return ((Principal)value).getName();
			} else if ( Indexed.class.isAssignableFrom(target)){
				return Integer.toString(((Indexed)value).getID());
			}else if ( IndexedReference.class.isAssignableFrom(target)){
				return Integer.toString(((IndexedReference)value).getID());
			}
		return value.toString();
	}
	/** get a human readable string for an object that will not need to be re-parsed 
	 * 
	 * @param target
	 * @param value
	 * @return String
	 * @throws Exception
	 */
	public <T> String displayByClass(Class<? extends T> target, T value) throws Exception{	
		if( value == null ){
			return "";
		}
		// check special display rules first before looking for a valueparser
				if ( Principal.class.isAssignableFrom(target)){
					return ((Principal)value).getName();
				} else if (DataObject.class.isAssignableFrom(target)){
					return ((DataObject)value).getIdentifier();
				}else if ( Indexed.class.isAssignableFrom(target)){
				
					return Integer.toString(((Indexed)value).getID());
				}else if ( IndexedReference.class.isAssignableFrom(target)){
					Indexed obj = ((IndexedReference)value).getIndexed(getContext());
					return displayByClass(obj.getClass(), obj);
				}
		ValueParser<T> vp = getValueParser(target);
		
		if( vp != null){
			String s = vp.format(value);
			if( s != null ){
				return s;
			}
		}
		
	return value.toString();
}
	/** Format a property for final display to the user.
	 * 
	 * @param <T>
	 * @param format {@link NumberFormat}
	 * @param tag {@link PropExpression} being formatted (can be null).
	 * @param value
	 * @return String
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public <T> String display(NumberFormat format, PropExpression<T> tag, T value) throws Exception {
		Object data = value;
		if (tag != null) {
			if( Number.class.isAssignableFrom(tag.getTarget()) && data == null){
				data = Double.valueOf(0.0);
			}
			if( tag instanceof FormatProvider){
				Labeller labeller = ((FormatProvider)tag).getLabeller();
				if( labeller != null ){
					return labeller.getLabel(conn, value).toString();
				}
			}
		}
		// use specified number format in preference ie csv
		if( format != null && data instanceof Number){
			String text = format.format((Number) data);
			return text;
		}
		return format(tag,value);
	}
	/** Get a {@link NumberFormat} optionally customised by Node attributes
	 * 
	 * @param node
	 * @return
	 */
	public NumberFormat getNumberFormat(Node node){
		
		if( node instanceof Element){
			Element e = (Element) node;
			NumberFormat result;
			if( nf == null){
				result=NumberFormat.getInstance();
			}else{
				result=(NumberFormat) nf.clone();
			}
			try{
				if( e.hasAttribute(MINIMUM_FRACTIONAL_DIGITS)){
					result.setMinimumFractionDigits(Integer.parseInt(e.getAttribute(MINIMUM_FRACTIONAL_DIGITS)));
				}
				if( e.hasAttribute(MAXIMUM_FRACTIONAL_DIGITS)){
					result.setMaximumFractionDigits(Integer.parseInt(e.getAttribute(MAXIMUM_FRACTIONAL_DIGITS)));
				}
				if( e.hasAttribute(MINIMUM_INTEGER_DIGITS)){
					result.setMinimumIntegerDigits(Integer.parseInt(e.getAttribute(MINIMUM_INTEGER_DIGITS)));
				}
				if( e.hasAttribute(MAXIMUM_INTEGER_DIGITS)){
					result.setMaximumIntegerDigits(Integer.parseInt(e.getAttribute(MAXIMUM_INTEGER_DIGITS)));
				}
			}catch(Throwable t){
				addError("Bad Number Format", t.getMessage(),e);
			}
			return result;
		}
		return nf;
	}
	public boolean checkNode(Element e) throws TemplateValidateException{
		return false;
	}
	public boolean empty(String value){
		return value == null || value.trim().length() == 0;
	}
	public PropExpression getPropertyExpression(Node node, PropertyTargetFactory producer, String name) {
		Element element = (Element) node;		
		String data_str=null;
		try {
			data_str = getParam(name, element);
		} catch (Exception e) {
			addError("Bad Property", "Error reading property",e);
			return null;
		}
		if (data_str == null || data_str.trim().length() == 0) {
			addError("Bad property", "No property specified",node);
			return null;
		}
		PropExpression data_tag = getExpression(producer, data_str);
		if (data_tag == null) {
			addError("Bad property", "No property found for " + data_str);
			return null;
		}
		return data_tag;
			
	}
	/**
	 * Gets the PropertyTag in the sub-tag 'name'.
	 * 
	 * @param node the XML node
	 * @param producer the producer, required to create the PropertyTags.
	 * @param name the name of the sub-tag.
	 * 
	 * @return the property tag
	 */
	public PropertyTag getProperty(Node node, PropertyTargetFactory producer, String name) {
		Element element = (Element) node;		
		String data_str=null;
		try {
			data_str = getParam(name, element);
		} catch (Exception e) {
			addError("Bad Property", "Error reading property",e);
			return null;
		}
		if (data_str == null || data_str.trim().length() == 0) {
			addError("Bad property", "No property specified",node);
			return null;
		}
		PropertyTag data_tag = getTag(producer, data_str);
		if (data_tag == null) {
			addError("Bad property", "No property found for " + data_str);
			return null;
		}
		return data_tag;
			
	}
	
	public DocumentFragment addReference(XMLGenerator gen){
		Document doc = getDocument();
		DocumentFragment result = doc.createDocumentFragment();
		int i=0;
		while( params.containsKey("xml-generator-"+Integer.toString(i))){
			i++;
		}
		String key = "xml-generator-"+Integer.toString(i);
		params.put(key,gen);
		result.appendChild(doc.createProcessingInstruction(XMLBuilderSaxHandler.EXTERNAL_CONTENT_PI, key));
		return result;
	}
	/** Generate a {@link DomFormatter} for the specified class based on a <b>format</b> string.
	 * Default behaviour is to look for a {@link DomFormatter} class definition using the qualifier <b>Formatter</b> (see {@link AppContext#makeObjectWithDefault(Class,Class,String,String)}
	 * Failing that a {@link ValueParser} is retrieved and converted into a DomFormatter.
	 * <p>
	 * If the format starts with <b>expression:</b> This is taken to be a PropExpression to be
	 * evaluated on the target object.
	 * @param clazz target class of formatter
	 * @param name format string
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected DomFormatter getFormatter(Class clazz, String name) {
		if ( name.startsWith(EXPRESSION_PREFIX) ){
			if( clazz != null && ExpressionTarget.class.isAssignableFrom(clazz) ){
		
				//TODO consider additional field with formatter for the expression
				return new ExpressionFormat(getContext(), name.substring(EXPRESSION_PREFIX.length()));
			}else{
				addDeveloperError("invalid_expression_format", "Class "+clazz.getCanonicalName()+" is not an expression target");
				return null;
			}
	    }else {
	    	DomFormatter f = getContext().makeObjectWithDefault(DomFormatter.class, null,FORMATTER_PREFIX, name);
	    	if( f != null ){
	    		log.debug("Got formatter "+f.getClass().getCanonicalName());
	    		return f;
	    	}
	    	// try a ValueFormatter
	    	ValueFormatter vf = getContext().makeObjectWithDefault(ValueFormatter.class, null,FORMATTER_PREFIX, name);
	    	if( vf != null ){
	    		return new DomValueFormatter(vf);
	    	}
	    	if( clazz != null ){
	    		// finally look for a ValueFormatter
	    		ValueParser parser = parse_vis.getValueParser(clazz, name);
	    		if( parser != null ){
	    			return new DomValueFormatter(parser);
	    		}else{
	    			addDeveloperError("no_formatter", "Cannot resolve "+name+" to formatter for "+clazz.getCanonicalName());
	    		}
	    	}
		}
		addDeveloperError("no_formatter", "No formatter found for "+name);
		log.debug("No formatter found for "+name);
		return null;
	}
	protected DocumentFragment formatObject(Object param, String parameterFormat)
			throws ReportException {
				Document doc=getDocument();
				DocumentFragment result = doc.createDocumentFragment();
				if( param == null ){
					// no parameter found but might be an optional parameter
					return result;
				}
			
				// See if there a formatter for the object?
				DomFormatter format = null;
					
				if (parameterFormat != null && !parameterFormat.trim().equals("")) {
					format = getFormatter(param.getClass(),parameterFormat);	
					if( format == null ){
						// developer will want to know this did not work
						addDeveloperError("unresolved format", "No formatter fount name="+parameterFormat+" class="+param.getClass().getCanonicalName());
					}
				} else {
					// Don't panic, just ignore it and carry on....
					// see if there's a default formatter based on the class name
					format = getFormatter(param.getClass(),param.getClass().getSimpleName());
					
				}
				try{
				if (format != null) {
					if( format.getTarget().isAssignableFrom(param.getClass())){
						Logger log = getContext().getService(LoggerService.class).getLogger(getClass());
						log.debug(" format is "+parameterFormat+" "+format.getClass().getName()+" value is "+param.getClass().getCanonicalName()+" "+param);
						Node n=format.format(doc,param);
						if( n != null ){
							result.appendChild(n);
						}else{
							log.debug("Formatter returns null node");
						}
					}else{
						addError("bad_format", "cannot apply format "+format.getClass().getCanonicalName()+" to class "+param.getClass().getCanonicalName()+" value="+param.toString());
					}
					
				} else {
					ValueParser vp = getValueParser(param.getClass());
					result.appendChild(doc.createTextNode( vp.format(param)));
					
				}
				}catch(Exception e){
					throw new ReportException("Error formatting parameter", e);
				}
				return result;
			}
	/** Add a "Filter" container to the {@link RecordSet}.
	   * 
	   * @param rs
	   * @param filter
	   * @return
	   */
	protected RecordSet addFilterElement(RecordSet rs, Element filter) {
		  NodeList list =filter.getChildNodes();
		  for(int i=0;i<list.getLength();i++){
			  Node c = list.item(i);
			  if( c.getNodeType() == Node.ELEMENT_NODE && c.getNamespaceURI() == filter.getNamespaceURI()){
				  Element e = (Element)c;
				  try {
					  if( e.getLocalName().equals(PRODUCER_ELEMENT)){
						  // This should strictly be first in a Filter element
						  // as reseting the producer clears the selector
						  rs.setUsageProducer(getText(e));
					  }else if( e.getLocalName().equals(TIME_BOUNDS_ELEMENT)){
						  rs.setBounds(getDateProperties(rs, e));
					  }else{
						  RecordSelector sel = getRecordSelectElement(rs.getUsageProducer().getFinder(), e);
						  if( sel != null ){
							  rs.addRecordSelector(sel);
						  }
					  }
				  } catch (FilterParseException e1) {
					  rs.addRecordSelector(new SelectClause()); // default to no select on exception
					  addError("Bad Filter",e1.getMessage(),e1);
				  } catch (Throwable e1) {
					  rs.addRecordSelector(new SelectClause()); // default to no select on exception
					  addError("Parse error", e1.getMessage(),e1);
				  } 
				  
			  }
		  }	  
		  
		  return rs;
	  }
	public Period makePeriod(Node region) throws Exception {
	
		
	
		Element e = null;
		if (region != null) {
			
			if (region.getNodeType() == Node.ELEMENT_NODE) {
				e = (Element) region;
			} else {
				throw new uk.ac.ed.epcc.safe.accounting.reports.exceptions.ParseException("Expecting Region as argument");
			}
	
		} else {
			debug("region is null");
	
		}
	
		// Start time
		Calendar start = Calendar.getInstance();
		String start_string = getParamNS(PERIOD_NS, START_TIME, e);
		if (start_string != null) {
			setTime(start, start_string);
	
		} else {
			// default setting to start of previous month
			start.add(Calendar.MONTH, -1);
			start.set(Calendar.DAY_OF_MONTH, 1);
			start.set(Calendar.HOUR_OF_DAY, 0);
			start.set(Calendar.MINUTE, 0);
			start.set(Calendar.SECOND, 0);
			start.set(Calendar.MILLISECOND, 0);
	
		}
		debug("Start is " + start.getTime());
	
		String end_string = getParamNS(PERIOD_NS,END_TIME, e);
		String unit = getParamNS(PERIOD_NS,SPLIT_UNIT,e);
	
		if( end_string != null) {
	
			Calendar end = Calendar.getInstance();
			setTime(end, end_string);
	
			// This conditional is put in to catch the case where a user 
			// specifies a start time and end time where start <= end (which is 
			// not valid).  If this happens we override the end time with the 
			// default (which is 1 month from the starT).
			if(start.getTimeInMillis() < end.getTimeInMillis()){
	
				debug("end is "+end.getTime());
	
				// Number of splits
				int nsplits =getIntParamNS(PERIOD_NS,NUMBER_OF_SPLITS, 4, e);
				if( nsplits < 2){
					// constraint on plotcode
					nsplits=2;
				}
	
				Period period = new RegularSplitPeriod(start.getTime(),end.getTime(),nsplits);
				return period;
	
			}else{
				// Default to 1 month start to end based on assuming the start time is correct 
				// and discarding the supplied end time.
	
				//End time
				end = Calendar.getInstance();
				// default to one month from start
				end.setTime(start.getTime());
				end.add(Calendar.MONTH,1);
	
				debug("end is "+end.getTime()+" this has been set as the supplied end and start times were incorrect (i.e. start >= end)" );
				
				// Number of splits
				int nsplits=2;
	
				Period period = new RegularSplitPeriod(start.getTime(),end.getTime(),nsplits);
				return period;
			}
	
	
	
		} else if( unit != null) {
			// Try for Calendar field based split
			int field = Calendar.DAY_OF_YEAR;
			if (unit.equalsIgnoreCase(DAY)) {
				field = Calendar.DAY_OF_YEAR;
	
			} else if (unit.equalsIgnoreCase(SECOND)) {					
				field = Calendar.SECOND;
	
			} else if (unit.equalsIgnoreCase(MINUTE)) {
				field = Calendar.MINUTE;
	
			} else if (unit.equalsIgnoreCase(HOUR)) {
				field = Calendar.HOUR;
	
			} else if (unit.equalsIgnoreCase(WEEK)) {
				field = Calendar.WEEK_OF_YEAR;
	
			} else if (unit.equalsIgnoreCase(MONTH)) {
				field = Calendar.MONTH;
	
			} else if (unit.equalsIgnoreCase(YEAR)) {
				field = Calendar.YEAR;
	
			} 
			int count = getIntParamNS(PERIOD_NS, NUMBER_OF_SPLIT_UNITS, 1, e);
	
			// Number of splits
			int nsplits =getIntParamNS(PERIOD_NS,NUMBER_OF_SPLITS, 1, e);
	
			return new CalendarFieldSplitPeriod(start, field, count, nsplits);
	
		} else {
	
			//End time
			Calendar end = Calendar.getInstance();
			// default to one month from start
			end.setTime(start.getTime());
			end.add(Calendar.MONTH,1);
	
			// Number of splits
			int nsplits =getIntParamNS(PERIOD_NS,NUMBER_OF_SPLITS, 4, e);
			if( nsplits < 2){
				// constraint on plotcode
				nsplits=2;
			}
	
			Period period = new RegularSplitPeriod(start.getTime(),end.getTime(),nsplits);
			return period;
		}
	
	}
	protected void setTime(Calendar time, String timeString) throws Exception {
	
		AppContext conn=getContext();
		if( timeString.equalsIgnoreCase("Epoch")){
			time.setTimeInMillis(0);
			return;
		}
		if( timeString.equalsIgnoreCase("Now")){
			time.setTimeInMillis(System.currentTimeMillis());
			return;
		}
		if( timeString.equalsIgnoreCase("Forever")){
			time.setTimeInMillis(Long.MAX_VALUE);
		}
	    ValueParser<Date>vp = getValueParser(Date.class);
	    timeString=timeString.trim();
	    boolean search=true;
	    for( int i=0; search && i< fmts.length; i++){
	    	try{
	    		SimpleDateFormat df = fmts[i];
	    		String pattern = df.toPattern();
	
	    		// We need to supress the 2 diget year matching
	    		// as this can incorrectly match days or months.
	    		if( pattern.length() == timeString.length() && pattern.indexOf('-') == timeString.indexOf('-')){
	    			time.setTime(fmts[i].parse(timeString));
	    			search = false;
	    		}
	    	}catch(Exception e){
	    		
	    	}
	    }
	    if( search ){
	    	try{
	    		if( vp != null ){
	    			time.setTime((Date) vp.parse(timeString.trim()));
	    		}
	    	}catch(Exception e5){
	    		throw new uk.ac.ed.epcc.safe.accounting.reports.exceptions.ParseException("Cannot parse "+timeString+" as date/time");
	    	}
	    }
			
	}
	/** Store the table in the parameters list and just reference it
	 * via a processing instruction. This is to expose the table to a ContentBuilder
	 * in an embedded report.
	 * 
	 * @param val
	 */
	public void setUseReference(boolean val) {
		use_reference=val;
	}
	protected DocumentFragment format(Table table, String type) {
		if( use_reference ){
			if( table != null && table.hasData()){
				return addReference(new TableXMLGenerator(getContext(), nf, table));
			}
		}
		DocumentFragment frag = getDocument().createDocumentFragment();
		if (table != null && table.hasData()) {
			Class<? extends TableFormatPolicy> clazz = getDefaultTableFormatPolicy();
			if( ! empty(type)){
				clazz=getContext().getPropertyClass(TableFormatPolicy.class, getDefaultTableFormatPolicy(), type);
			}
			TableFormatPolicy fmt;
			try {
				fmt = getContext().makeParamObject(clazz,new XMLDomBuilder(frag), nf);
				fmt.add(table);
			} catch (Exception e) {
				addError("format_error", "Error formatting table", e);
			}		
		}
		return frag;
	}
	protected Class<?extends TableFormatPolicy> getDefaultTableFormatPolicy() {
		return TableXMLFormatter.class;
	}
}