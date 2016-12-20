// Copyright - The University of Edinburgh 2015
/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.aggregation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.DateReductionTarget;
import uk.ac.ed.epcc.safe.accounting.NumberSumReductionTarget;
import uk.ac.ed.epcc.safe.accounting.Reduction;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.db.ConfigUsageRecordFactory;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.webapp.WebappTestBase;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.junit4.DataBaseFixtures;


public class AggregateUsageRecordFactoryTest extends WebappTestBase {

	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testProperties(){
		AggregateUsageRecordFactory fac = new DailyAggregateUsageRecordFactory(ctx,"DailyAggregate");
		for(PropertyTag<?> t : fac.getSumProperties()){
			assertTrue(t.getName(),fac.hasProperty(t));
			assertTrue("Sum prop"+t.getName()+" is writtable",fac.getAccessorMap().writable(t));
		}
		for(PropertyTag<?> t : fac.getKeyProperties()){
			assertTrue(t.getName(),fac.hasProperty(t));
		}
	}
	@SuppressWarnings("unchecked")
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testDerived() throws Exception{
		ConfigUsageRecordFactory sge_fac = new ConfigUsageRecordFactory(ctx, "SGERecord");
		AggregateUsageRecordFactory fac = ctx.makeParamObject(DailyAggregateUsageRecordFactory.class,ctx,"DailyAggregate",sge_fac);
		
		PropExpressionMap orig = sge_fac.getDerivedProperties();
		assertNotNull(orig);
		PropExpressionMap agg = fac.getDerivedProperties();
		assertNotNull(agg);
		
		for(PropertyTag t : orig.keySet()){
			PropExpression orig_expr = orig.get(t);
			assertNotNull(orig_expr);
			System.out.println(orig_expr.toString());
			if( orig_expr.getTarget() != Date.class){
			PropExpression agg_expr = agg.get(t);
			
			if( agg_expr != null ){ 
			
			System.out.println(agg_expr.toString());
			
				assertTrue(orig_expr.equals(agg_expr));
			}else{
				System.out.println("Not implmented in aggregate");
			}
			}
		}
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testMapping() throws Exception{
		ConfigUsageRecordFactory sge_fac = new ConfigUsageRecordFactory(ctx, "SGERecord");
		AggregateUsageRecordFactory fac = ctx.makeParamObject(DailyAggregateUsageRecordFactory.class,ctx,"DailyAggregate",sge_fac);
		Date point=new Date();
		
		Date start = fac.mapStart(point);
		Date end = fac.mapEnd(point);
		assertTrue("Start before point",start.before(point));
		assertTrue("end after start",end.after(start));
		assertFalse("end not before point",end.before(point));
		Date start2 = fac.mapStart(start);
		Date start3 = fac.mapEnd(start);
		assertTrue("start2 before start",start2.before(start));
		assertTrue("end2 = end",start3.equals(start));
		Calendar s = Calendar.getInstance();
		Calendar e = Calendar.getInstance();
		s.setTime(start);
		e.setTime(end);
		s.add(Calendar.DAY_OF_YEAR,1);
		assertTrue("Day period ",s.equals(e));
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testaggregate() throws Exception{
		ConfigUsageRecordFactory sge_fac = new ConfigUsageRecordFactory(ctx, "SGERecord");
		AggregateUsageRecordFactory fac = ctx.makeParamObject(DailyAggregateUsageRecordFactory.class,ctx,"DailyAggregate",sge_fac);
		UsageProducer<?> raw_fac = fac.getMaster();
		Date start = raw_fac.getReduction(new DateReductionTarget(Reduction.MIN,StandardProperties.ENDED_PROP), new AndRecordSelector());
		Date end = raw_fac.getReduction(new DateReductionTarget(Reduction.MAX,StandardProperties.ENDED_PROP), new AndRecordSelector());
		if( start == null || end == null){
			// nothing to regenerate
			return;
		}
		AndRecordSelector sel = new AndRecordSelector();
		sel.add(new SelectClause<Date>(StandardProperties.ENDED_PROP,MatchCondition.GT,fac.mapStart(start)));
		sel.add(new SelectClause<Date>(StandardProperties.ENDED_PROP,MatchCondition.LE,fac.mapEnd(end)));
		long raw_count = raw_fac.getRecordCount(sel);
		if( raw_count > 100000){
			System.out.println("Supress line by line regenerate, too many records");
			return;
		}
		for(Iterator<? extends ExpressionTargetContainer> it = raw_fac.getIterator(sel); it.hasNext();){
			ExpressionTargetContainer rec = it.next();
			fac.aggregate(rec);
		}
		
		verify(fac, raw_fac, sel);
		
		long count = fac.getRecordCount(sel);
		System.out.println("Raw count "+raw_count);
		System.out.println("Agregate count "+count);
		assertTrue("has aggreagated", count < raw_count);
		assertTrue("has differentiated", count > 1L);
		
	    assertEquals(raw_count, fac.getRawCounter());
		
		Date aggregate_start = fac.getReduction(new DateReductionTarget(Reduction.MIN,StandardProperties.STARTED_PROP),sel);
		Date raw_first = raw_fac.getReduction(new DateReductionTarget(Reduction.MIN,StandardProperties.ENDED_PROP), sel);
		System.out.println("Aggregate starts "+aggregate_start.toString());
		System.out.println("First end of raw "+raw_first.toString());
		assertTrue("Start before first end", ! aggregate_start.after(raw_first));
		Date aggregate_end = fac.getReduction(new DateReductionTarget(Reduction.MAX,StandardProperties.ENDED_PROP), sel);
		Date raw_last = raw_fac.getReduction(new DateReductionTarget(Reduction.MAX,StandardProperties.ENDED_PROP), sel);
		assertTrue("End after last end", ! aggregate_end.before(raw_last));
		
	}
	
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testRegenerate() throws Exception{
		AggregateUsageRecordFactory fac = new DailyAggregateUsageRecordFactory(ctx,"DailyAggregate");
		UsageProducer<?> raw_fac = fac.getMaster();
		Date start = raw_fac.getReduction(new DateReductionTarget(Reduction.MIN,StandardProperties.ENDED_PROP), new AndRecordSelector());
		Date end = raw_fac.getReduction(new DateReductionTarget(Reduction.MAX,StandardProperties.ENDED_PROP), new AndRecordSelector());
		if( start == null || end == null){
			// nothing to regenerate
			return;
		}
		AndRecordSelector sel = new AndRecordSelector();
		sel.add(new SelectClause<Date>(StandardProperties.ENDED_PROP,MatchCondition.GT,fac.mapStart(start)));
		sel.add(new SelectClause<Date>(StandardProperties.ENDED_PROP,MatchCondition.LE,fac.mapEnd(end)));
		long raw_count = raw_fac.getRecordCount(sel);
		if( raw_count > 100000){
			System.out.println("Supress line by line regenerate, too many records");
			return;
		}
		fac.regenerate();
		
		
		verify(fac, raw_fac, sel);
		
		long count = fac.getRecordCount(sel);
		System.out.println("Raw count "+raw_count);
		System.out.println("Agregate count "+count);
		assertTrue("has aggreagated", count < raw_count);
		assertTrue("has differentiated", count > 1L);
		
	
		
		Date aggregate_start = fac.getReduction(new DateReductionTarget(Reduction.MIN,StandardProperties.STARTED_PROP),sel);
		Date raw_first = raw_fac.getReduction(new DateReductionTarget(Reduction.MIN,StandardProperties.ENDED_PROP), sel);
		System.out.println("Aggregate starts "+aggregate_start.toString());
		System.out.println("First end of raw "+raw_first.toString());
		assertTrue("Start before first end", ! aggregate_start.after(raw_first));
		Date aggregate_end = fac.getReduction(new DateReductionTarget(Reduction.MAX,StandardProperties.ENDED_PROP),sel);
		Date raw_last = raw_fac.getReduction(new DateReductionTarget(Reduction.MAX,StandardProperties.ENDED_PROP), sel);
		assertTrue("End after last end", ! aggregate_end.before(raw_last));
		
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testRegenerate2() throws Exception{
		AggregateUsageRecordFactory fac = new DailyAggregateUsageRecordFactory(ctx,"DailyAggregate");
		UsageProducer<?> raw_fac = fac.getMaster();
		
		Date start = raw_fac.getReduction(new DateReductionTarget(Reduction.MIN,StandardProperties.ENDED_PROP), new AndRecordSelector());
		Date end = raw_fac.getReduction(new DateReductionTarget(Reduction.MAX,StandardProperties.ENDED_PROP), new AndRecordSelector());
		if( start == null || end == null){
			// nothing to regenerate
			return;
		}
		System.out.println("Start is "+start);
		System.out.println("End is "+end);
		fac.regenerate(start,end);
		
		
		AndRecordSelector sel = new AndRecordSelector();
		sel.add(new SelectClause<Date>(StandardProperties.ENDED_PROP,MatchCondition.GT,fac.mapStart(start)));
		sel.add(new SelectClause<Date>(StandardProperties.ENDED_PROP,MatchCondition.LE,fac.mapEnd(end)));
		verify(fac, raw_fac, sel);
		long count = fac.getRecordCount(sel);
		long raw_count = raw_fac.getRecordCount(sel);
		System.out.println("Raw count "+raw_count);
		System.out.println("Agregate count "+count);
		assertTrue("has aggreagated", count < raw_count);
		assertTrue("has differentiated", count > 1L);
		
	
		
		Date aggregate_start = fac.getReduction(new DateReductionTarget(Reduction.MIN,StandardProperties.STARTED_PROP),sel);
		Date raw_first = raw_fac.getReduction(new DateReductionTarget(Reduction.MIN,StandardProperties.ENDED_PROP), sel);
		System.out.println("Aggregate starts "+aggregate_start.toString());
		System.out.println("First end of raw "+raw_first.toString());
		assertTrue("Start before first end", ! aggregate_start.after(raw_first));
		Date aggregate_end = fac.getReduction(new DateReductionTarget(Reduction.MAX,StandardProperties.ENDED_PROP),sel);
		Date raw_last = raw_fac.getReduction(new DateReductionTarget(Reduction.MAX,StandardProperties.ENDED_PROP), sel);
		assertTrue("End after last end", ! aggregate_end.before(raw_last));
		// now a second time
		
		fac.regenerate();
		long count2 = fac.getRecordCount(sel);
		assertEquals("Same counnt after regenerate",count, count2);
		verify(fac, raw_fac, sel);
		fac.regenerate(start,end);
		long count3 = fac.getRecordCount(sel);
		assertEquals("Same counnt after re-apply",count, count3);
		verify(fac, raw_fac, sel);
	}
	@SuppressWarnings("unchecked")
	private void verify(AggregateUsageRecordFactory fac, UsageProducer<?> raw_fac,
			AndRecordSelector sel) throws Exception {
		for( PropertyTag t : fac.getAccessorMap().getProperties()){
			if( Number.class.isAssignableFrom(t.getTarget())){
				
				Number agg_sum = fac.getReduction(new NumberSumReductionTarget(t), sel);
				System.out.println("Aggregated "+t.getName()+" = "+agg_sum);
				Number raw_sum = raw_fac.getReduction(new NumberSumReductionTarget( t),sel);
				System.out.println("raw "+t.getName()+" = "+raw_sum);
				if( raw_sum.getClass() == Double.class || raw_sum.getClass() == Float.class){
				  double diff = agg_sum.doubleValue() - raw_sum.doubleValue();
				  System.out.println("Diff "+t.getName()+" = "+diff);
				  if( raw_sum.doubleValue() > 0.0){
					  diff = (diff*diff)/raw_sum.doubleValue();
					  System.out.println("Diff "+t.getName()+" = "+diff);
					  assertTrue("Diff "+t.getName()+" "+diff, diff < 1.0e-5);
				  }
				}else{
					assertEquals("Sum "+t.getName(),raw_sum,agg_sum);
				}
			}
		}
	}
	@Test
	@DataBaseFixtures({"Eddie.xml"})
	public void testHourlyRegenerate2() throws Exception{
		AggregateUsageRecordFactory fac = new HourlyAggregateUsageRecordFactory(ctx,"DailyAggregate");
		UsageProducer<?> raw_fac = fac.getMaster();
		
		Date start = raw_fac.getReduction(new DateReductionTarget(Reduction.MIN,StandardProperties.ENDED_PROP), new AndRecordSelector());
		Date end = raw_fac.getReduction(new DateReductionTarget(Reduction.MAX,StandardProperties.ENDED_PROP), new AndRecordSelector());
		if( start == null || end == null){
			// nothing to regenerate
			return;
		}
		System.out.println("Start is "+start);
		System.out.println("End is "+end);
		fac.regenerate(start,end);
		
		
		AndRecordSelector sel = new AndRecordSelector();
		sel.add(new SelectClause<Date>(StandardProperties.ENDED_PROP,MatchCondition.GT,fac.mapStart(start)));
		sel.add(new SelectClause<Date>(StandardProperties.ENDED_PROP,MatchCondition.LE,fac.mapEnd(end)));
		verify(fac, raw_fac, sel);
		long count = fac.getRecordCount(sel);
		long raw_count = raw_fac.getRecordCount(sel);
		System.out.println("Raw count "+raw_count);
		System.out.println("Agregate count "+count);
		assertTrue("has aggreagated", count < raw_count);
		assertTrue("has differentiated", count > 1L);
		
	
		
		Date aggregate_start = fac.getReduction(new DateReductionTarget(Reduction.MIN,StandardProperties.STARTED_PROP),sel);
		Date raw_first = raw_fac.getReduction(new DateReductionTarget(Reduction.MIN,StandardProperties.ENDED_PROP), sel);
		System.out.println("Aggregate starts "+aggregate_start.toString());
		System.out.println("First end of raw "+raw_first.toString());
		assertTrue("Start before first end", ! aggregate_start.after(raw_first));
		Date aggregate_end = fac.getReduction(new DateReductionTarget(Reduction.MAX,StandardProperties.ENDED_PROP),sel);
		Date raw_last = raw_fac.getReduction(new DateReductionTarget(Reduction.MAX,StandardProperties.ENDED_PROP), sel);
		assertTrue("End after last end", ! aggregate_end.before(raw_last));
		
	}
	
}