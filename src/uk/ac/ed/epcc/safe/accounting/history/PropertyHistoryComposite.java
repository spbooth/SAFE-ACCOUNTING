package uk.ac.ed.epcc.safe.accounting.history;

import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.db.AccessorContributer;
import uk.ac.ed.epcc.safe.accounting.db.AccessorMap;
import uk.ac.ed.epcc.safe.accounting.expr.DeRefExpression;
import uk.ac.ed.epcc.safe.accounting.expr.DoubleDeRefExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionCast;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceExpression;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceTag;
import uk.ac.ed.epcc.webapp.model.data.Composite;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.IndexedFieldValue;
import uk.ac.ed.epcc.webapp.model.data.Repository;
import uk.ac.ed.epcc.webapp.model.history.HistoryFactory;
import uk.ac.ed.epcc.webapp.model.history.HistoryFactory.HistoryRecord;
/** A {@link Composite} to customise a {@link HistoryFactory} for
 * property support.
 * 
 * @author spb
 *
 * @param <P>
 * @param <H>
 */
public class PropertyHistoryComposite<P extends DataObject,F extends DataObjectFactory<P>,H extends HistoryRecord<P>> extends Composite<H, PropertyHistoryComposite> implements AccessorContributer<H> {
	public static final PropertyRegistry history= new PropertyRegistry("history","History table properties");
	public static final PropertyTag<Date> HISTORY_START = new PropertyTag<Date>(history,HistoryFactory.START_TIME_FIELD,Date.class,"Start of history period");
	public static final PropertyTag<Date> HISTORY_END = new PropertyTag<Date>(history,HistoryFactory.END_TIME_FIELD,Date.class,"End of history period");
	private PropertyRegistry peer;
	private ReferenceTag<P, F> PEER;
	public PropertyHistoryComposite(HistoryFactory<P,H> fac) {
		super(fac);
	}

	@Override
	protected Class<? super PropertyHistoryComposite> getType() {
		return PropertyHistoryComposite.class;
	}

	/** Extension point to allow custom accessors and registries to be added.
	 * 
	 * @param mapi2
	 * @param finder
	 * @param derived
	 */
	public void customAccessors(AccessorMap<H> mapi2, MultiFinder finder,
			PropExpressionMap derived) {
		finder.addFinder(StandardProperties.time);
		finder.addFinder(history);
		if( useHistoryAsTimeBounds()){
			try{
				derived.put(StandardProperties.STARTED_PROP, HISTORY_START);
				derived.put(StandardProperties.ENDED_PROP, HISTORY_END);
			}catch(Exception e){
				getLogger().error("Unexpected exception",e);
			}
		}
		HistoryFactory<P, H> historyFactory = (HistoryFactory<P,H>)getFactory();
		F fac = (F) historyFactory.getPeerFactory();
		Repository res = getRepository();
		peer = new PropertyRegistry(historyFactory.getTag()+"history", "history properties for "+historyFactory.getTag());
		PEER = new ReferenceTag<P, F>(peer, "peer", (Class<F>) fac.getClass(), fac.getTag());
		IndexedFieldValue referenceExpression = res.getReferenceExpression(historyFactory.getTarget(),historyFactory.getPeerName(),fac);
		mapi2.put(PEER,  referenceExpression);
		
		ExpressionTargetFactory<P> etf = ExpressionCast.getExpressionTargetFactory(fac);
		if( etf != null){
		// import properties from peer.
			PropertyFinder peer_props = etf.getFinder();
			finder.addFinder(peer_props);
			
				AccessorMap peer_map = etf.getAccessorMap();
				PropExpressionMap peer_derived = peer_map.getDerivedProperties();
				for(PropertyTag t : peer_props.getProperties()){
					if( etf.hasProperty(t)){
						try{
							if( peer_map.isDerived(t)){
								
								// if its an expression add the same expression here
								// we want to use the history values in preference to the current.
								derived.put(t, peer_derived.get(t));
							}else{
								// Fall back to getting value from peer 
								// We have a problem if the implementation is a "magic" accessor that calculates quantities based on other properties.
								// This will calculate the properties based on the current peer state not the historical state so we don't want to forward to
								// on the other hand reference tags need special handling
								if( t instanceof ReferenceExpression){
									derived.put(t, new DoubleDeRefExpression(PEER, (ReferenceExpression) t));
								}else{
									if( !  peer_map.isAccessor(t)){
										derived.put(t, new DeRefExpression(PEER, t));
									}
								}
							}
						}catch(Exception e){
							getLogger().error("Error adding peer property",e);
						
					}	
				}
			}
		}
	}
	/** Should the history time bounds be used as the {@link ExpressionTargetContainer} time bounds
	 * 
	 * This defaults to true but should be overridden if the peer is itself a UsageRecord
	 * and the history table is being used to view reports as seen from the past.
	 * 
	 * @return
	 */
    protected boolean useHistoryAsTimeBounds(){
    	return getContext().getBooleanParameter(getFactory().getConfigTag()+".use_history_as_time_bounds", true);
    }
}
