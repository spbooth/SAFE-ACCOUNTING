// Copyright - The University of Edinburgh 2015
package uk.ac.ed.epcc.safe.accounting.reports;

import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactory;
import uk.ac.ed.epcc.webapp.model.data.DataObjectFactoryTestCase;

public class TemplateOverlayTest extends DataObjectFactoryTestCase {

	

	@Override
	public DataObjectFactory getFactory()  {
		try {
			return new TemplateOverlay(ctx, "ReportTemplates");
		} catch (Exception e) {
			throw new ConsistencyError("Error making overlay", e);
		}
	}

}