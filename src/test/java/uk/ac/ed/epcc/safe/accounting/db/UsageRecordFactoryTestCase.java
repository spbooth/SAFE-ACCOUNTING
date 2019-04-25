// Copyright - The University of Edinburgh 2015
package uk.ac.ed.epcc.safe.accounting.db;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.DateReductionTarget;
import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.charts.MapperEntry;
import uk.ac.ed.epcc.safe.accounting.charts.PlotEntry;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionCast;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.webapp.charts.PeriodSequencePlot;
import uk.ac.ed.epcc.webapp.charts.TimeChart;
import uk.ac.ed.epcc.webapp.charts.strategy.SetRangeMapper;
import uk.ac.ed.epcc.webapp.jdbc.expr.Reduction;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.model.data.CloseableIterator;
public abstract class UsageRecordFactoryTestCase<F extends UsageRecordFactory<T>,T extends UsageRecordFactory.Use> extends DataObjectPropertyFactoryTestCase<F, T> {

	
	@Test
	public void testFirstUse() throws Exception {

		UsageRecordFactory<T> fac = getFactory();
		if (!fac.isValid()) {
			return;
		}
		if(fac.hasProperty(StandardProperties.STARTED_PROP)){
			AndRecordSelector selector = new AndRecordSelector();
			selector.add(new SelectClause<>(StandardProperties.STARTED_PROP, MatchCondition.GT, new Date(0L)));
			if( fac.exists(fac.getFilter(selector))) {
					// exclude failed jobs

					Date d = fac.getReduction(new DateReductionTarget(Reduction.MIN,StandardProperties.STARTED_PROP),selector);
					assertNotNull(d);
					//System.out.println("Reduction "+d.toString());
					assertTrue("after epoch", d.after(new Date(0L)));
					assertTrue(" before now", d.before(new Date()));

			}
		}
	}
	@Test
	public void testLastUse() throws Throwable {
	
		UsageRecordFactory<T> fac = getFactory();
		if (!fac.isValid()) {
			return;
		}
		if(fac.hasProperty(StandardProperties.ENDED_PROP)){
			Date d = null;

			AndRecordSelector selector = new AndRecordSelector();
			if( fac.exists(fac.getFilter(selector))) {
					d = fac.getReduction(new DateReductionTarget(Reduction.MAX,StandardProperties.ENDED_PROP),selector);
					assertNotNull(d);
					//System.out.println(d.toString());
					assertTrue("after epoch", d.after(new Date(0L)));
					assertTrue(" before now", d.before(new Date()));
			}

		}
	}
	@Test
	public void testgetImplemenationInfo() {
	
		UsageRecordFactory<T> fac = getFactory();
		if (!fac.isValid()) {
			return;
		}
	
		PropertyFinder finder = fac.getFinder();
		ExpressionTargetFactory etf = ExpressionCast.getExpressionTargetFactory(fac);
		for (PropertyTag tag : finder.getProperties()) {
			String info = etf.getAccessorMap().getImplemenationInfo(tag);
			assertNotNull(info);
		}
	}
	
	 @Test
	 @Ignore
	public void testGetMappers() throws Exception {

		UsageRecordFactory<T> fac = getFactory();
		if (!fac.isValid()) {
			return;
		}
		Set<PlotEntry> pset = PlotEntry.getMappers(ctx, fac);
		assertNotNull(pset);
		for (PlotEntry pe : pset) {
			//System.out.println("Consider plot " + pe.getDescription());
			Set<MapperEntry> set = MapperEntry.getMappers(ctx, fac);
			assertNotNull(set);
			for (MapperEntry e : set) {
				assertTrue(e.compatible(fac));
				//System.out.println(pe.getDescription() + "-" + e.getDescription());
				SetRangeMapper m = e.getMapper(pe);
				assertNotNull(m);
				Calendar start = getDataStart();
				TimeChart tc = TimeChart.getInstance(ctx, start, Calendar.WEEK_OF_YEAR,
						2, 4, 10);
				
				// Only looking for exceptions even if no data is loaded a single value plot where the
				// reduction has a default result will show as having data and its more damaging to change this
				// than having the test
				
				//assertEquals(expectData(),e.plot(true,pe, tc, fac, new AndRecordSelector(), 20,true));
				plot(e,true,pe, tc, fac, new AndRecordSelector(), 20,true);
				TimeChart short_tc = TimeChart.getInstance(ctx, start,
						Calendar.HOUR_OF_DAY, 1, 4, 10);
				//assertEquals(expectData(),e.plot(true,pe, short_tc, fac, new AndRecordSelector(), 20,true));
				
				plot(e,true,pe, short_tc, fac, new AndRecordSelector(), 20,true);
			}
		}
		
		
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
	    public boolean plot(MapperEntry m,boolean graph_transforms,PlotEntry e, TimeChart tc, UsageProducer<?> ap,RecordSelector sel, int nplots,boolean allow_overlap) throws Exception {
	    	boolean use_line = m.getUseLine();
			PeriodSequencePlot ds = m.makeTimeChartPlot(e, tc, ap, sel, nplots, allow_overlap, use_line, graph_transforms);
			
			if( ds != null ){
				m.plotTimeChart(e, tc, ds, nplots, use_line,graph_transforms);
				return true;
			}
			return false;
	    }
	/** get the start date for a period of existing data available to
	 * the test.
	 * @return
	 */
	protected Calendar getDataStart() {
		Calendar start = Calendar.getInstance();
		start.set(2008,Calendar.OCTOBER, 1, 4, 0);
		return start;
	}
}