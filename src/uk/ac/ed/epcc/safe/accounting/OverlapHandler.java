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
package uk.ac.ed.epcc.safe.accounting;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.expr.BinaryPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.CasePropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ConstPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.DurationPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTuple;
import uk.ac.ed.epcc.safe.accounting.expr.PropertyCastException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.NullSelector;
import uk.ac.ed.epcc.safe.accounting.selector.OverlapType;
import uk.ac.ed.epcc.safe.accounting.selector.PeriodOverlapRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.ReductionSelector;
import uk.ac.ed.epcc.safe.accounting.selector.RelationClause;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Feature;
import uk.ac.ed.epcc.webapp.NumberOp;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.jdbc.expr.Operator;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.jdbc.filter.NoSQLFilterException;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.time.Period;
import uk.ac.ed.epcc.webapp.time.TimePeriod;
import uk.ac.ed.epcc.webapp.timer.TimerService;
//import uk.ac.ed.epcc.safe.accounting.charts.MapperEntry;

/** Class that implements the logic of overlap calculations.
 * <p>
 * This is needed whenever positive definite properties from a UsageRecord with known start and end dates is mapped
 * onto a {@link TimePeriod} for example a table column or a chart data point. 
 * These routines only make sense when the record period is significant compared to the
 * target period.
 * </p>
 * There are two kinds of scaling we need to worry about.
 * <ul>
 * <li><b>SUM</b> where the weight is the fraction of the record that overlaps. This is usually correct for Cumulative properties.
 * </li>
 * <li><b>AVG</b> where the weight is the fraction of the period that overlaps. This is usually correct for Point
 * properties such as rates as this gives the contribution to the <em>time average</em> over the period. 
 * </li>
 * </ul>
 * We keep this logic in a separate class so it can be used with different 
 * UsageProducer implementations.
 * 
 * @author spb
 * @param <T> 
 *
 */


public class OverlapHandler<T extends ExpressionTargetContainer> {
	private final AppContext conn;
    private final UsageProducer<T> prod;
    private Logger log=null;
	//TODO move this somewhere neutral also used in OverlapHander
	public static final Feature USE_QUERY_MAPPER_FEATURE = new Feature("use_query_mapper",true,"Use grouping queries rather than iteration in reductions");
	public static final Feature USE_CASE_OVERLAP = new Feature("use_case_overlap",false,"Use case expressions to implement overlap");
	  
