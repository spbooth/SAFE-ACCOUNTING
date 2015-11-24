package uk.ac.ed.epcc.safe.accounting.reports;

import uk.ac.ed.epcc.webapp.WebappTestBase;

public abstract class ExtensionTestCase extends WebappTestBase {

	
	public String getOutputDir(){
		return ctx.getInitParameter("test.output.dir","tests/uk/ac/ed/epcc/safe/accounting/reports/output/");
	}
}
