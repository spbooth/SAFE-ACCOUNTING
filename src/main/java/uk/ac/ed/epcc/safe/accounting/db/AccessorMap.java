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
import uk.ac.ed.epcc.safe.accounting.PropertyImplementationProvider;
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
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidSQLPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.MethodPropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.SetPropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.reference.IndexedTag;
import uk.ac.ed.epcc.safe.accounting.selector.OverlapType;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AbstractContexed;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Feature;
import uk.ac.ed.epcc.webapp.Indexed;
import uk.ac.ed.epcc.webapp.Targetted;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.jdbc.expr.Accessor;
import uk.ac.ed.epcc.webapp.jdbc.expr.CannotFilterException;
import uk.ac.ed.epcc.webapp.jdbc.expr.CaseExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.ConstExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.FilterProvider;
import uk.ac.ed.epcc.webapp.jdbc.expr.IndexedSQLValue;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLExpression;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLExpressionFilter;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLExpressionMatchFilter;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLExpressionNullFilter;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLExpressionOrderFilter;
import uk.ac.ed.epcc.webapp.jdbc.expr.SQLValue;
import uk.ac.ed.epcc.webapp.jdbc.expr.WrappedSQLExpression;
import uk.ac.ed.epcc.webapp.jdbc.filter.AndFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.FilterConverter;
import uk.ac.ed.epcc.webapp.jdbc.filter.GenericBinaryFilter;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.jdbc.filter.NoSQLFilterException;
import uk.ac.ed.epcc.webapp.jdbc.filter.SQLFilter;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.FieldValue;
import uk.ac.ed.epcc.webapp.model.data.FieldValueFilter;
import uk.ac.ed.epcc.webapp.model.data.Repository;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.time.Period;

/** Common {@link PropExpression} logic that can be incorporated (by composition) into factory classes for {@link DataObject}s to support {@link PropExpression}s
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
 * @param <X> type of {@link ExpressionTarget}
 *
 * 
 */


public abstract class AccessorMap<X> extends AbstractContexed implements ExpressionFilterTarget<X>, Targetted<X>, PropertyImplementationProvider{
	public static final Feature EVALUATE_CACHE_FEATURE = new Feature("evaluate.cache",true,"cache expression evaluations in ExpressionTargets");
	public static final Feature FORCE_SQLVALUE_FEATURE = new Feature("accounting.force_sqlvalue",false,"Use SQLValues in preference to SQLExpressions");
	public static final Feature PREFER_NESTED_EXPR_FEATURE = new Feature("accouning.prefer_nested_sqlexpressions",true,"Prefer the use of SQLExpressions as inner parts of SQLValues");
	protected final Class<X> target;
	
	protected final String config_tag;
	protected static final String CONFIG_PREFIX = "accounting.";
	
	
	// derived prop expressions
	private PropExpressionMap derived;
	// accessors for the leaf properties.
	private Map<PropertyTag, Accessor> accessor_map = new HashMap<>();
    // SQLValue for leaf values
	private Map<PropertyTag, SQLValue> value_map = new HashMap<>();
	// SQL expressions for leaf values.
	private Map<PropertyTag, SQLExpression> expression_map = new HashMap<>();

	
	
	
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
		public ExpressionTargetProxy(AppContext conn,X r,boolean use_cache){
			super(conn);
			this.record=r;
			missing=new HashSet<>();
			this.use_cache=use_cache;
		}
		public ExpressionTargetProxy(AppContext conn,X r){
			this(conn,r,EVALUATE_CACHE_FEATURE.isEnabled(conn));
			assert(r != null);
			assert(conn != null);
		}
		@SuppressWarnings("unchecked")
		public <R> R evaluateExpression(PropExpression<R> expr) throws InvalidExpressionException{
			if( use_cache){
			if( cache == null ){
				cache=new HashMap<>();
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
				String str_expr = expr.toString();
				getLogger().error("Unexpected exception evaluating expression "+str_expr,e);
				
				throw new InvalidExpressionException("Error evaluating expression "+str_expr, e);
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
				match_visitor=new MatchSelectVisitor<>(this);
			}
		
			return sel.visit(match_visitor).booleanValue();
			
		}
		@Override
		public void release() {
			missing.clear();
			flush();
			cache=null;
			match_visitor=null;
			eject(this);
		}
		@Override
		public boolean commit() throws DataFault {
			if( record instanceof DataObject) {
				return ((DataObject)record).commit();
			}
			throw new ConsistencyError("Can only commit DataObjects");
		}
		@Override
		public boolean delete() throws DataFault {
			if( record instanceof DataObject) {
				DataObject dataObject = (DataObject)record;
				release();
				return dataObject.delete();
			}
			throw new ConsistencyError("Can only delete DataObjects");
		}
		public X getRecord() {
			return record;
		}
		@Override
		public Object visitMethodPropExpression(MethodPropExpression<?> method) throws Exception {
			return method.evaluate(this);
		}
	}
	public class SQLExpressionVisitor extends CreateSQLExpressionPropExpressionVisitor{
		public SQLExpressionVisitor(AppContext c) {
			super(target,c);
		}
		private Set<PropertyTag> missing = new HashSet<>();
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
		
