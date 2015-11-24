// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.upload;
@uk.ac.ed.epcc.webapp.Version("$Id: UploadException.java,v 1.5 2014/09/15 14:32:30 spb Exp $")


public class UploadException extends Exception {

	public UploadException() {
		
	}

	public UploadException(String arg0) {
		super(arg0);
		
	}

	public UploadException(Throwable arg0) {
		super(arg0);
	
	}

	public UploadException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		
	}

}