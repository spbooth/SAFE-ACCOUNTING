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

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.PropertyImplementationProvider;
import uk.ac.ed.epcc.safe.accounting.ReductionMapResult;
import uk.ac.ed.epcc.safe.accounting.ReductionTarget;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.UsageProducerWrapper;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTuple;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.CannotUseSQLException;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterConverter;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.jdbc.filter.NoSQLFilterException;
import uk.ac.ed.epcc.webapp.model.TimePurgeFactory;
import uk.ac.ed.epcc.webapp.model.data.CloseableIterator;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.Repository.Record;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.filter.FilterDelete;
import uk.ac.ed.epcc.webapp.model.data.iterator.SkipIterator;

/** Common base class for turning a {@link DataObjectFactory} into a {@link UsageProducer}
 * This class implements most of the query logic needed to implement UsageProducer
 * but does not constrain the class of the {@link ExpressionTargetContainer} more than is required to implement {@link UsageProducer}
 *  It is not necessary to implement {@link UsageProducer} directly as any factory that
 *  supports properties can be wrapped in a {@link UsageProducerWrapper}
 * 
 * @author spb
 * @param <T> class of UsageRecord
 *
 */ 
public abstract  class DefaultUsageProducer<T extends DataObjectPropertyContainer>  extends DataObjectFactory<T> implements UsageProducer<T>,TimePurgeFactory,PropertyImplementationProvider {
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
		return etf.getDerivedProperties();
	}
	

	
	private ReductionHandler<T, ExpressionTargetFactoryComposite<T>> getReductionHandler(){
		return new ReductionHandler<>(etf,composite);
	}

	public final <I,T,D> Map<I, T> getReductionMap(PropExpression<I> index,
			ReductionTarget<T,D> property,  RecordSelector selector)
			throws Exception 
	{
		return getReductionHandler().getReductionMap(index, property, selector);
		
	}

	
	public final Map<ExpressionTuple, ReductionMapResult> getIndexedReductionMap( Set<ReductionTarget> sum, RecordSelector selector) throws Exception{
		return getReductionHandler().getIndexedReductionMap(sum, selector);
	}
	
	
	public final <R,D>  R getReduction(ReductionTarget<R,D> type, RecordSelector selector) throws Exception {
		return getReductionHandler().getReduction(type, selector);
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.UsageProducer#getExpressionTarget(java.lang.Object)
	 */
	@Override
	public final ExpressionTargetContainer getExpressionTarget(T record) {
		return etf.getExpressionTarget(record);
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
	public final <P> boolean hasProperty(PropertyTag<P> tag) {
		return etf.hasProperty(tag);
	}

	@Override
	public final  <I> boolean compatible(PropExpression<I> expr) {
		return etf.compatible(expr);
	}

	@Override
	public final boolean compatible(RecordSelector sel) {
		return etf.compatible(sel);
	}

	/** Get a filter from a {@link RecordSelector}
	 * 
	 * @param selector
	 * @return
	 * @throws CannotFilterException 
	 */
	
	public final BaseFilter<T> getFilter(RecordSelector selector) throws CannotFilterException {
		return etf.getAccessorMap().getFilter(selector);
	}
	public  final CloseableIterator<T> getIterator(RecordSelector sel,int skip,int count) throws DataFault, CannotFilterException {
		BaseFilter<T> filter = getFilter(sel);
		try{
			return this.new FilterIterator(FilterConverter.convert(filter),skip,count);
		}catch(NoSQLFilterException e){
			return new SkipIterator<>(new FilterIterator(filter), skip, count);
		}
	}

	@Override
	public final CloseableIterator<T> getIterator(RecordSelector sel) throws Exception {
		return getResult(getFilter(sel)).iterator();
	}

	@Override
	public final long getRecordCount(RecordSelector selector) throws Exception {
		return getCount(getFilter(selector));
	}

	@Override
	public final boolean exists(RecordSelector selector) throws Exception {
		return exists(getFilter(selector));
	}
	@Override
	public final  <PT> Set<PT> getValues(PropExpression<PT> tag, RecordSelector selector) throws Exception {
		if( ! compatible(tag)){
			return new HashSet<>();
		}
		BaseFilter<T> filter = getFilter(selector);	
		RepositoryAccessorMap<T> map = etf.getAccessorMap();
		try{
			PropertyMaker<T,PT> finder = new PropertyMaker<>(map,res,tag, true);			
			return finder.find(FilterConverter.convert(filter));
		}catch(CannotUseSQLException e){
			Set<PT> result = new HashSet<>();
			for(T o : new FilterSet(filter)){
				ExpressionTargetContainer proxy = map.getContainer(o);
				result.add(proxy.evaluateExpression(tag));
				proxy.release();
				o.release();
			}
			return result;
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.UsageProducer#getExpressionIterator(uk.ac.ed.epcc.safe.accounting.selector.RecordSelector)
	 */
	@Override
	public CloseableIterator<ExpressionTargetContainer> getExpressionIterator(RecordSelector sel) throws Exception {
		return etf.getExpressionIterator(sel);
	}

	@Override
	public void purgeOldData(Date epoch) throws Exception {
		FilterDelete<T> del = new FilterDelete<>(res);
		del.delete(FilterConverter.convert(getFilter(new SelectClause<>(StandardProperties.ENDED_PROP, MatchCondition.LT, epoch))));
		
	}
	
	private boolean composite = false;

	@Override
	public boolean setCompositeHint(boolean composite) {
		boolean old = this.composite;
		this.composite=composite;
		return old;
	}

	@Override
	public String getImplemenationInfo(PropertyTag<?> tag) {
	
		return etf.getAccessorMap().getImplemenationInfo(tag);
	}

	



}