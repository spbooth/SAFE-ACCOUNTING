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

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.TypeInfo;

import uk.ac.ed.epcc.safe.accounting.ErrorSet;
import uk.ac.ed.epcc.safe.accounting.parsers.value.DomParserAdapter;
import uk.ac.ed.epcc.safe.accounting.parsers.value.DomValueParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.DoubleParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.FloatParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.IntegerParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.LongParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.NumberParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.StringParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.XMLDateTimeParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.XMLDurationParser;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.reports.ReportBuilder;
import uk.ac.ed.epcc.safe.accounting.update.AbstractPropertyContainerUpdater;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerDomParser;
import uk.ac.ed.epcc.safe.accounting.xml.LSResolver;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.Feature;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.TextFileOverlay;
import uk.ac.ed.epcc.webapp.model.TextFileOverlay.TextFile;
import uk.ac.ed.epcc.webapp.model.data.Duration;

/**
 * An implementation of a {@link PropertyContainerDomParser} for data in an XML format. 
 * <p>
 * Normally this is included into a target parser by composition. The class of the target parser
 * is passed to the constructor to allow configuration by reflection and annotations.
 * <p>
 * Properties are extracted from each XML record using an Xpath expression to 
 * identify the fragment to parse and a {@link DomValueParser} to perform the parse.
 * Target classes can pre-define properties and use annotations to set the xpath and parser
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


public class XMLPropertyDomParser extends AbstractPropertyContainerUpdater implements Contexed,
		 PropertyContainerDomParser{

	
	public static final String USE_SCHEMA_FEATURE_PREFIX = "use_schema.";




	private static final String SCHEMA_URI = XMLConstants.W3C_XML_SCHEMA_NS_URI;
	

	

	/*
	 * ##########################################################################
	 * INSTANCE VARIABLES
	 * ##########################################################################
	 */

	boolean try_schema_info;
	boolean use_schema_info=false;


	/**
	 * Flag to indicate that the parser is in the process of parsing. This parser
	 * is not thread safe so it is important that parsing isn't started when it's
	 * already going on.
	 */
	private boolean parsing = false;

	/**
	 * Stores a collection of non-fatal errors that will be reported at the end of
	 * parsing
	 */
	private ErrorSet parseErrors;
	private DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
	private DocumentBuilder builder=null;
	
	/** Parse proceeds by extracting the Xpath expressions from each
	 * record and parsing the result using a ContainerEntryMaker
	 * 
	 */
	Map<PropertyTag,XPathExpression> targets;
	Map<PropertyTag,DomValueParser> parsers;
    private final NamespaceContext namespaces;
    private final Class target_class;
    private String default_schema_name;
    protected final Logger log;

	/*
	 * ##########################################################################
	 * PUBLIC METHODS
	 * ##########################################################################
	 */
	
	/** Constructs a new {@link XMLPropertyDomParser}
	 * 
	 * 
	 * @param context  AppContext
	 * @param target   target class to query for properties
	 * @param default_schema_name
	 */
	public XMLPropertyDomParser(AppContext context,Class target,NamespaceContext ns,String default_schema_name ) {
		super(context);
		this.target_class=target;
		this.namespaces=ns;
		this.default_schema_name=default_schema_name;
		log = context.getService(LoggerService.class).getLogger(getClass());
	}



	

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ed.epcc.safe.accounting.parsers.BaseParser#endParse()
	 */
	@Override
	public String endParse() {
		this.parsing = false;
		StringBuilder sb = new StringBuilder(super.endParse());
		sb.append(this.parseErrors);

		return sb.toString();
	}

	



	

