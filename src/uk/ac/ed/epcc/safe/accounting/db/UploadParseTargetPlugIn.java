package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.policy.DerivedPropertyPolicy;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.ConfigPlugInOwner;
import uk.ac.ed.epcc.safe.accounting.update.NullParser;
import uk.ac.ed.epcc.safe.accounting.update.PlugInOwner;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerPolicy;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
/**
 * 
 * @author spb
 *
 * @param <T>
 * @param <R>
 */
public abstract class UploadParseTargetPlugIn<T extends DataObject,R> extends PropertyContainerParseTargetComposite<T, R>
		implements UploadParseTarget<R> {
	
	public UploadParseTargetPlugIn(DataObjectFactory<T> fac) {
		super(fac);
	}

	@Override
	public boolean parse(DerivedPropertyMap map, R current_line) throws AccountingParseException {
		// Note each stage of the parse sees the derived properties 
		// as defined in the previous stage. Once its own parse is complete
		// It can then override the definition if it wants to
		PropExpressionMap derived = new PropExpressionMap();
		PropertyContainerParser<R> parser = getParser();
		if( parser.parse(map, current_line) ){
			derived=parser.getDerivedProperties(derived);
			map.addDerived(derived);
			for(PropertyContainerPolicy pol : getPlugInOwner().getPolicies()){
				pol.parse(map);
				derived = pol.getDerivedProperties(derived);
				map.addDerived(derived);
			}
			return true;
		}
		return false;
	}

	

	@Override
	public PropertyMap getGlobals(Map<String, Object> params) {
		
		return null;
	}

	@Override
	protected PlugInOwner<R> makePlugInOwner(AppContext c, PropertyFinder finder, String table) {
		return new ConfigPlugInOwner<DataObjectFactory<T>,R>(c,finder ,table,NullParser.class){

			@Override
			protected Set<PropertyContainerPolicy> makePolicies() {
				Set<PropertyContainerPolicy> pol = super.makePolicies();
				// Always have a derivedPropertyPolicy
				pol.add(new DerivedPropertyPolicy());
				return pol;
			}
			
		};
	}

}