	public OverlapHandler(AppContext conn,UsageProducer<T> prod){
    	this.conn=conn;
    	this.prod=prod;
    	LoggerService service = conn.getService(LoggerService.class);
    	if( service != null){
    		this.log=service.getLogger(getClass());
    	}
    }
    /** Combine the specified quantity matching the date range and selector.
     * All records that overlap the data range are processed.
     * Records that overlap the ends of the period can have their value rescaled appropriately.
     * Because of the possible re-scaling the result is always returned as a double 
     * @param op Reduction
     * @param type PropertyTag of property to Sum
     * @param start_prop PropertyTag defining the start of the record
     * @param end_prop ProeprtyTag defining the end of the record
     * @param start Start Date
     * @param end End Date
     * @param sel RecordSelector to select target records
     * @return sum of quantity
     * @throws Exception 
     */
    public double getOverlapSum(Reduction op,PropExpression<? extends Number> type,PropExpression<Date> start_prop, PropExpression<Date> end_prop,RecordSelector sel, Date start, Date end) throws Exception{
    	return getOverlapSum(op, type, start_prop, end_prop, sel, start, end,0L);
    }
    public double getOverlapSum(Reduction op,PropExpression<? extends Number> type,PropExpression<Date> start_prop, PropExpression<Date> end_prop,RecordSelector sel, Date start, Date end,long cutoff) throws Exception{
    	Number result = getOverlapSum(NumberReductionTarget.getInstance(op, type), start_prop, end_prop, sel, start, end,cutoff);
    	return result.doubleValue();
    }
    public Number getOverlapSum(NumberReductionTarget target,PropExpression<Date> start_prop, PropExpression<Date> end_prop,RecordSelector o_sel, Date start, Date end) throws Exception{
    	return getOverlapSum(target, start_prop, end_prop, o_sel, start, end,0L);
    }
    public Number getOverlapSum(NumberReductionTarget target,PropExpression<Date> start_prop, PropExpression<Date> end_prop,RecordSelector o_sel, Date start, Date end,long cutoff) throws Exception{
    		     	
    	if( ! prod.compatible(target.getExpression()) || ! prod.compatible(o_sel)){
    		return target.getDefault();
    	}
    	AndRecordSelector sel=new AndRecordSelector(o_sel);
    	final Period period = new Period(start, end);
    	//sel.add(new NullSelector(target.getExpression(), false)); //UsageManager may contain tables without type
    	if( USE_QUERY_MAPPER_FEATURE.isEnabled(conn) ){
    		try{
    			Number result=null;

    			if( USE_CASE_OVERLAP.isEnabled(conn)){
    				// do reduction in a single pass using a case statement
    				AndRecordSelector selector = new AndRecordSelector(sel);
    				
    				selector.add(new PeriodOverlapRecordSelector(period,start_prop,end_prop,OverlapType.ANY,cutoff));
    				result = prod.getReduction(makeCaseOverlapReductionTarget(target.getReduction(), target.getExpression(), start_prop, end_prop, start, end), selector).doubleValue();
    			}else{

    				//       log.debug(proj.getCode()+" "+start_secs+","+end_secs);
    				// First sum jobs totally within the period. 
    				AndRecordSelector inner=new AndRecordSelector(sel);
    				inner.add(new PeriodOverlapRecordSelector(period,start_prop,end_prop,OverlapType.INNER,cutoff));
    				if( target.getReduction() == Reduction.AVG){
    					// Do rescale in SQL
    					result = prod.getReduction(makeInnerAverageReductionTarget(target.getExpression(), start_prop, end_prop, start,
    							end),inner).doubleValue();
    				}else{
    					result = prod.getReduction(target,inner).doubleValue();	
    				}

    				// Now overlapps from the beginning
    				AndRecordSelector sel2=new AndRecordSelector(sel);
    				sel2.add(new PeriodOverlapRecordSelector(period, start_prop, end_prop,OverlapType.LOWER,cutoff));
    				result = addToOverlapSumByIterating(target, start_prop,
    						end_prop, period, result, sel2);
    				// log.debug("Front overlapp "+temp);

    				// This should be the slowest loop as it cannot use an index on end_prop
    				// to locate the end of the select so needs to search to the end of the table.
    				sel2=new AndRecordSelector(sel);
    				sel2.add(new PeriodOverlapRecordSelector(period, start_prop, end_prop,OverlapType.UPPER_OUTER,cutoff));

    				result = addToOverlapSumByIterating(target, start_prop,
    						end_prop, period, result, sel2);
    			}
    			return result;
    		}catch(NoSQLFilterException e){
    			// use fallback
    		}
    	}
    	AndRecordSelector select = new AndRecordSelector(sel);
    	select.add(new PeriodOverlapRecordSelector(period,start_prop,end_prop,OverlapType.ANY,cutoff));

    	// fallback result by iterating
    	Number result = target.getDefault();
    	result = addToOverlapSumByIterating(target, start_prop, end_prop,
				period, result, select);
    	return result;
    	
    }
	/**
	 * @param target
	 * @param start_prop
	 * @param end_prop
	 * @param start
	 * @param end
	 * @param result
	 * @param sel2
	 * @return
	 * @throws Exception
	 * @throws InvalidPropertyException
	 */
	private Number addToOverlapSumByIterating(NumberReductionTarget target,
			PropExpression<Date> start_prop, PropExpression<Date> end_prop,
			TimePeriod period, Number result, AndRecordSelector sel2)
			throws Exception, InvalidPropertyException {
		for(Iterator<T> it = prod.getIterator(sel2);it.hasNext();){
			T rec =   it.next();
			result = combinePartial(target,result, getOverlap(rec,target,start_prop,end_prop,period));
			rec.release();
		}
		return result;
	}

