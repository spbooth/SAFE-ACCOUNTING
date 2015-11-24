// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.db.transitions;

import uk.ac.ed.epcc.webapp.content.ContentBuilder;
import uk.ac.ed.epcc.webapp.session.SessionService;
/** interface to allow parsers and policies to add info to
 * the summary table.
 * 
 * @author spb
 *
 */
public interface SummaryProvider {
	public void getTableTransitionSummary(ContentBuilder hb,SessionService operator);
}