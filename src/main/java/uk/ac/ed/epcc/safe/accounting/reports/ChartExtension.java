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
package uk.ac.ed.epcc.safe.accounting.reports;


import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.function.Supplier;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;

import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.charts.*;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.charts.*;
import uk.ac.ed.epcc.webapp.content.Table;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
import uk.ac.ed.epcc.webapp.exceptions.InvalidArgument;
import uk.ac.ed.epcc.webapp.jdbc.expr.Reduction;
import uk.ac.ed.epcc.webapp.limits.LimitException;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.time.Period;
import uk.ac.ed.epcc.webapp.time.SplitPeriod;

/**
 * This is a class used to add Plots via a XSLT extension. An instance is passed
 * in as a parameter and methods are invoked to generate a plot
 * 
 * The chart creation, add data and format operations are implemented as separate operations
 * to allow the extension to also format charts generated independently by custom extensions.  
 * 
 * The XML parsing is slightly more forgiving than the schema as it does not enforce element ordering
 * but this is difficult to encode in the schema.
 * 
 * 
 * @author spb
 * 
 */
public abstract class ChartExtension extends ReportExtension {
	private static final String RANGE_ELEMENT = "Range";
	private static final String WARNING_LEVEL_ELEMENT = "WarningLevel";
	private static final String NUMBER_OF_TIME_BLOCKS_ELEMENT = "NumberOfTimeBlocks";
	private static final String QUANTITY_ELEMENT = "Quantity";
	private static final String TITLE_ELEMENT = "Title";
	private static final String STACKED_ELEMENT = "Stacked";
	private static final String LINE_ELEMENT = "Line";
	private static final String COLOURS_ELEMENT = "Colours";
	private static final String LABEL_ELEMENT = "Label";
	private static final String GROUP_BY_ELEMENT = "GroupBy";
	private static final String CUMULATIVE_ELEMENT = "Cumulative";
	private static final String N_PLOTS_ELEMENT = "NPlots";
	private static final String OVERLAP_ELEMENT = "Overlap";
	private static final String PLOT_LABEL_ELEMENT = "PlotLabel";
	private static final String RATE_LABEL_ELEMENT = "RateScaleLabel";
	private static final String NORM_ELEMENT = "Norm";
	private static final String REDUCTION_ELEMENT = "Reduction";
	private static final String SCALE_ELEMENT = "Scale";
	private static final String TIME_SCALE_ELEMENT = "TimeScale";
	private static final String RATE_SCALE_ELEMENT = "RateScale";
	private static final String END_PROP_ELEMENT = "EndProp";
	private static final String START_PROP_ELEMENT = "StartProp";
	private static final String PLOT_ELEMENT = "Plot";
	public static final String CHART_LOC = "http://safe.epcc.ed.ac.uk/chart";
	
	/** composite object holding the {@link PeriodChart}
	 * and a record if any data has been added
	 * 
	 * @author spb
	 * @param <P> 
	 *
	 */
	public static class Chart<P extends PeriodChart>{
		public Chart(P pc,String report_prefix){
			this.chart=pc;
			this.report_prefix=report_prefix;
		}
		public P chart;
		public String report_prefix;
		public boolean has_data=false;
	}
	
