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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.ExpressionFilterTarget;
import uk.ac.ed.epcc.safe.accounting.expr.CasePropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ConstPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.EvaluatePropExpressionVisitor;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTarget;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.expr.ImplementationPropExpressionVisitor;
import uk.ac.ed.epcc.safe.accounting.expr.Parser;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionVisitor;
import uk.ac.ed.epcc.safe.accounting.expr.PropertyCastException;
import uk.ac.ed.epcc.safe.accounting.expr.ResolveCheckVisitor;
import uk.ac.ed.epcc.safe.accounting.properties.FixedPropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidSQLPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.SetPropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.properties.TagFilter;
import uk.ac.ed.epcc.safe.accounting.reference.IndexedTag;
import uk.ac.ed.epcc.safe.accounting.reference.ReferencePropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceTag;
import uk.ac.ed.epcc.safe.accounting.selector.OverlapType;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.Feature;
import uk.ac.ed.epcc.webapp.Indexed;
import uk.ac.ed.epcc.webapp.Targetted;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.inputs.BooleanInput;
import uk.ac.ed.epcc.webapp.forms.inputs.TimeStampInput;
import uk.ac.ed.epcc.webapp.jdbc.expr.Accessor;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.jdbc.expr.CaseExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.ConstExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.FilterProvider;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLExpressionFilter;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLExpressionMatchFilter;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLExpressionNullFilter;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLExpressionOrderFilter;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLValue;
import uk.ac.ed.epcc.webapp.jdbc.filter.AndFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterConverter;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.jdbc.filter.NoSQLFilterException;
import uk.ac.ed.epcc.webapp.jdbc.filter.OrderFilter;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.Duration;
import uk.ac.ed.epcc.webapp.model.data.FieldValue;
import uk.ac.ed.epcc.webapp.model.data.IndexedFieldValue;
import uk.ac.ed.epcc.webapp.model.data.Repository;
import uk.ac.ed.epcc.webapp.model.data.forms.Selector;
import uk.ac.ed.epcc.webapp.model.data.forms.inputs.DurationInput;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedTypeProducer;
import uk.ac.ed.epcc.webapp.model.relationship.RelationshipProvider;
import uk.ac.ed.epcc.webapp.session.SessionService;
import uk.ac.ed.epcc.webapp.time.Period;

/** Common {@link PropExpression} logic that can be incorporated (by composition) into factory classes for {@link DataObject}s that implement {@link ExpressionTarget}.
 * 
 * <p>
 * Contains maps from properties to the {@link Accessor}/{@link SQLValue}/{@link SQLExpression} needed to extract 
 * data from the record/query.
 * </p>
 * <p>Also contains a {@link PropExpressionMap} allowing properties to be defined in terms of {@link PropExpression}s.
 * Special extended {@link PropExpressionVisitor}s are used when creating an {@link SQLValue}/{@link SQLExpression} or evaluating
 * an expression so these derived properties can be consulted when processing a {@link PropertyTag}.
 * </p>
 * <p>
 * While it is legal for two properties to map to the same accessor this means that the properties are aliases and
 * updating one will update the other. This can cause problems if a parser generates both properties so the default
 * logic that populates the AccessorMap from the {@link Repository} avoids this. Where parsing is not required a class can add duplicate mapping explicitly.
 * </p> 
 * <p>
 * Composite {@link Accessor}s are not created as evaluating the expression directly with a visitor takes the same amount of
 * recursive calls as a required by the {@link Accessor}.
 * </p>
 * Normally all properties are accessible from a {@link SQLValue}. This is true in the standard accounting classes
 * but it is possible to install a custom {@link Accessor} for a value that can only be evaluated in java.
 *  
 * 
 * 
 * @author spb
 * @param <X> type of DataObject
 *
 * 
 */


public class AccessorMap<X extends DataObject&ExpressionTarget> implements Contexed, ExpressionFilterTarget<X>, Targetted<X>{
	public static final Feature EVALUATE_CACHE_FEATURE = new Feature("evaluate.cache",true,"cache expression evaluations in ExpressionTargets");
	private final Class<? super X> target;
	private final Repository res;
	private final String config_tag;
	private static final String CONFIG_PREFIX = "accounting.";
	private Logger log;
	// derived prop expressions
	private PropExpressionMap derived;
	// accessors for the leaf properties.
	private Map<PropertyTag, Accessor> accessor_map = new HashMap<PropertyTag, Accessor>();
    // SQLValue for leaf values
	private Map<PropertyTag, SQLValue> value_map = new HashMap<PropertyTag, SQLValue>();
	// SQL expressions for leaf values.
	private Map<PropertyTag, SQLExpression> expression_map = new HashMap<PropertyTag, SQLExpression>();

	// additional selectors that cannot be determined directly from repository
	private Map<String,Object> selector_map = new HashMap<String,Object>();
	
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
	/** Class to evaluate a PropExpression on a record.
	 * 
	 * This acts as a proxy implementing most of {@link ExpressionTargetContainer} via the Accessors.
	 * Note that the getExpressionTargetFactory method is forwarded to the underlying object.
	 * Application classes will usually implement ExpressionTarget by holding a reference to
	 * one of these.
	 * 
	 * 
	 * This class can also implements a cache of result values that is populated
	 * by calling the evaluate method and cleared with the flush method.
	 * This allows a record to hold a copy of its own visitor and use it as a value cache.
	 * @author spb
	 *
	 */
	public class ExpressionTargetProxy  extends EvaluatePropExpressionVisitor implements ExpressionTargetContainer{

