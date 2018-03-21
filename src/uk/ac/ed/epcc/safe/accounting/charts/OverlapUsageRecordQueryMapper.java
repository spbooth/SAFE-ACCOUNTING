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
import java.util.Map;

import uk.ac.ed.epcc.safe.accounting.NumberReductionTarget;
import uk.ac.ed.epcc.safe.accounting.OverlapHandler;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.jdbc.expr.Reduction;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
/** QueryMapper that plots property data using the {@link OverlapHandler} overlap methods
 * Note that this may be inefficient if used in a TimeChart as the overlap records will be processed multiple times.
 * 
 * @author spb
 *
 * @param <K> type of key
 * @param <D> type of data
 */


public class OverlapUsageRecordQueryMapper<K,D extends Number> extends UsageRecordQueryMapper<K> {
    private final AppContext conn;
	private final PropExpression<K> key_prop;
    private final PropExpression<D> plot_prop;
    private final int set;
    private final PropExpression<Date> start_prop;
    private final PropExpression<Date> end_prop;
    private final RecordSelector sel;
    private final Reduction red;
    private final long cutoff;
	public OverlapUsageRecordQueryMapper(AppContext conn,RecordSelector sel,PropExpression<K> key_prop, Reduction red,PropExpression<D> plot_prop, PropExpression<Date> start_prop,PropExpression<Date> end_prop,long cutoff,PropertyKeyLabeller<K> lab) {
		super(lab);
		this.sel=sel;
		this.key_prop=key_prop;
		this.set=0;
		this.plot_prop=plot_prop;
		this.start_prop=start_prop;
		this.end_prop=end_prop;
		this.red=red;
		this.cutoff=cutoff;
		this.conn=conn;
	}
	public OverlapUsageRecordQueryMapper(AppContext conn,RecordSelector sel,int set, Reduction red,PropExpression<D> plot_prop, PropExpression<Date> start_prop,PropExpression<Date> end_prop,long cutoff,PropertyKeyLabeller<K> lab) {
		super(lab);
		this.sel=sel;
		this.key_prop=null;
		this.set=set;
		this.plot_prop=plot_prop;
		this.start_prop=start_prop;
		this.end_prop=end_prop;
		this.red=red;
		this.cutoff=cutoff;
		this.conn=conn;
	}

	public Map<Integer, Number> getOverlapMap(UsageProducer<?> o, Date start,
			Date end) {
		Map<Integer,Number> res = new HashMap<Integer,Number>();
		@SuppressWarnings("unchecked")
		OverlapHandler<?> handler = new OverlapHandler(labeller.getContext(), o);
		try{
			if( key_prop != null ){
				Map<K,? extends Number> dat= handler.getOverlapReductionMap(NumberReductionTarget.getInstance(red, plot_prop),key_prop
						, start_prop, end_prop, start, end, sel,cutoff);
				if( dat != null ){
					for(K key : dat.keySet()){
						int set = labeller.getSetByKey( key);
						Number prev = res.get(set);
						if( prev == null ){
							res.put(set,dat.get(key));
						}else{
							res.put(set, prev.doubleValue()+dat.get(key).doubleValue());
						}
					}
				}
			}else{
				res.put(set, handler.getOverlapSum(red,plot_prop, start_prop, end_prop, sel, start, end,cutoff));
			}
		} catch (Exception e) {
			conn.getService(LoggerService.class).getLogger(getClass()).error("error in PropertyQueryMapper",e);
		}
		return res;
	}
	
}