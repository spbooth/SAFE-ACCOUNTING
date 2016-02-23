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
package uk.ac.ed.epcc.safe.accounting.db.transitions;

import java.util.LinkedHashSet;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.db.AccessorMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.content.Table;
import uk.ac.ed.epcc.webapp.jdbc.table.AbstractTableRegistry;
import uk.ac.ed.epcc.webapp.jdbc.table.DefaultTableTransitionRegistry;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.jdbc.table.TableStructureTransitionTarget;
import uk.ac.ed.epcc.webapp.jdbc.table.TableTransitionRegistry;
import uk.ac.ed.epcc.webapp.model.data.Repository;
import uk.ac.ed.epcc.webapp.session.SessionService;


/** A {@link TableTransitionRegistry} for classes that support PropExpressions.
 * 
 * @author spb
 *
 */
public class TableRegistry<T extends TableStructureTransitionTarget> extends DefaultTableTransitionRegistry<T> implements TableTransitionRegistry{
	private AccessorMap<?> map;
	private final Set<PropertyTag> props;
	
	public TableRegistry(Repository res, TableSpecification spec, Set<PropertyTag> props,AccessorMap<?> m){
		super(res,spec);
		this.map=m;
		if( props == null){
			this.props=map.getProperties();
		}else{
			this.props=props;
		}
	}
	
	

	

	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.webapp.model.data.transition.TableTransitionTarget#getTableTransitionSummaryHTML(uk.ac.ed.epcc.webapp.model.AppUser)
	 */
	public  void getTableTransitionSummary(ContentBuilder hb,SessionService operator) {
		hb.addText("The following table shows those properties that are defined for the current configuration." +
				" and how they are currently implemented. If a property does not have a corresponding database field it " +
				"may be defined as an expression over other properties.");
		// put each registry into a seperate table 
		// this lets us show the descriptions and gives more flexibility to 
		// table layout
		Set<PropertyRegistry> registries = new LinkedHashSet<PropertyRegistry>();
		for( PropertyTag t : props){
			registries.add(t.getRegistry());
		}
		PropExpressionMap expression_map = map.getDerivedProperties();
		for( PropertyRegistry reg : registries){
			hb.addHeading(4,reg.toString());
			hb.addHeading(5,reg.getDescription());
			Table<String,String> t = new Table<String,String>();
			for(PropertyTag<?> tag : props){
				if( reg.hasProperty(tag)){
					String name = tag.getFullName();
					t.put("Target", name, tag.getTarget().getSimpleName());
					if( map != null ){
						t.put("Implementation", name, map.getImplemenationInfo(tag));
					}
					if( expression_map != null ){
						PropExpression<?> e = expression_map.get(tag);
						if( e != null ){
							t.put("Expression", name, e.toString());
						}else{
							t.put("Expression", name, "");
						}
					}
					t.put("Description", name, tag.getDescription());
				}
			}
			t.setKeyName("Name");
			t.sortRows();
			hb.addTable(operator.getContext(), t);
		}		
	}
	public final  Table getPropertyTable(){
		Table<String,String> t = new Table<String,String>();
		PropExpressionMap expression_map = map.getDerivedProperties();
		for(PropertyTag<?> tag : props){
			String name = tag.getFullName();
			t.put("Target", name, tag.getTarget().getSimpleName());
			if( map != null ){
				t.put("Implementation", name, map.getImplemenationInfo(tag));
			}
			if( expression_map != null ){
				PropExpression<?> e = expression_map.get(tag);
				if( e != null ){
					t.put("Expression", name, e.toString());
				}else{
					t.put("Expression", name, "");
				}
			}
			t.put("Description", name, tag.getDescription());
		}
		t.setKeyName("Name");
		t.sortRows();
		return t;
	}
}