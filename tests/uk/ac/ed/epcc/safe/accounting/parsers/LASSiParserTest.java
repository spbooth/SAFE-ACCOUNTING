package uk.ac.ed.epcc.safe.accounting.parsers;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
import uk.ac.ed.epcc.webapp.junit4.DataBaseFixtures;


@DataBaseFixtures({"RurParserTest.xml"})
public class LASSiParserTest extends AbstractRecordTestCase {

	


	public LASSiParserTest() {
		super("ARCHER","LASSiLog");
	}


	@Override
	public PropertyMap getDefaults() {
		PropertyMap defaults = new PropertyMap();
       
		return defaults;
		
	}

	private static final String testDataPath = "./LASSiData.txt";
	private static final Collection<RecordText> goodRecords = new ArrayList<>();
	private static final Collection<BadRecordText> badTexts = new ArrayList<>();
	
	
	@Test
	public void testParse() { 
		
		ArrayList<String> records = new ArrayList<>();
		
		try {
			DataInputStream in = new DataInputStream( getClass().getResourceAsStream(testDataPath) );
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
				
			String strLine;
			while ((strLine = br.readLine()) != null)   {
				records.add(strLine);
			}
			
			in.close();
		} catch (Exception e){
		    System.err.println("Error: " + e.getMessage());
		}
		
		
		for( String s : records){
			goodRecords.add(new RecordText(s));
		}
		
	}
	@Override
	public String getUpdateText() {
		
		try {
			return getResourceAsString(testDataPath);
		} catch (IOException e) {
			throw new ConsistencyError("Error getting data",e);
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

	@Override
	public String getReceiveAccountingExpected() {
		return "lassi_parse.xml";
	}
}
