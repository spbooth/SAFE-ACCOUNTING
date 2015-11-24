// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.reports;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

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
@uk.ac.ed.epcc.webapp.Version("$Id: ExpressionFormat.java,v 1.9 2015/03/10 16:56:03 spb Exp $")

public class ExpressionFormat implements DomFormatter<ExpressionTarget> {

	private final String expr;
	private final AppContext conn;
	public ExpressionFormat(AppContext c,String expr){
		this.conn=c;
		this.expr=expr;
	}
	
	public Node format(Document doc,ExpressionTarget t) throws InvalidExpressionException, ParseException {

		Parser p = t.getParser();

		PropExpression<?> e = p.parse(expr);
		Object result = t.evaluateExpression(e);
		if(result != null){
			return doc.createTextNode(result.toString());

		}
		return null;
	}

	public Class<ExpressionTarget> getTarget() {
		return ExpressionTarget.class;
	}

	

}