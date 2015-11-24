// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.policy;

import uk.ac.ed.epcc.safe.accounting.UsageRecord;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.update.UsageRecordPolicy;

public abstract class BaseUsageRecordPolicy extends BasePolicy implements
		UsageRecordPolicy {

	public void postCreate(PropertyContainer props, UsageRecord rec)
			throws Exception {
		

	}

	public void preDelete(UsageRecord rec) throws Exception {
		

	}

	public boolean allowReplace(PropertyContainer props, UsageRecord rec) {
		return true;
	}

	

}