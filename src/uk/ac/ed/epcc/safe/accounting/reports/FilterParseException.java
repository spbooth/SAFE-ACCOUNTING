// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.reports;
@uk.ac.ed.epcc.webapp.Version("$Id: FilterParseException.java,v 1.6 2014/09/15 14:32:28 spb Exp $")


public class FilterParseException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4212405799775364105L;

	public FilterParseException() {
		
	}

	public FilterParseException(String message) {
		super(message);
		
	}

	public FilterParseException(Throwable cause) {
		super(cause);
		
	}

	public FilterParseException(String message, Throwable cause) {
		super(message, cause);
		
	}

}