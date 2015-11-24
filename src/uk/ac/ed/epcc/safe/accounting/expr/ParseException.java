// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.expr;
@uk.ac.ed.epcc.webapp.Version("$Id: ParseException.java,v 1.6 2014/09/15 14:32:22 spb Exp $")


public class ParseException extends Exception {

	public ParseException(String string) {
		super(string);
	}
    public ParseException(Throwable t){
    	super(t);
    }
    public ParseException(String message,Throwable t){
    	super(message,t);
    }
}