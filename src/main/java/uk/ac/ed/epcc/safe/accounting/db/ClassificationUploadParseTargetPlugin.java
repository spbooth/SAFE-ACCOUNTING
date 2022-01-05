package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerPolicy;
import uk.ac.ed.epcc.webapp.model.Classification;
import uk.ac.ed.epcc.webapp.model.ClassificationCreateContributor;
import uk.ac.ed.epcc.webapp.model.ClassificationFactory;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
/** A {@link UploadParseTargetPlugIn} specific to {@link ClassificationFactory}s.
 * 
 * @author spb
 *
 * @param <T>
 * @param <R>
 */
public class ClassificationUploadParseTargetPlugin<T extends Classification,R> extends NameFinderUploadParseTargetPlugIn<T, R>
		implements ClassificationCreateContributor<T> {
	
	public ClassificationUploadParseTargetPlugin(ClassificationFactory<T> fac) {
		super(fac);
		
	}

	
	
	@Override
	public ExpressionTargetContainer make(PropertyContainer value) throws AccountingParseException {
		ExpressionTargetFactory<T> etf = getExpressionTargetFactory();
		ClassificationFactory<T> fac = (ClassificationFactory<T>) getFactory();
		if( etf == null ) {
			throw new AccountingParseException("ClassificationUploadParseTargetPlugin installed without ExpressionTargetFactory");
		}
		PropertyTag<String> match = getMatchProp();
		if( match == null ){
			throw new AccountingParseException("No match property specified for "+fac.getTag());
		}
		String name = value.getProperty(match, null);
	
		if( name == null ){
			throw new AccountingParseException("No name parsed property="+match.getFullName());
		}


		try {
			// we don't use makeFromString here as it also applies the policies.
			T record = fac.findFromString(name);
			if( record == null ){
				record = fac.makeBDO();
				record.setName(name);
			}
			return etf.getExpressionTarget(record);
		} catch (DataFault e) {
			throw new AccountingParseException("Error making name: " + e.getMessage());
		}
	}

	@Override
	public void postMakeByName(T c, String name) {
		ExpressionTargetFactory<T> etf = getExpressionTargetFactory();
		if( etf == null ) {
			getLogger().warn("ClassificationUploadParseTargetPlugin installed without ExpressionTargetFactory");
			return;
		}
		ExpressionTargetContainer container = etf.getExpressionTarget(c);
		// apply policies in case they apply actions based on the name.
		Set<PropertyContainerPolicy> pol = getPlugInOwner().getPolicies();
		PropertyTag<String> match = getMatchProp();
		if(match != null && pol != null && pol.size() > 0){
			try{
				DerivedPropertyMap map = new DerivedPropertyMap(getContext());
				map.setProperty(match, name);
				for(PropertyContainerPolicy p : pol){
					p.startParse(null);
				}
				for(PropertyContainerPolicy p : pol){
					p.parse(map);
				}
				for(PropertyContainerPolicy p : pol){
					p.lateParse(map);
				}
				for(PropertyContainerPolicy p : pol){
					p.endParse();
				}
				map.setContainer(container);
			}catch(Exception e){
				getLogger().error("Error applying policies in postMakeByName",e);
			}
		}
	}
	
}
