package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.PropertyImplementationProvider;
import uk.ac.ed.epcc.safe.accounting.ReductionMapResult;
import uk.ac.ed.epcc.safe.accounting.ReductionTarget;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTuple;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.forms.Identified;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;

/** A {@link UsageProducer} variant of {@link PropertyTupleFactory}
 * 
 * @author spb
 *
 * @param <A>
 * @param <AF>
 * @param <UR>
 */
public class TupleUsageProducer<
A extends DataObject, 
AF extends DataObjectFactory<A>,
UR extends TupleUsageProducer.TupleUsageRecord<A>
> extends PropertyTupleFactory<A,AF,UR> implements UsageProducer<UR>, PropertyImplementationProvider {

	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.db.PropertyTupleFactory#makeTuple()
	 */
	@Override
	public UR makeTuple() {
		return (UR) new TupleUsageRecord<A>(this);
	}

	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.db.PropertyTupleFactory#getTarget()
	 */
	@Override
	public Class<? super UR> getTarget() {
		return TupleUsageRecord.class;
	}

	public TupleUsageProducer(AppContext c, String config_tag) {
		super(c, config_tag);
	}

	
	public static  class TupleUsageRecord<A extends DataObject> extends PropertyTupleFactory.PropertyTuple<A> implements  Identified{

		public TupleUsageRecord(TupleUsageProducer prod) {
			super(prod);
		}



		@Override
		public String getIdentifier() {
			StringBuilder sb = new StringBuilder();
			for(Map.Entry<String, A> entry : entrySet()){
				sb.append(entry.getKey());
				sb.append(":");
				sb.append(Integer.toString(entry.getValue().getID()));
				sb.append("-");
			}
			return sb.toString();
		}

		
		@Override
		public String getIdentifier(int max_length) {
			return getIdentifier();
		}
		
	}

	private ReductionHandler<UR, TupleUsageProducer<A, AF, UR>> getReductionHandler(){
		return new ReductionHandler<UR, TupleUsageProducer<A, AF, UR>>(this){

			/* (non-Javadoc)
			 * @see uk.ac.ed.epcc.safe.accounting.db.ReductionHandler#isQualify()
			 */
			@Override
			public boolean isQualify() {
				return true;
			}
			
		};
	}

	public <I> Map<I, Number> getReductionMap(PropExpression<I> index,
			ReductionTarget<Number> property,  RecordSelector selector)
			throws Exception 
	{
		return getReductionHandler().getReductionMap(index, property, selector);
		
	}

	
	public  Map<ExpressionTuple, ReductionMapResult> getIndexedReductionMap( Set<ReductionTarget> sum, RecordSelector selector) throws Exception{
		return getReductionHandler().getIndexedReductionMap(sum, selector);
	}
	
	
	public  <R>  R getReduction(ReductionTarget<R> type, RecordSelector selector) throws Exception {
		return getReductionHandler().getReduction(type, selector);
	}


	@Override
	public String getImplemenationInfo(PropertyTag<?> tag) {
		return map.getImplemenationInfo(tag);
	}
}
