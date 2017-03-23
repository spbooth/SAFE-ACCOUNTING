//| Copyright - The University of Edinburgh 2015                            |
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
package uk.ac.ed.epcc.safe.accounting.reports;

import java.util.Date;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetGenerator;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.expr.Parser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParserPolicy;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTargetFactory;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.reports.exceptions.IllegalContentException;
import uk.ac.ed.epcc.safe.accounting.reports.exceptions.ReportException;
import uk.ac.ed.epcc.safe.accounting.reports.exceptions.UnexpandedContentException;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.NullSelector;
import uk.ac.ed.epcc.safe.accounting.selector.OrRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.OrderClause;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.RelationClause;
import uk.ac.ed.epcc.safe.accounting.selector.RelationshipClause;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;

/** Builds {@link RecordSelector} objects from a XML specification.
 * 
 * @author spb
 *
 */
public abstract class SelectBuilder {

	public static final String FILTER_CONTAINER_ELEMENT = "Filter";
	public static final String FORMAT_ATTR = "format";
	public static final String DESCENDING_ATTR = "descending";
	public static final String FILTER_ORDER_BY_ELEMENT = "OrderBy";
	public static final String RELATIONSHIP_ELEMENT = "Relationship";
	public static final String FILTER_PRODUCER_ELEMENT = "Producer";
	public static final String FILTER_AND_ELEMENT = "And";
	public static final String FILTER_OR_ELEMENT = "Or";
	public static final String FILTER_NULL_ELEMENT = "Null";
	public static final String FILTER_NOT_NULL_ELEMENT = "NotNull";
	public static final String FILTER_EQ_ELEMENT = "EQ";
	public static final String PROPERTY_ELEMENT = "Property";
	public static final String SECOND_PROPERTY_ELEMENT = "Property2";
	public static final String VALUE_ELEMENT = "Value";
	public static final String TIME_BOUNDS_ELEMENT = "TimeBounds";
	protected final AppContext conn;
	// This encodes the rules for parsing/formatting expressions/objects
	protected ValueParserPolicy parse_vis;
	static final String ALL_TIMES_ELEMENT = "AllTimes";
	public SelectBuilder(AppContext conn) {
		this.conn=conn;
		parse_vis = new ValueParserPolicy(conn);
	}

	public final AppContext getContext() {
		return conn;
	}
	
	public void setPolicy(ValueParserPolicy pol){
		parse_vis = pol;
	}
	
	protected abstract void addError(String type, String details, Node n);
	protected abstract void addError(String type, String details, Throwable e);
	protected abstract String debug(String messg);
	protected final String getAttribute(String name, Element element){
		  if( element == null ){
			  return null;
		  }
		  String attribute = element.getAttribute(name);
		  debug("lookup attribute ["+name+"] in "+element.getNodeName()+", found "+attribute);
		  return attribute;
	  }
	
