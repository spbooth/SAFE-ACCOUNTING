/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.junit.Before;

import uk.ac.ed.epcc.webapp.TestDataHelper;

/**
 * Adds OGF usage records to test. Some are good records that the parser should
 * be able to parse successfully. This test makes use of the entire accounting
 * framework as well as the parser.
 * 
 * @author jgreen4
 * 
 */
public class XMLRecordTest extends AbstractRecordTestCase {
	
	

	
	private Collection<RecordText> goodRecords;
	private Collection<BadRecordText> badRecords;
	
	/**
	 * Constructs a new <code>OGFUsageRecordTest</code> with the machine and table
	 * name in which correctly parsed OGF usage record information resides.
	 * @throws IOException 
	 * 
	 */
	public XMLRecordTest() throws IOException {
		super("OGFURMachine", "OGFURecord");
		
		
	}
	
	@Before
	public void setup() throws IOException{
		// Load all good records from the text files specified
				Collection<String> goodRecordFileNames = new ArrayList<String>();
				//goodRecordFileNames.add("no-namespace.xml");
				goodRecordFileNames.add("valid-eg1.xml");
				goodRecordFileNames.add("valid-eg2.xml");
				goodRecordFileNames.add("valid-safe_generated-1.xml");
				goodRecordFileNames.add("valid-safe_generated-2.xml");
				goodRecordFileNames.add("valid-safe_generated-3.xml");
				goodRecordFileNames.add("valid-safe_generated-4.xml");
				goodRecordFileNames.add("valid-safe_generated-5.xml");
				// Set good records
				this.goodRecords = new ArrayList<RecordText>();
				for(String fileName : goodRecordFileNames) {
					File file = new File(getFixtureDir(), fileName);
					String record = TestDataHelper.readFileAsString(file);
					this.goodRecords.add(new RecordText(record));
				}
				
				// Set bad records - No bad record tests yet
				this.badRecords = Collections.emptyList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.epcc.safe.accounting.parsers.AbstractRecordTest#getBadRecords()
	 */
	@Override
	public Collection<BadRecordText> getBadRecords() {
		return this.badRecords;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.epcc.safe.accounting.parsers.AbstractRecordTest#getGoodRecords()
	 */
	@Override
	public Collection<RecordText> getGoodRecords() {
		return this.goodRecords;
	}

	/**
	 * Added appropriate beginning and end tags needed for xml documents to be
	 * well formed
	 * 
	 * @see uk.ac.ed.epcc.safe.accounting.parsers.AbstractRecordTestCase#getUpdateText()
	 */
	@Override
	public String getUpdateText() {
		return "<UsageRecords>" + super.getUpdateText() + "</UsageRecords>";
	}

	/**
	 * Added appropriate beginning and end tags needed for xml documents to be
	 * well formed
	 * 
	 * @see uk.ac.ed.epcc.safe.accounting.parsers.AbstractRecordTestCase#getUpdateText()
	 */
	@Override
	public String getBadUpdateText() {
		return "<UsageRecords>" + super.getBadUpdateText() + "</UsageRecords>";
	}
}
