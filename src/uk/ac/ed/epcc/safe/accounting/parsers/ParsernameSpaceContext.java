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
/**
 * 
 */
package uk.ac.ed.epcc.safe.accounting.parsers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;


/** A simple run-time configurable {@link NamespaceContext} based on a map.
 * 
 * @author spb
 *
 */
public class ParsernameSpaceContext implements NamespaceContext,Contexed{
    private final AppContext ctx;
	private final Map<String,String> namespaces;
	private final Logger log;
	public ParsernameSpaceContext(AppContext conn){
		this.ctx=conn;
		namespaces = new HashMap<String,String>();
		namespaces.put(XMLConstants.DEFAULT_NS_PREFIX, XMLConstants.NULL_NS_URI);
		namespaces.put(XMLConstants.XML_NS_PREFIX,XMLConstants.XML_NS_URI);
		namespaces.put(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
		log = conn.getService(LoggerService.class).getLogger(getClass());
	}
	public String getNamespaceURI(String arg0) {
		if( arg0 == null){
			throw new IllegalArgumentException("Null prefix");
		}
		log.debug("lookup prefix "+arg0);
		
		String res = namespaces.get(arg0);
		if( res == null ){
			res = XMLConstants.NULL_NS_URI;
		}
		log.debug(" returns "+res);
		return res;
	}

	public String getPrefix(String arg0) {
		if( arg0 == null){
			throw new IllegalArgumentException("Null namespace");
		}
		log.debug("lookup uri "+arg0);
		if( namespaces.containsValue(arg0)){
			for(String prefix : namespaces.keySet()){
				if( arg0.equals(namespaces.get(prefix))){
					return prefix;
				}
			}
		}
		return null;
	}

	public Iterator getPrefixes(String arg0) {
		if( arg0 == null){
			throw new IllegalArgumentException("Null namespace");
		}
		HashSet<String> keys = new HashSet<String>();
		for( String key : namespaces.values()){
			if( arg0.equals(namespaces.get(key))){
				keys.add(key);
			}
		}
		return keys.iterator();
	}
	public void addNamespace(String prefix, String namespace){
		namespaces.put(prefix, namespace);
	}
	public AppContext getContext() {
		return ctx;
	}
	
}