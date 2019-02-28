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

import java.lang.reflect.Constructor;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import uk.ac.ed.epcc.safe.accounting.AccountingService;
import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.charts.MapperEntryInput;
import uk.ac.ed.epcc.safe.accounting.charts.PlotEntryInput;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionCast;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.formatters.value.DomFormatter;
import uk.ac.ed.epcc.safe.accounting.formatters.value.ValueFormatter;
import uk.ac.ed.epcc.safe.accounting.model.SetParamsVisitor;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParserService;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.reports.exceptions.ParameterParseException;
import uk.ac.ed.epcc.safe.accounting.reports.exceptions.ReportException;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.PropertyTargetGenerator;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.ClassTableCreator;
import uk.ac.ed.epcc.webapp.CurrentTimeService;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.exceptions.ParseException;
import uk.ac.ed.epcc.webapp.forms.inputs.BooleanInput;
import uk.ac.ed.epcc.webapp.forms.inputs.BoundedInput;
import uk.ac.ed.epcc.webapp.forms.inputs.CalendarFieldPeriodInput;
import uk.ac.ed.epcc.webapp.forms.inputs.DateInput;
import uk.ac.ed.epcc.webapp.forms.inputs.DayMultiInput;
import uk.ac.ed.epcc.webapp.forms.inputs.DoubleInput;
import uk.ac.ed.epcc.webapp.forms.inputs.EnumInput;
import uk.ac.ed.epcc.webapp.forms.inputs.ErrorInput;
import uk.ac.ed.epcc.webapp.forms.inputs.InfoInput;
import uk.ac.ed.epcc.webapp.forms.inputs.Input;
import uk.ac.ed.epcc.webapp.forms.inputs.IntegerInput;
import uk.ac.ed.epcc.webapp.forms.inputs.LengthInput;
import uk.ac.ed.epcc.webapp.forms.inputs.ListInput;
import uk.ac.ed.epcc.webapp.forms.inputs.LongInput;
import uk.ac.ed.epcc.webapp.forms.inputs.MonthMultiInput;
import uk.ac.ed.epcc.webapp.forms.inputs.MultiInput;
import uk.ac.ed.epcc.webapp.forms.inputs.OptionalInput;
import uk.ac.ed.epcc.webapp.forms.inputs.OptionalListInput;
import uk.ac.ed.epcc.webapp.forms.inputs.OptionalListInputWrapper;
import uk.ac.ed.epcc.webapp.forms.inputs.ParseInput;
import uk.ac.ed.epcc.webapp.forms.inputs.RealInput;
import uk.ac.ed.epcc.webapp.forms.inputs.RegularPeriodInput;
import uk.ac.ed.epcc.webapp.forms.inputs.RelativeDateInput;
import uk.ac.ed.epcc.webapp.forms.inputs.SetInput;
import uk.ac.ed.epcc.webapp.forms.inputs.SimplePeriodInput;
import uk.ac.ed.epcc.webapp.forms.inputs.TextInput;
import uk.ac.ed.epcc.webapp.forms.inputs.TimeStampInput;
import uk.ac.ed.epcc.webapp.forms.result.FormResult;
import uk.ac.ed.epcc.webapp.jdbc.filter.AndFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterPolicy;
import uk.ac.ed.epcc.webapp.jdbc.filter.GenericBinaryFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.model.data.CloseableIterator;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.forms.Selector;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
import uk.ac.ed.epcc.webapp.session.SessionService;
/** The ParameterExtension expands objects in the parameter map into the output.
 * The object may be expanded as text or as XML elements.
 * This can be controlled by the <b>format</b> attribute.
 * <ul>
 * <li>If the format attribute exists it is used to generate a {@link DomFormatter} using {@link #getFormatter(Class, String)}</li>
 * <li>If the format attribute does not exist, an attempt is made to look up the format using the simple name for the objects class.</li>
 * <li>If that fails the object is formatted as text using the  {@link ValueParser} appropriate to its type.
 * </ul>
 * 
 * This class also contains the logic to build parameter forms to populate the parameter map 
 * based on the <b>ParameterDef</b> elements.
 * 
 * @author spb
 *
 */

/** Extension to expand parameter values into the document text.
 * 
 * @author spb
 *
 */
public class ParameterExtension extends ReportExtension {
	
