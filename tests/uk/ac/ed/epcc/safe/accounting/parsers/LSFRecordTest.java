// Copyright - The University of Edinburgh 2015
/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.SkipRecord;
import uk.ac.ed.epcc.webapp.AppContext;

/**
 * Adds records to test. Some are good records that the parser should be able to
 * parse successfully. Some aren't so good and should result in exceptions being
 * thrown
 * 
 * @author malcolm
 * 
 */
public class LSFRecordTest extends AbstractRecordTestCase {

	private static final Collection<RecordText> goodRecords = new ArrayList<RecordText>();
	private static final Collection<BadRecordText> badTexts = new ArrayList<BadRecordText>();

	public LSFRecordTest() {
		super("LSFMachine", "LSFRecord");
	}
	
	/**
	 * Tests are not going to be altered at runtime so we declare them all in a
	 * static block and add them to either the good records or bad records
	 * collection
	 */
	static {
		String extendedLineRecords[] = new String[] {
				"\"JOB_FINISH\" \"7.03\" 1276677130 100622 14179 33620018 60 1276676915 0 0 1276676917 \"benzi\" \"n64_6\" \"type == any\" \"\" \"\" \"hapu33\" \"driven_cavity/flux/VHS/WallShear/Maxwell/alpha_pt005\" \"\" \"outo%J\" \"oute%J\" \"1276676915.100622\" 0 60 \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.loca!\n "
				+ "ldomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" 32 60.0 \"\" \"# !/bin/csh; ### Note, that number of processors requested is on the bsub command line!; # BSUB -o outo%J  -e oute%J; #; ### Use the 'mpirun' command in this way, the '-srun' is required for ; ### job to run on the nodes you're allocated (i.e. no 'nodes' file as on; ### other clusters)!;mpirun  -srun ./main\" 12166.591361 69.896343 80948 0 -1 0 0 1410825 1439 257144 0 0 -1 0 0 0 357087 948298 -1 \"\" \"default\" 65280 !\n "
				+ "60 \"\" \"\" 0 80948 257144 \"\" \"\" \"\" \"\" 0 \"slurm_id=107567;ncpus=60;slurm_alloc=hapu[1-15]\" 0 \"\" -1 \"/ceg\" \"\" \"\" \"\" -1 \"\" \"\"\n",
				
				"\"JOB_FINISH\" \"7.03\" 1276715766 100630 14179 33620018 60 1276698635 0 0 1276698638 \"benzi\" \"n64_12\" \"type == any\" \"\" \"\" \"hapu33\" \"driven_cavity/flux/VHS/WallShear/Maxwell/TopWall_diff\" \"\" \"outo%J\" \"oute%J\" \"1276698635.100630\" 0 60 \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.lo!\n "
				+ "caldomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" 64 60.0 \"\" \"# !/bin/csh; ### Note, that number of processors requested is on the bsub command line!; # BSUB -o outo%J  -e oute%J; #; ### Use the 'mpirun' command in this way, the '-srun' is required for ; ### job to run on the nodes you're allocated (i.e. no 'nodes' file as on; ### other clusters)!;mpirun  -srun ./main\" 1024804.815955 355.881867 2214648 0 -1 0 0 45748982 3456 3109572 0 0 -1 0 0 0 28006088 30802250 -1 \"\" \"de!\n "
				+ "fault\" 0 60 \"\" \"\" 0 2214648 3109572 \"\" \"\" \"\" \"\" 0 \"slurm_id=107575;ncpus=60;slurm_alloc=hapu[1-15]\" 0 \"\" -1 \"/ceg\" \"\" \"\" \"\" -1 \"\" \"\"\n"
		};
		
		String validRecords[] = new String[] {
				
				"\"JOB_FINISH\" \"7.03\" 1276596704 100581 14179 33620018 8 1276596666 0 0 1276596672 \"testuser\" \"n32_6\" \"type == any\" \"\" \"\" \"hapu33\" \"driven_cavity/flux/VHS/2dpartition/backup\" \"\" \"outo%J\" \"oute%J\" \"1276596666.100581\" 0 8 \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" 64 60.0 \"\" \"# !/bin/csh; ### Note, that number of processors requested is on the bsub command line!; # BSUB -o outo%J  -e oute%J; #; ### Use the 'mpirun' command in this way, the '-srun' is required for ; ### job to run on the nodes you're allocated (i.e. no 'nodes' file as on; ### other clusters)!;mpirun  -srun ./main\" 171.152974 17.418348 9364 0 -1 0 0 714455 0 256976 0 0 -1 0 0 0 11031 40214 -1 \"\" \"default\" 0 8 \"\" \"\" 0 9364 256976 \"\" \"\" \"\" \"\" 0 \"slurm_id=107526;ncpus=8;slurm_alloc=hapu[1-2]\" 0 \"\" -1 \"/ceg\" \"\" \"\" \"\" -1 \"\" \"\"\n",
				"\"JOB_FINISH\" \"7.03\" 1276598301 100588 14179 33620018 2 1276598233 0 0 1276598239 \"benzi\" \"n4_6\" \"type == any\" \"\" \"\" \"hapu33\" \"driven_cavity/flux/parallel_tests\" \"\" \"outo%J\" \"oute%J\" \"1276598233.100588\" 0 2 \"lsfhost.localdomain\" \"lsfhost.localdomain\" 64 60.0 \"\" \"# !/bin/csh; ### Note, that number of processors requested is on the bsub command line!; # BSUB -o outo%J  -e oute%J; #; ### Use the 'mpirun' command in this way, the '-srun' is required for ; ### job to run on the nodes you're allocated (i.e. no 'nodes' file as on; ### other clusters)!;mpirun  -srun ./main\" 91.775046 8.063773 109688 0 -1 0 0 688578 0 322520 0 0 -1 0 0 0 11053 7853 -1 \"\" \"default\" 0 2 \"\" \"\" 0 109688 322520 \"\" \"\" \"\" \"\" 0 \"slurm_id=107533;ncpus=4;slurm_alloc=hapu1\" 0 \"\" -1 \"/ceg\" \"\" \"\" \"\" -1 \"\" \"\"\n",
				"\"JOB_FINISH\" \"7.03\" 1276621225 100619 13545 33619987 4 1276621188 0 0 1276621193 \"jmht93\" \"n4_1\" \"type == any\" \"\" \"\" \"hapu33\" \"dalton-2.0/jens\" \"\" \"dalton.log.%J\" \"\" \"1276621188.100619\" 0 4 \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" \"lsfhost.localdomain\" 32 60.0 \"dalton\" \"#!/bin/bash;# BSUB -n 4;# BSUB -J dalton;# BSUB -o dalton.log.%J;# BSUB -W 00:15;####BSUB -ext \"\"SLURM[nodes=2]\"\";  script=/home/jmht93/dalton-2.0/bin/dalton;$script -N 1 energy_parallel\" 0.132978 0.129979 6656 0 -1 0 0 17679 56 348144 0 0 -1 0 0 0 328 130 -1 \"\" \"default\" 25600 4 \"\" \"\" 0 6656 348144 \"\" \"\" \"\" \"\" 0 \"slurm_id=107564;ncpus=4;slurm_alloc=hapu1\" 0 \"\" -1 \"/others*0\" \"\" \"\" \"\" -1 \"\" \"\"\n"
		};
		
		for (String record : validRecords)
			goodRecords.add(new RecordText(record));
		for (String record : extendedLineRecords)
			goodRecords.add(new RecordText(record));

		/*
		 * Records this parser shouldn't parse because they are the wrong type.
		 * Should throw SkipRecord
		 */
		String skippedRecords[] = new String[] {
				"\"EVENT_ADRSV_FINISH\" 1 2 3 4",
				"\"EVENT_ADRSV_FINISH\" 5 6 7 8"
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
		String tooFewFields = "not even recognisable as a record";
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

	@Override
	public Set getIgnore() {
		Set ignore = super.getIgnore();
		ignore.add("JobStatus");
		return ignore;
	}

	
}