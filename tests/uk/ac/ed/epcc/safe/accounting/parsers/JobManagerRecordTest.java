/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers;

import java.util.ArrayList;
import java.util.Collection;

import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.SkipRecord;

public class JobManagerRecordTest extends AbstractRecordTestCase{
	
	private static final Collection<RecordText> goodRecords = new ArrayList<RecordText>();
	private static final Collection<BadRecordText> badTexts = new ArrayList<BadRecordText>();
	public JobManagerRecordTest(){
		super("PBSMachine", "JobManager");
	}
	/**
	 * Tests are not going to be altered at runtime so we declare them all in a
	 * static block and add them to either the good records or bad records
	 * collection
	 */
	static {
		String validRecords[] = new String[] {
				"\"localUser=90069\" \"userDN=/C=DE/O=GridGermany/OU=Forschungszentrum Juelich GmbH/CN=Heinke Frerichs\" \"userFQAN=/euforia/Role=NULL/Capability=NULL\" \"jobID=https://i2g-rb01.lip.pt:9000/RNA3ptXR91dI1qbR_4Rh3g\" \"ceID=iwrce2.fzk.de:2119/jobmanager-lcgpbs-i2gpar\" \"lrmsID=710078.iwrcgpbs0.fzk.de\" \"timestamp=2009-06-10 06:45:08\"",
				"\"localUser=90069\" \"userDN=/C=DE/O=GridGermany/OU=Forschungszentrum Juelich GmbH/CN=Heinke Frerichs\" \"userFQAN=/euforia/Role=NULL/Capability=NULL\" \"jobID=https://i2g-rb01.lip.pt:9000/jP7sqe8z4IpB8FQ5QJFaOQ\" \"ceID=iwrce2.fzk.de:2119/jobmanager-lcgpbs-i2gpar\" \"lrmsID=710079.iwrcgpbs0.fzk.de\" \"timestamp=2009-06-10 06:45:39\"",
				"\"localUser=90069\" \"userDN=/C=DE/O=GridGermany/OU=Forschungszentrum Juelich GmbH/CN=Heinke Frerichs\" \"userFQAN=/euforia/Role=NULL/Capability=NULL\" \"jobID=https://i2g-rb01.lip.pt:9000/7Nt-8Rg_Eo6NT3cpnPhXGA\" \"ceID=iwrce2.fzk.de:2119/jobmanager-lcgpbs-i2gpar\" \"lrmsID=710080.iwrcgpbs0.fzk.de\" \"timestamp=2009-06-10 06:45:45\"",
				"\"localUser=90069\" \"userDN=/C=DE/O=GridGermany/OU=Forschungszentrum Juelich GmbH/CN=Heinke Frerichs\" \"userFQAN=/euforia/Role=NULL/Capability=NULL\" \"jobID=https://i2g-rb01.lip.pt:9000/QFGqhsR9UHKM2N0z1yVdRw\" \"ceID=iwrce2.fzk.de:2119/jobmanager-lcgpbs-i2gpar\" \"lrmsID=710081.iwrcgpbs0.fzk.de\" \"timestamp=2009-06-10 06:45:49\"",
				"\"localUser=90064\" \"userDN=/C=DE/O=GermanGrid/OU=FZK/CN=Rainer Stotzka\" \"userFQAN=/euforia/Role=NULL/Capability=NULL\" \"jobID=https://i2g-rb01.lip.pt:9000/FjPpgxOfzdbiy16mPbo6Tw\" \"ceID=iwrce2.fzk.de:2119/jobmanager-lcgpbs-i2gpar\" \"lrmsID=710083.iwrcgpbs0.fzk.de\" \"timestamp=2009-06-10 07:06:02\"",
				"\"localUser=90069\" \"userDN=/C=DE/O=GridGermany/OU=Forschungszentrum Juelich GmbH/CN=Heinke Frerichs\" \"userFQAN=/euforia/Role=NULL/Capability=NULL\" \"jobID=https://i2g-rb01.lip.pt:9000/tqZT5YblZee67bI_tsgJTA\" \"ceID=iwrce2.fzk.de:2119/jobmanager-lcgpbs-i2gpar\" \"lrmsID=710090.iwrcgpbs0.fzk.de\" \"timestamp=2009-06-10 09:03:34\"",
				"\"localUser=90069\" \"userDN=/C=DE/O=GridGermany/OU=Forschungszentrum Juelich GmbH/CN=Heinke Frerichs\" \"userFQAN=/euforia/Role=NULL/Capability=NULL\" \"jobID=https://i2g-rb01.lip.pt:9000/EPwo93mttquzCcY8wyuxlw\" \"ceID=iwrce2.fzk.de:2119/jobmanager-lcgpbs-i2gpar\" \"lrmsID=710091.iwrcgpbs0.fzk.de\" \"timestamp=2009-06-10 09:03:40\"",
				"\"localUser=90069\" \"userDN=/C=DE/O=GridGermany/OU=Forschungszentrum Juelich GmbH/CN=Heinke Frerichs\" \"userFQAN=/euforia/Role=NULL/Capability=NULL\" \"jobID=https://i2g-rb01.lip.pt:9000/RxCpgaJOSMtoFCeC52wRug\" \"ceID=iwrce2.fzk.de:2119/jobmanager-lcgpbs-i2gpar\" \"lrmsID=710092.iwrcgpbs0.fzk.de\" \"timestamp=2009-06-10 09:03:40\"",
				"\"localUser=90069\" \"userDN=/C=DE/O=GridGermany/OU=Forschungszentrum Juelich GmbH/CN=Heinke Frerichs\" \"userFQAN=/euforia/Role=NULL/Capability=NULL\" \"jobID=https://i2g-rb01.lip.pt:9000/dZ8wpuD_GQwKwD51lIEL8g\" \"ceID=iwrce2.fzk.de:2119/jobmanager-lcgpbs-i2gpar\" \"lrmsID=710093.iwrcgpbs0.fzk.de\" \"timestamp=2009-06-10 09:03:45\"",
				"\"localUser=90064\" \"userDN=/C=DE/O=GermanGrid/OU=FZK/CN=Rainer Stotzka\" \"userFQAN=/euforia/Role=NULL/Capability=NULL\" \"jobID=https://i2g-rb01.lip.pt:9000/zvUe1hR-YPrJ4o7mqm57ag\" \"ceID=iwrce2.fzk.de:2119/jobmanager-lcgpbs-i2gpar\" \"lrmsID=710101.iwrcgpbs0.fzk.de\" \"timestamp=2009-06-10 12:16:14\"",
				"\"localUser=90064\" \"userDN=/C=DE/O=GermanGrid/OU=FZK/CN=Rainer Stotzka\" \"userFQAN=/euforia/Role=NULL/Capability=NULL\" \"jobID=https://i2g-rb01.lip.pt:9000/zvUe1hR-YPrJ4o7mqm57ag\" \"ceID=iwrce2.fzk.de:2119/jobmanager-lcgpbs-i2gpar\" \"lrmsID=710102.iwrcgpbs0.fzk.de\" \"timestamp=2009-06-10 12:24:49\"",
				"\"localUser=90073\" \"userDN=/C=UK/O=eScience/OU=Edinburgh/L=NeSC/CN=adrian jackson\" \"jobID=https://i2g-rb01.lip.pt:9000/p_BpiMuhSIIMqAzDCWIYHQ\" \"ceID=iwrce2.fzk.de:2119/jobmanager-lcgpbs-i2gpar\" \"lrmsID=710105.iwrcgpbs0.fzk.de\" \"timestamp=2009-06-10 13:20:10\"",
				"\"localUser=90073\" \"userDN=/C=UK/O=eScience/OU=Edinburgh/L=NeSC/CN=adrian jackson\" \"jobID=https://i2g-rb01.lip.pt:9000/YXbbnokgvC55lfMtRx9_og\" \"ceID=iwrce2.fzk.de:2119/jobmanager-lcgpbs-i2gpar\" \"lrmsID=710107.iwrcgpbs0.fzk.de\" \"timestamp=2009-06-10 13:47:42\""
				};
		for (String record : validRecords)
			goodRecords.add(new RecordText(record));

		/*
		 * Records this parser shouldn't parse because they are the wrong type.
		 * Should throw SkipRecord
		 */
		String skippedRecords[] = new String[] {
				
				 };
		for (String record : skippedRecords)
			badTexts.add(new BadRecordText(record, SkipRecord.class));

		/*
		 * Make sure records with unknown types aren't parsed
		 */
		String badRecordTypeRecords[] = new String[] {
				};
		for (String record : badRecordTypeRecords)
			badTexts.add(new BadRecordText(record,
					SkipRecord.class));

		/*
		 * Make sure records without enough fields cause the parser to fail
		 * appropriately
		 */
		String tooFewFields = "";
		badTexts.add(new BadRecordText(tooFewFields,
				AccountingParseException.class));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.epcc.safe.accounting.parsers.AbstractRecordTest#getBadRecords()
	 */
	@Override
	public Collection<BadRecordText> getBadRecords() {
		return badTexts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.epcc.safe.accounting.parsers.AbstractRecordTest#getGoodRecords
	 * ()
	 */
	@Override
	public Collection<RecordText> getGoodRecords() {
		return goodRecords;
	}
	

}
