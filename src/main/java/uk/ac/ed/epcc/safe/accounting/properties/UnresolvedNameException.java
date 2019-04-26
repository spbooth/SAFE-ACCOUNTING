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
package uk.ac.ed.epcc.safe.accounting.properties;


public class UnresolvedNameException extends InvalidPropertyException {
	private final String name;
	private final PropertyFinder finder;
	public UnresolvedNameException(String name,PropertyFinder finder){
		super("Unresolved name "+name+(finder != null ? " from "+finder.toString():""));
		this.name=name;
		this.finder=finder;
	}
	public String getUnresolvedName(){
		return name;
	}
	/** Get the PropertyFinder in scope when the name failed to parse.
	 * 
	 * @return PropertyFinder or null;
	 */
	public PropertyFinder getFinder(){
		return finder;
	}
}