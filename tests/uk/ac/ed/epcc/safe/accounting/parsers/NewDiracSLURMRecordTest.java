// Copyright - The University of Edinburgh 2015
package uk.ac.ed.epcc.safe.accounting.parsers;

import java.util.ArrayList;
import java.util.Collection;

import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.SkipRecord;

public class NewDiracSLURMRecordTest extends AbstractRecordTestCase {
	private static final Collection<RecordText> goodRecords = new ArrayList<RecordText>();
	private static final Collection<BadRecordText> badTexts = new ArrayList<BadRecordText>();

	public NewDiracSLURMRecordTest() {
		super("SLURMMachine", "SLURMRecord");
	}

	/**
	 * Tests are not going to be altered at runtime so we declare them all in a
	 * static block and add them to either the good records or bad records
	 * collection
	 */
	static {
		String validRecords[] = new String[] {
				// Wilkes2-GPU
				"Cluster=csd3 JobID=137251 User=dc-mant1 Group=dp060 Account=dirac-dp060-gpu "+
				"JobName=c2h4_initial_vib_2016_intense_j41_j42_sym4 Partition=pascal Submit=2017-10-07T20:48:03 "+
				"Reserved=00:39:43 Start=2017-10-07T21:27:46 End=2017-10-07T22:52:33 Elapsed=01:24:47 NNodes=7 "+
				"NCPUS=84 Timelimit=07:00:00 ReqMem=8000Mc AllocTRES=cpu=84,mem=672000M,node=7,gres/gpu=28 "+
				"CPUTimeRAW=427308 ExitCode=0:0 State=COMPLETED\n",

			// CSD3-KNL
				"Cluster=csd3 JobID=136785 User=dc-coss2 Group=dp083 Account=dirac-dp008-knl JobName=32_k0.9 "+
				"Partition=knl Submit=2017-10-04T16:54:17 Reserved=00:00:00 Start=2017-10-07T21:38:05 End=2017-10-"+
				"07T21:39:50 Elapsed=00:01:45 NNodes=64 NCPUS=16384 Timelimit=12:00:00 ReqMem=375Mc "+
				"AllocTRES=cpu=16384,mem=6000G,node=64 CPUTimeRAW=1720320 ExitCode=127:0 State=FAILED\n",

				// CSD3-CPU
				"Cluster=csd3 JobID=116244 User=sjr20 Group=sjr20 Account=support-cpu JobName=_interactive "+
				"Partition=skylake Submit=2017-09-06T13:36:42 Reserved=00:00:00 Start=2017-09-06T13:36:42 End=2017-"+
				"09-06T13:42:41 Elapsed=00:05:59 NNodes=4 NCPUS=128 Timelimit=01:00:00 ReqMem=6020Mc "+
				"AllocTRES=cpu=128,mem=752.50G,node=4 CPUTimeRAW=45952 ExitCode=0:0 State=CANCELLED by 628\n" 
				
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
					IllegalArgumentException.class));

		/*
		 * Make sure records without enough fields cause the parser to fail
		 * appropriately
		 */
		String tooFewFields = "01/01/2009 12:00:00;E;";
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