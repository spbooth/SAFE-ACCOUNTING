//| Copyright - The University of Edinburgh 2011                            |
//|                                                                         |
//| Licensed under the Apache License, Version 2.0 (the "License");         |
//| you may not use this file except in compliance with the License.        |
//| You may obtain a copy of the License at                                 |
//|                                                                         |
//|    http://www.apache.org/licenses/LICENSE-2.0                           |
//|                                                                         |
//| Unless required by applicable law or agreed to in writing, software     |
//| distributed under the License is distributed on an "AS IS" BASIS,       |
//| WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.|
//| See the License for the specific language governing permissions and     |
//| limitations under the License.                                          |
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.db;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.IllegalReductionException;
import uk.ac.ed.epcc.safe.accounting.NumberReductionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.DurationPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionCast;
import uk.ac.ed.epcc.safe.accounting.properties.FixedPropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidSQLPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.TagFilter;
import uk.ac.ed.epcc.safe.accounting.reference.IndexedTag;
import uk.ac.ed.epcc.safe.accounting.reference.ReferencePropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceTag;
import uk.ac.ed.epcc.safe.accounting.reports.ReportBuilder;
import uk.ac.ed.epcc.safe.accounting.selector.AndRecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.OverlapType;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.safe.accounting.selector.RelationClause;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Feature;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
import uk.ac.ed.epcc.webapp.forms.inputs.BooleanInput;
import uk.ac.ed.epcc.webapp.forms.inputs.Input;
import uk.ac.ed.epcc.webapp.forms.inputs.TimeStampInput;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.jdbc.expr.ConstExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.Reduction;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLValue;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.Duration;
import uk.ac.ed.epcc.webapp.model.data.IndexedFieldValue;
import uk.ac.ed.epcc.webapp.model.data.Repository;
import uk.ac.ed.epcc.webapp.model.data.SelfSQLValue;
import uk.ac.ed.epcc.webapp.model.data.filter.FilterUpdate;
import uk.ac.ed.epcc.webapp.model.data.forms.Selector;
import uk.ac.ed.epcc.webapp.model.data.forms.inputs.DurationInput;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedTypeProducer;
import uk.ac.ed.epcc.webapp.model.relationship.RelationshipProvider;
import uk.ac.ed.epcc.webapp.preferences.Preference;
import uk.ac.ed.epcc.webapp.session.SessionService;
import uk.ac.ed.epcc.webapp.session.UnknownRelationshipException;
import uk.ac.ed.epcc.webapp.time.Period;
import uk.ac.ed.epcc.webapp.timer.TimeClosable;
import uk.ac.ed.epcc.webapp.timer.TimerService;

/** A {@link AccessorMap} for {@link DataObjectFactory}s
 * 
 * @author spb
 * @param <X> type of DataObject
 *
 * 
 */


public class RepositoryAccessorMap<X extends DataObject> extends AccessorMap<X>{
	//Dangerous to set this default because of manually edited tabled like allocation
	// can change the max record length significantly in a session
	// set it per table for big accounting tables.
	private static final Feature CACHE_CUTOFFS = new Preference("reporting.cache_cutoff",false,"Cache the cutoffs in session",ReportBuilder.REPORT_DEVELOPER);

	private final DataObjectFactory<X> fac;
	private final Repository res;
	// additional selectors that cannot be determined directly from repository
		// These have to be selectors as inputs are not immutable and may have their
		// state changed after being returned the first time,
		private Map<String,Selector> selector_map = new HashMap<>();
	
	/** Encodes the rules for which kind of fields can
	 * be implemented as a numberif database field.
	 * 
	 */
	public static final TagFilter NumberFilter = new TagFilter() {
		
		public boolean accept(PropertyTag tag) {
			Class clazz = tag.getTarget();
			if( Number.class.isAssignableFrom(clazz)){
				// numbers obviously
				return true;
			}
			if( Date.class.isAssignableFrom(clazz)){
				// dates 
				return true;
			}
			if( tag instanceof IndexedTag){
				// reference tag
				return true;
			}
			return false;
		}
	};
	
	
	/**
	 * create an empty AccessorMap
	 * @param filter_tag type of enclosing factory 
	 * @param res 
	 * @param config_tag 
	 * 
	 */
	public RepositoryAccessorMap(DataObjectFactory<X> fac,Repository res) {
		super(fac.getContext(),fac.getTag(),fac.getConfigTag());
		this.fac=fac;
		this.res=res;
	}
	

