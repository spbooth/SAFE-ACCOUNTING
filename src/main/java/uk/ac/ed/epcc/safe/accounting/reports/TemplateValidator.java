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
package uk.ac.ed.epcc.safe.accounting.reports;

import org.w3c.dom.Element;

/** Interface for classes that validate an unprocessed template 
 * (In DOM form)
 * The aim is to add additional validation beyond what can
 * be achieved via schema validation alone. For example 
 * to check the syntax of  PropExpressions. The checks need to be conservative 
 * to not flag problems due to unexpanded elements etc.
 * The validator is allowed to deference up into the surrounding document to find context.
 * 
 * 
 * 
 * @author spb
 *
 */
public interface TemplateValidator {
	/** Consider this element for validity.
	 * If the method returns true the node has been checked and need not be recursed into.
	 * If it returns false then either the Validator does not recognise the node or validates at a lower
	 * level of the document tree. In this case the surrounding framework should recurse into the child elements.
	 * 
	 * 
	 * @param e Element to be considered
	 * @return boolean status.
	 * @throws TemplateValidateException
	 */
	public boolean checkNode(Element e) throws TemplateValidateException;
}