	public boolean hasData(Chart c){
		if( c== null ){
			return false;
		}
		return c.has_data ;
	}
	private final ChartService serv;
	public ChartExtension(AppContext c,ReportType type) throws ParserConfigurationException {
		super(c,type);
		serv=c.getService(ChartService.class);
	}
	@Override
	public void setParams(ReportType type, Set<String> names, Map<String, Object> p) {
		super.setParams(type, names, p);
	}
	public PlotEntry getPlotEntry(RecordSet set, Node n) throws Exception{
		Element e = (Element) n;
		if( ! hasChild(PLOT_ELEMENT, e) ) {
			// Null plot is allowed so we can put everything
			// nested in AddChart if we want
			return null;
		}
		PropertyFinder finder = set.getFinder();
		Element plot_e = getParamElement(PLOT_ELEMENT, e);
		PlotEntry result=null;
		String start_name=null;
		String end_name=null;
		String name="";
		Object o = getParameterRef(plot_e);
		if( o != null && o instanceof PlotEntry) {
			result = (PlotEntry) o;
		}else {
			name = getExpandedParam(PLOT_ELEMENT,e);
			if( name == null  || name.isEmpty()){
				addError("Bad PlotEntry", "No plot property specified", e);
				return null;
			}
			start_name = getParam(START_PROP_ELEMENT, e);
			end_name = getParam(END_PROP_ELEMENT,e);
			result = serv.getPlotEntry( errors,finder, name,start_name,end_name);
		}
		if( result == null ){
			addError("Invalid Plot Quantity","The specified quantity "+name+" does not correspond to a plottable quantity or named PlotEntry",e);
			return null;
		}
		if( start_name != null && start_name.trim().length() > 0 && result.getStartProperty() == null ){
			addError("Bad property", "StartProp value "+start_name+" failed to parse", e);
		}
		if( end_name != null && end_name.trim().length() > 0 && result.getEndProperty() == null ){
			addError("Bad property", "EndProp value "+end_name+" failed to parse", e);
		}
		
		//TODO make this an element with additional params for label and scale.
		result.setRateScale( getBooleanParam(RATE_SCALE_ELEMENT,result.isRateScale(), e));
		result.setScale(getNumberParam(SCALE_ELEMENT, result.getScale(), e).doubleValue());
		result.setTimeScale(getNumberParam(TIME_SCALE_ELEMENT, result.getTimeScale(), e).doubleValue());
		String red = getParam(REDUCTION_ELEMENT, e);
		if( red != null){
			try{
				result.setReduction(Reduction.valueOf(red));
			}catch(Exception t){
				addError("Bad Reduction", red, t);
			}
		}
		String label = getExpandedParam(PLOT_LABEL_ELEMENT, e);
		if( label != null ){
			result.setLabel(label);
		}
		String rate_label = getExpandedParam(RATE_LABEL_ELEMENT, e);
		if( rate_label != null ){
			result.setTimeUnit(rate_label);
		}
		Element norm_e = getParamElement(NORM_ELEMENT, e);
		if( norm_e != null ) {
			PlotEntry norm = getPlotEntry(set, norm_e);
			result.setNorm(norm);
		}
		Element range_e = getParamElement(RANGE_ELEMENT, e);
		if(range_e != null) {
			Number min = getNumberParam("Min", null, range_e);
			Number max = getNumberParam("Max", null, range_e);
			DataRange r = new DataRange(min, max);
			result.setRange(r);
		}
	
		return result;
	}
	
	public MapperEntry getMapperEntry(RecordSet set, Node n) throws Exception{
		Element e = (Element) n;
		MapperEntry entry=null;
		AppContext ctx = getContext();
		PropertyFinder finder = set.getFinder();
		if (hasParam(GROUP_BY_ELEMENT, e)) {
			Element group_e = getParamElement(GROUP_BY_ELEMENT, e);
			Object o = getParameterRef(group_e);
			if( o != null && o instanceof MapperEntry) {
				entry = (MapperEntry) o;
			}else {
				String param = getParam(GROUP_BY_ELEMENT, e);
				log.debug("GroupBy="+param);
				entry = serv.getMapperEntry(errors,finder,param);
			}
		}
		if( entry == null ) {
			entry = serv.getMapperEntry(errors, finder, "");
			if( entry == null) {
				addError("Bad MapperEntry", "Mapper entry failed to parse", n);
				return null;
			}
			if( hasParam(LABEL_ELEMENT, e)){
				((SetMapperEntry)entry).setLabel(getExpandedParam(LABEL_ELEMENT, e));
			}
		}
		if( hasParam(LINE_ELEMENT, e)){
			entry.setUseLine(getBooleanParam(LINE_ELEMENT, false, e));
		}
		if( hasParam(CUMULATIVE_ELEMENT, e)){
			entry.setCumulative(getBooleanParam(CUMULATIVE_ELEMENT, false, e));
		}
		if( hasParam(STACKED_ELEMENT, e)){
			entry.setCumulative(getBooleanParam(STACKED_ELEMENT, false, e));
		}
		if(hasParam(COLOURS_ELEMENT, e)){
			List<Color> list = new LinkedList<>();
			for(String s : getParam(COLOURS_ELEMENT, e).split("\\s+")){
				Color color = Color.decode(ctx.getInitParameter("colour."+s, s));
				if( color != null ){
					list.add(color);
				}else{
					addError("bad colour", "Colour "+s+" did not parse");
				}
			}
			entry.setColours(list.toArray(new Color[0]));
		}
		
		return entry;
	}
	