    /** Convert a numerical property into the ReductionTarget needed to calculate
     * the overlap contribution to the time-average of records totally within the overlap period.  
     * 
     * @param type
     * @param start_prop
     * @param end_prop
     * @param start
     * @param end
     * @return NumberReductionTarget
     * @throws IllegalReductionException
     * @throws PropertyCastException
     */
	public static NumberReductionTarget makeInnerAverageReductionTarget(
			PropExpression<? extends Number> type, PropExpression<Date> start_prop,
			PropExpression<Date> end_prop, Date start, Date end)
			throws IllegalReductionException, PropertyCastException {
		return NumberReductionTarget.getInstance(Reduction.SUM,
				new BinaryPropExpression(type,Operator.MUL , 
						new BinaryPropExpression(new DurationPropExpression(start_prop,end_prop), Operator.DIV, new ConstPropExpression<Double>(Double.class,(double)( end.getTime()-start.getTime())))
						)					
				);
	}
	/** Calculate expression for the overlap of a record and a period.
     * @param overlap_type
     * @param start_prop
     * @param end_prop
     * @param start
     * @param end
     * @return NumberReductionTarget
     * @throws IllegalReductionException
     * @throws PropertyCastException
     */
	public static DurationPropExpression makeOverlapExpression(OverlapType overlap_type,
			PropExpression<Date> start_prop,PropExpression<Date> end_prop,
			Date start, Date end)
			throws IllegalReductionException, PropertyCastException {
		switch(overlap_type){
		case INNER: return new DurationPropExpression(start_prop,end_prop);
		case LOWER: return new DurationPropExpression(new ConstPropExpression<Date>(Date.class, start),end_prop);
		case UPPER: return new DurationPropExpression(start_prop,new ConstPropExpression<Date>(Date.class,end));
		case OUTER: return new DurationPropExpression(new ConstPropExpression<Date>(Date.class, start), new ConstPropExpression<Date>(Date.class, end));
		default:
			throw new ConsistencyError("Illegal overlap type requested "+overlap_type);
		}
	}
	public static NumberReductionTarget makeOverlapReductionTarget(OverlapType overlap_type, Reduction red,
			PropExpression<? extends Number> type, PropertyTag<Date> start_prop,
			PropertyTag<Date> end_prop, Date start, Date end)
			throws IllegalReductionException, PropertyCastException {
		DurationPropExpression over = makeOverlapExpression(overlap_type, start_prop, end_prop, start, end);
		DurationPropExpression den;
		if( red == Reduction.AVG){
			den = new DurationPropExpression(new ConstPropExpression<Date>(Date.class, start), new ConstPropExpression<Date>(Date.class, end));
			red = Reduction.SUM;
		}else{
			den = new DurationPropExpression(start_prop,end_prop);
		}
		if( den.equals(over)){
			return NumberReductionTarget.getInstance(red,type);
		}else{
			return NumberReductionTarget.getInstance(red,
					new BinaryPropExpression(type,Operator.MUL , 
							new BinaryPropExpression(over, Operator.DIV, den)
							)					
					);
		}
	}
	private static PropExpression<? extends Number> opt( PropExpression<? extends Number> type, DurationPropExpression num, DurationPropExpression den) throws IllegalReductionException, PropertyCastException{
		if( num.equals(den)){
			return type;
		}else{
			return new BinaryPropExpression(type,Operator.MUL , 
							new BinaryPropExpression(num, Operator.DIV, den)
					);
		}
	}
	public NumberReductionTarget makeCaseOverlapReductionTarget( Reduction red,
			PropExpression<? extends Number> type, PropExpression<Date> start_prop,
			PropExpression<Date> end_prop, Date start, Date end)
			throws IllegalReductionException, PropertyCastException {
		
		DurationPropExpression den;
		if( red == Reduction.AVG){
			// Average becomes a time average
			den = new DurationPropExpression(new ConstPropExpression<Date>(Date.class, start), new ConstPropExpression<Date>(Date.class, end));
			red = Reduction.SUM;
		}else{
			// otherwise scale by fraction of record
			den = new DurationPropExpression(start_prop,end_prop);
		}
		Period period = new Period(start,end);
		return NumberReductionTarget.getInstance(red, 
			new CasePropExpression<Number>(Number.class, 
				opt(type,makeOverlapExpression(OverlapType.INNER, start_prop, end_prop, start, end),den),
				new CasePropExpression.Case<Number>(
						new PeriodOverlapRecordSelector(period,start_prop,end_prop,OverlapType.LOWER,-1L), 
						opt(type,makeOverlapExpression(OverlapType.LOWER, start_prop, end_prop, start, end),den)),
				new CasePropExpression.Case<Number>(
						new PeriodOverlapRecordSelector(period,start_prop,end_prop,OverlapType.UPPER,-1L), 
						opt(type,makeOverlapExpression(OverlapType.UPPER, start_prop, end_prop, start, end),den)),
				new CasePropExpression.Case<Number>(
						new PeriodOverlapRecordSelector(period,start_prop,end_prop,OverlapType.OUTER,-1L), 
						opt(type,makeOverlapExpression(OverlapType.OUTER, start_prop, end_prop, start, end),den))
			)
		);
					
	}
	
