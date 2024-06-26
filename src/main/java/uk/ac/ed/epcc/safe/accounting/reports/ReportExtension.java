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
import java.text.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;
import org.w3c.dom.traversal.NodeIterator;

import uk.ac.ed.epcc.safe.accounting.*;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionCast;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
//import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.expr.Parser;
import uk.ac.ed.epcc.safe.accounting.expr.parse.FormatVisitor;
import uk.ac.ed.epcc.safe.accounting.formatters.value.*;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser;
import uk.ac.ed.epcc.safe.accounting.properties.*;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceExpression;
import uk.ac.ed.epcc.safe.accounting.reports.exceptions.ExpressionException;
import uk.ac.ed.epcc.safe.accounting.reports.exceptions.ReportException;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.webapp.*;
import uk.ac.ed.epcc.webapp.content.*;
import uk.ac.ed.epcc.webapp.limits.LimitException;
import uk.ac.ed.epcc.webapp.limits.LimitService;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedProducer;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
import uk.ac.ed.epcc.webapp.session.SessionService;
import uk.ac.ed.epcc.webapp.time.CalendarFieldSplitPeriod;
import uk.ac.ed.epcc.webapp.time.Period;
import uk.ac.ed.epcc.webapp.time.RegularSplitPeriod;
import uk.ac.ed.epcc.webapp.timer.TimerService;

/** Abstract superclass for Reporting Extensions
 * holds methods useful in multiple extensions. 
 * 
 * Each extension also implements {@link TemplateValidator} and should perform
 * additional checks for the Elements that they support.
 * 
 * 
 * Originally methods from these extensions were used to implement custom operations within an associated stylesheet
 * however stylesheet extensions mechanisms are tied to the xlst implementation so it is more standards conforming to
 * have the extension implement the entire DOM to DOM document translation
 * @author spb
 *
 */
public abstract class ReportExtension extends SelectBuilder implements Contexed, TemplateValidator, IdentityDomTransform{
	
	private static final String NAME_ATTR = "name";
	private static final String PARAMETER_REF_NAME = "ParameterRef";
	private static final String MAXIMUM_INTEGER_DIGITS = "max_integer";
	private static final String MINIMUM_INTEGER_DIGITS = "min_integer";
	private static final String MAXIMUM_FRACTIONAL_DIGITS = "max_fraction";
	private static final String MINIMUM_FRACTIONAL_DIGITS = "min_fraction";
	public static final String FORMATTER_PREFIX = "Formatter";
	protected static final String EXPRESSION_PREFIX = "expression:";
	protected static final String PRODUCER_ELEMENT = FILTER_PRODUCER_ELEMENT;
	protected static final String FILTER_ELEMENT = "Filter";
	
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
	
	public static final String PROPERTY_PARAM_PREFIX = "property:";
	private static final String TARGET_ELEMENT = "Target";
	
	private Document doc;
	protected final ErrorSet errors;
	final Logger log;
	protected final ReportType type;
	protected final NumberFormat nf; // number format used for final user display
	protected DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	protected Map<String,Object> params=null;
	protected Set<String> parameter_names=null;
	
	private boolean use_reference = false;
	private TimerService timer;
	
