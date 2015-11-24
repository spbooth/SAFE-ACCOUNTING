// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.properties;

import java.util.HashMap;

import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;

/** Defines a set of related {@link PropertyTag} objects in a single name-space
 * A PropertyRegistry allows enumeration over the set of 
 * available properties. It also implements {@link PropertyFinder} so String names can be parsed 
 * to {@link PropertyTag}s.
 * Where the set of properties are fixed at compile time the PropertyRegistry and the 
 * {@link PropertyTag}s it contains can be declared as static constants.
 * <b>
 * If a PropertyRegistry has to be created at run-time then it should be populated 
 * fully as soon as possible so the set of available Properties remains consistent over time.
 * <p>
 * Unfortunately It is possible to create multiple PropertyRegistry instances with the same name. 
 * For  example where there are multiple accounting tables each of which have generated a set of
 * dynamic properties using the same policy but the contents being dependent on the configuration of the
 * accounting table. This should be avoided because PropertyRegistry objects with the same name count as being equal
 * so problems will arise if these tables are then combined into a composite UsageProducer.
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: PropertyRegistry.java,v 1.3 2014/09/15 14:32:27 spb Exp $")

public class PropertyRegistry extends FixedPropertyFinder {
	private boolean locked=false;
	public PropertyRegistry(String name,String description){
		super(name,description,new HashMap<String, PropertyTag>());
	}
	void register(PropertyTag t){
		if( locked ){
			throw new ConsistencyError("PropertyRegistry is locked");
		}
	  PropertyTag currentKey = registry.get(t.getName());
	  if(currentKey == null){
	    // If the PropertyTag isn't currently in the registry, add it
	    registry.put(t.getName(), t);
	  } else if(t.equals(currentKey)) {
	    /* 
	     * If the new PropertyTag is identical to one already in the registry,
	     * silently ignore the registration.  The PropertyTag is there so
	     * there is nothing to register.
	     */
	    return;
	  } else {
	    /* 
	     * Can't add a PropertyTag with the same name as one currently in the 
	     * registry unless they are equal
	     */
	    throw new ConsistencyError("Can't register PropertyTag '"+t.getName() + 
	      "'.  A different PropertyTag with that name is already present in " +
	      "this registry");
		}
	}
	
	@Override
	public PropertyFinder copy() {
		return new FixedPropertyFinder(name,getDescription(),new HashMap<String, PropertyTag>(registry));
	}
	/** prevent further modifications to this regismenttry
	 * For a statically allocated PropertyREgistry this can be
	 * invoked in as assign
	 * 
	 */
	public void lock(){
		locked=true;
	}
}