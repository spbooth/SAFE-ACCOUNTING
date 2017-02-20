// Copyright - The University of Edinburgh 2015
package uk.ac.ed.epcc.safe.accounting.parsers;

import java.util.ArrayList;
import java.util.Collection;

import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.SkipRecord;

public class OxfordSLURMRecordTest extends AbstractRecordTestCase {
	private static final Collection<RecordText> goodRecords = new ArrayList<RecordText>();
	private static final Collection<BadRecordText> badTexts = new ArrayList<BadRecordText>();

	public OxfordSLURMRecordTest() {
		super("SLURMMachine", "OxfordSLURMRecord");
	}

	/**
	 * Tests are not going to be altered at runtime so we declare them all in a
	 * static block and add them to either the good records or bad records
	 * collection
	 */
	static {
		String validRecords[] = new String[] {
				
				"JobID|JobIDRaw|User|Group|Account|JobName|Partition|QOS|Start|End|Submit|AllocCPUS|AllocGRES|NNodes|Elapsed|Timelimit|State|\n"+
				"605547|605547|dops0572|dops-evospecs|dops-evospecs|prot|compute|normal|2017-01-04T16:57:11|2017-01-08T01:21:15|2017-01-04T16:57:11|16||1|3-08:24:04|4-04:00:00|COMPLETED|\n"+
				"605603|605603|lilizhang|engs-jerugroup|engs-jerugroup|POPC_hole_5-2|compute|priority|2017-01-04T20:03:47|2017-01-08T20:12:26|2017-01-04T20:03:47|128||8|4-00:08:39|5-00:00:00|COMPLETED|\n"+
				"605605|605605|lilizhang|engs-jerugroup|engs-jerugroup|POPC_hole_10-2|compute|priority|2017-01-04T20:40:23|2017-01-08T21:07:51|2017-01-04T20:40:22|128||8|4-00:27:28|5-00:00:00|COMPLETED|\n"+
				"605606|605606|lilizhang|engs-jerugroup|engs-jerugroup|POPC_hole_15-2|compute|priority|2017-01-04T20:54:07|2017-01-08T21:07:17|2017-01-04T20:54:07|128||8|4-00:13:10|5-00:00:00|COMPLETED|\n"+
				"605610|605610|lilizhang|engs-jerugroup|engs-jerugroup|POPC_hole_20-2|compute|priority|2017-01-04T21:49:40|2017-01-08T21:41:52|2017-01-04T21:49:40|128||8|3-23:52:12|5-00:00:00|COMPLETED|\n"+
				"605612|605612|lilizhang|engs-jerugroup|engs-jerugroup|POPC_hole_30-2|compute|priority|2017-01-04T22:00:43|2017-01-08T23:21:00|2017-01-04T22:00:43|128||8|4-01:20:17|5-00:00:00|COMPLETED|\n"+
				"605615|605615|lilizhang|engs-jerugroup|engs-jerugroup|POPC_hole_40-2|compute|priority|2017-01-04T22:12:42|2017-01-08T23:18:02|2017-01-04T22:12:41|128||8|4-01:05:20|5-00:00:00|COMPLETED|\n"+
				"605616|605616|lilizhang|engs-jerugroup|engs-jerugroup|POPC_hole_50-2|compute|priority|2017-01-04T22:27:08|2017-01-09T00:44:51|2017-01-04T22:27:07|128||8|4-02:17:43|5-00:00:00|COMPLETED|\n"+
				"605711|605711|sjoh4140|marine-cfd|marine-cfd|AL_om0pt8_F2|compute|priority|2017-01-05T12:14:11|2017-01-09T05:58:41|2017-01-05T11:37:31|32||2|3-17:44:30|5-00:00:00|COMPLETED|\n"+
				"605711.1|605711.1|||marine-cfd|orted|||2017-01-05T12:16:32|2017-01-09T05:52:23|2017-01-05T12:16:32|1||1|3-17:35:51||COMPLETED|\n"+
				"605712|605712|sjoh4140|marine-cfd|marine-cfd|AL_om0pt9_F2|compute|priority|2017-01-05T12:15:38|2017-01-09T06:41:34|2017-01-05T11:37:42|32||2|3-18:25:56|5-00:00:00|COMPLETED|\n"+
				"605712.1|605712.1|||marine-cfd|orted|||2017-01-05T12:17:51|2017-01-09T06:34:57|2017-01-05T12:17:51|1||1|3-18:17:06||COMPLETED|\n"+
				"605713|605713|sjoh4140|marine-cfd|marine-cfd|AL_om1_F2|compute|priority|2017-01-05T12:19:38|2017-01-09T01:27:36|2017-01-05T11:37:53|32||2|3-13:07:58|5-00:00:00|COMPLETED|\n"+
				"605713.1|605713.1|||marine-cfd|orted|||2017-01-05T12:21:53|2017-01-09T01:21:51|2017-01-05T12:21:53|1||1|3-12:59:58||COMPLETED|\n"+
				"605714|605714|sjoh4140|marine-cfd|marine-cfd|AL_om1pt1_F2|compute|priority|2017-01-05T12:59:38|2017-01-09T02:02:24|2017-01-05T11:38:02|32||2|3-13:02:46|5-00:00:00|COMPLETED|\n"+
				"605714.1|605714.1|||marine-cfd|orted|||2017-01-05T13:02:06|2017-01-09T01:57:00|2017-01-05T13:02:06|1||1|3-12:54:54||COMPLETED|\n"+
				"605717|605717|whgu0505|opic-3dem|opic-3dem|c6_subunit1job|gpu|normal|2017-01-05T12:02:55|2017-01-09T03:56:57|2017-01-05T12:02:55|1|gpu:2|1|3-15:54:02|4-03:50:00|COMPLETED|\n"+
				"605736|605736|sbenjamin|oums-quantopo|oums-quantopo|SCBqsim|compute|priority|2017-01-05T13:54:59|2017-01-08T03:42:30|2017-01-05T13:54:58|16||1|2-13:47:31|4-04:00:00|COMPLETED|\n"+
				"605840|605840|spet4261|chem-cosun|chem-cosun|MJ098.gjf|compute|normal|2017-01-05T20:32:19|2017-01-08T20:32:31|2017-01-05T18:51:02|16||1|3-00:00:12|3-00:00:00|TIMEOUT|\n"+
				"605854|605854|sjoh4400|coml-compbiosimulations|coml-compbiosimulations|matlab_test|compute|normal|2017-01-05T23:03:08|2017-01-08T01:32:23|2017-01-05T23:03:08|16||1|2-02:29:15|5-00:00:00|COMPLETED|\n"+
				"605855|605855|sjoh4400|coml-compbiosimulations|coml-compbiosimulations|matlab_test|compute|normal|2017-01-05T23:04:22|2017-01-08T08:36:56|2017-01-05T23:04:22|16||1|2-09:32:34|5-00:00:00|COMPLETED|\n"+
				"605857|605857|sjoh4400|coml-compbiosimulations|coml-compbiosimulations|matlab_test|compute|normal|2017-01-05T23:07:04|2017-01-08T03:21:35|2017-01-05T23:07:04|16||1|2-04:14:31|5-00:00:00|COMPLETED|\n"+
				"605861|605861|sjoh4400|coml-compbiosimulations|coml-compbiosimulations|matlab_test|compute|normal|2017-01-05T23:10:53|2017-01-08T08:16:58|2017-01-05T23:10:52|16||1|2-09:06:05|5-00:00:00|COMPLETED|\n"+
				"609108|609108|engs1687|eng-smg|eng-smg|IKM10401|compute|priority|2017-01-06T16:43:49|2017-01-08T22:07:43|2017-01-06T16:43:49|16||1|2-05:23:54|2-12:30:00|COMPLETED|\n"+
				"609182|609182|mert3216|cluster_thermo|cluster_thermo|1e-5R2|gpu|normal|2017-01-06T22:25:29|2017-01-08T02:49:52|2017-01-06T22:25:28|1|gpu:2|1|1-04:24:23|5-00:00:00|COMPLETED|\n"+
				"609197|609197|mengel|phys-oxdna|phys-oxdna|HJrelax|gpu|normal|2017-01-07T02:07:26|2017-01-09T04:07:32|2017-01-07T02:07:26|1|gpu:1|1|2-02:00:06|2-02:00:00|TIMEOUT|\n"+
				"609224|609224|chem1198|chem-cosun|chem-cosun|SwitchModelFluorene2PCMDCMRotTSQST2.com|compute|normal|2017-01-07T12:52:00|2017-01-08T05:46:42|2017-01-07T12:51:59|16||1|16:54:42|2-00:00:00|COMPLETED|\n"+
				"609227|609227|chem1198|chem-cosun|chem-cosun|CoDiyneDimerOutE2.com|compute|normal|2017-01-07T13:05:13|2017-01-08T13:28:12|2017-01-07T13:05:13|16||1|1-00:22:59|2-00:00:00|FAILED|\n"+
				"609237|609237|chem1198|chem-cosun|chem-cosun|CoTriyneDimer2.com|compute|normal|2017-01-07T14:02:44|2017-01-08T17:54:36|2017-01-07T14:02:44|16||1|1-03:51:52|2-00:00:00|FAILED|\n"+
				"609238|609238|chem1198|chem-cosun|chem-cosun|CoTriyneDimerE2.com|compute|normal|2017-01-07T14:05:40|2017-01-08T04:57:09|2017-01-07T14:05:39|16||1|14:51:29|2-00:00:00|FAILED|\n"+
				"609242|609242|chem1198|chem-cosun|chem-cosun|CoTriyneDimerInE2.com|compute|normal|2017-01-07T14:09:39|2017-01-08T03:35:44|2017-01-07T14:09:38|16||1|13:26:05|2-00:00:00|FAILED|\n"+
				"610274|610274|sbenjamin|oums-quantopo|oums-quantopo|SCBqsim|compute|priority|2017-01-07T16:23:59|2017-01-08T01:17:26|2017-01-07T16:23:58|16||1|08:53:27|4-04:00:00|COMPLETED|\n"+
				"610275|610275|sbenjamin|oums-quantopo|oums-quantopo|SCBqsim|compute|priority|2017-01-07T16:24:06|2017-01-08T01:14:56|2017-01-07T16:24:06|16||1|08:50:50|4-04:00:00|COMPLETED|\n"+
				"610276|610276|sbenjamin|oums-quantopo|oums-quantopo|SCBqsim|compute|priority|2017-01-07T16:25:13|2017-01-08T01:16:04|2017-01-07T16:25:12|16||1|08:50:51|4-04:00:00|COMPLETED|\n"+
				"610289|610289|zool2068|zool-avian-social-ecology|zool-avian-social-ecology|juliaModel|compute|normal|2017-01-07T16:40:34|2017-01-08T01:30:27|2017-01-07T16:37:30|16||1|08:49:53|4-03:00:00|COMPLETED|\n"+
				"610291|610291|zool2068|zool-avian-social-ecology|zool-avian-social-ecology|juliaModel|compute|normal|2017-01-07T16:40:34|2017-01-08T08:11:57|2017-01-07T16:37:33|16||1|15:31:23|4-03:00:00|COMPLETED|\n"+
				"610292|610292|zool2068|zool-avian-social-ecology|zool-avian-social-ecology|juliaModel|compute|normal|2017-01-07T21:10:49|2017-01-08T06:24:33|2017-01-07T16:37:35|16||1|09:13:44|4-03:00:00|COMPLETED|\n"+
				"610293|610293|zool2068|zool-avian-social-ecology|zool-avian-social-ecology|juliaModel|compute|normal|2017-01-07T21:10:49|2017-01-08T14:59:19|2017-01-07T16:37:38|16||1|17:48:30|4-03:00:00|COMPLETED|\n"+
				"610294|610294|zool2068|zool-avian-social-ecology|zool-avian-social-ecology|juliaModel|compute|normal|2017-01-07T21:10:49|2017-01-08T07:57:54|2017-01-07T16:37:40|16||1|10:47:05|4-03:00:00|COMPLETED|\n"+
				"610295|610295|zool2068|zool-avian-social-ecology|zool-avian-social-ecology|juliaModel|compute|normal|2017-01-07T21:10:49|2017-01-08T15:25:18|2017-01-07T16:37:42|16||1|18:14:29|4-03:00:00|COMPLETED|\n"+
				"610296|610296|zool2068|zool-avian-social-ecology|zool-avian-social-ecology|juliaModel|compute|normal|2017-01-07T21:10:49|2017-01-08T07:00:38|2017-01-07T16:37:44|16||1|09:49:49|4-03:00:00|COMPLETED|\n"+
				"610297|610297|zool2068|zool-avian-social-ecology|zool-avian-social-ecology|juliaModel|compute|normal|2017-01-07T21:10:49|2017-01-08T16:25:33|2017-01-07T16:37:46|16||1|19:14:44|4-03:00:00|COMPLETED|\n"+
				"610298|610298|zool2068|zool-avian-social-ecology|zool-avian-social-ecology|juliaModel|compute|normal|2017-01-07T21:10:49|2017-01-08T07:52:55|2017-01-07T16:37:47|16||1|10:42:06|4-03:00:00|COMPLETED|\n"+
				"610299|610299|zool2068|zool-avian-social-ecology|zool-avian-social-ecology|juliaModel|compute|normal|2017-01-07T21:10:49|2017-01-08T16:41:25|2017-01-07T16:37:49|16||1|19:30:36|4-03:00:00|COMPLETED|\n"+
				"610300|610300|zool2068|zool-avian-social-ecology|zool-avian-social-ecology|juliaModel|compute|normal|2017-01-07T21:11:38|2017-01-08T07:49:34|2017-01-07T16:37:51|16||1|10:37:56|4-03:00:00|COMPLETED|\n"+
				"610301|610301|zool2068|zool-avian-social-ecology|zool-avian-social-ecology|juliaModel|compute|normal|2017-01-07T21:11:49|2017-01-08T16:56:55|2017-01-07T16:37:53|16||1|19:45:06|4-03:00:00|COMPLETED|\n"+
				"610302|610302|zool2068|zool-avian-social-ecology|zool-avian-social-ecology|juliaModel|compute|normal|2017-01-07T21:12:49|2017-01-08T07:30:45|2017-01-07T16:37:56|16||1|10:17:56|4-03:00:00|COMPLETED|\n"+
				"610303|610303|zool2068|zool-avian-social-ecology|zool-avian-social-ecology|juliaModel|compute|normal|2017-01-07T21:12:49|2017-01-08T17:55:58|2017-01-07T16:37:59|16||1|20:43:09|4-03:00:00|COMPLETED|\n"+
				"610304|610304|zool2068|zool-avian-social-ecology|zool-avian-social-ecology|juliaModel|compute|normal|2017-01-07T21:13:38|2017-01-08T08:02:30|2017-01-07T16:38:02|16||1|10:48:52|4-03:00:00|COMPLETED|\n"+
				"610305|610305|zool2068|zool-avian-social-ecology|zool-avian-social-ecology|juliaModel|compute|normal|2017-01-07T21:13:38|2017-01-08T17:06:09|2017-01-07T16:38:04|16||1|19:52:31|4-03:00:00|COMPLETED|\n"+
				"610306|610306|zool2068|zool-avian-social-ecology|zool-avian-social-ecology|juliaModel|compute|normal|2017-01-07T21:13:38|2017-01-08T07:50:37|2017-01-07T16:38:08|16||1|10:36:59|4-03:00:00|COMPLETED|\n"+
				"611317|611317|sedm5504|marine-cfd|marine-cfd|17Jan|compute|priority|2017-01-07T21:09:38|2017-01-08T13:21:22|2017-01-07T19:40:37|16||1|16:11:44|3-08:00:00|COMPLETED|\n"+
				"611329|611329|shil3352|chem-perkin-c-sh|chem-perkin-c-sh|v2_4|compute|normal|2017-01-07T21:13:38|2017-01-08T08:21:06|2017-01-07T21:00:06|64||4|11:07:28|15:30:00|COMPLETED|\n"+
				"611330|611330|shil3352|chem-perkin-c-sh|chem-perkin-c-sh|v1.75_4|compute|normal|2017-01-07T21:16:28|2017-01-08T07:41:51|2017-01-07T21:16:27|64||4|10:25:23|15:30:00|COMPLETED|\n"+
				"611406|611406|shil3352|chem-perkin-c-sh|chem-perkin-c-sh|v_1.5_4|compute|normal|2017-01-07T23:03:35|2017-01-08T09:45:04|2017-01-07T23:03:34|64||4|10:41:29|15:30:00|FAILED|\n"+
				"611409|611409|sedm5504|marine-cfd|marine-cfd|17Jan|compute|priority|2017-01-07T23:14:13|2017-01-08T01:04:40|2017-01-07T23:14:12|16||1|01:50:27|3-08:00:00|COMPLETED|\n"+
				"611415|611415|coml0625|coml-sngroup|coml-sngroup|iebng1|gpu|normal|2017-01-07T23:21:16|2017-01-08T11:32:59|2017-01-07T23:21:15|1|gpu:1|1|12:11:43|5-00:00:00|CANCELLED by 4594|\n"
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