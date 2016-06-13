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
package uk.ac.ed.epcc.safe.accounting.properties;

import java.util.regex.Pattern;

import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;


/** Type safe tag for property names. 
 * As well as being generic with respect to its corresponding datatype a PropertyTag also contains an explicit 
 * reference to the corresponding class object. This allows the PropertyTag to perform explicit run-time 
 * type checks where it is not possible to use compile time generic checks. The run-time checks are allowed to be 
 * looser than the compile time checks. This is necessary as the compile-time type may contain generic information that 
 * cannot be represented in a class object.
 * 
 * 
 * PropertyTags do not form a finite set of values like an Enum as different accounting schemes will need to introduce 
 * additional properties. It should also be possible to define properties based on 
 * a configuration file parsed at run-time. However we do need to be able to evaluate the set of available 
 * PropertyTags. Therefore all PropertyTags are created as members of a PropertyRegistry which represents a 
 * set of related Property
 * 
 *
 * 
 * The PropertyTag class can be sub-classed to support functionality that will be common to all
 * implementations of the property such as formatting methods and form Selectors.
 * 
 * @author spb
 * @param <T> type of property
 *
 */


public class PropertyTag<T> implements PropExpression<T> {

	//Note this pattern should be compatible with database column names.
	public static final String PROPERT_TAG_NAME_PATTERN="[a-zA-Z]\\w*";
	public static final Pattern name_pattern=Pattern.compile(PROPERT_TAG_NAME_PATTERN);
	 @Override
	public boolean equals(Object obj) {
		 if( obj == this ){
			 return true;
		 }
		 if( obj.getClass() == getClass()){
			 PropertyTag tag = (PropertyTag) obj;
			 return tag.registry.equals(registry) && tag.name.equals(name);
		 }
		return false;
	}
	 
	 
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	/** Is the parameter a permitted value for this property.
	 * This is the run-time type check that may be less restrictive than the restriction imposed by compile time
	 * generics.
	 * 
	 * @param o
	 * @return boolean
	 */
    public boolean allow(Object o){
    	if( o == null ){
    		return true;
    	}
    	return allowClass(o.getClass());
	}
    public boolean allowClass(Class clazz){    	
    	return property_type.isAssignableFrom(clazz);
    }
    public boolean allowExpression(PropExpression e){
    	return allowClass(e.getTarget());
    }
    
    
    protected final PropertyRegistry registry;
	protected final String name;
	private final Class<? super T> property_type;
	private final String description;
	   
	   
	public PropertyTag(PropertyRegistry registry,String name, Class<? super T> property_type){
	   this(registry,name,property_type,null);
	}
	
	public PropertyTag(PropertyRegistry registry,String name, Class<? super T> property_type,String description){
		
		this.name = name;
		
		assert(property_type != null);
		assert(registry != null);
		
		this.property_type=property_type;
		this.registry=registry;
		
		if (description == null) {
		    this.description = property_type.getSimpleName();
		} else {
			this.description = description;
		}
		
		if (!name_pattern.matcher(name).matches()) {
			throw new ConsistencyError("Invalid PropertyTag name " + name);
		}
		
		registry.register(this);
	}
	
	  
	@Override
	public String toString(){
	   return getFullName();
	}
	public String getName(){
	   return name;
	}
	public String getFullName(){
	   return registry.getPrefix()+name;
	}
	public String getDescription(){
	   return description;
	}
	  
	public Class<? super T> getTarget(){
	   return property_type;
	}
	public PropertyRegistry getRegistry(){
	   return registry;
	}
	public <R> R accept(BasePropExpressionVisitor<R> vis) throws Exception {
		return vis.visitPropertyTag(this);
	}
	public PropertyTag<T> copy() {
		return this;
	}
	   
}