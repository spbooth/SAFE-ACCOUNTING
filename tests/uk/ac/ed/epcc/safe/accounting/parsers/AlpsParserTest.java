package uk.ac.ed.epcc.safe.accounting.parsers;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.webapp.junit4.DataBaseFixtures;


@DataBaseFixtures({"AprunApplication.xml"})
public class AlpsParserTest extends AbstractRecordTestCase {

	public AlpsParserTest() {
		super("ARCHER", "ALPSLog");
	}


	@Override
	public PropertyMap getDefaults() {
		PropertyMap defaults = new PropertyMap();
       
		return defaults;
		
	}

	private static final String testDataPath = "./ALPSData.txt";
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

	@Test
	public void testDateParser() throws ParseException{
		Date orig = AlpsLogParser.parseDate(true,"2016-06-02T12:04:51.255478+01:00");
		Date mod = AlpsLogParser.parseDate(true,"2016-06-02T12:04:51.255478+00:00");
		long orig_time = orig.getTime();
		long mod_time = mod.getTime();
		assertNotEquals(orig.getTime() , mod.getTime());
	}
	
	@Test
	public void testDateParserNoTZ() throws ParseException{
		Date orig = AlpsLogParser.parseDate(false,"2016-06-02T12:04:51.255478+01:00");
		Date mod = AlpsLogParser.parseDate(false,"2016-06-02T12:04:51.255478+00:00");
		long orig_time = orig.getTime();
		long mod_time = mod.getTime();
		assertEquals(orig.getTime() , mod.getTime());
	}
}
