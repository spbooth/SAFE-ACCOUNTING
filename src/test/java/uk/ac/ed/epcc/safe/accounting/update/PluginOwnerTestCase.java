package uk.ac.ed.epcc.safe.accounting.update;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyTargetFactoryTestCase;
import uk.ac.ed.epcc.safe.accounting.update.PlugInOwner;



public interface PluginOwnerTestCase<R,X extends PlugInOwner<R>> extends PropertyTargetFactoryTestCase<X> {
	@Test
	public void testGetParser();
	
	@Test
	public void testGetPolicies();
	
	
	@Test
	public void testGetDerivedProperties();
	
	public abstract X getPluginOwner();
	
}
