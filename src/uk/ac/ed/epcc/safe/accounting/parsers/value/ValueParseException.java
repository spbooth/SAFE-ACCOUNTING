// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers.value;
@uk.ac.ed.epcc.webapp.Version("$Id: ValueParseException.java,v 1.7 2014/09/15 14:32:26 spb Exp $")

/** Exception thrown when a {@link ValueParser} cannot parse its arguments
 * 
 * @author spb
 *
 */
public class ValueParseException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5330237814791821000L;

	public ValueParseException() {
	}

	public ValueParseException(String message) {
		super(message);
		
	}

	public ValueParseException(Throwable cause) {
		super(cause);
		
	}

	public ValueParseException(String message, Throwable cause) {
		super(message, cause);
		
	}

}