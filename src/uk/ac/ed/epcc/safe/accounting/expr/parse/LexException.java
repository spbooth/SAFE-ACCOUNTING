// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.expr.parse;

import uk.ac.ed.epcc.safe.accounting.expr.ParseException;


@uk.ac.ed.epcc.webapp.Version("$Id: LexException.java,v 1.3 2014/09/15 14:32:22 spb Exp $")


public class LexException extends ParseException {
   public LexException(CharSequence s){
	   super(s.toString());
   }
   public LexException(Throwable t){
	   super(t);
   }
}