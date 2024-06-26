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
package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.expr.parse.ExpressionLexer;
import uk.ac.ed.epcc.safe.accounting.expr.parse.ExpressionParser;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.UnresolvedNameException;
import uk.ac.ed.epcc.webapp.AbstractContexed;
import uk.ac.ed.epcc.webapp.AppContext;

/** Simple expression parser.
 *
 * @author spb
 *
 */



public class Parser extends AbstractContexed{
	
	PropertyFinder finder;
	int debug=0;
	public Parser(AppContext conn,PropertyFinder reg){
		super(conn);
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
		parser.setDebugLevel(debug);;
		try {
			if( parser.parse()){
				return parser.getExpression();
			}
		}catch( UnresolvedNameException ipe){
			throw ipe;
		} catch (Exception e) {
			throw new ParseException("bad expression <"+s+">",e);
		}
		throw new ParseException("Bad expression parse failed for ["+s+"]");
		
		
		
	}
	
	/**
	 * @return the debug
	 */
	public int getDebug() {
		return debug;
	}
	/**
	 * @param debug the debug to set
	 */
	public void setDebug(int debug) {
		this.debug = debug;
	}

}