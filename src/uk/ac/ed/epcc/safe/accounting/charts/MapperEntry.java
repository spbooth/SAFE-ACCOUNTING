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

import java.awt.Color;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;

import uk.ac.ed.epcc.safe.accounting.OverlapHandler;
import uk.ac.ed.epcc.safe.accounting.Reduction;
import uk.ac.ed.epcc.safe.accounting.UsageManager;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.UsageRecord;
import uk.ac.ed.epcc.safe.accounting.expr.Parser;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.NullSelector;
import uk.ac.ed.epcc.safe.accounting.selector.OverlapType;
import uk.ac.ed.epcc.safe.accounting.selector.PeriodOverlapRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.Feature;
import uk.ac.ed.epcc.webapp.charts.BarTimeChart;
import uk.ac.ed.epcc.webapp.charts.Chart;
import uk.ac.ed.epcc.webapp.charts.InvalidTransformException;
import uk.ac.ed.epcc.webapp.charts.PeriodChart;
import uk.ac.ed.epcc.webapp.charts.PeriodPlot;
import uk.ac.ed.epcc.webapp.charts.PeriodSequencePlot;
import uk.ac.ed.epcc.webapp.charts.PeriodSetPlot;
import uk.ac.ed.epcc.webapp.charts.PieTimeChart;
import uk.ac.ed.epcc.webapp.charts.SingleValueSetPlot;
import uk.ac.ed.epcc.webapp.charts.TimeChart;
import uk.ac.ed.epcc.webapp.charts.strategy.SetRangeMapper;
import uk.ac.ed.epcc.webapp.config.ConfigService;
import uk.ac.ed.epcc.webapp.config.FilteredProperties;
import uk.ac.ed.epcc.webapp.content.FormatProvider;
import uk.ac.ed.epcc.webapp.content.Labeller;
import uk.ac.ed.epcc.webapp.jdbc.filter.CannotUseSQLException;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.time.Period;
import uk.ac.ed.epcc.webapp.time.TimePeriod;

/** MapperEntry represents the mapping of UsageRecords to plot set in a chart
 * 
 * 
 * This class also holds the logic to build charts based on {@link PlotEntry} {@link UsageProducer} and 
 * {@link RecordSelector}. Each combination of a MapperEntry and a PlotEntry adds an additional 
 * dataset to the chart. If multiple properties are to be added then this should be performed as
 * multiple datasets.
 * 
 * Note that the chart quantity is set from the PlotEntry (if not already set). Therefore by default
 * if multiple PlotEntries are added the first one will set the chart Quantity.
 *  
 * @author spb
 *
 */
public abstract class MapperEntry implements Contexed,Cloneable{
	private static final Feature USE_OVERLAP_HANDLER_IN_TIMECHART = new Feature("use_overlap_handler_in_timechart", false, "Use the OverlapHandler for timecharts instead of ierating over overlaps");
	public static final String GROUP_ENTRY_BASE = "GroupEntry";
	private final String name;
	private final String description;
    protected final AppContext conn;
    private Color custom_colour[] = null;
    private boolean use_line=false;
    private boolean cumulative=false;
    private boolean stacked=true;
    
