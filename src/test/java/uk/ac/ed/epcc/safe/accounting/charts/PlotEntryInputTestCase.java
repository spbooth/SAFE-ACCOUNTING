package uk.ac.ed.epcc.safe.accounting.charts;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.AccountingService;
import uk.ac.ed.epcc.webapp.WebappTestBase;
import uk.ac.ed.epcc.webapp.forms.ListInputInterfaceTest;
import uk.ac.ed.epcc.webapp.forms.ListInputInterfaceTestImpl;
import uk.ac.ed.epcc.webapp.forms.TestDataProvider;

public class PlotEntryInputTestCase extends WebappTestBase implements TestDataProvider<String, PlotEntryInput> ,
ListInputInterfaceTest<String, PlotEntry, PlotEntryInput, TestDataProvider<String,PlotEntryInput>>
{

	public ListInputInterfaceTest<String, PlotEntry, PlotEntryInput, TestDataProvider<String,PlotEntryInput>> list_test = new ListInputInterfaceTestImpl<>(this);
	public PlotEntryInputTestCase() {
		
	}
	@Override
	@Test
	public void testGetItembyValue() throws Exception {
		list_test.testGetItembyValue();
		
	}
	@Override
	@Test
	public void testGetItems() throws Exception {
		list_test.testGetItems();
		
	}
	@Override
	@Test
	public void testGetTagByItem() throws Exception {
		list_test.testGetTagByItem();
		
	}
	@Override
	@Test
	public void testGetTagByValue() throws Exception {
		list_test.testGetTagByValue();
		
	}
	@Override
	@Test
	public void testGetText() throws Exception {
		list_test.testGetText();
		
	}
	@Override
	@Test
	public void testIsValid() throws Exception {
		list_test.testIsValid();
		
	}
	@Override
	public Set<String> getGoodData() throws Exception {
		Set<String> good = new HashSet<>();
		good.add("Time");
		good.add("WaitTime");
		good.add("Count");
		good.add("Wall");
		return good;
	}
	@Override
	public Set<String> getBadData() throws Exception {
		Set<String> bad = new HashSet<>();
		bad.add("boris");
		bad.add("nigel");
		return bad;
	}
	@Override
	public PlotEntryInput getInput() throws Exception {
		AccountingService serv = getContext().getService(AccountingService.class);
		return new PlotEntryInput(getContext(), serv.getUsageProducer(), null);
	}

}