    private static final String TITLE_ATTR = "title";
	static final String PARAMETER_REF_ELEMENT = "ParameterRef";
	private static final String FORMAT_PARAMETER_ELEMENT = "FormatParameter";
	private static final String FOR_ELEMENT = "For";
	private static final String CONTENT_ELEM = "Content";
	private static final String SOURCE_ATTR = "source";
	private static final String REPEAT_ELEMENT = "Repeat";
	private static final String SPLITTER_PREFIX = "Splitter";
	private static final String SPLIT_ATTR = "split";
	private static final String VAR_ATTR = "var";
	public static final String PARAMETER_ELEM = "Parameter";
	private static final String FORMAT_ATTR = "format";
	private static final String LENGTH_ATTR = "length";
	private static final String OPTIONAL_ATTR = "optional";
	private static final String MIN_ATTR = "min";
	private static final String MAX_ATTR = "max";
	private static final String UNSELECTED_ATTR = "unselected";
	private static final String VALUE_ATTR = "value";
	private static final String NAME_ATTR = "name";
	static final String PARAMETER_DEF_ELEMENT = "ParameterDef";
	static final String PARAMETER_STAGE_ELEMENT = "Stage";
	public static final String PARAMETER_LOC = "http://safe.epcc.ed.ac.uk/parameter";	
	public ParameterExtension(AppContext ctx, NumberFormat nf)
			throws ParserConfigurationException {
		super(ctx,nf);
		
	}
	protected Set<String> variable_names=new LinkedHashSet<>();
	/** Build a form based on the ParameterDef elements in the report.
	 * 
	 * @param form
	 * @param reportTemplateDocument
	 * @throws Exception
	 */
	
	public boolean buildReportParametersForm(FormResult self,Form form, Document reportTemplateDocument) throws Exception {
		SetParamsVisitor setter = new SetParamsVisitor(false, params);
		
		// Find the parameters which have been defined
		NodeList paramNodes = reportTemplateDocument.getElementsByTagNameNS(
				PARAMETER_LOC,"*");

		for( int i=0; i < paramNodes.getLength() ; i++){
			Element param =  (Element) paramNodes.item(i);
			switch( param.getLocalName()) {
			case PARAMETER_DEF_ELEMENT:
			// Get the name
			String name = getAttribute(NAME_ATTR,param);
			
			//TODO consider value attributes for multi-input value[.name]* maybe
			// Get the default value if there is one.
			String value = getAttribute(VALUE_ATTR,param);
			
			
			Input<?> input = getInput(name, value,param);
			if( input == null ){
				addError("no input","No input for "+name);
				input = new ErrorInput("An error occured");
			}else{
				try{
					setValue(name,value, input,param);
				}catch(Exception t){
					addError("bad_parameter_value", "input name="+name+" type="+input.getClass().getName()+" value="+value, t);
				}

				input = configureInput( param, input);
			}
			// Set up the label
			String label = getAttribute("label",param);		
			if (empty(label)) {
				label = getContext().getInitParameter("form.label." + name, name);
			}
			String title = getAttribute(TITLE_ATTR, param);
			if( empty(title)) {
				title=null;
			}
			form.addInput(name, label, title,input);
			// set any value from params
			// this ensures multi-stage form will validate
			// while being built if early stages are set from params
			
			
			if( params.containsKey(name)) {
				input.accept(setter);
			}
			break;
			case PARAMETER_STAGE_ELEMENT:
				if( form.poll(self)) {
					assert(params != null);
					ReportBuilder.extractReportParametersFromForm(form, params);
				}else {
					return false;
				}
			break;
			default:
			}

		}
		return true;
	}
	protected Input<?> configureInput( Element param,
			Input<?> input) {
		AppContext conn=getContext();
		int maxWidth = conn.getIntegerParameter("forms.max_text_input_width", 64);
		if (input instanceof LengthInput) {
			LengthInput lengthInput = (LengthInput) input;				
			// Get the max length value if there is one.
			int maxLength = -1;
		
			String length = getAttribute(LENGTH_ATTR,param);
			if (! empty(length)) {
				maxLength = Integer.parseInt(length);	
				lengthInput.setMaxResultLength(maxLength);
			}else{
				int width = lengthInput.getBoxWidth();
				if( width > maxWidth){
					lengthInput.setBoxWidth(maxWidth);
				}
			}
		}

		// Set up the optionality
		if (input instanceof OptionalInput) {
			OptionalInput optionalInput = (OptionalInput) input;
			boolean isOptional = false;
			String optional=getAttribute(OPTIONAL_ATTR,param);						
			if (! empty(optional)) {
				isOptional = Boolean.parseBoolean(optional);		
				optionalInput.setOptional(isOptional);
			}
		}
		// set min/max
		if( input instanceof BoundedInput) {
			BoundedInput boundedInput = (BoundedInput)input;
			String min = getAttribute(MIN_ATTR, param);
			if( ! empty(min)) {
				try {
					boundedInput.setMin(input.convert(min));
				}catch(Exception t) {
					addError("bad minimum", min,t);
				}
			}
			String max = getAttribute(MAX_ATTR, param);
			if( ! empty(max)) {
				try {
					boundedInput.setMax(input.convert(max));
				}catch(Exception t) {
					addError("bad maximum", max,t);
				}
			}
		}
		String unselected = getAttribute(UNSELECTED_ATTR, param);
		if( ! empty(unselected)){
			if( input instanceof OptionalListInput){
				((OptionalListInput)input).setUnselectedText(unselected);
			}else if (input instanceof ListInput ){
				input = new OptionalListInputWrapper((ListInput) input, unselected);
			}
		}
		return input;
	}

