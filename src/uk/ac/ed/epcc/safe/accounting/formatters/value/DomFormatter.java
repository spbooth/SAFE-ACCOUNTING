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
package uk.ac.ed.epcc.safe.accounting.formatters.value;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import uk.ac.ed.epcc.webapp.Targetted;

/** interface for classes that format an object into XML
 * 
 * @param <T> input type for formatter.
 * @author spb
 *
 */
public interface DomFormatter<T> extends Targetted<T>{

	/** Get the target type for this formatter.
	 * The input values must be assignable to this type.
	 * 
	 * @return Class of target
	 */
	Class<? super T> getTarget();
	
	/** format the input value as a Dom Node.
	 * @param doc document to create result in
	 * @param value
	 * @return Node or null
	 * @throws Exception 
	 */
	Node format(Document doc, T value) throws Exception;
}