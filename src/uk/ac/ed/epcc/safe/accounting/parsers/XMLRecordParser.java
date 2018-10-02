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
package uk.ac.ed.epcc.safe.accounting.parsers;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.ogf.ur.XMLSplitter;
import uk.ac.ed.epcc.safe.accounting.parsers.value.DomValueParser;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.BatchParser;
import uk.ac.ed.epcc.safe.accounting.xml.XMLErrorHandler;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;

/**
 * Parser for usage records in an XML format. 
 * <p>
 * Properties are extracted from each XML record using an Xpath expression to 
 * identify the fragment to parse and a {@link DomValueParser} to perform the parse.
 * Subclasses can pre-define properties and use annotations to set the xpath and parser
 * values.
 * </p>
 * <p> New properties can be defined by setting: <em>tag</em><b>.prop.</b><em>name</em>=<em>type</em>
 * where type is one of string, date or number.
 * </p>
 * <p>The xpath target is set using <em>tag</em><b>.</b><em>prop-name</em><b>.xpath</b>
 * <p>Namespaces for use in these Xpaths can be defined using <em>tag</em><b>.namespace.</b><em>prefix</em><b>=</b><em>uri</em></p>
 * </p>
 * <p>The {@link DomValueParser} can be set using <em>tag</em><b>.</b><em>prop-name</em><b>.parser</b>
 * though if this property is not set a default parser is generated using the schema type or the
 * target type of the property.</p>
 * 
 * 
 */


public class XMLRecordParser extends BatchParser implements Contexed {

	





	//private static final String SCHEMA_URI = XMLConstants.W3C_XML_SCHEMA_NS_URI;
	

	

	/*
	 * ##########################################################################
	 * INSTANCE VARIABLES
	 * ##########################################################################
	 */

	/**
	 * The context within which this parser operates
	 */
	private final AppContext context;
	private final XMLPropertyDomParser property_parser;
	
	//public static final String UR_MANAGER_BASE = "ogf.ur";

	
    private String splitter_targets[]=new String[]{"UsageRecord","JobUsageRecord","Usage"};
    Logger log;
    ParsernameSpaceContext namespaces;
	
	/**
	 * Constructs a new <code>OGFUsageRecordParser</code>. This parser is cannot
	 * be considered properly constructed until a called to
	 
	 * {@linkplain #initFinder(AppContext, PropertyFinder, String)} is made.
	 * Attempts to use this parser before either of the methods have been called
	 * will result in an <code>IllegalStateException</code> being thrown. The
	 * <code>AppContext</code> is replaced when
	 * {@linkplain #initFinder(AppContext, PropertyFinder,String)} is called.
	 * 
	 * @param context
	 *          The current <code>AppContext</code>
	 */
	public XMLRecordParser(AppContext context) {
		this.context=context;
		log = context.getService(LoggerService.class).getLogger(getClass());
		namespaces=makeNameSpaceContext();
		property_parser = new XMLPropertyDomParser(context, getClass(), namespaces,defaultSchemaName());
	}



	

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ed.epcc.safe.accounting.parsers.BaseParser#endParse()
	 */
	@Override
	public String endParse() {
		StringBuilder sb = new StringBuilder(super.endParse());
		sb.append(property_parser.endParse());
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ed.epcc.webapp.Contexed#getContext()
	 */
	public AppContext getContext() {
		return this.context;
	}
    protected final Logger getLogger(){
    	return log;
    }


	


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.epcc.safe.accounting.BaseParser#getFinder(uk.ac.ed.epcc.webapp
	 * .AppContext, uk.ac.ed.epcc.safe.accounting.PropertyFinder,
	 * java.lang.String)
	 */
	@Override
	public final PropertyFinder initFinder(AppContext context, PropertyFinder prev,
			String mode) {
		
		//Properties prop = new FilteredProperties(this.context.getService(ConfigService.class).getServiceProperties(),
		//		UR_MANAGER_BASE, mode);
		PropertyFinder orig = initFinder(context,prev);
		
		PropertyFinder result = property_parser.initFinder(context, orig, mode);
		
		String splitter_list = context.getInitParameter(mode+".splitter_targets");
		if( splitter_list != null){
			splitter_targets= splitter_list.split("\\s*,\\s*");
		}
		
		String ns_prefix = mode+".namespace.";
		Map<String,String> ns_props=context.getInitParameters(ns_prefix);
		for(String key : ns_props.keySet()){
			namespaces.addNamespace(key.substring(ns_prefix.length()), ns_props.get(key));
		}
		return result;
	}

