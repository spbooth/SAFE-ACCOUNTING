package uk.ac.ed.epcc.safe.accounting.parsers;

import java.util.ArrayList;
import java.util.Collection;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;

public class AprunParserTest extends AbstractRecordTestCase {

	public AprunParserTest() {
		super("ARCHER", "AprunCommandLog");
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
		String good[] = new String[] {
		"/opt/cray/alps/5.2.3-2.0502.9295.14.14.ari/bin/aprun -n 1024 ./imagenew 1024 1",
		"/opt/cray/alps/5.2.3-2.0502.9295.14.14.ari/bin/aprun -n 1000 ./imagenew 1024 1",
		"/opt/cray/alps/5.2.3-2.0502.9295.14.14.ari/bin/aprun -n 1 /work/e280/e280/sv375/Solvaware_v1.3/Solvaware_Trunk/HydrationSiteAnalysis/Utilities/WriteDensityProfile /fs2/e280/e280/sv375/carbonic_anhydrase_2/2NNG/Equilibration/2NNG_system.pdb /fs2/e280/e280/sv375/carbonic_anhydrase_2/2NNG/Equilibration/2NNG_system.psf /fs2/e280/e280/sv375/carbonic_anhydrase_2/2NNG/Dynamics/2NNG_system_dynamics.dcd 0 9000 1 1.2 59 59 59 108 480",
		"/opt/cray/alps/5.2.3-2.0502.9295.14.14.ari/bin/aprun -N 1 -n 1 -S 1 /work/n02/n02/pappas/src/oasis/crayxe6_cce-20160403/bin/oasis3.MPI1.x : -N 24 -d 1 -n 144 -S 12 -ss /work/n02/n02/pappas/um/xgspt/bin/ga30_kpp.exe-20160403 : -N 1 -n 1 -d 24 /work/n02/n02/bdong/um/xmnub/dataw/KPP_ocean",
		"/opt/cray/alps/5.2.3-2.0502.9295.14.14.ari/bin/aprun -ss -n 1920 -N 24 -d 1 -S 12 -j 1 -e OMP_NUM_THREADS=1 /work/n02/n02/wmcginty/um/xmrfd/bin/xmrfd.exe : -ss -n 24 -N 12 -d 2 -S 6 -j 1 -e OMP_NUM_THREADS=2 /work/n02/n02/wmcginty/um/xmrfd/bin/xmrfd.exe"
		};
		
		for( String s : good){
			goodRecords.add(new RecordText(s));
		}
		
		//TODO: resolve PlotEntry exceptions seen in console output
		
		//TODO: setup negative test cases
		String bad[] = new String[] {
		"/opt/cray/alps/5.2.3-2.0502.9295.14.14.ari/bin/aprun --ss -n 1920 -N 24 -d 1 -S 12 -j 1 -e OMP_NUM_THREADS=1 /work/n02/n02/wmcginty/um/xmrfd/bin/xmrfd.exe",
		"/opt/cray/alps/5.2.3-2.0502.9295.14.14.ari/bin/aprun -ss -n 24 -N 12 -d 2 -S 6 -j1 -e OMP_NUM_THREADS=2 /work/n02/n02/wmcginty/um/xmrfd/bin/xmrfd.exe",
		"/opt/cray/alps/5.2.3-2.0502.9295.14.14.ari/bin/aprun -ss -n 24 -N 12 -d 2 -S 6 -j1 env OMP_NUM_THREADS=2 /work/n02/n02/wmcginty/um/xmrfd/bin/xmrfd.exe"
		};
		
		/*
		for( String s : bad){
			Class<? extends Throwable> x = AccountingParseException;
			badTexts.add(new BadRecordText(s, new Exception()));
		}
		*/
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