		private final X record;
		private Set<PropertyTag> missing;
		private Map<PropExpression,Object> cache=null;
		private final boolean use_cache;
		public ExpressionTargetProxy(X r,boolean use_cache){
			super(r.getContext());
			this.record=r;
			missing=new HashSet<PropertyTag>();
			this.use_cache=use_cache;
		}
		public ExpressionTargetProxy(X r){
			this(r,EVALUATE_CACHE_FEATURE.isEnabled(r.getContext()));
		}
		@SuppressWarnings("unchecked")
		public <R> R evaluateExpression(PropExpression<R> expr) throws InvalidExpressionException{
			if( use_cache){
			if( cache == null ){
				cache=new HashMap<PropExpression, Object>();
			}else{
				if(cache.containsKey(expr)){
					return (R) cache.get(expr);
				}
			}
			}
			R result;
			try {
				result = (R) expr.accept(this);
			}catch(InvalidPropertyException ee){
				throw ee;
			} catch (Exception e) {
				getLogger().error("Unexpected exception evaluating expression",e);
				throw new InvalidExpressionException("Error evaluating expression "+expr.toString(), e);
			}
			if(use_cache){
				cache.put(expr, result);
			}
			return result;
		}
		public <R> R evaluateExpression(PropExpression<R> expr, R def){
			try {
				return evaluateExpression(expr);
			} catch (InvalidExpressionException e) {
				return def;
			}
		}
		public void flush(){
			if( cache != null ){
				cache.clear();
			}
		}
		public <T> void setProperty(PropertyTag<? super T> t, T value)throws InvalidPropertyException{
			@SuppressWarnings("unchecked")
			Accessor<T,X> a = accessor_map.get(t);
			if( a == null || ! a.canSet()){
				throw new InvalidPropertyException(config_tag,t);
			}
			flush();
			a.setValue(record, value);
		}
		public <T> void setOptionalProperty(PropertyTag<? super T> t, T value){
			@SuppressWarnings("unchecked")
			Accessor<T,X> a = accessor_map.get(t);
			if( a == null || ! a.canSet()){
				return;
			}
			flush();
			a.setValue(record, value);
			
		}
		
		@SuppressWarnings("unchecked")
		public Object visitPropertyTag(PropertyTag<?> tag) throws Exception {
			
			Accessor a = accessor_map.get(tag);
			if( a != null ){
				return a.getValue(record);
			}
			if( missing.contains(tag)){
				// We have looped
				throw new InvalidPropertyException(config_tag,tag);
			}
			
			PropExpression expr = derived.get(tag);
			if( expr == null){
				//log.debug("No definition for "+tag.getFullName());
				throw new InvalidPropertyException(config_tag,tag);
			}
			missing.add(tag);
			//log.debug("evaluate: "+tag.getFullName()+"->"+expr.toString());
			Object o = expr.accept(this);
			missing.remove(tag);
			return o;
		}
		public void reset(){
			missing.clear();
		}
		
		public <T> T getProperty(PropertyTag<T> tag, T def) {

			if( ! tag.allow(def)){
				throw new ClassCastException("Invalid object as default");
			}
			try{
				return evaluateExpression(tag);
			}catch(InvalidExpressionException e){
				return def;
			}
			
		}
	
		public boolean supports(PropertyTag<?> tag) {
			return hasProperty(tag);
		}
		
		
		public <T> T getProperty(PropertyTag<T> key)
				throws InvalidExpressionException {
			return evaluateExpression(key);
		}
		
