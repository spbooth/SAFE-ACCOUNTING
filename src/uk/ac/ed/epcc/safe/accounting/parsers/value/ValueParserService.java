// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers.value;

import java.util.HashMap;
import java.util.Map;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.AppContextService;
import uk.ac.ed.epcc.webapp.ClassTableCreator;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.content.Table;

/**
 * <p>
 * This class may be used to fetch a <code>ValueParser</code> by a specific
 * name. 
 * Parsers may  be added by defining their class in the current
 * <code>AppContext</code> with a property of the following Form:
 * </p>
 * 
 * <blockquote>
 * 
 * class.value_parser.<em>parserName</em> = <em>binary java name</em><br/>
 * 
 * </blockquote>
 * 
 * for example:
 * 
 * <blockquote> class.value_parser.myParser = com.example.MySpecialParser
 * 
 * </blockquote>
 * 
 * <p>
 * The class must be on the class path and implement the {@link ValueParser}
 * interface.
 * </p>
 * 
 * @author jgreen4
 * 
 */
@uk.ac.ed.epcc.webapp.Version("$Id: ValueParserService.java,v 1.19 2014/09/15 14:32:26 spb Exp $")

public class ValueParserService implements Contexed, AppContextService<ValueParserService> {

	private static final String VALUE_PARSER_PATH = "value_parser";
	/*
	 * The context under which this service operates
	 */
	private AppContext context;
	/*
	 * A mapping of all the currently loaded parsers to their names
	 */
	private Map<String, ValueParser> cachedParsers;


	

	

	/**
	 * Constructs a new <code>ValueParserService</code> which will configure
	 * itself based on the state presented by <code>context</code>.
	 * 
	 * @param context
	 *          The environment within which this service operates.
	 */
	public ValueParserService(AppContext context) {
		if (context == null)
			throw new NullPointerException("Context cannot be null");

		this.context = context;
		this.cachedParsers = new HashMap<String, ValueParser>();
       
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ed.epcc.webapp.Contexed#getContext()
	 */
	public AppContext getContext() {
		return this.context;
	}

	

	/**
	 * Returns a <code>ValueParser</code> with the specified name. Several names
	 * for parsers have already been predefined. Others may be defined via the
	 * <code>AppContext</code> provided during construction of this object.
	 * 
	 * @param parserName
	 *          The name of the parser to return
	 * @return The specified parser
	 * @throws IllegalArgumentException
	 *           If a <code>ValueParser</code> with the specified name cannot be
	 *           found
	 */
	public ValueParser getValueParser(String parserName)
			throws IllegalArgumentException {
		

		ValueParser bundle = this.cachedParsers.get(parserName);

		if (bundle == null){
			bundle = this.getNewParser(parserName);
		}
		
		return bundle;
	}
	


	/*
	 * ##########################################################################
	 * PRIVATE METHODS
	 * ##########################################################################
	 */

	/**
	 * Attempts to find the parser specified by it's name by looking for it's
	 * declaration in the current <code>AppContext</code>. The
	 * <code>AppContext</code> is checked for a property, indexed by the parser's
	 * name, that indicates which class should be used (identified by Java binary
	 * name). The class is then constructed via reflection and the appropriate
	 * ValueParser is returned
	 * 
	 * @param parserName
	 *          The name of the parser to find
	 * @return The <code>ValueParser</code> with the specified name or null
	 *
	 * @throws IllegalArgumentException
	 *           If the parser couldn't be constructed for some reason
	 */
	private ValueParser getNewParser(String parserName)
			throws ClassCastException, IllegalArgumentException {

		ValueParser<?> parser;
		try {
			parser = this.context.makeObjectWithDefault(ValueParser.class, null,VALUE_PARSER_PATH, parserName);
			if( parser == null ){
				return null;
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to construct ValueParser '"
					+ parserName + "'", e);
		}

		
		this.cachedParsers.put(parserName, parser);
		return parser;
	}

	

	public void cleanup() {
		cachedParsers.clear();
	}

	public Table getDocumentationTable(){
		ClassTableCreator creator = new ClassTableCreator(context);
		return creator.getList(ValueParser.class, VALUE_PARSER_PATH);
	}
	public Class<? super ValueParserService> getType() {
		return ValueParserService.class;
	}
}