	/**
	 * Populate this AccessorMap for the specified repository using the best
	 * match to the field name from the PropertyFinder. Basically each database field is mapped to
	 * a property. The
	 * default algorithm is to search for a property with the same name as the
	 * database field. This can be overridden by setting the 
	 * <b>accounting.</b><em>table-name</em><b>.</b><em>field-name</em> property. As this can
	 * specify a fully qualified property name then this can also be useful if
	 * there is more than one property with the same name. Optionally unmatched fields can have
	 * corresponding properties created.
	 * 
	 * Fields that reference have usually already
	 * been handled by {@link #makeReferences} so these can be safely ignored. However if there
	 * is a matching reference property then this is processed in case it is a static Property from
	 * a policy. 
	 * 
	 * @param finder PropertyFinder
	 * @param orphan_registy Optional registry to create tags without binding
	 * @param warn_orphan
	 *            boolean set to true to make unmatched fields an error.
	 */
	@SuppressWarnings("unchecked")
	public void populate(PropertyFinder finder,
			PropertyRegistry orphan_registy,
			boolean warn_orphan){

		//log.debug("AccessorMap.populate for "+res.getTag());
		String prefix = CONFIG_PREFIX + res.getTag() + ".";
		for (String field_name : res.getFields()) {
			Repository.FieldInfo info = res.getInfo(field_name);
			String prop_name = getContext().getInitParameter(prefix + field_name,
					field_name);
			//log.debug("consider field "+field_name);



			PropertyTag tag = null;
			if( finder != null ){  // first try a lookup
				// use some simple type based disambiguation.
				// string and date fields must be tags of the corresponding type.
				// The default is to just do name lookup. Numbers in particular
				// may get mapped to other types.
				if(info.isString()){
					tag = finder.find(String.class,prop_name);
				}else if( info.isDate()){
					tag = finder.find(Date.class,prop_name);
				}else if ( info.isReference()){
					tag = finder.find(IndexedReference.class,prop_name);
				}else if( info.isNumeric()){
					// This could be a date, number or reference 
					tag = finder.find(NumberFilter,prop_name);
				}else{
					tag = finder.find(prop_name);
				}
			}
			//log.debug("Finder returned "+(tag==null?" null ":tag.getFullName()));
			if( tag == null && orphan_registy != null && ! info.isReference()){ // then try to make the tag
				boolean force_date = getContext().getBooleanParameter(prefix+field_name+".forceDate", false);
				if( force_date ){
					selector_map.put(field_name, new Selector(){
						@Override
						public Input getInput() {
							return new TimeStampInput(res.getResolution());
						}});
				}

				int idx = prop_name.lastIndexOf(FixedPropertyFinder.PROPERTY_FINDER_SEPERATOR);
				if (idx >= 0) {
					// remove any repository name as we always want to create within
					// the local repository but populate may want to rename.
					// use the new bare-name in case we want to reference this
					// renamed property.
					prop_name = prop_name.substring(idx + 1);
				}

				if (info.isString()) {
					tag = new PropertyTag<>(orphan_registy, prop_name,String.class);
				} else if (info.isDate() || (info.isNumeric()&&force_date)) {
					tag = new PropertyTag<>(orphan_registy, prop_name,Date.class);
				} else if (info.isNumeric()) {

					tag = new PropertyTag<>(orphan_registy, prop_name,
							Number.class);
				}
				//						if( tag != null ){
				//							log.debug("made tag "+tag.getFullName()+" "+tag.getTarget().getCanonicalName());
				//						}
			}
			//log.debug("field="+field_name+" prop_name="+prop_name+" tag="+tag.getFullName());
			if( tag != null && ! testProperty(tag, false)){
				//log.debug("tag is "+tag.getFullName());
				Class t = tag.getTarget();
				if (String.class.isAssignableFrom(t)) {
					put(tag, res.getStringExpression(field_name));
				} else if (Date.class.isAssignableFrom(t)) {
					put(tag, res.getDateExpression(field_name));
					selector_map.put(field_name, new Selector(){
						@Override
						public Input getInput() {
							return new TimeStampInput(res.getResolution());
						}});
				} else if (Number.class.isAssignableFrom(t)) {
					//Duration is supported at native millisecond resolution.
					put(tag, res.getNumberExpression(t,field_name));
					if (Duration.class.isAssignableFrom(t)) {
						// we could support different resolutions using a DurationFieldValue
						//put(tag, new DurationFieldValue(res.getNumberExpression(target,Number.class,field_name),1L));
						selector_map.put(field_name, new Selector(){
							@Override
							public Input getInput() {
								//Duration is supported at native millisecond resolution.
								return new DurationInput(1L);
							}});
					} 
				} else if (Boolean.class.isAssignableFrom(t)) {
					put(tag, res.getBooleanExpression(field_name));
					selector_map.put(field_name, new Selector(){
						@Override
						public Input getInput() {
							return new BooleanInput();
						}});
				} else if( tag instanceof IndexedTag ){
					// This may be an explicit reference from a policy registry
					// where the tag has the necessary info to create the TypeProducer
					// SafePolicy does this.
					// In this case the repository may or may not have the field recorded as a reference tag.
					// If we add a type-producer to the registry the field will thereafter become a reference field
					// so re-check the reference registry. We don't want to have a different result if we subsequently 
					// create a new AccessorMap from a cached copy of the Repository
					
					if( ! (tag.getRegistry() instanceof ReferencePropertyRegistry)){
						// this is not just an existing ref prop with the field name equal to the remote table
						IndexedTag ref_tag = (IndexedTag) tag;
						IndexedFieldValue referenceExpression=null;
						if( res.hasTypeProducer(field_name)){
							// known to be a reference field. add the name match tag
							referenceExpression = res.getReferenceExpression(field_name);
						}else{

							//log.debug("Reference tag "+ref_tag.getFactoryClass().getCanonicalName()+" "+ref_tag.getTable());
							IndexedTypeProducer prod = new IndexedTypeProducer(field_name, getContext(),ref_tag.getFactoryClass(),ref_tag.getTable());

							res.addTypeProducer(prod);
							referenceExpression= new IndexedFieldValue(res,prod);
							
							// Now look for the table tag that might also match 
							ReferenceTag table_tag = (ReferenceTag) finder.find(IndexedReference.class, ReferencePropertyRegistry.REFERENCE_REGISTRY_NAME+FixedPropertyFinder.PROPERTY_FINDER_SEPERATOR+field_name);
							if( table_tag != null ){
								put(table_tag,referenceExpression);
							}
						}
						put( ref_tag, referenceExpression);
						if( referenceExpression instanceof Selector){
							selector_map.put(field_name,referenceExpression);
						}
					}

				} else {
					String prob = "Unsupported target class " + t.getCanonicalName()
							+ " for field " + field_name;
					//log.debug(prob);
					if (warn_orphan) {
						throw new ConsistencyError(prob);
					}
					getLogger().error(prob);
				}
			}else{
				if( warn_orphan && ! info.isReference() ){
					throw new ConsistencyError("No matching tag found for field "+field_name);
				}
			}

		}

	}
	/** Add properties defined as a Relationship between the object and the 
	 * current user from an external {@link RelationshipProvider}.
	 * We assume that directly implemented relationships can be added from the implementing class.
	 * 
	 * The list of tags corresponding to the {@link RelationshipProvider}s to add should be
	 * set in:
	 *  <b><em>factory-tag</em>.relationships</b>
	 * 
	 * @param tag 
	 * @return PropertyFinder
	 */
	@SuppressWarnings("unchecked")
	public PropertyFinder setRelationshipProperties(DataObjectFactory<X> fac){
		AppContext c = getContext();
		// Relationship properties
		String tag = fac.getTag();
		SessionService serv = c.getService(SessionService.class);
		MultiFinder finder = new MultiFinder();
		if( serv != null){
		String relationships=c.getInitParameter(tag+".relationships");
		if( relationships != null){
			String tags[] = relationships.split(",");
			for(String t : tags){
			   try{
				   // If target factory is wrong relationship always returns false
				RelationshipProvider<?,X> rel = c.makeObject(RelationshipProvider.class, t);
				if( rel != null ){
					PropertyRegistry reg = new PropertyRegistry(t,"Relationships via "+t);
				    for(String role : rel.getRelationships()){
				    	put(new PropertyTag<>(reg, role,Boolean.class), new RelationshipAccessor<>(fac, tag+"."+role));
				    }
				    reg.lock();
				    finder.addFinder(reg);
				}
			   }catch(Exception e){
				   getLogger().error("Error adding relationship "+t+" to "+tag,e);
			   }
			}
		}
		}
		return finder;
	}
	/** Register references from the {@link ReferencePropertyRegistry}
	 * This generates properties named after the target table.
	 * @param reference_registry
	 */
	@SuppressWarnings("unchecked")
	public void makeReferences(
			ReferencePropertyRegistry reference_registry) {
	


		for (String field_name : res.getFields()) {
			Repository.FieldInfo info = res.getInfo(field_name);
			String ref = info.getReferencedTable();
			
				
				if( ref != null ){
					// this should be a reference tag. 
					// look for a reference tag named after the target table
					// If the tag is not known then we can't make a handler factory so ignore.
					IndexedTag tag=(IndexedTag) reference_registry.find(IndexedReference.class, ref);
					if( tag != null ){
						IndexedFieldValue referenceExpression = res.getReferenceExpression(field_name);
						if( referenceExpression != null ){
							put(tag, referenceExpression);
							selector_map.put(referenceExpression.getFieldName(), referenceExpression);
						}
					}
				}
		}
		
		
		// Self reference
		// Needed when filtering on a specific recordID
		// Also a way of getting the id from a proxy wrapper
		ReferenceTag tag=(ReferenceTag) reference_registry.find(IndexedReference.class, res.getTag());
		if( tag != null && ! selector_map.containsValue(tag)){
			put(tag, new SelfSQLValue<>(fac));
		}
				
	}


	

   
    /** Generate default set of form selectors
     * 
     * @return Map of seelctors
     */
    public Map<String,Object> getSelectors(){
    	return new HashMap<>(selector_map);
    }
	
	
	protected void addSource(StringBuilder sb) {
		res.addSource(sb, true);
		
	}
	
