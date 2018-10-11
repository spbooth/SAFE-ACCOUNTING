package uk.ac.ed.epcc.safe.accounting.parsers;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.db.TupleUsageProducer;
import uk.ac.ed.epcc.webapp.WebappTestBase;
import uk.ac.ed.epcc.webapp.junit4.DataBaseFixtures;
@DataBaseFixtures({"RurParserTest.xml","lassi_parse.xml"})
public class LassiTupleTest extends WebappTestBase {

	@Test
	public void testTuple() throws Exception {
		TupleUsageProducer prod = new TupleUsageProducer<>(ctx, "IOReportJoin");
		
		assertEquals(3, prod.getRecordCount(null));
	}
}
