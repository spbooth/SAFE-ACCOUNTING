// Copyright - The University of Edinburgh 2011
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
@uk.ac.ed.epcc.webapp.Version("$Id: ReferenceTag.java,v 1.8 2014/09/15 14:32:27 spb Exp $")

public class ReferenceTag<D extends DataObject, F extends DataObjectFactory> extends IndexedTag<D,F> implements  FormatProvider<IndexedReference<D>,String>{
	
   public ReferenceTag(PropertyRegistry reg,String name, Class<? extends F> fac, String table){
	   super(reg,name,fac,table,"Reference to "+table);
   }
   public ReferenceTag(PropertyRegistry reg,String name, Class<? extends F> fac, String table,String desc){
	   super(reg,name,fac,table,desc);
   }
//   public ReferenceTag(PropertyRegistry reg,String name, Class<F> fac){
//	   super(reg,name,fac,null,"Reference to "+fac.getSimpleName());
//   }
   

   public Labeller<IndexedReference<D>,String> getLabeller() {

	   return new ReferenceLabeller<D>();
   }

 
}