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

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import uk.ac.ed.epcc.safe.accounting.expr.ExpressionCast;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.ParseException;
import uk.ac.ed.epcc.safe.accounting.expr.Parser;
import uk.ac.ed.epcc.safe.accounting.formatters.value.DomFormatter;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.AppContext;
/** A Format that evaluates an expression on an ExpressionTarget
 * 
 * @author spb
 *
 */


public class ExpressionFormat implements DomFormatter<Object> {

	private final String expr;
	private final AppContext conn;
	public ExpressionFormat(AppContext c,String expr){
		this.conn=c;
		this.expr=expr;
	}
	
	public Node format(Document doc,Object o) throws InvalidExpressionException, ParseException {
		ExpressionTarget t = ExpressionCast.getExpressionTarget(o);
		if( t == null) {
			throw new InvalidExpressionException("ExpressionTarget cannot be generated for "+o);
		}
		
		Parser p = t.getParser();

		PropExpression<?> e = p.parse(expr);
		Object result = t.evaluateExpression(e);
		if(result != null){
			return doc.createTextNode(result.toString());

		}
		return null;
	}

	public Class<Object> getTarget() {
		return Object.class;
	}

	

}