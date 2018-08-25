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
package uk.ac.ed.epcc.safe.accounting.reports;

import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParserPolicy;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.AbstractContexed;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.content.Table;

/** Class that generates Table summaries of a PropertyContainer
 * 
 * @author spb
 *
 */


public class PropertyTableMaker extends AbstractContexed {

	private final PropertyFinder finder;
	private ValueParserPolicy policy;
	
	public PropertyTableMaker(AppContext conn,PropertyFinder finder){
		super(conn);
		this.finder=finder;
		policy = new ValueParserPolicy(conn);
	}

	public Table getTable(PropertyContainer pc){
		Table<String,PropertyTag> t = new Table<String,PropertyTag>();
		for(PropertyTag<?> tag : finder.getProperties()){
			if( pc.supports(tag)){
				addProperty(tag, t, pc);
			}
		}
		
	
		return t;
	}
	private <T> void addProperty(PropertyTag<T> tag, Table<String,PropertyTag> t, PropertyContainer pc){
		T dat = pc.getProperty(tag, null);
		if( dat != null ){
			try{
			@SuppressWarnings("unchecked")
			ValueParser<T> vp = (ValueParser<T>)policy.visitPropertyTag(tag);
			t.put("Name", tag, tag.getName());
			t.put("Description",tag,tag.getDescription());
			t.put("Value",tag,vp.format(dat));
			}catch(Throwable te){
				getLogger().error("Error formatting property",te);
			}
		}
	}
}