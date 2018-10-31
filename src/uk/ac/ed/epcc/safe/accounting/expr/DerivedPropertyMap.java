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

import java.util.HashSet;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.db.MatchSelectVisitor;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.SetPropertyFinder;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
/** A PropertyMap that can also define derived properties.
 * stored properties always take precidence even when a derived definition exists
 * 
 * @author spb
 *
 */


public class DerivedPropertyMap extends PropertyMap implements ExpressionTarget{
  private final AppContext conn;
  private final PropExpressionMap derived = new PropExpressionMap();
  //private Logger log;
  
  public DerivedPropertyMap(AppContext c){
	  conn=c;
	 // log=c.getService(LoggerService.class).getLogger(getClass());
  }
  protected final Logger getLogger(){
	  return conn.getService(LoggerService.class).getLogger(getClass());
  }
  public <T extends Number> void addDerived(PropertyTag<T> tag,PropExpression<? extends T> expr ) throws PropertyCastException{
	  derived.put(tag, expr);
  }
  public void addDerived(PropExpressionMap map){
	  derived.getAllFrom(map);
  }
  
  @Override
public <T> T getProperty(PropertyTag<T> key) {
	T value = getNonDerivedProperty(key);
	if( value != null ){
		return value;
	}
	if( derived.containsKey(key)){
		try {
			T result = (T) evaluate(key.getTarget(),derived.get(key));
			assert(key.allow(result));
			return result;
		}catch(InvalidPropertyException e){
			return null;
		} catch (Exception e) {
			getLogger().error("Error in derived property evaluation "+derived.get(key),e);
		}
	
	}
	return null;
}
public <T> T getNonDerivedProperty(PropertyTag<T> key) {
	return super.getProperty(key);
}
@Override
public Set<PropertyTag> propertySet() {
	Set<PropertyTag> res = new HashSet<>();
	res.addAll(super.propertySet());
	res.addAll(derived.keySet());
	return res;
}
/** evaluate a derived property expression.
 * To prevent infinite recursion due to self referential property definitions
 * this call has a limited recursion depth.
 * @param <T> Type of expression
 * @param target target class

 * 
 * @param texpr expression to evaluate
 * @return Number value
 * @throws Exception 
 */
@SuppressWarnings("unchecked")
public <T> T evaluate(Class<T> target,PropExpression texpr) throws Exception{
    EvaluatePropExpressionVisitor vis = new EvaluatePropExpressionVisitor(conn) {
		private int depth=derived.size();
		
		public Object visitPropertyTag(PropertyTag<?> tag) throws Exception {
			Object n = getNonDerivedProperty(tag);
			  if( n != null){
				 return n;
			  }else if( derived.containsKey(tag) && depth > 0){
				// If the derived properties map has been used more times than we have derived properties
				 // then we must have recursive definition so don't recurse any deeper
				 depth--;
				 PropExpression<?> propExpression = derived.get(tag);
				Object result= propExpression.accept(this);
				 depth++;
				 return result;
			  }
			  throw new InvalidPropertyException(tag.getFullName());
		}

		@Override
		protected boolean matches(RecordSelector sel) throws Exception {
			MatchSelectVisitor<DerivedPropertyMap> vis = new MatchSelectVisitor<>(DerivedPropertyMap.this);
			return sel.visit(vis).booleanValue();
		}
	};
	Object result = texpr.accept(vis);
	if( result == null || target == null || target.isAssignableFrom(result.getClass())){
		return (T) result;
	}
	throw new PropertyCastException("Result of expression has wrong type");
	
  }
public Parser getParser() {
	return new Parser(conn,new SetPropertyFinder(propertySet()));
}
public <T> T evaluateExpression(PropExpression<T> expr)
		throws InvalidPropertyException {
	try {
		return evaluate(null, expr);
	}catch(InvalidPropertyException ip){
		throw ip;
	} catch (Exception e) {
		throw new ConsistencyError("Error in evaluate", e);
	}
}
public <T> T evaluateExpression(PropExpression<T> expr, T def){
	try {
		return evaluate(null, expr);
	}catch(InvalidPropertyException ip){
		return def;
	} catch (Exception e) {
		throw new ConsistencyError("Error in evaluate", e);
	}
}
public void release() {
	super.release();
	if( derived != null ){
		derived.clear();
	}
	
}
/* (non-Javadoc)
 * @see uk.ac.ed.epcc.safe.accounting.properties.PropertyMap#getContents()
 */
@Override
protected String getContents() {
	return super.getContents()+" derv="+derived.toString();
}
}