	public ReportExtension(AppContext conn,ReportType type) throws ParserConfigurationException{
		super(conn);
		
		this.type=type;
		if( type == null) {
			this.nf=NumberFormat.getInstance();
		}else {
			this.nf=type.getNumberFormat(conn);
		}
		
		DocumentBuilderFactory fac= DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = fac.newDocumentBuilder();
		doc = builder.newDocument();
		errors=new ErrorSet();
		errors.setName(getClass().getName());
		errors.setMaxDetails(16);
		errors.setMaxEntry(16);
		LoggerService loggin_serv = conn.getService(LoggerService.class);
		if( loggin_serv == null){
			log=null;
		}else{
			log=loggin_serv.getLogger(getClass());
		}
	    timer = conn.getService(TimerService.class);
	}
	/** Get a logger for this class
	 * 
	 * @return
	 */
	public final Logger getLogger(){
		return log;
	}
	/** Utility method to start a named timer
	 * 
	 * @param name
	 */
	protected final void startTimer(String name){
		if( timer != null){
			timer.startTimer(name);
		}
	}
	/** Utility method to stop a named timer
	 * 
	 * @param name
	 */
	protected final void stopTimer(String name){
		if( timer != null){
			timer.stopTimer(name);
		}
	}
	/** Cache the reporting context internally
	 * 
	 * @param type
	 * @param names
	 * @param p
	 */
	public void setParams(ReportType type,Set<String> names,Map<String,Object> p){
		this.parameter_names=names;
		this.params=p;
	}
	/** Add an error to the internal error set
	 * 
	 * @param type
	 * @param details
	 */
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
	/** Add an error to the internal error set. A text version of the 
	 * Node will be added to the detailed error message so only use for
	 * nodes relatively close to the leaf nodes
	 * 
	 */
	public final void addError(String type, String details, Node e){
		addError(type, details+"\n"+makeString(e));
	}
	public final void addError(String type, String details, Node e, Exception t){
		addError(type, details+"\n"+makeString(e),t);
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
		if( t instanceof LimitException) {
			throw (LimitException) t;
		}
	}
	/** Get the ErrorSet for this object
	 * 
	 * @return
	 */
	public ErrorSet getErrors(){
		return errors;
	}
	public String debug(String text){
		if( log != null){
			log.debug(text);
		}
		return text;
	}
	/** get a parameter value.
	 * We can evaluate an expression on a parameter by using name[expr]syntax.
	 * We can also extract configuration parameters by prefixing the name with
	 * <em>property:</em>
	 * @param name
	 * @return
	 */
	public Object getFormParameter(String name) {
		if( name.startsWith(PROPERTY_PARAM_PREFIX)){
			// lookup  config parameter
			return getContext().getInitParameter(name.substring(PROPERTY_PARAM_PREFIX.length()));
		}
		Object value = null;
		if( params != null ){
			if( name.contains("[")){
				String parm_name = name.substring(0,name.indexOf("["));
				String expr_str = name.substring(name.indexOf("[")+1);
				if( expr_str.endsWith("]")){
					expr_str=expr_str.substring(0, expr_str.lastIndexOf("]"));
					value = params.get(parm_name);
					ExpressionTarget et = ExpressionCast.getExpressionTarget(getContext(), value);
					if( et != null){
						Parser p = et.getParser();
						try {
							return et.evaluateExpression(p.parse(expr_str));
						} catch (Exception e) {
							addError("Bad expression parameter", name, e);
						}
					}else{
						addError("Bad parameter", "Invalid parameter expression "+name);
						return null;
					}
				}
			}
			value = params.get(name);
		}
		
		return value;
	}
	