    /** sum the specified quantity matching date range and selector
     * grouped by the other specified property. 
     *  All records that overlap the data range are processed.
     * Records that overlap the ends of the period can have their value rescaled appropriately.
     * @param main_target 
     * @param <R>
     * @param tag
     * @param start_prop 
     * @param end_prop 
     * @param start
     * @param end
     * @param sel 
     * @return Map
     * @throws Exception 
     * @throws DataException 
     */
	public <R> Map<R, Number> getOverlapReductionMap(NumberReductionTarget main_target,
			PropExpression<R> tag, 
			PropExpression<Date> start_prop, PropExpression<Date> end_prop,
			Date start, Date end, 
			RecordSelector sel)
					throws Exception{
		return getOverlapReductionMap(main_target, tag, start_prop, end_prop, start, end, sel,0L);
	}
	public <R> Map<R, Number> getOverlapReductionMap(NumberReductionTarget main_target,
			PropExpression<R> tag, 
			PropExpression<Date> start_prop, PropExpression<Date> end_prop,
			Date start, Date end, 
			RecordSelector sel, long cutoff)
					throws Exception{
		AndRecordSelector selector=new AndRecordSelector(sel);
		selector.add(new NullSelector(tag, false));
		selector.add(new NullSelector(main_target.getExpression(), false));
		if( ! prod.compatible(selector) ){

			// no matching property
			return new HashMap<R,Number>();
		}
		Period period=new Period(start,end);
		if( USE_QUERY_MAPPER_FEATURE.isEnabled(conn)){

			try{
				//Logger log = conn.getService(LoggerService.class).getLogger(getClass());
				TimerService tim=conn.getService(TimerService.class);
				Map<R, Number> result;
				if( tim != null ){
					tim.startTimer("getOverlapMap");
				}
				if( USE_CASE_OVERLAP.isEnabled(conn)){
					// do reduction in a single pass using a case statement
					AndRecordSelector qs = new AndRecordSelector(selector);

					qs.add(new PeriodOverlapRecordSelector(period,start_prop,end_prop,OverlapType.ANY,cutoff));
					result = prod.getReductionMap(tag,makeCaseOverlapReductionTarget(main_target.getReduction(), main_target.getExpression(), start_prop, end_prop, start, end), qs);
				}else{
					


					AndRecordSelector inner=new AndRecordSelector();
					inner.add(selector);
					inner.add(new PeriodOverlapRecordSelector(period, start_prop, end_prop,OverlapType.INNER,cutoff));


					NumberReductionTarget target;
					if( main_target.getReduction() == Reduction.AVG){
						target = makeInnerAverageReductionTarget(main_target.getExpression(), start_prop, end_prop, start,
								end);
					}else{
						target = main_target;
					}
					result = prod.getReductionMap(tag, target, inner);


					//log.debug("getMap finder returns "+result.size());

					try{
						//TODO add option  to perform overlaps in SQL if we can create a SQL expression for the weight
						// Now overlapps from the beginning		
						if( tim != null ){
							tim.startTimer("getOverlapMap-Loop1");
						}
						AndRecordSelector sel2=new AndRecordSelector(selector);
						sel2.add(new PeriodOverlapRecordSelector(period, start_prop, end_prop,OverlapType.LOWER,cutoff));


						addToOverlapReductionMapByIterating(tag, start_prop,
								end_prop, period, target, result, sel2);
						//log.debug("First loop "+records+" of which "+over+" overlap");
						if( tim != null ){
							tim.stopTimer("getOverlapMap-Loop1");
							tim.startTimer("getOverlapMap-Loop2");
						}
						// This should be the slowest loop as it cannot use an index on end_prop
						// to locate the end of the select so needs to search to the end of the table.
						// unless end is constrained by the outer selector.
						sel2=new AndRecordSelector(selector);
						sel2.add(new PeriodOverlapRecordSelector(period, start_prop, end_prop,OverlapType.UPPER_OUTER,cutoff));
						addToOverlapReductionMapByIterating(tag, start_prop,
								end_prop, period, target, result, sel2);
						//log.debug("Both loop "+records+" of which "+over+" overlap");
						if( tim != null ){
							tim.stopTimer("getOverlapMap-Loop2");
						}
					}catch(InvalidPropertyException e){
						// should never happen
						throw new ConsistencyError("Impossible error",e);
					}
					
				}
				if( tim != null ){
					tim.stopTimer("getOverlapMap");
				}
				return result;
			}catch(NoSQLFilterException e){
				// fallback below
			}
		}
		
		//fallback by iterating
		Map<R,Number> result = new HashMap<R, Number>();
		AndRecordSelector it_sel = new AndRecordSelector(selector);
		it_sel.add(new PeriodOverlapRecordSelector(period,start_prop,end_prop,OverlapType.ANY,cutoff));
		addToOverlapReductionMapByIterating(tag, start_prop, end_prop, period,
				main_target, result, it_sel);
		return result;
	}

