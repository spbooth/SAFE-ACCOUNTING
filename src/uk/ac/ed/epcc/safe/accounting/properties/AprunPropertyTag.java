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


/** Type safe tag for aprun command attribute names. 
 *  
 *  aprun attribute names have hyphen prefixes that render
 *  these names incompatible as SQL table column names.
 *  
 *  Hence, the actual names are stored as aliases, which also
 *  solves the problem of aprun attributes having short and long
 *  forms (e.g., "-cc" and "--cpu-binding"), and the tag name
 *  associated with these aliases is SQL compatible (e.g., cpu_binding).
 *  * 
 * @author mrb
 * @param <T> type of property
 *
 */

public class AprunPropertyTag<T> extends PropertyTag<T> {

	private final String[] aliases;	 
	private final T default_value;
	
	public AprunPropertyTag(PropertyRegistry registry, String name, Class<T> property_type) {
		super(registry,name,property_type,null);
		this.aliases = null;
		this.default_value = null;
	}
	
	public AprunPropertyTag(PropertyRegistry registry, String name, String[] aliases, Class<T> property_type, T default_value) {
		super(registry,name,property_type,null);
		this.aliases = aliases;
		this.default_value = default_value;
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
}