    public MapperEntry(AppContext c,String name,String description){
    	conn=c;
    	this.name=name;
    	this.description=description;
    }
    public AppContext getContext(){
    	return conn;
    }
    private Logger getLogger(){
    	return getContext().getService(LoggerService.class).getLogger(getClass());
    }
    public void setColours(Color custom[]){
    	custom_colour=custom;
    }
    public void setUseLine(boolean use){
    	use_line=use;
    }
    /** Should the data be converted to a cummulative plot
	 * 
	 * @return boolean
	 */
	public boolean isCumulative() {
		return cumulative;
	}
	public void setCumulative(boolean cumulative) {
		this.cumulative = cumulative;
	}
    public String getName(){
    	return name;
    }
	/** Should the data be converted to a stacked plot
	 * 
	 * @return boolean
	 */
	public boolean isStacked() {
		return stacked;
	}
	public void setStacked(boolean stacked) {
		this.stacked = stacked;
	}
    public String getDescription(){
        return description;
    }
    public abstract SetRangeMapper getMapper(PlotEntry e);
    /** Get a Query mapper that combined records selected on a single Date Property
     * No ovrelapp is considered.
     * 
     * @param sel
     * @param red
     * @param prop_tag
     * @param end_prop
     * @return
     */
    protected abstract UsageRecordQueryMapper getPointQueryMapper(RecordSelector sel,
			Reduction red,PropExpression<? extends Number> prop_tag, PropertyTag<Date> end_prop) throws CannotUseSQLException;
    /** Get a Query mapper that combined records based on the overlap of records 
     * defined by a pair of properties as defined in {@link OverlapHandler}
     * @param s
     * @param red
     * @param prop_tag
     * @param start_prop
     * @param end_prop
     * @return
     */
    protected abstract UsageRecordQueryMapper getOverlapQueryMapper(
			RecordSelector s,Reduction red,
			PropExpression<? extends Number> prop_tag, PropertyTag<Date> start_prop,
			PropertyTag<Date> end_prop) throws CannotUseSQLException;
    /** Get a QueryMapper that combined records completely within a specified period
     * This is intended to be used as part of an overlap calculation.
     * 
     * @param sel
     * @param red
     * @param prop_tag
     * @param start_prop
     * @param end_prop
     * @return
     */
    protected abstract UsageRecordQueryMapper getInnerQueryMapper(RecordSelector sel,
			Reduction red,PropExpression<? extends Number> prop_tag,
			PropertyTag<Date> start_prop, PropertyTag<Date> end_prop) throws CannotUseSQLException;
	
    /** Get the cutoff (longest record length) to use for
	 * records overlapping a time period.
	 * A zero cutoff means not known
	 * @param e
	 * @param period
	 * @param prod
	 * @return
	 */
	private long getCutoff(PlotEntry e,TimePeriod period,UsageProducer<?> prod){
		
			// use cutoff configured into PlotEntry if nay
			return e.getCutoff();
	
	}
	
    /** Get a Selector that includes the date range.
     * If the start_prop is defined this will be any overlapping record.
     * If only the end_prop is defined then we match records where this time is within the
     * period.
     * 
     * @param e PlotEntry specifying the boundary props
     * @param cutoff
     * @param start
     * @param end
     * @param sel
     * @return RecordSelector
     */
    private RecordSelector getRangeSelector(PlotEntry e,boolean allow_overlap,long cutoff, Date start, Date end, RecordSelector sel){
    	AndRecordSelector res = new AndRecordSelector(sel);
    	if( e.getStartProperty() != null && allow_overlap){
    		res.add(new PeriodOverlapRecordSelector(new Period(start,end), e.getStartProperty(), e.getEndProperty(),OverlapType.ANY,cutoff));
    	}else{
    		res.add(new SelectClause<Date>(e.getEndProperty(), MatchCondition.GT, start));
    		res.add(new SelectClause<Date>(e.getEndProperty(), MatchCondition.LE, end));
    	}
    	return res;
    	
    }
   

    public boolean compatible(UsageProducer<?> ap){
    	return true;
    }
    protected Vector<String> getLabels(){
    	return null;
    }
    /** Generate a dataset for a PieTimeChart using this mapper.
     * 
     * @param e
     * @param tc
     * @param ap
     * @param sel
     * @param nplots
     * @param overlap
     * @return dataset or null if no data
     * @throws InvalidTransformException
     */
    public PeriodSetPlot makePieTimeChartPlot(PlotEntry e, PieTimeChart tc, UsageProducer ap,RecordSelector sel, int nplots,boolean overlap) throws InvalidTransformException {
        if( ! (e.compatible(ap) && compatible(ap))){
        	return null;
        }
    	
    	
        PeriodSetPlot ds= tc.addPieChart(0,custom_colour);
      
        boolean data_added = addData(e,tc, ap, sel, ds,overlap);
        if( data_added){
        	return ds;
        }
        return null;
    }
    /** plot data on a PieTimeChart using this mapper
     * @param e 
     * @param tc  PieTimeChart
     * @param ap 
     * @param sel 
 
     * @param nplots int max number of plots
     * @param overlap 
     * @return boolean true of ok
     * @throws InvalidTransformException
     */
    public boolean plot(PlotEntry e, PieTimeChart tc, UsageProducer ap,RecordSelector sel, int nplots,boolean overlap) throws InvalidTransformException {
        SingleValueSetPlot ds = makePieTimeChartPlot(e, tc, ap, sel, nplots, overlap);
   
        
       
    	if( ds != null ){
    		plotPieTimeChart(e, tc, nplots, ds);
    		
        	return true;
    	}
    	return false;
    }
	public void plotPieTimeChart(PlotEntry e, PieTimeChart tc, int nplots,
			SingleValueSetPlot ds) {
		Vector<String> labels = getLabels();
		setQuant(e.getLabel(), tc);
		setLegendName(tc);
		float scale = (float) e.getScale();
		if( scale != 1.0 ){
			ds.scale(scale);
		}
		if( labels != null ){
			ds.setLegends((String[]) labels.toArray(new String[labels.size()]));
		}
		
		tc.sortSets(ds,nplots);
	}

