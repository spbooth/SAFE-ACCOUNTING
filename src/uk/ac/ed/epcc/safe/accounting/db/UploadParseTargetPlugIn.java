package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParserPolicy;
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
import uk.ac.ed.epcc.webapp.model.data.Composite;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
/** A {@link Composite} to implement {@link UploadParseTarget}
 * 
 * Global properties are parsed by setting a list of properties in
 * <b>global_properties_parameters.<i>table-name</i></b>. The post parameters are
 * assumed to follow the property names and are parsed using the default {@link ValueParser}
 * for that property
 * 
 * 
 * @author spb
 *
 * @param <T>
 * @param <R>
 */
public abstract class UploadParseTargetPlugIn<T extends DataObject,R> extends MatcherPropertyContainerParseTargetComposite<T, R>
		implements UploadParseTarget<R> {
	
	private static final String GLOBAL_PROPERTIES_PARAMETERS_PREFIX = "global_properties_parameters.";

	private final Class<? extends PropertyContainerParser> default_parser;
	public UploadParseTargetPlugIn(DataObjectFactory<T> fac) {
		super(fac);
		this.default_parser=NullParser.class; // default is no time fields
	}
	// Constructor to allow explicit composites to specify a different default parser
	public UploadParseTargetPlugIn(DataObjectFactory<T> fac,Class<? extends PropertyContainerParser> parser) {
		super(fac);
		this.default_parser=parser;
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
		String global_parse_properties = getContext().getInitParameter(GLOBAL_PROPERTIES_PARAMETERS_PREFIX+getFactory().getConfigTag());
		ValueParserPolicy policy = new ValueParserPolicy(getContext());
		if( global_parse_properties != null) {
			try {
				PropertyMap map = new PropertyMap();
				Set<PropertyTag> props = parsePropertyList(global_parse_properties);
				for( PropertyTag t : props) {
					Object o = params.get(t.getName());
					if( o  != null ) {
						if( t.allow(o)) {
							map.setProperty(t, o);
						}else if( o instanceof String) {
							ValueParser parser = (ValueParser) t.accept(policy);
							map.setProperty(t, parser.parse((String)o));
						}
					}

				}
				return map;
			}catch(Exception e) {
				getLogger().error("Error parsing globals", e);
			}
		}
		return null;
	}

	@Override
	protected PlugInOwner<R> makePlugInOwner(AppContext c, PropertyFinder finder, String table) {
		return new ConfigPlugInOwner<DataObjectFactory<T>,R>(c,finder ,table,default_parser){

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
