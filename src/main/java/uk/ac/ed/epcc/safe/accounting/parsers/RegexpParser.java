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
package uk.ac.ed.epcc.safe.accounting.parsers;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParserPolicy;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.update.AbstractPropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.webapp.AppContext;
/** PropertyContainerParser based on regular expressions.
 * 
 * This class should be sub-classed to make a working parser. Any {@link PropertyTag}
 * fields that are marked with the {@link Regexp} annotation will be parsed using the
 * defined regular expression and the {@link ValueParserPolicy}
 * 
 * @author spb
 *
 */
public abstract class RegexpParser extends AbstractPropertyContainerParser {
   
	public RegexpParser(AppContext conn) {
		super(conn);
	}
	Map<PropertyTag,Pattern> targets;
    Map<PropertyTag,ValueParser> parsers;
    
   
	@SuppressWarnings("unchecked")
	@Override
	public boolean parse(DerivedPropertyMap map, String record)
			throws AccountingParseException {
		boolean match=false;
		for(PropertyTag t : targets.keySet()){
			//System.out.println("Tag "+t.getFullName()+" "+targets.get(t).pattern());
			Matcher m = targets.get(t).matcher(record);
			if( m.find()){
				match=true;
				ValueParser v=parsers.get(t);
				String val = m.group(1);
				//System.out.println("Value is ["+val+"]");
				try{
				
					map.setProperty(t, v.parse(val));
				
				}catch(Exception e){
					throw new AccountingParseException("Bad value for "+t.getName(), e);
				}
			}
		}
		return match;
	}

	@Override
	public Iterator<String> splitRecords(String update)
			throws AccountingParseException {
		return new UnixFileSplitter(update);
	}

	
	

	

	@SuppressWarnings("unchecked")
	@Override
	public void startParse(PropertyContainer staticProps) throws Exception {
		targets=new HashMap<>();
		parsers=new HashMap<>();
		Class myclass=getClass();
		// Set default targets for field tags.
		ValueParserPolicy vis = new ValueParserPolicy(getContext());
		for( Field f : myclass.getFields()){
			try{
				if( PropertyTag.class.isAssignableFrom(f.getType()) && f.isAnnotationPresent(Regexp.class)){
					PropertyTag tag = (PropertyTag) f.get(this);
					String patt = f.getAnnotation(Regexp.class).value();
					Pattern p = Pattern.compile(patt); 
					targets.put(tag, p);
					parsers.put(tag, (ValueParser) tag.accept(vis));
				}
			}catch(Exception e){
				getLogger().error("Error making targets for property fields "+f.toGenericString(),e);
			}
		}
	}
	@Override
	public String endParse() {
		targets.clear();
		targets=null;
		parsers.clear();
		parsers=null;
		return super.endParse();
	}

}