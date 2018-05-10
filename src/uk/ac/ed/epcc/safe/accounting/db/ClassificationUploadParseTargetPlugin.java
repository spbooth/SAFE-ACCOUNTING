package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropertyCastException;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerPolicy;
import uk.ac.ed.epcc.webapp.AppContext;
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
public class ClassificationUploadParseTargetPlugin<T extends Classification,R> extends UploadParseTargetPlugIn<T, R>
		implements ClassificationCreateContributor<T> {
	

	private static final String MATCH_SUFFIX = ".match";
	private PropertyTag<String> match_prop=null;
	public ClassificationUploadParseTargetPlugin(ClassificationFactory<T> fac) {
		super(fac);
		
	}

	
	@Override
	public void customAccessors(AccessorMap<T> mapi2, MultiFinder finder, PropExpressionMap derived) {
		super.customAccessors(mapi2, finder, derived);
		ClassificationFactory<T> fac = (ClassificationFactory<T>) getFactory();
		AppContext c = getContext();
		match_prop = (PropertyTag<String>) finder.find(String.class,c.getInitParameter(fac.getConfigTag()+MATCH_SUFFIX,Classification.NAME));
		if( match_prop == null ){
			getLogger().error("No match property defined "+fac.getTag());
		}
		
		if( match_prop != null && ! match_prop.equals(AccountingClassificationFactory.NAME_PROP)){
			try {
				derived.put(AccountingClassificationFactory.NAME_PROP, match_prop);
			} catch (PropertyCastException e) {
				getLogger().error("Error adding derived mapping for name",e);
			}
		}
	}



	@Override
	public ExpressionTargetContainer make(PropertyContainer value) throws AccountingParseException {
		ExpressionTargetFactory<T> etf = getExpressionTargetFactory();
		ClassificationFactory<T> fac = (ClassificationFactory<T>) getFactory();
		if( etf == null ) {
			throw new AccountingParseException("ClassificationUploadParseTargetPlugin installed without ExpressionTargetFactory");
		}
		if( match_prop == null ){
			throw new AccountingParseException("No match property specified for "+fac.getTag());
		}
		String name = value.getProperty(match_prop, null);
	
		if( name == null ){
			throw new AccountingParseException("No name parsed");
		}


		try {
			// we don't use makeByName here as it also applies the policies.
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
		if(match_prop != null && pol != null && pol.size() > 0){
			try{
				PropertyMap map = new PropertyMap();
				map.setProperty(match_prop, name);
				for(PropertyContainerPolicy p : pol){
					p.startParse(null);
				}
				for(PropertyContainerPolicy p : pol){
					p.parse(map);
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
	@Override
	public void addConfigParameters(Set<String> params) {
		super.addConfigParameters(params);
		params.add(getFactory().getTag()+MATCH_SUFFIX);
	}
}
