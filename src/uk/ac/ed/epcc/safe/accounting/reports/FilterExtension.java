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

import java.text.NumberFormat;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;

import uk.ac.ed.epcc.safe.accounting.AccountingService;
import uk.ac.ed.epcc.safe.accounting.ExpressionTargetGenerator;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionCast;
import uk.ac.ed.epcc.safe.accounting.reports.exceptions.ReportException;
import uk.ac.ed.epcc.safe.accounting.reports.exceptions.UnexpandedContentException;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.time.Period;


  
/**
 * This is a class used to build RecordSelectors via XSLT extension.
 * The stylesheet selects the appropriate elements which is then parsed by the 
 * extension. 
 * 
 * @author spb
 * 
 */


public class FilterExtension extends ReportExtension{

	private static final String TARGET_ELEMENT = "Target";
	
	public FilterExtension(AppContext conn) throws ParserConfigurationException{
		  super(conn,null);
	  }
	  
	
  public FilterExtension( AppContext conn,NumberFormat nf) throws ParserConfigurationException{
	  super(conn,nf);
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
  /** Make a RecordSet based on all the Filter clauses in scope.
   * 
   * @param filters
   * @return RecordSet
   */
  public RecordSet makeFilter(Object filters){
	  return makeFilter(null, filters);
  }  
  public RecordSet makeFilter(RecordSet prev,Object filters){
	  if( prev == null ){
		  prev = makeSelector();
	  }
	  try{
		  
		  if( filters == null ){
			  return prev;
		  }
    
		  if( filters instanceof String){
			  String fil = (String) filters;
			  if( fil.trim().length()==0){
				  return prev;
			  }
			  addError("Bad Filter Specification", "Non empty string "+fil);
			  // Don't match anything on error.
			  prev.addRecordSelector(new SelectClause());
			  prev.setError(true);
			  return prev;
		  }
		  try{
			  if( filters instanceof Node){
				  Node n = (Node) filters;
				  prev = addNode(prev,n);
			  }else if(filters instanceof NodeIterator){
				  NodeIterator list = (NodeIterator) filters;
				  for(Node n=list.nextNode(); n != null; n=list.nextNode()){
					  prev=addNode(prev, n);
				  }
			  }else{
				  addError("Bad Filter Specification","unexpected type "+filters.getClass().getCanonicalName());
			  }

		  }catch(Exception t){
			  getLogger().error("Error parsing filter clause",t);
			  addError("Filter parse error", t.getMessage());
			  prev.addRecordSelector(new SelectClause());
			  prev.setError(true);
		  }
		  return prev;
	  }catch(Exception t){
		  getLogger().error("Error parsing filter clause",t);
		  addError("Filter parse error", t.getMessage());
		  return null;
	  }
  }
  public ObjectSet makeObjectSet(Object filters) throws ReportException{
	  try{
		  if( filters instanceof Element){
			  return makeObjectSet((Element)filters);
		  }else if(filters instanceof NodeIterator){
			  NodeIterator list = (NodeIterator) filters;
			  for(Node n=list.nextNode(); n != null; n=list.nextNode()){
				  if( n  instanceof Element){
					  return makeObjectSet((Element)n);
				  }
			  }
		  }else{
			  addError("Bad ObjectSet Specification","unexpected type "+filters.getClass().getCanonicalName());
		  }

	  }catch(Exception t){
		  getLogger().error("Error parsing filter clause",t);
		  addError("ObjectSet parse error", t.getMessage());
	  }
	  return null;
  }
	
  public ObjectSet makeObjectSet(Element elem) throws ReportException{

	  
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
  /** recursively walk the tree adding all Filter elements
   * 
   * @param up
   * @param n
   * @return
   */
  protected RecordSet addNode(RecordSet up, Node n) {
	if( n.getNodeType()==Node.ELEMENT_NODE && n.getLocalName().equals(FILTER_ELEMENT)){
		return addFilterElement(up, (Element)n);
	}else{
		NodeList list =n.getChildNodes();
		for(int i=0;i<list.getLength();i++){
			Node c = list.item(i);
			up = addNode(up,c);
		}
		assert(up != null);
		return up;
	}
	
}
  public boolean hasRecords(Period period,RecordSet set){
	  if(set.hasError()) {
		  return false;
	  }
	  UsageProducer<?> producer = set.getUsageProducer();
		AndRecordSelector selector = set.getPeriodSelector(period);
		try {
			return producer.getRecordCount(selector) > 0;
		} catch (Exception e) {
			addError("Filter Error", "Error checking for records", e);
			return false;
		}
  }

@Override
public boolean checkNode(Element e) throws TemplateValidateException {
	if( FILTER_LOC.equals(e.getNamespaceURI())){
		if( e.getLocalName().equals(PRODUCER_ELEMENT)){
			try{
				String name = getText(e);
				AccountingService serv = getContext().getService(AccountingService.class);
				if( name == null || name.trim().length() == 0){
					serv.getUsageManager();
				}else{
					serv.getUsageManager(name.trim());
				}
			}catch(UnexpandedContentException e1){
				return false;
			} catch (ReportException e2) {
				throw new TemplateValidateException("Bad Producer clause", e2);
			}
		}
	}
	return false;
}

}