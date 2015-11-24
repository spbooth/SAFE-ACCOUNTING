// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.reference;

import uk.ac.ed.epcc.safe.accounting.parsers.value.ReferenceValueParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParserProvider;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTarget;
//import uk.ac.ed.epcc.safe.accounting.expr.ReferenceExpression;
import uk.ac.ed.epcc.safe.accounting.selector.SelectClause;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Indexed;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedProducer;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
/** Tag for an IndexedTag type.
 * 
 * @author spb
 *
 * @param <I> Indexed type
 * @param <F> type of IndexedProducer
 */
@uk.ac.ed.epcc.webapp.Version("$Id: IndexedTag.java,v 1.8 2015/09/23 08:50:29 spb Exp $")

public class IndexedTag<I extends Indexed, F extends IndexedProducer> extends PropertyTag<IndexedReference<I>> implements ReferenceExpression<I>, ValueParserProvider<IndexedReference<I>>{
	   protected final Class<? extends F> fac;
	   protected final String table; // optional table parameter, may not be needed as the target class may have the table hardwired
	   				
	   public IndexedTag(PropertyRegistry reg,String name, Class<? extends F> fac, String table){
		   super(reg,name,IndexedReference.class);
		   this.fac=fac;
		   this.table=table;
	   }
	   public IndexedTag(PropertyRegistry reg,String name, Class<? extends F> fac, String table,String description){
		   super(reg,name,IndexedReference.class,description);
		   this.fac=fac;
		   this.table=table;
	   }
	   public IndexedTag(PropertyRegistry reg,String name, Class<? extends F> fac){
		   this(reg,name,fac,null);
	   }
	   public void set(PropertyContainer cont, I value) throws InvalidPropertyException{
		   cont.setProperty(this, makeReference(value));
	   }
	public IndexedReference<I> makeReference(I value) {
		if( value == null ){
			return new IndexedReference<I>(0,fac,table);
		}
		return new IndexedReference<I>(value.getID(),fac,table);
	}
	public IndexedReference<I> makeReference(int value) {
		if( value < 0){
			return new IndexedReference<I>(0,fac,table);
		}
		return new IndexedReference<I>(value,fac,table);
	}
	public SelectClause<IndexedReference<I>> makeSelect(I value){
		return new SelectClause<IndexedReference<I>>(this,makeReference(value));
	}
	   public void setOptional(PropertyContainer cont, I value) {
		   if( cont.supports(this)){
		        cont.setOptionalProperty(this,makeReference(value) );
		   }
	   }
	   public I get(AppContext c, PropertyTarget cont)  {
		   IndexedReference<I> prop = cont.getProperty(this,null);
		   if( prop != null && ! prop.isNull()){
			   return  prop.getIndexed(c);
		   }
		   return null;
	   }
	   public Class<? extends F> getFactoryClass(){
		   return fac;
	   }
	   
	   public String getTable(){
		   return table;
	   }
	   public F getFactory(AppContext c){
		   F result = c.makeObject(fac,table);
		   assert(result != null);
		   return result;
	   }
	  
	   
	@Override
	public boolean allow(Object o) {
		if( super.allow(o)){
			if( o == null){
				return true;
			}
			// can do more detailed check, only check assignable and don't check table because
			// the UsageREcordFactory could refine class and table from properties
			IndexedReference<?> ref = (IndexedReference)o;
			return fac.isAssignableFrom(ref.getFactoryClass());
		}
		return false;
	}
	public IndexedTag<I,F> copy(){
		return this;
	}
	public ValueParser<IndexedReference> getValueParser(AppContext c) {
		return new ReferenceValueParser<I>(c, getFactory(c));
	}
}