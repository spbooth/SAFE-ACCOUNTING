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
package uk.ac.ed.epcc.safe.accounting.update;

import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
/** An object that adds properties to a PropertyContainer.
 * Note that the {@link #initFinder(AppContext, PropertyFinder, String)} method should always be called before any other methods from this interface.
 * While it would be cleaner to initialise the object within the constructor this process may need information about
 * the higher stages of the call chain.
 * 
 * @author spb
 *
 */
public interface PropertyContainerUpdater {
	  /** Generate a PropertyFinder that will find any PropertyTag generated by this class.
	   * This call should be treated as a secondary constructor and should be called before any
	   * other method from this interface.
	   * <p>
	    * All properties defined at run-time should have their PropertyTag objects constructed 
	    * by the time the first call to this method returns.
	    * As some parsers properties will generate different things depending on the other properties already
	    * in scope this method is passed a PropertyFinder for any properties that may already be in scope. This will 
	    * include any default parameters specified when the data was uploaded and any properties generated from
	    * the database meta-data. Though it is perfectly legal to return a PropertyFinder that returns a
	    * superset of the generated properties the only requirement is that the returned finder can find any property that
	    * this classes parse method may actually generate values for. So Normally the contents of the super-finder are not included in the
	    * result unless these values are actually generated by the parse. If values are generated then a MultiFinder can be populated with 
	    * the PropertyRegistrys from the generated properties
	    * @param ctx 
	    * @param prev  PropertyFinder for the higher levels in the call chain. 
	    * @param table String name of the destination table in case per-table customisation is needed
	    * @return PropertyFinder or null
	    */
	   public PropertyFinder initFinder(AppContext ctx, PropertyFinder prev, String table);
	 
	  
	 /** return a set of derived property definitions.
	  * Each derived property is defined in terms of a simple expression over other properties
	  * (that may themselves be derived properties). 
	  * <p>
	  * Definitions are allowed for properties that are also generated directly in the parse phase.
	  * In this case the parse value should always be used by preference and the derived definition 
	  * only provides a fall-back implementation if the parse value is unavailable. For example if not 
	  * persisted in the database layer.
	  * This allows one parser/policy to specify a default implementation as a derived property definition which
	  * is then overridden by having the same property generated in a parse method of a different policy/parser
	  * @param previous Previous definitions
	  * @return Map modified map
	  */
	  public PropExpressionMap getDerivedProperties(PropExpressionMap previous);
	 
	  /** Start a batch parse. This allocates any temporary state
	   * appropriate to the policy. Some properties may be set globally for all records in the parse
	   * rather than provided record by record. The startParse method is passed all known properties of this type 
	   * as they may be useful in setting up the parse. There is no requirement to process these in any way.
	   * 
	   * 
	   * @param static_props properties constant for this accounting run.
	   * @throws Exception 
	   */
	  public void startParse(PropertyContainer static_props) throws Exception;
	  /** End a batch parse this de-allocates any temporary storage and
	   * and perform any final operations.
	   * @return Error string if any
	   * 
	   *
	   */
	  public String endParse();
	  /** Modify the default table specification if appropriate
		 * A null specification denotes that a table should not be auto-created.
		 * 
		 * Most work is usually done by the parser (modifying a blank or minimal specification)
		 * 
		 * A policy may also make changes but these will obviously only be visible is the
		 * Policy has been configured before the {@link TableSpecification} is used.
		 * 
		 * A {@link PropExpressionMap} can be passed to this call to allow some resolution of
		 * alternative field names
		 * 
		 * 
		 * @param conn AppContext
		 * @param t {@link TableSpecification} or null
		 * @param derv {@link PropExpressionMap} or null
		 * @param table_name
		 * @return modified TableSpecification
		 */
		public TableSpecification modifyDefaultTableSpecification(AppContext conn, TableSpecification t,PropExpressionMap derv,String table_name);
}