	protected void setQuant(String quant, PeriodChart tc) {
		String current_quant = tc.getChartData().getQuantityName();
		if( (current_quant == null || current_quant.trim().length()==0) && quant != null ){
		    tc.getChartData().setQuantityName(quant);
		}
	}
	
	public PeriodSetPlot makeBarTimeChartPlot(PlotEntry e, BarTimeChart tc, UsageProducer ap,RecordSelector sel, int nplots,boolean allow_overlap) throws InvalidTransformException {
        if( ! (e.compatible(ap) && compatible(ap))){
        	return null;
        }
    	
    	
        PeriodSetPlot ds= tc.addBarChart(0);
      
        boolean data_added = addData(e,tc, ap, sel, ds,allow_overlap);
        if( data_added){
        	return ds;
        }
        return null;
	}
    /** plot data on a PieTimeChart using this mapper
     * @param e 
     * @param tc  PieTimeChart
     * @param ap 
     * @param sel 
 
     * @param nplots int max number of plots
     * @param allow_overlap 
     * @return boolean true of ok
     * @throws InvalidTransformException
     */
    public boolean plot(PlotEntry e, BarTimeChart tc, UsageProducer ap,RecordSelector sel, int nplots,boolean allow_overlap) throws InvalidTransformException {
        
    	SingleValueSetPlot ds = makeBarTimeChartPlot(e, tc, ap, sel, nplots, allow_overlap);
       
    	if( ds != null ){
    		plotBarTimeChart(e, tc, nplots, ds);
        	return true;
    	}
    	return false;
    }
	public void plotBarTimeChart(PlotEntry e, BarTimeChart tc, int nplots,
			SingleValueSetPlot ds) {
		Vector<String> labels=getLabels();
		setQuant(e.getLabel(), tc);
		setLegendName(tc);
		float scale = (float) e.getScale();
		if( scale != 1.0 ){
			ds.scale(scale);
		}
		if( labels != null ){
		   ds.setLegends((String[]) labels.toArray(new String[labels.size()]));
		}
		if( nplots > 0 ){
			//if( nplots > 0 && nplots < ds.getNumSets()){
			// Don't need to sort barcharts but its the only
			// way at present to reduce the number of bars
			//TODO consider a non sorting reduction
			tc.sortSets(ds,nplots);
		}
	}
	@SuppressWarnings("unchecked")
	private boolean addData(PlotEntry e, PeriodChart tc, UsageProducer ap,
			RecordSelector sel, PeriodPlot ds,boolean allow_overlap) throws InvalidTransformException {
		
		boolean data_added=false;
        // create dataset, don't add labels yet as labels
		// vector may grow as data added
		PropExpression<? extends Number> prop_tag = e.getPlotProperty();
		PropertyTag<Date> start_prop = e.getStartProperty();
		PropertyTag<Date> end_prop = e.getEndProperty();
		Reduction red = e.getReduction();
		long cutoff = getCutoff(e, tc.getPeriod(), ap);
        boolean query_mapper_on = OverlapHandler.USE_QUERY_MAPPER_FEATURE.isEnabled(conn);
       
        if( query_mapper_on ){
        	query_mapper_on = conn.getBooleanParameter(ap.getTag()+".use_query_mapper",true);
        }
        
        RecordSelector s =getRangeSelector(e, allow_overlap,cutoff, tc.getStartDate(), tc.getEndDate(), sel);
		if( query_mapper_on  ){ //use fmapper if it exists for piecharts
			try{
        	UsageRecordQueryMapper fmapper;
        	if( start_prop != null && allow_overlap){
        		fmapper = getOverlapQueryMapper(s,red, prop_tag, start_prop,
						end_prop);
        	}else{
        		fmapper = getPointQueryMapper(sel, red,prop_tag, end_prop);
        	}
        	return ds.addMapData( fmapper, ap);
			}catch(CannotUseSQLException e1){
				// default to iterating
				
			}
        }
		SetRangeMapper map = getMapper(e);
		Iterator iter;
		try {
			iter = ap.getIterator(s);
			if( iter.hasNext()){
				tc.addDataIterator(ds, map, iter);
				data_added=true;
			}
		} catch (Exception e1) {
			getLogger().error("Error making iterator",e1);
		} 

		return data_added;
	}
	
