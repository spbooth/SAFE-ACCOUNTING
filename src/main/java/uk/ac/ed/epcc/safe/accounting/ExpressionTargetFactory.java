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

import uk.ac.ed.epcc.safe.accounting.db.AccessorMap;
import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyFactory;
/** Interface for property enabled factories.
 * Essentially this means it is a {@link ExpressionTargetGenerator} that can generate an {@link AccessorMap}.
 * {@link AccessorMap}s are specific to a single table.
 * 
 * This may either be implemented directly by the factory or a composite
 * @author spb
 *
 * @param <T>
 */
public interface ExpressionTargetFactory<T> extends ExpressionTargetGenerator<T>, DerivedPropertyFactory
{
	/** fetch the underlying {@link AccessorMap}
	 * @return AccessorMap
	 * 
	 */
	public AccessorMap<T> getAccessorMap();
	
	
}