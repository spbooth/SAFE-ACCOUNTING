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
package uk.ac.ed.epcc.safe.accounting.expr;



import uk.ac.ed.epcc.safe.accounting.db.DataObjectPropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;


/** Interface for records targeted by the report generator.
 * These may be wrapper object that proxy to the real underlying record.
 * 
 * This is a simple combination of {@link PropertyContainer} and {@link ExpressionTarget}.
 * so all {@link DataObjectPropertyContainer}s implement it.
 * 
 * @author spb
 *
 */
public interface ExpressionTargetContainer extends PropertyContainer, ExpressionTarget{
	/** flush changes to the underlying database.
	 * @return true if changed
	 * 
	 * @throws DataFault
	 */
    public boolean commit() throws DataFault;
    /** remove the underlying database record.
     * 
     * @throws DataFault
     */
    public boolean delete() throws DataFault;
}