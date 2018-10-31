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

import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ed.epcc.safe.accounting.parsers.ContainerEntryMaker;
import uk.ac.ed.epcc.safe.accounting.parsers.MakerMap;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;


/** Type safe tag for rur attributes whose names are represented
 *  as regular expressions. 
 *  
 *  The regular expression is stored as Pattern object and an associated
 *  set of group names.
 *  
 * @author mrb
 * @param <T> type of property
 *
 */

public class DynamicAttributePropertyTag<T> extends AttributePropertyTag<T> {

	private final Pattern name_pattern;
	private final Set<String> group_names;
	private String[] group_values;
	private String matched_name, last_matched_name;
	private String matched_data;
	
	private final boolean subattrs;
	private final boolean multattrs;
	
	
	public DynamicAttributePropertyTag(PropertyRegistry registry,
			String name, String regex, String[] aliases,
			Class<T> property_type,	String description, T default_value,
			boolean subattrs, boolean multattrs) {
		
		super(registry,name,aliases,property_type,description,default_value);
		
		name_pattern = Pattern.compile(regex);
		group_names = getGroupNames(regex);
		group_values = new String[group_names.size()];
		for (int i = 0; i < group_values.length; ++i) {
			group_values[i] = "";
		}
				
		matched_name = "";
		last_matched_name = "";
		matched_data = "";
		
		this.subattrs = subattrs;
		this.multattrs = multattrs;
	}
	
	
	/** Return the group names contained within the regular
	 *  expression regex.
	 * 
	 */
	private static Set<String> getGroupNames(String regex) {
        Set<String> names = new TreeSet<>();
        Matcher m = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>").matcher(regex);

        while (m.find()) {
        	names.add(m.group(1));
        }

        return names;
    }


	/**
	 * Return true if the name parameter matches the pattern
	 * defined by the regular expression.
	 * 
	 */
	@Override
	public boolean compareNames(String name) {
		Matcher m = name_pattern.matcher(name);
	
		boolean match_found = m.matches();
		if (match_found) {
			if (multattrs && matched_name.length() > 0) {
				matched_name += ", " + name;
			}
			else {
				matched_name = name;
			}
			last_matched_name = name;
        }
		
		return match_found;
	}
	
	
	/**
	 * Set the value for this dynamically named attribute - this will be the string matched the regular expression.
	 * However, the parts of this string that match the various group names are also stored within the property map. 
	 */
	@Override
	public void setValue(MakerMap maker_map, PropertyMap map, String name, String value) throws NullPointerException, AccountingParseException {
		
		Matcher m = name_pattern.matcher(last_matched_name);
		if (!m.matches()) {
			throw new AccountingParseException("Problem with matching attributes within '"
					+ matched_name + "' of attribute '" + name + "'.");
		}
		
		String nm="", val="";
		
		try {
			nm = name + "name";
			val = matched_name;
			ContainerEntryMaker maker = maker_map.get(nm);
			if (null != maker) {
				maker.setValue(map, val);
			}
			
			int i = 0;
			for (String nm2: group_names) {
				nm = nm2;
				val = m.group(nm);
					
				if (multattrs && group_values[i].length() > 0) {
					group_values[i] += ", "+val;
				}
				else {
					group_values[i] = val;
				}
				
				val = group_values[i];
				maker = maker_map.get(nm);
				if (null == maker) {
					continue;
				}
				
				maker.setValue(map, val);
				i++;
			}	
					
			if (!subattrs) {
				nm = name + "_data";
				
				if (multattrs && matched_data.length() > 0) {
					matched_data += ", ["+value+"]";
				}
				else {
					matched_data = "["+value+"]";
				}
				
				val = matched_data;
				maker = maker_map.get(nm);
				if (null != maker) {
					maker.setValue(map, val);	
				}
			}
		
		} catch (IllegalArgumentException e) {
			throw new AccountingParseException("Problem with subattribute '" + nm
				+ "' of attribute '" + name
				+ "': unable to parse value '" + val + "'.", e);
		}
		
	}
	
}