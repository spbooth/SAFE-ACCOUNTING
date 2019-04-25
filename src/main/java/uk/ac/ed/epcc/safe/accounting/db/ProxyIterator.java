package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Iterator;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.webapp.model.data.CloseableIterator;
import uk.ac.ed.epcc.webapp.model.data.iterator.DecoratingIterator;
/** A  {@link DecoratingIterator} that generates the {@link ExpressionTargetContainer} proxy
 * for the object from the underlying iterator.
 * 
 * @author spb
 *
 * @param <S>
 */
public class ProxyIterator<S> extends DecoratingIterator<ExpressionTargetContainer,S> {
    private final ExpressionTargetFactory<S> etf;
	public ProxyIterator(ExpressionTargetFactory<S> etf,CloseableIterator<S> i) {
		super(i);
		this.etf=etf;
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.webapp.model.data.iterator.DecoratingIterator#next()
	 */
	@Override
	public ExpressionTargetContainer next() {
		return etf.getExpressionTarget(nextInput());
	}
	
}
