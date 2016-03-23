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
package uk.ac.ed.epcc.safe.accounting.reference;
import uk.ac.ed.epcc.safe.accounting.charts.ReferenceLabeller;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.webapp.content.FormatProvider;
import uk.ac.ed.epcc.webapp.content.Labeller;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
/** A Property tag for a quantity that encodes a reference to an entry in
 * a database table
 * 
 * @author spb
 * @param <D> type of dataObject
 * @param <F> type of DataObjectFactory
 *
 */


public class ReferenceTag<D extends DataObject, F extends DataObjectFactory> extends IndexedTag<D,F> implements  FormatProvider<IndexedReference<D>,Object>{
	
   public ReferenceTag(PropertyRegistry reg,String name, Class<? extends F> fac, String table){
	   super(reg,name,fac,table,"Reference to "+table);
   }
   public ReferenceTag(PropertyRegistry reg,String name, Class<? extends F> fac, String table,String desc){
	   super(reg,name,fac,table,desc);
   }
//   public ReferenceTag(PropertyRegistry reg,String name, Class<F> fac){
//	   super(reg,name,fac,null,"Reference to "+fac.getSimpleName());
//   }
   

   public Labeller<IndexedReference<D>,Object> getLabeller() {

	   return new ReferenceLabeller<D>();
   }

 
}