	/** get a unique name for this {@link AccessorMap} when caching cutoffs in the session. 
	 * 
	 * @return
	 */
	protected String getCutoffTag() {
		String tag = res.getTable();
		String db_tag = getDBTag();
		if( db_tag != null ) {
			tag = db_tag+":"+tag;
		}
		return tag;
	}
	protected String getDBTag() {
		return res.getDBTag();
	}


	@Override
	public BaseFilter<X> getRelationshipFilter(String relationship) throws CannotFilterException {

		try {
			return getContext().getService(SessionService.class).getRelationshipRoleFilter(fac, relationship);
		} catch (UnknownRelationshipException e) {
			throw new CannotFilterException(e);
		}
	}


	
	public static final Feature AUTO_CUTOFF_FEATURE = new Preference("auto_cutoff",true,"automatically calculate cutoffs (maximum record time extent, used to optimise search) using additional queries",ReportBuilder.REPORT_DEVELOPER);
	
	public BaseFilter<X> getPeriodFilter(Period period,
			PropExpression<Date> start, PropExpression<Date> end, OverlapType type,long cutoff)
					throws CannotFilterException {
		if( start == null || end == null || start.equals(end)){
			cutoff=0L;
		}else if(getContext().getBooleanParameter("reporting.auto_cutoff."+fac.getConfigTag(), AUTO_CUTOFF_FEATURE.isEnabled(getContext()))){
			// Only check for an explicit zero
			// a negative cutoff will supress auto-generation and
			// allow records with a zero start time.
			if( cutoff == 0L) {
			
				cutoff=calculateCutoff(start, end);
			}
		}
		return super.getPeriodFilter(period, start, end,type,cutoff);
	}

