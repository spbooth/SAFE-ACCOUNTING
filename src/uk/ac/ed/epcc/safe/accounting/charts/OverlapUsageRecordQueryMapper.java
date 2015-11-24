// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.charts;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import uk.ac.ed.epcc.safe.accounting.NumberReductionTarget;
import uk.ac.ed.epcc.safe.accounting.OverlapHandler;
import uk.ac.ed.epcc.safe.accounting.Reduction;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;
/** QueryMapper that plots property data using the {@link OverlapHandler} overlap methods
 * Note that this may be inefficient if used in a TimeChart as the overlap records will be processed multiple times.
 * 
 * @author spb
 *
 * @param <K> type of key
 * @param <D> type of data
 */
@uk.ac.ed.epcc.webapp.Version("$Id: OverlapUsageRecordQueryMapper.java,v 1.5 2014/09/15 14:32:18 spb Exp $")

public class OverlapUsageRecordQueryMapper<K,D extends Number> extends UsageRecordQueryMapper<K> {
    private final AppContext conn;
	private final PropExpression<K> key_prop;
    private final PropExpression<D> plot_prop;
    private final int set;
    private final PropertyTag<Date> start_prop;
    private final PropertyTag<Date> end_prop;
    private final RecordSelector sel;
    private final Reduction red;
	public OverlapUsageRecordQueryMapper(AppContext conn,RecordSelector sel,PropExpression<K> key_prop, Reduction red,PropExpression<D> plot_prop, PropertyTag<Date> start_prop,PropertyTag<Date> end_prop,PropertyKeyLabeller<K> lab) {
		super(lab);
		this.sel=sel;
		this.key_prop=key_prop;
		this.set=0;
		this.plot_prop=plot_prop;
		this.start_prop=start_prop;
		this.end_prop=end_prop;
		this.red=red;
		this.conn=conn;
	}
	public OverlapUsageRecordQueryMapper(AppContext conn,RecordSelector sel,int set, Reduction red,PropExpression<D> plot_prop, PropertyTag<Date> start_prop,PropertyTag<Date> end_prop,PropertyKeyLabeller<K> lab) {
		super(lab);
		this.sel=sel;
		this.key_prop=null;
		this.set=set;
		this.plot_prop=plot_prop;
		this.start_prop=start_prop;
		this.end_prop=end_prop;
		this.red=red;
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
						, start_prop, end_prop, start, end, sel);
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
				res.put(set, handler.getOverlapSum(red,plot_prop, start_prop, end_prop, sel, start, end));
			}
		} catch (Exception e) {
			conn.error(e,"error in PropertyQueryMapper");
		}
		return res;
	}
	
}
