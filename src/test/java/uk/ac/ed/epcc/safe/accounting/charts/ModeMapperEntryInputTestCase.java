package uk.ac.ed.epcc.safe.accounting.charts;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.AccountingService;
import uk.ac.ed.epcc.webapp.WebappTestBase;
import uk.ac.ed.epcc.webapp.forms.ListInputInterfaceTest;
import uk.ac.ed.epcc.webapp.forms.ListInputInterfaceTestImpl;
import uk.ac.ed.epcc.webapp.forms.TestDataProvider;

public class ModeMapperEntryInputTestCase extends WebappTestBase implements TestDataProvider<String, MapperEntryInput> ,
ListInputInterfaceTest<String, MapperEntry, MapperEntryInput, TestDataProvider<String,MapperEntryInput>>
{

	public ListInputInterfaceTest<String, MapperEntry, MapperEntryInput, TestDataProvider<String,MapperEntryInput>> list_test = new ListInputInterfaceTestImpl<>(this);
	public ModeMapperEntryInputTestCase() {
		
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
		good.add("pig.Queue");
		good.add("pig.CPUs");
		good.add("pig.UserName");
		good.add("pig.MachineName");
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
	public MapperEntryInput getInput() throws Exception {
		AccountingService serv = getContext().getService(AccountingService.class);
		return new MapperEntryInput(getContext(), serv.getUsageProducer(), "pig");
	}

}
