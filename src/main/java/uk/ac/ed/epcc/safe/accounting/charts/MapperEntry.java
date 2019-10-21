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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import uk.ac.ed.epcc.safe.accounting.ErrorSet;
import uk.ac.ed.epcc.safe.accounting.NumberReductionTarget;
import uk.ac.ed.epcc.safe.accounting.OverlapHandler;
import uk.ac.ed.epcc.safe.accounting.UsageManager;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.expr.DurationPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.expr.Parser;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.NullSelector;
import uk.ac.ed.epcc.safe.accounting.selector.OverlapType;
import uk.ac.ed.epcc.safe.accounting.selector.PeriodOverlapRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.webapp.AbstractContexed;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Feature;
import uk.ac.ed.epcc.webapp.charts.BarTimeChart;
import uk.ac.ed.epcc.webapp.charts.Chart;
import uk.ac.ed.epcc.webapp.charts.PeriodChart;
import uk.ac.ed.epcc.webapp.charts.PeriodPlot;
import uk.ac.ed.epcc.webapp.charts.PeriodSequencePlot;
import uk.ac.ed.epcc.webapp.charts.PeriodSetPlot;
import uk.ac.ed.epcc.webapp.charts.PieTimeChart;
import uk.ac.ed.epcc.webapp.charts.Plot;
import uk.ac.ed.epcc.webapp.charts.SingleValueSetPlot;
import uk.ac.ed.epcc.webapp.charts.TimeChart;
import uk.ac.ed.epcc.webapp.charts.strategy.SetRangeMapper;
import uk.ac.ed.epcc.webapp.config.ConfigService;
import uk.ac.ed.epcc.webapp.config.FilteredProperties;
import uk.ac.ed.epcc.webapp.content.FormatProvider;
import uk.ac.ed.epcc.webapp.content.Labeller;
import uk.ac.ed.epcc.webapp.jdbc.expr.Reduction;
import uk.ac.ed.epcc.webapp.jdbc.filter.CannotUseSQLException;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.CloseableIterator;
import uk.ac.ed.epcc.webapp.model.data.Duration;
import uk.ac.ed.epcc.webapp.preferences.Preference;
import uk.ac.ed.epcc.webapp.session.SessionService;
import uk.ac.ed.epcc.webapp.time.Period;
import uk.ac.ed.epcc.webapp.time.TimePeriod;
import uk.ac.ed.epcc.webapp.timer.TimeClosable;
import uk.ac.ed.epcc.webapp.timer.TimerService;

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
public abstract class MapperEntry extends AbstractContexed implements Cloneable{
	private static final Feature USE_OVERLAP_HANDLER_IN_TIMECHART = new Feature("use_overlap_handler_in_timechart", false, "Use the OverlapHandler for timecharts instead of iterating over overlaps");
	
	private static final Feature NARROW_CUTOFF_IN_TIMECHART = new Preference("reports.narrow_cutoff_in_timechart",false,"Run additional query to reduce cutoff in timechart by default (overidden by setting per producer)");
	private static final Feature CACHE_NARROWED_CUTOFFS = new Preference("reporting.cache_narrowed_cutoff",false,"Cache the narrowed cutoffs in session");

	public static final String GROUP_ENTRY_BASE = "GroupEntry";
	private final String name;
	private final String description;
    private Color custom_colour[] = null;
    private boolean use_line=false;
    private boolean cumulative=false;
    private boolean stacked=true;
    private String mode=null;
    