	@SuppressWarnings("unchecked")
	private <X> void setValue(String name,String value, Input<X> input, Element param) throws ParseException  {
		if(input == null){
			return;
		}
		
			if( value != null && value.trim().length() > 0){
				if( input instanceof ParseInput){
					((ParseInput)input).parse(value);
				}else{
					//also try the convert method
					// if the input is a multi-input but not a parse input
					// this might be easier.
					input.setValue(input.convert(value));
				}
			}
			if( input instanceof MultiInput){
				MultiInput multi = (MultiInput) input;
				// look for nested values
				NodeList list = param.getChildNodes();
				if( list != null){
					for(int i=0; i< list.getLength();i++){
						Node n = list.item(i);
						if( n.getNodeType() == Node.ELEMENT_NODE && n.getLocalName().equals("Value")){
							Element value_element = (Element) n;
							String sub_name=value_element.getAttribute(NAME_ATTR);
							String sub_value = value_element.getAttribute(VALUE_ATTR);
							if( sub_name != null && value !=null ){
								setValue(name+"."+sub_name,sub_value,multi.getInput(sub_name),value_element);
							}
						}
					}
					
				}
			}
		
	}
	@SuppressWarnings("unchecked")
	private Input<?> getInput(String name,String value,Element param) throws Exception {
		AppContext conn = getContext();
		
		String type = getAttribute("type",param);
		String role=getAttribute("role",param);	
		CurrentTimeService time = getContext().getService(CurrentTimeService.class);
		Date now = time.getCurrentTime();
			
		//TODO make this more parameter configurable
		if (type.equals("ReadOnly")) {
			if (value != null) {	
				return new InfoInput(value);
			} else {
				throw new ParameterParseException("Readonly parameter "+name+" has no value.");
			}

		} else if (type.equals("Long")) {		
			return new LongInput();			

		} else if (type.equals("Integer")) {
			return new IntegerInput();

		} else if (type.equals("Float")) {
			return new RealInput();

		} else if (type.equals("Double")) {
			return new DoubleInput();

		} else if (type.equals("String")) {
			return new TextInput();			

		} else if (type.equals("Date")) {
			return new RelativeDateInput();
			//return new DayMultiInput();	
		} else if (type.equals("Month")) {
			return new MonthMultiInput();
			//return new MonthInput();	
		} else if (type.equals("TimeStamp")) {
			return new TimeStampInput();

		} else if (type.equals("Boolean")) {
			return new BooleanInput();

		} else if (type.equals("List")) {
			SetInput<String> set_input = new SetInput<>();
			NodeList list = param.getChildNodes();
			for (int j = 0; j < list.getLength(); j++) {
				Node n = list.item(j);
				if (n.getNodeType() == Node.ELEMENT_NODE
						&& n.getLocalName().equals("Choice")
						&& n.getNamespaceURI().equals(
								param.getNamespaceURI())) {
					set_input.addChoice(getText((Element) n));
				}
			}
			return set_input;

		} else if (type.equals("Plot")) {
			AccountingService service = conn.getService(AccountingService.class);
			String prod=getAttribute("producer",param);
			String tag=getAttribute("tag", param);
			UsageProducer producer;
			if( empty(prod)  ){
				producer= service.getUsageProducer();
			}else{
				producer = service.getUsageProducer(prod);
			}
			return new PlotEntryInput(conn, producer, tag);

		} else if (type.equals("Group")) {
			AccountingService service = conn.getService(AccountingService.class);
			String prod=getAttribute("producer",param);
			String tag=getAttribute("tag", param);
			UsageProducer producer;
			if( empty(prod)  ){
				producer= service.getUsageProducer();
			}else{
				producer = service.getUsageProducer(prod);
			}
			return new MapperEntryInput(conn, producer, tag);
						
		} else if (type.equals("Period")){
			return new SimplePeriodInput();
		}else if (type.equals("RegularSplitPeriod")){
			return new RegularPeriodInput(now);
		}else if (type.equals("CalendarPeriod")){
			return new CalendarFieldPeriodInput(now);
		}else if (type.equals("DayCalendarPeriod")){
			return new CalendarFieldPeriodInput(now,Calendar.DATE);
		}else if (type.equals("MonthCalendarPeriod")){
			return new CalendarFieldPeriodInput(now,Calendar.MONTH);
		}
		// Not a built in type try various methods in turn
		
		// first a role selector
		if (! empty(role)) {
			type=conn.getInitParameter("typealias."+type, type);
			DataObjectFactory fac = conn.makeObjectWithDefault(DataObjectFactory.class,null,type);
			if( fac != null ){
				AndFilter fil = new AndFilter(fac.getTarget());
				if(role.startsWith("#")){
					// Starting a role with a hash supresses the default select-filter
					role=role.substring(1);
				}else{
					//narrow the default selector by default
					fil.addFilter(fac.getFinalSelectFilter());
				}
				BaseFilter fil2 = conn.getService(SessionService.class).getRelationshipRoleFilter(fac, role);
				if( fil2 == null ){
					fil2 = new GenericBinaryFilter(fac.getTarget(),false);
				}
				fil.addFilter(fil2);
				BaseFilter efil = getFilter(param, fac);
				if( efil !=null) {
					fil.addFilter(efil);
				}
				return fac.getInput(fil);
			}
		}
		
		// Try an Enum
		try {					
			Class<?> theClass = Class.forName(type);
			if (theClass.isEnum()) {
				EnumInput<?> newInput = new EnumInput(theClass);
				if (value != null) {
					newInput.setValue(newInput.convert(value));
				}
				return newInput;	
			}

		} catch (ClassNotFoundException ex) {
		}		
		// See if it's a simple Selector e.g a DataObject
		Selector factory = conn.makeObjectWithDefault(Selector.class,null,type);			
		if (factory != null) {
			if( factory instanceof DataObjectFactory) {
				DataObjectFactory fac = (DataObjectFactory) factory;
				// try to make explicit filter based on composites
				BaseFilter efil = getFilter(param, fac);
				if( efil != null) {
					return fac.getInput(efil);
				}
			}
			return factory.getInput();	

		} 


		throw new ParameterParseException("Invalid parameter type "+type);
	}
	private BaseFilter getFilterFromPolicy(DataObjectFactory fac,Element e) {
		String name = e.getAttribute("name");
		if( name == null ) {
			addError("bad FilterPolicy","No name attribute", e);
			return null;
		}
		Class<? extends FilterPolicy> clazz = getContext().getPropertyClass(FilterPolicy.class, name);
		if( clazz == null ) {
			addError("bad FilterPolicy","No class definition for "+name, e);
			return null;
		}
		LinkedList list = getParameterRefList(e);
		list.addFirst(fac);
		Object args[] = list.toArray();
		Constructor<? extends FilterPolicy> cons = getContext().findConstructor(clazz, args);
		if( cons == null ) {
			addError("bad FilterPolicy","No matching constructor for "+clazz.getSimpleName(), e);
			return null;
		}
		try {
			return cons.newInstance(args).getFilter();
		} catch (Exception e1) {
			addError("bad FilterPolicy","Error constructing "+clazz.getSimpleName(), e1);
			return null;
		}
	}
	private BaseFilter getFilter(Element e,DataObjectFactory prod) throws Exception {
		AndFilter result = new AndFilter(prod.getTarget());
		ExpressionTargetFactory ptf = ExpressionCast.getExpressionTargetFactory(prod);
		if( ptf != null){
			// If factory implements the correct interface further narrow the selection using
			// embedded filter clauses
			
			PropertyFinder finder = ptf.getFinder();
		
			NodeList paramNodes = (e).getElementsByTagNameNS(
					FilterExtension.FILTER_LOC,"Filter");
			if( paramNodes.getLength() > 0 ){
				AndRecordSelector selector = new AndRecordSelector();
				for( int i=0; i < paramNodes.getLength() ; i++){
					Element filter =  (Element) paramNodes.item(i);
					 NodeList list =filter.getChildNodes();
					  for(int j=0;j<list.getLength();j++){
						  Node c = list.item(j);
						  if( c.getNodeType() == Node.ELEMENT_NODE && c.getNamespaceURI() == filter.getNamespaceURI()){
							  RecordSelector s = getRecordSelectElement(finder,  (Element)c);
							  if( s != null ){
								  selector.add(s);
							  }
						  }
					  }	  
				}
				result.addFilter( (BaseFilter) ptf.getAccessorMap().getFilter(selector));
			}
		}
		NodeList policyNodes = e.getElementsByTagNameNS(PARAMETER_LOC, "FilterPolicy");
		AndFilter and = new AndFilter<>(prod.getTarget());
		for( int i=0 ; i < policyNodes.getLength() ; i++) {
			BaseFilter f = getFilterFromPolicy(prod,(Element) policyNodes.item(i));
			if( f != null ) {
				result.addFilter(f);
			}
		}
		if( result.isEmpty()) {
			return null;
		}
		return result;
	}
	
