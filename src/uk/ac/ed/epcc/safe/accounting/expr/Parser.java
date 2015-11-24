// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.expr.parse.ExpressionLexer;
import uk.ac.ed.epcc.safe.accounting.expr.parse.ExpressionParser;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.UnresolvedNameException;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;

/** Simple expression parser.
 *
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: Parser.java,v 1.33 2014/09/15 14:32:22 spb Exp $")


public class Parser implements Contexed{
	
	AppContext conn;
	PropertyFinder finder;
	public Parser(AppContext conn,PropertyFinder reg){
		this.conn=conn;
		this.finder=reg;
	}
	/** Parse a String into a PropExpression
	 * 
	 * @param s String to parse
	 * @return PropExpression
	 * @throws ParseException
	 * @throws UnresolvedNameException 
	
	 */
	public PropExpression parse(String s) throws ParseException, UnresolvedNameException{

		ExpressionParser parser = new ExpressionParser(new ExpressionLexer(conn,s));
		parser.init(conn,finder);
		try {
			if( parser.parse()){
				return parser.getExpression();
			}
		}catch( UnresolvedNameException ipe){
			throw ipe;
		} catch (Exception e) {
			throw new ParseException("bad expression <"+s+">",e);
		}
		throw new ParseException("Bad expression parse failed");
		
		
		
	}
	public AppContext getContext() {
		return conn;
	}

}