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
import uk.ac.ed.epcc.webapp.Indexed;
import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.content.Labeller;
import uk.ac.ed.epcc.webapp.content.UIGenerator;
import uk.ac.ed.epcc.webapp.forms.Identified;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;



public class ReferenceLabeller<D extends Indexed> implements Labeller<IndexedReference<D>,Object> {
    private static final String DEFAULT_LABEL = "Unknown";
	
  
	public final Object getLabel(AppContext conn, IndexedReference<D> key) {
		if( key == null || key.isNull()){
			return getDefaultLabel();
		}
		assert(key instanceof IndexedReference);
		D res = key.getIndexed(conn);
		if( res != null ){
			return getLabel(conn,res);
		}
		return getDefaultLabel();
	}
	public final Class<? super Object> getTarget(){
		return Object.class;
	}
	/** Default label to generate
	 * @return
	 */
	public String getDefaultLabel() {
		return DEFAULT_LABEL;
	}
	/** A wrapper class for a {@link UIGenerator} that ensures 
	 * the string representation follows the {@link ReferenceLabeller} rules.
	 * 
	 * @author spb
	 *
	 */
	public static class UIWrapper implements UIGenerator, Comparable<UIWrapper>{
		@Override
		public String toString() {
			if( inner instanceof Identified){
				return ((Identified)inner).getIdentifier();
			}
			return inner.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((inner == null) ? 0 : inner.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			UIWrapper other = (UIWrapper) obj;
			if (inner == null) {
				if (other.inner != null)
					return false;
			} else if (!inner.equals(other.inner))
				return false;
			return true;
		}

		@Override
		public ContentBuilder addContent(ContentBuilder builder) {
			return inner.addContent(builder);
		}

		public UIWrapper(UIGenerator inner) {
			super();
			this.inner = inner;
		}

		private final UIGenerator inner;

		@Override
		public int compareTo(UIWrapper o) {
			
			return toString().compareTo(o.toString());
		}
	}
	public Object getLabel(AppContext conn,D val){
		if( val instanceof UIGenerator){
			return new UIWrapper((UIGenerator)val);
		}
		if( val instanceof Identified){
			return ((Identified)val).getIdentifier(conn.getIntegerParameter("referencelabeller.max_identified", 32));
		}
		return val.toString();
	}
	public boolean accepts(Object o) {
		if( o != null && o instanceof IndexedReference){
			return true;
		}
		return false;
	}
}