	public DocumentFragment value(Node node) throws DOMException, Exception{
		Element element = (Element)node;
		return parameter(element.getElementsByTagNameNS(PARAMETER_LOC, "Value").item(0));
	}

	public boolean isSet(String name){
		Object dat= getFormParameter(name);
		if( dat == null ){
			return false;
		}
		if( dat instanceof Boolean){
			return ((Boolean)dat).booleanValue();
		}
		if( dat.toString().trim().length() == 0){
			return false;
		}
		return true;
	}
	
	/** Expand a template using properties from a Parameter 
	 * 
	 * @param name
	 * @param template
	 * @return DocumentFragment
	 */
	public DocumentFragment formatParameter(String name, NodeList template){
		Document doc=getDocument();
		DocumentFragment result = doc.createDocumentFragment();
		Object o = getFormParameter(name);
		if( o != null && ! (o instanceof ExpressionTarget)){
			o = ExpressionCast.getExpressionTarget(o);
		}
		if( o != null && o instanceof ExpressionTarget){
			ExpressionExpander expander = new ExpressionExpander(getContext(), parse_vis);
			
			expander.setExpressionTarget((ExpressionTarget)o);
			for(int i=0;i<template.getLength();i++){
				Node new_n = copyNode(expander,doc, template.item(i));
				if( new_n != null){
					result.appendChild(new_n);
				}
			}
			result.appendChild(doc.createTextNode("\n"));
		}else {
			addError("Invalid parameter for format", o == null ? "null" : o.getClass().getSimpleName());
		}
		return result;
	}
	/** Perform a node clone with re-write.
	 * Similar to Document.importNode but with sufficient explicit tree-walking
	 * to get access to the Nodes that need re-writing
	 * 
	 * 
	 * @param r UsageRecord being written
	 * @param doc Document to clone into
	 * @param e Node to clone
	 * @return cloned Node or null
	 */
	private Node copyNode(ExpressionExpander expander,Document doc, Node e){
		
		if( e.getNodeType() == Node.ELEMENT_NODE ){
			return  copyElement(expander,doc,(Element)e);
		}else if( e.getNodeType() == Node.TEXT_NODE){
			return editNode(expander,doc,(Text) e);
		}else{
			// default to a deep import of the node
			return doc.importNode(e, true);
		}
	}

	
/** Generate a copy of the element. May return null if element is to be 
 * omitted from output.
 * 
 * @param expander
 * @param doc
 * @param e
 * @return copy of Node or null
 */
	@SuppressWarnings("unchecked")
	private Node copyElement(ExpressionExpander expander,Document doc, Element e) {
	
		String namespaceURI = e.getNamespaceURI();
		String nodeName = e.getLocalName();
		
		//TODO consider flag to control isTrivial check
		if( PARAMETER_LOC.equals(namespaceURI)){
			if( "IfDef".equals(nodeName)){
				String prop = e.getAttribute("required");
				// only process contents if required property not null
				if( prop != null ){
					
					if( expander.isDefined(prop) ){
							DocumentFragment result = doc.createDocumentFragment();
							NodeList contents = e.getChildNodes();
							for(int i=0;i<contents.getLength();i++){
								Node item = contents.item(i);
								Node n = copyNode(expander,doc, item);
								if( n != null){
									result.appendChild(n);
								}
							}
							return result;
					}
				}else{
					addError("missing attribute","No required attribute in IfDef");
				}
			}else if( "IfNDef".equals(nodeName)){
				String prop = e.getAttribute("required");
				// only process contents if required property is null
				if( prop != null ){
					if( ! expander.isDefined(prop) ){
							DocumentFragment result = doc.createDocumentFragment();
							NodeList contents = e.getChildNodes();
							for(int i=0;i<contents.getLength();i++){
								Node item = contents.item(i);
								Node n = copyNode(expander,doc, item);
								if( n != null){
									result.appendChild(n);
								}
							}
							return result;
					}
				}else{
					addError("missing attribute","No required attribute in IfDef");
				}
			}else if ( "If".equals(nodeName)){
				String prop = e.getAttribute("expr");
				
				if( prop != null ){
					PropExpression expr = expander.parse(prop);
			
					if( expr != null ){
						
						String value = e.getAttribute("value");
						Object record_value = expander.evaluate(expr);
						Object target_value=null;
						try {
							target_value = parse(expr, null,value);
						} catch (Exception e1) {
							addError("parse error", "Error parsing "+value+" as "+expr.toString(), e1);
						}
						MatchCondition m = null;
						String match= e.getAttribute("match");
						if( match != null && match.length() > 0){
							try{
							m = MatchCondition.valueOf(match);
							}catch(Exception x){
								addError("bad match code", "match code "+match+" illegal", e);
							}
						}
						if ( ! Comparable.class.isAssignableFrom(expr.getTarget())){
							m = null;
						}
						if( record_value != null && target_value != null ){
							if( (m == null && record_value.equals(target_value)) || 
									( m != null && m.compare(record_value, target_value))){

								// expand contents
								DocumentFragment result = doc.createDocumentFragment();
								NodeList contents = e.getChildNodes();
								for(int i=0;i<contents.getLength();i++){
									Node item = contents.item(i);
									Node n = copyNode(expander, doc, item);
									if( n != null){
										result.appendChild(n);
									}
								}
								return result;
							}
						}
					}else{
						addError("missing property", "Property "+prop+" not found");
					}
				}else{
					addError("missing attribute","No property attribute in If");
				}
			}
			return null;
		}else{
			Element new_e = doc.createElementNS(namespaceURI, nodeName);
			if( e.hasAttributes()){
				NamedNodeMap attr = e.getAttributes();
				for( int i=0 ;i<attr.getLength();i++){
					Attr a = (Attr) attr.item(i);
					new_e.setAttributeNodeNS((Attr) editNode(expander,doc,a));
				}
			}
			if( e.hasChildNodes()){
				NodeList children = e.getChildNodes();
				for(int i=0;i<children.getLength();i++){
					Node new_n = copyNode(expander,doc, children.item(i));
					if( new_n != null){
						new_e.appendChild(new_n);
					}
				}
			}
			return new_e;
		}
	}
	public boolean isTrivial(Object o){
		if( o == null ){
			return true;
		}
		if( o instanceof IndexedReference){
			return ((IndexedReference)o).isNull();
		}
		
		if( o instanceof String ){
			return ((String)o).trim().length() == 0;
		}
		if( o instanceof Date ){
			return ((Date)o).getTime() == 0L;
		}
		return false;
	}
	/** duplicate a leaf  Node editing its value
	 * 
	 * @param doc
	 * @param a
	 * @return
	 */
	private Node editNode(ExpressionExpander expander,Document doc,Node a) {
		Node new_a = doc.importNode(a, true);
		new_a.setNodeValue(editString(expander,a.getNodeValue()));
		return new_a;
	}
	/** parameter expansion on text.
	 * 
	 * @param r
	 * @param orig
	 * @return
	 */
	private String editString(ExpressionExpander expander,String orig){
		return expander.expand(orig);
	}
	@SuppressWarnings("unchecked")
	public DocumentFragment parameter(Node node) throws ReportException {
		Element element = (Element)node;
		
		

		if( element == null ){
			addError("Malformed Parameter", "Missing Parameter or Value element");
			return getDocument().createDocumentFragment();
		}
		String parameterName = this.getAttribute(NAME_ATTR, element);
		Object param = getFormParameter(parameterName);
		String parameterFormat = getAttribute(FORMAT_ATTR, element);
		if( parameterName == null ){
			addError("Missing name", "No name specified for Parameter element");
			// No name
			return getDocument().createDocumentFragment();
		}
		
		
		return formatObject(param, parameterFormat);

	}
	/** Do repeated expansion of a document fragment based on a parameter that provides a series of values
	 * 
	 * @param node
	 * @return DocumentFragment
	 */
	public DocumentFragment repeat(Node node){
		Element element = (Element)node;
		
		Document doc=getDocument();
		DocumentFragment result = doc.createDocumentFragment();

		if( element == null ){
			addError("Malformed Parameter", "Missing Parameter or Value element");
			return result;
		}
		String parameterName = this.getAttribute(NAME_ATTR, element);
		if( parameterName == null ){
			addError("Missing name", "No name specified for Parameter element");
			// No name
			return result;
		}
		Object param = getFormParameter(parameterName);
		
		String variable = this.getAttribute(VAR_ATTR, element);
		if( variable == null || variable.trim().length() == 0){
			addError("Bad Repeat", "No variable specified");
			return result;
		}
		if( parameter_names != null && parameter_names.contains(variable)){
			addError("Bad Variable","Overiding the existing parameter named "+variable);
			return result;
		}
		if( parameter_names != null){
			parameter_names.add(variable);
		}
		
		String splitter = this.getAttribute(SPLIT_ATTR, element);
		Splitter split = getContext().makeObjectWithDefault(Splitter.class, null, SPLITTER_PREFIX, splitter);
		try{
			variable_names.add(variable);
			Object[] list;
			if( split == null ){
				if( param instanceof Collection ){
					Collection collection = (Collection)param;
					list = collection.toArray(new Object[collection.size()]);
				}else{
					addError("Bad Repeat", "No Splitter class defined for tag "+splitter);
					return result;
				}
			}else{
				list = split.split(param);
			}



			if( list == null){
				// explicit request to do no expansion
				return result;
			}
			for( Object dat : list){
				try{
					params.put(variable,dat);
					result.appendChild(expand(doc,element.getChildNodes()));
				}catch(Exception e){
					addError("Bad Repeat", "Exception in repeat expansion", e);
				}
			}
		}catch(Exception e){
			addError("Bad Repeat", "Exception in split", e);
		}finally{
			if( parameter_names != null){
				parameter_names.remove(variable);
			}
			variable_names.remove(variable);
		}
		return result;
	}
	/** Do repeated expansion of a document fragment setting a parameter to a set of objects
	 * generated by a filter. 
	 * @param node
	 * @return DocumentFragment
	
	 */
	public DocumentFragment For(Node node) {
		Element element = (Element)node;
		Document doc=getDocument();
		DocumentFragment result = doc.createDocumentFragment();

		if( element == null ){
			addError("Malformed For", "Missing filter and content");
			return result;
		}
		
		String variable = this.getAttribute(VAR_ATTR, element);
		if( variable == null || variable.trim().length() == 0){
			addError("Bad Repeat", "No variable specified");
			return result;
		}
		if( parameter_names != null && parameter_names.contains(variable)){
			addError("Bad Variable","Overiding the existing parameter named "+variable);
			return result;
		}
		if( parameter_names != null){
			parameter_names.add(variable);
		}
		
		String source = this.getAttribute(SOURCE_ATTR, element);
		PropertyTargetGenerator split = ExpressionCast.makePropertyTargetGenerator(conn, source);
		if( split == null ){
			addError("Bad For", "No Factory class defined for tag "+source);
			return result;
		}
		try{
			variable_names.add(variable);
		PropertyFinder finder = split.getFinder();
		AndRecordSelector sel = new AndRecordSelector();
		NodeList list = element.getChildNodes();
		

		for(int j=0;j<list.getLength();j++){
			Node c = list.item(j);
			if( c.getNodeType() == Node.ELEMENT_NODE && c.getNamespaceURI().equals(FilterExtension.FILTER_LOC)){
				RecordSelector s  = getRecordSelectElement(finder, (Element)c); 
				if( s != null ){
					sel.add(s);
				}
			}
		}	 
		try(CloseableIterator it=split.getIterator(sel)){
			while(it.hasNext()){

				Object o = it.next();
				try{
					params.put(variable, o);
					NodeList content_list = element.getChildNodes();
					for(int j=0; j<content_list.getLength();j++){
						// expand all child content elements (schema only expects one)
						Node item = content_list.item(j);
						if( item.getNodeType() == Node.ELEMENT_NODE && 
								((Element)item).getNamespaceURI().equals(element.getNamespaceURI())  &&
								((Element)item).getLocalName().equals(CONTENT_ELEM)) {
							result.appendChild(expand(doc,item.getChildNodes()));
						}
					}
				}catch(Exception e){
					addError("For error", "Error expanding For content", e);
				}
			}	
		}
		}catch(Exception e){
			addError("For error","Error generating expansion set",e);
		}finally{
			if( parameter_names != null){
				parameter_names.remove(variable);
			}
			variable_names.remove(variable);
		}
		return result;
		
	}
	private Node expand(Document doc,NodeList childNodes) throws ReportException {
		DocumentFragment result = doc.createDocumentFragment();
		for(int i=0;i<childNodes.getLength();i++){
			Node n = expandNode(doc, childNodes.item(i));
			if( n != null ){
				result.appendChild(n);
			}
		}
		return result;
	}
	private Node expandNode(Document doc, Node item) throws ReportException {
		if( item.getNodeType() == Node.ELEMENT_NODE){
			return expandElement(doc, (Element) item);
		}
		return doc.importNode(item, true);
	}
	private Node expandElement(Document doc, Element item) throws ReportException {
		String s = makeString(item);
		if(item.getNamespaceURI().equals(PARAMETER_LOC)){
			if( item.getLocalName().equals(PARAMETER_ELEM)){
				return  parameter(item);
			}else if( item.getLocalName().equals(REPEAT_ELEMENT)){
				return repeat(item);
			}else if( item.getLocalName().equals(FOR_ELEMENT)){
				return For(item);
			}else if( item.getLocalName().equals(FORMAT_PARAMETER_ELEMENT)){
				String name=item.getAttribute(NAME_ATTR);
				return formatParameter(name, item.getChildNodes());
			}else if( item.getLocalName().equals(PARAMETER_REF_ELEMENT) ||
					item.getLocalName().equals("IfSet") ||
					item.getLocalName().equals("IfNotSet") ||
					item.getLocalName().equals("Content") ||
					item.getLocalName().equals("Fallback")) {
				// elements that are legal provided the name parameter is not a variable.
				String name = item.getAttribute(NAME_ATTR);
				if( variable_names.contains(name)) {
					addError("Illegal expansion","Parameter "+name+" is a loop variable and cannot be accessed using "+item.getLocalName());
				}
				// Import the node we will do additional XSL template 
				// expansion on the result to resolve.
				// expand child nodes as IfSet/Set might reference variables
				// or additional loops
				Node child = doc.importNode(item, false);
				Node frag = expand(doc,item.getChildNodes());
				
				child.appendChild(frag);
				return child;
			}else{
				addError("Illegal expansion", item.getLocalName()+ " not allowed in loop expansion");
			}
		}else{
			Node child = doc.importNode(item, false);
			Node frag = expand(doc,item.getChildNodes());
			child.appendChild(frag);
			return child;
		}
		return null;
	}
	@Override
	protected  String getText(Element e) throws ReportException{
		// Filters etc may want to use pre-defined parameters
		// like current authenticated user.
		if( e.getElementsByTagNameNS(PARAMETER_LOC, PARAMETER_ELEM).getLength()>0){
			
				final String text = super.getText((Element)expandElement(getDocument(), e));
				log.debug("Text expanded to "+text);
				return text;
		}else{
			return super.getText(e);
		}
	}
	@Override
	public boolean checkNode(Element e) throws TemplateValidateException {
		final String ns = e.getNamespaceURI();
		if( ns == null || ! ns.equals(PARAMETER_LOC)){
			return false;
		}
		final String localName = e.getLocalName();
		if( localName.equals(PARAMETER_DEF_ELEMENT)){
			// Check the syntax of a parameter definition.
			String name = getAttribute(NAME_ATTR, e);
			String value = getAttribute(VALUE_ATTR, e);
			try {
				Input i = getInput(name, value, e);
				if( i == null ){
					throw new TemplateValidateException("Bad Input specification");
				}
				setValue(name, value, i,e);
				configureInput(e, i);
			} catch (Exception e1) {
				throw new TemplateValidateException("Bad parameter specification", e1);
			}
			return true;
		}else if( localName.equals(PARAMETER_ELEM)){
			// really need run-time type to check format
			// Could add target type to input but hard to 
			// guarantee sufficiently specified to avoid false +ve
			return true;
		}else if( localName.equals(REPEAT_ELEMENT)){
			String splitter = this.getAttribute(SPLIT_ATTR, e);
			Splitter split = getContext().makeObjectWithDefault(Splitter.class, null, SPLITTER_PREFIX, splitter);
			if( split == null ){
				throw new TemplateValidateException("Undefined splitter "+splitter);
			}
			NodeList list = e.getElementsByTagNameNS(PARAMETER_LOC, "*");
			for( int i=0;i<list.getLength();i++){
				if(!((Element)list.item(i)).getLocalName().equals(PARAMETER_ELEM)){
					throw new TemplateValidateException("Only Parameter elements from "+PARAMETER_LOC+" allowed in repeat");
				}
			}
			// don;t abort checks.
			return false;
		}
		// other parameter elements no checks.
		return false;
	}
	public static ContentBuilder getDocumentation(AppContext c,ContentBuilder cb){
		ClassTableCreator creator = new ClassTableCreator(c);
		cb.addHeading(2, "Parameter formats");
		cb.addText("Parameter elements can be formatted in the following ways");
		cb.addHeading(3, "Expressions");
		cb.addText("A format starting with "+EXPRESSION_PREFIX+" is a property expression that should be expanded on the parameter value, this only works if the parameter value is an expression-target");
		cb.addHeading(3, "Value formatters");
		cb.addText("Value formatters generate text");
		cb.addTable(c, creator.getList(ValueFormatter.class, FORMATTER_PREFIX));
		cb.addHeading(3, "Dom formatters");
		cb.addText("DOM formatters may generate xml as well as text");
		cb.addTable(c, creator.getList(DomFormatter.class, FORMATTER_PREFIX));
		cb.addHeading(3, "Value parsers");
		cb.addText("Any of the value parser names can also be used to format a parameter value (in a format compatible with that consumed by the parser). However if you are formatting a parameter to be parsed by a later stage of " +
				"the report generation it might be easier to use a ParameterRef element");
		cb.addTable(c, c.getService(ValueParserService.class).getDocumentationTable());
		return cb;
	}
}