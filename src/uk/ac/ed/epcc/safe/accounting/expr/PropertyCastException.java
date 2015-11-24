// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.expr;
/** Exception thrown when the target type for a prop-expression is inappropriate for its context+
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: PropertyCastException.java,v 1.3 2014/09/15 14:32:22 spb Exp $")

public class PropertyCastException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3458183443198227797L;

	public PropertyCastException() {
		
	}

	public PropertyCastException(String message) {
		super(message);
		
	}

	public PropertyCastException(Throwable cause) {
		super(cause);
		
	}

	public PropertyCastException(String message, Throwable cause) {
		super(message, cause);
		
	}

}