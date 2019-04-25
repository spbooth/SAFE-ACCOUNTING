//| Copyright - The University of Edinburgh 2011                            |
//|                                                                         |
//| Licensed under the Apache License, Version 2.0 (the "License");         |
//| you may not use this file except in compliance with the License.        |
//| You may obtain a copy of the License at                                 |
//|                                                                         |
//|    http://www.apache.org/licenses/LICENSE-2.0                           |
//|                                                                         |
//| Unless required by applicable law or agreed to in writing, software     |
//| distributed under the License is distributed on an "AS IS" BASIS,       |
//| WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.|
//| See the License for the specific language governing permissions and     |
//| limitations under the License.                                          |
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