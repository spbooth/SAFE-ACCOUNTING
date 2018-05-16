// Copyright - The University of Edinburgh 2015
package uk.ac.ed.epcc.safe.accounting.db;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionCast;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidExpressionException;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidSQLPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactoryTestCase;
import uk.ac.ed.epcc.webapp.model.data.Exceptions.DataFault;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;

public abstract class DataObjectPropertyFactoryTestCase<D extends DataObjectFactory<O>,O extends DataObjectPropertyContainer> extends
DataObjectFactoryTestCase<D, O> {


	@SuppressWarnings("unchecked")
	@Test
	public void testGetProperties(){
		DataObjectFactory fac = (DataObjectFactory)getFactory();
		if( fac.isValid()){
			ExpressionTargetFactory<O> etf = ExpressionCast.getExpressionTargetFactory(fac);
			assertNotNull(etf);
			Set<PropertyTag> set = etf.getAccessorMap().getProperties();
			for(PropertyTag t : set){
				boolean has = etf.hasProperty(t);

				boolean exp;
				try {
					exp = (etf.getAccessorMap().getSQLExpression(t)!=null);
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
		DataObjectFactory<O> fac = getFactory();
		if( fac.isValid()){
			ExpressionTargetFactory<O> etf = ExpressionCast.getExpressionTargetFactory(fac);
			assertNotNull(etf);
			Set<PropertyTag> set = etf.getAccessorMap().getProperties();
			O dat =  fac.makeBDO();
			ExpressionTargetContainer proxy = etf.getExpressionTarget(dat);
			for(PropertyTag<?> t : set){
				boolean has = etf.hasProperty(t);

				boolean supports = proxy.supports(t);
				boolean writable = proxy.writable(t);
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
					Object property = proxy.getProperty(t);
					if( t.getTarget() == IndexedReference.class){
						assertNotNull("indexed reference property returned null",property);
						assertTrue("refernce not null for "+t.getFullName(),((IndexedReference)property).isNull());
					}else if( t.getTarget() ==  Boolean.class){
						assertTrue("Null boolean defautls true",((Boolean)property));
					}else{
						assertNull("property "+t.getFullName()+" "+etf.getAccessorMap().getImplemenationInfo(t)+" generates "+property,property);
					}
				}
			}
		}
	}

	@Test
	public void testGetAccessorMap(){
		DataObjectFactory fac = (DataObjectFactory)getFactory();
		if( fac.isValid()){
			ExpressionTargetFactory<O> etf = ExpressionCast.getExpressionTargetFactory(fac);
			assertNotNull(etf);
			AccessorMap map = etf.getAccessorMap();
			assertNotNull(map);
		}
	}
}