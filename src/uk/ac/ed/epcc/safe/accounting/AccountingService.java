//| Copyright - The University of Edinburgh 2011                            |
//|                                                                         |
//| Licensed under the Apache License, Version 2.0 (the "License");         |
//| you may not use this file except in compliance with the License.        |
//| You may obtain a copy of the License at                                 |
//|                                                                         |
//|    http://www.apache.org/licenses/LICENSE-2.0                           |
//|                                                                         |
//| Unless required by applicable law or agreed to in writing, software     |
//| distributed under the License is distributed on an "AS IS" BASIS,       |
//| WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.|
//| See the License for the specific language governing permissions and     |
//| limitations under the License.                                          |
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting;

import uk.ac.ed.epcc.webapp.AppContextService;
import uk.ac.ed.epcc.webapp.Contexed;

/** Service used to configure the Accounting
 * @author spb
 *
 */


public interface AccountingService extends AppContextService<AccountingService>, Contexed{
	/** Get the default {@link UsageManager}
	 * 
	 * 
	 * Unless you need to process nested {@link UsageProducer}s explicitly you normally need
	 * {@link #getUsageProducer()}
	 * 
	 * @return {@link UsageManager}
	 */
	public UsageManager getUsageManager();
	/** Get a named non-default {@link UsageManager}
	 * 
	 * 
	 * Unless you need to process nested {@link UsageProducer}s explicitly you normally need
	 * {@link #getUsageProducer(String)}
	 * 
	 * @param name
	 * @return {@link UsageManager}
	 */
	public UsageManager getUsageManager(String name);
	/** Get the default {@link UsageProducer}
	 * Usually this will return the same as {@link #getUsageManager()} and will be a combination
	 * of the standard accounting tables defined using the <b>accounting</b> tag.
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
}