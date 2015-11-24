// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.update;
/** An exception that indicates a fatal error in the parse of an AccountingRecord
 * If such an exception is thrown the parse will move onto the next record in the update.
 * 
 * Care should be taken to ensure that if the same error occurs in multiple update records
 * the same exception message should be returned. This will allow
 * the parse to return the message once with a count value and keep the 
 * amount of error reporting to a minimum
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: AccountingParseException.java,v 1.3 2014/09/15 14:32:29 spb Exp $")

public class AccountingParseException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3405399311516702174L;

	public AccountingParseException() {
		super();
	}

	public AccountingParseException(String arg0) {
		super(arg0);
	}

	public AccountingParseException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public AccountingParseException(Throwable arg0) {
		super(arg0);
	}

}