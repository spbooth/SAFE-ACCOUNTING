// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.Operator;
/** An expression that negates a numerical value.
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: NegatePropExpression.java,v 1.8 2014/09/15 14:32:22 spb Exp $")
public class NegatePropExpression extends BinaryPropExpression {

	public NegatePropExpression(PropExpression<? extends Number> b)
			throws PropertyCastException {
		super(new ConstPropExpression<Double>(Double.class,0.0), Operator.SUB, b);
	}

}