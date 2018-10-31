package uk.ac.ed.epcc.safe.accounting;

import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.db.ReductionHandler;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTuple;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.model.data.CloseableIterator;
/** wrap an {@link ExpressionTargetFactory} to make a {@link UsageProducer}
 * 
 * @author spb
 *
 * @param <UR>
 */
public class UsageProducerWrapper<UR> implements UsageProducer<UR> {

	public UsageProducerWrapper(AppContext conn, String tag,ExpressionTargetFactory<UR> etf) {
		super();
		this.conn=conn;
		this.tag=tag;
		this.etf = etf;
	}
    private final AppContext conn;
    private final String tag;
	private final ExpressionTargetFactory<UR> etf;
	
	@Override
	public <I> boolean compatible(PropExpression<I> expr) {
		return etf.compatible(expr);
	}

	@Override
	public ExpressionTargetContainer getExpressionTarget(UR record) {
		return etf.getExpressionTarget(record);
	}

	@Override
	public boolean isMyTarget(UR record) {
		return etf.isMyTarget(record);
	}

	@Override
	public boolean compatible(RecordSelector sel) {
		return etf.compatible(sel);
	}

	@Override
	public CloseableIterator<UR> getIterator(RecordSelector sel, int skip, int count) throws Exception {
		return etf.getIterator(sel, skip, count);
	}

	@Override
	public CloseableIterator<UR> getIterator(RecordSelector sel) throws Exception {
		return etf.getIterator(sel);
	}

	@Override
	public long getRecordCount(RecordSelector selector) throws Exception {
		return etf.getRecordCount(selector);
	}

	@Override
	public <PT> Set<PT> getValues(PropertyTag<PT> data_tag, RecordSelector selector) throws Exception {
		return etf.getValues(data_tag, selector);
	}

	@Override
	public PropertyFinder getFinder() {
		return etf.getFinder();
	}

	@Override
	public <P> boolean hasProperty(PropertyTag<P> tag) {
		return etf.hasProperty(tag);
	}


	

	private ReductionHandler<UR, ExpressionTargetFactory<UR>> getReductionHandler(){
		return new ReductionHandler<>(etf);
	}


	@Override
	public <T> T getReduction(ReductionTarget<T> target, RecordSelector sel) throws Exception {
		return getReductionHandler().getReduction(target, sel);
	}

	@Override
	public Map<ExpressionTuple, ReductionMapResult> getIndexedReductionMap(Set<ReductionTarget> property,
			RecordSelector selector) throws Exception {
		return getReductionHandler().getIndexedReductionMap(property, selector);
	}

	@Override
	public <I> Map<I, Number> getReductionMap(PropExpression<I> index, ReductionTarget<Number> property,
			RecordSelector selector) throws Exception {
		return getReductionHandler().getReductionMap(index, property, selector);
	}

	@Override
	public String getTag() {
		return tag;
	}

	@Override
	public AppContext getContext() {
		return conn;
	}

	@Override
	public PropExpressionMap getDerivedProperties() {
		return etf.getDerivedProperties();
	}

	@Override
	public CloseableIterator<ExpressionTargetContainer> getExpressionIterator(RecordSelector sel) throws Exception {
		return etf.getExpressionIterator(sel);
	}

}