    public boolean plot(boolean graph_transform,PlotEntry e, PeriodChart tc, UsageProducer ap,RecordSelector sel, int nplots,boolean allow_overlap) throws Exception {
		if( tc instanceof TimeChart){
			return plot(graph_transform,e,(TimeChart)tc,ap,sel,nplots,allow_overlap);
		}
		if( tc instanceof PieTimeChart){
			return plot(e,(PieTimeChart)tc,ap,sel,nplots,allow_overlap);
		}
		if( tc instanceof BarTimeChart){
			return plot(e,(BarTimeChart)tc,ap,sel,nplots,allow_overlap);
		}
		return false;
	}
	/** plot data on a TimeChart using this SimpleMapper
	 * @param graph_transforms
     * 					should transforms that only make sense graphically be applied
	 * @param e 
	 * 			PlotEntry
     * @param tc 
     * 			timeChart
     * @param ap 
     * 			UsageProducer
     * @param sel 
     *			RecordSelector
     * @param nplots 
     * 					int max number of plots
	 * @param allow_overlap 
  
     * @return boolean true of ok
	 * @throws Exception 
     */
    public boolean plot(boolean graph_transforms,PlotEntry e, TimeChart<?> tc, UsageProducer<?> ap,RecordSelector sel, int nplots,boolean allow_overlap) throws Exception {
    	return plot(e,tc,ap,sel,nplots,allow_overlap,use_line,graph_transforms);
    }
    public boolean plot(PlotEntry e, TimeChart<?> tc, UsageProducer<?> ap,RecordSelector sel, int nplots,boolean allow_overlap) throws Exception {
    	return plot(e,tc,ap,sel,nplots,allow_overlap,use_line,true);
    }
    /** Make a dataset for a TimeChart and plot data to it using this transform.
     * 
     * @param e
     * @param tc
     * @param ap
     * @param request_sel
     * @param nplots
     * @param allow_overlap
     * @param use_line
     * @param graph_transforms
     * @return DataSet or null if no data added
     * @throws Exception 
     */
    @SuppressWarnings("unchecked")
    public <P extends PeriodSequencePlot> P makeTimeChartPlot(PlotEntry e, TimeChart<P> tc, UsageProducer<?> ap,RecordSelector request_sel, int nplots,boolean allow_overlap,boolean use_line,boolean graph_transforms) throws Exception {
    	PropExpression<? extends Number> prop_tag = e.getPlotProperty();
		
    	if( prop_tag == null ){
         	return null;
        }
    	// This ensures we have the plot property.
    	// For a mixed table UsageProducer it lets tables that don't define
    	// the needed prop skip 
    	AndRecordSelector sel;
    	sel = modifySelector(request_sel, prop_tag);
    	if(  ! compatible(ap)){
         	return null;
         }
        
    	P ds= (P) tc.makeDataset(0);
       
        boolean data_added = false;
        if( ap instanceof UsageManager){
        	// as addTimeChartData involves multiple queries.
        	// do each nested producer in turn. This should make things easier for the
        	// underlying database
        	for(UsageProducer<?> up : ((UsageManager<?>)ap).getProducers(UsageProducer.class)){
        		if( compatible(up) && up.compatible(sel)){
        			if( addTimeChartData(e,tc, up, sel, ds,allow_overlap)){
        				data_added=true;
        			}
        		}
        	}
        }else{
        	data_added = addTimeChartData(e,tc, ap, sel, ds,allow_overlap);
    	}
        if( data_added){
        	 //TODO: could combine scale factors with expression
        	// keeping backwards compat but removing data scaling
        	if( graph_transforms && e.isRateScale()){
    			ds.rateScale( e.getTimeScale()*e.getScale());
    		}else{
    			float scale = (float) e.getScale();
    			if( scale != 1.0 ){
    				ds.scale(scale);
    			}
    		}
        	PlotEntry s = e.getScaleEntry();
    		if( s != null ){
    			SetMapperEntry se = new SetMapperEntry(conn, "norm", "normalisation");
    			P norm = se.makeTimeChartPlot(s, tc, ap, request_sel, nplots, allow_overlap, use_line, graph_transforms);
    			if( norm != null ){
    				ds.datasetScale( 1.0, norm);
    			}else{
    				// better no data than not applying norm and getting different
    				// result than asked.
    				return null;
    			}
    		}
        	return ds;
        }
        return null;
    }
    /** Add a dataset to a TimeChart using the category data from this mapper. 
     * 
     * @param e
     * @param tc
     * @param ds
     * @param nplots
     * @param use_line
     * @param graph_transforms
     * @throws Exception 
     */
    public <P extends PeriodSequencePlot>void plotTimeChart(PlotEntry e,TimeChart<P> tc, P ds, int nplots,boolean use_line, boolean graph_transforms) throws Exception{
    	if(  isCumulative()){ 
			tc.getChartData().setCumulative(true);
    	}
    	Vector<String> labels=getLabels();
		 if( use_line ){
	        	tc.addLineGraph(ds,custom_colour);
	        }else{
	            tc.addAreaGraph(ds,custom_colour);
	        }
		if( labels != null ){
			ds.setLegends((String[]) labels.toArray(new String[labels.size()]));
		}
		
		tc.sortSets(ds,nplots);
		
		String quant=e.getLabel();
		if(  isCumulative()){ 
			//should we switch this on graphical_transforms
			// the Plot label would be wrong at least
			ds.scaleCumulative(1.0, new double[ds.getNumSets()]);
		}else if( graph_transforms && e.isRateScale()){
			String time_unit = e.getTimeUnit();
			if( time_unit != null ){
				quant = time_unit;
			}
		}
		setQuant(quant,tc);
		setLegendName(tc);
		if(graph_transforms &&  isStacked()){
			ds.doConvertToStacked();
		}
    }
    /** make and plot a single dataset.
     * 
     * @param e
     * @param tc
     * @param ap
     * @param request_sel
     * @param nplots
     * @param allow_overlap
     * @param use_line
     * @param graph_transforms
     * @return Plot
     * @throws Exception 
     */
	public  <P extends PeriodSequencePlot> boolean plot(PlotEntry e, TimeChart<P> tc, UsageProducer<?> ap,RecordSelector request_sel, int nplots,boolean allow_overlap,boolean use_line,boolean graph_transforms) throws Exception {
		
    	P ds = makeTimeChartPlot(e, tc, ap, request_sel, nplots, allow_overlap, use_line, graph_transforms);
    	
    	if( ds != null ){
    		plotTimeChart(e, tc, ds, nplots, use_line,graph_transforms);
    		return true;
    	}
    	return false;
    
    }