@Override
	public final PropertyFinder initFinder(PropertyFinder prev,
			String mode) {
		AppContext context = getContext();
		try_schema_info = context.getBooleanParameter(mode+".use_schema", false);
		
		MultiFinder mf = new MultiFinder();
		mf.addFinder(prev);
		PropertyRegistry reg = new PropertyRegistry(mode+"_extra", "Additional properties defined at run-time");
		mf.addFinder(reg);
		
		String prefix = mode+".prop.";
		Map<String,String> props=context.getInitParameters(prefix);
		for(String key : props.keySet()){
			String name=key.substring(prefix.length());
			String type=props.get(key);
			if( type.equalsIgnoreCase("string")){
			    new PropertyTag<>(reg, name,String.class);
			}else if( type.equalsIgnoreCase("date")){
				new PropertyTag<>(reg, name,Date.class);
			}else if( type.equalsIgnoreCase("number")){
				new PropertyTag<>(reg,name, Number.class);
			}
		}
		// construct the target
		
		targets = new HashMap<>();
		parsers = new HashMap<>();
		
		XPath xpath = XPathFactory.newInstance().newXPath();
	
		
		xpath.setNamespaceContext(namespaces);
		
		if( target_class != null ){
			// Set default targets for field tags.
			for( Field f : target_class.getFields()){
				try{
					if( PropertyTag.class.isAssignableFrom(f.getType()) && f.isAnnotationPresent(Path.class)){
						PropertyTag tag = (PropertyTag) f.get(this);
						String path=null;
						DomValueParser parser = null;
						if( f.isAnnotationPresent(Path.class)){	
							path = f.getAnnotation(Path.class).value();
						}
						if( path != null ){
							XPathExpression exp = xpath.compile(path);
							log.debug("expression for "+tag.getFullName()+" is ["+path+"]");
							targets.put(tag, exp);
						}
						if( f.isAnnotationPresent(ParseClass.class)){
							parser = context.makeObjectWithDefault(DomValueParser.class, f.getAnnotation(ParseClass.class).parser(), mode+"."+tag.getName()+".parser");
							parsers.put(tag,parser);
						}
					}
				}catch(Exception e){
					log.error("Error making targets for property fields "+f.toGenericString(),e);
				}
			}
		}
		// set targets from properties.
		for( PropertyTag t : mf.getProperties()){
			String path = context.getInitParameter(mode+"."+t.getName()+".xpath");
			if( path != null ){
				try{
				XPathExpression exp = xpath.compile(path);
    			targets.put(t, exp);
				}catch( Exception e){
					log.error("Error setting path from property",e);
				}
			}
			DomValueParser parser=context.makeObjectWithDefault(DomValueParser.class, null, mode+"."+t.getName()+".parser");
			if( parser != null){
				parsers.put(t, parser);
			}
		}
		
		
		documentFactory.setNamespaceAware(true);
		// Set schema if defined
		if( Feature.checkDynamicFeature(context,USE_SCHEMA_FEATURE_PREFIX+mode, true)){
		TextFileOverlay schema_overlay = context.makeObject(TextFileOverlay.class, context.getInitParameter("schema.overlay", "schema"));
		if( schema_overlay.isValid()){
			String schema = getSchemaName(context, mode);
			if( schema != null ){
				try{
				TextFile sheet = schema_overlay.find(ReportBuilder.SCHEMA_GROUP, schema);
				if (sheet != null && sheet.hasData()) {
				
				
						SchemaFactory factory = SchemaFactory
						.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
						factory.setResourceResolver(new LSResolver(documentFactory.newDocumentBuilder().getDOMImplementation(),schema_overlay,ReportBuilder.SCHEMA_GROUP,factory.getResourceResolver()));
						documentFactory.setSchema(factory.newSchema(new StreamSource(sheet.getDataReader())));
				}
				}catch(Exception e){
					log.error("Error setting schema",e);
				}
			}
		}
		}
		return mf;
	}

    
	/** Get the schema name to verify the input against. 
	 * If this returns null no schema check is performed.
	 * 
	 * @param context
	 * @param mode
	 * @return
	 */
	private  String getSchemaName(AppContext context, String mode) {
		return context.getInitParameter(mode+".schema",default_schema_name);
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
		if (this.parsing){
			throw new IllegalStateException(
					"Cannot start parsing.  Parsing has already begun");
		}
		
		
		this.parsing = true;
		this.parseErrors = new ErrorSet();
		documentFactory.setNamespaceAware(true);
		builder = documentFactory.newDocumentBuilder();

		if( try_schema_info){
			DOMImplementation imp = builder.getDOMImplementation();
			use_schema_info=imp.hasFeature("Core", "3.0");
		}else{
			use_schema_info=false;
		}
	}

  




	public Set<PropertyTag> defines(Node record) {
		Set<PropertyTag> result = new HashSet<>();
		for(PropertyTag t : targets.keySet()){
			XPathExpression e = targets.get(t);
			try {
				if( e.evaluate(record, XPathConstants.NODE) != null ){
					result.add(t);
				}
			} catch (XPathExpressionException e1) {
				log.error("Error in defines call",e1);
			}
		}
		return result;
	}





	@SuppressWarnings("unchecked")
	public boolean parse(PropertyMap map, Node record)
			throws AccountingParseException {
		
		log.debug("input is "+format(record));
		for(PropertyTag t : targets.keySet()){
			try{
				log.debug("Try to parse "+t.getFullName());
			  XPathExpression exp = targets.get(t);
			 
			  log.debug("match is \""+(String) exp.evaluate(record, XPathConstants.STRING)+"\"");
			  Node n = (Node) exp.evaluate(record, XPathConstants.NODE);
			  if( n != null ){
				  log.debug("node found "+format(n));
				  log.debug("node namespace "+n.getNamespaceURI()+" "+n.getNodeName()+" "+n.getPrefix());
				  DomValueParser p = parsers.get(t);
				  if( p == null ){
					  log.debug("parser is null");
					  p =  getParser(t.getTarget(),n);
					  assert(p==null||t.getTarget().isAssignableFrom(p.getType()));
				  }
				  if( p != null && t.getTarget().isAssignableFrom(p.getType())){
					  log.debug("go for parse target="+t.getTarget().getCanonicalName()+" parser type "+p.getType().getCanonicalName());
					  map.setProperty(t, p.parse(n));
				  }
			  }
			}catch(Exception e){
				throw new AccountingParseException(e);
			}
		}
		return true;
	}
	@SuppressWarnings("unchecked")
	public <T> DomValueParser<? extends T> getParser(TypeInfo info, Node n){
		if( info == null ){
			log.debug("Schema TypeInfo is null");
			return null;
		}
		//TODO probably should use extension or restriction here.
		// probably want a map of schema types to parsers that can be augmented
		// by properties
		log.debug("typeinfo is "+info.getTypeNamespace()+" "+info.getTypeName());
		if( info.isDerivedFrom(SCHEMA_URI, "duration", TypeInfo.DERIVATION_EXTENSION)){
			return new DomParserAdapter<>((ValueParser<T>) new XMLDurationParser());
		}
		
		if( info.isDerivedFrom(SCHEMA_URI, "dateTime", TypeInfo.DERIVATION_EXTENSION)){
			
			return new DomParserAdapter<>((ValueParser<T>) new XMLDateTimeParser());
		}
		
		if( info.isDerivedFrom(SCHEMA_URI, "decimal", TypeInfo.DERIVATION_EXTENSION)){			
			return new DomParserAdapter<>((ValueParser<T>) new NumberParser());
		}
		if( info.isDerivedFrom(SCHEMA_URI, "float", TypeInfo.DERIVATION_EXTENSION)){			
			return new DomParserAdapter<>((ValueParser<T>) new FloatParser());
		}
		if( info.isDerivedFrom(SCHEMA_URI, "double", TypeInfo.DERIVATION_EXTENSION)){			
			return new DomParserAdapter<>((ValueParser<T>) new DoubleParser());
		}
		if( info.isDerivedFrom(SCHEMA_URI, "string", TypeInfo.DERIVATION_EXTENSION)){
			return new DomParserAdapter<>((ValueParser<T>) new StringParser());
		}
		return null;
		
	}
    @SuppressWarnings("unchecked")
	public <T> DomValueParser<? extends T> getParser(Class<T> clazz, Node n){
    	if( use_schema_info){
    		if( n.getNodeType() == Node.ELEMENT_NODE){
    			Element e = (Element) n;


    			log.debug("check for element schema based parser");

    			DomValueParser res = getParser(e.getSchemaTypeInfo(), n);

    			if( res != null){
    				log.debug("schema parser found");
    				//use schema to determine parser
    				return res;
    			}

    		}else if( n.getNodeType() == Node.ATTRIBUTE_NODE){
    			Attr e = (Attr) n;


    			log.debug("check for attribute schema based parser");

    			DomValueParser res = getParser(e.getSchemaTypeInfo(), n);

    			if( res != null){
    				log.debug("attribute schema parser found");
    				//use schema to determine parser
    				return res;
    			}
    		}
    	}
    	//TODO consider using ValueParserService
    	// default to using target type.
		if( clazz == String.class){
			log.debug("string");
			return new DomParserAdapter<>((ValueParser<T>) new StringParser());
		}
		if( clazz.isAssignableFrom(Date.class)){
			log.debug("date");
			return new DomParserAdapter<>((ValueParser<T>) new XMLDateTimeParser());
		}
		// Number first as otehrwise sub-class parsers will match
		if( clazz.isAssignableFrom(Number.class)){
			log.debug("number");
			return new DomParserAdapter<>((ValueParser<T>) new NumberParser());
		}
		if( clazz.isAssignableFrom(Integer.class)){
			log.debug("integer");
			return new DomParserAdapter<>((ValueParser<T>)new IntegerParser());
		}
		if( clazz.isAssignableFrom(Long.class)){
			log.debug("long");
			return new DomParserAdapter<>((ValueParser<T>)new LongParser());
		}
		if( clazz.isAssignableFrom(Double.class)){
			log.debug("double");
			return new DomParserAdapter<>((ValueParser<T>)new DoubleParser());
		}
		if( clazz.isAssignableFrom(Float.class)){
			log.debug("float");
			return new DomParserAdapter<>((ValueParser<T>)new FloatParser());
		}
		if( clazz.isAssignableFrom(Duration.class)){
			log.debug("duration");
			return new DomParserAdapter<>((ValueParser<T>) new XMLDurationParser());
		}
	
    	return null;
    }
    
    private String format(Node node){
    	try
        {
          // Set up the output transformer
          TransformerFactory transfac = TransformerFactory.newInstance();
          Transformer trans = transfac.newTransformer();
          trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
          trans.setOutputProperty(OutputKeys.INDENT, "yes");

          // Print the DOM node

          StringWriter sw = new StringWriter();
          StreamResult result = new StreamResult(sw);
          DOMSource source = new DOMSource(node);
          trans.transform(source, result);
          return sw.toString();

        }
        catch (TransformerException e)
        {
          log.error("Error printing dom",e);
          return "BAD DOM";
        }

    }





	public DocumentBuilder getBuilder() {
		return builder;
	}
}