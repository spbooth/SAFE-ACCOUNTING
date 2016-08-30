package uk.ac.ed.epcc.safe.accounting.parsers;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.webapp.junit4.DataBaseFixtures;


@DataBaseFixtures({"AprunApplication.xml"})
public class RurParserTest extends AbstractRecordTestCase {

	public RurParserTest() {
		super("ARCHER", "RURLog");
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
	static {
		
		String records[] = new String[] {
		"2013-08-30T11:19:06.545114-05:00 c0-0c0s2n2 RUR 18657 p2-20130829t090349 [RUR@34] uid: 12345, apid: 10963, jobid: 0, cmdname: /opt/intel/vtune_xe_2013/bin64/amplxe-cl, plugin: energy ['energy_used', 318]",
		"2014-01-17T10:05:54.026557-06:00 c0-0c0s1n1 RUR 11674 p0-20140116t214834 [rur@34] uid: 12345, apid: 286342, jobid: 0, cmdname: /bin/cat, plugin: energy {\"energy_used\": 5641, \"accel_energy_used\": 1340, \"nodes\": 32, \"nodes_power_capped\": 3, \"min_power_cap\": 155, \"min_power_cap_count\": 2, \"max_power_cap\": 355, \"max_power_cap_count\": 1, \"nodes_accel_power_capped\": 3, \"min_accel_power_cap\": 200, \"min_accel_power_cap_count\": 3, \"max_accel_power_cap\": 200, \"max_accel_power_cap_count\": 3, \"nodes_throttled\": 0, \"nodes_with_changed_power_cap\": 0}",
		"2013-11-02T11:09:49.457770-05:00 c0-0c1s1n2 RUR 2417 p0-20131101t153028 [RUR@34] uid: 12345, apid: 86989, jobid: 0, cmdname: /lus/tmp/rur01.2338/./CPU01-2338, plugin: taskstats ['utime', 10000000, 'stime', 0, 'max_rss', 940, 'rchar', 107480, 'wchar', 90, 'exitcode:signal', ['0:0'], 'core', 0]",
		"2013-11-02T11:12:45.020716-05:00 c0-0c1s1n2 RUR 3731 p0-20131101t153028 [RUR@34] uid: 12345, apid: 86996, jobid: 0, cmdname: /lus/tmp/rur01.3657/./exit04-3657, plugin: taskstats ['utime', 4000, 'stime', 144000, 'max_rss', 7336, 'rchar', 252289, 'wchar', 741, 'exitcode:signal', ['0:9', '139:0', '0:11', '0:0'], 'core', 1]",
		"2013-10-18T10:29:38.285378-05:00 c0-0c0s1n1 RUR 24393 p1-20131018t081133 [RUR@34] uid: 12345, apid: 370583, jobid: 0, cmdname: /bin/cat, plugin: taskstats ['btime', 1386061749, 'etime', 8000, 'utime', 0, 'stime', 4000, 'coremem', 442, 'max_rss', 564, 'max_vm', 564, 'pgswapcnt', 63, 'minfault', 15, 'majfault', 48, 'rchar', 2608, 'wchar', 686, 'rcalls', 19, 'wcalls', 7, 'bkiowait', 1000, 'exitcode:signal', [0], 'core', 0]",
		"2013-12-03T13:25:34.446167-06:00 c0-0c2s0n2 RUR 7623 p3-20131202t090205 [RUR@34] uid: 12345, apid: 1560, jobid: 0, cmdname: ./it.sh, plugin: taskstats {\"uid\": 12795, \"wcalls\": 37, \"pid\": 2997, \"vm\": 16348, \"jid\": 395136991233, \"bkiowait\": 1201616, \"majfault\": 1, \"etime\": 0, \"btime\": 1386098731, \"gid\": 0, \"ppid\": 2992, \"utime\": 0, \"nice\": 0, \"sched\": 0, \"nid\": \"92\", \"prid\": 0, \"comm\": \"mount\", \"stime\": 4000, \"wchar\": 3465, \"rss\": 1028, \"minfault\": 352, \"coremem\": 1109, \"ecode\": 0, \"rcalls\": 22, \"pjid\": 7045, \"pgswapcnt\": 0, \"rchar\": 12208}",
		"2013-12-03T13:25:34.949138-06:00 c0-0c2s0n2 RUR 7623 p3-20131202t090205 [RUR@34] uid: 12345, apid: 1561, jobid: 0, cmdname: ./it.sh, plugin: taskstats {\"uid\": 12795, \"wcalls\": 0, \"pid\": 2998, \"vm\": 20268, \"jid\": 395136991233, \"bkiowait\": 0, \"majfault\": 0, \"etime\": 0, \"btime\": 1386098731, \"gid\": 0, \"ppid\": 2992, \"utime\": 0, \"nice\": 0, \"sched\": 0, \"nid\": \"92\", \"prid\": 0, \"apid\": 1560, \"comm\": \"ls\", \"stime\": 4000, \"wchar\": 0, \"rss\": 1040, \"minfault\": 360, \"coremem\": 3140, \"ecode\": 0, \"rcalls\": 19, \"pjid\": 7045, \"pgswapcnt\": 0, \"rchar\": 10629}",
		"2014-03-21T11:37:24.480982-05:00 c0-0c0s0n2 RUR 23710 p0-20140321t091957 [RUR@34] uid: 12345, apid: 33079, jobid: 0, cmdname: /bin/hostname, plugin: memory {\"current_freemem\": 21858372, \"meminfo\": {\"Active(anon)\": 35952, \"Slab\": 105824, \"Inactive(anon)\": 1104}, \"hugepages-2048kB\": {\"nr\": 5120, \"surplus\": 5120}, \"%_of_boot_mem\": [\"67.23\", \"67.23\", \"67.23\", \"67.22\", \"67.21\", \"67.18\", \"67.11\", \"67.04\", \"66.94\", \"66.83\", \"66.77\", \"66.66\", \"66.53\", \"66.38\", \"65.87\", \"65.07\", \"63.05\", \"61.43\"], \"boot_freemem\": 32432628}",
		"2014-03-21T11:37:24.480982-05:00 c0-0c0s0n2 RUR 23710 p0-20140321t091957 [RUR@34] uid: 12345, apid: 33090, jobid: 0, cmdname: /bin/hostname, plugin: memory {\"current_freemem\": 21858372, \"meminfo\": {\"Active(anon)\": 35952, \"Slab\": 105824, \"Inactive(anon)\": 1104}, \"hugepages-2048kB\": {\"nr\": 5120, \"surplus\": 5120}, \"Node_0_zone_DMA\": [\"0.05\", \"0.05\", \"0.05\", \"0.05\", \"0.05\", \"0.05\", \"0.05\", \"0.05\", \"0.05\", \"0.04\", \"0.04\", \"0.03\", \"0.00\", \"0.00\", \"0.00\", \"0.00\", \"0.00\", \"0.00\"], \"%_of_boot_mem\": [\"67.23\", \"67.23\", \"67.23\", \"67.22\", \"67.21\", \"67.18\", \"67.11\", \"67.04\", \"66.94\", \"66.83\", \"66.77\", \"66.66\", \"66.53\", \"66.38\", \"65.87\", \"65.07\", \"63.05\", \"61.43\"], \"boot_freemem\": 32432628, \"Node_0_zone_DMA32\": [\"6.07\", \"6.07\", \"6.07\", \"6.07\", \"6.07\", \"6.07\", \"6.07\", \"6.06\", \"6.05\", \"6.04\", \"6.01\", \"5.94\", \"5.86\", \"5.76\", \"5.46\", \"4.85\", \"3.23\", \"3.23\"], \"Node_0_zone_Normal\": [\"61.11\", \"61.11\", \"61.11\", \"61.11\", \"61.09\", \"61.07\", \"60.99\", \"60.93\", \"60.84\", \"60.75\", \"60.72\", \"60.70\", \"60.67\", \"60.62\", \"60.42\", \"60.22\", \"59.81\", \"58.20\"]}"
		};
				
		/*
		ArrayList<String> records = new ArrayList<String>();
		
		try {
			FileInputStream fstream = new FileInputStream("/Users/michaelbareford/Downloads/alps/alps.201607");
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
		*/
		
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
