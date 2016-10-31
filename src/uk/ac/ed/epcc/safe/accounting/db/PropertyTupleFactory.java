package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Iterator;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ExpressionFilterTarget;
import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.ExpressionTargetGenerator;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
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

public class PropertyTupleFactory<
A extends DataObject & ExpressionTarget, 
AF extends DataObjectFactory<A>&ExpressionTargetFactory<A>,
T extends PropertyTupleFactory<A,AF,?>.PropertyTuple
>extends TupleFactory<A,AF,T> implements ExpressionTargetGenerator<T>, ExpressionFilterTarget{

	

	public PropertyTupleFactory(AppContext c,AF ...a_fac) {
		super(c,a_fac);
	}

	public class PropertyTuple extends TupleFactory.Tuple<A> implements ExpressionTarget{

		

		@Override
		public <T> T getProperty(PropertyTag<T> tag, T def) {
			
			//TODO
			return null;
		}

		@Override
		public Parser getParser() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <T> T evaluateExpression(PropExpression<T> expr) throws InvalidExpressionException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <T> T evaluateExpression(PropExpression<T> expr, T def) {
			// TODO Auto-generated method stub
			return null;
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
		return false;
	}

	@Override
	public <I> boolean compatible(PropExpression<I> expr) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Class getTarget() {
		return Object.class;
	}

	@Override
	public BaseFilter getFilter(PropExpression expr, MatchCondition match, Object value) throws CannotFilterException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseFilter getNullFilter(PropExpression expr, boolean is_null) throws CannotFilterException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseFilter getRelationFilter(PropExpression left, MatchCondition match, PropExpression right)
			throws CannotFilterException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseFilter getPeriodFilter(Period period, PropExpression start_prop, PropExpression end_prop,
			OverlapType type, long cutoff) throws CannotFilterException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseSQLFilter getOrderFilter(boolean descending, PropExpression expr) throws CannotFilterException {
		// TODO Auto-generated method stub
		return null;
	}
}
