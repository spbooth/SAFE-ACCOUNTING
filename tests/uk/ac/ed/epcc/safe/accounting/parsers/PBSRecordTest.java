// Copyright - The University of Edinburgh 2015
/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers;

import java.util.ArrayList;
import java.util.Collection;

import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.SkipRecord;
import uk.ac.ed.epcc.webapp.AppContext;

/**
 * Adds records to test. Some are good records that the parser should be able to
 * parse successfully. Some aren't so good and should result in exceptions being
 * thrown
 * 
 * @author jgreen4
 * 
 */
public class PBSRecordTest extends AbstractRecordTestCase {

	private static final Collection<RecordText> goodRecords = new ArrayList<RecordText>();
	private static final Collection<BadRecordText> badTexts = new ArrayList<BadRecordText>();

	public PBSRecordTest() {
		super("PBSMachine", "PBSRecord");
	}
	
	/**
	 * Tests are not going to be altered at runtime so we declare them all in a
	 * static block and add them to either the good records or bad records
	 * collection
	 */
	static {
		String validRecords[] = new String[] {
				"08/12/2008 00:01:34;E;100559.sdb;user=shiv group=-default- account=y06 jobname=STDIN queue=par:n8c2_20m ctime=1218495664 qtime=1218495664 etime=1218495664 start=1218495693 exec_host=nid00007/24 session=19384 alt_id=2872 end=1218495694 Exit_status=0",
				"08/12/2008 00:04:14;E;100083.sdb;user=msseay group=-default- account=e71-DQ jobname=fnp_equil.pbs queue=par:n32c2_12h ctime=1218451557 qtime=1218451557 etime=1218451557 start=1218454455 exec_host=nid00007/17 session=26609 alt_id=2501 end=1218495854 Exit_status=0",
				"08/22/2008 22:01:05;E;106636.sdb;user=ptelford group=-default- account=n02-chem jobname=xdggj000 queue=par:n32c2_20m ctime=1219438652 qtime=1219438652 etime=1219438652 start=1219438684 exec_host=nid00004/13 session=11260 alt_id=1065 end=1219438865 Exit_status=0",
				"01/04/2008 09:42:09;E;4188.sdb;user=expc01 group=-default- account=z02 jobname=report16n2c queue=par:n16c2_20m ctime=1199439548 qtime=1199439548 etime=1199439548 start=1199439548 exec_host=nid00004/0 session=21187 end=1199439729 Exit_status=0",
				"08/12/2008 00:02:08;E;100560.sdb;user=pjh503 group=-default- account=c01-mat jobname=al3x3-64 queue=par:n32c2_3h ctime=1218495709 qtime=1218495709 etime=1218495709 start=1218495725 exec_host=nid00004/16 session=13852 alt_id=2873 end=1218495728 Exit_status=1", 
				"06/07/2018 21:36:09;E;3710.master.cm.cluster;user=dc-bate1 group=dirac account=dp015 jobname=Red5_M01 queue=dirac2 ctime=1528403764 qtime=1528403764 etime=1528403764 start_count=1 start=1528403767 owner=dc-bate1@dirac03.cm.cluster exec_host=dnode011/0-15+dnode010/0-15+dnode009/0-15+dnode008/0-15 Resource_List.nodes=4:ppn=16 Resource_List.walltime=120:00:00 Resource_List.nodect=4 Resource_List.neednodes=4:ppn=16 session=18613 total_execution_slots=64 unique_node_count=4 end=1528403769 Exit_status=174 resources_used.cput=8 resources_used.vmem=0kb resources_used.walltime=00:00:02 resources_used.mem=1080kb resources_used.energy_used=0"
		};
		for (String record : validRecords)
			goodRecords.add(new RecordText(record));

		/*
		 * Records this parser shouldn't parse because they are the wrong type.
		 * Should throw SkipRecord
		 */
		String skippedRecords[] = new String[] {
				"10/27/2008 15:44:07;S;319540.torque.ifca.es;user=euforia003 group=euforia jobname=STDIN queue=euforia ctime=1225115373 qtime=1225115373 etime=1225115373 start=1225118647 owner=euforia003@i2gce01.ifca.es exec_host=cms24.ifca.es/2",
				"10/27/2008 18:16:50;S;320125.torque.ifca.es;user=euforia003 group=euforia jobname=STDIN queue=euforia ctime=1225126083 qtime=1225126083 etime=1225126083 start=1225127810 owner=euforia003@i2gce01.ifca.es exec_host=ingrid02.ifca.es/0",
				"10/27/2008 20:55:45;A;320342.torque.ifca.es;",
				"10/27/2008 21:03:20;D;320350.torque.ifca.es;user123@euforiahost321",
				"10/27/2008 23:22:00;Q;320554.torque.ifca.es;queue=par:n16c2_20m" };
		for (String record : skippedRecords)
			badTexts.add(new BadRecordText(record, SkipRecord.class));

		/*
		 * Make sure records with unknown types aren't parsed
		 */
		String badRecordTypeRecords[] = new String[] {
				"08/12/2008 00:01:34;z;100559.sdb;user=shiv group=-default- account=y06 jobname=STDIN queue=par:n8c2_20m ctime=1218495664 qtime=1218495664 etime=1218495664 start=1218495693 exec_host=nid00007/24 exec_vnode=(hector02:ncpus=1) session=19384 alt_id=2872 end=1218495694 Exit_status=0",
				"06/11/2004 00:15:31;X;job123;" };
		for (String record : badRecordTypeRecords)
			badTexts.add(new BadRecordText(record,
					SkipRecord.class));

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