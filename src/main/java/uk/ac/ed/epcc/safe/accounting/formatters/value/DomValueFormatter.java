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



public class DomValueFormatter<T> implements DomFormatter<T> {

	private final ValueFormatter<T> parser;

	public DomValueFormatter(ValueFormatter<T> p){
		this.parser=p;
	}
	public Class<T> getTarget() {
		return parser.getType();
	}

	public Node format(Document doc, T value) {
		return doc.createTextNode(parser.format(value));
	};
	
}