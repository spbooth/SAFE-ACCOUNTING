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
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.charts.ChartService;
import uk.ac.ed.epcc.safe.accounting.charts.KeyMapperEntry;
import uk.ac.ed.epcc.safe.accounting.charts.MapperEntry;
import uk.ac.ed.epcc.safe.accounting.charts.PlotEntry;
import uk.ac.ed.epcc.safe.accounting.charts.SetMapperEntry;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.reports.exceptions.FormatException;
import uk.ac.ed.epcc.safe.accounting.reports.exceptions.ReportException;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.charts.BarTimeChart;
import uk.ac.ed.epcc.webapp.charts.BarTimeChartData;
import uk.ac.ed.epcc.webapp.charts.PeriodChart;
import uk.ac.ed.epcc.webapp.charts.PeriodSequencePlot;
import uk.ac.ed.epcc.webapp.charts.PeriodSetPlot;
import uk.ac.ed.epcc.webapp.charts.PieTimeChart;
import uk.ac.ed.epcc.webapp.charts.Plot;
import uk.ac.ed.epcc.webapp.charts.SetPeriodChart;
import uk.ac.ed.epcc.webapp.charts.TimeChart;
import uk.ac.ed.epcc.webapp.content.Table;
import uk.ac.ed.epcc.webapp.exceptions.InvalidArgument;
import uk.ac.ed.epcc.webapp.jdbc.expr.Reduction;
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
	private static final String NORM_ELEMENT = "Norm";
	private static final String REDUCTION_ELEMENT = "Reduction";
	private static final String SCALE_ELEMENT = "Scale";
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
		return c.has_data;
	}
	private final ChartService serv;
	public ChartExtension(AppContext c,NumberFormat nf) throws ParserConfigurationException {
		super(c,nf);
		serv=c.getService(ChartService.class);
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
				throw new ReportException("No Plot quantity was specified");
			}
			start_name = getParam(START_PROP_ELEMENT, e);
			end_name = getParam(END_PROP_ELEMENT,e);
			result = serv.getPlotEntry( errors,finder, name,start_name,end_name);
		}
		if( result == null ){
			addError("Invalid Plot Quantity","The specified quantity "+name+" does not correspond to a plottable quantity or named PlotEntry");
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
		Element norm_e = getParamElement(NORM_ELEMENT, e);
		if( norm_e != null ) {
			PlotEntry norm = getPlotEntry(set, norm_e);
			result.setNorm(norm);
		}
		
	
		return result;
	}
	/** serialise a PlotEntry to XML in a way that {@link #getPlotEntry(RecordSet, Node)}
	 * can re-create it.
	 * 
	 * @param e
	 * @return new PlotEntry that should match the one formatted
	 * @throws Exception 
	 * @throws DOMException 
	 */
	public PlotEntry formatPlotEntry(RecordSet set,PlotEntry e,Node entry) throws DOMException, Exception {
		Document doc = getDocument();
		if( e == null) {
			return null;
		}
		PropertyFinder finder = set.getFinder();
		Element plot = doc.createElementNS(CHART_LOC, PLOT_ELEMENT);
		entry.appendChild(plot);
		plot.appendChild(doc.createTextNode(e.getQualifiedName()));

		PlotEntry cand = serv.getPlotEntry(errors, finder, e.getQualifiedName(), formatPropExpression(e.getStartProperty()), formatPropExpression(e.getEndProperty()));
		if( cand == null) {
			throw new FormatException("Cannot recreate PlotEntry "+e.getQualifiedName());
		}
		
		
		String label = e.getLabel();
		if( label != null  && ! label.equals(cand.getLabel())) {
			Element p_label = doc.createElementNS(CHART_LOC, PLOT_LABEL_ELEMENT);
			entry.appendChild(p_label);
			p_label.appendChild(doc.createTextNode(label));
			cand.setLabel(label);
		}
		Reduction r = e.getReduction();
		if( r != null && ! r.equals(cand.getReduction())) {
			Element red = doc.createElementNS(CHART_LOC, REDUCTION_ELEMENT);
			entry.appendChild(red);
			red.appendChild(doc.createTextNode(r.name()));
			cand.setReduction(r);
		}
		// always set time props as we used themto make the candidate
		PropExpression start = e.getStartProperty();
		if( start != null ) {
			Element start_e = doc.createElementNS(CHART_LOC, START_PROP_ELEMENT);
			entry.appendChild(start_e);
			start_e.appendChild(doc.createTextNode(formatPropExpression(start)));
		}
		PropExpression end = e.getEndProperty();
		if( end != null ) {
			Element end_e = doc.createElementNS(CHART_LOC, END_PROP_ELEMENT);
			entry.appendChild(end_e);
			end_e.appendChild(doc.createTextNode(formatPropExpression(end)));
		}
		
		double scale = e.getScale();
		if( scale != cand.getScale()) {
			Element scale_e = doc.createElementNS(CHART_LOC, SCALE_ELEMENT);
			entry.appendChild(scale_e);
			scale_e.appendChild(doc.createTextNode(Double.toString(scale)));
			cand.setScale(scale);
		}
		boolean ratescale = e.isRateScale();
		if( ratescale != cand.isRateScale()) {
			Element rate = doc.createElementNS(CHART_LOC, RATE_SCALE_ELEMENT);
			entry.appendChild(rate);
			rate.appendChild(doc.createTextNode(Boolean.toString(ratescale)));
			cand.setRateScale(ratescale);
		}
		PlotEntry norm = e.getNorm();
		if( norm != null && ! norm.equals(cand.getNorm())) {
			Element norm_e = doc.createElementNS(CHART_LOC, NORM_ELEMENT);
			entry.appendChild(norm_e);
			cand.setNorm(formatPlotEntry(set,norm,norm_e));
			
		}
		
		return cand;
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
	public MapperEntry formatMapperEntry(RecordSet set,MapperEntry m , Node result) throws DOMException, Exception {
		if( m == null) {
			return null;
		}
		Document doc = getDocument();
		PropertyFinder finder = set.getFinder();
		String name = m.getQualifiedName();
		MapperEntry cand = serv.getMapperEntry(errors, finder, name);
		if( cand == null ) {
			throw new FormatException("Cannot create candidate MapperEntry from "+name);
		}
		if( name != null && ! name.isEmpty()) {
			Element group_by = doc.createElementNS(CHART_LOC, GROUP_BY_ELEMENT);
			result.appendChild(group_by);
			group_by.appendChild(doc.createTextNode(name));
		}
		
			
		if( m instanceof SetMapperEntry) {
			if( ! (cand instanceof SetMapperEntry)) {
				throw new FormatException("Candidate does not match class of original");
			}
			SetMapperEntry s = (SetMapperEntry) m;
			String label = s.getLabel();
			if(label != null && ! label.equals(((SetMapperEntry)cand).getLabel())) {
				result.appendChild(doc.createElementNS(CHART_LOC, LABEL_ELEMENT)).appendChild(doc.createTextNode(label));
				((SetMapperEntry)cand).setLabel(label);
			}
		}
		if( cand.getUseLine() != m.getUseLine()) {
			result.appendChild(doc.createElementNS(CHART_LOC, LINE_ELEMENT)).appendChild(doc.createTextNode(Boolean.toString(m.getUseLine())));
			cand.setUseLine(m.getUseLine());
		}
		if( cand.isCumulative() != m.isCumulative()) {
			result.appendChild(doc.createElementNS(CHART_LOC, CUMULATIVE_ELEMENT)).appendChild(doc.createTextNode(Boolean.toString(m.isCumulative())));
			cand.setCumulative(m.isCumulative());
		}
		if( cand.isStacked() != m.isStacked()) {
			result.appendChild(doc.createElementNS(CHART_LOC, STACKED_ELEMENT)).appendChild(doc.createTextNode(Boolean.toString(m.isStacked())));
			cand.setStacked(m.isStacked());
		}

	    Color cols[] = m.getColours();
	    if( cols != null && cols.length > 0 && ! cols.equals(cand.getColours())) {
	    	String text[] = new String[cols.length];
	    	for(int i=0 ; i < cols.length; i++) {
	    		text[i] = Integer.toHexString(cols[i].getRGB());
	    	}
	    	result.appendChild(doc.createElementNS(CHART_LOC, COLOURS_ELEMENT)).appendChild(doc.createTextNode(String.join(" ", text)));
	    	cand.setColours(cols);
	    }
		return cand;
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
	public void copyChartOptions(Element dest, Element src) throws DOMException, ReportException {
		
		copyParams(dest, src, NUMBER_OF_TIME_BLOCKS_ELEMENT, WARNING_LEVEL_ELEMENT,TITLE_ELEMENT, QUANTITY_ELEMENT,N_PLOTS_ELEMENT);
	}
	public void copyParams(Element dest, Element src, String ... elements) throws ReportException {
		Document doc = getDocument();
		for(String name : elements) {
			if( hasParam(name, src)) {
				dest.appendChild(doc.createElementNS(CHART_LOC, name)).appendChild(doc.createTextNode(getParam(name, src)));
			}
		}
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
			// changed.
			addError("Bad Plot","Usage producer not compatible with current MapperEntry");
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
	/** Creates a stand-alone XML fragment specifying the chart
	 * 
	 * @param set
	 * @param period
	 * @param n
	 * @param caption
	 * @return
	 */
	public Node addDeferredChart(RecordSet set,Period period,PlotEntry plot,MapperEntry mapper,Node n) {
		// Default null implementation
		Document doc = getDocument();
		DocumentFragment result = doc.createDocumentFragment();
		return result;
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
}