	/** Iterate over records adding results to a reduction map.
	 * @param tag
	 * @param start_prop
	 * @param end_prop
	 * @param start
	 * @param end
	 * @param target
	 * @param result
	 * @param sel2
	 * @throws Exception
	 * @throws InvalidPropertyException
	 */
	private <R> void addToOverlapReductionMapByIterating(PropExpression<R> tag,
			PropExpression<Date> start_prop, PropExpression<Date> end_prop,
			TimePeriod period, NumberReductionTarget target,
			Map<R, Number> result, AndRecordSelector sel2) throws Exception,
			InvalidPropertyException {
		for(Iterator<T> it = prod.getIterator(sel2);it.hasNext();){
			T rec =   it.next();
			// Make the distinction between a record that does not
			// overlap the period and a record where the overlap value is zero
			// this gives a true zero when a zero valued record overlaps and
			// a null if no record overlaps.
			if( overlaps(rec, start_prop, end_prop, period.getStart(),period.getEnd())){
				Number temp = getOverlap(rec,target,start_prop,end_prop,period);


				R key = rec.evaluateExpression(tag);
				assert(key != null);
				Number n = result.get(key);
				if( n == null){
					n=temp;
				}else{
					n = combinePartial(target, temp, n);
				}
				result.put(key, n);
			
			}
			rec.release();
		}
	}
	/**
	 * @param target
	 * @param temp
	 * @param n
	 * @return
	 */
	private Number combinePartial(NumberReductionTarget target, Number temp,
			Number n) {
		if( target.getReduction() == Reduction.SUM || target.getReduction() == Reduction.AVG){
			// Normally Average uses a special Number type. However for a time average we
			// use a weighted sum.
			return NumberOp.add(temp,n);
		}
		return target.combine(temp , n);
	}
	public Map<ExpressionTuple, ReductionMapResult> getOverlapIndexedReductionMap(
			Set<ReductionTarget> property,
			PropExpression<Date> start_prop, PropExpression<Date> end_prop, Date start, Date end,
			RecordSelector input_selector) throws Exception {
		return getOverlapIndexedReductionMap(property, start_prop, end_prop, start, end, input_selector,0L);
	}
	public Map<ExpressionTuple, ReductionMapResult> getOverlapIndexedReductionMap(
				Set<ReductionTarget> property,
				PropExpression<Date> start_prop, PropExpression<Date> end_prop, Date start, Date end,
				RecordSelector input_selector,long cutoff) throws Exception {
		Period period = new Period(start,end);
		
		
		Set<PropExpression> index_set = new HashSet<PropExpression>();
		AndRecordSelector selector=new AndRecordSelector();
		selector.add(input_selector);
		selector.add(new RelationClause<Date>(start_prop,MatchCondition.LT, end_prop));
		selector.add(new ReductionSelector(property));
		if( ! prod.compatible(input_selector)){
			if( log != null ){
				log.debug("selector not compatible");
			}
			return new HashMap<ExpressionTuple, ReductionMapResult>();
		}
		for(ReductionTarget target : property ){
			// This prevents a composite producer from querying only those
			// expressions that resolve. bad if you are doing a record count
			// or any other correlated query but forces the use of composite queries
			// if you have uncorrelated queries
			// 
			// selector.add(new NullSelector(target.getExpression(), false));

			if( target.getReduction()==Reduction.INDEX){
				index_set.add(target.getExpression());
		//		selector.add(new NullSelector(target.getExpression(), false));

			}
		}
		if( USE_QUERY_MAPPER_FEATURE.isEnabled(conn) ){

			try{
				TimerService tim=conn.getService(TimerService.class);
				if( tim != null ){
					tim.startTimer("getOverlapMap");
				}
				Map<ExpressionTuple, ReductionMapResult> result;
				Set<ReductionTarget> inner_properties = new LinkedHashSet<ReductionTarget>();
				Map<ReductionTarget,ReductionTarget> mapping = new HashMap<ReductionTarget, ReductionTarget>();
				try{
					if( USE_CASE_OVERLAP.isEnabled(conn)){
						for(ReductionTarget target : property ){
							if( target instanceof NumberReductionTarget){
								NumberReductionTarget replace = makeCaseOverlapReductionTarget(target.getReduction(), (PropExpression<? extends Number>)target.getExpression(), start_prop, end_prop, start, end);
								inner_properties.add(replace);
								mapping.put(replace,target);
							}else{
								inner_properties.add(target);
								mapping.put(target, target);
							}
						}
						AndRecordSelector inner=new AndRecordSelector();
						inner.add(selector);
						inner.add(new PeriodOverlapRecordSelector(period, start_prop, end_prop,OverlapType.ANY,cutoff));
						result = new HashMap<ExpressionTuple, ReductionMapResult>();
						Map<ExpressionTuple, ReductionMapResult>scratch = prod.getIndexedReductionMap(inner_properties, inner);
						//copy over to correct keys
						for(ExpressionTuple key : scratch.keySet()){
							ReductionMapResult data = scratch.get(key);
							ReductionMapResult new_data = new ReductionMapResult();
							for(ReductionTarget data_key : data.keySet()){
								ReductionTarget key2 = mapping.get(data_key);
								Object value = data.get(data_key);
								new_data.put(key2,value);
							}
							data.clear();
							result.put(key,new_data);
						}
						scratch.clear();
					}else{

						boolean has_avg=false;
						for(ReductionTarget target : property ){
							if( target.getReduction() == Reduction.AVG){
								// time average weighting
								NumberReductionTarget replace = NumberReductionTarget.getInstance(Reduction.SUM,
										new BinaryPropExpression(target.getExpression(),Operator.MUL , 
												new BinaryPropExpression(new DurationPropExpression(start_prop,end_prop), Operator.DIV, new ConstPropExpression<Double>(Double.class,(double)( end.getTime()-start.getTime())))
												)					
										);
								inner_properties.add(replace);
								has_avg=true;
								mapping.put(replace, target);
							}else{
								inner_properties.add(target);
								mapping.put(target, target);
							}
						}



						AndRecordSelector inner=new AndRecordSelector();
						inner.add(selector);
						inner.add(new PeriodOverlapRecordSelector(period, start_prop, end_prop,OverlapType.INNER,cutoff));

						if( has_avg){
							result = new HashMap<ExpressionTuple, ReductionMapResult>();
							Map<ExpressionTuple, ReductionMapResult>scratch = prod.getIndexedReductionMap(inner_properties, inner);
							//copy over to correct keys
							for(ExpressionTuple key : scratch.keySet()){
								ReductionMapResult data = scratch.get(key);
								ReductionMapResult new_data = new ReductionMapResult();
								for(ReductionTarget data_key : data.keySet()){
									new_data.put(mapping.get(data_key),data.get(data_key));
								}
								data.clear();
								result.put(key,new_data);
							}
							scratch.clear();
						}else{
							result = prod.getIndexedReductionMap(property, inner);
						}
						//log.debug("getMap finder returns "+result.size());

						try{
							// Now overlapps from the beginning		
							if( tim != null ){
								tim.startTimer("getOverlapMap-Loop1");
							}
							AndRecordSelector sel2=new AndRecordSelector(selector);
							sel2.add(new PeriodOverlapRecordSelector(period, start_prop, end_prop,OverlapType.LOWER,cutoff));

							int records = addToOverlapIndexedReductionByIterating(property,
									start_prop, end_prop, period, index_set, result,
									sel2);
							//log.debug("First loop "+records+" of which "+over+" overlap");
							if( tim != null ){
								tim.stopTimer("getOverlapMap-Loop1");
								tim.startTimer("getOverlapMap-Loop2");
							}
							// This should be the slowest loop as it cannot use an index on end_prop
							// to locate the end of the select so needs to search to the end of the table.
							sel2=new AndRecordSelector(selector);
							sel2.add(new PeriodOverlapRecordSelector(period, start_prop, end_prop,OverlapType.UPPER_OUTER,cutoff));
							records += addToOverlapIndexedReductionByIterating(property, start_prop, end_prop, period, index_set, result, sel2);

							//log.debug("Both loop "+records+" of which "+over+" overlap");
							if( tim != null ){
								tim.stopTimer("getOverlapMap-Loop2");
							}
						}catch(InvalidPropertyException e){
							// should never happen
							throw new ConsistencyError("Impossible error",e);
						}
					}
				}catch(InvalidPropertyException e){
					// DO it all by iteration
					result = new HashMap<ExpressionTuple, ReductionMapResult>();
					AndRecordSelector sel = new AndRecordSelector(selector);
					sel.add(new PeriodOverlapRecordSelector(period, start_prop, end_prop,OverlapType.ANY,cutoff));
					addToOverlapIndexedReductionByIterating(property, start_prop, end_prop, period, index_set, result, sel);
				}

				if( tim != null ){
					tim.stopTimer("getOverlapMap");
				}
				return result;
			}catch(NoSQLFilterException e){
				// go to fallback
			}
		}
		//fallback to iterating
		if( log != null ){
			log.debug("Fallback by iterating");
		}
		selector.add(new PeriodOverlapRecordSelector(period,start_prop,end_prop));
		Map<ExpressionTuple,ReductionMapResult>result = new HashMap<ExpressionTuple, ReductionMapResult>();
		addToOverlapIndexedReductionByIterating(property, start_prop, end_prop, period, index_set, result, selector);

		return result;
	}

