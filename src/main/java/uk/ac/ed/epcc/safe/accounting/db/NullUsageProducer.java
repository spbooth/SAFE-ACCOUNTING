package uk.ac.ed.epcc.safe.accounting.db;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ReductionMapResult;
import uk.ac.ed.epcc.safe.accounting.ReductionTarget;
import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTuple;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.SetPropertyFinder;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AbstractContexed;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.model.data.CloseableIterator;
import uk.ac.ed.epcc.webapp.model.data.NullIterator;

public class NullUsageProducer<UR> extends AbstractContexed implements UsageProducer<UR> {

	public NullUsageProducer(AppContext conn) {
		super(conn);
	}

	@Override
	public <I> boolean compatible(PropExpression<I> expr) {
		return false;
	}

	@Override
	public ExpressionTargetContainer getExpressionTarget(UR record) {
		return null;
	}

	@Override
	public boolean isMyTarget(UR record) {
		return false;
	}

	@Override
	public CloseableIterator<ExpressionTargetContainer> getExpressionIterator(RecordSelector sel) throws Exception {
		return new NullIterator<>();
	}

	@Override
	public <P> boolean hasProperty(PropertyTag<P> tag) {
		return false;
	}

	@Override
	public boolean compatible(RecordSelector sel) {
		return false;
	}

	@Override
	public CloseableIterator<UR> getIterator(RecordSelector sel, int skip, int count) throws Exception {
		return new NullIterator<>();
	}

	@Override
	public CloseableIterator<UR> getIterator(RecordSelector sel) throws Exception {
		return new NullIterator<>();
	}

	@Override
	public long getRecordCount(RecordSelector selector) throws Exception {
		return 0;
	}

	@Override
	public boolean exists(RecordSelector selector) throws Exception {
		return false;
	}

	@Override
	public <PT> Set<PT> getValues(PropertyTag<PT> data_tag, RecordSelector selector) throws Exception {
		return new HashSet<>();
	}

	@Override
	public PropertyFinder getFinder() {
		return new SetPropertyFinder();
	}

	@Override
	public String getTag() {
		return "NullPRoducer";
	}

	

	@Override
	public <T, D> T getReduction(ReductionTarget<T, D> target, RecordSelector sel) throws Exception {
		return target.getDefault();
	}

	@Override
	public Map<ExpressionTuple, ReductionMapResult> getIndexedReductionMap(Set<ReductionTarget> property,
			RecordSelector selector) throws Exception {
		return new HashMap<>();
	}

	@Override
	public <I, T, D> Map<I, T> getReductionMap(PropExpression<I> index, ReductionTarget<T, D> property,
			RecordSelector selector) throws Exception {
		return new HashMap<>();
	}

	@Override
	public PropExpressionMap getDerivedProperties() {
		return new PropExpressionMap();
	}

	@Override
	public boolean setCompositeHint(boolean composite) {
		return false;
	}

}