	/** Parse a {@link PropExpression} in a nested element
	 * this can be overridden so the value can also be provided as a ParameterRef element
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
				
					final String text = getText(v);
					
					if( text == null || text.trim().length()==0){
						return null;
					}
					final String format = getAttribute(FORMAT_ATTR, v);
					return parse(expr,format,text);
				
			}
	
	
	@SuppressWarnings("unchecked")
	public <T> T parse(PropExpression<T> tag,String fmt, String value) throws Exception {
		if (tag == null) {
			return null;
		}
		ValueParser vp = getValueParser(fmt,tag);
		value=value.trim();
	    return (T) vp.parse(value);
		
	}

	public PropExpression getExpression(PropertyFinder finder, String name) {
		if(name == null || name.trim().length()==0){
			addError("Cannot parse Expression", "Expression is empty",new Exception());
			return null;
		}
		Parser p = new Parser(getContext(),finder);
		try {
			return p.parse(name);
		} catch (uk.ac.ed.epcc.safe.accounting.expr.ParseException e) {
			addError("Cannot parse Expression", "Expression "+name+" Not parsed", e);
			return null;
		} catch (InvalidPropertyException e) {
			addError("Cannot parse Expression", "Expression "+name+" Not parsed", e);
			return null;
		}
		
	}
	@SuppressWarnings("unchecked")
	protected <T> ValueParser<T> getValueParser(String fmt,PropExpression<T> tag)
			throws Exception {
		parse_vis.setFormat(fmt);
		ValueParser result = tag.accept(parse_vis);
		parse_vis.setFormat(null);
		return result;
	}
	
	protected <T> ValueParser<T> getValueParser(Class<? extends T> clazz)
	throws Exception {
		return parse_vis.getValueParser(clazz);
	}
	/**
	 *   Generate a RecordSelect element.
	 *   
	   * 
	   * @param up PropertyFinder
	   * @param e  Element
	   * @return RecordSelector or null
	 * @throws Exception  
	   */
	@SuppressWarnings("unchecked")
	protected RecordSelector getRecordSelectElement(PropertyFinder up, Element e)
			throws Exception {
				  String name=e.getLocalName();
				  // The style-sheets usually don't select a Filter element only its content but
				  // for consistency we can treat a Filter as an AND element. 
				  // helpdesk config code does parse Filter directly
				  if( name.equals(FILTER_AND_ELEMENT) || name.equals(FILTER_CONTAINER_ELEMENT)){
					  AndRecordSelector and = new AndRecordSelector();					
					  NodeList list = e.getChildNodes();
					  for(int i=0;i<list.getLength();i++){
							Node c = list.item(i);
							if( c.getNodeType() == Node.ELEMENT_NODE && c.getNamespaceURI() == e.getNamespaceURI()){
								RecordSelector child = getRecordSelectElement(up, (Element)c);
								if( child != null ){
									and.add(child);
								}
							}
					  }
					  and.lock();
					  return and;
				  }else if( name.equals(FILTER_OR_ELEMENT)){
					  OrRecordSelector or = new OrRecordSelector();
					  NodeList list = e.getChildNodes();
					  for(int i=0;i<list.getLength();i++){
							Node c = list.item(i);
							if( c.getNodeType() == Node.ELEMENT_NODE && c.getNamespaceURI() == e.getNamespaceURI()){
								RecordSelector child = getRecordSelectElement(up, (Element)c);
								if( child != null ){
									or.add(child);
								}
							}
					  }
					  or.lock();
					  return or;
				  }else if( name.equals(FILTER_NULL_ELEMENT)){
					  PropExpression expr=getExpression(up, getParam(PROPERTY_ELEMENT, e));
					  if(expr == null ){
						  throw new FilterParseException("Undefined expression "+getParam(PROPERTY_ELEMENT, e));
					  }
					  return new NullSelector(expr, true);
				  }else if( name.equals(FILTER_NOT_NULL_ELEMENT)){
					  PropExpression expr=getExpression(up, getParam(PROPERTY_ELEMENT, e));
					  if(expr == null ){
						  throw new FilterParseException("Undefined expression "+getParam(PROPERTY_ELEMENT, e));
					  }
					  return new NullSelector(expr, false);
				  }else if( name.equals(FILTER_ORDER_BY_ELEMENT)){
					  boolean descending=false;
					  PropExpression expr=getExpression(up, getParam(PROPERTY_ELEMENT, e));
					  if( e.hasAttribute(DESCENDING_ATTR)){
						  descending= Boolean.parseBoolean(e.getAttribute(DESCENDING_ATTR));
					  }
					  return new OrderClause(descending,expr);
				  }else if( name.equals(RELATIONSHIP_ELEMENT)){
					  return new RelationshipClause(normalise(getText(e)));
				  }else{
					  MatchCondition cond;
					  if(name.equals(FILTER_EQ_ELEMENT)){
						  cond=null;
					  }else{
						  try{
							  cond= MatchCondition.valueOf(e.getLocalName());
						  }catch(Throwable t){
							  cond=null;
						  }
						  if(cond == null ){
							  throw new FilterParseException("Unexpected comparison "+e.getLocalName());
							 
						  }
					  }
					  PropExpression expr=getExpression(up, getParam(PROPERTY_ELEMENT, e));
					  if(expr == null ){
						  throw new FilterParseException("Undefined expression "+getParam(PROPERTY_ELEMENT, e));
					  }
					  return getSelectClause(up,expr, cond, e);
				  }
			  }
	protected final boolean hasChild(String name,Element elem){
		if( elem == null ){
			  return false;
		  }
		  NodeList list = elem.getChildNodes();
		  for(int i=0; i<list.getLength();i++){
			  Node n = list.item(i);
			  String namespace = n.getNamespaceURI();
			  if( n.getNodeType() == Node.ELEMENT_NODE && n.getLocalName().equals(name) && (namespace == null || namespace.equals(elem.getNamespaceURI()))){
				return true;
			  }
		  }
		  return false;
	}
	public class DateBounds{
		public DateBounds(PropExpression<Date>[] bounds, boolean overlap) {
			super();
			this.bounds = bounds;
			this.overlap = overlap;
		}
		public final PropExpression<Date>[] bounds;
		public final boolean overlap;
	}
	public final DateBounds getDateProperties(ObjectSet recordSet,Node datePropertyNode) {		
		
		ExpressionTargetGenerator producer = recordSet.getGenerator();
		if (datePropertyNode != null) {
			if( hasChild(ALL_TIMES_ELEMENT,(Element)datePropertyNode)){
				return new DateBounds(new PropertyTag[0],false);
			}
			PropExpression property = null;
			try{
				
			String param = getParam(PROPERTY_ELEMENT,(Element) datePropertyNode);
			if( param != null ){
				// only look if the element exists 
				// to supress error reporting 
				property = getExpression(producer.getFinder(), param); 
			}
			
			if (property != null && Date.class.isAssignableFrom(property.getTarget())) {
				return new DateBounds(new PropExpression[]{property},false);
				
			} else {
				// check for overlap properties.
				String start = getParam("StartProperty", (Element) datePropertyNode);
				String end = getParam("EndProperty", (Element) datePropertyNode);
				PropExpression<Date> startProperty = getExpression(producer.getFinder(), start); 
				PropExpression<Date> endProperty = getExpression(producer.getFinder(), end);
				boolean overlap = ! hasChild("NoOverlap", (Element) datePropertyNode);
				if (startProperty != null && Date.class.isAssignableFrom(startProperty.getTarget()) &&
					endProperty != null && Date.class.isAssignableFrom(endProperty.getTarget())) {
	
					return new DateBounds(new PropExpression[]{
							startProperty,
							endProperty},overlap);
				}			
			}
			}catch(Exception e){
				addError("Bad Property", "Error reading property",e);
				return null;
			}
		}
		if( recordSet.getGenerator().compatible(StandardProperties.ENDED_PROP)){
			// default to ended  if supported.
			return new DateBounds(new PropExpression[]{StandardProperties.ENDED_PROP},false);	
		}
		return new DateBounds(new PropExpression[0],false);
				
	}
	protected <T> RecordSelector getSelectClause(PropertyFinder up,PropExpression<T> expr, MatchCondition cond, Element e)
			throws Exception {
				T value = getParamExpressionValue(expr, VALUE_ELEMENT, e);
				if( value == null ){
					// look for second expression
					if( ! hasChild(VALUE_ELEMENT, e)){
						String string = getParam(SECOND_PROPERTY_ELEMENT,e);
						if( string != null ){
							PropExpression<T> expr2=getExpression(up, string);
							if( expr2 != null ){
								return new RelationClause<T>(expr, cond,expr2);
							}
						}
						throw new FilterParseException("No value or second property in select clause");
					}
					// assume optional form input if no value
					return null;
				}
				return new SelectClause<T>(expr,cond,value);
			}

