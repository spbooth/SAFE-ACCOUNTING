package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

import uk.ac.ed.epcc.safe.accounting.ExpressionFilterTarget;
import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.expr.*;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceTag;
import uk.ac.ed.epcc.safe.accounting.selector.*;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.jdbc.expr.*;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
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
public class SequenceTupleProducer<A extends DataObject & ExpressionTarget, AF extends DataObjectFactory<A> & ExpressionTargetFactory<A>,UR extends SequenceTupleProducer.PeriodTuple<A>> 
extends TupleUsageProducer<A,AF,UR> {

	public SequenceTupleProducer(AppContext c, String config_tag) {
		super(c, config_tag);
		
		
	}

	@Override
	protected void customAccessors(AccessorMap<UR> mapi2, MultiFinder finder, PropExpressionMap derived) {
		finder.addFinder(StandardProperties.time);
		finder.addFinder(StandardProperties.base);
	
		LinkedList<PropExpression> starts = new LinkedList<>();
		LinkedList<PropExpression> ends = new LinkedList<>();
		LinkedList<PropExpression> start_stamps = new LinkedList<>();
		LinkedList<PropExpression> end_stamps = new LinkedList<>();
		for(ReferenceTag<A, AF> tag : getMemberTags()){
			starts.add(new DeRefExpression<>(tag, StandardProperties.STARTED_PROP));
			ends.add(new DeRefExpression<>(tag, StandardProperties.ENDED_PROP));
			try {
				start_stamps.add(new DeRefExpression<>(tag,new MilliSecondDatePropExpression(StandardProperties.STARTED_PROP)));
				end_stamps.add(new DeRefExpression<>(tag,new MilliSecondDatePropExpression(StandardProperties.ENDED_PROP)));
			}catch(PropertyCastException e) {
				getLogger().error("Error making timestamps",e);
			}
		}
		try {
			derived.put(StandardProperties.STARTED_PROP, ArrayFuncPropExpression.makeArrayFunc(ArrayFunc.GREATEST, starts));
			derived.put(StandardProperties.ENDED_PROP, ArrayFuncPropExpression.makeArrayFunc(ArrayFunc.LEAST, ends));
			derived.put(StandardProperties.RUNTIME_PROP, new BinaryPropExpression(
					ArrayFuncPropExpression.makeArrayFunc(ArrayFunc.LEAST, end_stamps),
					Operator.SUB,
					ArrayFuncPropExpression.makeArrayFunc(ArrayFunc.GREATEST, start_stamps)));
		} catch (PropertyCastException e) {
			getLogger().error("Error setting time expressions", e);
		}
	}

	
	/** collections of properties that must match in <em>all</em> members of the tuple. 
	 * 
	 * @return
	 */
	protected Collection<PropExpression> getCommonProperties(){
		return new LinkedList<>();
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
						m.addFilter(getAccessorMap().getRawFilter(
						  new RelationClause<>(
							new DeRefExpression<>(tag_a, StandardProperties.STARTED_PROP),
							MatchCondition.LT, 
							new DeRefExpression<>(tag_b, StandardProperties.ENDED_PROP))));
						for(PropExpression common : getCommonProperties()){
							m.addFilter(getAccessorMap().getRawFilter(
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

	public static  class PeriodTuple<A extends DataObject & ExpressionTarget> extends TupleUsageProducer.TupleUsageRecord<A> implements TimePeriod{

		public PeriodTuple(SequenceTupleProducer prod) {
			super(prod);
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

	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.db.TupleUsageProducer#makeTuple()
	 */
	@Override
	public UR makeTuple() {
		return (UR) new PeriodTuple<A>(this);
	}

	public class SequenceMutator extends CopySelectorVisitor{

		@Override
		public RecordSelector visitPeriodOverlapRecordSelector(PeriodOverlapRecordSelector o) throws Exception {
			PropExpression<Date> start = o.getStart();
			PropExpression<Date> end = o.getEnd();
			if( (start == null || start == StandardProperties.STARTED_PROP) && end == StandardProperties.ENDED_PROP) {
				//  This is a standard date range
				// narrow the selection filter with the same range on each member
				AndRecordSelector sel = new AndRecordSelector();
				sel.add(o);
				TimePeriod period = o.getPeriod();
				
				
				for(ReferenceTag t : getMemberTags()) {
					AF fac = (AF) t.getFactory(getContext());
				
					sel.add(new PeriodOverlapRecordSelector(period, 
							new DeRefExpression<>(t, StandardProperties.STARTED_PROP),
							new DeRefExpression<>(t, StandardProperties.ENDED_PROP),
							OverlapType.ANY, fac.getAccessorMap().calculateCutoff(StandardProperties.STARTED_PROP, StandardProperties.ENDED_PROP)));
				}
				return sel;
			}
			return super.visitPeriodOverlapRecordSelector(o);
		}
		
	}
	@Override
	protected RecordSelector mutateSelector(RecordSelector selector) {
		try {
			return selector.visit(new SequenceMutator());
		} catch (Exception e) {
			getLogger().error("Error mutating selector", e);
			return selector;
		}
	}

	

}
