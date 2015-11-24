// Copyright - The University of Edinburgh 2011
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
@uk.ac.ed.epcc.webapp.Version("$Id: ParsernameSpaceContext.java,v 1.7 2014/09/15 14:32:24 spb Exp $")

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