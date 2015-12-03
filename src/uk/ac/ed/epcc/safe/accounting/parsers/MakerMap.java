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
package uk.ac.ed.epcc.safe.accounting.parsers;

import java.util.HashMap;

import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
/** A map from keywords to {@link ContainerEntryMaker}
 * 
 * @author spb
 *
 */
public class MakerMap extends HashMap<String, ContainerEntryMaker> {
	/**
	 * Convenience method for adding a PropertyEntryMaker to a MakerMap. Used in
	 * the numerous static blocks within this class.
	 * 
	 * @param <T>
	 *          The type of value the maker will parse value strings into
	 * @param tag
	 *          The property tag to be associated with the values generated
	 * @param parser
	 *          The parser used to generate values from value strings
	 */
	public <T> ContainerEntryMaker addParser(PropertyTag<T> tag, ValueParser<? extends T> parser){
		return addParser(tag.getName(),tag, parser);
	}
	/**
	 * Convenience method for adding a PropertyEntryMaker to a MakerMap. Used in
	 * the numerous static blocks within this class.
	 * 
	 * @param <T>
	 *          The type of value the maker will parse value strings into
	 * @param name 
	 * 			The attribute name to store the entry.
	 * @param tag
	 *          The property tag to be associated with the values generated
	 * @param parser
	 *          The parser used to generate values from value strings
	 */
	public <T> ContainerEntryMaker addParser(String name,PropertyTag<T> tag, ValueParser<? extends T> parser){
		PropertyEntryMaker<T> propertyMaker = new PropertyEntryMaker<T>(tag, parser);
		ContainerEntryMaker maker = (ContainerEntryMaker) propertyMaker;
		return put(name, maker);
	}
}