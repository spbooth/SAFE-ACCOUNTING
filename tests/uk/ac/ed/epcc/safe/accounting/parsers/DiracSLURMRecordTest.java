// Copyright - The University of Edinburgh 2015
package uk.ac.ed.epcc.safe.accounting.parsers;

import java.util.ArrayList;
import java.util.Collection;

import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.SkipRecord;

public class DiracSLURMRecordTest extends AbstractRecordTestCase {
	private static final Collection<RecordText> goodRecords = new ArrayList<>();
	private static final Collection<BadRecordText> badTexts = new ArrayList<>();

	public DiracSLURMRecordTest() {
		super("SLURMMachine", "SLURMRecord");
	}

	/**
	 * Tests are not going to be altered at runtime so we declare them all in a
	 * static block and add them to either the good records or bad records
	 * collection
	 */
	static {
		String validRecords[] = new String[] {
				
				"JobID=\"90205\" User=\"my304\" Group=\"my304\" Account=\"dirac-dp022\" JobName=\"Stochastic_3D_N_1e6_beta_7\" Partition=\"sandybridge\" Submit=\"2014-02-25T18:15:24\" Reserved=\"00:00:17\" Start=\"2014-02-25T18:15:51\" End=\"2014-02-27T00:52:27\" Elapsed=\"1-06:36:36\" NNodes=\"2\" NCPUS=\"32\" Timelimit=\"1-12:00:00\" ReqMem=\"0n\" ExitCode=\"0:0\" State=\"COMPLETED\"",
				"JobID=\"90242\" User=\"dc-alre1\" Group=\"dp020\" Account=\"dirac-dp020\" JobName=\"HOOH_save_ext_mat_all\" Partition=\"sandybridge\" Submit=\"2014-02-25T17:56:31\" Reserved=\"00:00:00\" Start=\"2014-02-25T17:56:31\" End=\"2014-02-25T17:56:31\" Elapsed=\"00:00:00\" NNodes=\"2\" NCPUS=\"0\" Timelimit=\"1-12:00:00\" ReqMem=\"62.50Gn\" ExitCode=\"0:1\" State=\"FAILED\"",
				"JobID=\"90240\" User=\"dc-alre1\" Group=\"dp020\" Account=\"dirac-dp020\" JobName=\"HOOH_save_ext_mat_all\" Partition=\"sandybridge\" Submit=\"2014-02-25T17:56:12\" Reserved=\"00:00:00\" Start=\"2014-02-25T17:56:12\" End=\"2014-02-25T17:56:12\" Elapsed=\"00:00:00\" NNodes=\"2\" NCPUS=\"0\" Timelimit=\"1-12:00:00\" ReqMem=\"62.50Gn\" ExitCode=\"0:1\" State=\"FAILED\"",
				"JobID=\"36862\" User=\"hpccha1\" Group=\"hpccha1\" Account=\"dirac-dp019\" JobName=\"phi6496_282\" Partition=\"sandybridge\" Submit=\"2014-02-07T19:16:43\" Reserved=\"00:00:00\" Start=\"2014-02-07T19:16:43\" End=\"2014-02-07T19:16:43\" Elapsed=\"00:00:00\" NNodes=\"2\" NCPUS=\"0\" Timelimit=\"07:00:00\" ReqMem=\"62.50Gn\" ExitCode=\"0:1\" State=\"FAILED\"", 
				"JobID=\"41976\" User=\"ch558\" Group=\"ch558\" Account=\"dirac-dp019\" JobName=\"rad200\" Partition=\"sandybridge\" Submit=\"2014-02-08T23:05:32\" Reserved=\"00:00:00\" Start=\"2014-02-08T23:05:32\" End=\"2014-02-08T23:05:32\" Elapsed=\"00:00:00\" NNodes=\"2\" NCPUS=\"0\" Timelimit=\"00:10:00\" ReqMem=\"62.50Gn\" ExitCode=\"0:1\" State=\"FAILED\"", 
				"JobID=\"11198\" User=\"dc-yach1\" Group=\"dp020\" Account=\"dirac-dp020\" JobName=\"c2h4_2m_400p_48_p9_save_split_3_3\" Partition=\"sandybridge\" Submit=\"2014-02-05T17:17:52\" Reserved=\"00:06:42\" Start=\"2014-02-05T17:24:34\" End=\"2014-02-05T17:24:34\" Elapsed=\"00:00:00\" NNodes=\"2\" NCPUS=\"0\" Timelimit=\"1-12:00:00\" ReqMem=\"63900Mn\" ExitCode=\"0:0\" State=\"CANCELLED by 20265\"",
				"JobID=\"4901\" User=\"jash2\" Group=\"dp002\" Account=\"dirac-dp002\" JobName=\"140Primal\" Partition=\"sandybridge\" Submit=\"2014-02-04T11:31:45\" Reserved=\"00:00:04\" Start=\"2014-02-04T11:31:49\" End=\"2014-02-05T23:01:59\" Elapsed=\"1-11:30:10\" NNodes=\"2\" NCPUS=\"32\" Timelimit=\"1-12:00:00\" ReqMem=\"0n\" ExitCode=\"0:0\" State=\"COMPLETED\"",
				"JobID=\"11334\" User=\"mm855\" Group=\"dp002\" Account=\"dirac-dp002\" JobName=\"CM_Halofinder_PICOLA_L768_N1024_R101\" Partition=\"sandybridge\" Submit=\"2014-02-05T19:29:04\" Reserved=\"00:00:00\" Start=\"2014-02-05T19:29:04\" End=\"2014-02-05T19:29:04\" Elapsed=\"00:00:00\" NNodes=\"2\" NCPUS=\"0\" Timelimit=\"02:00:00\" ReqMem=\"255600Mn\" ExitCode=\"0:1\" State=\"FAILED\"",
						
				"JobID=\"175121\" User=\"dc-vido1\" Group=\"dp001\" Account=\"dirac-dp001\" JobName=\"20140326-hd189733-aug13-starg11-10rstar\" Partition=\"sandybridge\" Submit=\"2014-03-26T10:39:40\" Reserved=\"4-20:27:16\" Start=\"2014-03-31T08:06:56\" End=\"2014-03-31T08:06:56\" Elapsed=\"00:00:00\" NNodes=\"2\" NCPUS=\"0\" Timelimit=\"1-00:00:00\" ReqMem=\"0n\" ExitCode=\"0:0\" State=\"CANCELLED by 20019\"", 
				"JobID=\"870\" User=\"dc-yurc1\" Group=\"dp020\" Account=\"dirac-dp020\" JobName=\"h2co_nonrigid_saveall\" Partition=\"sandybridge\" Submit=\"2014-02-01T19:02:20\" Reserved=\"00:00:00\" Start=\"2014-02-01T19:02:20\" End=\"2014-02-01T23:36:28\" Elapsed=\"04:34:08\" NNodes=\"1\" NCPUS=\"16\" Timelimit=\"1-12:00:00\" ReqMem=\"63900Mn\" ExitCode=\"0:0\" State=\"COMPLETED\"",
				"JobID=\"995\" User=\"dc-yurc1\" Group=\"dp020\" Account=\"dirac-dp020\" JobName=\"$name\" Partition=\"sandybridge\" Submit=\"2014-02-01T23:15:59\" Reserved=\"00:00:00\" Start=\"2014-02-01T23:15:59\" End=\"2014-02-01T23:16:01\" Elapsed=\"00:00:02\" NNodes=\"1\" NCPUS=\"16\" Timelimit=\"1-12:00:00\" ReqMem=\"0n\" ExitCode=\"0:0\" State=\"COMPLETED\"",
				"JobID=\"996\" User=\"dc-yurc1\" Group=\"dp020\" Account=\"dirac-dp020\" JobName=\"ch4_1.1_matdivide-4\" Partition=\"sandybridge\" Submit=\"2014-02-01T23:17:54\" Reserved=\"00:00:00\" Start=\"2014-02-01T23:17:54\" End=\"2014-02-01T23:18:41\" Elapsed=\"00:00:47\" NNodes=\"1\" NCPUS=\"16\" Timelimit=\"1-12:00:00\" ReqMem=\"63900Mn\" ExitCode=\"0:0\" State=\"CANCELLED by 20091\"",
				"JobID=\"997\" User=\"dc-yurc1\" Group=\"dp020\" Account=\"dirac-dp020\" JobName=\"ch4_1.1_matdivide-4\" Partition=\"sandybridge\" Submit=\"2014-02-01T23:18:46\" Reserved=\"00:00:00\" Start=\"2014-02-01T23:18:46\" End=\"2014-02-01T23:18:58\" Elapsed=\"00:00:12\" NNodes=\"1\" NCPUS=\"16\" Timelimit=\"1-00:00:00\" ReqMem=\"63900Mn\" ExitCode=\"0:0\" State=\"CANCELLED by 20091\"", 
				"JobID=10603 User=rd419 Group=rd419 Account=dirac-dp019-gpu JobName=gpujob Partition=tesla Submit=2013-12-19T16:30:25 Reserved=00:00:00 Start=2013-12-19T16:30:25 End=2013-12-19T16:30:29 Elapsed=00:00:04 NNodes=1 NCPUS=12 Timelimit=01:00:00 ReqMem=0n ExitCode=0:0 State=COMPLETED", 
						"JobID=11234 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=test Partition=tesla Submit=2013-12-23T02:43:48 Reserved=00:00:00 Start=2013-12-23T02:43:48 End=2013-12-23T02:53:48 Elapsed=00:10:00 NNodes=4 NCPUS=48 Timelimit=02:00:00 ReqMem=0n ExitCode=0:0 State=COMPLETED", 
						"JobID=11236 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=test Partition=tesla Submit=2013-12-23T03:16:56 Reserved=00:00:00 Start=2013-12-23T03:16:56 End=2013-12-23T03:27:31 Elapsed=00:10:35 NNodes=4 NCPUS=48 Timelimit=02:00:00 ReqMem=0n ExitCode=0:0 State=COMPLETED", 
						"JobID=11483 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=test Partition=tesla Submit=2013-12-23T15:43:40 Reserved=00:00:00 Start=2013-12-23T15:43:40 End=2013-12-23T15:51:05 Elapsed=00:07:25 NNodes=4 NCPUS=48 Timelimit=02:00:00 ReqMem=0n ExitCode=0:0 State=COMPLETED", 
						"JobID=11484 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=test Partition=tesla Submit=2013-12-23T15:58:39 Reserved=00:00:00 Start=2013-12-23T15:58:39 End=2013-12-23T16:06:07 Elapsed=00:07:28 NNodes=4 NCPUS=48 Timelimit=02:00:00 ReqMem=0n ExitCode=0:0 State=COMPLETED", 
						"JobID=11492 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-23T16:14:19 Reserved=00:00:00 Start=2013-12-23T16:14:19 End=2013-12-23T20:54:19 Elapsed=04:40:00 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=0:0 State=COMPLETED", 
						"JobID=11499 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-23T16:29:04 Reserved=00:00:00 Start=2013-12-23T16:29:04 End=2013-12-23T21:12:29 Elapsed=04:43:25 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=0:0 State=COMPLETED", 
						"JobID=11504 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-23T16:32:51 Reserved=00:00:00 Start=2013-12-23T16:32:51 End=2013-12-23T21:13:15 Elapsed=04:40:24 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=0:0 State=COMPLETED", 
						"JobID=12065 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-24T21:38:34 Reserved=00:00:00 Start=2013-12-24T21:38:34 End=2013-12-25T02:23:43 Elapsed=04:45:09 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=0:0 State=COMPLETED", 
						"JobID=12072 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-24T22:03:42 Reserved=00:00:00 Start=2013-12-24T22:03:42 End=2013-12-25T02:49:03 Elapsed=04:45:21 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=0:0 State=COMPLETED",
						"JobID=12074 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-24T22:09:42 Reserved=00:00:00 Start=2013-12-24T22:09:42 End=2013-12-25T02:47:44 Elapsed=04:38:02 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=0:0 State=COMPLETED", 
						"JobID=12075 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-24T22:12:42 Reserved=00:00:00 Start=2013-12-24T22:12:42 End=2013-12-25T02:56:34 Elapsed=04:43:52 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=0:0 State=COMPLETED", 
						"JobID=12076 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-24T22:33:09 Reserved=00:00:00 Start=2013-12-24T22:33:09 End=2013-12-25T03:19:02 Elapsed=04:45:53 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=0:0 State=COMPLETED", 
						"JobID=12077 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-24T22:37:09 Reserved=00:00:00 Start=2013-12-24T22:37:09 End=2013-12-25T03:23:10 Elapsed=04:46:01 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=0:0 State=COMPLETED", 
						"JobID=12078 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-24T22:41:09 Reserved=00:00:00 Start=2013-12-24T22:41:09 End=2013-12-25T03:24:54 Elapsed=04:43:45 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=0:0 State=COMPLETED", 
						"JobID=12079 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-24T22:45:09 Reserved=00:00:00 Start=2013-12-24T22:45:09 End=2013-12-25T03:25:52 Elapsed=04:40:43 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=0:0 State=COMPLETED", 
						"JobID=12081 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-24T22:53:09 Reserved=00:00:00 Start=2013-12-24T22:53:09 End=2013-12-25T03:34:16 Elapsed=04:41:07 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=0:0 State=COMPLETED",
						"JobID=12083 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-24T23:01:09 Reserved=00:00:00 Start=2013-12-24T23:01:09 End=2013-12-25T03:44:17 Elapsed=04:43:08 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=0:0 State=COMPLETED",
						"JobID=13011 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-30T22:41:48 Reserved=00:00:00 Start=2013-12-30T22:41:48 End=2013-12-31T04:24:17 Elapsed=05:42:29 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=0:0 State=COMPLETED", 
						"JobID=13013 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-30T22:49:47 Reserved=00:00:00 Start=2013-12-30T22:49:47 End=2013-12-31T03:48:38 Elapsed=04:58:51 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=0:0 State=COMPLETED", 
						"JobID=13017 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-30T23:05:48 Reserved=00:30:07 Start=2013-12-30T23:35:55 End=2013-12-31T04:43:34 Elapsed=05:07:39 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=0:0 State=COMPLETED", 
						"JobID=13020 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-30T23:13:48 Reserved=00:54:12 Start=2013-12-31T00:08:00 End=2013-12-31T04:55:02 Elapsed=04:47:02 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=0:0 State=COMPLETED", 
						"JobID=13021 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-30T23:17:48 Reserved=00:53:56 Start=2013-12-31T00:11:44 End=2013-12-31T05:00:06 Elapsed=04:48:22 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=0:0 State=COMPLETED", 
						"JobID=13022 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-30T23:21:48 Reserved=01:00:09 Start=2013-12-31T00:21:57 End=2013-12-31T05:07:52 Elapsed=04:45:55 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=0:0 State=COMPLETED", 
						"JobID=13023 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-30T23:25:48 Reserved=01:50:01 Start=2013-12-31T01:15:49 End=2013-12-31T06:07:50 Elapsed=04:52:01 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=0:0 State=COMPLETED",
						"JobID=13030 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-31T04:55:02 Reserved=00:12:52 Start=2013-12-31T05:07:54 End=2013-12-31T05:08:03 Elapsed=00:00:09 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=0:0 State=COMPLETED", 
						"JobID=13031 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-31T05:00:06 Reserved=00:07:59 Start=2013-12-31T05:08:05 End=2013-12-31T05:08:10 Elapsed=00:00:05 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=0:0 State=COMPLETED", 
						"JobID=13032 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-31T05:07:52 Reserved=00:00:20 Start=2013-12-31T05:08:12 End=2013-12-31T05:08:17 Elapsed=00:00:05 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=0:0 State=COMPLETED", 
						"JobID=13033 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-31T06:07:50 Reserved=00:00:00 Start=2013-12-31T06:07:50 End=2013-12-31T06:07:54 Elapsed=00:00:04 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=0:0 State=COMPLETED",
						
						"JobID=11229 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=test Partition=tesla Submit=2013-12-23T02:06:42 Reserved=00:00:00 Start=2013-12-23T02:06:42 End=2013-12-23T02:06:47 Elapsed=00:00:05 NNodes=4 NCPUS=48 Timelimit=02:00:00 ReqMem=0n ExitCode=1:0 State=FAILED", 
						"JobID=11230 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=test Partition=tesla Submit=2013-12-23T02:09:02 Reserved=00:00:00 Start=2013-12-23T02:09:02 End=2013-12-23T02:09:07 Elapsed=00:00:05 NNodes=4 NCPUS=48 Timelimit=02:00:00 ReqMem=0n ExitCode=1:0 State=FAILED",
						"JobID=11231 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=test Partition=tesla Submit=2013-12-23T02:10:41 Reserved=00:00:00 Start=2013-12-23T02:10:41 End=2013-12-23T02:20:10 Elapsed=00:09:29 NNodes=4 NCPUS=48 Timelimit=02:00:00 ReqMem=0n ExitCode=0:0 State=CANCELLED by 9792", 
						"JobID=11232 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=test Partition=tesla Submit=2013-12-23T02:25:43 Reserved=00:00:00 Start=2013-12-23T02:25:43 End=2013-12-23T02:31:50 Elapsed=00:06:07 NNodes=4 NCPUS=48 Timelimit=02:00:00 ReqMem=0n ExitCode=0:0 State=CANCELLED by 9792", 
						"JobID=11233 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=test Partition=tesla Submit=2013-12-23T02:34:08 Reserved=00:00:00 Start=2013-12-23T02:34:08 End=2013-12-23T03:08:18 Elapsed=00:34:10 NNodes=4 NCPUS=48 Timelimit=02:00:00 ReqMem=0n ExitCode=0:0 State=CANCELLED by 9792", 
						"JobID=11235 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=test Partition=tesla Submit=2013-12-23T03:12:37 Reserved=00:00:00 Start=2013-12-23T03:12:37 End=2013-12-23T03:13:31 Elapsed=00:00:54 NNodes=4 NCPUS=48 Timelimit=02:00:00 ReqMem=0n ExitCode=0:0 State=CANCELLED by 9792",
						"JobID=11490 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-23T16:13:27 Reserved=00:00:00 Start=2013-12-23T16:13:27 End=2013-12-23T16:13:32 Elapsed=00:00:05 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=2:0 State=FAILED", 
						"JobID=12064 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-24T21:35:53 Reserved=00:00:00 Start=2013-12-24T21:35:53 End=2013-12-24T22:10:50 Elapsed=00:34:57 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=1:0 State=FAILED",  
						"JobID=12068 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-24T21:51:38 Reserved=00:00:00 Start=2013-12-24T21:51:38 End=2013-12-24T22:24:43 Elapsed=00:33:05 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=1:0 State=FAILED", 
						"JobID=12069 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-24T21:54:42 Reserved=00:00:00 Start=2013-12-24T21:54:42 End=2013-12-24T22:26:46 Elapsed=00:32:04 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=1:0 State=FAILED", 
						"JobID=12070 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-24T21:57:42 Reserved=00:00:00 Start=2013-12-24T21:57:42 End=2013-12-24T23:13:17 Elapsed=01:15:35 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=1:0 State=FAILED", 
						"JobID=12071 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-24T22:00:42 Reserved=00:00:00 Start=2013-12-24T22:00:42 End=2013-12-24T23:13:50 Elapsed=01:13:08 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=1:0 State=FAILED", 
						"JobID=12073 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-24T22:06:42 Reserved=00:00:00 Start=2013-12-24T22:06:42 End=2013-12-25T00:17:44 Elapsed=02:11:02 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=1:0 State=FAILED",  
						"JobID=12080 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-24T22:49:09 Reserved=00:00:00 Start=2013-12-24T22:49:09 End=2013-12-24T23:43:31 Elapsed=00:54:22 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=1:0 State=FAILED", 
						"JobID=12082 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-24T22:57:09 Reserved=00:00:00 Start=2013-12-24T22:57:09 End=2013-12-24T23:28:29 Elapsed=00:31:20 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=1:0 State=FAILED", 
						"JobID=12084 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-24T23:05:09 Reserved=00:00:00 Start=2013-12-24T23:05:09 End=2013-12-25T00:25:13 Elapsed=01:20:04 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=1:0 State=FAILED", 
						"JobID=12085 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-24T23:09:09 Reserved=00:00:00 Start=2013-12-24T23:09:09 End=2013-12-25T00:25:01 Elapsed=01:15:52 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=1:0 State=FAILED",  
						"JobID=13012 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-30T22:45:47 Reserved=00:00:00 Start=2013-12-30T22:45:47 End=2013-12-31T00:11:42 Elapsed=01:25:55 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=1:0 State=FAILED",  
						"JobID=13014 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-30T22:53:47 Reserved=00:00:00 Start=2013-12-30T22:53:47 End=2013-12-30T23:23:04 Elapsed=00:29:17 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=1:0 State=FAILED", 
						"JobID=13015 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-30T22:57:48 Reserved=00:09:29 Start=2013-12-30T23:07:17 End=2013-12-31T00:21:55 Elapsed=01:14:38 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=1:0 State=FAILED", 
						"JobID=13016 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-30T23:01:48 Reserved=00:21:19 Start=2013-12-30T23:23:07 End=2013-12-31T00:07:57 Elapsed=00:44:50 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=1:0 State=FAILED", 
						"JobID=13019 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-30T23:09:48 Reserved=00:30:31 Start=2013-12-30T23:40:19 End=2013-12-31T01:15:24 Elapsed=01:35:05 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=1:0 State=FAILED", 
						"JobID=13024 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-30T23:29:48 Reserved=04:18:53 Start=2013-12-31T03:48:41 End=2013-12-31T06:32:51 Elapsed=02:44:10 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=1:0 State=FAILED", 
						"JobID=13025 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-30T23:33:48 Reserved=04:50:31 Start=2013-12-31T04:24:19 End=2013-12-31T06:35:08 Elapsed=02:10:49 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=1:0 State=FAILED", 
						"JobID=13026 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-30T23:37:48 Reserved=05:05:49 Start=2013-12-31T04:43:37 End=2013-12-31T07:40:33 Elapsed=02:56:56 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=1:0 State=FAILED", 
						"JobID=13039 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-31T10:34:42 Reserved=00:00:00 Start=2013-12-31T10:34:42 End=2013-12-31T13:37:42 Elapsed=03:03:00 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=1:0 State=FAILED", 
						"JobID=13040 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-31T10:35:01 Reserved=00:00:00 Start=2013-12-31T10:35:01 End=2013-12-31T10:35:19 Elapsed=00:00:18 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=1:0 State=FAILED", 
						"JobID=13041 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-31T10:39:01 Reserved=00:00:00 Start=2013-12-31T10:39:01 End=2013-12-31T10:39:18 Elapsed=00:00:17 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=1:0 State=FAILED", 
						"JobID=13042 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-31T10:43:01 Reserved=00:00:00 Start=2013-12-31T10:43:01 End=2013-12-31T10:43:45 Elapsed=00:00:44 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=1:0 State=FAILED", 
						"JobID=13043 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-31T10:47:01 Reserved=00:00:00 Start=2013-12-31T10:47:01 End=2013-12-31T10:47:45 Elapsed=00:00:44 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=1:0 State=FAILED", 
						"JobID=13044 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-31T10:51:01 Reserved=00:00:00 Start=2013-12-31T10:51:01 End=2013-12-31T10:51:46 Elapsed=00:00:45 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=1:0 State=FAILED", 
						"JobID=13045 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-31T10:55:01 Reserved=00:00:00 Start=2013-12-31T10:55:01 End=2013-12-31T10:55:45 Elapsed=00:00:44 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=1:0 State=FAILED", 
						"JobID=13046 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-31T10:59:01 Reserved=00:00:00 Start=2013-12-31T10:59:01 End=2013-12-31T10:59:45 Elapsed=00:00:44 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=1:0 State=FAILED", 
						"JobID=13047 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-31T11:03:01 Reserved=00:00:00 Start=2013-12-31T11:03:01 End=2013-12-31T11:03:45 Elapsed=00:00:44 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=1:0 State=FAILED", 
						"JobID=13048 User=cet34 Group=cet34 Account=dirac-dp019-gpu JobName=prop Partition=tesla Submit=2013-12-31T11:07:01 Reserved=00:00:00 Start=2013-12-31T11:07:01 End=2013-12-31T11:07:45 Elapsed=00:00:44 NNodes=4 NCPUS=48 Timelimit=10:00:00 ReqMem=0n ExitCode=1:0 State=FAILED", 	
						"JobID=\"167520\" User=\"dc-barb2\" Group=\"dp013\" Account=\"dirac-dp013\" JobName=\"Ca46hw28_Gitr_ddd\" Partition=\"sandybridge\" Submit=\"2014-03-23T09:44:30\" Reserved=\"INVALID\" Start=\"2014-03-23T09:45:24\" End=\"2014-03-23T09:45:24\" Elapsed=\"00:00:00\" NNodes=\"2\" NCPUS=\"64\" Timelimit=\"10:00:00\" ReqMem=\"0n\" ExitCode=\"0:0\" State=\"CANCELLED by 20314\"",
						"JobID=\"735068\" User=\"sh759\" Group=\"sh759\" Account=\"dirac-dp012\" JobName=\"g3\" Partition=\"sandybridge\" Submit=\"2014-10-21T10:28:34\" Reserved=\"05:34:38\" Start=\"2014-10-21T16:03:22\" End=\"2014-10-22T22:42:01\" Elapsed=\"1-06:38:39\" NNodes=\"16\" NCPUS=\"256\" Timelimit=\"1-12:00:00\" ReqMem=\"0n\" ExitCode=\"0:0\" State=\"COMPLETED\"",
						
						"JobID=\"57795\" User=\"dc-rich4\" Group=\"dp004\" Account=\"dp004\" JobName=\"1013_MID\" Partition=\"cosma6\" Submit=\"2017-04-08T03:36:09\" Reserved=\"00:00:01\" Start=\"2017-04-08T03:36:10\" End=\"2017-04-10T05:36:17\" Elapsed=\"2-02:00:07\" NNodes=\"32\" NCPUS=\"512\" Timelimit=\"3-00:00:00\" ReqMem=\"120000Mn\" ExitCode=\"15:0\" State=\"FAILED\" resources_used_mem=71833671K",
						"JobID=\"57796\" User=\"dc-rich4\" Group=\"dp004\" Account=\"dp004\" JobName=\"1013_HI\" Partition=\"cosma6\" Submit=\"2017-04-08T04:03:01\" Reserved=\"00:00:01\" Start=\"2017-04-08T04:03:02\" End=\"2017-04-10T17:18:07\" Elapsed=\"2-13:15:05\" NNodes=\"64\" NCPUS=\"1024\" Timelimit=\"3-00:00:00\" ReqMem=\"120000Mn\" ExitCode=\"15:0\" State=\"FAILED\" resources_used_mem=572207861K",
						"JobID=\"57804\" User=\"llinares\" Group=\"dp004\" Account=\"dp004\" JobName=\"job_name\" Partition=\"cosma6\" Submit=\"2017-04-10T07:58:49\" Reserved=\"00:00:01\" Start=\"2017-04-10T07:58:50\" End=\"2017-04-10T07:59:45\" Elapsed=\"00:00:55\" NNodes=\"4\" NCPUS=\"64\" Timelimit=\"00:30:00\" ReqMem=\"120000Mn\" ExitCode=\"0:0\" State=\"COMPLETED\" resources_used_mem=2018796K",
						"JobID=\"57805\" User=\"llinares\" Group=\"dp004\" Account=\"dp004\" JobName=\"run_01\" Partition=\"cosma6\" Submit=\"2017-04-10T08:01:50\" Reserved=\"00:00:01\" Start=\"2017-04-10T08:01:51\" End=\"2017-04-10T09:20:44\" Elapsed=\"01:18:53\" NNodes=\"8\" NCPUS=\"128\" Timelimit=\"02:00:00\" ReqMem=\"120000Mn\" ExitCode=\"0:0\" State=\"CANCELLED by 64681\" resources_used_mem=447605628K",
						"JobID=\"57806\" User=\"llinares\" Group=\"dp004\" Account=\"dp004\" JobName=\"run_s_01\" Partition=\"cosma6\" Submit=\"2017-04-10T08:01:51\" Reserved=\"00:00:00\" Start=\"2017-04-10T08:01:51\" End=\"2017-04-10T09:20:44\" Elapsed=\"01:18:53\" NNodes=\"8\" NCPUS=\"128\" Timelimit=\"04:00:00\" ReqMem=\"120000Mn\" ExitCode=\"0:0\" State=\"CANCELLED by 64681\" resources_used_mem=691967883K",
						"JobID=\"57807\" User=\"llinares\" Group=\"dp004\" Account=\"dp004\" JobName=\"run_02\" Partition=\"cosma6\" Submit=\"2017-04-10T08:01:51\" Reserved=\"00:00:00\" Start=\"2017-04-10T08:01:51\" End=\"2017-04-10T09:20:44\" Elapsed=\"01:18:53\" NNodes=\"8\" NCPUS=\"128\" Timelimit=\"02:00:00\" ReqMem=\"120000Mn\" ExitCode=\"0:0\" State=\"CANCELLED by 64681\" resources_used_mem=444685703K",
						"JobID=\"57808\" User=\"llinares\" Group=\"dp004\" Account=\"dp004\" JobName=\"run_s_02\" Partition=\"cosma6\" Submit=\"2017-04-10T08:01:51\" Reserved=\"00:00:00\" Start=\"2017-04-10T08:01:51\" End=\"2017-04-10T09:20:44\" Elapsed=\"01:18:53\" NNodes=\"8\" NCPUS=\"128\" Timelimit=\"04:00:00\" ReqMem=\"120000Mn\" ExitCode=\"0:0\" State=\"CANCELLED by 64681\" resources_used_mem=710431531K",
						"JobID=\"57809\" User=\"llinares\" Group=\"dp004\" Account=\"dp004\" JobName=\"run_03\" Partition=\"cosma6\" Submit=\"2017-04-10T08:01:51\" Reserved=\"00:00:00\" Start=\"2017-04-10T08:01:51\" End=\"2017-04-10T09:20:44\" Elapsed=\"01:18:53\" NNodes=\"8\" NCPUS=\"128\" Timelimit=\"02:00:00\" ReqMem=\"120000Mn\" ExitCode=\"0:0\" State=\"CANCELLED by 64681\" resources_used_mem=462686567K",
						"JobID=\"57810\" User=\"llinares\" Group=\"dp004\" Account=\"dp004\" JobName=\"run_s_03\" Partition=\"cosma6\" Submit=\"2017-04-10T08:01:51\" Reserved=\"00:00:00\" Start=\"2017-04-10T08:01:51\" End=\"2017-04-10T09:20:44\" Elapsed=\"01:18:53\" NNodes=\"8\" NCPUS=\"128\" Timelimit=\"04:00:00\" ReqMem=\"120000Mn\" ExitCode=\"0:0\" State=\"CANCELLED by 64681\" resources_used_mem=699101628K",
						"JobID=\"57811\" User=\"llinares\" Group=\"dp004\" Account=\"dp004\" JobName=\"run_04\" Partition=\"cosma6\" Submit=\"2017-04-10T08:01:51\" Reserved=\"00:00:00\" Start=\"2017-04-10T08:01:51\" End=\"2017-04-10T09:20:44\" Elapsed=\"01:18:53\" NNodes=\"8\" NCPUS=\"128\" Timelimit=\"02:00:00\" ReqMem=\"120000Mn\" ExitCode=\"0:0\" State=\"CANCELLED by 64681\" resources_used_mem=440411184K",
						"JobID=\"57812\" User=\"llinares\" Group=\"dp004\" Account=\"dp004\" JobName=\"run_s_04\" Partition=\"cosma6\" Submit=\"2017-04-10T08:01:51\" Reserved=\"00:00:00\" Start=\"2017-04-10T08:01:51\" End=\"2017-04-10T09:20:45\" Elapsed=\"01:18:54\" NNodes=\"8\" NCPUS=\"128\" Timelimit=\"04:00:00\" ReqMem=\"120000Mn\" ExitCode=\"0:0\" State=\"CANCELLED by 64681\" resources_used_mem=703819800K",
						"JobID=\"57813\" User=\"llinares\" Group=\"dp004\" Account=\"dp004\" JobName=\"run_05\" Partition=\"cosma6\" Submit=\"2017-04-10T08:01:51\" Reserved=\"00:00:03\" Start=\"2017-04-10T08:01:54\" End=\"2017-04-10T09:20:45\" Elapsed=\"01:18:51\" NNodes=\"8\" NCPUS=\"128\" Timelimit=\"02:00:00\" ReqMem=\"120000Mn\" ExitCode=\"0:0\" State=\"CANCELLED by 64681\" resources_used_mem=436914087K",
						"JobID=\"57814\" User=\"llinares\" Group=\"dp004\" Account=\"dp004\" JobName=\"run_s_05\" Partition=\"cosma6\" Submit=\"2017-04-10T08:01:51\" Reserved=\"00:00:03\" Start=\"2017-04-10T08:01:54\" End=\"2017-04-10T09:20:45\" Elapsed=\"01:18:51\" NNodes=\"8\" NCPUS=\"128\" Timelimit=\"04:00:00\" ReqMem=\"120000Mn\" ExitCode=\"0:0\" State=\"CANCELLED by 64681\" resources_used_mem=669574499K",
						"JobID=\"57815\" User=\"llinares\" Group=\"dp004\" Account=\"dp004\" JobName=\"run_06\" Partition=\"cosma6\" Submit=\"2017-04-10T08:01:51\" Reserved=\"00:00:03\" Start=\"2017-04-10T08:01:54\" End=\"2017-04-10T09:20:45\" Elapsed=\"01:18:51\" NNodes=\"8\" NCPUS=\"128\" Timelimit=\"02:00:00\" ReqMem=\"120000Mn\" ExitCode=\"0:0\" State=\"CANCELLED by 64681\" resources_used_mem=432839832K",
						"JobID=\"57816\" User=\"llinares\" Group=\"dp004\" Account=\"dp004\" JobName=\"run_s_06\" Partition=\"cosma6\" Submit=\"2017-04-10T08:01:51\" Reserved=\"00:00:03\" Start=\"2017-04-10T08:01:54\" End=\"2017-04-10T09:20:45\" Elapsed=\"01:18:51\" NNodes=\"8\" NCPUS=\"128\" Timelimit=\"04:00:00\" ReqMem=\"120000Mn\" ExitCode=\"0:0\" State=\"CANCELLED by 64681\" resources_used_mem=698748468K",
						"JobID=\"57817\" User=\"llinares\" Group=\"dp004\" Account=\"dp004\" JobName=\"run_07\" Partition=\"cosma6\" Submit=\"2017-04-10T08:01:52\" Reserved=\"00:00:02\" Start=\"2017-04-10T08:01:54\" End=\"2017-04-10T09:20:45\" Elapsed=\"01:18:51\" NNodes=\"8\" NCPUS=\"128\" Timelimit=\"02:00:00\" ReqMem=\"120000Mn\" ExitCode=\"0:0\" State=\"CANCELLED by 64681\" resources_used_mem=424992088K",
						"JobID=\"57818\" User=\"llinares\" Group=\"dp004\" Account=\"dp004\" JobName=\"run_s_07\" Partition=\"cosma6\" Submit=\"2017-04-10T08:09:37\" Reserved=\"01:09:07\" Start=\"2017-04-10T09:20:45\" End=\"2017-04-10T09:20:45\" Elapsed=\"00:00:00\" NNodes=\"8\" NCPUS=\"128\" Timelimit=\"04:00:00\" ReqMem=\"120000Mn\" ExitCode=\"0:0\" State=\"CANCELLED by 64681\" resources_used_mem=0K",
						"JobID=\"57819\" User=\"llinares\" Group=\"dp004\" Account=\"dp004\" JobName=\"run_08\" Partition=\"cosma6\" Submit=\"2017-04-10T08:01:52\" Reserved=\"00:00:02\" Start=\"2017-04-10T08:01:54\" End=\"2017-04-10T09:20:45\" Elapsed=\"01:18:51\" NNodes=\"8\" NCPUS=\"128\" Timelimit=\"02:00:00\" ReqMem=\"120000Mn\" ExitCode=\"0:0\" State=\"CANCELLED by 64681\" resources_used_mem=432973920K",
						"JobID=\"57820\" User=\"llinares\" Group=\"dp004\" Account=\"dp004\" JobName=\"run_s_08\" Partition=\"cosma6\" Submit=\"2017-04-10T08:01:52\" Reserved=\"00:00:02\" Start=\"2017-04-10T08:01:54\" End=\"2017-04-10T09:20:45\" Elapsed=\"01:18:51\" NNodes=\"8\" NCPUS=\"128\" Timelimit=\"04:00:00\" ReqMem=\"120000Mn\" ExitCode=\"0:0\" State=\"CANCELLED by 64681\" resources_used_mem=724918684K",
						"JobID=\"57821\" User=\"llinares\" Group=\"dp004\" Account=\"dp004\" JobName=\"run_09\" Partition=\"cosma6\" Submit=\"2017-04-10T08:01:52\" Reserved=\"00:00:02\" Start=\"2017-04-10T08:01:54\" End=\"2017-04-10T09:20:45\" Elapsed=\"01:18:51\" NNodes=\"8\" NCPUS=\"128\" Timelimit=\"02:00:00\" ReqMem=\"120000Mn\" ExitCode=\"0:0\" State=\"CANCELLED by 64681\" resources_used_mem=470750471K",
						"JobID=\"58106_50\" User=\"wmfw23\" Group=\"dp004\" Account=\"dp004\" JobName=\"EglSrvy_chunk\" Partition=\"cosma6\" Submit=\"2017-04-10T12:22:20\" Reserved=\"00:04:47\" Start=\"2017-04-10T12:27:07\" End=\"2017-04-10T14:03:17\" Elapsed=\"01:36:10\" NNodes=\"1\" NCPUS=\"16\" Timelimit=\"1-00:00:00\" ReqMem=\"110Gn\" ExitCode=\"0:0\" State=\"COMPLETED\" resources_used_mem=501576K",
						"JobID=\"58106_51\" User=\"wmfw23\" Group=\"dp004\" Account=\"dp004\" JobName=\"EglSrvy_chunk\" Partition=\"cosma6\" Submit=\"2017-04-10T12:22:20\" Reserved=\"00:04:47\" Start=\"2017-04-10T12:27:07\" End=\"2017-04-10T13:27:11\" Elapsed=\"01:00:04\" NNodes=\"1\" NCPUS=\"16\" Timelimit=\"1-00:00:00\" ReqMem=\"110Gn\" ExitCode=\"0:0\" State=\"COMPLETED\" resources_used_mem=9414280K",
						"JobID=\"58106_52\" User=\"wmfw23\" Group=\"dp004\" Account=\"dp004\" JobName=\"EglSrvy_chunk\" Partition=\"cosma6\" Submit=\"2017-04-10T12:22:20\" Reserved=\"00:04:47\" Start=\"2017-04-10T12:27:07\" End=\"2017-04-10T13:30:05\" Elapsed=\"01:02:58\" NNodes=\"1\" NCPUS=\"16\" Timelimit=\"1-00:00:00\" ReqMem=\"110Gn\" ExitCode=\"0:0\" State=\"COMPLETED\" resources_used_mem=1986128K",
						"JobID=\"58106_53\" User=\"wmfw23\" Group=\"dp004\" Account=\"dp004\" JobName=\"EglSrvy_chunk\" Partition=\"cosma6\" Submit=\"2017-04-10T12:22:20\" Reserved=\"00:04:47\" Start=\"2017-04-10T12:27:07\" End=\"2017-04-10T13:29:50\" Elapsed=\"01:02:43\" NNodes=\"1\" NCPUS=\"16\" Timelimit=\"1-00:00:00\" ReqMem=\"110Gn\" ExitCode=\"0:0\" State=\"COMPLETED\" resources_used_mem=181184K",
						"JobID=\"58106_54\" User=\"wmfw23\" Group=\"dp004\" Account=\"dp004\" JobName=\"EglSrvy_chunk\" Partition=\"cosma6\" Submit=\"2017-04-10T12:22:20\" Reserved=\"00:04:47\" Start=\"2017-04-10T12:27:07\" End=\"2017-04-10T13:25:16\" Elapsed=\"00:58:09\" NNodes=\"1\" NCPUS=\"16\" Timelimit=\"1-00:00:00\" ReqMem=\"110Gn\" ExitCode=\"0:0\" State=\"COMPLETED\" resources_used_mem=492128K",
						"JobID=\"58106_55\" User=\"wmfw23\" Group=\"dp004\" Account=\"dp004\" JobName=\"EglSrvy_chunk\" Partition=\"cosma6\" Submit=\"2017-04-10T12:22:20\" Reserved=\"00:04:47\" Start=\"2017-04-10T12:27:07\" End=\"2017-04-10T14:04:32\" Elapsed=\"01:37:25\" NNodes=\"1\" NCPUS=\"16\" Timelimit=\"1-00:00:00\" ReqMem=\"110Gn\" ExitCode=\"0:0\" State=\"COMPLETED\" resources_used_mem=651836K",
						"JobID=\"58106_56\" User=\"wmfw23\" Group=\"dp004\" Account=\"dp004\" JobName=\"EglSrvy_chunk\" Partition=\"cosma6\" Submit=\"2017-04-10T12:22:20\" Reserved=\"00:04:47\" Start=\"2017-04-10T12:27:07\" End=\"2017-04-10T13:30:17\" Elapsed=\"01:03:10\" NNodes=\"1\" NCPUS=\"16\" Timelimit=\"1-00:00:00\" ReqMem=\"110Gn\" ExitCode=\"0:0\" State=\"COMPLETED\" resources_used_mem=401984K",
						"JobID=\"58106_57\" User=\"wmfw23\" Group=\"dp004\" Account=\"dp004\" JobName=\"EglSrvy_chunk\" Partition=\"cosma6\" Submit=\"2017-04-10T12:22:20\" Reserved=\"00:04:47\" Start=\"2017-04-10T12:27:07\" End=\"2017-04-10T13:22:26\" Elapsed=\"00:55:19\" NNodes=\"1\" NCPUS=\"16\" Timelimit=\"1-00:00:00\" ReqMem=\"110Gn\" ExitCode=\"0:0\" State=\"COMPLETED\" resources_used_mem=1706332K",
						"JobID=\"58106_58\" User=\"wmfw23\" Group=\"dp004\" Account=\"dp004\" JobName=\"EglSrvy_chunk\" Partition=\"cosma6\" Submit=\"2017-04-10T12:22:20\" Reserved=\"00:04:47\" Start=\"2017-04-10T12:27:07\" End=\"2017-04-10T14:07:06\" Elapsed=\"01:39:59\" NNodes=\"1\" NCPUS=\"16\" Timelimit=\"1-00:00:00\" ReqMem=\"110Gn\" ExitCode=\"0:0\" State=\"COMPLETED\" resources_used_mem=787212K",
						"JobID=\"58106_59\" User=\"wmfw23\" Group=\"dp004\" Account=\"dp004\" JobName=\"EglSrvy_chunk\" Partition=\"cosma6\" Submit=\"2017-04-10T12:22:20\" Reserved=\"00:04:47\" Start=\"2017-04-10T12:27:07\" End=\"2017-04-10T13:29:39\" Elapsed=\"01:02:32\" NNodes=\"1\" NCPUS=\"16\" Timelimit=\"1-00:00:00\" ReqMem=\"110Gn\" ExitCode=\"0:0\" State=\"COMPLETED\" resources_used_mem=610300K",

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