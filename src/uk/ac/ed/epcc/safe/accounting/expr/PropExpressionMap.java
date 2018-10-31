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
package uk.ac.ed.epcc.safe.accounting.expr;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.properties.FixedPropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.UnresolvedNameException;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceExpression;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceTag;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.config.ConfigService;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;

/** Class that holds a set of PropertyTag definitions in terms of PropExpressions.
 * This is essentially a wrapper around a Map object with additional run-time type checking.
 * 
 * @author spb
 *
 */


public final class PropExpressionMap {
  private static final String PROPERTY_PREFIX = "properties.";
  private final Map<PropertyTag,PropExpression> map; 
  private static final PropExpressionNormaliser norm = new PropExpressionNormaliser();
  public PropExpressionMap(){
	 map  = new LinkedHashMap<>();
  }
  public PropExpressionMap(PropExpressionMap orig){
	  map = new LinkedHashMap<>(orig.map);
  }
  @SuppressWarnings("unchecked")
public <T> void put(PropertyTag<T> key, PropExpression<? extends T> expr) throws PropertyCastException{
	  try {
		expr=expr.accept(norm);
	} catch (Exception e) {
		
	}
	  if( key.allowExpression(expr)){
		  map.put(key,expr);
	  }else{
		  // do casts
		  if( expr.getTarget()==Date.class && key.allowClass(Long.class)){
			  map.put(key,new MilliSecondDatePropExpression((PropExpression<Date>) expr));
			  return;
		  }
		  if( key.allowClass(String.class)){
			  map.put(key, new StringPropExpression(expr));
			  return;
		  }
		  if( key.allowClass(Integer.class)){
			  map.put(key,new IntPropExpression(expr));
			  return;
		  }
		  if( key.allowClass(Long.class)){
			  map.put(key,new LongCastPropExpression(expr));
			  return;
		  }
		  throw new PropertyCastException("Incompatible target types for PropertyTag and PropExpression "+key.getFullName()+"("+key.getTarget().getCanonicalName()+") "+expr+"("+expr.getTarget().getCanonicalName()+")");
	  }
  }
  /** alias two property tags to each other.
   * Its good practice to peer equivalent tags as otherwise care needs to be taken
   * to ensure the primary tag resolves ahead of the secondary.
   * @param a
   * @param b
 * @throws PropertyCastException 
   */
  public <T> void peer(PropertyTag<T> a, PropertyTag<T> b) throws PropertyCastException{
	  put(a,b);
	  put(b,a);
  }
  
  public <T> void remove(PropertyTag<T> key){
	  map.remove(key);
  }
  
  @SuppressWarnings("unchecked")
  public <T> PropExpression<? extends T> get(PropertyTag<T> key){
	  return map.get(key);
  }
  
