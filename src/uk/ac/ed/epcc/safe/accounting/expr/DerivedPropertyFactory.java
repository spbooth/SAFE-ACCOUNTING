// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.expr;

/** A factory that defines a set of derived properties.
 * 
 * @author spb
 *
 */
public interface DerivedPropertyFactory {
	public abstract PropExpressionMap getDerivedProperties();
}