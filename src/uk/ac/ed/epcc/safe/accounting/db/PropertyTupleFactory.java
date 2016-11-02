package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Iterator;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ExpressionFilterTarget;
import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.ExpressionTargetGenerator;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.expr.Parser;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.selector.OverlapType;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseSQLFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.jdbc.filter.SQLFilter;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.TupleFactory;
import uk.ac.ed.epcc.webapp.time.Period;
/** A property enabled {@link TupleFactory}
 * The component parts are accessible as references
 * 
 * @author spb
 *
 * @param <A>
 * @param <AF>
 * @param <T>
 */
public class PropertyTupleFactory<
A extends DataObject & ExpressionTarget, 
AF extends DataObjectFactory<A>&ExpressionTargetFactory<A>,
T extends PropertyTupleFactory<A,AF,?>.PropertyTuple
>extends TupleFactory<A,AF,T> implements ExpressionTargetGenerator<T>, ExpressionFilterTarget{

	private final TupleAccessorMap map;

	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.webapp.model.data.TupleFactory#makeTuple()
	 */
	@Override
	public T makeTuple() {
		
		return (T) new PropertyTuple();
	}
	public PropertyTupleFactory(AppContext c,AF ...a_fac) {
		super(c,a_fac);
		map = new TupleAccessorMap(this, "XXXX");
	}

	public class PropertyTuple extends TupleFactory.Tuple<A> implements ExpressionTarget{

		 public PropertyTuple() {
			super();
			this.proxy = map.getProxy(this);
		}

		protected final ExpressionTargetContainer proxy;

		@Override
		public <R> R getProperty(PropertyTag<R> tag, R def) {
			return proxy.evaluateExpression(tag, def);
		}

		@Override
		public Parser getParser() {
			return proxy.getParser();
		}

		@Override
		public <T> T evaluateExpression(PropExpression<T> expr) throws InvalidExpressionException {
			return proxy.evaluateExpression(expr);
		}

		@Override
		public <T> T evaluateExpression(PropExpression<T> expr, T def) {
			return proxy.evaluateExpression(expr, def);
		}
		
	}

	@Override
	public boolean compatible(RecordSelector sel) {
		CompatibleSelectVisitor vis = new CompatibleSelectVisitor(null,this,false);
		try {
			return sel.visit(vis);
		} catch (Exception e) {
			return false;
		}
	}
	protected final SQLFilter getFilter(RecordSelector selector) throws CannotFilterException {
		//TODO implement me
		return null;
	}
	@Override
	public Iterator<T> getIterator(RecordSelector sel, int skip, int count) throws Exception {
		
		return makeResult(getFilter(sel),skip,count).iterator();
	}

	@Override
	public Iterator<T> getIterator(RecordSelector sel) throws Exception {
		return makeResult(getFilter(sel)).iterator();
	}

	@Override
	public long getRecordCount(RecordSelector selector) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <PT> Set<PT> getValues(PropertyTag<PT> data_tag, RecordSelector selector) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PropertyFinder getFinder() {
		MultiFinder finder = new MultiFinder();
		
		return finder;
	}

	@Override
	public <P> boolean hasProperty(PropertyTag<P> tag) {
		return map.hasProperty(tag);
	}

	@Override
	public <I> boolean compatible(PropExpression<I> expr) {

		return map.resolves(expr, false);
	}

	@Override
	public Class getTarget() {
		return PropertyTuple.class;
	}

	@Override
	public BaseFilter getFilter(PropExpression expr, MatchCondition match, Object value) throws CannotFilterException {
		return map.getFilter(expr, match, value);
	}

	@Override
	public BaseFilter getNullFilter(PropExpression expr, boolean is_null) throws CannotFilterException {
		return map.getNullFilter(expr, is_null);
	}

	@Override
	public BaseFilter getRelationFilter(PropExpression left, MatchCondition match, PropExpression right)
			throws CannotFilterException {
		return map.getRelationFilter(left, match, right);
	}

	@Override
	public BaseFilter getPeriodFilter(Period period, PropExpression start_prop, PropExpression end_prop,
			OverlapType type, long cutoff) throws CannotFilterException {
		return map.getPeriodFilter(period, start_prop, end_prop, type, cutoff);
	}

	@Override
	public BaseSQLFilter getOrderFilter(boolean descending, PropExpression expr) throws CannotFilterException {
		return map.getOrderFilter(descending, expr);
	}
}