  public PropExpressionMap getAllFrom(PropExpressionMap m){
	  if( m != null){
		  for(PropertyTag<?> t : m.keySet()){
			  map.put(t, m.get(t));
		  }
	  }
	  return this;
  }

public Set<PropertyTag> keySet() {
	
	return map.keySet();
}

public boolean containsKey(PropertyTag t) {
	return map.containsKey(t);
}

public int size() {
	return map.size();
}

/** Populate the PropExpressionMap from configuration properties creating properties in a registry.
 * 
 * This is under the control of java properties of the form
 * <b>
 * properties.<em>table-name</em>.name=<em>prop-expression</em>
 * </b>
 * 
 * 
 * 
 * @param reg PropertyRegistry to add definitions to 
 * @param prev PropertyFinder to use in parse
 * @param ctx  AppContext
 * @param table table tag to identify property definitions
 */
  public void addFromProperties(PropertyRegistry reg,PropertyFinder prev, AppContext ctx, String table ){
	  String prefix=PROPERTY_PREFIX+table+".";
	  // allow expressions to depend on other derived properties as well.
	  MultiFinder parse_finder=new MultiFinder();
	  Set<String> missing = new HashSet<>(); // tags already not found.
	  parse_finder.addFinder(reg);
	  parse_finder.addFinder(prev);
		Parser parser = new Parser(ctx,parse_finder);
		Map<String,String> derived_properties = ctx.getInitParameters(prefix);
		for(String key : derived_properties.keySet()){
			try {
				resolve(reg, ctx, prefix, missing, parser, derived_properties, key);
			} catch (Exception e) {
				getLogger(ctx).error("Cannot resolve property for: "+key.toString()+"="+derived_properties.get(key),e);
			}
		}
  }
@SuppressWarnings("unchecked")
private void resolve(PropertyRegistry reg, AppContext ctx, String prefix,
		Set<String> missing, Parser parser,
		Map<String, String> derived_properties, String key) throws UnresolvedNameException {
	String name=key.substring(prefix.length());
	String def = derived_properties.get(key);
	if( def !=  null && def.trim().length() > 0){
		try{
			missing.add(name);
			PropExpression e = null;
			do{
				try{
					e = parser.parse(def);
				}catch(UnresolvedNameException une){
					if( missing.contains(une.getUnresolvedName())){
						// no circular dependencies
						throw une;
					}
					// try to make dependencies by recursion
					resolve(reg, ctx, prefix, missing, parser, derived_properties, prefix+une.getUnresolvedName());
				}
			}while( e == null);
			if( reg.find(name) == null){
				// ignore if already added by recursion
				addExpression(reg, name, e);
			}
			missing.remove(name);
		}catch(ParseException e){
			getLogger(ctx).error("Error making derived property "+name+" from "+def,e);
		} catch (PropertyCastException e) {
			getLogger(ctx).error("Type of expression and PropertyTag don't match",e);
		}
	}
}

/** Add aliases to existing properties.
 * 
 * Note i
 * 
 * @param prev
 * @param ctx
 * @param table
 */
@SuppressWarnings("unchecked")
public void addFromProperties(PropertyFinder prev, AppContext ctx, String table ){
	  String prefix=PROPERTY_PREFIX+table+".";
		Parser parser = new Parser(ctx,prev);
		Map<String,String> derived_properties = ctx.getInitParameters(prefix);
		for(String key : derived_properties.keySet()){
			String name=key.substring(prefix.length());
			String def = derived_properties.get(key);
			if( def !=  null && def.trim().length() > 0){
				try{
					PropertyTag tag = prev.find( name);
					if( tag == null ){
						getLogger(ctx).error("Tag "+name+" not found setting derived properties for "+table);
					}else{
						PropExpression e = parser.parse(def);
						put(tag,e);
					}
				}catch(ParseException e){
					getLogger(ctx).error("Error making derived property "+name+" from "+def,e);
				} catch (PropertyCastException e) {
					getLogger(ctx).error("Type of expression and PropertyTag don't match",e);
				} catch (InvalidPropertyException e) {
					getLogger(ctx).error("Error making property "+name+" from "+def,e);
				} catch(Exception t){
					getLogger(ctx).error("Serious Error making property "+name+" from "+def,t);
				}
			}
		}
  }
@SuppressWarnings("unchecked")
private <T> PropertyTag<T> addExpression(PropertyRegistry reg, String name,
		PropExpression<T> e) throws PropertyCastException {
	// now make a PropertyTag for this expression
	// where possible we can provide more info by using a PropertyTag sub-class
	PropertyTag<T> tag;
	if( e instanceof NamePropExpression && ((NamePropExpression)e).getTargetRef() instanceof ReferenceTag){
		// Store name expressions as ReferenceTag as they should automatically
		// convert to strings where needed and Strings are not generally combined.
		//
		NamePropExpression expr = (NamePropExpression) e;
		ReferenceExpression oldtag =  expr.getTargetRef();
		tag = new ReferenceTag(reg,name,oldtag.getTarget(),oldtag.getTable());
	}else if( e instanceof ReferenceExpression){
		// this is more legitimate than a DeRefTag as the factory class
		// can be considered part of the expression type.
		tag = new ReferenceTag(reg, name, ((ReferenceExpression)e).getFactoryClass(),((ReferenceExpression)e).getTable());
	}else{
		tag = new PropertyTag<>(reg,name,e.getTarget(),e.toString());
	}
	put(tag, e);
	return tag;
}
 
  /** Register a new definition in the config service 
   * 
   * @param ctx AppContext
 * @param reg 
 * @param prev PropertyFinder to use in parse
 * @param table 
   * @param name String name of new definition
 * @param def String to parse for prop-expression 
 * @throws ParseException 
   */
  @SuppressWarnings("unchecked")
public void addConfigProperty(AppContext ctx, PropertyRegistry reg, PropertyFinder prev, String table, String name, String def) throws  ParseException{
	  ConfigService serv = ctx.getService(ConfigService.class);
	  Parser parser = new Parser(ctx,prev);
		  PropExpression e;
		try {
			e = parser.parse(def);
		} catch (UnresolvedNameException e2) {
			throw new ParseException(e2);
		} // check it parses
		  
		  if( reg != null ){
			  try {
				addExpression(reg, name, e);
				serv.setProperty(PROPERTY_PREFIX+table+"."+name, def);
				serv.clearServiceProperties();
			} catch (PropertyCastException e1) {
				getLogger(ctx).error("Error adding prop expression",e1);
			}
		  }
  }
  
  @SuppressWarnings("unchecked")
public void addConfigProperty(AppContext ctx,PropertyFinder prev,String table,PropertyTag tag,String def) throws ParseException{
	  ConfigService serv = ctx.getService(ConfigService.class);
	  Parser parser = new Parser(ctx,prev);
		  PropExpression e;
		try {
			e = parser.parse(def);
		} catch (UnresolvedNameException e2) {
			throw new ParseException(e2);
		} // check it parses
			  try {
				put(tag,e);
				serv.setProperty(PROPERTY_PREFIX+table+"."+tag.getFullName(), def);
				serv.clearServiceProperties();
			} catch (PropertyCastException e1) {
				getLogger(ctx).error("Error adding prop expression",e1);
			}
  }
  public void removeConfigProperty(AppContext ctx, String table, PropertyTag tag){
	  ConfigService serv = ctx.getService(ConfigService.class);
	  serv.setProperty(PROPERTY_PREFIX+table+"."+tag.getName(), "");
	  serv.setProperty(PROPERTY_PREFIX+table+"."+tag.getFullName(), "");
	  serv.clearServiceProperties();
  }

  public void clear() {
	  map.clear();
  }
  
  protected Logger getLogger(AppContext conn){
	  return conn.getService(LoggerService.class).getLogger(getClass());
  }
  public String toString(){
	  return map.toString();
  }
}