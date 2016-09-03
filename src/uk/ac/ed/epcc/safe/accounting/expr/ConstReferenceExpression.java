package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.properties.BasePropExpressionVisitor;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceExpression;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Indexed;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedProducer;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;

/** A {@link ConstPropExpression} that is also a {@link ReferenceExpression}.
 * 
 * @author spb
 *
 * @param <I>
 */
public class ConstReferenceExpression<I extends Indexed> extends ConstPropExpression<IndexedReference<I>> implements ReferenceExpression<I>{

	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.reference.ReferenceExpression#getFactory(uk.ac.ed.epcc.webapp.AppContext)
	 */
	@Override
	public IndexedProducer<I> getFactory(AppContext c) {
		return IndexedReference.makeIndexedProducer(c, getFactoryClass(), val.getTag());
	}

	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.reference.ReferenceExpression#getFactoryClass()
	 */
	@Override
	public Class<? extends IndexedProducer> getFactoryClass() {
		return val.getFactoryClass();
	}

	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.reference.ReferenceExpression#getTable()
	 */
	@Override
	public String getTable() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.expr.ConstPropExpression#copy()
	 */
	@Override
	public ConstReferenceExpression<I> copy() {
		return this;
	}

	public ConstReferenceExpression( IndexedReference<I> n) {
		super(IndexedReference.class, n);
	}

	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.expr.ConstPropExpression#accept(uk.ac.ed.epcc.safe.accounting.properties.BasePropExpressionVisitor)
	 */
	@Override
	public <R> R accept(BasePropExpressionVisitor<R> vis) throws Exception {
		if( vis instanceof PropExpressionVisitor){
			return ((PropExpressionVisitor<R>)vis).visitConstReferenceExpression(this);
		}
		throw new UnsupportedExpressionException(this);
	}

}
