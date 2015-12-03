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
package uk.ac.ed.epcc.safe.accounting.charts;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.content.Labeller;


/** Labeller to map a number to the next highest power of two.
 * It returns an integer so the results sort numerically.
 * @author spb
 *
 * @param <T>
 */
public class Power2Labeller<T extends Number> implements Labeller<T,Integer> {

	public Integer getLabel(AppContext conn, T key) {
		int i=key.intValue();
		int j=1;
		while(j< i){
			j*=2;
		}
		return j;
	}
	
	public Class<? super Integer> getTarget(){
		return Integer.class;
	}

	public boolean accepts(Object o) {
		if( o != null && o instanceof Number){
			return true;
		}
		return false;
	}

}