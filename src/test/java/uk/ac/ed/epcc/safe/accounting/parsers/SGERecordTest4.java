// Copyright - The University of Edinburgh 2015
/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers;

import java.io.IOException;

import org.junit.Before;

import uk.ac.ed.epcc.safe.accounting.db.ConfigUsageRecordFactory;
import uk.ac.ed.epcc.safe.accounting.db.ParseUsageRecordFactoryTestCase;
import uk.ac.ed.epcc.safe.accounting.db.UsageRecordFactory;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.webapp.junit4.ConfigFixtures;

@ConfigFixtures("aggregate.properties")
public class SGERecordTest4 extends ParseUsageRecordFactoryTestCase{

	@Before
	public void loadData() throws Exception{
		// Table is auto created in this case
		load("Eddie2.xml");
	}
	

	@Override
	public PropertyMap getDefaults() {
		PropertyMap defaults = new PropertyMap();
        defaults.setProperty(StandardProperties.MACHINE_NAME_PROP, "Eddie");
		return defaults;
	}

	@Override
	public UsageRecordFactory getFactory() {
		return new ConfigUsageRecordFactory(ctx,"SGERecord");
	}


	@Override
	public String getUpdateText() throws IOException {
		return getResourceAsString("sge_accounting.txt");
	
	}


	@Override
	public String getReceiveAccountingExpected() {
		
		return "sge_expected4.xml";
	}

}