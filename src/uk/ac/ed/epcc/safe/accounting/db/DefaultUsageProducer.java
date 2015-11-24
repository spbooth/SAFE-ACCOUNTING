package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.NumberReductionTarget;
import uk.ac.ed.epcc.safe.accounting.Reduction;
import uk.ac.ed.epcc.safe.accounting.ReductionMapResult;
import uk.ac.ed.epcc.safe.accounting.ReductionTarget;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.UsageRecord;
import uk.ac.ed.epcc.safe.accounting.expr.DurationPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTuple;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.OverlapType;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Feature;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.Duration;
import uk.ac.ed.epcc.webapp.time.Period;

/** Common base class for turning a {@link DataObjectPropertyFactory} into a {@link UsageProducer}
 * This class implements most of the query logic needed to implement UsageProducer
 * but down not constrain the class of the {@link UsageRecord} more than is required to implement {@link UsageProducer}
 * @author spb
 * @param <T> class of UsageRecord
 *
 */ 
public abstract  class DefaultUsageProducer<T extends DataObjectPropertyContainer & UsageRecord>  extends DataObjectPropertyFactory<T> implements UsageProducer<T> {
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

	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.UsageProducer#getImplemenationInfo(uk.ac.ed.epcc.safe.accounting.PropertyTag)
	 */
	public String getImplemenationInfo(PropertyTag<?> tag) {
		return  getAccessorMap().getImplemenationInfo(tag);
	}


	public final PropExpressionMap getDerivedProperties(){
		return getAccessorMap().getDerivedProperties();
	}
	private static class CutoffKey{
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((end == null) ? 0 : end.hashCode());
			result = prime * result + ((start == null) ? 0 : start.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CutoffKey other = (CutoffKey) obj;
			if (end == null) {
				if (other.end != null)
					return false;
			} else if (!end.equals(other.end))
				return false;
			if (start == null) {
				if (other.start != null)
					return false;
			} else if (!start.equals(other.start))
				return false;
			return true;
		}
		public CutoffKey(PropExpression<Date> start, PropExpression<Date> end) {
			super();
			this.start = start;
			this.end = end;
		}
		public final PropExpression<Date> start;
		public final PropExpression<Date> end;
		
	}
	public Map<CutoffKey,Long> cutoffs=null;
	public static final Feature AUTO_CUTOFF_FEATURE = new Feature("auto_cutoff",true,"automatically calculate cutoffs (maximum record time extent, used to optimise search) using additional queried");
	
	public BaseFilter<T> getPeriodFilter(Period period,
			PropExpression<Date> start, PropExpression<Date> end, OverlapType type,long cutoff)
			throws CannotFilterException {
		if( start == null || end == null || start.equals(end)){
			cutoff=0L;
		}else if(AUTO_CUTOFF_FEATURE.isEnabled(getContext())){
			if( cutoffs == null ){
				cutoffs=new HashMap<DefaultUsageProducer.CutoffKey, Long>();
			}
			CutoffKey key = new CutoffKey(start, end);
			Long calc_cutoff = cutoffs.get(key);
			if(calc_cutoff ==null){
				try {
					final DurationPropExpression duration = new DurationPropExpression(start, end);
					// go for global max length. More likely to cache
					// answer in Sql level and independent of period so can cheaply cache
					// at this level. Alternative would need map keyed by props and period.
					AndRecordSelector sel = new AndRecordSelector();
					sel.add(new SelectClause<Duration>(duration,MatchCondition.GT,new Duration(0L,1L)));
					sel.add(new SelectClause<Date>(start,MatchCondition.GT,new Date(0L)));
					calc_cutoff = new Long(getReduction(NumberReductionTarget.getInstance(Reduction.MAX, duration), sel).longValue()+1L);
					if( log !=null) log.debug(getTag()+": calculated cutoff for "+start+","+end+" as "+cutoff);
					cutoffs.put(key,calc_cutoff);
				} catch (Exception e) {
					getContext().error(e, "Error making cutoff");
					calc_cutoff=0L;
				}
			}
			cutoff=calc_cutoff;
		}
		return super.getPeriodFilter(period, start, end,type,cutoff);
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
	
	
	
}
