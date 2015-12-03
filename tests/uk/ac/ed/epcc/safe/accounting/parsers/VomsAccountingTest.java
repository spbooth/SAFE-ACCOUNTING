// Copyright - The University of Edinburgh 2015
/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers;

import uk.ac.ed.epcc.safe.accounting.db.ConfigUsageRecordFactory;
import uk.ac.ed.epcc.safe.accounting.db.ParseUsageRecordFactory;
import uk.ac.ed.epcc.safe.accounting.db.ParseUsageRecordFactoryTestCase;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;

public class VomsAccountingTest extends ParseUsageRecordFactoryTestCase {



	@Override
	public PropertyMap getDefaults() {
		PropertyMap defaults = new PropertyMap();
        defaults.setProperty(StandardProperties.MACHINE_NAME_PROP, "Hector");
		return defaults;
	}
	
	@Override
	public ParseUsageRecordFactory getFactory() {
		return new ConfigUsageRecordFactory(ctx,"VomsAccounting");
	}
	@Override
	public boolean expectData(){
		return true;
	}

	@Override
	public String getUpdateText() {
		return 
"jid:2010-05-30.00:24:11.0000012793.0000000000.jobmanager-pbs dn:\"/C=UK/O=eScience/OU=CLRC/L=RAL/CN=inca2.ngs.ac.uk/emailAddress=support@grid-support.ac.uk\" uid:618 vo0:\"vomss://voms.ngs.ac.uk:8443/voms/ngs.ac.uk\" poolindex:\"%2fc%3duk%2fo%3descience%2fou%3dclrc%2fl%3dral%2fcn%3dinca2%2engs%2eac%2euk%2femailaddress%3dsupport%40grid%2dsupport%2eac%2euk\"\n"+
"jid:2010-05-30.00:24:12.0000012813.0000000000.jobmanager-pbs dn:\"/C=UK/O=eScience/OU=CLRC/L=RAL/CN=inca2.ngs.ac.uk/emailAddress=support@grid-support.ac.uk\" uid:618 vo0:\"vomss://voms.ngs.ac.uk:8443/voms/ngs.ac.uk\" poolindex:\"%2fc%3duk%2fo%3descience%2fou%3dclrc%2fl%3dral%2fcn%3dinca2%2engs%2eac%2euk%2femailaddress%3dsupport%40grid%2dsupport%2eac%2euk\"\n"+
"jid:2010-05-30.00:24:14.0000012835.0000000000.jobmanager-pbs dn:\"/C=UK/O=eScience/OU=CLRC/L=RAL/CN=inca2.ngs.ac.uk/emailAddress=support@grid-support.ac.uk\" uid:618 vo0:\"vomss://voms.ngs.ac.uk:8443/voms/ngs.ac.uk\" poolindex:\"%2fc%3duk%2fo%3descience%2fou%3dclrc%2fl%3dral%2fcn%3dinca2%2engs%2eac%2euk%2femailaddress%3dsupport%40grid%2dsupport%2eac%2euk\"\n"+
"jid:2010-05-30.00:24:14.0000012855.0000000000.jobmanager-pbs dn:\"/C=UK/O=eScience/OU=CLRC/L=RAL/CN=inca2.ngs.ac.uk/emailAddress=support@grid-support.ac.uk\" uid:618 vo0:\"vomss://voms.ngs.ac.uk:8443/voms/ngs.ac.uk\" poolindex:\"%2fc%3duk%2fo%3descience%2fou%3dclrc%2fl%3dral%2fcn%3dinca2%2engs%2eac%2euk%2femailaddress%3dsupport%40grid%2dsupport%2eac%2euk\"\n"+
"jid:2010-05-30.00:25:02.0000013718.0000000000.jobmanager-fork dn:\"/C=UK/O=eScience/OU=CLRC/L=RAL/CN=inca2.ngs.ac.uk/emailAddress=support@grid-support.ac.uk\" uid:618 vo0:\"vomss://voms.ngs.ac.uk:8443/voms/ngs.ac.uk\" poolindex:\"%2fc%3duk%2fo%3descience%2fou%3dclrc%2fl%3dral%2fcn%3dinca2%2engs%2eac%2euk%2femailaddress%3dsupport%40grid%2dsupport%2eac%2euk\"\n"+
"jid:2010-05-30.00:25:23.0000014359.0000000000.jobmanager dn:\"/C=UK/O=eScience/OU=CLRC/L=RAL/CN=inca2.ngs.ac.uk/emailAddress=support@grid-support.ac.uk\" uid:618 vo0:\"vomss://voms.ngs.ac.uk:8443/voms/ngs.ac.uk\" poolindex:\"%2fc%3duk%2fo%3descience%2fou%3dclrc%2fl%3dral%2fcn%3dinca2%2engs%2eac%2euk%2femailaddress%3dsupport%40grid%2dsupport%2eac%2euk\"\n"+
"jid:2010-05-30.00:29:01.0000017446.0000000000.jobmanager-pbs dn:\"/C=UK/O=eScience/OU=CLRC/L=RAL/CN=inca2.ngs.ac.uk/emailAddress=support@grid-support.ac.uk\" uid:618 vo0:\"vomss://voms.ngs.ac.uk:8443/voms/ngs.ac.uk\" poolindex:\"%2fc%3duk%2fo%3descience%2fou%3dclrc%2fl%3dral%2fcn%3dinca2%2engs%2eac%2euk%2femailaddress%3dsupport%40grid%2dsupport%2eac%2euk\"\n"+
"jid:2010-05-30.00:29:02.0000017466.0000000000.jobmanager-pbs dn:\"/C=UK/O=eScience/OU=CLRC/L=RAL/CN=inca2.ngs.ac.uk/emailAddress=support@grid-support.ac.uk\" uid:618 vo0:\"vomss://voms.ngs.ac.uk:8443/voms/ngs.ac.uk\" poolindex:\"%2fc%3duk%2fo%3descience%2fou%3dclrc%2fl%3dral%2fcn%3dinca2%2engs%2eac%2euk%2femailaddress%3dsupport%40grid%2dsupport%2eac%2euk\"\n"+
"jid:2010-05-30.00:31:27.0000019636.0000000000.jobmanager-fork dn:\"/C=UK/O=eScience/OU=CLRC/L=RAL/CN=inca2.ngs.ac.uk/emailAddress=support@grid-support.ac.uk\" uid:618 vo0:\"vomss://voms.ngs.ac.uk:8443/voms/ngs.ac.uk\" poolindex:\"%2fc%3duk%2fo%3descience%2fou%3dclrc%2fl%3dral%2fcn%3dinca2%2engs%2eac%2euk%2femailaddress%3dsupport%40grid%2dsupport%2eac%2euk\"\n"+
"jid:2010-05-30.00:31:27.0000019676.0000000000.jobmanager-fork dn:\"/C=UK/O=eScience/OU=CLRC/L=RAL/CN=inca2.ngs.ac.uk/emailAddress=support@grid-support.ac.uk\" uid:618 vo0:\"vomss://voms.ngs.ac.uk:8443/voms/ngs.ac.uk\" poolindex:\"%2fc%3duk%2fo%3descience%2fou%3dclrc%2fl%3dral%2fcn%3dinca2%2engs%2eac%2euk%2femailaddress%3dsupport%40grid%2dsupport%2eac%2euk\"\n"+
"jid:2010-05-30.00:31:27.0000019657.0000000000.jobmanager-fork dn:\"/C=UK/O=eScience/OU=CLRC/L=RAL/CN=inca2.ngs.ac.uk/emailAddress=support@grid-support.ac.uk\" uid:618 vo0:\"vomss://voms.ngs.ac.uk:8443/voms/ngs.ac.uk\" poolindex:\"%2fc%3duk%2fo%3descience%2fou%3dclrc%2fl%3dral%2fcn%3dinca2%2engs%2eac%2euk%2femailaddress%3dsupport%40grid%2dsupport%2eac%2euk\"\n"+
"jid:2010-05-30.00:31:28.0000019704.0000000000.jobmanager-fork dn:\"/C=UK/O=eScience/OU=CLRC/L=RAL/CN=inca2.ngs.ac.uk/emailAddress=support@grid-support.ac.uk\" uid:618 vo0:\"vomss://voms.ngs.ac.uk:8443/voms/ngs.ac.uk\" poolindex:\"%2fc%3duk%2fo%3descience%2fou%3dclrc%2fl%3dral%2fcn%3dinca2%2engs%2eac%2euk%2femailaddress%3dsupport%40grid%2dsupport%2eac%2euk\"\n"+
"jid:2010-05-30.00:31:52.0000020720.0000000000.jobmanager dn:\"/C=UK/O=eScience/OU=CLRC/L=RAL/CN=inca2.ngs.ac.uk/emailAddress=support@grid-support.ac.uk\" uid:618 vo0:\"vomss://voms.ngs.ac.uk:8443/voms/ngs.ac.uk\" poolindex:\"%2fc%3duk%2fo%3descience%2fou%3dclrc%2fl%3dral%2fcn%3dinca2%2engs%2eac%2euk%2femailaddress%3dsupport%40grid%2dsupport%2eac%2euk\""+
"jid:2010-05-30.00:21:35.0000004594.0000010010.ssh dn:\"/C=UK/O=eScience/OU=CLRC/L=RAL/CN=inca2.ngs.ac.uk/emailAddress=support@grid-support.ac.uk\" uid:618 vo0:\"vomss://voms.ngs.ac.uk:8443/voms/ngs.ac.uk\" poolindex:\"%2fc%3duk%2fo%3descience%2fou%3dclrc%2fl%3dral%2fcn%3dinca2%2engs%2eac%2euk%2femailaddress%3dsupport%40grid%2dsupport%2eac%2euk\""+
"jid:2010-05-30.00:21:35.0000004594.0000010010.ssh dn:\"/C=UK/O=eScience/OU=CLRC/L=RAL/CN=inca2.ngs.ac.uk/emailAddress=support@grid-support.ac.uk\" uid:618 vo0:\"vomss://voms.ngs.ac.uk:8443/voms/ngs.ac.uk\" poolindex:\"%2fc%3duk%2fo%3descience%2fou%3dclrc%2fl%3dral%2fcn%3dinca2%2engs%2eac%2euk%2femailaddress%3dsupport%40grid%2dsupport%2eac%2euk\"";
	}

}