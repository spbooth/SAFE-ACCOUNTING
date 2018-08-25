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
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers.value;

import uk.ac.ed.epcc.webapp.AbstractContexed;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.Description;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
/** ValueParser for IndexedReference types.
 * 
 * @author spb
 *
 */

@Description("Parse a type-safe reference")
public class IndexedReferenceValueParser extends AbstractContexed implements ValueParser<IndexedReference> {
	
	public IndexedReferenceValueParser(AppContext c){
		super(c);
	}
	
	public Class<IndexedReference> getType() {
		
		return IndexedReference.class;
	}

	public IndexedReference parse(String valueString)
			throws ValueParseException {
		return IndexedReference.parseIndexedReference(getContext(), valueString);
	}

	public String format(IndexedReference value) {
		return value.toString();
	}
}