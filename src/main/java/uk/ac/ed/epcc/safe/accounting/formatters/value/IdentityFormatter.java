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

import uk.ac.ed.epcc.webapp.Description;
import uk.ac.ed.epcc.webapp.forms.Identified;


@Description("Generate the Identity string of the target")
public class IdentityFormatter implements DomFormatter<Identified> {

	public Class<Identified> getTarget() {
		return Identified.class;
	}

	public Node format(Document doc, Identified value) throws Exception {
		if(value == null){
			return null;
		}
		return doc.createTextNode(value.getIdentifier());
	}

}