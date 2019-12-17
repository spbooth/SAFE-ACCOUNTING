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

import uk.ac.ed.epcc.safe.accounting.CountReduction;
import uk.ac.ed.epcc.safe.accounting.IndexReduction;
import uk.ac.ed.epcc.safe.accounting.NumberReductionTarget;
import uk.ac.ed.epcc.safe.accounting.ReductionMapResult;
import uk.ac.ed.epcc.safe.accounting.ReductionTarget;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTuple;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.NumberOp;
import uk.ac.ed.epcc.webapp.content.Operator;
import uk.ac.ed.epcc.webapp.jdbc.expr.Reduction;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
/** QueryMapper that plots property data based on a single DateProperty being within the target range
 * 
 * @author spb
 *
 * @param <K> type of key
 * @param <D> type of data
 */


public class PointUsageRecordQueryMapper<K,D> extends UsageRecordQueryMapper<K> {
    private final AppContext conn;
	private final PropExpression<K> key_prop;
    private final int set;
    private final PropExpression<D> plot_prop;
    private final PropExpression<Date> point_prop;
    private final Reduction red;
    private final RecordSelector sel;
    
	public PointUsageRecordQueryMapper(AppContext conn,RecordSelector sel,PropExpression<K> key_prop,Reduction red, PropExpression<D> plot_prop, PropExpression<Date> point_prop,PropertyKeyLabeller<K> lab) {
		super(lab);
		this.sel=sel;
		this.key_prop=key_prop;
		set=0;
		this.plot_prop=plot_prop;
		this.point_prop=point_prop;
		this.red=red;
		this.conn=conn;
	}
	public PointUsageRecordQueryMapper(AppContext conn,RecordSelector sel, int set,Reduction red, PropExpression<D> plot_prop, PropExpression<Date> point_prop,PropertyKeyLabeller<K> lab) {
		super(lab);
		this.sel=sel;
		this.key_prop=null;
		this.set=set;
		this.plot_prop=plot_prop;
		this.point_prop=point_prop;
		this.red=red;
		this.conn=conn;
	}
	public Map<Integer, Number> getOverlapMap(UsageProducer<?> o, Date start,
			Date end) {
		AndRecordSelector selector = new AndRecordSelector(sel);
		selector.add(new SelectClause<>(point_prop,MatchCondition.GT,start));
		selector.add(new SelectClause<>(point_prop,MatchCondition.LE,end));
		Map<Integer,Number> res = new HashMap<>();
		try{
			ReductionTarget<Number,?> sum_target;
			if( red.equals(Reduction.DISTINCT)) {
				sum_target = new CountReduction(plot_prop);
			}else {
				sum_target = NumberReductionTarget.getInstance(red,(PropExpression<? extends Number>) plot_prop);
			}
			if( key_prop == null ){
				Number n = o.getReduction(sum_target, selector);
				if( n != null ){  
					res.put(set,n);
				}
			}else{
				boolean old = o.setCompositeHint(true); // labeller may combine
				try {
					Set<ReductionTarget> req = new LinkedHashSet<>();
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
								res.put(set, sum_target.combine(prev, val));
							}
						}
					}
				}finally {
					o.setCompositeHint(old);
				}

			}
		} catch (Exception e) {
			conn.getService(LoggerService.class).getLogger(getClass()).error("error in PropertyQueryMapper",e);
		}
		return res;
	}

}