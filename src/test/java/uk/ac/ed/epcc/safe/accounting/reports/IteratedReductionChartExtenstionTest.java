package uk.ac.ed.epcc.safe.accounting.reports;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.OverlapHandler;
import uk.ac.ed.epcc.safe.accounting.db.ReductionHandler;
import uk.ac.ed.epcc.webapp.junit4.ConfigFixtures;

@ConfigFixtures("iteratedreduction.properties")
public class IteratedReductionChartExtenstionTest extends ChartExtensionTest {

	public IteratedReductionChartExtenstionTest() {
		// TODO Auto-generated constructor stub
	}

	@Test
	public void checkFeatures() {
		assertTrue(OverlapHandler.USE_QUERY_MAPPER_FEATURE.isEnabled(ctx));
		assertFalse(ReductionHandler.FILTER_REDUCTION.isEnabled(ctx));
	}

	
}