		public boolean writable(PropertyTag<?> tag) {
			return AccessorMap.this.writable(tag);
		}
		
		
		public Set<PropertyTag> getDefinedProperties() {
			return getProperties();
		}
		@SuppressWarnings("unchecked")
		public void setAll(PropertyContainer source) {
			for(PropertyTag t : source.getDefinedProperties()){
				if( writable(t)){
					try {
						setProperty(t, source.getProperty(t));
					} catch (InvalidExpressionException e) {
						getLogger().error("Error copying property",e);
					}
				}
			}
			
		}
		public Parser getParser() {
			return new Parser(getContext(), new SetPropertyFinder(getDefinedProperties()));
		}
		private MatchSelectVisitor<ExpressionTargetProxy> match_visitor=null;
		@Override
		protected boolean matches(RecordSelector sel) throws Exception {
			if(match_visitor == null){
				match_visitor=new MatchSelectVisitor<AccessorMap<X>.ExpressionTargetProxy>(this);
			}
		
			return sel.visit(match_visitor).booleanValue();
			
		}
		
	}
	public class SQLExpressionVisitor extends CreateSQLExpressionPropExpressionVisitor{
		public SQLExpressionVisitor(AppContext c) {
			super(c);
		}
		private Set<PropertyTag> missing = new HashSet<PropertyTag>();
		public SQLExpression visitPropertyTag(PropertyTag<?> tag)
				throws Exception {
			SQLExpression exp = expression_map.get(tag);
			if( exp != null ){
				return exp;
			}
			if( missing.contains(tag)){
				// We have looped
				throw new InvalidSQLPropertyException(config_tag,tag);
			}
			
			PropExpression<?> expr = derived.get(tag);
			if( expr == null){
				//log.debug("SQLExpressionVisitor No definition for "+tag.getFullName()+" "+config_tag);
				throw new InvalidSQLPropertyException(config_tag,tag);
			}
			missing.add(tag);
			
			//log.debug("evaluating SQLExpression: "+tag.getFullName()+"->"+expr.toString()+" "+config_tag);
			SQLExpression o = expr.accept(this);
			//log.debug("found SQLExpression: "+tag.getFullName()+"->"+o.toString()+" "+config_tag);
			// remember this
			expression_map.put(tag, o);
			missing.remove(tag);
			return o;
		}
		public void reset() {
			missing.clear();
		}
		@Override
		public SQLExpression<? extends Number> convertDateExpression(SQLExpression<Date> d) {
			return res.convertDateExpression(d);
		}
		@Override
		public SQLExpression<Date> convertMilliExpression(SQLExpression<? extends Number> d) {
			return res.convertMilliExpression(d);
		}
		public <T> SQLExpression visitCasePropExpression(
				CasePropExpression<T> expr) throws Exception {
			return getCaseExpression(expr);
		}
		
	}
	public class SQLValueVisitor extends CreateSQLValuePropExpressionVisitor{
		public SQLValueVisitor(AppContext c) {
			super(target,c);
		}
		private Set<PropertyTag> missing = new HashSet<PropertyTag>();
		public SQLValue visitPropertyTag(PropertyTag<?> tag)
				throws Exception {
			SQLValue exp = value_map.get(tag);
			if( exp != null ){
				return exp;
			}
			if( missing.contains(tag)){
				// We have looped
				throw new InvalidSQLPropertyException(config_tag,tag);
			}
			
			PropExpression<?> expr = derived.get(tag);
			if( expr == null){
				//log.debug("SQLValueVisitor No definition for "+tag.getFullName()+" "+config_tag+" "+derived.size());
//				for(PropertyTag<?> t : derived.keySet()){
//					log.debug(config_tag+" "+t.getFullName()+"->"+derived.get(t).toString());
//				}
				throw new InvalidSQLPropertyException(config_tag,tag);
			}
			missing.add(tag);
			//log.debug("evaluate SQLValue: "+tag.getFullName()+"->"+expr.toString()+" "+config_tag);
			SQLValue o = expr.accept(this);
			//log.debug("found SQLValue: "+tag.getFullName()+"->"+o.toString()+" "+config_tag);
			// remember this 
			value_map.put(tag,o);
			missing.remove(tag);
			return o;
		}
		public void reset(){
			missing.clear();
		}
		@Override
		public <P> boolean includeSelectClause(PropExpression<P> e) {
			return resolves(e,false);
		}
		@Override
		protected SQLValue convertDateExpression(SQLExpression<Date> de) {
			return res.convertDateExpression(de);
		}
		@Override
		protected SQLValue convertMilliExpression(SQLExpression<Number> de) {
			return res.convertMilliExpression(de);
		}
		public <T> SQLValue visitCasePropExpression(CasePropExpression<T> expr)
				throws Exception {
			return getCaseExpression(expr);
		}
		
	}
	public class ResolveChecker extends ResolveCheckVisitor{
		private final Set<PropertyTag> missing = new HashSet<PropertyTag>();
		private final boolean require_sql;
		public ResolveChecker(Logger log,boolean require_sql) {
			super(log);
			this.require_sql=require_sql;
		}
		public boolean getRequreSQL(){
			return require_sql;
		}
		public Boolean visitPropertyTag(PropertyTag<?> tag) throws Exception {
			//log.debug("resolve check for "+tag.getFullName()+" "+config_tag);
			if( ! require_sql){
				if( accessor_map.containsKey(tag)){
					//log.debug(tag.getFullName()+" resolves to accessor "+config_tag);
					return Boolean.TRUE;
				}
				if( value_map.containsKey(tag)){
					//log.debug(tag.getFullName()+" resolves to SQLValue "+config_tag);
					return Boolean.TRUE;
				}
			}
			if( expression_map.containsKey(tag)){
				//log.debug(tag.getFullName()+" resolves to SQLExpression "+config_tag);
				return Boolean.TRUE;
			}
			if( missing.contains(tag)){
				debug("Already looking for "+tag.getFullName()+" "+config_tag);
				return Boolean.FALSE;
			}
			PropExpression<?> expr = derived.get(tag);
			if( expr == null){
				debug("No derived definition for "+tag.getFullName()+" from "+config_tag);
				return Boolean.FALSE;
			}
			missing.add(tag);
			
			Boolean o = expr.accept(this);
			if( o ){
				missing.remove(tag);
			}
			return o;
		}
		public void reset(){
			missing.clear();
		}
	}
	public class ImplementationVisitor extends ImplementationPropExpressionVisitor{
		private Set<PropertyTag> missing = new HashSet<PropertyTag>();
		public String visitPropertyTag(PropertyTag<?> tag) throws Exception {
			// Just return tag names for defined leaf properties
			if( value_map.containsKey(tag)){
				return tag.getFullName();
			}
			if( accessor_map.containsKey(tag)){
				return tag.getFullName();
			}
			if( missing.contains(tag)){
				// We have looped
				throw new InvalidPropertyException(config_tag,tag);
			}
			
			PropExpression<?> expr = derived.get(tag);
			if( expr == null){
				throw new InvalidPropertyException(config_tag,tag);
			}
			missing.add(tag);
			//log.debug("evaluate SQLValue: "+tag.getFullName()+"->"+expr.toString()+" "+config_tag);
			String o = expr.accept(this);
		
			missing.remove(tag);
			return o;
			
		}
		
	}
	/**
	 * create an empty AccessorMap
	 * @param target type of enclosing factory 
	 * @param res 
	 * @param config_tag 
	 * 
	 */
	public AccessorMap(Class<? super X> target,Repository res,String config_tag) {
		this.target=target;
		this.res=res;
		this.config_tag=config_tag;
		derived = new PropExpressionMap();
		log = getContext().getService(LoggerService.class).getLogger(getClass());
		
		// might be better to do this as an expression
		// the following only works because we are adding a SQLAccessor
		put(StandardProperties.TABLE_PROP, new ConstExpression<String,X>(String.class, config_tag));
		
		try {
			// This is only a fall-back expression. Aggregate records may store the
			// job count as a DB field so we can't hardwire this as an accessor
			derived.put(StandardProperties.COUNT_PROP,  new ConstPropExpression<Long>(Long.class, Long.valueOf(1L)));
		} catch (PropertyCastException e) {
			getLogger().error("Error adding count expression",e);
		}
	}
	public <T> void put(PropertyTag<? extends T> tag, Targetted<T> obj){
		//log.debug("add accessor "+config_tag+" "+tag.getFullName()+" "+obj.toString()+" class:"+obj.getClass().getCanonicalName());
		boolean added=false;
		if( obj instanceof Accessor){
			accessor_map.put(tag, (Accessor) obj);
			added=true;
		}
		if( obj instanceof SQLValue){
			value_map.put(tag, (SQLValue) obj);
			added=true;
		}
		if( obj instanceof SQLExpression){
			expression_map.put(tag, (SQLExpression) obj);
			added=true;
		}
		if( ! added ){
			throw new ConsistencyError("Targettted object not a known type.");
		}
	}