	@Override
	public long calculateCutoff(PropExpression<Date> start, PropExpression<Date> end) {
		Long calc_cutoff=null;
		SessionService sess=null;		
		TimerService timer = getContext().getService(TimerService.class);
		String cutoff_name = "auto_cutoff."+getCutoffTag()+"_"+start.toString()+"_"+end.toString();
		if(getContext().getBooleanParameter("reporting.cache_cutoff."+fac.getConfigTag(), CACHE_CUTOFFS.isEnabled(getContext()))) {
			sess = getContext().getService(SessionService.class);
			if(sess !=null) {
				calc_cutoff=(Long) sess.getAttribute(cutoff_name);
			}
		}
		if( calc_cutoff == null){
			calc_cutoff = (Long) getContext().getAttribute(cutoff_name);
		}
		if(calc_cutoff==null) {
			try(TimeClosable tim = new TimeClosable(timer, cutoff_name)) {
				// Check this uses SQL
				SQLValue<Date> start_value = getSQLValue(start);
				SQLValue<Date> end_value = getSQLValue(end);
				if( start_value != null && end_value != null) {
					if( start_value instanceof ConstExpression || end_value instanceof ConstExpression) {
						// Constant date bounds are not good candidates for using cutoff
						// probably relate to the current time anyway
						// just suppress use of cutoff in this case.
						calc_cutoff=0L;
					}else {
						calc_cutoff = getCutoff(null,start, end);
					}
					//if( log !=null) log.debug(getDBTag()+": calculated cutoff for "+start+","+end+" as "+cutoff);
				}else {
					calc_cutoff=0L;
				}
			}catch(InvalidSQLPropertyException isp) {
				calc_cutoff=0L;
			} catch (Exception e) {
				getLogger().error("Error making cutoff",e);
				calc_cutoff=0L;
			}
		}
		if(sess !=null ) {
			sess.setAttribute(cutoff_name, calc_cutoff);
		}else {
			getContext().setAttribute(cutoff_name, calc_cutoff);
		}
		return calc_cutoff;
	}