		public <T> SQLExpression visitCasePropExpression(
				CasePropExpression<T> expr) throws Exception {
			return getCaseExpression(expr);
		}
		@Override
		public <T> SQLValue<T> getSQLValue(PropExpression<T> expr) throws InvalidSQLPropertyException {
			
			return AccessorMap.this.getSQLValue(expr);
		}
		@Override
		public SQLExpression visitMethodPropExpression(MethodPropExpression<?> method) throws Exception {
			throw new InvalidSQLPropertyException(config_tag, method);
		}
		
	}
	public class SQLValueVisitor extends CreateSQLValuePropExpressionVisitor{
		@Override
		protected <T> SQLValue<T> process(PropExpression<T> e) throws Exception {
			if( exp != null) {
				try {
					return e.accept(exp);
				}catch(InvalidSQLPropertyException e1) {
					
				}catch(Exception e1) {
					getLogger().error("Error attempting SQLExpression", e1);
				}
			}
			return super.process(e);
		}
		private final SQLExpressionVisitor exp;
		public SQLValueVisitor(AppContext c,SQLExpressionVisitor exp) {
			super(target,c);
			this.exp=exp;
		}
		private Set<PropertyTag> missing = new HashSet<>();
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
		
		public <T> SQLValue visitCasePropExpression(CasePropExpression<T> expr)
				throws Exception {
			return getCaseExpression(expr);
		}
		@Override
		public SQLValue visitMethodPropExpression(MethodPropExpression<?> method) throws Exception {
			throw new InvalidSQLPropertyException(method);
		}
		
	}
	public class ResolveChecker extends ResolveCheckVisitor{
		private final Set<PropertyTag> missing = new HashSet<>();
		private final boolean require_sql;
		public ResolveChecker(AppContext conn,Logger log,boolean require_sql) {
			super(conn,log);
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
		@Override
		public Boolean visitMethodPropExpression(MethodPropExpression<?> method) throws Exception {
			for(PropertyTag t : method.required()) {
				if( ! ((Boolean)t.accept(this))) {
					return false;
				}
			}
			return true;
		}
	}
	public class ImplementationVisitor extends ImplementationPropExpressionVisitor{
		private Set<PropertyTag> missing = new HashSet<>();
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
	public AccessorMap(AppContext conn,Class<X> target,String config_tag) {
		super(conn);
		this.target=target;
		this.config_tag=config_tag;
		derived = new PropExpressionMap();
		
		// might be better to do this as an expression
		// the following only works because we are adding a SQLAccessor
		put(StandardProperties.TABLE_PROP, new ConstExpression<String,X>(target,String.class, config_tag));
		
		try {
			// This is only a fall-back expression. Aggregate records may store the
			// job count as a DB field so we can't hardwire this as an accessor
			derived.put(StandardProperties.COUNT_PROP,  new ConstPropExpression<>(Long.class, Long.valueOf(1L)));
		} catch (PropertyCastException e) {
			getLogger().error("Error adding count expression",e);
		}
	}
	public final <T> void put(PropertyTag<? extends T> tag, Targetted<T> obj){
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
			if( obj instanceof WrappedSQLExpression) {
				// wrapped SQL value is preferred in contexts where value is ok
				value_map.put(tag, ((WrappedSQLExpression<T>)obj).getSQLValue());
			}
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
	public final boolean possibleCompatible(Class a, Class b) {
		return a.isInterface() || b.isInterface() || a.isAssignableFrom(b)
				|| b.isAssignableFrom(a);
	}

	

	

	private SQLExpressionVisitor sql_expression_visitor = null;
	
	private SQLExpressionVisitor getSQLExpressionVisitor() {
		if( sql_expression_visitor == null ){
			sql_expression_visitor= new SQLExpressionVisitor(getContext());
		}
		return sql_expression_visitor;
	}
	@SuppressWarnings("unchecked")
	public final <T> SQLExpression<T> getSQLExpression(PropExpression<T> expr) throws InvalidSQLPropertyException {
	
		if( expr instanceof PropertyTag){
			PropertyTag<T> tag = (PropertyTag<T>) expr;
			SQLExpression<T> a = expression_map.get(tag);
			if( a != null ){
				return a;
			}
		}
		if( FORCE_SQLVALUE_FEATURE.isEnabled(getContext())) {
			throw new InvalidSQLPropertyException(expr);
		}
		
		try {
			return expr.accept(getSQLExpressionVisitor());
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
	public final <T> SQLValue<T> getSQLValue(PropExpression<T> expr) throws InvalidSQLPropertyException{
		if( expr instanceof PropertyTag){
			PropertyTag<T> tag = (PropertyTag<T>) expr;
			SQLValue<T> a = value_map.get(tag);
			if( a != null ){
				return a;
			}
		}
		//log.debug("Get SQLValue for"+expr.toString());
		if( sql_value_visitor == null ){
			boolean use_expr = PREFER_NESTED_EXPR_FEATURE.isEnabled(getContext()) && ! FORCE_SQLVALUE_FEATURE.isEnabled(getContext());
			sql_value_visitor= new SQLValueVisitor(getContext(),use_expr? getSQLExpressionVisitor(): null);
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
	
	
	public final boolean writable(PropertyTag t){
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
	public final Set<PropertyTag> getProperties() {

		
			Set<PropertyTag> defined=new LinkedHashSet<>();
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

	private X last_record=null;
	private ExpressionTargetProxy last_proxy=null;
	/**  create/find an {@link ExpressionTargetProxy} for a record.
	 * 
	 * This method must be used when creating a proxy cached within the record itself
	 * so the record can implement {@link ExpressionTargetContainer} otherwise you should probably be using
	 * {@link #getContainer(Object)}
	 * 
	 * @see ProxyOwner
	 * @param record
	 * @return
	 */
	final ExpressionTargetProxy getProxy(X record){
		if( record == null ) {
			return null;
		}
		// We implement very basic proxy caching for records that don't implement ProxyOwner
		// this ensures the 
		if( last_record == record) {
			return last_proxy;
		}
		last_record=record;
		last_proxy =  new ExpressionTargetProxy(getContext(),record);
		return last_proxy;
	}
	/** Map a record to an {@link ExpressionTargetContainer}
	 * 
	 * if the record itself implements {@link ExpressionTargetContainer} then return the record.
	 * if it implements {@link ProxyOwner} return the cached {@link ExpressionTargetContainer}
	 * Otherwise return a new {@link ExpressionTargetProxy}.
	 * 
	 * This method must not be used as part of the implementation of the interface on the record itself
	 * use {@link #getProxy(Object)}
	 * @param record
	 * @return
	 */
	public final ExpressionTargetContainer getContainer(X record) {
		if( record instanceof ExpressionTargetContainer) {
			return (ExpressionTargetContainer) record;
		}else if( record instanceof ProxyOwner) {
			ExpressionTargetContainer proxy = ((ProxyOwner)record).getProxy();
			if( proxy != null) {
				// A ProxyOwner SHOULD use getProxy to populate its cached value
				// but it CAN use getContainer provided it returns null if
				// called recursively.
				return proxy;
			}
		}
		return getProxy(record);
	}
	
	
	/** Eject a cached proxy from the cache.
	 * passing null ejects all cached proxies
	 * 
	 * @param proxy 
	 */
	public void eject(ExpressionTargetProxy proxy) {
		if(proxy == last_proxy || proxy == null) {
			last_proxy=null;
			last_record=null;
		}
	}
	
	/** Get the set of defines properties whose resutls may be assigned to the
	 * specified target type
	 * 
	 * @param c Class of target type
	 * @return Set<PropertyTag
	 */
	public final Set<PropertyTag> getProperties(Class<?> c){
		LinkedHashSet<PropertyTag> set = new LinkedHashSet<>();
		for(PropertyTag t : getProperties()){
			if( c.isAssignableFrom(t.getTarget())){
				set.add(t);
			}
		}
		return set;
	}
	
	private Map<PropertyTag,Boolean> defined_props_cache=new HashMap<>();
	/** Is this a defined property
	 * 
	 * @param tag
	 * @return boolean
	 */
	public final <P> boolean hasProperty(PropertyTag<P> tag) {
		Boolean result = defined_props_cache.get(tag);
		if( result != null ){
			return result;
		}
		result = testProperty(tag,true);
		defined_props_cache.put(tag, result);
		return result;
	}
	protected final <P> Boolean testProperty(PropertyTag<P> tag, boolean check_derived){
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
	public final <P> boolean isAccessor(PropertyTag<P> tag){
		return accessor_map.containsKey(tag) && ! value_map.containsKey(tag) && ! expression_map.containsKey(tag);
	}
	/** Does the property resolve as a derived property only.
	 * 
	 * @param tag
	 * @return
	 */
	public final <P> boolean isDerived(PropertyTag<P> tag){
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
	public final  <T> Boolean resolves(PropExpression<T> e,boolean require_sql){
		if( checker == null || checker.getRequreSQL() != require_sql){
			checker = new ResolveChecker(getContext(),getLogger(),require_sql);
		}
		try {
			return e.accept(checker);
		} catch (Exception e1) {
			getLogger().error("AccessorMap.resolves fails "+config_tag,e1);
			return Boolean.FALSE;
		//}finally{
		//	checker.reset();
		}
	}


	public final <T> boolean resolves(PropExpression<T> expr) {
		return resolves(expr,false);
	}


	public final String getImplemenationInfo(PropertyTag<?> tag) {
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
				}catch (Exception e3) {
					getLogger().error("Error calculating implementation of "+tag.getFullName(),e3);
					sb.append("Error");
				}
			}else{
				sb.append(a.toString());
			}
		} catch (Exception e) {
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
	
	public final void addDerived(AppContext conn,PropExpressionMap input) {
//		for(PropertyTag<?> t : input.keySet()){
//			log.debug(config_tag+" add definition "+t.getFullName()+"->"+input.get(t));
//		}
		this.derived.getAllFrom(input);
	}
	public final PropExpressionMap getDerivedProperties(){
		return new PropExpressionMap(derived);
	}
	/** clear any PropExpression definitions that cannot be resolved
	 * 
	 */
	public final void clearUnresolvableDefinitions(){
		ResolveChecker checker = new ResolveChecker(getContext(),null,false);
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
	public final String getField(PropertyTag t){
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
    public final Set<String> getFieldSet(Set<PropertyTag> input){
    	Set<String> result = new HashSet<>();
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
    public final <R> Map<String,R> getFieldMap(Map<PropertyTag,R> input){
    	Map<String,R> result = new HashMap<>();
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
	public final PropertyContainer addFormContents(PropertyContainer map,Form f) throws InvalidPropertyException{
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
			return SQLExpressionFilter.getFilter(target,exp, match,data);
			
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
		    if( match == null  && val instanceof FieldValue) {
		    	// FieldValues normally implement FilterProvider
		    	// but for an exact match this should work
		    	// probably needed for TypeProducerFieldValue
		    	return new FieldValueFilter<I, X>(target, (FieldValue<I, X>) val, data);
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
    		return new ExpressionAcceptFilter<>(target,this,expr, match, data);
    	}
    	// we might still have a derived property but cannot make a SQLfilter
		
		throw new CannotFilterException("Cannot filter on expression "
				+ expr);
	}
  
	public final <R> BaseFilter<X> getRelationFilter(PropExpression<R> left,
			MatchCondition match, PropExpression<R> right)
			throws CannotFilterException {
		if( left == null || right == null ){
			throw new CannotFilterException("Cannot filter on null expression");
		}
		if( left instanceof ConstPropExpression && right instanceof ConstPropExpression){
			// Do constant evaluation in properties as we may not be able to make a SQLExpression for them
			ConstPropExpression<R> left_const = (ConstPropExpression<R>)left;
			ConstPropExpression<R> right_const = (ConstPropExpression<R>)right;
			if( match == null ){
				return new GenericBinaryFilter<>(target, left_const.val.equals(right_const.val));
			}
			return new GenericBinaryFilter<>(target,match.compare(left_const.val,right_const.val));
		}
		try {
			SQLExpression<R> exp1 = getSQLExpression(left);
			SQLExpression<R> exp2 = getSQLExpression(right);
			return SQLExpressionMatchFilter.getFilter(target, exp1,match, exp2);
			
		} catch (InvalidSQLPropertyException e) {
			
			
		}
		// Try matching references
		try{
			SQLValue<R> val1 = getSQLValue(left);
			SQLValue<R> val2 = getSQLValue(right);
			if( val1 != null && val2 != null && val1 instanceof IndexedSQLValue && val2 instanceof IndexedSQLValue){
				IndexedSQLValue ival1 = (IndexedSQLValue) val1;
				IndexedSQLValue ival2 = (IndexedSQLValue) val2;
				if( ival1.getFactory().equals(ival2.getFactory())){
					return SQLExpressionMatchFilter.getFilter(target, ival1.getIDExpression(),match, ival2.getIDExpression());
				}else{
					throw new CannotFilterException("Incompatible reference expressions");
				}
			}
		} catch (Exception e) {
		
		}
		return new ExpressionAcceptMatchFilter<>(target,this,left, match,right);
	}
    @SuppressWarnings("unchecked")
	public final <I> BaseFilter<X> getNullFilter(PropExpression<I> expr,
			boolean is_null)  throws CannotFilterException {
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
    		return SQLExpressionNullFilter.getFilter(target,exp, is_null);
    		
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
    		return new ExpressionAcceptNullFilter<>(target,this,expr, is_null);
    	}


    	throw new CannotFilterException("Cannot filter on expression "
    			+ expr+" for "+config_tag);
    }
    
		
	/** Generate an overlap filter
	 * 
	 * Normally this will exclude records with a zero start time. These can be included
	 * by passing a cutoff of -1.
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
		AndFilter<X> res = new AndFilter<>(getTarget());
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
    				// cutoff of -1 to allow zero start date.
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
	public final <I> SQLFilter<X> getOrderFilter(boolean descending, PropExpression<I> expr)
			throws CannotFilterException {
		try {
			SQLExpression<I> sqlExpression = getSQLExpression(expr);
			if(sqlExpression == null ){
				return null;
			}
			return SQLExpressionOrderFilter.getFilter(target,descending,sqlExpression);
		} catch (InvalidSQLPropertyException e) {
			throw new CannotFilterException(e);
		}
	}
	public  BaseFilter<X> getFilter(RecordSelector selector) throws CannotFilterException {
		if( selector == null ){
			return null;
		}
		try {
			return selector.visit(new FilterSelectVisitor<>(this));
		}catch(CannotFilterException e){
			throw e;
		} catch (Exception e) {
			throw new CannotFilterException(e);
		}
	}
	protected abstract void addSource(StringBuilder sb) ;
	
	protected abstract String getDBTag() ;
	
	protected abstract Set<Repository> getSourceTables();
	
	protected final <T> CaseExpression<X,T> getCaseExpression(CasePropExpression<T> expr) throws NoSQLFilterException, InvalidSQLPropertyException, Exception{
		LinkedList<CaseExpression.Clause<X,T>> list = new LinkedList<>();
		FilterSelectVisitor<X> vis = new FilterSelectVisitor<>(this);
		for( CasePropExpression.Case<T> c : expr.getCases()){
			list.add( new CaseExpression.Clause<X,T>(FilterConverter.convert(c.sel.visit(vis)), getSQLExpression(c.expr)));
		}
		PropExpression<? extends T> def = expr.getDefaultExpression();
		SQLExpression<? extends T> def_sql = null;
		if( def != null ){
			def_sql=getSQLExpression(def);
		}
		Class<T> clazz = (Class<T>) expr.getTarget();
		return new CaseExpression<>(clazz, def_sql, list);
	}
	public final Class<X> getTarget() {
		return target;
	}
	
	public final void release(){
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
		eject(null);
	}
}