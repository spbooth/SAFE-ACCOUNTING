package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionCast;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.webapp.model.NameFinder;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
/** An {@link UploadParseTargetPlugIn} for factories that implement {@link NameFinder}
 * 
 * @author spb
 *
 * @param <T>
 */
public class NameFinderUploadParseTargetPlugIn<T extends DataObject,R> extends UploadParseTargetPlugIn<T, R> {

	private static final String MATCH_SUFFIX = ".match";
	public NameFinderUploadParseTargetPlugIn(DataObjectFactory<T> fac,String tag) {
		super(fac,tag);
	}

	protected boolean makeOnUpload() {
		return true;
	}
	public ExpressionTargetContainer make(PropertyContainer value) throws AccountingParseException{
		DataObjectFactory<T> fac = (DataObjectFactory<T>) getFactory();
		ExpressionTargetFactory<T> etf = ExpressionCast.getExpressionTargetFactory(fac);
		PropertyTag<String> match = getMatchProp();
		if( match == null ){
			throw new AccountingParseException("No match property specified for "+fac.getTag());
		}
		if( etf == null ) {
			throw new AccountingParseException("No ExpressionTargetFactory for "+fac.getTag());
		}
		if( ! (fac instanceof NameFinder)) {
			throw new AccountingParseException("Factory not a NameFinder");
		}
		NameFinder<T> nf = (NameFinder<T>)fac;
		String name = value.getProperty(match, null);
		if( name == null ){
			throw new AccountingParseException("No name parsed");
		}
		if( makeOnUpload()){
			try {
				return etf.getExpressionTarget(nf.makeFromString(name));
			} catch (Exception e) {
				throw new AccountingParseException("Cannot make new record",e);
			}
		}
		return etf.getExpressionTarget(nf.findFromString(name));
		
	}

	private String match_prop_name=null;
    private PropertyTag<String> match_prop=null;
    protected PropertyTag<String> getMatchProp(){
		if( match_prop_name == null) {
			// Defer lookup till needed so AccessorMap
			// is fully configured by all contributors
			match_prop_name = getContext().getInitParameter(getFactory().getConfigTag()+MATCH_SUFFIX,getDefaultMatchPropName());
			
			match_prop = (PropertyTag<String>) getExpressionTargetFactory().getFinder().find(String.class,match_prop_name);
		}
		return match_prop;
	}
    protected String getDefaultMatchPropName() {
    	return "Name";
    }
    @Override
	public void addConfigParameters(Set<String> params) {
		super.addConfigParameters(params);
		params.add(getFactory().getTag()+MATCH_SUFFIX);
	}
}
