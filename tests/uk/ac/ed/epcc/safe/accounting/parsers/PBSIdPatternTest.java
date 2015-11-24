package uk.ac.ed.epcc.safe.accounting.parsers;

import static org.junit.Assert.*;

import java.util.regex.Matcher;

import org.junit.Test;

public class PBSIdPatternTest {

	@Test
	public void testArrayPattern(){
		Matcher m = AbstractPbsParser.PBSIdStringEntryMaker.array_patt.matcher("12345[16].sdb");
		
		assertTrue(m.matches());
		assertEquals("12345", m.group(1));
		assertEquals("16", m.group(2));
	}
	
	@Test
	public void testNormalPattern(){
		Matcher m = AbstractPbsParser.PBSIdStringEntryMaker.array_patt.matcher("12345.sdb");
		
		assertFalse(m.matches());
		
	}
}
