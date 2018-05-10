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

import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ReductionMapResult;
import uk.ac.ed.epcc.safe.accounting.ReductionTarget;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTuple;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
/** Default implementation of the DataObjectPropertyFactory logic.
 * 
 * To keep this lightweight when this functionality is not in use the 
 * {@link AccessorMap} is made lazily.
 * @author spb
 *
 * @param <T>
 */
public abstract  class DefaultDataObjectPropertyFactory<T extends DataObjectPropertyContainer> extends
		DataObjectPropertyFactory<T> implements  AccessorContributer<T>{
	
	

	private ExpressionTargetFactoryComposite<T> etf = new ExpressionTargetFactoryComposite<>(this);
	 
	
	/** Extension point to allow custom accessors and registries to be added.
	 * 
	 * @param mapi2
	 * @param finder
	 * @param derived
	 */
	public void customAccessors(AccessorMap<T> mapi2, MultiFinder finder,
			PropExpressionMap derived) {
		
	}
	
	
	@Override
	public final PropExpressionMap getDerivedProperties() {
		return etf.getDerivedProperties();
	}


	@Override
	public final PropertyFinder getFinder() {
		return etf.getFinder();
	}


	@Override
	public final RepositoryAccessorMap<T> getAccessorMap() {
		return etf.getAccessorMap();
	}


	
	
	
	
	
	
}