	private <P extends PeriodChart> Chart<P> setChartOptions(P chart, Element e){
		try{
		if( hasParam(TITLE_ELEMENT, e)){
			chart.getChartData().setTitle(getParam(TITLE_ELEMENT, e).trim());
		}
		}catch(Exception e1){
			addError("Bad Plot", "Error setting title", e1);
		}
		try{
			if( hasParam(QUANTITY_ELEMENT, e)){
				chart.getChartData().setQuantityName(getParam(QUANTITY_ELEMENT, e).trim());
			}
		}catch(Exception e1){
			addError("Bad Plot", "Error setting title", e1);
		}
		return new Chart<>(chart,getReportPrefix(e));
	}
	
	public Chart<TimeChart> makeTimeChart(Period period, Node node) {
		startTimer("makeTimeChart");
		try{
			if (node.getNodeType() != Node.ELEMENT_NODE) {
				addError("Bad Plot", "Non element fragment passed to makeTimeChart");
				return null;
			}


			Element e = (Element) node;
			AppContext ctx = getContext();

			int timeBlocks = 1;
			if( graphOutput()) {
				// Only use timeBlocks for graphical output
				// in tables they will be merged anyway
				// and merge will just use a sum
				// which will be incorrect for time averages
				timeBlocks = getContext().getIntegerParameter("timechart.default.NumberOfTimeBlocks", 10);
				try {
					timeBlocks = getIntParam(NUMBER_OF_TIME_BLOCKS_ELEMENT,timeBlocks , e);
				} catch (Exception e1) {
					addError("Bad Plot", "Error setting NumberOfTimeBlocks", e1);
				}	
			}
			

			try {
				TimeChart chart;
				if( period instanceof SplitPeriod ){
					// This is the normal operation periods should normally be 
					// SplitPeriods
					chart = TimeChart.getInstance(ctx, (SplitPeriod) period,timeBlocks);
				}else{
					// fallback operation. This should not happen normally but
					// might happen for example the SAFE legacy public reports
					// might use a non SplitPeriod
					int nSplits = ctx.getIntegerParameter("timechart.default.NumberOfSplits", 10);
					chart = TimeChart.getInstance(ctx, period.getStart(), period.getEnd(), nSplits, timeBlocks);
				}
				chart.getChartData().setGraphical(graphOutput());
				
				return setChartOptions(chart,e);
			} catch (InvalidArgument e2) {
				addError("Bad Timechart specification", e2.getClass().getCanonicalName(),e2);
				return null;
			}
		} catch(Exception t){
			addError("internal error", "Error making TimeChart", t);

		}finally{
			stopTimer("makeTimeChart");
		}
		return null;
		}
//	public boolean addPlot(RecordSet set, Chart chart, Node node ){
//		if (node.getNodeType() != Node.ELEMENT_NODE) {
//			addError("Bad Plot", "Non element fragment passed to makeTimeChart");
//			return false;
//		}
//		Element e = (Element) node;
//		
//		chart.has_data = addPlot(false,set,chart,node) || chart.has_data;
//		// look for additional plots
//		if( chart.chart instanceof TimeChart){
//			NodeList list = e.getChildNodes();
//			for(int i=0; i<list.getLength();i++){
//				Node n = list.item(i);
//				String namespace = n.getNamespaceURI();
//				if( n.getNodeType() == Node.ELEMENT_NODE && n.getLocalName().equals("AddChart") && (namespace == null || namespace.equals(e.getNamespaceURI()))){
//					chart.has_data = addPlot(set, chart, n) || chart.has_data;
//				}
//			}
//		}
//		return chart.has_data;
//	}

