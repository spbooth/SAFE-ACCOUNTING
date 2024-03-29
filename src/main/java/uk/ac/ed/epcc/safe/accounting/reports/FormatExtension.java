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

import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;

import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.PeriodOverlapRecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.model.data.CloseableIterator;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
import uk.ac.ed.epcc.webapp.time.Period;
/** Extension to format a number of selected records using a template NodeList.
 * Test nodes and attribute values are transformed using a {@link MacroExpander}.
 * Sub-trees of the template may be omitted using IfDef elements that test for non-null properties
 * in the current record.
 * 
 * @author spb
 *
 */


public class FormatExtension<T> extends ReportExtension {
    public static final String FORMAT_LOC="http://safe.epcc.ed.ac.uk/format";
	public FormatExtension(AppContext conn, ReportType type)
			throws ParserConfigurationException {
		super(conn, type);
	}
	public Element getFirstElement(NodeList list, String name){
		for(int i=0 ;i< list.getLength();i++){
			Node n = list.item(i);
				if( n != null && n.getNodeType() == Node.ELEMENT_NODE && FORMAT_LOC.equals(n.getNamespaceURI()) && name.equals(n.getLocalName())){
					return  (Element)n;
			}
		}
		return null;
	}

	
	public DocumentFragment format(RecordSet recordSet, Period period,NodeList template){
		Document doc = getDocument();
		try {
		DocumentFragment result = doc.createDocumentFragment();
		UsageProducer<T> prod = recordSet.getUsageProducer();
		if( prod == null ) {
			return result;
		}
		
		ExpressionExpander expander = new ExpressionExpander(getContext(),parse_vis);
		AndRecordSelector sel = recordSet.getPeriodSelector(period);
		
		
		// TODO: Deprecate this in favour of the generic Filter;TimeBounds
		Element boundsElement = getFirstElement(template, "TimeBounds");
		if( boundsElement != null ){
			PropExpression<Date> bounds[]=getDateProperties(recordSet, boundsElement).bounds; 
			sel.add(new PeriodOverlapRecordSelector(period, bounds[0],bounds[1]));
		}
		int start=-1;
		int count=-1;
		Element limit = getFirstElement(template, "Limit");
		if( limit != null ){

			start = getIntParam("Start", -1, limit);

			count = getIntParam("Count", -1, limit);

		}
		try(CloseableIterator<T> it=( start == -1 && count == -1) ? prod.getIterator(sel) : prod.getIterator(sel, start, count)){


			while(it.hasNext()){
				T obj = it.next();
				ExpressionTargetContainer rec = prod.getExpressionTarget(obj);
				expander.setExpressionTarget(rec);
				for(int i=0;i<template.getLength();i++){
					Node n = template.item(i);
					if( n.getNodeType() != Node.ATTRIBUTE_NODE) {
						Node new_n = copyNode(expander,doc, n);
						if( new_n != null){
							result.appendChild(new_n);
						}
					}
				}
				result.appendChild(doc.createTextNode("\n"));
				rec.release();
			}
		}
		//result.normalize();
		return result;
		}catch(Exception e) {
			addError("Bad format section", "", e);
			return doc.createDocumentFragment();
		}
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
 * @param finder
 * @param r
 * @param doc
 * @param e
 * @return copy of Node or null
 */
	@SuppressWarnings("unchecked")
	private Node copyElement(ExpressionExpander expander,Document doc, Element e) {
	
		String namespaceURI = e.getNamespaceURI();
		String nodeName = e.getLocalName();
		// remove nested filter elements so they can be nested within Format
		if( FilterExtension.FILTER_LOC.equals(namespaceURI)){
			return null;
		}
		// remove nested period elements
		if( PeriodExtension.PERIOD_NS.equals(namespaceURI)){
			return null;
		}
		//TODO consider flag to control isTrivial check
		if( FORMAT_LOC.equals(namespaceURI)){
			if( "IfDef".equals(nodeName)){
				String prop = e.getAttribute("required");
				// only process contents if required property not null
				if( prop != null && ! prop.isEmpty()){
					PropExpression<?> t = expander.parse(prop);
					if( t != null ){
						
						if( expander.evaluate(t) != null){
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
					}else{
						addError("missing property", "Property ["+prop+"] not found");
					}
				}else{
					addError("missing attribute","No required attribute in IfDef");
				}
			}else if( "IfNDef".equals(nodeName)){
				String prop = e.getAttribute("required");
				// only process contents if required property is null
				if( prop != null ){
					PropExpression<?> t = expander.parse(prop);
					if( t != null ){
						if( expander.evaluate(t) == null){
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
					}else{
						addError("missing property", "Property "+prop+" not found");
					}
				}else{
					addError("missing attribute","No required attribute in IfDef");
				}
			}else if ( "If".equals(nodeName)){
				String prop = e.getAttribute("property");
				if( prop != null ){
					PropExpression<?> t = expander.parse(prop);
					if( t != null ){
						
						String value = e.getAttribute("value");
						Object record_value = expander.evaluate(t);
						
						if( record_value == null ){
							return null;
						}
						Object target_value=null;
						try {
							target_value = parse(t, null,value);
						} catch (Exception e1) {
							addError("parse error", "Error parsing "+value+" as "+t.toString(), e1);
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
						if ( ! Comparable.class.isAssignableFrom(t.getTarget())){
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
									Node n = copyNode(expander,doc, item);
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
	
	public boolean isTrivial(Object o){
		if( o == null ){
			return true;
		}
		if( o instanceof IndexedReference){
			return ((IndexedReference)o).isNull();
		}
		if( o instanceof Number ){
			return ((Number)o).doubleValue() == 0.0;
		}
		if( o instanceof String ){
			return ((String)o).trim().length() == 0;
		}
		if( o instanceof Date ){
			return ((Date)o).getTime() == 0L;
		}
		return false;
	}
	@Override
	public boolean wantReplace(Element e) {
		return FORMAT_LOC.equals(e.getNamespaceURI());
	}
	@Override
	public Node replace(Element e) {
		String name = e.getLocalName();
		try {
			switch(name) {
			case "Format": return format(addFilterElementSet(makeSelector(), ElementSet.ancestors_self(e).select(new Match(FILTER_LOC,FILTER_ELEMENT))), findPeriodInScope(e), e.getChildNodes());
			}
		} catch (Exception e1) {
			addError("bad Format "+name, "Error formatting section",e1);
		}
		return null;
	}
}