	/** Get a Document object needed to create result Nodes
	 * 
	 * @return Document
	 */
	@Override
	public final Document getDocument(){
		return doc;
	}
	@Override
	public final void setDocument(Document doc) {
		this.doc=doc;
	}
	/** Read a value of a type corresponding to a {@link PropExpression} from a child element
	 * the value can also be provided as a ParameterRef element
	 * @param expr {@link PropExpression} acting as target for the value
	 * @param name Name of the element containing the target
	 * @param elem parent Element
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
	/** Tests that the element has a parameter node of the specified name and that that
	 * node has non-trivial content (or contains a non-null parameter ref). 
	 * Use {@link #hasChild(String, Element)} to test
	 * for a child element without content.
	 * 
	 * @param name
	 * @param elem
	 * @return
	 * @throws ReportException
	 */
	public final boolean hasParam(String name, Element elem) throws ReportException{
		if( elem == null) {
			return false;
		}
		Element v = getParamElementNS(elem.getNamespaceURI(),name, elem);
		if( v == null ) {
			return false;
		}
		if( hasParameterRef(v)) {
			return ( getParameterRef(v) != null);
		}
		String param = normalise(getText(v));
		return param != null && param.trim().length() > 0; 
	}
	/** Get the parameter referenced by a nested ParameterRef element
	 * 
	 * @param e
	 * @return
	 */
	protected Object getParameterRef(Element e){
		// This looks for all decendent nodes but there should only be one
		NodeList list = e.getElementsByTagNameNS(ReportBuilder.PARAMETER_LOC, PARAMETER_REF_NAME);
		if( list.getLength()==1){
			Element ref = (Element) list.item(0);
			return getFormParameter(ref.getAttribute(NAME_ATTR));
		}
		return null;
	}
	/** get an array of parameter values for all ParameterRef elements
	 * that are descendents of the specified element
	 * @param e
	 * @return
	 */
	protected LinkedList getParameterRefList(Element e) {
		LinkedList list = new LinkedList();
		NodeList paramNodes = e.getElementsByTagNameNS(
				ReportBuilder.PARAMETER_LOC,PARAMETER_REF_NAME);
		for(int i=0 ; i < paramNodes.getLength() ; i++) {
			Element ref = (Element) paramNodes.item(i);
			list.add(getFormParameter(ref.getAttribute(NAME_ATTR)));
		}
		return list;
	}
	/** Test for existance of a ParameterRefs as descendant elements of
	 * a target element
	 * @param e
	 * @return
	 */
	protected boolean hasParameterRef(Element e){
		NodeList elems = e.getElementsByTagNameNS(ReportBuilder.PARAMETER_LOC, PARAMETER_REF_NAME);
		return elems.getLength()==1;
	}
	/** Get an integer value from a sub-element.
	 * The sub-element should be from the same namespace as the parent
	 * 
	 * @param name Name of the sub-element
	 * @param def  default value
	 * @param elem parent element
	 * @return
	 * @throws Exception
	 */
	public final int getIntParam(String name, int def, Element elem) throws Exception{
		return getIntParamNS(elem.getNamespaceURI(), name, def, elem);
	}
	/** Get a boolean value from a sub-element
	 * The sub-element should be from the same namespace as the parent
	 * 
	 * @param name
	 * @param def
	 * @param elem
	 * @return
	 * @throws Exception
	 */
	public final boolean getBooleanParam(String name, boolean def, Element elem) throws Exception{
		return getBooleanParamNS(elem.getNamespaceURI(), name, def, elem);
	}
	/** Get a boolean value from a sub-element
	 * 
	 * @param namespace  The namespace of the sub-element
	 * @param name       The name of the sub-element
	 * @param def        The default value to use
	 * @param elem       The parent element
	 * @return
	 * @throws Exception
	 */
	public final boolean getBooleanParamNS(String namespace,String name, boolean def, Element elem) throws Exception{
		Element inner = getParamElementNS(namespace,name, elem);
		if( inner == null) {
			return def;
		}
		String s=null;
		if( hasParameterRef(inner)) {
			Object ref_data = getParameterRef(inner);
			if( ref_data != null ){
				if( ref_data instanceof Boolean) {
					return ((Boolean)ref_data).booleanValue();
				}else if( ref_data instanceof String) {
					s=ref_data.toString();
				}else {
					addError("Expecting Boolean", "Got "+ref_data.toString()+" instead of boolean for "+name);
					return def;
				}
			}
		}else {
		    s = normalise(getText(inner));
		}
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
	public final Number getNumberParam(String name, Number def, Element elem) throws Exception{
		return getNumberParamNS(elem.getNamespaceURI(), name, def, elem);
	}	
	public final Number getNumberParamNS(String namespace,String name, Number def, Element elem) throws Exception{
		Element v;
		if( name == null ){
			v = elem;
		}else{
			v = getParamElementNS(namespace, name, elem);
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
			return def;
		}else{
			final String text = getText(v);
		return parseNumberWithDef( def, text);
		}
	}
	/** Parse a number with a default value (used if the text is empty or fails to parse)
	 * 
	 * @param def
	 * @param s
	 * @return
	 * @throws ParseException
	 */
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
	/** Get an integer value from sub-element with a specific namespace
	 * 
	 * @param namespace
	 * @param name
	 * @param def
	 * @param elem
	 * @return
	 * @throws Exception
	 */
	public final int getIntParamNS(String namespace,String name, int def, Element elem) throws Exception{
		Element inner = getParamElementNS(namespace, name, elem);
		if( inner == null ) {
			return def;
		}
		if( hasParameterRef(inner)) {
			Object ref_data = getParameterRef(inner);
			if( ref_data != null ){
				Number val =  convert(null,Number.class,ref_data);
				if( val != null ){
					return val.intValue();
				}
			}
			return def;
		}
		String s = normalise(getText(inner));
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
	
	
	
	/** Parse a {@link PropertyTag} from a {@link PropertyFinder} with the name specified in
	 * an {@link Element}
	 * 
	 * @param finder
	 * @param element
	 * @return
	 * @throws Exception
	 */
	public PropertyTag getTag(PropertyFinder finder, Element element) throws Exception{
		String data_str = getText(element);
		if( data_str == null || data_str.trim().length() == 0){
			addError("Bad property", "No property specified",element);
			return null;
		}
		return getTag(finder,data_str);
	}
	public PropertyTag getTag(PropertyFinder finder, String name){
		return finder.find(name.trim());
	}
	

	/** format a {@link PropExpression} in a format that can be parsed
	 * 
	 * @param e
	 * @return
	 * @throws Exception 
	 */
	public String formatPropExpression(PropExpression<?> e) throws Exception {
		if( e == null) {
			return null;
		}
		return e.accept(new FormatVisitor());
	}
	

	/** get a template param with config-parameter expansion
	 * 
	 * @param name  The Name of the sub-element containing the parameter
	 * @param elem  The parent {@link Element}
	 * @return
	 * @throws ReportException
	 */
	protected final String getExpandedParam(String name, Element elem)
			throws ReportException {
		String param = getParam(name, elem);
		if( param != null){
			param = getContext().expandText(param);
		}
		return param;
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
		if( target == Boolean.class) {
			if( dat instanceof String) {
				String val = (String)dat;
				return (T) Boolean.valueOf(val);
			}
		}
		if( Number.class.isAssignableFrom(target) ) {
			if( dat instanceof String) {
				try {
					return (T) parseNumber(null, target, (String)dat);
				} catch (ParseException e) {
					addError("convert error", "Error parsing number", e);
				}
			}
		}
		addError("bad type conversion", "Cannot convert "+dat.getClass().getCanonicalName()+" to "+target.getCanonicalName());
		return null;
	}
	
	/** Generic Number parsing class
	 * 
	 * @param nf
	 * @param target
	 * @param value
	 * @return
	 * @throws ParseException
	 */
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
	
	/** Format a value based on its class
	 * 
	 * @param <T>
	 * @param target
	 * @param value
	 * @return
	 * @throws Exception
	 */
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
					IndexedReference ref = (IndexedReference)value;
					if( ref.isNull()){
						return "";
					}
					Indexed obj = ref.getIndexed(getContext());
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
	public <T> String display(ValueFormatter<Number> format, PropExpression<T> tag, T value) throws Exception {
		Object data = value;
		if (tag != null) {
			if( Number.class.isAssignableFrom(tag.getTarget()) && data == null){
				data = Double.valueOf(0.0);
			}
			if( tag instanceof FormatProvider){
				Labeller labeller = ((FormatProvider)tag).getLabeller();
				// check accepts in case we are doing count disctint on an expression with a labeller
				if( labeller != null && labeller.accepts(value) ){
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
	public ValueFormatter<Number> getNumberFormat(Node node){
		
		if( node instanceof Element){
			Element e = (Element) node;
			String  format = e.getAttribute("format");
			if( format != null && ! format.isEmpty()) {
				ValueFormatter vf = getContext().makeObjectWithDefault(ValueFormatter.class, null,FORMATTER_PREFIX, format);
				if( vf != null  && Number.class.isAssignableFrom(vf.getType())) {
					return vf;
				}
		    	
			}
			
			
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
			}catch(Exception t){
				addError("Bad Number Format", t.getMessage(),e);
			}
			return new NumberValueFormatter(result);
		}
		return new NumberValueFormatter(nf);
	}
	public boolean checkNode(Element e) throws TemplateValidateException{
		return false;
	}
	public boolean empty(String value){
		return value == null || value.trim().length() == 0;
	}
	/** PArse a {@link PropExpression} from the contents of a parameter node
	 * 
	 * @param node   parent Node
	 * @param finder  {@link PropertyFinder}
	 * @param name   name of parameter node
	 * @return
	 * @throws ExpressionException
	 */
	public PropExpression getPropertyExpression(Node node, PropertyFinder finder, String name) throws ExpressionException {
		Element element = (Element) node;		
		String data_str=null;
		try {
			data_str = getParam(name, element);
		} catch (Exception e) {
			throw new ExpressionException("Error reading property",e);
		}
		if (data_str == null || data_str.trim().length() == 0) {
			addError("Bad property", "No property specified",node);
			throw new ExpressionException("null expression");
		}
		try {
			PropExpression data_tag = getExpression(finder, data_str);
			if (data_tag == null) {
				addError("Bad property", "No property found for " + data_str,node);
				throw new ExpressionException("No property found for "+data_str);
			}
			return data_tag;
		} catch (UnresolvedNameException e) {
			throw new ExpressionException("unresolved property", e);
		} catch (uk.ac.ed.epcc.safe.accounting.expr.ParseException e) {
			throw new ExpressionException("Parse failed", e);
		}
	}
	/** Parse a {@link PropExpression} from the contents of an Element
	 * 
	 * @param element
	 * @param finder
	 * @return
	 * @throws ExpressionException
	 */
	public PropExpression getPropertyExpression(Element element, PropertyFinder finder) throws ExpressionException {
	
		String data_str=null;
		try {
			data_str = normalise(getText(element));
		} catch (Exception e) {
			throw new ExpressionException("Error reading property",e);
		}
		if (data_str == null || data_str.trim().length() == 0) {
			addError("Bad property", "No property specified",element);
			throw new ExpressionException("null expression");
		}
		try {
			PropExpression data_tag = getExpression(finder, data_str);
			if (data_tag == null) {
				addError("Bad property", "No property found for " + data_str,element);
				throw new ExpressionException("No property found for "+data_str);
			}
			return data_tag;
		} catch (UnresolvedNameException e) {
			throw new ExpressionException("unresolved property", e);
		} catch (uk.ac.ed.epcc.safe.accounting.expr.ParseException e) {
			throw new ExpressionException("Parse failed", e);
		}	
	}
	/** Add a reference to an {@link XMLGenerator} to the document.
	 * The {@link XMLGenerator} will be added to the parameter list
	 * allowing a later stage to call the generator.
	 * Normally this is used for {@link XMLGenerator}s that can generate clickable
	 * links to avoid this being lost during processing stages
	 * 
	 * @param gen
	 * @return
	 */
	public DocumentFragment addReference(XMLGenerator gen){
		Document doc = getDocument();
		DocumentFragment result = doc.createDocumentFragment();
		int i=0;
		while( params.containsKey("xml-generator-"+Integer.toString(i))){
			i++;
		}
		String key = "xml-generator-"+Integer.toString(i);
		parameter_names.add(key);
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
			//TODO consider additional field with formatter for the expression
			return new ExpressionFormat(getContext(), name.substring(EXPRESSION_PREFIX.length()));
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
	    		// finally look for a ValueParser
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
	/** Format an object into a {@link DocumentFragment} controlled by a named formatter.
	 * 
	 * @param param
	 * @param parameterFormat
	 * @return
	 * @throws ReportException
	 */
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
						addDeveloperError("unresolved format", "No formatter found name="+parameterFormat+" class="+param.getClass().getCanonicalName());
					}
				} else {
					// Don't panic, just ignore it and carry on....
					// see if there's a default formatter based on the class name
					format = getFormatter(param.getClass(),param.getClass().getSimpleName());
					
				}
				try{
				if (format != null) {
					if( param instanceof IndexedReference && ! format.getTarget().isAssignableFrom(param.getClass())) {
						// promote refernce to Indexed automatically when applying formatters
						param = ((IndexedReference)param).getIndexed(getContext());
					}
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
						  // This also sets the ProducerTag 
						  rs.setUsageProducer(getText(e));
					  }else if( e.getLocalName().equals(TIME_BOUNDS_ELEMENT)){
						  DateBounds db = getDateProperties(rs, e);
						  rs.setBounds(db.bounds);
						  rs.setUseOverlap(db.overlap);
					  }else{
						  RecordSelector sel = getRecordSelectElement(rs.getFinder(), e);
						  if( sel != null ){
							  rs.addRecordSelector(sel);
						  }
					  }
				  } catch (FilterParseException e1) {
					  rs.addRecordSelector(new SelectClause()); // default to no select on exception
					  addError("Bad Filter",e1.getMessage(),e1);
				  } catch (Exception e1) {
					  rs.addRecordSelector(new SelectClause()); // default to no select on exception
					  addError("Parse error", e1.getMessage(),e1);
				  } 
				  
			  }
		  }	  
		  
		  return rs;
	  }
	protected RecordSet addFilterElementSet(RecordSet set, ElementSet elements) {
		for(Element e : elements) {
			set = addFilterElement(set, e);
		}
		return set;
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
		ReportDateParser p = new ReportDateParser(getValueParser(Date.class));
		String start_string = getParamNS(PERIOD_NS, START_TIME, e);
		if (start_string != null) {
			p.setTime(start, start_string);
	
		} else {
			// for debugging use CurrentTimeService
			CurrentTimeService ct = getContext().getService(CurrentTimeService.class);
			if( ct != null ){
				start.setTime(ct.getCurrentTime());
			}
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
			p.setTime(end, end_string);
			
			if( start.getTime().equals(end.getTime())) {
				// single point
				return new RegularSplitPeriod(start.getTime(), end.getTime(), 1);
			}
	
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
	
	
	
	/** find the period element that is in scope for an element.
	 *  Equivalent to (ancestor::*\/per:Period|per:Period)[last()])
	 * 
	 * @param e
	 * @return
	 * @throws Exception 
	 */
	public Period findPeriodInScope(Element e) throws Exception {
		Element period = ElementSet.ancestors_self(e).select(new Match(PERIOD_NS,PERIOD_ELEMENT)).pollLast();
		return makePeriod(period);
	}
	
	/** Store the table in the parameters list and just reference it
	 * via a processing instruction. This is to expose the table to a ContentBuilder
	 * in an embedded report.
	 * @see {@link XMLBuilderSaxHandler}
	 * @param val
	 */
	public void setUseReference(boolean val) {
		use_reference=val;
	}
	protected DocumentFragment format(Table table, String table_type,String prefix) {
		if( use_reference ){
			if( table != null && table.hasData()){
				return addReference(new TableXMLGenerator(getContext(), nf, table));
			}
		}
		Document document = getDocument();
		
		DocumentFragment frag = document.createDocumentFragment();
		if (table != null && table.hasData()) {
			Class<? extends TableFormatPolicy> clazz = getDefaultTableFormatPolicy();
			if( ! empty(table_type)){
				clazz=getContext().getPropertyClass(TableFormatPolicy.class, getDefaultTableFormatPolicy(), table_type);
			}
			TableFormatPolicy fmt;
			try {
				XMLDomBuilder xmlDomBuilder = new XMLDomBuilder(frag);
				if( prefix != null && ! prefix.isEmpty()) {
					xmlDomBuilder.setNameSpace(ReportBuilder.REPORT_LOC);
					xmlDomBuilder.setPrefix(prefix);
				}
				fmt = getContext().makeParamObject(clazz,xmlDomBuilder, nf);
				fmt.setAllowSpan(type.allowCellSpan());
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
	/** get the xml prefix used in the input document for the report namespace
	 * 
	 * This is so we can create elements without defining a new prefix.
	 * 
	 * @param e
	 * @return
	 */
	public String getReportPrefix(Node e) {
		Document doc  = e.getOwnerDocument();
		String prefix=null;
		NodeList list = doc.getChildNodes();
		// Look for the "report" prefix used by this document
		// we add content in this namespace
		for(int i=0 ; i< list.getLength(); i++) {
			Node n = list.item(i);
			if( n.getNodeType() == Node.ELEMENT_NODE && ReportBuilder.REPORT_LOC.equals(n.getNamespaceURI()) ) {
				return n.getPrefix();
			}
		}
		return null;
	}
	
	public void checkLimit() {
		LimitService limit = getContext().getService(LimitService.class);
		if( limit != null) {
			limit.checkLimit();
		}
	}
	
	public DocumentFragment formatRecordSet(RecordSet set) throws DOMException, Exception {
		Document doc = getDocument();
		DocumentFragment result = doc.createDocumentFragment();
		if( set != null) {
			Element fil = doc.createElementNS(FILTER_LOC, FILTER_ELEMENT);
			result.appendChild(fil);
			String name = set.getProducerTag();
			if( name != null) {
				Element producer = doc.createElementNS(FILTER_LOC,PRODUCER_ELEMENT);
				fil.appendChild(producer);
				producer.appendChild(doc.createTextNode(name));
			}
			PropExpression<Date> bounds[] = set.getBounds();
			if( bounds != null) {
				Element time = doc.createElementNS(FILTER_LOC, TIME_BOUNDS_ELEMENT);
				fil.appendChild(time);
				if( bounds.length == 0) {
					time.appendChild(doc.createElementNS(FILTER_LOC, ALL_TIMES_ELEMENT));
				}else if(bounds.length == 1) {
					Element prop = doc.createElementNS(FILTER_LOC, PROPERTY_ELEMENT);
					time.appendChild(prop);
					prop.appendChild(doc.createTextNode(formatPropExpression(bounds[0])));
				}else if(bounds.length == 2) {
					Element prop = doc.createElementNS(FILTER_LOC, START_PROPERTY_ELEMENT);
					time.appendChild(prop);
					prop.appendChild(doc.createTextNode(formatPropExpression(bounds[0])));
					Element end = doc.createElementNS(FILTER_LOC, END_PROPERTY_ELEMENT);
					time.appendChild(end);
					end.appendChild(doc.createTextNode(formatPropExpression(bounds[1])));
					if( ! set.useOverlap()) {
						time.appendChild(doc.createElementNS(FILTER_LOC, NO_OVERLAP_ELEMENT));
					}
				}
			}
			RecordSelector sel = set.getRecordSelector();
			if( sel != null) {
				try {
					Element e = sel.visit(new RecordSelectorFormatter(this, doc));
					fil.appendChild(e);
				} catch (Exception e) {
					getLogger().error("error formatting RecordSet", e);
				}
			}
		}
		
		return result;
		
	}
	/** recursively walk the tree adding all Filter elements
	   * 
	   * @param up
	   * @param n
	   * @return
	   */
	protected RecordSet addFilterNode(RecordSet up, Node n) {
		debug(makeString(n));
		if( n.getNodeType()==Node.ELEMENT_NODE && n.getLocalName().equals(FILTER_ELEMENT)){
			return addFilterElement(up, (Element)n);
		}else{
			NodeList list =n.getChildNodes();
			for(int i=0;i<list.getLength();i++){
				Node c = list.item(i);
				up = addFilterNode(up,c);
			}
			assert(up != null);
			return up;
		}
		
	}
	
	public Text addText(String text) {
		return getDocument().createTextNode(text);
	}
	/** Make the default RecordSet if no Filter clauses are specified
	   * 
	   * @return RecordSet
	   */
	public RecordSet makeSelector() {
		  try{
			  AccountingService serv = getContext().getService(AccountingService.class);
			  return new RecordSet(serv);
		  }catch(Exception t){
			  addError("RecordSet Error","Error making default selector" , t);
			  return null;
		  }
	  }
	public boolean hasRecords(Period period, RecordSet set) {
		if(set.hasError()) {
			return false;
		}
		try {
			UsageProducer<?> producer = set.getUsageProducer();
			if( producer == null ) {
				return false;
			}
			AndRecordSelector selector = set.getPeriodSelector(period);
	
			return producer.exists(selector);
		} catch (Exception e) {
			addError("Filter Error", "Error checking for records", e);
			return false;
		}
	}
	public ObjectSet makeObjectSet(Element elem) throws ReportException {
	
	
		ObjectSet result = new ObjectSet();
		String target = getParam( TARGET_ELEMENT, elem);
		ExpressionTargetGenerator gen = ExpressionCast.makeExpressionTargetFactory(conn, target);
		if( gen == null ){
			throw new ReportException("No Expression Generator found for target "+target);
		}
		result.setGenerator(gen);
		NodeList list =elem.getChildNodes();
		for(int i=0;i<list.getLength();i++){
			Node c = list.item(i);
			if( c.getNodeType() == Node.ELEMENT_NODE && c.getNamespaceURI() == elem.getNamespaceURI()){
				Element e = (Element)c;
				try {
					if( ! e.getLocalName().equals(TARGET_ELEMENT)){ 
						RecordSelector sel = getRecordSelectElement(result.getGenerator().getFinder(), e);
						if( sel != null ){
							result.addRecordSelector(sel);
						}
					}
				} catch (FilterParseException e1) {
					result.addRecordSelector(new SelectClause()); // default to no select on exception
					result.setError(true);
					addError("Bad Filter",e1.getMessage(),e1);
				} catch (Exception e1) {
					result.addRecordSelector(new SelectClause()); // default to no select on exception
					result.setError(true);
					addError("Parse error", e1.getMessage(),e1);
				} 
	
			}
		}	    
	
		return result;
	}
	@SuppressWarnings("unchecked")
	public DocumentFragment inlineParameterRef(Node node) throws ReportException {
		Element element = (Element)node;
		
		
	
		if( element == null ){
			addError("Malformed Parameter", "Missing Parameter or Value element");
			return getDocument().createDocumentFragment();
		}
		String parameterName = this.getAttribute(NAME_ATTR, element);
		Object param = getFormParameter(parameterName);
		
		if( parameterName == null ){
			addError("Missing name", "No name specified for Parameter element");
			// No name
			return getDocument().createDocumentFragment();
		}
		if( param instanceof Number) {
			// force raw formatting of numbers
			param =  ((Number)param).toString();
		}
		
		return formatObject(param, null);
	
	}

}