	/**
	 * Test to see if it is possible to have an object that can be assigned to
	 * both of two specified types.
	 * 
	 * @param a
	 * @param b
	 * @return boolean
	 */
	@SuppressWarnings("unchecked")
	public boolean possibleCompatible(Class a, Class b) {
		return a.isInterface() || b.isInterface() || a.isAssignableFrom(b)
				|| b.isAssignableFrom(a);
	}

	

	

	private SQLExpressionVisitor sql_expression_visitor = null;
	@SuppressWarnings("unchecked")
	public <T> SQLExpression<T> getSQLExpression(PropExpression<T> expr) throws InvalidSQLPropertyException {
		if( expr instanceof PropertyTag){
			PropertyTag<T> tag = (PropertyTag<T>) expr;
			SQLExpression<T> a = expression_map.get(tag);
			if( a != null ){
				return a;
			}
		}
		if( sql_expression_visitor == null ){
			sql_expression_visitor= new SQLExpressionVisitor(getContext());
		}
		try {
			return expr.accept(sql_expression_visitor);
		}catch(InvalidSQLPropertyException ee){
			throw ee;
		} catch (Exception e) {
			getLogger().error("Unexpected error evaluating SQLexpression",e);
			throw new ConsistencyError("Error evaluating SQLexpression", e);
		//}finally{
		//	sql_expression_visitor.reset();
		}
	}
	private SQLValueVisitor sql_value_visitor=null;
	/** Get a SQLValue corresponding to the expression.
	 * Any supported property can be converted to a SQLValue
	 * 
	 * @param <T>
	 * @param expr
	 * @return SQLValue
	 * @throws InvalidSQLPropertyException
	 */
	@SuppressWarnings("unchecked")
	public <T> SQLValue<T> getSQLValue(PropExpression<T> expr) throws InvalidSQLPropertyException{
		if( expr instanceof PropertyTag){
			PropertyTag<T> tag = (PropertyTag<T>) expr;
			SQLValue<T> a = value_map.get(tag);
			if( a != null ){
				return a;
			}
		}
		//log.debug("Get SQLValue for"+expr.toString());
		if( sql_value_visitor == null ){
			sql_value_visitor= new SQLValueVisitor(getContext());
		}
		try {
			return expr.accept(sql_value_visitor);
		}catch(InvalidSQLPropertyException ee){
			throw ee;
		} catch (Exception e) {
			getLogger().error("Unexpected exception type in getSQLValue",e);
			throw new ConsistencyError("Error evaluating SQLValue "+expr.toString(), e);
		//}finally{
		//	sql_value_visitor.reset();
		}
	}
	
	
	public boolean writable(PropertyTag t){
		Accessor a = accessor_map.get(t);
//		if( a == null ){
//			log.debug("writable returns null accessor for "+t.getFullName());
//		}else{
//			log.debug("writtable "+t.getFullName()+" "+a.toString()+" "+a.getClass().getCanonicalName());
//		}
		return ( a != null && a.canSet());
	}
	/** Get the set of <em>Defined</em> properties.
	 * 
	 * @return Set,PropertyTag>
	 */
	public Set<PropertyTag> getProperties() {

		
			Set<PropertyTag> defined=new LinkedHashSet<PropertyTag>();
			defined.addAll(accessor_map.keySet());
			if( derived != null){
			for(PropertyTag<?> t : derived.keySet()){
				if( hasProperty(t)){
					defined.add(t);
				}
			}
			}
		
		return defined;
	}
	public ExpressionTargetContainer getProxy(X record){
		return new ExpressionTargetProxy(record);
	}
	/** Get the set of defines properties whose resutls may be assigned to the
	 * specified target type
	 * 
	 * @param c Class of target type
	 * @return Set<PropertyTag
	 */
	public Set<PropertyTag> getProperties(Class<?> c){
		LinkedHashSet<PropertyTag> set = new LinkedHashSet<PropertyTag>();
		for(PropertyTag t : getProperties()){
			if( c.isAssignableFrom(t.getTarget())){
				set.add(t);
			}
		}
		return set;
	}
	
