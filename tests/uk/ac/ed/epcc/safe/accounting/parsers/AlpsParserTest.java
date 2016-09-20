package uk.ac.ed.epcc.safe.accounting.parsers;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.webapp.junit4.DataBaseFixtures;


@DataBaseFixtures({"AprunApplication.xml"})
public class AlpsParserTest extends AbstractRecordTestCase {

	public AlpsParserTest() {
		super("ARCHER", "ALPSLog");
	}


	@Override
	public PropertyMap getDefaults() {
		PropertyMap defaults = new PropertyMap();
       
		return defaults;
		
	}

	private static final String testDataPath = null;
	//private static final String testDataPath = "/Users/michaelbareford/Downloads/rur/alps.test";
	private static final Collection<RecordText> goodRecords = new ArrayList<RecordText>();
	private static final Collection<BadRecordText> badTexts = new ArrayList<BadRecordText>();
	
	
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.db.ParseUsageRecordFactoryTestCase#getUpdateText()
	 */
	static {
		
		String local_records[] = new String[] {
		"<150>1 2016-07-10T00:06:20.261556+01:00 c2-1c0s0n1 aprun 2035 p0-20160622t161139 [alps_msgs@34] apid=22444371, Starting, user=15269, batch_id=3815975.sdb, cmd_line=\"/opt/cray/alps/5.2.3-2.0502.9295.14.14.ari/bin/aprun -n 192 /work/e89/e89/zd242/src/vasp.5.4.1/bin/vasp_ncl \", num_nodes=8, node_list=2258-2259,2263,2322,2991,3005-3006,3016, cwd=\"/fs3/e89/e89/zd242/work/3815975.sdb\"",
		"<150>1 2016-07-10T00:20:21.291116+01:00 c2-1c0s0n1 apsys 2039 p0-20160622t161139 [alps_msgs@34] apid=22444371, Finishing, user=15269, batch_id=3815975.sdb, exit_code=0, exitcode_array=0, exitsignal_array=0",
		
		"<150>1 2016-07-10T00:18:47.860624+01:00 c0-1c0s0n1 aprun 18934 p0-20160622t161139 [alps_msgs@34] apid=22444398, Starting, user=14217, batch_id=3813206[16].sdb, cmd_line=\"/opt/cray/alps/5.2.3-2.0502.9295.14.14.ari/bin/aprun -n 40 castep.mpi VI \", num_nodes=2, node_list=22-23, cwd=\"/fs3/e89/e89/eae32/KDP/VI/1x1x1_Anh/Supercell_1/kpoint.1/configurations/mode.94.7\"",
		"<150>1 2016-07-10T00:22:58.198995+01:00 c0-1c0s0n1 apsys 18939 p0-20160622t161139 [alps_msgs@34] apid=22444398, Finishing, user=14217, batch_id=3813206[16].sdb, exit_code=0, exitcode_array=0, exitsignal_array=0",
				
		"<150>1 2016-07-10T00:46:48.807819+01:00 c2-0c1s1n1 aprun 31070 p0-20160622t161139 [alps_msgs@34] apid=22444442, Starting, user=16626, batch_id=3812810.sdb, cmd_line=\"/opt/cray/alps/5.2.3-2.0502.9295.14.14.ari/bin/aprun -n 1056 mdrun_mpi -cpi state.cpt -append -nsteps 250000 \", num_nodes=44, node_list=451,456,490,496-498,515-521,523-528,541-546,551-553,560,564-565,572-573,595-598,628,643,645,785,787-789, cwd=\"/fs2/e446/e446/mgschn2/Slipids/gold/ribbonslab/PMFsingle/mod95\"",
		"<150>1 2016-07-10T00:54:07.461169+01:00 c2-0c1s1n1 apsys 31074 p0-20160622t161139 [alps_msgs@34] apid=22444442, Finishing, user=16626, batch_id=3812810.sdb, exit_code=0, exitcode_array=0, exitsignal_array=0",
		
		"<150>1 2016-05-20T01:37:36.037244+01:00 c4-1c0s0n1 aprun 9259 p0-20160509t103958 [alps_msgs@34] apid=21701310, Starting, user=14597, batch_id=3695119.sdb, cmd_line=\"/opt/cray/alps/5.2.3-2.0502.9295.14.14.ari/bin/aprun -N 1 -n 1 -S 1 /work/n02/n02/pappas/src/oasis/crayxe6_cce-20160403/bin/oasis3.MPI1.x : -N 24 -d 1 -n 144 -S 12 -ss /work/n02/n02/pappas/um/xgspt/bin/ga30_kpp.exe-20160403 : -N 1 -n 1 -d 24 /work/n02/n02/bdong/um/xmnub/dataw/KPP_ocean \", num_nodes=8, node_list=2502-2504,2511-2513,4200,4983, cwd=\"/fs2/n02/n02/bdong/um/xmnub/dataw\"",
		"<150>1 2016-05-20T01:52:54.531097+01:00 c4-1c0s0n1 apsys 9282 p0-20160509t103958 [alps_msgs@34] apid=21701310, Finishing, user=14597, batch_id=3695119.sdb, exit_code=137, exitcode_array=139:0, exitsignal_array=9:0",

		"<150>1 2016-05-20T00:00:00.244775+01:00 c4-1c0s0n1 aprun 30934 p0-20160509t103958 [alps_msgs@34] apid=21699839, Starting, user=15998, batch_id=3696699.sdb, cmd_line=\"/opt/cray/alps/5.2.3-2.0502.9295.14.14.ari/bin/aprun -n 1024 ./imagenew 1024 1 \", num_nodes=43, node_list=1131-1133,1135-1142,2515-2516,2726-2732,2736-2738,2823-2838,3381-3382,4379-4380, cwd=\"/fs4/d89/d89/s1514982/Work/MPP/bitbucket/mpp/coursework1/src/unstriped\"",
		"<150>1 2016-05-20T00:00:29.188586+01:00 c4-1c0s0n1 apsys 30938 p0-20160509t103958 [alps_msgs@34] apid=21699839, Finishing, user=15998, batch_id=3696699.sdb, exit_code=0, exitcode_array=0, exitsignal_array=0",
		
		"<150>1 2016-05-20T00:02:39.015905+01:00 c4-1c0s0n1 aprun 334 p0-20160509t103958 [alps_msgs@34] apid=21699856, Starting, user=15998, batch_id=3696699.sdb, cmd_line=\"/opt/cray/alps/5.2.3-2.0502.9295.14.14.ari/bin/aprun -n 1024 ./imagenew 1024 1 \", num_nodes=43, node_list=1131-1133,1135-1142,2515-2516,2726-2732,2736-2738,2823-2838,3381-3382,4379-4380, cwd=\"/fs4/d89/d89/s1514982/Work/MPP/bitbucket/mpp/coursework1/src/unstriped\"",
		"<150>1 2016-05-20T00:03:05.355536+01:00 c4-1c0s0n1 apsys 338 p0-20160509t103958 [alps_msgs@34] apid=21699856, Finishing, user=15998, batch_id=3696699.sdb, exit_code=0, exitcode_array=0, exitsignal_array=0",
		
		"<150>1 2016-05-20T00:39:23.291704+01:00 c4-1c0s0n1 aprun 26297 p0-20160509t103958 [alps_msgs@34] apid=21699984, Starting, user=16257, batch_id=3696734.sdb, cmd_line=\"/opt/cray/alps/5.2.3-2.0502.9295.14.14.ari/bin/aprun -n 1 /work/e280/e280/sv375/Solvaware_v1.3/Solvaware_Trunk/HydrationSiteAnalysis/Utilities/WriteDensityProfile /fs2/e280/e280/sv375/carbonic_anhydrase_2/2NNG/Equilibration/2NNG_system.pdb /fs2/e280/e280/sv375/carbonic_anhydrase_2/2NNG/Equilibration/2NNG_system.psf /fs2/e280/e280/sv375/carbonic_anhydrase_2/2NNG/Dynamics/2NNG_system_dynamics.dcd 0 9000 1 1.2 59 59 59 108 480 \", num_nodes=1, node_list=4830, cwd=\"/fs2/e280/e280/sv375/carbonic_anhydrase_2/2NNG/Cluster\"",
		"<150>1 2016-05-20T00:40:04.401205+01:00 c4-1c0s0n1 apsys 26725 p0-20160509t103958 [alps_msgs@34] apid=21699984, Finishing, user=16257, batch_id=3696734.sdb, exit_code=0, exitcode_array=0, exitsignal_array=0",

		"<150>1 2016-05-20T03:40:21.393759+01:00 c4-1c0s0n1 aprun 2270 p0-20160509t103958 [alps_msgs@34] apid=21705594, Starting, user=5833, batch_id=3692330.sdb, cmd_line=\"/opt/cray/alps/5.2.3-2.0502.9295.14.14.ari/bin/aprun -ss -n 1920 -N 24 -d 1 -S 12 -j 1 -e OMP_NUM_THREADS=1 /work/n02/n02/wmcginty/um/xmrfd/bin/xmrfd.exe : -ss -n 24 -N 12 -d 2 -S 6 -j 1 -e OMP_NUM_THREADS=2 /work/n02/n02/wmcginty/um/xmrfd/bin/xmrfd.exe \", num_nodes=82, node_list=970-971,1534,1540-1555,1560-1587,1597-1603,1608-1615,1620,1622-1640, cwd=\"/fs2/n02/n02/wmcginty/VERA/um/xmrjc\"",
		"<150>1 2016-05-20T14:14:06.978843+01:00 c4-1c0s0n1 apsys 2274 p0-20160509t103958 [alps_msgs@34] apid=21705594, Finishing, user=5833, batch_id=3692330.sdb, exit_code=0, exitcode_array=0, exitsignal_array=0",
		
		"<150>1 2015-11-01T00:01:07.515128+00:00 c0-1c0s0n1 aprun 3706 p0-20151014t172436 [alps_msgs@34] apid=18543822, Error, user=15559, batch_id=3245599.sdb, [NID 03497] 2015-11-01 00:01:07 Apid 18543822: Cpuset file /dev/cpuset/18543822/cpus wrote -1 of 5; found 1 other local apid: 18540933 ",
		"<150>1 2016-07-01T00:16:49.799797+01:00 c2-0c1s1n1 aprun 8929 p0-20160622t161139 [alps_msgs@34] apid=none, Error, user=13676, batch_id=unknown, user specified option error",
		"<150>1 2016-06-02T12:05:42.581451+01:00 c2-0c1s1n1 apsys 11428 p0-20160525t121452 [alps_msgs@34] apid=21897763, Finishing, user=15961, batch_id=3728292[1091].sdb, exit_code=0, exitcode_array=0, exitsignal_array=0",
		"<150>1 2016-06-02T12:04:51.255478+01:00 c6-0c1s1n1 apsys 7157 p0-20160525t121452 [alps_msgs@34] apid=21897856, Finishing, user=15961, batch_id=3728292[1099].sdb, exit_code=0, exitcode_array=0, exitsignal_array=0"
		};
		
		ArrayList<String> records = new ArrayList<String>();
		
		if (null == testDataPath) {
			for (String s : local_records) {
				records.add(s);
			}
		}
		else {
			try {
				FileInputStream fstream = new FileInputStream(testDataPath);
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				
				String strLine;
				while ((strLine = br.readLine()) != null)   {
					records.add(strLine);
				}
				
				in.close();
			} catch (Exception e){
			    System.err.println("Error: " + e.getMessage());
			}
		}
		
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

	@Test
	public void testDateParser() throws ParseException{
		Date orig = AlpsLogParser.parseDate(true,"2016-06-02T12:04:51.255478+01:00");
		Date mod = AlpsLogParser.parseDate(true,"2016-06-02T12:04:51.255478+00:00");
		long orig_time = orig.getTime();
		long mod_time = mod.getTime();
		assertNotEquals(orig.getTime() , mod.getTime());
	}
	
	@Test
	public void testDateParserNoTZ() throws ParseException{
		Date orig = AlpsLogParser.parseDate(false,"2016-06-02T12:04:51.255478+01:00");
		Date mod = AlpsLogParser.parseDate(false,"2016-06-02T12:04:51.255478+00:00");
		long orig_time = orig.getTime();
		long mod_time = mod.getTime();
		assertEquals(orig.getTime() , mod.getTime());
	}
}
