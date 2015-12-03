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
package uk.ac.ed.epcc.safe.accounting.parsers.value;

import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.AppContext;

/** Interface that can be implemented by a {@link uk.ac.ed.epcc.safe.accounting.properties.PropertyTag}
 * to allow it to provide a custom {@link ValueParser} for the property.
 * 
 * @author spb
 * @param <T> expression type
 *
 */
public interface ValueParserProvider<T> extends PropExpression<T> {
	// Note we have to allow super-type as ValueParsers are
	// type with the bare type with generics removed.
	ValueParser<? super T> getValueParser(AppContext c);
}