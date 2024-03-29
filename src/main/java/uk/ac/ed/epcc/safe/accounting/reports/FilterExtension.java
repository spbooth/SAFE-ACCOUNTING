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

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

import uk.ac.ed.epcc.safe.accounting.AccountingService;
import uk.ac.ed.epcc.safe.accounting.reports.exceptions.ReportException;
import uk.ac.ed.epcc.safe.accounting.reports.exceptions.UnexpandedContentException;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.webapp.AppContext;



/**
 * This is a class used to build RecordSelectors via XSLT extension.
 * The stylesheet selects the appropriate elements which is then parsed by the 
 * extension. 
 * 
 * @author spb
 * 
 */


public class FilterExtension extends ReportExtension{
	public FilterExtension(AppContext conn) throws ParserConfigurationException{
		super(conn,null);
	}


	public FilterExtension( AppContext conn,ReportType type) throws ParserConfigurationException{
		super(conn,type);
	}
	@Override
	public boolean checkNode(Element e) throws TemplateValidateException {
		if( FILTER_LOC.equals(e.getNamespaceURI())){
			if( e.getLocalName().equals(PRODUCER_ELEMENT)){
				try{
					String name = getText(e);
					AccountingService serv = getContext().getService(AccountingService.class);
					if( name == null || name.trim().length() == 0){
						serv.getUsageProducer();
					}else{
						serv.getUsageProducer(name.trim());
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
	/** Make a RecordSet based on all the Filter clauses in scope.
	 * 
	 * @param filters
	 * @return RecordSet
	 */
	public RecordSet makeFilter(Object filters) {
		return makeFilter(null, filters);
	}
	/** Extend an existing {@link RecordSet} 
	 * 
	 * @param prev     current {@link RecordSet}
	 * @param filters   filter nodes to add
	 * @return  updated {@link RecordSet}
	 */
	public RecordSet makeFilter(RecordSet prev, Object filters) {
		if( prev == null ){
			prev = makeSelector();
			//prev.setProducerTagFromCurrent();
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
					prev = addFilterNode(prev,n);
				}else if(filters instanceof NodeIterator){
					NodeIterator list = (NodeIterator) filters;
					for(Node n=list.nextNode(); n != null; n=list.nextNode()){
						prev=addFilterNode(prev, n);
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
	/** Generate a {@link RecordSet} representing 
	 * just the specified additions
	 * @param prev     {@link RecordSet} for 
	 * @param filters   filter nodes to parse
	 * @return  fragment {@link RecordSet}
	 */
	public RecordSet makeFilterFragment(RecordSet parent, Object filters) {

		RecordSet  result = makeSelector();
		if( parent != null ) {
			result.setUsageProducer(parent.getGenerator());
		}

		try{

			if( filters == null ){
				return result;
			}

			if( filters instanceof String){
				String fil = (String) filters;
				if( fil.trim().length()==0){
					return result;
				}
				addError("Bad Filter Specification", "Non empty string "+fil);
				// Don't match anything on error.
				result.addRecordSelector(new SelectClause());
				result.setError(true);
				return result;
			}
			try{
				if( filters instanceof Node){
					Node n = (Node) filters;
					result = addFilterNode(result,n);
				}else if(filters instanceof NodeIterator){
					NodeIterator list = (NodeIterator) filters;
					for(Node n=list.nextNode(); n != null; n=list.nextNode()){
						result=addFilterNode(result, n);
					}
				}else{
					addError("Bad Filter Specification","unexpected type "+filters.getClass().getCanonicalName());
				}

			}catch(Exception t){
				getLogger().error("Error parsing filter clause",t);
				addError("Filter parse error", t.getMessage());
				result.addRecordSelector(new SelectClause());
				result.setError(true);
			}
			return result;
		}catch(Exception t){
			getLogger().error("Error parsing filter clause",t);
			addError("Filter parse error", t.getMessage());
			return null;
		}
	}
	public ObjectSet makeObjectSet(Object filters) throws ReportException {
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
}