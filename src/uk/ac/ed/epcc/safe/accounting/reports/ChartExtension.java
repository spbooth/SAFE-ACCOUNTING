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
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import uk.ac.ed.epcc.safe.accounting.Reduction;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.charts.ChartService;
import uk.ac.ed.epcc.safe.accounting.charts.MapperEntry;
import uk.ac.ed.epcc.safe.accounting.charts.PlotEntry;
import uk.ac.ed.epcc.safe.accounting.charts.SetMapperEntry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.charts.BarTimeChart;
import uk.ac.ed.epcc.webapp.charts.PeriodChart;
import uk.ac.ed.epcc.webapp.charts.PieTimeChart;
import uk.ac.ed.epcc.webapp.charts.TimeChart;
import uk.ac.ed.epcc.webapp.content.Table;
import uk.ac.ed.epcc.webapp.exceptions.InvalidArgument;
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
	/** composite object holding the {@link PeriodChart}
	 * and a record if any data has been added
	 * 
	 * @author spb
	 * @param <P> 
	 *
	 */
	public static class Chart<P extends PeriodChart>{
		public Chart(P pc){
			this.chart=pc;
		}
		public P chart;
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
	public PlotEntry getPlotEntry(PropertyFinder finder, Element e) throws Exception{
		String name = getParam("Plot",e);
		if( name == null ){
			addError("No Plot Quantity","No Plot quantity was specified");
			return null;
		}
		String start_name = getParam("StartProp", e);
		String end_name = getParam("EndProp",e);
		PlotEntry result = serv.getPlotEntry( finder, name,start_name,end_name);
		if( start_name != null && start_name.trim().length() > 0 && result.getStartProperty() == null ){
			addError("Bad property", "StartProp value "+start_name+" failed to parse", e);
		}
		if( end_name != null && end_name.trim().length() > 0 && result.getEndProperty() == null ){
			addError("Bad property", "EndProp value "+end_name+" failed to parse", e);
		}
		if( result == null ){
			addError("Invalid Plot Quantity","The specified quantity "+name+" does not correspond to a plottable quantity");
		}
		//TODO make this an element with additional params for label and scale.
		result.setRateScale( getBooleanParam("RateScale",result.isRateScale(), e));
		result.setScale(getNumberParam("Scale", result.getScale(), e).doubleValue());
		String red = getParam("Reduction", e);
		if( red != null){
			try{
				result.setReduction(Reduction.valueOf(red));
			}catch(Throwable t){
				addError("Bad Reduction", red, t);
			}
		}
		String label = getParam("PlotLabel", e);
		if( label != null ){
			result.setLabel(label);
		}
		return result;
	}
	public MapperEntry getMapperEntry(PlotEntry p,PropertyFinder finder, Element e) throws Exception{
		MapperEntry entry;
		AppContext ctx = getContext();
		if (hasParam("GroupBy", e)) {
			entry = serv.getMapperEntry(finder,getParam("GroupBy", e));
			
		} else {
			entry = serv.getMapperEntry( finder, "");
			if( hasParam("Label", e)){
				((SetMapperEntry)entry).setLabel(getParam("Label", e));
			}else{
				// multiple plots need labels gor each set
				// so always set these
				((SetMapperEntry)entry).setLabel(p.getLabel());
			}
		}
		if( hasParam("Line", e)){
			entry.setUseLine(getBooleanParam("Line", false, e));
		}
		if( hasParam("Cumulative", e)){
			entry.setCumulative(getBooleanParam("Cumulative", false, e));
		}
		if(hasParam("Colours", e)){
			List<Color> list = new LinkedList<Color>();
			for(String s : getParam("Colours", e).split("\\s+")){
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
		if( hasParam("Title", e)){
			chart.getChartData().setTitle(getParam("Title", e).trim());
		}
		}catch(Exception e1){
			addError("Bad Plot", "Error setting title", e1);
		}
		return new Chart<P>(chart);
	}
	public Chart<TimeChart> makeTimeChart(Period period, Node node) {
		
		if (node.getNodeType() != Node.ELEMENT_NODE) {
			addError("Bad Plot", "Non element fragment passed to makeTimeChart");
			return null;
		}
		
		
		Element e = (Element) node;
		AppContext ctx = getContext();
		
		int timeBlocks = getContext().getIntegerParameter("timechart.default.NumberOfTimeBlocks", 10);
		try {
			timeBlocks = getIntParam("NumberOfTimeBlocks",timeBlocks , e);
		} catch (Exception e1) {
			addError("Bad Plot", "Error setting NumberOfTimeBlocks", e1);
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
			try{
			if( hasParam("WarningLevel", e)){
				chart.addWarningLevel(getNumberParam("WarningLevel", 0.0, e).doubleValue());
			}
			}catch(Exception e3){
				addError("Bad Timechart specification", "Error setting WarningLevel",e3);
			}
			return setChartOptions(chart,e);
		} catch (InvalidArgument e2) {
			addError("Bad Timechart specification", e2.getClass().getCanonicalName(),e2);
			return null;
		}
		

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
		Document doc = getDocument();
		DocumentFragment result = doc.createDocumentFragment();
		if( chart == null){
			return result;
		}
		if (node.getNodeType() != Node.ELEMENT_NODE) {
			addError("Bad Plot", "Non element fragment passed to makeTimeChart");
			return result;
		}
		
		Element e = (Element) node;
		try{
			UsageProducer up = set.getUsageProducer();
			if( up == null ){
				addError("No UsageProducer","No UsageProducer defined in addPlot");
				return result;
			}
			PropertyFinder finder = up.getFinder();

			PlotEntry plot = getPlotEntry(finder, e);
			if( plot == null ){
				return result;
			}
			MapperEntry entry = getMapperEntry(plot,finder, e);


			int nPlots = getIntParam("NPlots", 10, e);
			boolean overlap = getBooleanParam("Overlap", true, e);
			RecordSelector sel = set.getRecordSelector();
			
			chart.has_data =  entry.plot(graphOutput(),plot,chart.chart, up, sel, nPlots,overlap) || chart.has_data;
			
			return result;
		}catch(Throwable t){
			addError("Error in plot", t.getClass().getCanonicalName(), t);
			return result;
		}
	}
	public Chart<PieTimeChart> makePieTimeChart(Period period, Node node) {
		
		if (node.getNodeType() != Node.ELEMENT_NODE) {
			addError("Bad Plot", "Non element fragment passed to makeChart");
			return null;
		}
		
		AppContext ctx = getContext();
	
		PieTimeChart chart = PieTimeChart.getInstance(ctx, (SplitPeriod) period);
		chart.getChartData().setGraphical(graphOutput());
		return  setChartOptions(chart,(Element) node);
		
		
		

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
			return  setChartOptions(chart,(Element) node);
		} catch (Exception e) {
			addError("Bad Plot", "Error making BarChart", e);
			return null;
		}
	
		

	}

	public DocumentFragment addChartTable(Chart chart,String caption) throws Exception {
		   
		
		Table t = chart.chart.getTable();
		if (t.hasData()) {
			return format(t, null);
			//TODO handle caption
		} else {
			Document doc = getDocument();
			DocumentFragment result = doc.createDocumentFragment();
			Element e = doc.createElement("NoData");
			result.appendChild(e);
			return result;
		}
	}
	
	public abstract DocumentFragment addChart(Chart chart,String caption) throws Exception;
	
	
	public abstract boolean graphOutput();

}