	@SuppressWarnings("unchecked")
	private int addToOverlapIndexedReductionByIterating(
			Set<ReductionTarget> property, PropExpression<Date> start_prop,
			PropExpression<Date> end_prop, TimePeriod period,
			Set<PropExpression> index_set,
			Map<ExpressionTuple, ReductionMapResult> result,
			RecordSelector sel2) throws Exception {
		int records=0;

		//TODO Try to think of some clean way of skipping incompatible
		// nested UsageProducers. Add getReductionIterator to UsageProducer?
		// add new ReductionRecordSelector?
		for(Iterator<T> it = prod.getIterator(sel2);it.hasNext();){
			T rec =   it.next();
			
				ExpressionTuple key = new ExpressionTuple(index_set, rec);
				ReductionMapResult res = result.get(key);
				boolean made=false;
				if( res == null){
					res = new ReductionMapResult();
					made=true;
				}
				records++;
				boolean seen=false;
				for(ReductionTarget target : property){
					// Note we are using exception handling to identify unsupported expressions.
					// Though this IS expensive it is by far the cleanest way of exiting a recursive 
					// call tree.
					try{
						if( target.getReduction() == Reduction.SUM || target.getReduction() == Reduction.AVG){
							// Use ADD not combine as NumberAverageReductionTarget uses a special numeric type
							// to combine averages but we want to weight instead
							Number temp = getOverlap(rec,(NumberReductionTarget)target, start_prop, end_prop,period);
							res.put(target, NumberOp.add((Number)res.get(target), temp));
							seen=true;
						}else{
							res.put(target, target.combine(res.get(target), rec.evaluateExpression(target.getExpression())));
							if( target.getReduction() != Reduction.INDEX){
								seen=true;
							}
						}
					}catch(InvalidPropertyException e){
						// just skip this property.
						//conn.getService(LoggerService.class).getLogger(getClass()).warn("Bad proeprty", e);
					}
				}
				if( made && seen ){
					// only add in a new result if we 
					// have generated any data
					result.put(key, res);
				}
				rec.release();
		}
		return records;
	}
	