	/** Get the text body of the Element
	 * The Element is assumed to be a leaf node with only text content.
	 * 
	 * The ParameterExtension overrides this to do on-the-fly parameter expansion
	 * for example standard pre-existing parameters like the current user.
	 * 
	 * @param e Element
	 * @return String
	 */
	protected String getText(Element e) throws ReportException {
		StringBuilder result = new StringBuilder();
		NodeList list = e.getChildNodes();
		for(int i=0; i< list.getLength(); i++){
			Node n = list.item(i);
			int type = n.getNodeType();
			switch( type ){
				case Node.TEXT_NODE: result.append(n.getNodeValue()); break;
				case Node.COMMENT_NODE: break;
				case Node.ATTRIBUTE_NODE: break;
				case Node.ELEMENT_NODE:
					Element c = (Element)n;
					if(ParameterExtension.PARAMETER_LOC.equals(c.getNamespaceURI()) && c.getLocalName().equals(ParameterExtension.PARAMETER_ELEM)){
						throw new UnexpandedContentException();
					}
				default: throw new IllegalContentException();
			}
		}
		return result.toString();
	}

	/** Get a parameter contained in a child Element.
	 * The Child Element must be in the same name-space as the target element
	 * 
	 * @param name Parameter Element name
	 * @param elem Target Element
	 * @return String
	 * @throws Exception 
	 */
	protected final String getParam(String name, Element elem)
			throws ReportException {
				return getParamNS(elem.getNamespaceURI(), name, elem);
			  }

