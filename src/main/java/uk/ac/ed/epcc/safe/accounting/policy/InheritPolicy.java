package uk.ac.ed.epcc.safe.accounting.policy;

import java.util.LinkedHashSet;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.db.transitions.SummaryProvider;
import uk.ac.ed.epcc.safe.accounting.expr.DeRefExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionCast;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropertyCastException;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceTag;
import uk.ac.ed.epcc.safe.accounting.selector.PropertyTargetGenerator;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.content.ExtendedXMLBuilder;
import uk.ac.ed.epcc.webapp.model.data.ConfigParamProvider;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedProducer;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
import uk.ac.ed.epcc.webapp.session.SessionService;
/**A policy to inherit all properties from a parent record. The reference to the parent is assumed
 * to already be set
 * <p>
 * Config Parameters
 * <ul>
 * <li><b>IneritPolicy.<em>table-name</em>.parent</b> Reference Property Name of the parent table 
 * </li>
 * </ul>
 * 
 * @author spb
 *
 */
public class InheritPolicy extends BasePolicy implements SummaryProvider,ConfigParamProvider {

	private static final String PARENT_SUFFIX = ".parent";
	private static final String INHERIT_POLICY_PREFIX = "InheritPolicy.";
	public InheritPolicy(AppContext conn) {
		super(conn);
	}

	Set<ReferenceTag> parents=null;
	PropExpressionMap map=null;
	String table=null;
	@Override
	public PropertyFinder initFinder(PropertyFinder prev, String table) {
		AppContext ctx = getContext();
		MultiFinder finder =null;
		try{
			this.table=table;
			String parent_names = ctx.getInitParameter(INHERIT_POLICY_PREFIX+table+PARENT_SUFFIX);
			for(String parent_name : parent_names.split("\\s*,\\s*")){
				if( parent_name != null && ! parent_name.trim().isEmpty()){
					ReferenceTag parent = (ReferenceTag) prev.find(IndexedReference.class, parent_name);
					if( parent == null ){
						getLogger().error("No parent found for "+table+":"+parent_name);
						continue;
					}
					if( parents == null ){
						parents = new LinkedHashSet<>();
					}
					parents.add(parent);
					IndexedProducer prod = parent.getFactory(ctx);
					PropertyTargetGenerator fac = ExpressionCast.getPropertyTargetGenerator(prod);
					if( fac != null ){
						
						if( finder == null){
							finder = new MultiFinder();
						}
						finder.addFinder(fac.getFinder()); 
						finder.addFinder(prev); // add the previous finder second so it takes precidence
						if( map == null ){
							map = new PropExpressionMap();
						}
						for(PropertyTag t: fac.getFinder().getProperties()){
							if( parents.contains(t)){
								continue;
							}
							if( t instanceof ReferenceTag && ((ReferenceTag)t).getTable().equals(table)){
								continue;
							}

							try {
								if( fac.hasProperty(t)){
									map.put(t, new DeRefExpression(parent, t));
								}
							} catch (PropertyCastException e) {
								getLogger().error("error forwarding property", e);
							}
						}
					}
				}
			}
			return finder;
		}catch(Exception t){
			getLogger().error("Error setting  up InheritPolicy",t);
			return null;
		}
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.update.AbstractPropertyContainerUpdater#getDerivedProperties(uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap)
	 */
	@Override
	public PropExpressionMap getDerivedProperties(PropExpressionMap previous) {
		if( map == null ){
			return previous;
		}
		PropExpressionMap result = new PropExpressionMap(map);
		result.getAllFrom(previous); // previous values take precidence
		return result;
	}
	@Override
	public void getTableTransitionSummary(ContentBuilder hb, SessionService operator) {
		hb.addText("This policy adds derived property definitions for all properties defiend in a referenced (parent) table that are not previously defined by the parser or other policies");
		ExtendedXMLBuilder para = hb.getText();
		if( parents == null ){
			
			para.clean("Parent table is not defiend set the property ");
			para.open("b");
			para.clean(INHERIT_POLICY_PREFIX);
			para.open("em");
			para.clean("table-name");
			para.close();
			para.clean("parent");
			para.close();
			
		}else{
			para.clean("Parents are: "+parents.toString());
		}
		para.appendParent();
	}
	@Override
	public void addConfigParameters(Set<String> params) {
		params.add(INHERIT_POLICY_PREFIX+table+PARENT_SUFFIX);
		
	}

}