    /** First stage of the initFinder process.
     * This only generates the PropertyFinder that is used by the normal
     * initFinder call
     * 
     * @param context2
     * @param prev
     * @return
     */
	protected PropertyFinder initFinder(AppContext context2, 
			PropertyFinder prev) {
		
		
		MultiFinder mf = new MultiFinder();
		mf.addFinder(prev);
		mf.addFinder(StandardProperties.time);
		mf.addFinder(StandardProperties.base);
		mf.addFinder(BatchParser.batch);
		
		return mf;
	}


   
	/** Default Schema to use when none set via properties
	 * 
	 * @return
	 */
	protected String defaultSchemaName(){
		return null;
	}
	



	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.epcc.safe.accounting.UsageRecordParser#parse(uk.ac.ed.epcc.safe
	 * .accounting.PropertyMap, java.lang.String)
	 */
	@Override
	public boolean parse(DerivedPropertyMap map, String record)
			throws AccountingParseException {
		try {
		
			log.debug("record is "+record);
			XMLErrorHandler handler = new XMLErrorHandler(getContext());
			DocumentBuilder builder = property_parser.getBuilder();
			builder.setErrorHandler(handler);
			
			Document doc = builder.parse(new InputSource(new StringReader(record)));
			if ( property_parser.parse(map,doc) ){
				postProcess(map);
				return true;
			}
			return false;
			
		} catch (Exception e) {
			if( e instanceof AccountingParseException){
				throw (AccountingParseException)e;
			}
			throw new AccountingParseException(e);
		}
	}

	/** Extension point to allow a parser to post-process the values.
	 * Though this could be performed by a policy this makes it easier to do
	 * post-processing that is specific to the parser
	 * 
	 * @param map
	 */
	protected void postProcess(PropertyMap map){
		
	}

	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.epcc.safe.accounting.BaseParser#splitRecords(java.lang.String)
	 */
	@Override
	public Iterator<String> splitRecords(String records)
			throws AccountingParseException {

		try {
			XMLSplitter handler = getSplitter();
			try {
				XMLReader parser = XMLReaderFactory.createXMLReader();
				parser.setContentHandler(handler);
				parser.parse(new InputSource(new StringReader(records)));
			} catch (SAXException e) {
				throw new AccountingParseException(
						"Problem while separating OGF-UR usage records", e);
			}
			
			return handler.iterator();
		} catch (IOException e) {
			getLogger().error(
					"I/O exception occured while splitting OGF usage records",e);
			throw new AccountingParseException(
					"Problem while separating OGF-UR usage records", e);
		}
	}
	/** This is used for namespace resolution 
	 * In particular for the Xpath expressions used by the parser.
	 * If a sub-class uses name-spaces in the Xpath then 
	 * override this method to add them to the NameSpaceContext
	 * @return
	 */
	protected ParsernameSpaceContext makeNameSpaceContext() {
		return new ParsernameSpaceContext(context);
	}
	protected String[] getTargets(){
		return splitter_targets;
	}
	protected XMLSplitter getSplitter() {
		return new XMLSplitter(getTargets());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.epcc.safe.accounting.parsers.BaseParser#startParse(uk.ac.ed.epcc
	 * .safe.accounting.PropertyContainer)
	 */
	@Override
	public void startParse(PropertyContainer defaults) throws Exception {
		property_parser.startParse(defaults);
	}

  




	




	
	
}