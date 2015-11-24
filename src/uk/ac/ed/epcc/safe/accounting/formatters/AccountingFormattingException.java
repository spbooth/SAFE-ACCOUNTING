// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.formatters;

/**
 * An exception that indicates a fatal error while formatting an
 * AccountingRecord.
 * 
 * Care should be taken to ensure that if the same error occurs in multiple
 * update records the same exception message should be returned. This will allow
 * the parse to return the message once with a count value and keep the amount
 * of error reporting to a minimum
 * 
 * @author jgreen4
 * 
 */
@Deprecated
@uk.ac.ed.epcc.webapp.Version("$Id: AccountingFormattingException.java,v 1.7 2014/09/15 14:32:22 spb Exp $")

public class AccountingFormattingException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Exception#Exception()
	 */
	public AccountingFormattingException() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Exception#Exception(String)
	 */
	public AccountingFormattingException(String arg0) {
		super(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Exception#Exception(String, Throwable)
	 */
	public AccountingFormattingException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Exception#Exception(Throwable)
	 */
	public AccountingFormattingException(Throwable arg0) {
		super(arg0);
	}
}