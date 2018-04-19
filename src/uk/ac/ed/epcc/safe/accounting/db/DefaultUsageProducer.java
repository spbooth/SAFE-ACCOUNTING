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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.IllegalReductionException;
import uk.ac.ed.epcc.safe.accounting.NumberReductionTarget;
import uk.ac.ed.epcc.safe.accounting.PropertyImplementationProvider;
import uk.ac.ed.epcc.safe.accounting.ReductionMapResult;
import uk.ac.ed.epcc.safe.accounting.ReductionTarget;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.expr.DurationPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTuple;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidSQLPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.OverlapType;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Feature;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.jdbc.expr.Reduction;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.Duration;
import uk.ac.ed.epcc.webapp.preferences.Preference;
import uk.ac.ed.epcc.webapp.session.SessionService;
import uk.ac.ed.epcc.webapp.time.Period;
import uk.ac.ed.epcc.webapp.timer.TimerService;

/** Common base class for turning a {@link DataObjectPropertyFactory} into a {@link UsageProducer}
 * This class implements most of the query logic needed to implement UsageProducer
 * but down not constrain the class of the {@link ExpressionTargetContainer} more than is required to implement {@link UsageProducer}
 * @author spb
 * @param <T> class of UsageRecord
 *
 */ 
public abstract  class DefaultUsageProducer<T extends DataObjectPropertyContainer & ExpressionTargetContainer>  extends DataObjectPropertyFactory<T> implements UsageProducer<T> ,PropertyImplementationProvider{
	

	protected Logger log;
	    
		protected DefaultUsageProducer(){
			
		}
		
		protected DefaultUsageProducer(AppContext c, String table) {
			setContext(c, table);
			log=c.getService(LoggerService.class).getLogger(getClass());
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

	public <I> Map<I, Number> getReductionMap(PropExpression<I> index,
			ReductionTarget<Number> property,  RecordSelector selector)
			throws Exception 
	{
		return getReductionHandler().getReductionMap(index, property, selector);
		
	}

	
	public  Map<ExpressionTuple, ReductionMapResult> getIndexedReductionMap( Set<ReductionTarget> sum, RecordSelector selector) throws Exception{
		return getReductionHandler().getIndexedReductionMap(sum, selector);
	}
	
	
	public  <R>  R getReduction(ReductionTarget<R> type, RecordSelector selector) throws Exception {
		return getReductionHandler().getReduction(type, selector);
	}
	
	
	@Override
	public final String getImplemenationInfo(PropertyTag<?> tag) {
		return getAccessorMap().getImplemenationInfo(tag);
	}
}