	private AndRecordSelector modifySelector(RecordSelector request_sel,
			PropExpression<? extends Number> prop_tag) {
		AndRecordSelector sel;
		sel = new AndRecordSelector(request_sel);
    	sel.add(new NullSelector(prop_tag, false));
		return sel;
	}

	protected void setLegendName(Chart tc) {
		if( tc.getLegendName()==null && description != null && description.trim().length() > 0){
		 	tc.setLegendName(description);
		}
	}
	@SuppressWarnings("unchecked")
	private boolean addTimeChartData(PlotEntry e, TimeChart tc, UsageProducer ap, RecordSelector sel,
			PeriodSequencePlot ds,boolean allow_overlap) throws InvalidTransformException {
		PropExpression<? extends Number> prop_tag = e.getPlotProperty();
		PropertyTag<Date> start_prop = e.getStartProperty();
		PropertyTag<Date> end_prop = e.getEndProperty();
		Reduction red = e.getReduction();
		long cutoff = getCutoff(e, tc.getPeriod(), ap);
		boolean data_added=false;
        Logger log = conn.getService(LoggerService.class).getLogger(getClass());
        log.debug(" params end="+tc.getEndDate()+" start="+tc.getStartDate());
     // create dataset, don't add labels yet as labels
		// vector may grow as data added
    	boolean query_mapper_on = OverlapHandler.USE_QUERY_MAPPER_FEATURE.isEnabled(conn);
    	if( query_mapper_on  ){
    		try{
    			log.debug("using fmapper");
    			if( start_prop == null || ! allow_overlap){
    				log.debug("using point");
    				UsageRecordQueryMapper fmapper;
    				fmapper = getPointQueryMapper(sel,red, prop_tag, end_prop);
    				data_added = tc.addMapData(ds, fmapper, ap);
    			}else{
    				log.debug("using overlap");
    				if( USE_OVERLAP_HANDLER_IN_TIMECHART.isEnabled(conn)){
    					log.debug("using overlap handler");
    					// This might be faster if the OverlapHandler is using a CaseExpression 
    					// less SQL queries but more complex.
    					// however they are also ANY overlap queries so at the very least we need auto_cutoff 
    					// in place 
    					UsageRecordQueryMapper fmapper;
    					fmapper = getOverlapQueryMapper( sel,red, prop_tag,
    							start_prop,end_prop);
    					data_added = tc.addMapData(ds, fmapper, ap);
    				}else{
    					// Because we have adjacent sequences, 
    					// rather than use the methods in OverlapHander we can use a single
    					// iteration phases for all periods the record overlaps.

    					// start with just the inner regions
    					UsageRecordQueryMapper fmapper;
    					fmapper = getInnerQueryMapper(sel, red,prop_tag, start_prop,
    							end_prop);
    					data_added = tc.addMapData(ds, fmapper, ap);
    					// overlap with sub-region start
    					SetRangeMapper map = getMapper(e); // note this uses same pkl as previous step
    					for(Iterator<TimePeriod> it = ds.getSubPeriods() ; it.hasNext() ;){
    						TimePeriod p = it.next();
    						Date start = p.getStart();
    						Date end = p.getEnd();
    						AndRecordSelector selector = new AndRecordSelector(sel);
    						selector.add(new PeriodOverlapRecordSelector(p, start_prop, end_prop,OverlapType.LOWER,cutoff));
    						// REcords that overlap the start of the period but end within the period
    						// (make sure we don't overcount)

    						try {
    							Iterator<UsageRecord> iter = ap.getIterator(selector);
    							if( iter.hasNext()){
    								tc.addDataIterator(ds, map, iter);
    								data_added=true;
    							}
    						} catch (Exception e1) {
    							getLogger().error("Error making iterator",e1);
    						} 
    					}

    					// final (expensive) overlap with end
    					// use full timechart period for OUTER records
    					AndRecordSelector selector = new AndRecordSelector(sel);
    					selector.add(new PeriodOverlapRecordSelector(new Period(tc.getPeriod()), start_prop, end_prop, OverlapType.UPPER_OUTER, cutoff));

    					try {
    						Iterator<UsageRecord> iter = ap.getIterator(selector);
    						if( iter.hasNext()){
    							tc.addDataIterator(ds, map, iter);
    							data_added=true;
    						}
    					} catch (Exception e1) {
    						getLogger().error("Error making iterator",e1);
    					} 
    				}
    			}
    			return data_added;
    		}catch(CannotUseSQLException e1){
    			if( data_added ){
    				getLogger().error("MapperEntry aborted after adding data",e1);
    			}
    			// default to iterating
    		}
    	}
		//log.debug("using transform");
		SetRangeMapper map = getMapper(e);
		Iterator iter;
		try {
			iter = ap.getIterator(getRangeSelector(e,allow_overlap,cutoff,tc.getStartDate(), tc.getEndDate(),sel));
			if( iter.hasNext()){
				tc.addDataIterator(ds, map, iter);
				data_added=true;
			}
		} catch (Exception e1) {
			getLogger().error("Error making iterator",e1);
		} 


    	//log.debug("data added "+data_added);
		return data_added;
	}
	
//    @SuppressWarnings("unchecked")
//	public  Table getTable(PlotEntry e, PeriodChart tc,  UsageProducer ap,RecordSelector sel,boolean allow_overlap) throws InvalidTransformException {
//    	
//    	if(  !( e.compatible(ap) && compatible(ap))){
//         	return new Table();
//         }
//    	boolean data_added=false;
//    	
//        Plot ds= tc.makeDataset(0);
//        if( tc instanceof SetPeriodChart){
//        	data_added = addData(e,(SetPeriodChart)tc, ap, sel, ds,allow_overlap);
//        }else if( tc instanceof TimeChart){
//        	data_added = addTimeChartData(e,(TimeChart)tc, ap, sel, (SplitSetPlot) ds,allow_overlap);
//        	if( e.isCumulative()){
//        		((TimeChart)tc).scaleCumulative((SplitSetPlot)ds, e.getScale());
// 	        }
//        }
//        Vector<String> labels=getLabels();
//       
//        
//    	if( data_added ){
//    		if( labels != null ){
//               tc.setLegends((String[]) labels.toArray(new String[labels.size()]));
//    		}
//            ds.sortSets(0);
//            
//            
//            	// If we have a quantity label use that. 
//            	// OTherwise use the PlotEntry descrioption 
//            	String lab = e.getLabel();
//            	if( lab != null ){
//            	   tc.setQuantityName(lab);
//            	}else{
//            		tc.setQuantityName(e.getDescription());
//            	}
//            
//    	}
//         Table t = tc.getTable(ds);
//         
//         if( description != null && description.trim().length() > 0){
//        	 t.setKeyName(description);
//         }
//         return t;
//    }
    
    
    /** Generate a HTML selector for the possible Mappers
     * 
     * @param plot_type
     * @param list  Vector of MapperEntry
     * @return String HTML fragment selecting Mappers
     */
   public static String MapSelector(String plot_type, Set<MapperEntry> list) {
       
       String result="";
       
       boolean first=true;
       for(Iterator it=list.iterator();it.hasNext();){
           MapperEntry me = (MapperEntry) it.next();
           result += "<tr><td><input type=\"radio\" name=\"plot_type\" value=\"";
           result += me.getDescription();
           result += "\"";
           if( (plot_type==null && first) ||( plot_type != null && plot_type.equals(me.getDescription()))){
               result += " checked ";
           }    
           result += "> "+me.getDescription()+" </td></tr>";
           first=false;
       }
       return result;
   }
   public static String getMapType(String plot_type, Set<MapperEntry> list) {
	 
       if( plot_type == null && ! list.isEmpty() ){
           plot_type=list.iterator().next().getDescription();
       }
       return plot_type;
   }
   /** parse the HTML request for the possible mappers.
    * 
    * @param plot_type
 * @param list Vector of MapperEntry
    * @return MapperEntry
    */
   public static MapperEntry parseMapSelector(String plot_type, Set<MapperEntry> list) {
       
       for(Iterator it=list.iterator();it.hasNext();){
           MapperEntry me = (MapperEntry) it.next();
           if( plot_type == null || plot_type.equals(me.getDescription())){
               return me;
           }

       }
       return null;
       
   }
   /** Get the set of MappeEntrys defined by the config properties
 * @param finder 
    * 
    * @param c  AppContext
 * @param mode 
    * @return Set of MapperEntry
    */
   public static Set<MapperEntry> getMapperSet(PropertyFinder finder, AppContext c,String mode) {
	   FilteredProperties prop = new FilteredProperties(c.getService(ConfigService.class).getServiceProperties(), GROUP_ENTRY_BASE,mode);
		   
		  
		   Set<MapperEntry> set = new LinkedHashSet<MapperEntry>();
		   
		   String list = prop.getProperty("list");
		   if( list != null ){
			   for(String name : list.split(",")){
				   try{
					   set.add(getConfigMapperEntry(c, prop,finder, name));
				   }catch(Exception e){
					   c.getService(LoggerService.class).getLogger(MapperEntry.class).error("Error making MapperEntry "+name,e);
				   }
			   }
		   }
		   
		   
	   return set;
   }
   public static MapperEntry getMapperEntry(AppContext conn,PropertyFinder finder,String name) throws Exception{
       return conn.getService(ChartService.class).getMapperEntry(finder, name);
   }
   	@SuppressWarnings("unchecked")
	public static MapperEntry getConfigMapperEntry(AppContext conn,FilteredProperties prop,PropertyFinder finder,String name) throws Exception{
   	   Logger log = conn.getService(LoggerService.class).getLogger(MapperEntry.class);
	   name=name.trim();
	   String tag =name.replaceAll("\\s", "_")+".";
	   String desc=prop.getProperty(tag+"description",name);
	   String group_tag=prop.getProperty(tag+"group",name);
	   PropExpression group=null;
	   if( group_tag != null && group_tag.trim().length() > 0 ){
		   try{
			   Parser parser = new Parser(conn,finder);
			   group = parser.parse(group_tag);
		   }catch(Exception e){
			   log.warn("Error parsing group tag "+name, e);
		   }
	   }else{
		   // Try just the raw group name
		   group = finder.find(desc);
	   }
	   MapperEntry me=null;
	   // Cumulative
	   if (group == null ) {
		   me= new SetMapperEntry(conn, name,desc);
		   
	   } else {
		   Labeller lab = null;
		   String lab_tag = prop.getProperty(tag+"labeller");
		   if( lab_tag != null ){
			   // Original implementation used class names in the config
			   Class<? extends Labeller> clazz = conn.getClassFromName(Labeller.class, null, lab_tag);
			   if( clazz != null){
				   lab = conn.makeObject(clazz);
			   }else{
				   // Try making as a tag
				   lab=conn.makeObjectWithDefault(Labeller.class, null, lab_tag);
				   if( lab == null){
					  // still can't do anything with this tag.
					  conn.getService(LoggerService.class).getLogger(MapperEntry.class).error("Specified class "+lab_tag+" is not assignable to Labeller");
				   }
			   }
		   }
		   if(lab == null && group instanceof FormatProvider ){
			   lab = ((FormatProvider)group).getLabeller();
		   }		   
		   me= new KeyMapperEntry(conn,group,lab,name,desc);
		   
	   }
	   me.setCumulative(prop.getBooleanProperty(tag+"cumulative", me.isCumulative()));
	   // Stacked??
	   me.setStacked(prop.getBooleanProperty(tag+"stacked", me.isStacked()));

	   return me;
   }
   	public static Set<MapperEntry> getMappers(AppContext c,UsageProducer up){
   		return getMappers(c, up, null);
   	}
   public static Set<MapperEntry> getMappers(AppContext c,UsageProducer up,String mode){
	   // force load of properties
	   PropertyFinder finder = up.getFinder();
	   Set<MapperEntry> res = new LinkedHashSet<MapperEntry>();
	   for(MapperEntry e: getMapperSet(finder,c,mode)){
		   if( e.compatible(up)){
			   res.add(e);
		   }
	   }
	   return res;
   }
   
@Override
public boolean equals(Object obj) {
   return (obj instanceof MapperEntry) && ((MapperEntry) obj).description.equals(description);
}
@Override
public int hashCode() {
	return description.hashCode();
}
public String toString() {
	String desc = getDescription();
	if( desc != null && desc.trim().length() > 0){
		return desc;
	}
	String name = getName();
	if( name != null && name.trim().length() > 0){
		return name;
	}
	return super.toString();
}

}