	private Map<PropertyTag,Boolean> defined_props_cache=new HashMap<PropertyTag,Boolean>();
	/** Is this a defined property
	 * 
	 * @param tag
	 * @return boolean
	 */
	public <P> boolean hasProperty(PropertyTag<P> tag) {
		Boolean result = defined_props_cache.get(tag);
		if( result != null ){
			return result;
		}
		result = testProperty(tag,true);
		defined_props_cache.put(tag, result);
		return result;
	}
	private <P> Boolean testProperty(PropertyTag<P> tag, boolean check_derived){
		if( accessor_map.containsKey(tag)){
			return Boolean.TRUE;
		}
		if( value_map.containsKey(tag)){
			return Boolean.TRUE;
		}
		if( expression_map.containsKey(tag)){
			return Boolean.TRUE;
		}
		if( check_derived){
			PropExpression<? extends P> expr = derived.get(tag);
			if( expr != null ){
				return resolves(expr,false);
			}
		}
		return Boolean.FALSE;
	}
	public <P> boolean isAccessor(PropertyTag<P> tag){
		return accessor_map.containsKey(tag) && ! value_map.containsKey(tag) && ! expression_map.containsKey(tag);
	}
	/** Does the property resolve as a derived property only.
	 * 
	 * @param tag
	 * @return
	 */
	public <P> boolean isDerived(PropertyTag<P> tag){
		return hasProperty(tag) && ! testProperty(tag, false);
	}
	private ResolveChecker checker=null;
	/** Does this property Expression resolve
	 * 
	 * @param <T>
	 * @param e
	 * @param require_sql 
	 * @return boolean
	 */
	public <T> Boolean resolves(PropExpression<T> e,boolean require_sql){
		if( checker == null || checker.getRequreSQL() != require_sql){
			checker = new ResolveChecker(log,require_sql);
		}
		try {
			return e.accept(checker);
		} catch (Exception e1) {
			//log.debug("AccessorMap.resolves fails "+config_tag,e1);
			return Boolean.FALSE;
		//}finally{
		//	checker.reset();
		}
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
					selector_map.put(field_name, new TimeStampInput(res.getResolution()));
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
					tag = new PropertyTag<String>(orphan_registy, prop_name,String.class);
				} else if (info.isDate() || (info.isNumeric()&&force_date)) {
					tag = new PropertyTag<Date>(orphan_registy, prop_name,Date.class);
				} else if (info.isNumeric()) {

					tag = new PropertyTag<Number>(orphan_registy, prop_name,
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
					put(tag, res.getStringExpression(target,field_name));
				} else if (Date.class.isAssignableFrom(t)) {
					put(tag, res.getDateExpression(target,field_name));
					selector_map.put(field_name, new TimeStampInput(res.getResolution()));
				} else if (Number.class.isAssignableFrom(t)) {
					//Duration is supported at native millisecond resolution.
					put(tag, res.getNumberExpression(target,t,field_name));
					if (Duration.class.isAssignableFrom(t)) {
						// we could support different resolutions using a DurationFieldValue
						//put(tag, new DurationFieldValue(res.getNumberExpression(target,Number.class,field_name),1L));
						selector_map.put(field_name, new DurationInput());
					} 
				} else if (Boolean.class.isAssignableFrom(t)) {
					put(tag, res.getBooleanExpression(target,field_name));
					selector_map.put(field_name,new BooleanInput());
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
							referenceExpression = res.getReferenceExpression(target, field_name);
						}else{

							//log.debug("Reference tag "+ref_tag.getFactoryClass().getCanonicalName()+" "+ref_tag.getTable());
							IndexedTypeProducer prod = new IndexedTypeProducer(field_name, getContext(),ref_tag.getFactoryClass(),ref_tag.getTable());

							res.addTypeProducer(prod);
							referenceExpression= new IndexedFieldValue(target,res,prod);
							
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
				    	put(new PropertyTag<Boolean>(reg, role,Boolean.class), new RelationshipAccessor<X>(fac, tag+"."+role));
				    }
				    reg.lock();
				    finder.addFinder(reg);
				}
			   }catch(Throwable e){
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
						IndexedFieldValue referenceExpression = res.getReferenceExpression(target,field_name);
						if( referenceExpression != null ){
							put(tag, referenceExpression);
							selector_map.put(referenceExpression.getFieldName(), referenceExpression);
						}
					}
				}
		}