	 /** property proportional to fraction of record that overlaps period
	 * @param rec 
	 * @param target 
	 * @param start_prop 
	 * @param end_prop 
     * @param p {@link TimePeriod}
     * @return weighted value of property
	 * @throws InvalidPropertyException 
     */
    public static Number getOverlap(ExpressionTarget rec,NumberReductionTarget target,PropExpression<Date> start_prop,PropExpression<Date> end_prop,TimePeriod p) throws InvalidExpressionException {
    	Number tmp=rec.evaluateExpression(target.getExpression());
    	
    	if( tmp == null ){
    		return target.getDefault();
    	}
    	
    	double fac = getOverlappWeight(rec,start_prop,end_prop,p,target.getReduction());
    	assert(fac >= 0.0 && fac <=1.0);
    	if( fac == 0.0 ){
    		return target.getDefault();
    	}
    	
    	double res = fac * tmp.doubleValue();
    	
//    	if( res < 0){
//    		return  0.0;
//    		//throw new ConsistencyError("negative extensive property calculated");
//    	}
    	return res;
    
    }
    public static boolean overlaps(ExpressionTarget rec,PropExpression<Date> start_prop,PropExpression<Date> end_prop,Date start,Date end){
    	assert(end!=null);
    	assert(start!=null);
    	assert(end.after(start));
    	assert(start_prop != null && end_prop != null);
    	if( start_prop==null || end_prop == null || start ==  null || end == null ){
			return false;
		}
    	long start_time=start.getTime();
    	long end_time=end.getTime();
    	try{
    		
    		Date myend = rec.evaluateExpression(end_prop);
    		Date mystart = rec.evaluateExpression(start_prop);
    		long myend_time = myend.getTime();
    		long mystart_time=mystart.getTime();
    			if( mystart_time == 0L){
    				// bad record
    				return false;
    			}
    		
    		return  mystart_time < end_time && start_time < myend_time;
    		
    	}catch(InvalidExpressionException e){
    		return false;
    	}
    }
  
  

