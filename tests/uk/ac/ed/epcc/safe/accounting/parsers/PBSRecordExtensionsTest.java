// Copyright - The University of Edinburgh 2015
/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers;

import java.util.ArrayList;
import java.util.Collection;

import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser;
import uk.ac.ed.epcc.webapp.AppContext;

/**
 * Adds records to test. These records test the extensions mechanism of the
 * parser as well as some of it's normal operations. Some are good records that
 * the parser should be able to parse successfully. Some aren't so good and
 * should result in exceptions being thrown
 * 
 * @author jgreen4
 * 
 */
public class PBSRecordExtensionsTest extends AbstractRecordTestCase {

	private static final Collection<RecordText> goodRecords = new ArrayList<RecordText>();
	private static final Collection<BadRecordText> badTexts = new ArrayList<BadRecordText>();

	public PBSRecordExtensionsTest() {
		super("PBSMachine", "PBSRecord");
	}

	/*
	 * Records that should be parsed appropriately
	 */
	private static final String validRecords[] = new String[] {
			"08/12/2008 00:01:34;E;101.user123;user=shiv group=-default- account=y06 jobname=STDIN queue=par:n8c2_20m ctime=1218495664 qtime=1218495664 etime=1218495664 start=1218495693 exec_host=nid00007/24 exec_vnode=(hector02:ncpus=1) Resource_List.mpparch=XT Resource_List.mppnppn=2 Resource_List.mppwidth=1 Resource_List.ncpus=1 Resource_List.nodect=1 Resource_List.place=pack Resource_List.select=1 Resource_List.walltime=00:02:00 session=19384 alt_id=2872 end=1218495694 Exit_status=0 resources_used.cpupercent=0 resources_used.cput=00:00:00 resources_used.mem=1272kb resources_used.ncpus=1 resources_used.vmem=10092kb resources_used.walltime=00:00:01",
			"08/12/2008 00:04:14;E;102.user123;user=msseay group=-default- account=e71-DQ jobname=fnp_equil.pbs queue=par:n32c2_12h ctime=1218451557 qtime=1218451557 etime=1218451557 start=1218454455 exec_host=nid00007/17 exec_vnode=(hector02:ncpus=1) Resource_List.mpparch=XT Resource_List.mppnppn=2 Resource_List.mppwidth=64 Resource_List.ncpus=1 Resource_List.nodect=1 Resource_List.place=pack Resource_List.select=1 Resource_List.walltime=12:00:00 session=26609 alt_id=2501 end=1218495854 Exit_status=0 resources_used.cpupercent=0 resources_used.cput=00:00:00 resources_used.mem=6200kb resources_used.ncpus=1 resources_used.vmem=36032kb resources_used.walltime=11:30:00",
			"08/22/2008 22:01:05;E;103.user123;user=ptelford group=-default- account=n02-chem jobname=xdggj000 queue=par:n32c2_20m ctime=1219438652 qtime=1219438652 etime=1219438652 start=1219438684 exec_host=nid00004/13 exec_vnode=(hector01:ncpus=1) Resource_List.mpparch=XT Resource_List.mppnppn=2 Resource_List.mppwidth=64 Resource_List.ncpus=1 Resource_List.nodect=1 Resource_List.place=pack Resource_List.select=1 Resource_List.walltime=00:20:00 session=11260 alt_id=1065 end=1219438865 Exit_status=0 resources_used.cpupercent=0 resources_used.cput=00:00:06 resources_used.mem=4200kb resources_used.ncpus=1 resources_used.vmem=15360kb resources_used.walltime=00:03:02",
			"01/04/2008 09:42:09;E;104.user234;user=expc01 group=-default- account=a02 jobname=report16n2c queue=par:n16c2_20m ctime=1199439548 qtime=1199439548 etime=1199439548 start=1199439548 exec_host=nid00004/0 exec_vnode=(login:ncpus=1) Resource_List.mpparch=XT Resource_List.mppnppn=2 Resource_List.mppwidth=32 Resource_List.ncpus=1 Resource_List.nodect=1 Resource_List.place=pack Resource_List.select=1 Resource_List.walltime=00:03:00 session=21187 end=1199439729 Exit_status=0 resources_used.cpupercent=0 resources_used.cput=00:00:00 resources_used.mem=6344kb resources_used.ncpus=1 resources_used.vmem=39792kb resources_used.walltime=00:03:01",
			"01/04/2008 11:00:37;E;105.user234;user=lucian group=-default- account=a03 jobname=liquid1 queue=par:n8c2_1h ctime=1199444256 qtime=1199444256 etime=1199444256 start=1199444256 exec_host=nid00004/0 exec_vnode=(login:ncpus=1) Resource_List.mpparch=XT Resource_List.mppnppn=2 Resource_List.mppwidth=10 Resource_List.ncpus=1 Resource_List.nodect=1 Resource_List.place=pack Resource_List.select=1 Resource_List.walltime=01:00:00 session=23145 end=1199444437 Exit_status=1 resources_used.cpupercent=0 resources_used.cput=00:00:00 resources_used.mem=6364kb resources_used.ncpus=1 resources_used.vmem=39756kb resources_used.walltime=00:03:01",
			"08/12/2008 00:02:08;E;106.user234;user=pjh503 group=-default- account=d01-mat jobname=al3x3-64 queue=par:n32c2_3h ctime=1218495709 qtime=1218495709 etime=1218495709 start=1218495725 exec_host=nid00004/16 exec_vnode=(hector01:ncpus=1) Resource_List.mpparch=XT Resource_List.mppnppn=2 Resource_List.mppwidth=64 Resource_List.ncpus=1 Resource_List.nodect=1 Resource_List.place=pack Resource_List.select=1 Resource_List.walltime=02:00:00 session=13852 alt_id=2873 end=1218495728 Exit_status=1 resources_used.cpupercent=0 resources_used.cput=00:00:01 resources_used.mem=5564kb resources_used.ncpus=1 resources_used.vmem=38912kb resources_used.walltime=00:00:03" };
	static {
		for (String record : validRecords)
			goodRecords.add(new RecordText(record));
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

	public static class MemParser implements ValueParser<Number> {
		public MemParser() {
		}

		public Class<Number> getType() {
			return Number.class;
		}

		public Number parse(String valueString) throws IllegalArgumentException,
				NullPointerException {

			int len = valueString.length();
			String number = valueString.substring(0, len - 2);
			String unit = valueString.substring(len - 2);

			long multiplier = 1;

			if (unit.equalsIgnoreCase("kb"))
				multiplier = 1024;
			else if (unit.equalsIgnoreCase("mb"))
				multiplier = 1024 * 1024;
			else if (unit.matches("\\d\\d")) {
				number = valueString; // If the number has no unit
			} else {
				throw new IllegalArgumentException("Unknown unit: " + unit);
			}

			return Long.parseLong(number) * multiplier;
		}

		public String format(Number value) {
			// NOT needed here
			return null;
		}

	}
}