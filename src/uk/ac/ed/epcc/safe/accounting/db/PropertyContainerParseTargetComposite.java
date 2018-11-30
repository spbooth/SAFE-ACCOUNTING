package uk.ac.ed.epcc.safe.accounting.db;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.update.PlugInOwner;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerPolicy;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
import uk.ac.ed.epcc.webapp.forms.transition.Transition;
import uk.ac.ed.epcc.webapp.jdbc.table.TableContentProvider;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionContributor;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionKey;
import uk.ac.ed.epcc.webapp.model.data.Composite;
import uk.ac.ed.epcc.webapp.model.data.ConfigParamProvider;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
/** A {@link Composite} implementation of {@link PropertyContainerParseTarget}
 * 
 * A table that supports parsing should have a sub-class composite installed.
 * This class acts as a bridge between the generic {@link Composite}s and the
 * parse specific {@link PlugInOwner} and has to forward any interfaces we might want to support
 * on the {@link PlugInOwner} but query as {@link Composite}s
 * <p>
 * Subclasses can implement different sub-interfaces of {@link PropertyContainerParseTarget}
 * and provide different ways of building the {@link PlugInOwner}.
 * @author spb
 *
 */
public abstract class PropertyContainerParseTargetComposite<T extends DataObject,R> extends Composite<T, PropertyContainerParseTargetComposite>
		implements PropertyContainerParseTarget<R>, AccessorContributer<T>, ConfigParamProvider, TableContentProvider,TableTransitionContributor {

	
	@Override
	public TableSpecification modifyDefaultTableSpecification(TableSpecification spec, String table) {
		// The parser can specify a default table to create.
		// We don't have the propertyFinder yet so create a scratch
		// plugin owner just to initialise the table specification
		PlugInOwner<R> owner = makePlugInOwner(getContext(),null, table);

		PropertyContainerParser parser=owner.getParser();
		if( parser == null ){
			// If we explicitly don't have a parser then we don't want auto-table creation.

			return null;
		}
		PropExpressionMap map = new PropExpressionMap();
		map = parser.getDerivedProperties(map);
		for(PropertyContainerPolicy pol : owner.getPolicies()){
			map = pol.getDerivedProperties(map);
		}
		spec = parser.modifyDefaultTableSpecification(getContext(),spec,map,table);
		// If the parser explicitly returns no spec then no table is created.
		// This will also happen if there are no AutoTable annotatiosn so no fields are added by the parser
		// You can avoid this in sub-classes by creating fields in makeInitialSpecification
		if( spec != null ){
			for(PropertyContainerPolicy pol : owner.getPolicies()){
				spec = pol.modifyDefaultTableSpecification(getContext(),spec,map,table);
			}
		}
		return spec;
	}
	@Override
	public void customAccessors(AccessorMap<T> mapi2, MultiFinder finder,
			PropExpressionMap derived) {
		DataObjectFactory<T> fac = getFactory();
		// Note this must not depend on getPluginOwner directly
		// if the plugin is not initialised we will
		// trigger building the accessor-map by calling getFinder on the 
		// ExpressionTargetFactory. This will eventually recurse to here
		plugin_owner = makePlugInOwner(fac.getContext(),finder,fac.getTag());
		finder.addFinder(plugin_owner.getFinder());
		derived.getAllFrom(plugin_owner.getDerivedProperties());
		
	}

	private PlugInOwner<R> plugin_owner=null;
	protected PropertyContainerParseTargetComposite(DataObjectFactory<T> fac) {
		super(fac);
	}
	public PropertyContainerParser<R> getParser(){
		return getPlugInOwner().getParser();
	}
	public final PlugInOwner<R> getPlugInOwner(){
		if( plugin_owner == null) {
			// Trigger initialisation of properties
			// This should recurse to customAccessors
			// and populate plugin_owner
			getExpressionTargetFactory().getFinder();
			assert( plugin_owner != null);
		}
		return plugin_owner;
	}
	/** Create a {@link PlugInOwner} for this class;
	 * Normally this is called lazily when the {@link PlugInOwner} is
	 * first required. It may also be called to create a {@link PlugInOwner}
	 * when creating the {@link TableSpecification}. In this case a null
	 * {@link PropertyFinder} is passed.
	 * 
	 * 
	 * @param c   {@link AppContext}
	 * @param finder {@link PropertyFinder}
	 * @param table String table name
	 * @return
	 */
	protected abstract PlugInOwner<R> makePlugInOwner(AppContext c,PropertyFinder finder,String table);
	
	protected ExpressionTargetFactory<T> getExpressionTargetFactory(){
		DataObjectFactory<T> fac = getFactory();
		if( fac instanceof ExpressionTargetFactory) {
			return (ExpressionTargetFactory<T>) fac;
		}
		ExpressionTargetFactory<T> comp =fac.getComposite(ExpressionTargetFactoryComposite.class);
		if( comp != null) {
			return comp;
		}
		throw new ConsistencyError("No ExpressionTargetFactory");
	}
 
	public void startParse(PropertyMap defaults) throws Exception {
		getRepository().setAllowNull(true);
		PlugInOwner<R> plugin_owner = getPlugInOwner();
		PropertyContainerParser tmp_parser = plugin_owner.getParser();
		tmp_parser.startParse(defaults);
		for(PropertyContainerPolicy pol : plugin_owner.getPolicies()){
			pol.startParse(defaults);
		}
	}

	public StringBuilder endParse() {
		PlugInOwner<R> plugin_owner = getPlugInOwner();
		StringBuilder tmp = new StringBuilder();
    	for(PropertyContainerPolicy pol : plugin_owner.getPolicies()){
    		//tmp.append(pol.getClass().getCanonicalName());
    		//tmp.append("\n");
    		tmp.append(pol.endParse());
    		//tmp.append("-----------------------\n");
    	}
    	PropertyContainerParser parser = plugin_owner.getParser();
    	//tmp.append(parser.getClass().getCanonicalName());
		//tmp.append("\n");
    	tmp.append(parser.endParse());
    	//tmp.append("-----------------------\n");
    	getRepository().setAllowNull(false);
		return tmp;
	}

	public PropertyFinder getFinder() {
		return getExpressionTargetFactory().getFinder();
	}

	@Override
	protected final Class<? super PropertyContainerParseTargetComposite> getType() {
		return PropertyContainerParseTargetComposite.class;
	}
	@Override
	public void addConfigParameters(Set<String> params) {
		PlugInOwner<R> owner = getPlugInOwner();
		if( owner instanceof ConfigParamProvider) {
			((ConfigParamProvider)owner).addConfigParameters(params);
		}
		
	}
	@Override
	public void addSummaryContent(ContentBuilder cb) {
		PlugInOwner<R> owner = getPlugInOwner();
		if( owner instanceof TableContentProvider) {
			((TableContentProvider)owner).addSummaryContent(cb);
		}
		
	}
	@Override
	public Map<TableTransitionKey, Transition> getTableTransitions() {
		PlugInOwner<R> owner = getPlugInOwner();
		if( owner instanceof TableTransitionContributor) {
			return ((TableTransitionContributor) owner).getTableTransitions();
		}
		return new LinkedHashMap<>();
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.db.PropertyContainerParseTarget#getDerivedProperties()
	 */
	@Override
	public final PropExpressionMap getDerivedProperties() {
		return getPlugInOwner().getDerivedProperties();
	}

}
