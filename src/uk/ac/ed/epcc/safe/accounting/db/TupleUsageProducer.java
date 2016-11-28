package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ReductionMapResult;
import uk.ac.ed.epcc.safe.accounting.ReductionTarget;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.UsageRecord;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTuple;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;
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
> extends PropertyTupleFactory<A,AF,UR> implements UsageProducer<UR> {

	public TupleUsageProducer(AppContext c, String config_tag) {
		super(c, config_tag);
	}

	
	public static  class TupleUsageRecord<A extends DataObject> extends PropertyTupleFactory.PropertyTuple<A> implements UsageRecord{

		public TupleUsageRecord(TupleAccessorMap map) {
			super(map);
		}

		@Override
		public boolean supports(PropertyTag<?> tag) {
			return proxy.supports(tag);
		}

		@Override
		public boolean writable(PropertyTag<?> tag) {
			return false;
		}

		@Override
		public <T> T getProperty(PropertyTag<T> key) throws InvalidExpressionException {
			return proxy.getProperty(key);
		}

		@Override
		public <T> void setProperty(PropertyTag<? super T> key, T value) throws InvalidPropertyException {
			proxy.setProperty(key, value);
		}

		@Override
		public <T> void setOptionalProperty(PropertyTag<? super T> key, T value) {
			proxy.setOptionalProperty(key, value);
		}

		@Override
		public Set<PropertyTag> getDefinedProperties() {
			return proxy.getDefinedProperties();
		}

		@Override
		public void setAll(PropertyContainer source) {
			proxy.setAll(source);
		}

		@Override
		public Object getKey() {
			return getIdentifier();
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
		
	}

	private GeneratorReductionHandler<UR, TupleUsageProducer<A, AF, UR>> getReductionHandler(){
		return new GeneratorReductionHandler<UR, TupleUsageProducer<A, AF, UR>>(this);
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
