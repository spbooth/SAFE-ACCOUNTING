package uk.ac.ed.epcc.safe.accounting.db;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionCast;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerParser;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
/** Am {@link UploadParseTargetPlugIn} that matches using the unique properties.
 * 
 * @author Stephen Booth
 *
 * @param <T>
 * @param <R>
 */
public class ConfigUploadParseTargetPlugin<T extends DataObject,R> extends UploadParseTargetPlugIn<T, R> {

	/**
	 * @param fac
	 * @param parser
	 */
	public ConfigUploadParseTargetPlugin(DataObjectFactory<T> fac, Class<? extends PropertyContainerParser> parser) {
		super(fac, parser);
	}

	public ConfigUploadParseTargetPlugin(DataObjectFactory<T> fac) {
		super(fac);
	}

	@Override
	public ExpressionTargetContainer make(PropertyContainer value) throws AccountingParseException {
		try {
			
			
			ExpressionTargetContainer result = findDuplicate(value);
			if( result != null ) {
				return result;
			}
			if( makeOnUpload()) {
				DataObjectFactory<T> fac = (DataObjectFactory<T>) getFactory();
				ExpressionTargetFactory<T> etf = ExpressionCast.getExpressionTargetFactory(fac);
				T bdo = fac.makeBDO();
				result = etf.getExpressionTarget(bdo);
				for(PropertyTag t : getUniqueProperties()) {
					result.setProperty(t, value.getProperty(t));
				}
			}

			return result;
		}catch(Exception e) {
			throw new AccountingParseException(e);
		}
	}
	protected boolean makeOnUpload() {
		return getContext().getBooleanParameter("make_on_upload."+getFactory().getConfigTag(), true);
	}
}
