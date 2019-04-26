// Copyright - The University of Edinburgh 2015
package uk.ac.ed.epcc.safe.accounting.reports;

import uk.ac.ed.epcc.webapp.WebappTestBase;

public abstract class ExtensionTestCase extends WebappTestBase {

	/**
	 * @param output
	 * @return
	 */
	public final String normalise(String output) {
		return output.replaceAll("\r?\n", "\n").replaceAll(", ", ",");
	}

	public String getOutputDir(){
		return ctx.getInitParameter("test.output.dir","src/test/java/uk/ac/ed/epcc/safe/accounting/reports/output/");
	}
}