//		// Self reference
//		// only really needed when filtering on a specific recordID
//		ReferenceTag tag=(ReferenceTag) reference_registry.find(IndexedReference.class, res.getTag());
//		if( tag != null ){
//			Class clazz = conn.getPropertyClass(DataObjectFactory.class, res.getTag());
//			if( clazz != null){
//				put(tag, new SelfSQLValue(res, clazz));
//			}
//		}
				
	}


	public String getImplemenationInfo(PropertyTag<?> tag) {
		StringBuilder sb = new StringBuilder();
		try {
			// The SQLValue should usually be implemented
			// show this in preference 
			SQLValue<?> s = getSQLValue(tag);
			if( s instanceof SQLExpression){
				sb.append("SQLExpression: ");
			}else{
				sb.append("SQLValue: ");
			}
			sb.append(s.toString());
			
	
		} catch (InvalidSQLPropertyException e) {
			Accessor a = accessor_map.get(tag);
			if( a == null ){
				try{
					ImplementationVisitor vis = new ImplementationVisitor();
					sb.append("evaluate(");
					sb.append(tag.accept(vis));
					sb.append(")");
				}catch(InvalidPropertyException e2){
					return "Not Saved/Defined";
				}catch (Throwable e3) {
					getLogger().error("Error calculating implementation of "+tag.getFullName(),e3);
					sb.append("Error");
				}
			}else{
				sb.append(a.toString());
			}
		} catch (Throwable e) {
			getLogger().error("Error calculating implementation of "+tag.getFullName(),e);
			sb.append("Error");
		}
		// sb.append("}");
		return sb.toString().replace("(", " (").replace("[", "[ "); // make it pretty print
													// better

	}
