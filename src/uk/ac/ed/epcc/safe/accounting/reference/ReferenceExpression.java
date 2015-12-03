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
package uk.ac.ed.epcc.safe.accounting.reference;

import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Indexed;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedProducer;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
/** A prop-expression for references
 * 
 * In this case the getTarget method is insufficient to define the type of the
 * result fully so we add additional methods to provide the missing information.
 * 
 * @author spb
 *
 * @param <I> type of reference
 */
public interface ReferenceExpression<I extends Indexed> extends PropExpression<IndexedReference<I>> {

	public IndexedProducer<I> getFactory(AppContext c);
	
	public Class<? extends IndexedProducer> getFactoryClass();
	
	public String getTable();
	
	public ReferenceExpression<I> copy();
}