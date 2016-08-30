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

import java.util.Iterator;
import java.util.Map.Entry;

import uk.ac.ed.epcc.safe.accounting.parsers.ContainerEntryMaker;
import uk.ac.ed.epcc.safe.accounting.parsers.MakerMap;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;

/** Type safe tag for aprun and rur attribute names. 
 *  
 *  aprun attribute names have hyphen prefixes that render
 *  these names incompatible as SQL table column names.
 *  
 *  Hence, the actual names are stored as aliases, which also
 *  solves the problem of aprun attributes having short and long
 *  forms (e.g., "-cc" and "--cpu-binding"), and the tag name
 *  associated with these aliases is SQL compatible (e.g., cpu_binding).
 *  
 *  Similarly, some rur attribute names begin with non-alphanumeric
 *  characters, e.g., %_of_boot_mem.
 *  
 * @author mrb
 * @param <T> type of property
 *
 */

public class AttributePropertyTag<T> extends PropertyTag<T> {

	private final String[] aliases;	 
	private final T default_value;
	
	
	public AttributePropertyTag(PropertyRegistry registry, String name, String[] aliases, Class<? super T> property_type, T default_value) {
		super(registry,name,property_type,null);
		this.aliases = aliases;
		this.default_value = default_value;
	}
	
	public AttributePropertyTag(PropertyRegistry registry, String name, String[] aliases, Class<? super T> property_type, String description, T default_value) {
		super(registry,name,property_type,description);
		this.aliases = aliases;
		this.default_value = default_value;
	}
	
	
	public String[] getAliases() {
		return aliases;
	}
	
	/**
	 * @param i
	 * @return the ith alias or null if i out of range
	 */
	public String getAlias(int i) {
		String alias = null;
		
		if (null != aliases) {
			if (i >= 0 && i < aliases.length) {
				alias = aliases[i];
			}
		}
		
		return alias;
	}
	
	/**
	 * @param attrName
	 * @return true if one of the aliases matches attrName
	 */
	public boolean aliasMatch(String attrName) {
		boolean matchFound = false;
		
		if (null != attrName && null != aliases) {
			for (int i = 0; !matchFound && i < aliases.length; ++i) {
				matchFound = attrName.equals(aliases[i]);
			}
		}
		
		return matchFound;
	}
	
	
	public T getDefaultValue() {
		return default_value;
	}
	

	public boolean compareNames(String name) {
		return name.equals(getName());
	}
	
	/**
	 * Iterate through the attribute registry and look for an attribute
	 * that uses an alias that matches the alias parameter.
	 * 
	 * @param reg
	 * @param alias
	 * @param compare_name if true first compare alias with property name before comparing aliases
	 * @return null if alias not used by any tag in registry, otherwise
	 * return an appropriately constructed AttributePropertyTag<?> object.
	 */
	public static AttributePropertyTag<?> findAttribute(PropertyRegistry reg, String alias, boolean compare_name) {
		AttributePropertyTag<?> tag = null;
		
		boolean matchFound = false;
		Iterator<Entry<String, PropertyTag>> props = reg.getIterator();
		while (!matchFound && props.hasNext()) {
			PropertyTag prop = props.next().getValue();
			
			tag = (AttributePropertyTag<?>) reg.find(prop.getName());
			if (null != tag) {
				if (compare_name) {
					matchFound = tag.compareNames(alias);
				}
				if (!matchFound) {
					matchFound = tag.aliasMatch(alias);
				}
			}
		}
		
		return matchFound ? tag : null;
	}
	
	
	/**
	 * Iterate through the attribute registry and look for an attribute
	 * that uses an alias that matches the alias parameter.
	 * 
	 * @param reg
	 * @param alias
	 * @return null if alias not used by any tag in registry, otherwise
	 * return an appropriately constructed AttributePropertyTag<?> object.
	 */
	public static AttributePropertyTag<?> findAttribute(PropertyRegistry reg, String alias) {
		return findAttribute(reg, alias, false);
	}
	
	
	/**
	 * Add the value of this property to a property map.
	 * 
	 * @param maker_map
	 * @param map
	 * @param name
	 * @param value
	 * @throws NullPointerException
	 * @throws AccountingParseException
	 */
	public void setValue(MakerMap maker_map, PropertyMap map, String name, String value) throws NullPointerException, AccountingParseException {
		ContainerEntryMaker maker = maker_map.get(name);
		if (null == maker) {
			return;
		}
		
		try {
			maker.setValue(map, value);
		} catch (IllegalArgumentException e) {
			throw new AccountingParseException("Problem with attribute '" + name
					+ "': Unable to parse value '" + value + "'", e);
		}
	}
	
	
	/**
	 * Iterate through the attribute registry and add those attributes
	 * that have not yet been added to map.
	 * 
	 * @param reg
	 * @param make_map
	 * @param map
	 * @throws AccountingParseException 
	 * @throws NullPointerException 
	 */
	public static void completePropertyMap(PropertyRegistry reg, MakerMap maker_map, PropertyMap map) throws NullPointerException, AccountingParseException {
		
		Iterator<Entry<String, PropertyTag>> props = reg.getIterator();
		while (props.hasNext()) {
			PropertyTag prop = props.next().getValue();
			if (null == map.getProperty(prop)) {
				// this attribute was not set
				String nm = prop.getName();
				
				// set the default value for this property
				// if it exists in maker map
				ContainerEntryMaker maker = maker_map.get(nm);
				if (null == maker) {
					continue;
				}
				
				AttributePropertyTag tag = (AttributePropertyTag) reg.find(nm);
				tag.setValue(maker_map, map, nm, tag.getDefaultValue().toString());
			}
		}
		
	}
}