	/** calculate the maximum difference between the two date expressions for the entire table
	 * @param start
	 * @param end
	 * @return
	 * @throws Exception
	 * @throws IllegalReductionException
	 */
	private Long getCutoff(RecordSelector narrow,PropExpression<Date> start, PropExpression<Date> end)
			throws Exception{
		Long calc_cutoff;
		final DurationPropExpression duration = new DurationPropExpression(start, end);
		// go for global max length. More likely to cache
		// answer in Sql level and independent of period so can cheaply cache
		// at this level. Alternative would need map keyed by props and period.
		AndRecordSelector sel = new AndRecordSelector(narrow);
		sel.add(new SelectClause<>(duration,MatchCondition.GT,new Duration(0L,1L)));
		sel.add(new SelectClause<>(start,MatchCondition.GT,new Date(0L)));
		// Want to avoid reversed time bounds. Won't affect result as 
		// we are generating a max but 
		// Might occur legitimately if we are using a constant date as one time-bound
		sel.add(new RelationClause<>(start,MatchCondition.LT,end));
		Number reduction = getReductionHandler().getReduction(NumberReductionTarget.getInstance(Reduction.MAX, duration), sel);
		// This might be null if no records
		if( reduction == null ) {
			return Long.valueOf(1L);
		}
		long l = reduction.longValue()+1L;
		calc_cutoff = Long.valueOf(l);
		return calc_cutoff;
	}
	private ReductionHandler<X, ExpressionTargetFactory<X>> getReductionHandler(){
		return new ReductionHandler<>(ExpressionCast.getExpressionTargetFactory(fac),false);
	}
	public FilterUpdate<X> getFilterUpdate(){
		return new FilterUpdate<>(res);
	}


	@Override
	protected final Set<Repository> getSourceTables() {
		HashSet<Repository> set = new HashSet<>();
		set.add(res);
		return set;
	}
}