	protected final String getParamNSWithDefault(String namespace, String name,
			String def, Element elem) throws Exception {
				String s = getParamNS(namespace,name,elem);
				if( s == null || s.trim().length() == 0){
					return def;
				}
				return s;
			}

	protected final String getParamNS(String namespace, String name, Element elem)
			throws ReportException {
				if( elem == null){
					return null;
				}
				Element v = getParamElementNS(namespace,name, elem);
				if( v != null ){
					String result = normalise(getText(v));
					debug("lookup ["+name+"] in "+elem.getNodeName()+", found ["+result+"]");
					return result;
				}
				debug("lookup ["+name+"] in "+elem.getNodeName()+", no element found");
				return null;  
			}
	/** Map whitespace to normalised form. This is important for configuration elements if
	 * we want to be able to reformat the XML without breaking things
	 * 
	 * @param value
	 * @return
	 */
	protected final String normalise(String value){
		if( value == null ){
			return null;
		}
		return value.trim().replaceAll("\\s+", " ");
	}

	protected final Element getParamElement(String name, Element e) {
		return getParamElementNS(e.getNamespaceURI(), name, e);
	}

	/** get a child element specified by namespace and tag.
	 * 
	 * @param target_namespace
	 * @param name
	 * @param elem
	 * @return
	 */
	protected final Element getParamElementNS(String target_namespace, String name,
			Element elem) {
				  if( elem == null ){
					  return null;
				  }
				  Element result=null;
				  NodeList list = elem.getChildNodes();
				  for(int i=0; i<list.getLength();i++){
					  Node n = list.item(i);
					  String namespace = n.getNamespaceURI();
					  if( n.getNodeType() == Node.ELEMENT_NODE && n.getLocalName().equals(name) && (namespace == null || namespace.equals(target_namespace))){
						if( result == null){
							result=((Element)n);
						}else{
							addError("Duplicate parameter", "Multiple child nodes of type "+name,elem);
						}
					  }
				  }
				 
				  return result;
			  }

	protected final String getParamWithDefault(String name, String def,
			Element elem) throws Exception {
				String s = getParam(name,elem);
				if( s == null || s.trim().length() == 0){
					return def;
				}
				return s;
			}

}