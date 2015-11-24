// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.properties;

import uk.ac.ed.epcc.webapp.jdbc.filter.CannotUseSQLException;


/** Exception thrown when a Property cannot be implemented in SQL
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: InvalidSQLPropertyException.java,v 1.2 2014/09/15 14:32:27 spb Exp $")

public class InvalidSQLPropertyException extends CannotUseSQLException {
  /**
	 * 
	 */
	private static final long serialVersionUID = -8081265936899370239L;
	public InvalidSQLPropertyException(PropExpression tag){
		this(null,tag);
	}
  public InvalidSQLPropertyException(String table,PropExpression tag){
	  super("Invalid property "+(tag==null?" Null expression " : tag.toString())+(table==null?"":" in "+table));
  }
  public InvalidSQLPropertyException(String s){
	  super(s);
  }
}