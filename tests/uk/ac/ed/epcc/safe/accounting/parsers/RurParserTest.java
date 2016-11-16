package uk.ac.ed.epcc.safe.accounting.parsers;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

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

	private static final String testDataPath = "./RURData.txt";
	private static final Collection<RecordText> goodRecords = new ArrayList<RecordText>();
	private static final Collection<BadRecordText> badTexts = new ArrayList<BadRecordText>();
	
	@Test
	public void testParse() { 
		
		ArrayList<String> records = new ArrayList<String>();
		
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
	public Collection<BadRecordText> getBadRecords() {
		
		return badTexts;
	}



	@Override
	public Collection<RecordText> getGoodRecords() {
		
		return goodRecords;
	}

	
}
