package uk.ac.ed.epcc.safe.accounting.parsers;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
import uk.ac.ed.epcc.webapp.junit4.DataBaseFixtures;


@DataBaseFixtures({"AprunApplication.xml"})
public class NewAlpsParserTest extends AbstractRecordTestCase {

	public NewAlpsParserTest() {
		super("ARCHER", "ALPSLog");
	}


	@Override
	public PropertyMap getDefaults() {
		PropertyMap defaults = new PropertyMap();
       
		return defaults;
		
	}

	private static final String testDataPath = "./NewAlps.txt";
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
	public Collection<BadRecordText> getBadRecords() {
		
		return badTexts;
	}



	@Override
	public Collection<RecordText> getGoodRecords() {
		
		return goodRecords;
	}

	
	

	@Override
	public String getUpdateText()   {
		try {
			return getResourceAsString("NewAlps.txt");
		} catch (IOException e) {
			throw new ConsistencyError("update text", e);
		}
	}
	@Override
	public String getReceiveAccountingExpected() {
		return "new_alps_parse.xml";
	}

}
