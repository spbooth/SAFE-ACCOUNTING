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
	private final Transform transform;
	public ColName(PropExpression tag, String name){
		this(tag,name,null);
	}
	public ColName(PropExpression tag, String name,Transform transform){
		this.tag=tag;
		this.name=name;
		this.transform=transform;
	}
	public PropExpression getTag(){
		return tag;
	}
	public String getName(){
		if( name != null){
			return name;
		}
		if( tag instanceof PropertyTag){
			return ((PropertyTag)tag).getName();
		}
		return tag.toString();
	}
	public Transform getTransform(){
		return transform;
	}
}