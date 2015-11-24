// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting;

import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.AppContextService;
import uk.ac.ed.epcc.webapp.session.SessionService;

/** Service used to configure the Accounting
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: AccountingService.java,v 1.16 2014/09/15 14:32:17 spb Exp $")

public interface AccountingService extends AppContextService<AccountingService>{
	/** Get the default {@link UsageManager}
	 * 
	 * @return {@link UsageManager}
	 */
	public UsageManager getUsageManager();
	/** Get a named non-default {@link UsageManager}
	 * 
	 * @param name
	 * @return {@link UsageManager}
	 */
	public UsageManager getUsageManager(String name);
	/** Get the default {@link UsageProducer}
	 * Usually this will return the same as {@link #getUsageManager()}
	 * @return {@link UsageProducer}
	 */
	
	public UsageProducer getUsageProducer();
	/** Get a named {@link UsageProducer}
	 * 
	 * A name of the format <em>manager:producer</em> will select a specific
	 * producer from a named manager. A bare name may return a manager or 
	 * generate the {@link UsageProducer} directly using the tag as a name.
	 * 
	 * @param name
	 * @return {@link UsageProducer}
	 */
	public UsageProducer getUsageProducer(String name);
	/** Does the User have access to all the specified records
	 * 
	 * @param user AppUser making request
	 * @param sel RecordSelector requested
	 * @return boolean
	 */
	public boolean allow(SessionService user, RecordSelector sel);
}