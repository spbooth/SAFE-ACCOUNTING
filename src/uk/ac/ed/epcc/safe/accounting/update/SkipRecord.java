// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.update;


/** Indicates a record that should be ignored without warning.
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: SkipRecord.java,v 1.3 2014/09/15 14:32:29 spb Exp $")

public class SkipRecord extends AccountingParseException {
  /**
	 * 
	 */
	private static final long serialVersionUID = 1983746511919729616L;

public SkipRecord(String reason){
	  super(reason);
  }
}