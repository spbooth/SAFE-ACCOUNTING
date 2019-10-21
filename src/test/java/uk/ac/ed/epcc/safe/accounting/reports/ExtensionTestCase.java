// Copyright - The University of Edinburgh 2015
package uk.ac.ed.epcc.safe.accounting.reports;

import org.junit.Before;

import uk.ac.ed.epcc.webapp.WebappTestBase;
import uk.ac.ed.epcc.webapp.session.AbstractSessionService;

public abstract class ExtensionTestCase extends WebappTestBase {
	@Before
	public void setRoleTable() {
		AbstractSessionService.setupRoleTable(ctx);
	}
	/**
	 * @param output
	 * @return
	 */
	public String normalise(String output) {
		return output.replaceAll("\\s*\r?\n", "\n").replaceAll(", ", ",");
	}

	public String getOutputDir(){
		return ctx.getInitParameter("test.output.dir","src/test/resources/uk/ac/ed/epcc/safe/accounting/reports/output/");
	}
}