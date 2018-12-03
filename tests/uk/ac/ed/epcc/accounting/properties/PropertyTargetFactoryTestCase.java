package uk.ac.ed.epcc.accounting.properties;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyTargetFactory;

public interface PropertyTargetFactoryTestCase<X extends PropertyTargetFactory>  {

	@Test
	public void testGetFinder();
	
	
}
