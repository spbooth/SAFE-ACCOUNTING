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
package uk.ac.ed.epcc.safe.accounting;



import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.webapp.AppContext;


/** Interface implemented by all individual usage records.
 * 
 * Typically usage records will have properties of type {@link Date}
 * that define when the usage occured.
 * @author spb
 *
 */
public interface UsageRecord extends PropertyContainer, ExpressionTarget{
	
    /** Unique key for populating a Table 
	 * Note the Table may have entries from more than one {@link UsageProducer}
	 *  @return Object unique to this table and record
	 */
	public Object getKey();
	/** get AppContext
	 * 
	 * @return AppContext
	 */
    public AppContext getContext();
    /** get identifying string.
     * e.g. for pull-down menus.
     * 
     * @return
     */
    public String getIdentifier();
}