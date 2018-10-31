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

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
/** MultiFinder is a PropertyFinder that spans multiple PropertyRegistry objects.
 * If MultiFinders can be added then each of the wrapped PropertyRegistry objects will be added 
 * in turn. This means that any subsequent changes to the child MultiFinder will not be tracked by the 
 * parent.
 * Finders are always searched for in the reverse order they were added. Re-adding a {@link FixedPropertyFinder}
 * will promote that finder to the top of the search order.
 * @author spb
 *
 */


public final class MultiFinder implements PropertyFinder{
	// The set iterates in inserted order the list runs backwards.
    Set<FixedPropertyFinder> finders=new HashSet<>();
    LinkedList<FixedPropertyFinder> search_list = new LinkedList<>();
    /** Add a PropertyFinder to the Set of finders to be searched.
     * Finders are always searched in the reverse order they were added.
     * 
     * @param f PropertyFinder to add
     */
    public void addFinder(PropertyFinder f){
    	if( f == null ){
    		return;
    	}
    	if( f instanceof MultiFinder){
    		// If a multifinder add the nested finders individually
    		// to stop us getting anything twice. This should not
    		// change the behaviour over adding the MultiFinder directly
    		// The sub entries need to be added to preserve the search order
    		// in the original MultiFinder but promote the constiuent finders
    		// to the top of the search list
    		MultiFinder m = (MultiFinder) f;
    		
    		
    		// cant use the finders as this may have lost iteration order
//    		Iterator<FixedPropertyFinder> it = m.search_list.descendingIterator();
//    		while( it.hasNext()){
//    			addFinder(it.next());
//    		}
    		
    		// descendingIterator id JDK 1.6
    		for(int i=(m.search_list.size()-1); i>=0;i--){
    			addFinder(m.search_list.get(i));
    		}
    		
    	}else if( f instanceof FixedPropertyFinder){
    		FixedPropertyFinder fixed = (FixedPropertyFinder)f;
    		if( finders.contains(fixed)){	
    			// Delete if we already have it so it adds first.
    			search_list.remove(fixed);
    		}
    		finders.add(fixed);
    		search_list.addFirst(fixed);
    		
    	}else{
			throw new ConsistencyError("Cannot add PropertyFinder that is not FixedPropertyFinder of MultiFinder");
		}
    }
	public PropertyTag<?> make(String name) throws InvalidPropertyException {
		
		  for(PropertyFinder f : search_list){
		    	PropertyTag<?> t = f.find(name);
		    	if( t != null ){
		    		return t;
		    	}
		    }
			throw new UnresolvedNameException(name,this);
	}
	public PropertyTag<?> find(String name) {
		  for(PropertyFinder f : search_list){
			  
		    	PropertyTag t = f.find(name);
		    	if( t != null ){
		    		return t;
		    	}
		    }
			return null;
	}
	
	public <T> PropertyTag<? extends T> find(Class<T> clazz,String name) {
		  for(PropertyFinder f : search_list){
		    	PropertyTag<? extends T> t = f.find(clazz,name);
		    	if( t != null ){
		    		return t;
		    	}
		    }
			return null;
	}
	
	public PropertyTag<?> find(TagFilter fil,String name) {
		  for(PropertyFinder f : search_list){
		    	PropertyTag<?> t = f.find(fil,name);
		    	if( t != null ){
		    		return t;
		    	}
		    }
			return null;
	}
	
	public Set<PropertyTag> getProperties() {
		LinkedHashSet<PropertyTag> result = new LinkedHashSet<>();
		for( PropertyFinder f : search_list){
			result.addAll(f.getProperties());
		}
		return result;
	}
	
	public boolean hasProperty(PropertyTag tag){
		for(PropertyFinder f : search_list){
			if( f.hasProperty(tag)){
				return true;
			}
		}
		return false;
	}
	
	public <T> Set<PropertyTag<? extends T>> getProperties(Class<T> clazz) {
		Set<PropertyTag<? extends T>> result = new LinkedHashSet<>();
		for(PropertyFinder f: search_list){
			// though the result does not have to be sorted
			// put the higher pri tags first.
			result.addAll(f.getProperties(clazz));
		}	
		return result;
	}
	
	
	public PropertyFinder copy() {
		MultiFinder copy = new MultiFinder();
		copy.addFinder(this);
		return copy;
	}
	
    public Set<FixedPropertyFinder> getNested(){
    	return new LinkedHashSet<>(search_list);
    }
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("MultiFinder");
		for(FixedPropertyFinder f : finders){
			sb.append("-");
			sb.append(f.toString());
		}
		return sb.toString();
	}
}