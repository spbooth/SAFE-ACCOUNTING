//| Copyright - The University of Edinburgh 2015                            |
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

import uk.ac.ed.epcc.safe.accounting.PropertyImplementationProvider;
import uk.ac.ed.epcc.safe.accounting.ReductionMapResult;
import uk.ac.ed.epcc.safe.accounting.ReductionTarget;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTuple;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;

/** Common base class for turning a {@link DataObjectPropertyFactory} into a {@link UsageProducer}
 * This class implements most of the query logic needed to implement UsageProducer
 * but down not constrain the class of the {@link ExpressionTargetContainer} more than is required to implement {@link UsageProducer}
 * @author spb
 * @param <T> class of UsageRecord
 *
 */ 
public abstract  class DefaultUsageProducer<T extends DataObjectPropertyContainer>  extends DataObjectPropertyFactory<T> implements UsageProducer<T> ,PropertyImplementationProvider{
	private ExpressionTargetFactoryComposite<T> etf = new ExpressionTargetFactoryComposite<>(this);
	    
		protected DefaultUsageProducer(){
			
		}
		
		protected DefaultUsageProducer(AppContext c, String table) {
			setContext(c, table);
		}
		
	/** Method to supports unit tests 
	 * 
	 * @return
	 */
	String getUnderlyingTable(){
		return res.getTag();
	}


	public final PropExpressionMap getDerivedProperties(){
		return getAccessorMap().getDerivedProperties();
	}
	

	
	private ReductionHandler<T, DefaultUsageProducer<T>> getReductionHandler(){
		return new ReductionHandler<T, DefaultUsageProducer<T>>(this);
	}

	public final <I> Map<I, Number> getReductionMap(PropExpression<I> index,
			ReductionTarget<Number> property,  RecordSelector selector)
			throws Exception 
	{
		return getReductionHandler().getReductionMap(index, property, selector);
		
	}

	
	public final Map<ExpressionTuple, ReductionMapResult> getIndexedReductionMap( Set<ReductionTarget> sum, RecordSelector selector) throws Exception{
		return getReductionHandler().getIndexedReductionMap(sum, selector);
	}
	
	
	public final <R>  R getReduction(ReductionTarget<R> type, RecordSelector selector) throws Exception {
		return getReductionHandler().getReduction(type, selector);
	}
	
	
	@Override
	public final String getImplemenationInfo(PropertyTag<?> tag) {
		return getAccessorMap().getImplemenationInfo(tag);
	}

	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.UsageProducer#getExpressionTarget(java.lang.Object)
	 */
	@Override
	public final ExpressionTargetContainer getExpressionTarget(T record) {
		return getAccessorMap().getProxy(record);
	}
	@Override
	public final boolean isMyTarget(T record) {
		return isMine(record);
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