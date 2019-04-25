// Copyright - The University of Edinburgh 2015
/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers;

import org.junit.Before;

import uk.ac.ed.epcc.safe.accounting.db.ConfigUsageRecordFactory;
import uk.ac.ed.epcc.safe.accounting.db.ParseUsageRecordFactoryTestCase;
import uk.ac.ed.epcc.safe.accounting.db.UsageRecordFactory;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;

public class SGERecordTest extends ParseUsageRecordFactoryTestCase{

	@Before
	public void loadData() throws Exception{
		load("Eddie.xml");
	}
	

	@Override
	public PropertyMap getDefaults() {
		PropertyMap defaults = new PropertyMap();
        defaults.setProperty(StandardProperties.MACHINE_NAME_PROP, "Eddie");
		return defaults;
	}

	@Override
	public UsageRecordFactory getFactory() {
		return new ConfigUsageRecordFactory(ctx,"SGERecord");
	}


	@Override
	public String getUpdateText() {
		return "ecdf:node025.beowulf.cluster:physics_ifp_ppe:s0571079:afs_toy_so:1155664:sge:5:1222847031:1222848422:1222851714:0:0:3292:3287:0:0.000000:0:0:0:0:72259:0:0:0.000000:0:0:0:0:13885:21950:ecdf_baseline:defaultdepartment:NONE:1:0:3292.000000:8037.109375:0.000000:-u s0571079 -l h_rt=14400,h_vmem=2500M -P ecdf_baseline:0.000000:NONE:384315392.000000\n"+
"ecdf:node052.beowulf.cluster:dteam:dteam020:STDIN:1156411:sge:5:1222851611:1222851645:1222851720:0:0:75:1:0:0.000000:0:0:0:0:130665:0:0:0.000000:0:0:0:0:4183:1473:dteam:defaultdepartment:NONE:1:0:75.000000:219.726562:0.000000:-u dteam020 -q ecdf -l h_fsize=5G,h_rt=172799,h_vmem=3000M -P dteam:0.000000:NONE:583712768.000000\n"+
"ecdf:node010. beowulf.cluster:physics_ifp_ppe:s0571079:afs_toy_so:1155565:sge:5:1222846926:1222848246:1222851720:0:0:3474:3456:2:0.000000:0:0:0:0:66653:0:0:0.000000:0:0:0:0:12463:174154:ecdf_baseline:defaultdepartment:NONE:1:0:3474.000000:8481.445312:0.000000:-u s0571079 -l h_rt=14400,h_vmem=2500M -P ecdf_baseline:0.000000:NONE:360767488.000000\n"+
"ecdf:node015.beowulf.cluster:inf_iccs_smt:s0565741:mert15-ab:1156260:sge:5:1222851209:1222851254:1222851728:0:0:474:422:19:0.000000:0:0:0:0:7213832:0:0:0.000000:0:0:0:0:1876:20641:ecdf_baseline:defaultdepartment:NONE:1:0:474.000000:948.462891:0.000000:-u s0565741 -l mem_free=0.5G -P ecdf_baseline:0.000000:NONE:998363136.000000\n"+
"ecdf:node131.beowulf.cluster:inf_iccs_smt:s0565741:mert15-bb:1156286:sge:5:1222851210:1222851255:1222851735:0:0:480:421:25:0.000000:0:0:0:0:7604551:0:0:0.000000:0:0:0:0:3164:41464:ecdf_baseline:defaultdepartment:NONE:1:0:480.000000:960.468750:0.000000:-u s0565741 -l mem_free=0.5G -P ecdf_baseline:0.000000:NONE:940011520.000000\n"+
"ecdf:node130.beowulf.cluster:inf_iccs_smt:s0565741:mert15-av:1156280:sge:5:1222851210:1222851255:1222851746:0:0:491:436:19:0.000000:0:0:0:0:7264022:0:0:0.000000:0:0:0:0:2100:6358:ecdf_baseline:defaultdepartment:NONE:1:0:491.000000:982.479492:0.000000:-u s0565741 -l mem_free=0.5G -P ecdf_baseline:0.000000:NONE:979292160.000000\n"+
"ecdf:node158.beowulf.cluster:informatics:bhaddow:MOSES-as:1145341:sge:5:1222780168:1222843899:1222851753:0:0:7854:7764:26:0.000000:0:0:0:0:9375769:0:0:0.000000:0:0:0:0:2770:5957:ecdf_baseline:defaultdepartment:memory:4:0:7854.000000:15715.669922:0.000000:-u bhaddow -l h_rt=86400 -pe memory 4 -P ecdf_baseline:0.000000:NONE:3591389184.000000\n"+
"ecdf:node025.beowulf.cluster:inf_iccs_smt:s0565741:mert15-ai:1156267:sge:5:1222851210:1222851254:1222851760:0:0:506:447:19:0.000000:0:0:0:0:6952667:0:0:0.000000:0:0:0:0:1850:53684:ecdf_baseline:defaultdepartment:NONE:1:0:506.000000:1012.494141:0.000000:-u s0565741 -l mem_free=0.5G -P ecdf_baseline:0.000000:NONE:935964672.000000\n"+
"ecdf:node033.beowulf.cluster:inf_iccs_smt:s0565741:mert15-aq:1156275:sge:5:1222851210:1222851254:1222851764:0:0:510:459:18:0.000000:0:0:0:0:6933874:0:0:0.000000:0:0:0:0:1983:25117:ecdf_baseline:defaultdepartment:NONE:1:0:510.000000:1020.498047:0.000000:-u s0565741 -l mem_free=0.5G -P ecdf_baseline:0.000000:NONE:929492992.000000\n"+
"ecdf:node120.beowulf.cluster:informatics:bhaddow:MOSES-ad:1145282:sge:5:1222780106:1222842962:1222851766:0:0:8804:8717:36:0.000000:0:0:0:0:14199083:0:0:0.000000:0:0:0:0:2169:20311:ecdf_baseline:defaultdepartment:memory:4:0:8804.000000:17616.597656:0.000000:-u bhaddow -l h_rt=86400 -pe memory 4 -P ecdf_baseline:0.000000:NONE:3520663552.000000\n"+
"parallel.q:comp036.nw-grid.ac.uk:MACE:mcsxr:SFR19_10k:2211:sge:0:1258821869:1258821871:1258971533:100:137:149662:2.452627:3.719434:0.000000:0:0:0:0:183247:0:0:0.000000:0:0:0:0:3570:983:NONE:defaultdepartment:openmpi:16:0:9555112.430000:2241396.219366:0.000000:-pe openmpi 16:0.000000:NONE:22628118528.000000:0:0\n" + 

"all.q:eddie244.ecdf.ed.ac.uk:ecdf_isiti:ywan:rotating.sh:40:sge:0:1273571650:1273571650:1273571651:0:0:1:0.026995:0.047992:0.000000:0:0:0:0:5919:20:0:0.000000:0:0:0:0:311:61:NONE:defaultdepartment:openmpi:8:0:0.074987:0.00000:0.000179:-pe openmpi 8:0.000000:1.eddie244:426389504.000000:0:0\n"+
"all.q:eddie243.ecdf.ed.ac.uk:ecdf_isiti:ywan:rotating.sh:40:sge:0:1273571643:1273571650:1273571651:0:0:1:0.047992:0.059990:0.000000:0:0:0:0:7894:19:0:0.000000:0:0:0:0:207:62:NONE:defaultdepartment:openmpi:8:0:0.107982:0.000000:0.001000:-pe openmpi 8:0.000000:NONE:518365184.000000:0:0\n"+
"all.q:eddie243.ecdf.ed.ac.uk:ecdf_isiti:ywan:rotating.sh:39:sge:0:1273571527:1273571530:1273571531:0:0:1:0.102983:0.129979:0.000000:0:0:0:0:13820:42:0:0.000000:0:0:0:0:843:135:NONE:defaultdepartment:openmpi:8:0:0.232962:0.000000:0.001179:-pe openmpi 8:0.000000:NONE:944750592.000000:0:0\n"+
"";		
	
	}

}