    public MapperEntry(AppContext c,String name,String mode,String description){
    	super(c);
    	this.name=name;
    	this.description=description;
    	this.mode=mode;
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
    /** get the {@link FilteredProperties} mode tag used to create this object.
	 * 
	 * Effectively this is a name-space for extended versions of the {@link MapperEntry}
	 * 
	 * @return
	 */
    public String getMode() {
    	return mode;
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
			Reduction red,PropExpression prop_tag, PropExpression<Date> end_prop) throws CannotUseSQLException;
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
			PropExpression<? extends Number> prop_tag, PropExpression<Date> start_prop,
			PropExpression<Date> end_prop,long cutoff) throws CannotUseSQLException;
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
			PropExpression<Date> start_prop, PropExpression<Date> end_prop,long cutoff) throws CannotUseSQLException;
	
    /** Get the cutoff (longest record length) to use for
	 * records overlapping a time period.
	 * A zero cutoff means not known
	 * @param e
	 * @param period
	 * @param prod
	 * @return
	 */
	private long getCutoff(PlotEntry e,TimePeriod period,RecordSelector sel,UsageProducer<?> prod){
		
			// use cutoff configured into PlotEntry if any
			long cutoff = e.getCutoff();
			
				boolean narrow = getContext().getBooleanParameter("narrow_cutoff."+prod.getTag(), NARROW_CUTOFF_IN_TIMECHART.isEnabled(getContext()));
				if( narrow) {
					TimerService timer = getContext().getService(TimerService.class);
					if( timer != null) {
						timer.startTimer("narrow_cutoff."+prod.getTag());
					}
					try {
						PropExpression<Date> start = e.getStartProperty();
						PropExpression<Date> end = e.getEndProperty();
						if( start != null && end != null) {

							final DurationPropExpression duration = new DurationPropExpression(start, end);
							// This may be a very long query but the hope is that
							// we can save the time back by narrowing the cutoff based on the filter
							AndRecordSelector fil = new AndRecordSelector(sel);
							fil.add(new PeriodOverlapRecordSelector(period, start,end,OverlapType.ANY,cutoff));
							fil.add(new SelectClause<>(duration,MatchCondition.GT,new Duration(0L,1L)));
							fil.add(new SelectClause<>(start,MatchCondition.GT,new Date(0L)));

							Number calc_cutoff = null;

							if(CACHE_NARROWED_CUTOFFS.isEnabled(getContext())) {
								String name="narrowed_cutoffs."+prod.getTag();
								SessionService sess = getContext().getService(SessionService.class);
								Map<RecordSelector,Number> values = (Map<RecordSelector, Number>) sess.getAttribute(name);
								if( values == null) {
									values=new HashMap<>();
								}
								calc_cutoff = values.get(fil);
								if( calc_cutoff ==null) {
									calc_cutoff = prod.getReduction(NumberReductionTarget.getInstance(Reduction.MAX, duration), fil);
									values.put(fil,calc_cutoff);
									sess.setAttribute(name, values);
								}
							}else {
								calc_cutoff = prod.getReduction(NumberReductionTarget.getInstance(Reduction.MAX, duration), fil);
							}
							cutoff = calc_cutoff.longValue()+1L;
						}
					}catch(Exception ex) {
						getLogger().error("Error narrowing cutoff",ex);
					}finally {
						if( timer != null) {
							timer.stopTimer("narrow_cutoff."+prod.getTag());
						}
					}
				}
			
			return cutoff;
	
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
    		res.add(new SelectClause<>(e.getEndProperty(), MatchCondition.GT, start));
    		res.add(new SelectClause<>(e.getEndProperty(), MatchCondition.LE, end));
    	}
    	return res;
    	
    }
   
    /** Is the {@link UsageProducer} compatible with this {@link MapperEntry}
     * 
     * Applied by {@link KeyMapperEntry} to ensure the group property is supported.
     * 
     * @param ap
     * @return
     */
    public boolean compatible(UsageProducer<?> ap){
    	return true;
    }
    protected abstract Vector<String> getLabels();
    /** Generate a dataset for a PieTimeChart using this mapper.
     * 
     * @param e
     * @param tc
     * @param ap
     * @param sel
     * @param nplots
     * @param overlap
     * @return dataset or null if no data
     * @throws Exception 
     */
    public PeriodSetPlot makePieTimeChartPlot(PlotEntry e, PieTimeChart tc, UsageProducer ap,RecordSelector sel, int nplots,boolean overlap) throws Exception {
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
	
	public PeriodSetPlot makeBarTimeChartPlot(String series,PlotEntry e, BarTimeChart tc, UsageProducer ap,RecordSelector sel, int nplots,boolean allow_overlap) throws Exception {
        if( ! (e.compatible(ap) && compatible(ap))){
        	return null;
        }
    	
    	
		PeriodSetPlot ds= tc.getBarChartSeries(series, 0);
      
        boolean data_added = addData(e,tc, ap, sel, ds,allow_overlap);
        if( data_added){
        	return ds;
        }
        return null;
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
			RecordSelector sel, PeriodPlot ds,boolean allow_overlap) throws Exception {
		if( ap == null) {
			// assume narrowed composite producer
			return false;
		}
		boolean data_added=false;
        // create dataset, don't add labels yet as labels
		// vector may grow as data added
		PropExpression prop_tag = e.getPlotProperty();
		PropExpression<Date> start_prop = e.getStartProperty();
		PropExpression<Date> end_prop = e.getEndProperty();
		Reduction red = e.getReduction();
		TimerService timer = conn.getService(TimerService.class);
		// We can't necessarily afford an additional query here
		// may be a single additional query so just use any cutoff from the config
		long cutoff = e.getCutoff();
		RecordSelector s =getRangeSelector(e, allow_overlap,cutoff, tc.getStartDate(), tc.getEndDate(), sel);
		
		
        boolean query_mapper_on = OverlapHandler.USE_QUERY_MAPPER_FEATURE.isEnabled(conn);
       
        if( query_mapper_on ){
        	query_mapper_on = conn.getBooleanParameter(ap.getTag()+".use_query_mapper",true);
        }
       
		if( query_mapper_on  ){ //use fmapper if it exists for piecharts
			try(TimeClosable tim = new TimeClosable(timer, "MapperEntry.addData.query_mapper")){
				UsageRecordQueryMapper fmapper;
				if( start_prop != null && allow_overlap){
					fmapper = getOverlapQueryMapper(s,red, prop_tag, start_prop,
							end_prop,cutoff);
				}else{
					fmapper = getPointQueryMapper(sel, red,prop_tag, end_prop);
				}
				return ds.addMapData( fmapper, ap);
			}catch(CannotUseSQLException e1){
				// default to iterating
				
			}
        }
		SetRangeMapper map = getMapper(e);
		try(CloseableIterator iter = ap.getExpressionIterator(s);TimeClosable tim = new TimeClosable(timer, "MapperEntry.addData.iterator")){
			if( iter.hasNext()){
				tc.addDataIterator(ds, map, iter);
				data_added=true;
			}
		}

		return data_added;
	}
	
  
    
    public boolean plotDataSet(Plot ds,boolean graph_transform,PlotEntry e, PeriodChart tc, UsageProducer ap,RecordSelector sel, int nplots) throws Exception {
    	if( ds == null) {
    		return false;
    	}
    	if( tc instanceof TimeChart){
    		plotTimeChart(e, (TimeChart)tc, (PeriodSequencePlot)ds, nplots, use_line,graph_transform);
    		return true;
    	}
    	if( tc instanceof PieTimeChart){
    		plotPieTimeChart(e, (PieTimeChart)tc, nplots, (SingleValueSetPlot)ds);
    		return true;
    	}
    	if( tc instanceof BarTimeChart){
    		plotBarTimeChart(e, (BarTimeChart) tc, nplots, (SingleValueSetPlot)ds);
    		return true;
    	}
    	return false;
    }
    /** Create a {@link Plot} dataset corresponding to the desired {@link PlotEntry} etc.
     * 
     * @param graph_transform
     * @param e
     * @param tc
     * @param ap
     * @param sel
     * @param nplots
     * @param allow_overlap
     * @return {@link Plot}
     * @throws Exception
     */
	public Plot makePlot(boolean graph_transform,PlotEntry e, PeriodChart tc, UsageProducer ap,RecordSelector sel, int nplots,boolean allow_overlap) throws Exception {
		if( tc instanceof TimeChart){
			return makeTimeChartPlot(e, (TimeChart) tc, ap, sel, nplots, allow_overlap, use_line, graph_transform);
		}
		if( tc instanceof PieTimeChart){
			return makePieTimeChartPlot(e, (PieTimeChart) tc, ap, sel, nplots, allow_overlap);
		}
		if( tc instanceof BarTimeChart){
			return makeBarTimeChartPlot(e.getLabel(),e, (BarTimeChart) tc, ap, sel, nplots, allow_overlap);
		}
		return null;
	}
	
    public boolean getUseLine() {
    	return use_line;
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
        if( ap == null ) {
        	// assume narrowed composite producer
        	return ds;
        }
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
        	if( compatible(ap) && ap.compatible(sel)) {
        		data_added = addTimeChartData(e,tc, ap, sel, ds,allow_overlap);
        	}
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
    			SetMapperEntry se = new SetMapperEntry(conn, "norm", null,"normalisation");
    			P norm = se.makeTimeChartPlot(s, tc, ap, request_sel, nplots, allow_overlap, use_line, graph_transforms);
    			if( norm != null ){
    				ds.datasetScale( 1.0, norm);
    			}else{
    				// better no data than not applying norm and getting different
    				// result than asked.
    				return null;
    			}
    		}
        	
        }
        return ds;
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
    	}else{
    		String plot_label = e.getLabel();
    		if( plot_label != null) {
    			ds.setLegends(new String[] { plot_label});
    		}
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
			PeriodSequencePlot ds,boolean allow_overlap) throws Exception {
		PropExpression<? extends Number> prop_tag = e.getPlotProperty();
		PropExpression<Date> start_prop = e.getStartProperty();
		PropExpression<Date> end_prop = e.getEndProperty();
		Reduction red = e.getReduction();
		long cutoff = getCutoff(e, tc.getPeriod(), sel,ap);
		boolean data_added=false;
        Logger log = conn.getService(LoggerService.class).getLogger(getClass());
        log.debug(" params end="+tc.getEndDate()+" start="+tc.getStartDate());
       
		
     // create dataset, don't add labels yet as labels
		// vector may grow as data added
    	boolean query_mapper_on = conn.getBooleanParameter("use_query_mapper."+ap.getTag(), OverlapHandler.USE_QUERY_MAPPER_FEATURE.isEnabled(conn));
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
    				if(  conn.getBooleanParameter("use_overlap_handler_in_timechart."+ap.getTag(), USE_OVERLAP_HANDLER_IN_TIMECHART.isEnabled(conn))){
    					log.debug("using overlap handler");
    					// This might be faster if the OverlapHandler is using a CaseExpression 
    					// less SQL queries but more complex.
    					// however they are also ANY overlap queries so at the very least we need auto_cutoff 
    					// in place 
    					UsageRecordQueryMapper fmapper;
    					fmapper = getOverlapQueryMapper( sel,red, prop_tag,
    							start_prop,end_prop,cutoff);
    					data_added = tc.addMapData(ds, fmapper, ap);
    				}else{
    					// Because we have adjacent sequences, 
    					// rather than use the methods in OverlapHander we can use a single
    					// iteration phases for all periods the record overlaps.

    					// start with just the inner regions
    					UsageRecordQueryMapper fmapper;
    					fmapper = getInnerQueryMapper(sel, red,prop_tag, start_prop,
    							end_prop,cutoff);
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


    						try(CloseableIterator<ExpressionTargetContainer> iter = ap.getExpressionIterator(selector)){
    							if( iter.hasNext()){
    								tc.addDataIterator(ds, map, iter);
    								data_added=true;
    							}
    						}
    					}

    					// final (expensive) overlap with end
    					// use full timechart period for OUTER records
    					AndRecordSelector selector = new AndRecordSelector(sel);
    					selector.add(new PeriodOverlapRecordSelector(new Period(tc.getPeriod()), start_prop, end_prop, OverlapType.UPPER_OUTER, cutoff));


    					try(CloseableIterator<ExpressionTargetContainer> iter = ap.getExpressionIterator(selector)){
    						if( iter.hasNext()){
    							tc.addDataIterator(ds, map, iter);
    							data_added=true;
    						}
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
    	try(CloseableIterator<ExpressionTargetContainer> iter= ap.getExpressionIterator(getRangeSelector(e,allow_overlap,cutoff,tc.getStartDate(), tc.getEndDate(),sel))){
    		if( iter.hasNext()){
    			tc.addDataIterator(ds, map, iter);
    			data_added=true;
    		}
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
		   
	   Logger log = c.getService(LoggerService.class).getLogger(MapperEntry.class);
		
		   Set<MapperEntry> set = new LinkedHashSet<>();
		   
		   String list = prop.getProperty("list");
		   if( list != null ){
			   list = c.expandText(list);
			   log.debug("list="+list);
			   for(String name : list.split(",")){
				   ErrorSet e = new ErrorSet();
				   MapperEntry entry = getConfigMapperEntry(c, e, prop, finder, name);
				   if( entry != null){
					   set.add(entry); 
				   }
				   if( e.hasError()){
					   e.report(
					   c.getService(LoggerService.class).getLogger(MapperEntry.class));
				   } 
			   }
		   }
		   
		   
	   return set;
   }
   public static MapperEntry getMapperEntry(AppContext conn,ErrorSet errors,PropertyFinder finder,String name) throws Exception{
       return conn.getService(ChartService.class).getMapperEntry(errors,finder, name);
   }
   	@SuppressWarnings("unchecked")
	public static MapperEntry getConfigMapperEntry(AppContext conn,ErrorSet errors,FilteredProperties prop,PropertyFinder finder,String name) {
   	   Logger log = conn.getService(LoggerService.class).getLogger(MapperEntry.class);
	   name=name.trim();
	   log.debug("Requesting MapperEntry name=["+name+"]");
	   String tag =name.replaceAll("\\s", "_")+".";
	   String desc=prop.getProperty(tag+"description",name);
	   String group_tag=prop.getProperty(tag+"group",name);
	   PropExpression group=null;
	   if( group_tag != null && group_tag.trim().length() > 0 ){
		   try{
			   Parser parser = new Parser(conn,finder);
			   group = parser.parse(group_tag);
		   }catch(Exception e){
			   errors.add("Error parsing group tag/expression", group_tag, e);
			   return null; 
		   }
	   }
	   MapperEntry me=null;
	   // Cumulative
	   if (group == null ) {
		   me= new SetMapperEntry(conn, name,prop.getMode(),desc);   
	   } else {
		   Labeller lab = null;
		   String lab_tag = prop.getProperty(tag+"labeller");
		   if( lab_tag != null ){
			   // Original implementation used class names in the config
			   Class<? extends Labeller> clazz = conn.getClassFromName(Labeller.class, null, lab_tag);
			   if( clazz != null){
				   try {
					lab = conn.makeObject(clazz);
				} catch (Exception e) {
					errors.add("Error constructing labeller",lab_tag,e);
				}
			   }else{
				   // Try making as a tag
				   lab=conn.makeObjectWithDefault(Labeller.class, null, lab_tag);
				   if( lab == null){
					  // still can't do anything with this tag.
					  errors.add("Invalid chart labeller", lab_tag);
				   }
			   }
		   }
		   if(lab == null && group instanceof FormatProvider ){
			   lab = ((FormatProvider)group).getLabeller();
		   }		   
		   me= new KeyMapperEntry(conn,group,lab,name,prop.getMode(),desc);
		   
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
	   Set<MapperEntry> res = new LinkedHashSet<>();
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