	/** Add a plot to the chart.
	 * 
	 * This method returns a DocumentFragment that is added
	 * to the output document. This is to ensure the stylesheet processor
	 * will not optimise away the operation. 
	 * Normally no output is returned but this might be used to add
	 * debugging output to the result.
	 * 
	 * @param set
	 * @param chart
	 * @param node
	 * @return DocumentFragment
	 */
	public DocumentFragment addPlot(RecordSet set, Chart<?> chart, Node node ){
		
		try {
			PlotEntry p = getPlotEntry(set, (Element) node);
			
			MapperEntry entry = getMapperEntry(set, (Element) node);
			Plot ds = makeDataSet(null, set, p, entry, chart, node);
			return addPlot(ds,set,p,entry,chart,node);
		}catch(Exception t){
			addError("Error in plot", t.getClass().getCanonicalName(), t);
			Document doc = getDocument();
			return doc.createDocumentFragment();
		}
	}
	public Plot makeDataSet(RecordSet set,PlotEntry plot,MapperEntry entry, Chart<?> chart, Node node ) throws Exception {
		return makeDataSet(null,set,plot,entry,chart,node);
	}
	public Plot makeDataSet(Plot orig,RecordSet set,PlotEntry plot,MapperEntry entry, Chart<?> chart, Node node ) throws Exception {
		if(plot == null) {
			// ok to skip top level plot
			return null;
		}
		if( chart == null){
			addError("Bad Plot","No chart object");
			return orig;
		}
		if (node.getNodeType() != Node.ELEMENT_NODE) {
			addError("Bad Plot", "Non element fragment passed to makeTimeChart");
			return orig;
		}
		Element e = (Element) node;
		int nPlots = getNumberParam(N_PLOTS_ELEMENT, 10, e).intValue();
		UsageProducer up = set.getUsageProducer();
		if( up == null) {
			return orig;
		}
		if( ! entry.compatible(up)) {
			// This could happen in an AddData clause where the
			// MapperEntry is already set but the UsageProducer is
			// changed. However it might just be becasue the producer has been narrowed
			// so check the un-narrowed generator jsut in case.
			UsageProducer generator = set.getGenerator();
			if( up == generator || ! entry.compatible(generator)) {
				addError("Bad Plot","Usage producer not compatible with current MapperEntry");
			}
			return orig;
		}
		
		boolean overlap = getBooleanParam(OVERLAP_ELEMENT, true, e);
		RecordSelector sel = set.getRecordSelector();
		PeriodChart tc = chart.chart;
		Plot ds =  entry.makePlot(graphOutput(), plot, tc, up, sel, nPlots, overlap);

		if( orig == null) {
			return ds;
		}else {
			if( hasParam(CUMULATIVE_ELEMENT, e) && getBooleanParam(CUMULATIVE_ELEMENT, false, e) && ! entry.isCumulative() && ds instanceof PeriodSequencePlot) {
				// cumulative only in sub-plot so scale before add
				PeriodSequencePlot psp = (PeriodSequencePlot)ds;
				psp.scaleCumulative(1.0, new double[psp.getNumSets()]);
			}
			orig.addData(ds);
			if( chart.chart instanceof SetPeriodChart) {
				// set period charts only plot one dataset
				// so make sure this is the right one
				((SetPeriodChart)chart.chart).setPlot((PeriodSetPlot) orig);
			}
			return orig;
		}
	}
	public DocumentFragment addPlot(RecordSet set,PlotEntry plot,MapperEntry entry, Chart<?> chart, Node node ){
		try {
			Plot ds = makeDataSet(null, set, plot, entry, chart, node);
			return addPlot(ds,set,plot,entry,chart,node);
		}catch(Exception t){
			addError("Error in plot", t.getClass().getCanonicalName(), t);
			Document doc = getDocument();
			return doc.createDocumentFragment();
		}
	}	
	public DocumentFragment addPlot(Plot ds,RecordSet set,PlotEntry plot,MapperEntry entry, Chart<?> chart, Node node ){
		Document doc = getDocument();
		DocumentFragment result = doc.createDocumentFragment();
		if( ds == null) {
			return result;
		}
		if( chart == null){
			return result;
		}
		if (node.getNodeType() != Node.ELEMENT_NODE) {
			addError("Bad Plot", "Non element fragment passed to makeTimeChart");
			return result;
		}
		
		if( plot == null ){
			return result;
		}
		checkLimit();
		Element e = (Element) node;
		startTimer("addPlot");
		try{
			
			

			int nPlots = getNumberParam(N_PLOTS_ELEMENT, 10, e).intValue();
			UsageProducer up = set.getUsageProducer();
			if( up == null) {
				return result;
			}
			RecordSelector sel = set.getRecordSelector();
			PeriodChart tc = chart.chart;
			
			boolean added = entry.plotDataSet(ds, graphOutput(), plot, tc, up, sel, nPlots);
			chart.has_data =  added || chart.has_data;
			try{
				if( chart.chart instanceof TimeChart &&  hasParam(WARNING_LEVEL_ELEMENT, (Element) node)){
					((TimeChart)chart.chart).addWarningLevel(getNumberParam(WARNING_LEVEL_ELEMENT, 0.0,(Element) node).doubleValue());
				}
			}catch(Exception e3){
				addError("Bad Timechart specification", "Error setting WarningLevel",e3);
			}			
			return result;
		}catch(Exception t){
			addError("Error in plot", t.getClass().getCanonicalName(), t);
			return result;
		}finally{
			stopTimer("addPlot");
		}
	}
	public Chart<PieTimeChart> makePieTimeChart(Period period, Node node) {
		
		if (node.getNodeType() != Node.ELEMENT_NODE) {
			addError("Bad Plot", "Non element fragment passed to makeChart");
			return null;
		}
		
		AppContext ctx = getContext();

		try{
			PieTimeChart chart = PieTimeChart.getInstance(ctx, (SplitPeriod) period);
			chart.getChartData().setGraphical(graphOutput());
			return  setChartOptions(chart,(Element) node);


		}catch(Exception t){
			addError("Error in makePieTimeChart", t.getClass().getCanonicalName(), t);
			return null;
		}

	}
	/** utility method to allow editing of the deferred chart description
	 * 
	 * @param parent
	 * @param child
	 * @return
	 */
	public Node merge(Node parent, Node child) {
		Node parent_e = parent;
		if( parent.getNodeType() != Node.ELEMENT_NODE) {
			parent_e = parent_e.getFirstChild();
		}
		if( child.getNodeType() != Node.ELEMENT_NODE) {
			child = child.getFirstChild();
		}
		parent_e.appendChild(child);
		logFragment("Merged fragments", parent);
		return parent;
	}
	protected void logFragment(String message, Node n) {
		Logger log = getLogger();
		
		// write the content into xml file

		
			log.debug(new Supplier<String>() {
				
				@Override
				public String get() {
					try {
						ByteArrayOutputStream res = new ByteArrayOutputStream();
						TransformerFactory transformerFactory = TransformerFactory.newInstance();
						Transformer transformer = transformerFactory.newTransformer();
						DOMSource source = new DOMSource(n);
						StreamResult out = new StreamResult(res);
						transformer.transform(source, out);
						return message+": "+res.toString();
					}catch(Exception e) {
						return "Failed to format debug fragemnt "+e.getMessage();
					}
						
				}
			});
			
			
	
	}
	public Chart<BarTimeChart> makeBarTimeChart(Period period, Node node) {
	

		if (node.getNodeType() != Node.ELEMENT_NODE) {
			addError("Bad Plot", "Non element fragment passed to makeTimeChart");
			return null;
		}
		
		
		
		AppContext ctx = getContext();
		
		try {
			BarTimeChart chart = BarTimeChart.getInstance(ctx,  period);
			chart.getChartData().setGraphical(graphOutput());
			String stacked = getAttribute("stacked", (Element)node);
			if( stacked != null && Boolean.valueOf(stacked)) {
				((BarTimeChartData)chart.getChartData()).setStacked(true);
			}
			
			return  setChartOptions(chart,(Element) node);
		} catch (Exception e) {
			addError("Bad Plot", "Error making BarChart", e);
			return null;
		}
	
		

	}

public DocumentFragment addChartTable(Chart chart,String caption) throws Exception {
		   
		checkLimit();
		Table t = chart.chart.getTable();
		if (t.hasData()) {
			return format(t, null,chart.report_prefix);
			//TODO handle caption
		} else {
			return addNoData(chart);
		}
	}
	public DocumentFragment addNoData(Chart chart) throws Exception {
	   
		Document doc = getDocument();
		
		DocumentFragment result = doc.createDocumentFragment();
		Element e;
		String prefix=null;
		if( chart != null ) {
			prefix=chart.report_prefix;
		}
		if( prefix == null) {
			e = doc.createElement("NoData");
		}else {
			e = doc.createElementNS(ReportBuilder.REPORT_LOC ,(prefix==null || prefix.trim().isEmpty())? "NoData": prefix +":NoData");
		}
		result.appendChild(e);
		return result;
	}

