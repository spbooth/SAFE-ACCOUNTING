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
package uk.ac.ed.epcc.safe.accounting.charts;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.IndexReduction;
import uk.ac.ed.epcc.safe.accounting.NumberReductionTarget;
import uk.ac.ed.epcc.safe.accounting.OverlapHandler;
import uk.ac.ed.epcc.safe.accounting.Reduction;
import uk.ac.ed.epcc.safe.accounting.ReductionMapResult;
import uk.ac.ed.epcc.safe.accounting.ReductionTarget;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTuple;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.OverlapType;
import uk.ac.ed.epcc.safe.accounting.selector.PeriodOverlapRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.time.Period;
/** QueryMapper that plots property data based on the record being entirely within
 * the target range. note that this will net generate sensible plots by itself it
 * Any records overlapping the ends of the target period need to be added using a transform.
 * 
 * We are using the convention from {@link OverlapHandler} that AVG means a time average so that
 * in that case records need to be scaled by the fraction of the period they overlap. 
 * @author spb
 *
 * @param <K> type of key
 * @param <D> type of data
 */


public class InnerUsageRecordQueryMapper<K,D extends Number> extends UsageRecordQueryMapper<K> {
	private final AppContext conn;
	private final int set;
	private final PropExpression<K> key_prop;
    private final PropExpression<D> plot_prop;
    private final PropertyTag<Date> start_prop;
    private final PropertyTag<Date> end_prop;
    private final RecordSelector sel;
    private final Reduction red;
	public InnerUsageRecordQueryMapper(AppContext conn,RecordSelector sel,PropExpression<K> key_prop, Reduction red,PropExpression<D> plot_prop, PropertyTag<Date> start_prop,PropertyTag<Date> end_prop,PropertyKeyLabeller<K> lab) {
		super(lab);
		this.sel=sel;
		this.key_prop=key_prop;
		this.plot_prop=plot_prop;
		this.start_prop=start_prop;
		this.end_prop=end_prop;
		this.red=red;
		this.set=0;
		this.conn=conn;
	}
	public InnerUsageRecordQueryMapper(AppContext conn,RecordSelector sel,int set, Reduction red,PropExpression<D> plot_prop, PropertyTag<Date> start_prop,PropertyTag<Date> end_prop,PropertyKeyLabeller<K> lab) {
		super(lab);
		this.sel=sel;
		this.key_prop=null;
		this.plot_prop=plot_prop;
		this.start_prop=start_prop;
		this.end_prop=end_prop;
		this.red=red;
		this.set=set;
		this.conn=conn;
	}

	public Map<Integer, Number> getOverlapMap(UsageProducer<?> o, Date start,
			Date end) {
		
		AndRecordSelector selector = new AndRecordSelector(sel);
		selector.add(new PeriodOverlapRecordSelector(new Period(start,end), start_prop, end_prop,OverlapType.INNER,0L));
		
		Map<Integer,Number> res = new HashMap<Integer,Number>();
		try{
			NumberReductionTarget sum_target;
			if( red == Reduction.AVG){
				sum_target = OverlapHandler.makeInnerAverageReductionTarget(plot_prop, start_prop, end_prop, start, end);
			}else{
				sum_target = NumberReductionTarget.getInstance(red,plot_prop);
			}
			if( key_prop == null ){
				Number n =  o.getReduction(sum_target, selector);
				if( n != null){
					res.put(set,n);
				}
			}else{
				Set<ReductionTarget> req = new LinkedHashSet<ReductionTarget>();
				req.add(new IndexReduction(key_prop));
				req.add(sum_target);
				Map<ExpressionTuple, ReductionMapResult> dat = o.getIndexedReductionMap(req, selector);
				if( dat != null ){
					for(ExpressionTuple t : dat.keySet()){
						K key = t.get(key_prop);
						int set = labeller.getSetByKey( key);
						Number prev = res.get(set);
						Number val = (Number) dat.get(t).get(sum_target);
						if( prev == null ){
							res.put(set,val);
						}else{
							res.put(set, prev.doubleValue()+val.doubleValue());
						}
					}
				}
			}
		} catch (Exception e) {
			conn.getService(LoggerService.class).getLogger(getClass()).error("error in PropertyQueryMapper",e);
		}
		return res;
	}

}