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
package uk.ac.ed.epcc.safe.accounting.charts;

import java.security.Principal;

import uk.ac.ed.epcc.webapp.Indexed;
/** A {@link ReferenceLabeller} that returns the simple name rather than the full
 * identifier.
 * 
 * @author spb
 *
 * @param <D>
 */
public class ReferenceNameLabeller<D extends Indexed> extends ReferenceLabeller<D> {

	public ReferenceNameLabeller() {
		
	}

	@Override
	public String getLabel(D val) {
		if( val instanceof Principal){
			return ((Principal)val).getName();
		}
		return super.getLabel(val);
	}

	

}