// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/

package uk.ac.ed.epcc.safe.accounting.policy;

import uk.ac.ed.epcc.safe.accounting.UsageRecord;
import uk.ac.ed.epcc.safe.accounting.aggregation.AggregateUsageRecordFactory;
import uk.ac.ed.epcc.safe.accounting.aggregation.DailyAggregateUsageRecordFactory;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;

/** The AggregationPolicy creates aggregated usage records into a different table.
 * Most of the work is handled by the {@link AggregateUsageRecordFactory}.
 * This policy is unusual in that it does not generate any new properties only consume them.
 * 
 * Configuration Properties:
 * <ul>
 * <li> <b>AggregationPolicy.target.<i>table-name</i></b> defines a comma separated list of 
 * remote tables we are updating.
 * 
 * </ul>
 * 
 * @see AggregateUsageRecordFactory
 * @author spb
 * @deprecated Use the more general ListenerPolicy 
 */
@uk.ac.ed.epcc.webapp.Version("$Id: AggregationPolicy.java,v 1.29 2014/09/15 14:32:26 spb Exp $")

public class AggregationPolicy extends BaseUsageRecordPolicy{
    Logger log;
    AppContext ctx;
    private String list;

    private int updates;

	@Override
	public void postCreate(PropertyContainer props, UsageRecord rec)
			throws Exception {
		
		for(AggregateUsageRecordFactory fac : aggregators){
			if( fac != null ){
				if (fac.aggregate(rec)){
					updates++;
				}
			}
		}
	}
	@Override
	public void preDelete(UsageRecord rec) throws Exception {
		for(AggregateUsageRecordFactory fac : aggregators){
			if( fac != null ){
				fac.deAggregate(rec);
			}
		}
		
	}
	private AggregateUsageRecordFactory aggregators[];
	public PropertyFinder initFinder(AppContext ctx, PropertyFinder prev,
			String table) {
		log = ctx.getService(LoggerService.class).getLogger(getClass());
		this.ctx=ctx;
		list =ctx.getInitParameter("AggregationPolicy."+table, "");
		return null;
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.policy.BasePolicy#startParse(uk.ac.ed.epcc.safe.accounting.PropertyContainer)
	 */
	@Override
	public void startParse(PropertyContainer defaults) throws DataException,
	InvalidPropertyException {
		updates=0;
		// defer the creation of the AggregateUsageRecordFactory classes to the parse stage.
		// remember the master object created internally contains this policy
		// so if we don't delay creation we get infinite recursion.
		log.debug("AggregationPolicy list="+list);
		if( list != null && list.trim().length() > 0){
			String tables[] = list.trim().split(",");
			aggregators = new AggregateUsageRecordFactory[tables.length];
			for(int i=0;i<tables.length;i++){
				String aggregate_table=tables[i].trim();
				if( aggregate_table.length() > 0 ){
					log.debug("create "+aggregate_table);
					try {	
						aggregators[i] = ctx.makeObjectWithDefault(AggregateUsageRecordFactory.class, DailyAggregateUsageRecordFactory.class,aggregate_table);

					} catch (Throwable e) {
						ctx.error(e,"Error making AggregateUsageFactory list="+list+" table="+tables[i]);
						aggregators[i]=null;
					}
				}else{
					aggregators[i] = null;
				}
			}
		}
		

	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.policy.BasePolicy#endParse()
	 */
	@Override
	public String endParse() {
		StringBuilder sb = new StringBuilder();
		boolean seen=false;
		for( int i=0 ; i <  aggregators.length ; i++){
			AggregateUsageRecordFactory fac = aggregators[i];
			if( fac != null ){
				seen=true;
				sb.append("aggregator[");
				sb.append(i);
				sb.append("] raw_records=");
				sb.append(fac.getRawCounter());
				sb.append(" aggregates_fetched=");
				sb.append(fac.getFetchCounter());
				sb.append(" updates=");
				sb.append(updates);
				sb.append("\n");
				aggregators[i]=null;
			}
		}
		if( ! seen ){
			sb.append("No aggregation tables configured");
		}
		aggregators=null;
		updates=0;
		log.info(sb.toString());
		return "";
	}

}