//| Copyright - The University of Edinburgh 2015                            |
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
package uk.ac.ed.epcc.safe.accounting;

import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.content.Transform;
/** Simple parameter class for building tables from properties.
 * 
 * @author spb
 *
 */
public class ColName{
	private final PropExpression tag;
	private final String name;
	private final PropExpression name_expr;
	private final Transform transform;
	public ColName(PropExpression tag, String name,PropExpression name_expr){
		this(tag,name,name_expr,null);
	}
	/** Parameters for a Table column
	 * 
	 * @param tag  {@link PropExpression} to generate in column
	 * @param name Name of the column (or group if dynamic)
	 * @param name_expr Optional {@link PropExpression} to generate the column name dynamically
	 * @param transform Optional {@link Transform} to apply to the column
	 */
	public ColName(PropExpression tag, String name,PropExpression name_expr,Transform transform){
		this.tag=tag;
		this.name=name;
		this.name_expr=name_expr;
		this.transform=transform;
	}
	/** Get the expression to generate as the cell data
	 * 
	 * @return
	 */
	public PropExpression getTag(){
		return tag;
	}
	/** Get the name of the colum (or column group)
	 * 
	 * @return
	 */
	public String getName(){
		if( name != null){
			return name;
		}
		if( tag instanceof PropertyTag){
			return ((PropertyTag)tag).getName();
		}
		return tag.toString();
	}
	/** Get a dynamic name of a column within a column group.
	 * If null is returned this represents a simple column
	 * 
	 * @param t
	 * @return
	 */
	public PropExpression getNameExpression() {
		return name_expr;
	}
	/** Get an optional transform for the Column
	 * 
	 * @return
	 */
	public Transform getTransform(){
		return transform;
	}
}