	public abstract DocumentFragment addChart(Chart chart,String caption);
	
	
	public abstract boolean graphOutput();

	/** should chart generation be deferred till the image is requested
	 * 
	 * Defaults to false as this only makes sense in a browser context
	 * 
	 * @return boolean
	 */
	public boolean deferrCharts() {
		return false;
	}
	/** records the chart fragment for later generation
	 * emits a XML fragment to be included in the report.
	 * 
	 * @param spec
	 * @param caption
	 * @return
	 */
	public Node emitDeferredChart(Node spec,String caption) {
		// Default null implementation
		Document doc = getDocument();
		DocumentFragment result = doc.createDocumentFragment();
		return result;
	}
	@Override
	public boolean wantReplace(Element e) {
		return CHART_LOC.equals(e.getNamespaceURI());
	}
	private class DeferredTransform implements IdentityDomTransform{
		public DeferredTransform(RecordSet filcontext) {
			super();
			this.filcontext = filcontext;
		}

		private final RecordSet filcontext;
		@Override
		public void setDocument(Document doc) {
			
		}

		@Override
		public Document getDocument() {
			return ChartExtension.this.getDocument();
		}

		@Override
		public boolean wantReplace(Element e) {
			if( ParameterExtension.PARAMETER_LOC.equals(e.getNamespaceURI()) && ParameterExtension.PARAMETER_REF_ELEMENT.equals(e.getLocalName())) {
				return true;
			}
			if( FILTER_LOC.equals(e.getNamespaceURI()) && FILTER_ELEMENT.equals(e.getLocalName())) {
				return true;
			}
			
			return false;
		}

