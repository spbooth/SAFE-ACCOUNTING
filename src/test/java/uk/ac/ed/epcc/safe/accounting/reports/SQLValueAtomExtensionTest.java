package uk.ac.ed.epcc.safe.accounting.reports;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.OverlapHandler;
import uk.ac.ed.epcc.safe.accounting.db.AccessorMap;
import uk.ac.ed.epcc.webapp.junit4.ConfigFixtures;

@ConfigFixtures("sqlvalue.properties")
public class SQLValueAtomExtensionTest extends AtomExtensionTest {

	public SQLValueAtomExtensionTest() {
	}

	
	@Test
	public void checkFeatures() {
		assertTrue(AccessorMap.FORCE_SQLVALUE_FEATURE.isEnabled(ctx));
		assertTrue(OverlapHandler.USE_QUERY_MAPPER_FEATURE.isEnabled(ctx));
		assertTrue(OverlapHandler.USE_CASE_OVERLAP.isEnabled(ctx));
	}
}
