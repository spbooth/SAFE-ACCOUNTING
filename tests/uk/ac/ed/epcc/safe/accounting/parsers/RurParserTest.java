package uk.ac.ed.epcc.safe.accounting.parsers;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.webapp.forms.inputs.TypeError;
import uk.ac.ed.epcc.webapp.junit4.DataBaseFixtures;


@DataBaseFixtures({"AprunApplication.xml", "RurParserTest.xml"})
public class RurParserTest extends AbstractRecordTestCase {

	public RurParserTest() {
		super("ARCHER", "RURLog");
	}


	@Override
	public PropertyMap getDefaults() {
		PropertyMap defaults = new PropertyMap();
       
		return defaults;
		
	}

	private static final String testDataPath = null;
	//private static final String testDataPath = "/Users/michaelbareford/Downloads/rur/rur.test";
	private static final Collection<RecordText> goodRecords = new ArrayList<RecordText>();
	private static final Collection<BadRecordText> badTexts = new ArrayList<BadRecordText>();
	
	
	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.db.ParseUsageRecordFactoryTestCase#getUpdateText()
	 */
	static {
		
		String local_records[] = new String[] {
			"2014-06-17T10:05:54.026557-06:00 c0-0c1s1n2 RUR 11674 p0-20140616t214834 [RUR@34] uid: 12345, apid: 22542623, jobid: 0, cmdname: /bin/cat, plugin: energy {\"energy_used\": 5641, \"accel_energy_used\": 1340, \"nodes\": 32, \"nodes_power_capped\": 3, \"min_power_cap\": 155, \"min_power_cap_count\": 2, \"max_power_cap\": 355, \"max_power_cap_count\": 1, \"nodes_accel_power_capped\": 3, \"min_accel_power_cap\": 200, \"min_accel_power_cap_count\": 3, \"max_accel_power_cap\": 200, \"max_accel_power_cap_count\": 3, \"nodes_throttled\": 0, \"nodes_with_changed_power_cap\": 0}",
			"2014-06-17T10:06:54.026557-06:00 c0-0c1s1n2 RUR 3731 p0-20140616t214834 [RUR@34] uid: 12345, apid: 22542623, jobid: 0, cmdname: /lus/tmp/rur01.3657/./exit04-3657, plugin: taskstats ['utime', 4000, 'stime', 144000, 'max_rss', 7336, 'rchar', 252289, 'wchar', 741, 'exitcode:signal', ['0:9', '139:0', '0:11', '0:0'], 'core', 1]",
			"2014-06-17T10:06:54.026557-06:00 c0-0c1s1n2 RUR 3781 p0-20140616t214834 [RUR@34] uid: 12345, apid: 22542623, jobid: 58848.sdb, cmdname: ./monc, plugin: timestamp APP_START 2014-06-17T10:03:23BST APP_STOP 2014-06-17T10:08:03BST",
			"2015-06-21T11:37:24.480982-05:00 c0-0c0s0n2 RUR 7623 p0-20150621t091957 [RUR@34] uid: 12345, apid: 22542637, jobid: 0, cmdname: ./it.sh, plugin: taskstats {\"uid\": 12795, \"wcalls\": 37, \"pid\": 2997, \"vm\": 16348, \"jid\": 395136991233, \"bkiowait\": 1201616, \"majfault\": 1, \"etime\": 0, \"btime\": 1386098731, \"gid\": 0, \"ppid\": 2992, \"utime\": 0, \"nice\": 0, \"sched\": 0, \"nid\": \"92\", \"prid\": 0, \"comm\": \"mount\", \"stime\": 4000, \"wchar\": 3465, \"rss\": 1028, \"minfault\": 352, \"coremem\": 1109, \"ecode\": 0, \"rcalls\": 22, \"pjid\": 7045, \"pgswapcnt\": 0, \"rchar\": 12208}",
			"2015-06-21T11:38:24.480982-05:00 c0-0c0s0n2 RUR 23710 p0-20150621t091957 [RUR@34] uid: 12345, apid: 22542637, jobid: 0, cmdname: /bin/hostname, plugin: memory {\"current_freemem\": 21858372, \"meminfo\": {\"Active(anon)\": 35952, \"Slab\": 105824, \"Inactive(anon)\": 1104}, \"hugepages-2048kB\": {\"nr\": 5120, \"surplus\": 5120}, \"%_of_boot_mem\": [\"67.23\", \"67.23\", \"67.23\", \"67.22\", \"67.21\", \"67.18\", \"67.11\", \"67.04\", \"66.94\", \"66.83\", \"66.77\", \"66.66\", \"66.53\", \"66.38\", \"65.87\", \"65.07\", \"63.05\", \"61.43\"], \"boot_freemem\": 32432628}",
			"2015-06-21T11:38:24.480982-05:00 c0-0c0s0n2 RUR 23810 p0-20150621t091957 [RUR@34] uid: 12345, apid: 22542637, jobid: 58848.sdb, cmdname: ./monc, plugin: timestamp APP_START 2015-06-21T11:36:23BST APP_STOP 2015-06-21T11:40:03BST", 
			"2016-06-21T11:37:24.480982-05:00 c0-0c0s1n1 RUR 24393 p0-20160621t091957 [RUR@34] uid: 12345, apid: 22543522, jobid: 0, cmdname: /bin/cat, plugin: taskstats ['btime', 1386061749, 'etime', 8000, 'utime', 0, 'stime', 4000, 'coremem', 442, 'max_rss', 564, 'max_vm', 564, 'pgswapcnt', 63, 'minfault', 15, 'majfault', 48, 'rchar', 2608, 'wchar', 686, 'rcalls', 19, 'wcalls', 7, 'bkiowait', 1000, 'exitcode:signal', [0], 'core', 0]",
			"2016-06-21T11:38:24.480982-05:00 c0-0c0s1n1 RUR 33710 p0-20160621t091957 [RUR@34] uid: 12345, apid: 22543522, jobid: 0, cmdname: /bin/hostname, plugin: memory {\"current_freemem\": 21858372, \"meminfo\": {\"Active(anon)\": 35952, \"Slab\": 105824, \"Inactive(anon)\": 1104}, \"hugepages-2048kB\": {\"nr\": 5120, \"surplus\": 5120}, \"Node_0_zone_DMA\": [\"0.05\", \"0.05\", \"0.05\", \"0.05\", \"0.05\", \"0.05\", \"0.05\", \"0.05\", \"0.05\", \"0.04\", \"0.04\", \"0.03\", \"0.00\", \"0.00\", \"0.00\", \"0.00\", \"0.00\", \"0.00\"], \"%_of_boot_mem\": [\"67.23\", \"67.23\", \"67.23\", \"67.22\", \"67.21\", \"67.18\", \"67.11\", \"67.04\", \"66.94\", \"66.83\", \"66.77\", \"66.66\", \"66.53\", \"66.38\", \"65.87\", \"65.07\", \"63.05\", \"61.43\"], \"boot_freemem\": 32432628, \"Node_0_zone_DMA32\": [\"6.07\", \"6.07\", \"6.07\", \"6.07\", \"6.07\", \"6.07\", \"6.07\", \"6.06\", \"6.05\", \"6.04\", \"6.01\", \"5.94\", \"5.86\", \"5.76\", \"5.46\", \"4.85\", \"3.23\", \"3.23\"], \"Node_0_zone_Normal\": [\"61.11\", \"61.11\", \"61.11\", \"61.11\", \"61.09\", \"61.07\", \"60.99\", \"60.93\", \"60.84\", \"60.75\", \"60.72\", \"60.70\", \"60.67\", \"60.62\", \"60.42\", \"60.22\", \"59.81\", \"58.20\"]",
			"2016-06-21T11:39:24.480982-05:00 c0-0c0s1n1 RUR 33810 p0-20160621t091957 [RUR@34] uid: 12345, apid: 22543522, jobid: 58848.sdb, cmdname: ./monc, plugin: timestamp APP_START 2016-06-21T11:52:23BST APP_STOP 2016-06-21T11:55:03BST"
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

	
}