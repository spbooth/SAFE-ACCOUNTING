package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.expr.ArrayFuncPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.DeRefExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropertyCastException;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceTag;
import uk.ac.ed.epcc.safe.accounting.selector.OverlapType;
import uk.ac.ed.epcc.safe.accounting.selector.RelationClause;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.jdbc.expr.ArrayFunc;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.time.Period;
import uk.ac.ed.epcc.webapp.time.TimePeriod;
/** A {@link TupleUsageProducer} that represents a time-sequence join
 * 
 * All records in the nested factories are assumes to implement
 * the {@link StandardProperties#time} time properties. a mandatory filter
 * restricts the selection to tuples that overlap in time and derived
 * properties select the appropriate time bounds.
 * 
 * @author spb
 * @param <A> 
 * @param <AF> 
 * @param <UR> 
 *
 */
public class SequenceTupleProducer<
A extends DataObject & ExpressionTarget, 
AF extends DataObjectFactory<A> & ExpressionTargetFactory<A>,
UR extends SequenceTupleProducer.PeriodTuple<A>
> extends TupleUsageProducer<A,AF,UR> {

	public SequenceTupleProducer(AppContext c, String config_tag) {
		super(c, config_tag);
		finder.addFinder(StandardProperties.time);
		PropExpressionMap expr = new PropExpressionMap();
		customiseAccessors(finder,map, expr);
		LinkedList<PropExpression> starts = new LinkedList<>();
		LinkedList<PropExpression> ends = new LinkedList<>();
		for(ReferenceTag<A, AF> tag : getMemberTags()){
			starts.add(new DeRefExpression<>(tag, StandardProperties.STARTED_PROP));
			ends.add(new DeRefExpression<>(tag, StandardProperties.ENDED_PROP));
		}
		try {
			expr.put(StandardProperties.STARTED_PROP, ArrayFuncPropExpression.makeArrayFunc(ArrayFunc.GREATEST, starts));
			expr.put(StandardProperties.ENDED_PROP, ArrayFuncPropExpression.makeArrayFunc(ArrayFunc.LEAST, ends));
			map.addDerived(c, expr);
		} catch (PropertyCastException e) {
			getLogger().error("Error setting time expressions", e);
		}
		
	}

	protected void customiseAccessors(MultiFinder finder,TupleAccessorMap map,PropExpressionMap expr){
		
	}
	/** collections of properties that must match in all members of the tuple. 
	 * 
	 * @return
	 */
	protected Collection<PropExpression> getCommonProperties(){
		return new LinkedList<PropExpression>();
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.db.PropertyTupleFactory#addMandatoryFilter(uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter)
	 */
	@Override
	protected BaseFilter<UR> addMandatoryFilter(BaseFilter<UR> fil)  {
		TupleAndFilter m = new TupleAndFilter();
		if( fil != null ){
			m.addFilter(fil);
		}
		// Now overlaps between all pairwise members
		Collection<ReferenceTag<A,AF>> member_tags = getMemberTags();
		for( ReferenceTag<A, AF> tag_a : member_tags){
			for(ReferenceTag<A, AF> tag_b : member_tags){
				if( tag_a != tag_b ){
					
					try {
						m.addFilter(getRawFilter(
						  new RelationClause<Date>(
							new DeRefExpression<>(tag_a, StandardProperties.STARTED_PROP),
							MatchCondition.LT, 
							new DeRefExpression<>(tag_b, StandardProperties.ENDED_PROP))));
						for(PropExpression common : getCommonProperties()){
							m.addFilter(getRawFilter(
								new RelationClause<>(
										new DeRefExpression<>(tag_a, common), 
										new DeRefExpression<>(tag_b,common)
									)		
							));
						}
					} catch (CannotFilterException e) {
						getLogger().error("Cannot generate overlap filter",e);
					}
				}
			}
		}
		return m;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.db.PropertyTupleFactory#getPeriodFilter(uk.ac.ed.epcc.webapp.time.Period, uk.ac.ed.epcc.safe.accounting.properties.PropExpression, uk.ac.ed.epcc.safe.accounting.properties.PropExpression, uk.ac.ed.epcc.safe.accounting.selector.OverlapType, long)
	 */
	@Override
	public BaseFilter getPeriodFilter(Period period, PropExpression start_prop, PropExpression end_prop,
			OverlapType type, long cutoff) throws CannotFilterException {
		// TODO Auto-generated method stub
		return super.getPeriodFilter(period, start_prop, end_prop, type, cutoff);
	}

	public static  class PeriodTuple<A extends DataObject & ExpressionTarget> extends TupleUsageProducer.TupleUsageRecord<A> implements TimePeriod{

		public PeriodTuple(AppContext conn, TupleAccessorMap map) {
			super(conn, map);
		}

		@Override
		public Date getStart() {
			
			return getProperty(StandardProperties.STARTED_PROP,null);
		}

		@Override
		public Date getEnd() {
			return getProperty(StandardProperties.ENDED_PROP,null);
		}
		
	}
}
