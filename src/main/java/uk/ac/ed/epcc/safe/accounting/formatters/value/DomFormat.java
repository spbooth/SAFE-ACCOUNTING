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

import java.text.Format;

import org.w3c.dom.Document;
import org.w3c.dom.Node;



public class DomFormat<T> implements DomFormatter<T> {

	private final Format fmt;
	private final Class<T> clazz;

	public DomFormat(Class<T> clazz,Format f){
		this.fmt=f;
		this.clazz=clazz;
	}
	public Class<T> getTarget() {
		return clazz;
	}

	public Node format(Document doc, T value) {
		return doc.createTextNode(fmt.format(value));
	};
	
}