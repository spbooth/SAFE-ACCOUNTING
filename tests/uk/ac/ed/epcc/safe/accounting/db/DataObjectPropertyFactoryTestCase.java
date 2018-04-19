// Copyright - The University of Edinburgh 2015
package uk.ac.ed.epcc.safe.accounting.db;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidSQLPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactoryTestCase;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;

public abstract class DataObjectPropertyFactoryTestCase<D extends DataObjectPropertyFactory<O>,O extends DataObjectPropertyContainer> extends
		DataObjectFactoryTestCase<D, O> {

	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetProperties(){
		DataObjectPropertyFactory fac = (DataObjectPropertyFactory)getFactory();
		if( fac.isValid()){
		Set<PropertyTag> set = fac.getProperties();
		for(PropertyTag t : set){
			boolean has = fac.hasProperty(t);
			
			boolean exp;
			try {
				exp = (fac.getAccessorMap().getSQLExpression(t)!=null);
			} catch (InvalidSQLPropertyException e) {
				exp = false;
			}
			if( exp){
				assertTrue(has);
			}
			
			if( ! has ){
				assertFalse(exp);
				
			}
			
		}
		}
	}
	@Test
	public void testNullRecord() throws InvalidExpressionException, DataFault{
		DataObjectPropertyFactory<O> fac = getFactory();
		if( fac.isValid()){
		Set<PropertyTag> set = fac.getProperties();
		O dat =  fac.makeBDO();
		for(PropertyTag<?> t : set){
			boolean has = fac.hasProperty(t);
			
			boolean supports = dat.supports(t);
			boolean writable = dat.writable(t);
			if( ! has ){
				assertFalse(t.getFullName(),supports);
				assertFalse(t.getFullName(),writable);
			}
			if( writable){
				assertTrue("writable "+t.getFullName()+" should be supported",supports);
			}
			// Some properties are constant values so 
			// only check those we can write
			if( writable ){
				Object property = dat.getProperty(t);
				if( t.getTarget() == IndexedReference.class){
					assertNotNull("indexed reference property returned null",property);
					assertTrue("refernce not null for "+t.getFullName(),((IndexedReference)property).isNull());
				}else if( t.getTarget() ==  Boolean.class){
					assertTrue("Null boolean defautls true",((Boolean)property));
				}else{
					assertNull("property "+t.getFullName()+" "+fac.getAccessorMap().getImplemenationInfo(t)+" generates "+property,property);
				}
			}
		}
		}
	}
	
	@Test
	public void testGetAccessorMap(){
		DataObjectPropertyFactory fac = (DataObjectPropertyFactory)getFactory();
		if( fac.isValid()){
		AccessorMap map = fac.getAccessorMap();
		assertNotNull(map);
		}
	}
}