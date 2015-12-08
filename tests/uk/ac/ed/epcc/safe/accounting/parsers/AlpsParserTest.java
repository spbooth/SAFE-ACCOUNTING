package uk.ac.ed.epcc.safe.accounting.parsers;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Ignore;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
@Ignore
public class AlpsParserTest extends AbstractRecordTestCase {

	public AlpsParserTest() {
		super("ARCHER", "AlpsRecord");
		// TODO Auto-generated constructor stub
	}



	@Override
	public PropertyMap getDefaults() {
		PropertyMap defaults = new PropertyMap();
       
		return defaults;
		
	}
	private static final Collection<RecordText> goodRecords = new ArrayList<RecordText>();
	private static final Collection<BadRecordText> badTexts = new ArrayList<BadRecordText>();
	
	
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.db.ParseUsageRecordFactoryTestCase#getUpdateText()
	 */
	static{
		String records[] =new String[]{
		"<150>1 2015-11-01T00:00:03.957208+01:00 c0-1c0s0n1 apsys 2809 p0-20151014t172436 [alps_msgs@34] apid=18543815, Finishing, user=15559, batch_id=3245599.sdb",
		"<150>1 2015-11-01T00:00:15.128132+00:00 c2-0c1s1n1 aprun 16111 p0-20151014t172436 [alps_msgs@34] apid=18543818, Starting, user=15031, batch_id=3246064.sdb, cmd_line=\"aprun -n 96 -N 24 -S 12 -d 1 /usr/local/packages/gmx/4.6.5-phase1/bin/mdrun_mpi -s -v -cpi -tunepme -maxh 23.75 \", num_nodes=4, node_list=1519-1520,1522,1525, cwd=\"/fs2/e280/e280/gsala280/MAPK_DMotif/ERK-MSK-on-RSK/PP/nvt\"",
		"<150>1 2015-11-01T00:00:15.250913+00:00 c0-1c0s0n1 aprun 3578 p0-20151014t172436 [alps_msgs@34] apid=18543819, Starting, user=15031, batch_id=3246065.sdb, cmd_line=\"aprun -n 96 -N 24 -S 12 -d 1 /usr/local/packages/gmx/4.6.5-phase1/bin/mdrun_mpi -s -v -cpi -tunepme -maxh 23.75 \", num_nodes=4, node_list=1526,1603,1634-1635, cwd=\"/fs2/e280/e280/gsala280/MAPK_DMotif/ERK-MSK-on-RSK/wt/nvt\"",
		"<150>1 2015-11-01T00:00:16.533355+01:00 c6-0c1s1n1 apsys 24532 p0-20151014t172436 [alps_msgs@34] apid=18543788, Finishing, user=16032, batch_id=3246052.sdb, exit_code=0, exitcode_array=0, exitsignal_array=0",
		"<150>1 2015-11-01T00:00:30.482836+01:00 c6-0c1s1n1 apsys 21204 p0-20151014t172436 [alps_msgs@34] apid=18543707, Finishing, user=16033, batch_id=3246011.sdb, exit_code=0, exitcode_array=0, exitsignal_array=0",
		"<150>1 2015-11-01T00:00:31.448373+01:00 c2-0c1s1n1 apsys 12079 p0-20151014t172436 [alps_msgs@34] apid=18543783, Finishing, user=13854, batch_id=3245590.sdb, exit_code=0, exitcode_array=0, exitsignal_array=0",
		"<150>1 2015-11-01T00:00:32.204004+00:00 c2-0c1s1n1 aprun 16178 p0-20151014t172436 [alps_msgs@34] apid=18543820, Starting, user=13854, batch_id=3245590.sdb, cmd_line=\"aprun -n 24 /home/e05/shared/red/vasp5 \", num_nodes=1, node_list=1765, cwd=\"/fs3/e05/e05/federico/alloys/211/balena/1I_5Br/001\"",
		"<150>1 2015-11-01T00:01:04.217889+00:00 c0-1c0s0n1 aprun 3706 p0-20151014t172436 [alps_msgs@34] apid=18543822, Starting, user=15559, batch_id=3245599.sdb, cmd_line=\"aprun -n 336 pw.x -npool 28 -ntg 4 -in monoVACMD_53_xyz_362_add_1H_2000.000.pwi \", num_nodes=14, node_list=3484-3497, cwd=\"/fs4/e304/e304-WP3-Csanyi/tdd20h/Iron/ConfigGen/AddedHydrogen/monoVACMD_53\"",
		"<150>1 2015-11-01T00:01:07.302401+01:00 c2-0c1s1n1 apsys 16115 p0-20151014t172436 [alps_msgs@34] apid=18543818, Finishing, user=15031, batch_id=3246064.sdb, exit_code=137, exitcode_array=134:0, exitsignal_array=9:0",
		"<150>1 2015-11-01T00:01:07.515128+00:00 c0-1c0s0n1 aprun 3706 p0-20151014t172436 [alps_msgs@34] apid=18543822, Error, user=15559, batch_id=3245599.sdb, [NID 03497] 2015-11-01 00:01:07 Apid 18543822: Cpuset file /dev/cpuset/18543822/cpus wrote -1 of 5; found 1 other local apid: 18540933 "
		};
		for( String s : records){
			goodRecords.add(new RecordText(s));
		}
	}


	@Override
	public Collection<BadRecordText> getBadRecords() {
		
		return badTexts;
	}



	@Override
	public Collection<RecordText> getGoodRecords() {
		
		return goodRecords;
	}

	
}
