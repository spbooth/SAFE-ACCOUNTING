// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.properties;


/** Exception thrown when a PropertyContainer does not support the specified property
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: InvalidPropertyException.java,v 1.4 2015/03/10 16:56:02 spb Exp $")

public class InvalidPropertyException extends InvalidExpressionException {
  /**
	 * 
	 */
	private static final long serialVersionUID = -8081265936899370239L;
	public InvalidPropertyException(PropExpression tag){
		this(null,tag);
	}
  public InvalidPropertyException(String table,PropExpression tag){
	  super("Invalid property "+(tag==null?" Null expression " : tag.toString())+(table==null?"":" in "+table));
  }
  public InvalidPropertyException(String s){
	  super(s);
  }
}