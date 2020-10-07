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
package uk.ac.ed.epcc.safe.accounting.db;

import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
/** Default implementation of the DataObjectPropertyFactory logic.
 * 
 * To keep this lightweight when this functionality is not in use the 
 * {@link AccessorMap} is made lazily.
 * @author spb
 *
 * @param <T>
 */
public abstract  class DefaultDataObjectPropertyFactory<T extends DataObjectPropertyContainer> extends
		DataObjectFactory<T> implements  AccessorContributer<T>{
	
	

	protected final ExpressionTargetFactoryComposite<T> etf = new ExpressionTargetFactoryComposite<>(this);
	 
	
	/** Extension point to allow custom accessors and registries to be added.
	 * 
	 * @param mapi2
	 * @param finder
	 * @param derived
	 */
	public void customAccessors(AccessorMap<T> mapi2, MultiFinder finder,
			PropExpressionMap derived) {
		
	}
	
	
	

	
	
	
	
	
	
}