		@Override
		public Node replace(Element e) {
			String name = e.getLocalName();
			try {
				switch(name) {
				case ParameterExtension.PARAMETER_REF_ELEMENT: return inlineParameterRef(e);
				case FILTER_ELEMENT: 
					RecordSet set = makeSelector();
					set.setUsageProducer(filcontext.getUsageProducer()); // wont set the name
					set = addFilterElement(set, e);
					return formatRecordSet(set);
				}
			}catch(Exception ex) {
				addError("Expansion error in "+name, ex.getMessage(), e, ex);
			}
			return null;
		}
		
	}
	@Override
	public Node replace(Element source) {
		String name = source.getLocalName();
		boolean table_output = ! source.getAttribute("table").isEmpty();
		boolean graphic_output = source.getAttribute("nographic").isEmpty();
		boolean quiet = ! source.getAttribute("quiet").isEmpty();
		String caption = null;
		try {
			caption = getParam("Caption", source);
		}catch(Exception e) {
			addError("chart error chart:"+name, "Error getting caption", e);
		}
		Document doc = getDocument();
		DocumentFragment result = doc.createDocumentFragment();
		Element period_element = ElementSet.ancestors_self(source).select(new Match(PERIOD_NS, PERIOD_ELEMENT)).pollLast();
		
		RecordSet set = addFilterElementSet(makeSelector(), ElementSet.ancestors_self(source).select(new Match(FILTER_LOC, FILTER_ELEMENT)));
		
		if( deferrCharts() && ! table_output) {
			// handle a deferred chart
			try {
				Node spec = doc.importNode(source, false);
				
				spec.appendChild(doc.importNode(period_element, true));
				spec.appendChild(formatRecordSet(set));
				ElementSet content = ElementSet.select(source, new Match(CHART_LOC,"*"));
				DeferredTransform transform = new DeferredTransform(set);
				spec.appendChild(transform.transformElementSet(content));
				return emitDeferredChart(spec, caption);
			}catch(Exception e) {
				addError("Error generating deferred chart chart:"+name, e.getMessage(), source, e);
			}
			
			return null;
		}else {
			try {
				Period period = makePeriod(period_element);
				
				
				
				PlotEntry plot_entry = getPlotEntry(set, source);
				MapperEntry mapper_entry = getMapperEntry(set, source);
				Chart chart=null;
				String extra=null;
				boolean new_mapper =false;
				switch(name) {
				case "TimeChart": 
					chart = makeTimeChart(period, source); 
					extra = "AddChart";
					new_mapper=true;
					break;
				case "PieTimeChart":
					chart = makePieTimeChart(period, source);
					break;
				case "BarTimeChart":
					chart = makeBarTimeChart(period, source); 
					extra = "AddSeries";
					break;
				}
				if( chart == null) {
					addError("Unrecognised chart type", name);
					return result;
				}

				Plot ds = makeDataSet(set, plot_entry, mapper_entry, chart, source);
				for(Element add_data : ElementSet.select(source, new Match(CHART_LOC, "AddData"))) {
					RecordSet set2 = addFilterElementSet(set, ElementSet.select(add_data, new Match(FILTER_LOC, FILTER_ELEMENT)));
					PlotEntry plot2 = getPlotEntry(set2, add_data);
					Plot ds2 = makeDataSet(ds,set2, plot2, mapper_entry, chart, add_data);
				}
				result.appendChild(addPlot(ds, set, plot_entry, mapper_entry, chart, source));
				if( extra != null) {
					for(Element add_chart: ElementSet.select(source, new Match(CHART_LOC, extra))) {
						RecordSet set2 = addFilterElementSet(set, ElementSet.select(add_chart,new Match( FILTER_LOC, FILTER_ELEMENT)));
						PlotEntry plot_entry2 = getPlotEntry(set2, add_chart);
						MapperEntry mapper_entry2;
						if( new_mapper) {
						   mapper_entry2 = getMapperEntry(set2, add_chart);
						}else {
							mapper_entry2= mapper_entry;
						}
						Plot dsd = makeDataSet(set2, plot_entry2, mapper_entry2, chart, add_chart);
						for(Element add_data : ElementSet.select(add_chart, new Match(CHART_LOC, "AddData"))) {
							RecordSet set3 = addFilterElementSet(set2, ElementSet.select(add_data, new Match(FILTER_LOC, FILTER_ELEMENT)));
							PlotEntry plot3 = getPlotEntry(set3, add_data);
							Plot ds3 = makeDataSet(dsd,set3, plot3, mapper_entry2, chart, add_data);
						}
						result.appendChild(addPlot(dsd, set2, plot_entry2, mapper_entry2, chart, add_chart));
					}
				}
				if( hasData(chart)) {
					if( graphic_output) {
						result.appendChild(addChart(chart, caption));
					}
					if( table_output) {
						result.appendChild(addChartTable(chart, caption));
					}
				}else {
					if( ! quiet) {
						result.appendChild(addNoData(chart));
					}
				}
			}catch(LimitException le) {
				throw le;
			}catch(Exception e) {
				addError("Bad chart element chart:"+name, e.getMessage(), source, e);
			}
			return result;
		}
	}

	
	
}