	/** Get the fraction of this record that overlaps with the specified period.
	 * The algorithm used depends on the requested operation.
	 * For SUM we weight by the fraction of the record that overlaps.
	 * For AVG we weight by the fraction of the period (a time average).
	 * @param rec 
	 * @param start_prop PropertyTag for record start 
	 * @param end_prop   PropertyTag for record end
	 * @param p_start    Period start
	 * @param p_end      Period end
	 * @param op   Reduction operation
	 * @return double fraction
	 */
    public static double getOverlappWeight(ExpressionTarget rec,PropExpression<Date> start_prop, PropExpression<Date> end_prop,TimePeriod p,Reduction op)  {
    	assert(start_prop != null);
    	assert(end_prop != null);
    	assert(p != null);
    	assert(p.getStart() != null);
    	assert(p.getEnd() != null );
    	assert(p.getEnd().after(p.getStart()));
    	if( start_prop==null || end_prop == null || p ==  null ){
			return 0.0;
		}
    	
    	if( op == Reduction.MIN || op == Reduction.MAX){
    		// should not really do these operations with overlap but this is the
    		// value to use if we do.
    		return 1.0;
    	}
    	try{
    		long start = p.getStart().getTime();
    		long end = p.getEnd().getTime();
 
    		long myend = rec.evaluateExpression(end_prop).getTime();
    		long mystart= rec.evaluateExpression(start_prop).getTime();
    		
    		
    		long ostart = mystart;
    		long oend = myend;

    		double fac;
    		if (mystart <= 0 || myend <= mystart || mystart >= end || start >= myend ){
    			return 0.0;
    		}


    		if (ostart < start) {
    			ostart = start;
    		}
    		if (oend > end) {
    			oend = end;
    		}
    		if( oend <= ostart){
    			return 0.0;
    		}
    		if( op == Reduction.SUM ){
    			fac = (double) (oend - ostart) / (double) (myend - mystart);
    		}else if ( op == Reduction.AVG){
    			fac = (double) (oend - ostart) / (double) (end - start);
    		}else{
    			fac = 1.0;
    		}
    		return fac;

    	}catch(InvalidExpressionException e){
    		return 0.0;
    	}
    }

}