/** Add definitions from a PropExpressionMap 
 * This will not override explicit accessors
 * 
 * @param conn
 * @param input 
 */
	
	public void addDerived(AppContext conn,PropExpressionMap input) {
//		for(PropertyTag<?> t : input.keySet()){
//			log.debug(config_tag+" add definition "+t.getFullName()+"->"+input.get(t));
//		}
		this.derived.getAllFrom(input);
	}
	public PropExpressionMap getDerivedProperties(){
		return new PropExpressionMap(derived);
	}
	/** clear any PropExpression definitions that cannot be resolved
	 * 
	 */
	public void clearUnresolvableDefinitions(){
		ResolveChecker checker = new ResolveChecker(null,false);
		for(Iterator<PropertyTag> it = derived.keySet().iterator(); it.hasNext();){
			
			PropertyTag<?> t = it.next();
			//log.debug("check definitions resolve "+t.getFullName()+" "+config_tag);
			try {
				if( ! t.accept(checker)){
					//log.debug("Remove definition for "+t.getFullName()+" does not resolve");
					it.remove();
				}
			} catch (Exception e) {
				getLogger().error("Error checking resolve in clearUnresolvableDefinitions",e);
				it.remove();
			}
		}
//		for(PropertyTag<?> t : derived.keySet()){
//			log.debug(config_tag+" post clear "+t.getFullName()+"->"+derived.get(t));
//		}
	}

	/** Return the field name a PropertyTag maps to.
	 * If the property is read-only, composite or unimplemented then
	 * return null.
	 * 
	 * @param t PropertyTag
	 * @return String or null
	 */
	public String getField(PropertyTag t){
		Accessor a = accessor_map.get(t);
		if( a != null && a instanceof FieldValue){
			return ((FieldValue)a).getFieldName();
		}
		return null;
	}
	/** Map a set of PropertyTags to their corresponding database fields.
	 * 
	 * @param input
	 * @return Set<String> of field names
	 */
    public Set<String> getFieldSet(Set<PropertyTag> input){
    	Set<String> result = new HashSet<String>();
    	for(PropertyTag t : input){
    		String name = getField(t);
    		if( name != null ){
    			result.add(name);
    		}
    	}
    	return result;
    }
    /** Extract the entries from a map that are keyed by PropertyTags corresponding
     * to database fields and return a new map re-keyed by the field names.
     * 
     * @param <R>
     * @param input Map<PropertyTag,R>
     * @return Map<String,R> map keyed by field names
     */
    public <R> Map<String,R> getFieldMap(Map<PropertyTag,R> input){
    	Map<String,R> result = new HashMap<String,R>();
    	for(PropertyTag t : input.keySet()){
    		String name = getField(t);
    		if( name != null ){
    			result.put(name, input.get(t));
    		}
    	}
    	return result;
    }

    /** Try to read an update {@link Form} (keyed by field names)
     * into a property container.
     * @param map {@link PropertyContainer} to modify
     * @param f Form to read
     * @return {@link PropertyContainer}
     * @throws InvalidPropertyException 
     */
    @SuppressWarnings("unchecked")
	public PropertyContainer addFormContents(PropertyContainer map,Form f) throws InvalidPropertyException{
    	for(PropertyTag tag : accessor_map.keySet()){
    		String field = getField(tag);
    		if( field != null){
    			Object o = f.get(field);
    			if( o != null ){
    				if( tag.allow(o)){
    					map.setProperty(tag, o);
    				}else{
    					o = f.getItem(field);
    					if( tag.allow(o)){
    						map.setProperty(tag, o);
    					}else if( tag instanceof IndexedTag && o instanceof Indexed ){
    						IndexedTag itag = (IndexedTag) tag;
    						Indexed i = (Indexed) o;
    						map.setProperty(itag, itag.makeReference(i));
    					}
    				}
    			}
    		}
    	}
    	return map;
    }

    
    @SuppressWarnings("unchecked")
	public final<I> BaseFilter<X> getFilter(PropExpression<I> expr,
			MatchCondition match, I data) throws CannotFilterException {
    	if( expr == null ){
    		throw new CannotFilterException("Cannot filter on null expression");
    	}
    	// first choice is to use an SQLExpression
    	try{
			SQLExpression<I> exp = getSQLExpression(expr);
			if(exp instanceof FilterProvider){
				try {
					return ((FilterProvider<X,I>) exp).getFilter(match, data);
				} catch (NoSQLFilterException e) {
					// default to simple SQLExpressionFilter
				}
			}
			return new SQLExpressionFilter<X, I>(target,exp, match,data);
		}catch(Exception e){
			//keep looking
		}
    	try{
		// Next an SQLValue that provides its own filter.
		try{
			SQLValue<I> val = getSQLValue(expr);
			if( val instanceof FilterProvider){
				return ((FilterProvider<X,I>) val).getFilter(match, data);
			}
		
		}catch(InvalidSQLPropertyException e){
			//keep looking
		}
		// finally an accessor that provides its own filter
    	if( expr instanceof PropertyTag){
    		PropertyTag<I> tag = (PropertyTag<I>) expr;
    		Accessor<I,X> m = accessor_map.get(tag);
    		if( m == null ){
    			// follow derived property chain in case this leads to
    			// an accessor that defines a filter. We don't make composite accessors as that
    			// is much more complex than just evaluating the expressions.
    			while( tag != null && m == null){
    				PropExpression<? extends I> tmp = derived.get(tag);
    				if( tmp != null && tmp instanceof PropertyTag){
    					if( tmp.equals(expr)){
    						// circular definition loop
    						break;
    					}
    					// follow the chain
    					tag = (PropertyTag<I>) tmp;
    					m = accessor_map.get(tag);
    				}else{
    					tag = null;
    				}
    			}
    		}
    		if( m != null ){
    			if( m instanceof FilterProvider){
    				return ((FilterProvider<X,I>)m).getFilter(match, data);
    			}
    		}
    	}
    	}catch( NoSQLFilterException e){
    		// Try an accept filter below
    	}
    	
    	if( resolves(expr,false)){
    		return new ExpressionAcceptFilter<X, I>(target,expr, match, data);
    	}
    	// we might still have a derived property but cannot make a SQLfilter
		
		throw new CannotFilterException("Cannot filter on expression "
				+ expr);
	}
  
	public <R> BaseFilter<X> getRelationFilter(PropExpression<R> left,
			MatchCondition match, PropExpression<R> right)
			throws CannotFilterException {
		if( left == null || right == null ){
			throw new CannotFilterException("Cannot filter on null expression");
		}
		try {
			SQLExpression<R> exp1 = getSQLExpression(left);
			SQLExpression<R> exp2 = getSQLExpression(right);
			return new SQLExpressionMatchFilter<X,R>(target,exp1,match,exp2);
		} catch (InvalidSQLPropertyException e) {
			
		}
		return new ExpressionAcceptMatchFilter<X,R>(target,left, match,right);
	}
    @SuppressWarnings("unchecked")
	public final <I> BaseFilter<X> getNullFilter(PropExpression<I> expr,
			boolean is_null) throws CannotFilterException {
    	if( expr == null ){
    		throw new CannotFilterException("Cannot filter on null expression");
    	}

    	// fitst choice a SQLExpression
    	try{
    		SQLExpression<I> exp = getSQLExpression(expr);
    		if( exp instanceof FilterProvider){
    			try {
    				return ((FilterProvider<X, I>)exp).getNullFilter(is_null);
    			} catch (NoSQLFilterException e) {
    				// Go with default SQLExpressionNullFilter
    			}
    		}
    		return new SQLExpressionNullFilter<X, I>(target,exp, is_null);
    	}catch(InvalidSQLPropertyException e){
    		// keep looking
    	}
    	try{
    		// next try an sqlvalue
    		try{
    			SQLValue<I> val = getSQLValue(expr);
    			if( val instanceof FilterProvider){
    				
    				return ((FilterProvider<X,I>) val).getNullFilter(is_null);
    				
    			}

    		}catch(InvalidSQLPropertyException e){
    			// keep looking
    		}
    		if( expr instanceof PropertyTag){
    			Accessor<I,X> m = accessor_map.get((PropertyTag<I>) expr);
    			if( m != null && m instanceof FilterProvider){
    				return ((FilterProvider<X,I>)m).getNullFilter(is_null);
    			}
    		}
    	}catch(NoSQLFilterException e){
    		// try an accept filter below
    	}
    	// could still have a derived property using accessors but cannot make these into
    	// an SQLFilter
    	if(resolves(expr,false)){
    		return new ExpressionAcceptNullFilter<X, I>(target,expr, is_null);
    	}


    	throw new CannotFilterException("Cannot filter on expression "
    			+ expr+" for "+config_tag);
    }
    /** Generate default set of form selectors
     * 
     * @return Map of seelctors
     */
    public Map<String,Object> getSelectors(){
    	return selector_map;
    }
	public AppContext getContext() {
		return res.getContext();
	}
	private Logger getLogger() {
		if( log == null ){
			log =getContext().getService(LoggerService.class).getLogger(getClass());
		}
		return log;
	}
	
	/** Generate an overlap filter
	 * 
	 * @param period   Period to overlap
	 * @param start_prop start property may be null
	 * @param end_prop   end property
	 * @param type 	  {@link OverlapType}
	 * @param cutoff	maximum record length for cutoff calculation, zero for unknown
	 * @return BaseFilter
	 * @throws CannotFilterException
	 */
	public BaseFilter<X> getPeriodFilter(Period period,
			PropExpression<Date> start_prop,
			PropExpression<Date> end_prop, OverlapType type,long cutoff)
			throws CannotFilterException {
		AndFilter<X> res = new AndFilter<X>(getTarget());
		assert(end_prop != null);
		boolean use_simple = start_prop == null || start_prop.equals(end_prop);
		
		if( ! use_simple){
			res.addFilter(getRelationFilter(start_prop, MatchCondition.LT, end_prop));
		}
		Date start = period.getStart();
		Date end = period.getEnd();
		// we can have start and end the same if looking for a point overlap
		assert(! start.after(end));
		if( use_simple ){
			assert(start.before(end));
    		// simple selector
    		res.addFilter(getFilter(end_prop,MatchCondition.GT,period.getStart()));
    		res.addFilter(getFilter(end_prop,MatchCondition.LE,period.getEnd()));
    		//res.addFilter(getFilter(start_prop, MatchCondition.GT, new Date(0L)));
    		return res;
    	}else{
    		// start lower bound
    		if( type == OverlapType.LOWER || type == OverlapType.OUTER){
    			res.addFilter(getFilter(start_prop, MatchCondition.LT, start));
    		}else{
    			res.addFilter(getFilter(start_prop, MatchCondition.LT, end));
    		}
    		// start upper bound
    		if( type == OverlapType.INNER || type == OverlapType.UPPER){
    			res.addFilter(getFilter(start_prop, MatchCondition.GE, start));
    		}else{
    			if( cutoff == 0L ){
    				res.addFilter(getFilter(start_prop, MatchCondition.GT, new Date(0L)));
    			}else if(cutoff > 0L){
    				res.addFilter(getFilter(start_prop, MatchCondition.GE, new Date(start.getTime()-cutoff)));
    			}
    		}
    		// end upper bound
    		if( type == OverlapType.LOWER || type == OverlapType.INNER){
    			res.addFilter(getFilter(end_prop, MatchCondition.LE, end));
    		}else{
    			if( cutoff > 0L){
    				res.addFilter(getFilter(end_prop, MatchCondition.LE, new Date(end.getTime()+cutoff)));
    			}
    		}
    		
    		// end lower bound
    		if( type == OverlapType.UPPER_OUTER || type == OverlapType.UPPER  || type == OverlapType.OUTER){
    			res.addFilter(getFilter(end_prop, MatchCondition.GT, end));
    		}else{
    			res.addFilter(getFilter(end_prop, MatchCondition.GT, start));
    		}
    		
    	}
		return res;
	}
	public <I> OrderFilter<X> getOrderFilter(boolean descending, PropExpression<I> expr)
			throws CannotFilterException {
		try {
			SQLExpression<I> sqlExpression = getSQLExpression(expr);
			if(sqlExpression == null ){
				return null;
			}
			return new SQLExpressionOrderFilter<I, X>(target,descending,sqlExpression);
		} catch (InvalidSQLPropertyException e) {
			throw new CannotFilterException(e);
		}
	}
	
	protected void addSource(StringBuilder sb) {
		res.addTable(sb, true);
		
	}
	
	protected String getDBTag() {
		return res.getDBTag();
	}
	protected <T> CaseExpression<X,T> getCaseExpression(CasePropExpression<T> expr) throws NoSQLFilterException, InvalidSQLPropertyException, Exception{
		LinkedList<CaseExpression.Clause<X,T>> list = new LinkedList<CaseExpression.Clause<X,T>>();
		FilterSelectVisitor<X> vis = new FilterSelectVisitor<X>(this);
		for( CasePropExpression.Case<T> c : expr.getCases()){
			list.add( new CaseExpression.Clause<X,T>(FilterConverter.convert(c.sel.visit(vis)), getSQLExpression(c.expr)));
		}
		PropExpression<? extends T> def = expr.getDefaultExpression();
		SQLExpression<? extends T> def_sql = null;
		if( def != null ){
			def_sql=getSQLExpression(def);
		}
		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>) expr.getTarget();
		return new CaseExpression<X,T>(clazz, def_sql, list);
	}
	public Class<? super X> getTarget() {
		return target;
	}
	
	public void release(){
		if( derived != null ){
			derived.clear();
			derived=null;
		}
		if( accessor_map != null ){
			accessor_map.clear();
			accessor_map=null;
		}
		if(value_map != null ){
			value_map.clear();
			value_map=null;
		}
		if(expression_map != null ){
			expression_map.clear();
			expression_map=null;
		}
	}
}