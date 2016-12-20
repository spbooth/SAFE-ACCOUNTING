package uk.ac.ed.epcc.safe.accounting.policy;

import uk.ac.ed.epcc.safe.accounting.db.UsageRecordFactory;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceTag;
import uk.ac.ed.epcc.safe.accounting.update.UsageRecordPolicy;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Indexed;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
/** A {@link UsageRecordPolicy} that adds a reverse reference to the created object from one of the records 
 * it references.
 * 
 * @author spb
 *
 */
public class BackLinkPolicy extends BaseUsageRecordPolicy {

	
	private AppContext c;
	private ReferenceTag remote_tag=null;
	private ReferenceTag back_ref=null;
	private UsageRecordFactory<?> remote_fac=null;
	private static final String BACKLINK_POLICY_TARGET = "BackLinkPolicy.target.";
	@Override
	public PropertyFinder initFinder(AppContext ctx, PropertyFinder prev, String table) {
		c = ctx;
		String target_name = c.getInitParameter(BACKLINK_POLICY_TARGET+table);
		PropertyTag<? extends IndexedReference>tag = prev.find(IndexedReference.class,target_name );
		if( tag instanceof ReferenceTag ){
			ReferenceTag ref_tag = (ReferenceTag) tag;
			if( UsageRecordFactory.class.isAssignableFrom(ref_tag.getFactoryClass())){
			   remote_tag=ref_tag;
			   remote_fac=(UsageRecordFactory) remote_tag.getFactory(c);
			   PropertyFinder remote_finder=remote_fac.getFinder();
			   back_ref = (ReferenceTag) remote_finder.find(IndexedReference.class,table);
			 
			}
		}
		// this policy defines no new properties.
		return null;
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.policy.BaseUsageRecordPolicy#postCreate(uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer, uk.ac.ed.epcc.safe.accounting.UsageRecord)
	 */
	@Override
	public void postCreate(PropertyContainer props, ExpressionTargetContainer rec) throws Exception {
		if( remote_tag == null || back_ref == null ){
			return;
		}
		Indexed remote = remote_tag.get(c, props);
		if( remote != null && remote instanceof PropertyContainer && remote instanceof DataObject){
			back_ref.set((PropertyContainer)remote, (Indexed) rec);
			((DataObject)remote).commit();
		}
	}
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.policy.BaseUsageRecordPolicy#preDelete(uk.ac.ed.epcc.safe.accounting.UsageRecord)
	 */
	@Override
	public void preDelete(ExpressionTargetContainer rec) throws Exception {
		if( remote_tag == null || back_ref == null ){
			return;
		}
		Indexed remote = remote_tag.get(c, rec);
		if( remote != null && remote instanceof PropertyContainer && remote instanceof DataObject){
			PropertyContainer container = (PropertyContainer)remote;
			IndexedReference ref = (IndexedReference) container.getProperty(back_ref);
			if( ref != null && ref.getID() == ((DataObject)rec).getID()){
				container.setProperty(back_ref, null);
				((DataObject)remote).commit();
			}
		}
	}
}
