package uk.ac.ed.epcc.safe.accounting.db;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyTargetFactory;
import uk.ac.ed.epcc.webapp.model.data.Composite;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
/** A {@link Composite} that is added to a standard {@link DataObjectFactory}
 * to provide accounting support.
 * 
 * Only one such composite can be added which is always registered under {@link AccountingComposite}.
 * However various sub-classes are available to provide different levels of support
 * 
 * 
 * @author spb
 *
 * @param <BDO>
 */
public abstract class AccountingComposite<BDO extends DataObject> extends Composite<BDO, AccountingComposite> implements PropertyTargetFactory {

	protected AccountingComposite(DataObjectFactory<BDO> fac) {
		super(fac);
	}

	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.webapp.model.data.Composite#getType()
	 */
	@Override
	protected final Class<? super AccountingComposite> getType() {
		return AccountingComposite.class;
	}

	

}
