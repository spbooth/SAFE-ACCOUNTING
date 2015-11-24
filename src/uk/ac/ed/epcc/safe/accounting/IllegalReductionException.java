// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting;
/** Exception thrown when an illegal reduction is requested
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: IllegalReductionException.java,v 1.6 2014/09/15 14:32:17 spb Exp $")

public class IllegalReductionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8876524908297766156L;

	public IllegalReductionException() {
		
	}

	public IllegalReductionException(String arg0) {
		super(arg0);
		
	}

	public IllegalReductionException(Throwable arg0) {
		super(arg0);
		
	}

	public IllegalReductionException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		
	}

}