package uk.ac.ed.epcc.safe.accounting.reports;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.OverlapHandler;
import uk.ac.ed.epcc.webapp.junit4.ConfigFixtures;

@ConfigFixtures("noquerymapper.properties")
public class NoQueryMapperChartExtenstionTest extends ChartExtensionTest {

	public NoQueryMapperChartExtenstionTest() {
		// TODO Auto-generated constructor stub
	}

	@Test
	public void checkFeatures() {
		assertFalse(OverlapHandler.USE_QUERY_MAPPER_FEATURE.isEnabled(ctx));
	}
	
	@Override
	public boolean expectDistictSupported() {
		// We cant do a DISTICT reduction without a query-mapper 
		return false;
	}
}
