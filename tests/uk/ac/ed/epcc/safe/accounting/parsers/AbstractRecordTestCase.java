// Copyright - The University of Edinburgh 2015
/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.ErrorSet;
import uk.ac.ed.epcc.safe.accounting.db.ConfigUsageRecordFactory;
import uk.ac.ed.epcc.safe.accounting.db.ParseUsageRecordFactoryTestCase;
import uk.ac.ed.epcc.safe.accounting.db.UsageRecordFactory;
import uk.ac.ed.epcc.safe.accounting.db.UsageRecordFactory.Use;
import uk.ac.ed.epcc.safe.accounting.expr.ParseException;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.update.PlugInOwner;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerPolicy;
import uk.ac.ed.epcc.webapp.config.ConfigService;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;

/**
 * A superclass for some record parsing tests. Most of the test functionality
 * is provided by this class. The idea is that subclasses construct appropriate
 * <code>Record</code> object and return them via the abstract methods defined
 * in this class. This class uses the methods returned by the subclasses to
 * construct the text chunks of usage record required by the tests is
 * <code>UsageRecordFactoryTestCase</code>. This class also handles the testing
 * of bad record that are expected to throw exceptions.
 * 
 * 
 * @author jgreen4
 * 
 */
public abstract class AbstractRecordTestCase<R> extends
		ParseUsageRecordFactoryTestCase<ConfigUsageRecordFactory<UsageRecordFactory.Use,R>,UsageRecordFactory.Use,R> {
	

	private String machineName;

	/*
	 * The name of the table the parser parses records into, and the table that
	 * should already contain correct records for this test to use to check the
	 * parser has parsed data correctly.
	 */
	private String tableName;

	/**
	 * Constructs a new <code>AbstractRecordTest</code>.
	 *
	 * @param machineName The name of the machine whose table stores records that are being tested
	 * @param tableName The table in which records are stored
	 */
	public AbstractRecordTestCase(String machineName, String tableName) {
		this.machineName = machineName;
		this.tableName = tableName;
	}
	
	@Before
	public void loadData() throws Exception{
		//TestDataHelper.loadDataSetsForTest(getClass().getSimpleName());
		//save(getClass().getSimpleName());
		load(getClass().getSimpleName());
		getContext().getService(ConfigService.class).clearServiceProperties();
	}
	@Test
	public void testCreateTable(){
		PlugInOwner<R> fac = getFactory();
		
		PropertyContainerParser<R> parser = fac.getParser();
		PropExpressionMap map = new PropExpressionMap();
		map = parser.getDerivedProperties(map);
		for(PropertyContainerPolicy pol : fac.getPolicies()){
			map = pol.getDerivedProperties(map);
		}
		TableSpecification spec = parser.modifyDefaultTableSpecification(ctx,new TableSpecification(),map,tableName);
		for(PropertyContainerPolicy pol : fac.getPolicies()){
			pol.modifyDefaultTableSpecification(ctx,spec,map,tableName);
		}
		// We expect at least a unique key
		assertTrue("No index for "+tableName,spec.getIndexes().hasNext());
	}
	/*
	 * ##########################################################################
	 * OVERRIDDEN METHODS
	 * ##########################################################################
	 */

	/**
	 * Required by the superclass. The name of the machine that is associated with
	 * the appropriate parser is set here
	 */
	@Override
	public PropertyMap getDefaults() {
		PropertyMap defaults = new PropertyMap();
		defaults.setProperty(StandardProperties.MACHINE_NAME_PROP, this.machineName);
		return defaults;
	}

	/**
	 * Required by the superclass
	 */
	
	@Override
	public final ConfigUsageRecordFactory<Use,R> getFactory() {
		return new ConfigUsageRecordFactory<Use,R>(ctx, this.tableName);
	}

	/**
	 * <p>
	 * Fetches a string holding the text of one or more good records for testing.
	 * This method essentially concatenates the records in the the collection
	 * returned by {@linkplain #getGoodRecords()}.
	 * </p>
	 * <p>
	 * This method assumes that records can either be separated with a new line,
	 * or records can just be chained together and the parser will be able to
	 * separate them. This may be a bad assumption to make but it works for now
	 * with PBS, SGE and OGF usage records. Subclasses can always override this
	 * method if the approach is too simplistic.
	 * </p>
	 * 
	 * @return The usage record to test.
	 */
	@Override
	public String getUpdateText() {
		StringBuilder sb = new StringBuilder(10000);
		Collection<? extends RecordText> records = this.getGoodRecords();

		for (RecordText record : records)
			sb.append(record.getText()).append("\n");

		return sb.toString();
	}

	/**
	 * <p>
	 * Fetches a string holding the text of zero or more bad records for testing.
	 * This method essentially concatenates the records in the the collection
	 * returned by {@linkplain #getGoodRecords()}. This method may return an empty
	 * string if no bad records are present.
	 * </p>
	 * <p>
	 * This method assumes that records can either be separated with a new line,
	 * or records can just be chained together and the parser will be able to
	 * separate them. This may be a bad assumption to make but it works for now
	 * with PBS, SGE and OGF usage records. Subclasses can always override this
	 * method if the approach is too simplistic.
	 * </p>
	 * 
	 * @return The usage record to test.
	 */
	@Override
	public String getBadUpdateText() {
		StringBuilder sb = new StringBuilder(10000);
		Collection<? extends RecordText> records = this.getBadRecords();

		for (RecordText record : records)
			sb.append(record.getText()).append("\n");

		return sb.toString();
	}

	/**
	 * Replaces the superclass's implementation. If exceptions are thrown, this
	 * method makes sure they are expected. If the exception wasn't expected, it
	 * is re-thrown which will fail the test with an error. This method allows
	 * records to be marked as bad to make sure the parser fails to parse them
	 * appropriately.
	 */
	@Override
	protected void processBadParseErrors(Collection<String> successfulRecords,
			ErrorSet failedRecords, ErrorSet errors) throws Exception {
		HashMap<Integer, BadRecordText> recordTable = new HashMap<Integer, BadRecordText>();
		PropertyContainerParser<R> parser = getFactory().getParser();
		/*
		 * Put all the record in a table referenced by hash code for fast look up
		 * and comparison to the record that caused the error
		 */
		for (BadRecordText record : this.getBadRecords()){
			
			Iterator<R> lines = parser.splitRecords(record.getText());
			while(lines.hasNext()){
				R rec = lines.next();
				String line = parser.formatRecord(rec);
				System.out.println("hash="+line.hashCode()+" text=["+line+"]");
				recordTable.put(line.hashCode(), record);
			}
		}

		/*
		 * All records should have failed. So any that pass indicate a failed test
		 */
		if (successfulRecords.size() > 0) {
			String firstSuccess = successfulRecords.iterator().next();

			BadRecordText thisRecord = recordTable.get(firstSuccess.hashCode());
			assertNotNull("Didn't recognise record : " + firstSuccess, thisRecord);

			Class<? extends Throwable> expectedException;
			expectedException = thisRecord.getExpectedThrowableType();

			fail("Expected exception '" + expectedException.getName()
					+ "' was expected by not thrown.  Problem with record: "
					+ firstSuccess);
		}

		if (errors != null && errors.size() > 0) {
			/*
			 * If errors.size() > 0, both iterators should never throw
			 * NoSuchElementException
			 */
			ErrorSet.Entry firstEntry = errors.getEntries().iterator().next();
			ErrorSet.Detail firstDetail = firstEntry.getDetails().iterator().next();
			Throwable firstError = firstDetail.getThrowable();
			if (firstError == null)
				fail(firstEntry.getText() + "\n\tDetails: " + firstDetail.getText());
			else
				throw new ParseException(firstError);
		}

		/*
		 * Go through all the errors and make sure they were expected
		 */
		for (ErrorSet.Detail details : failedRecords.getAllErrorDetails()) {
			String record = details.getText();
			System.out.println("look for hash="+record.hashCode()+" text=["+record+"]");
			RecordText thisRecord = recordTable.get(record.hashCode());

			assertNotNull("Didn't recognise record : [" + record+"]", thisRecord);

			Throwable errorEncountered = details.getThrowable();
			assertNotNull(
					"Encountered error should be supplied but it wasn't present.  "
							+ "Cannot continue testing", errorEncountered);

			if (errorEncountered == null) {
				fail("This unit test needs the encountered error to be supplied but "
						+ "it wasn't present.  Cannot continue testing");
			} else {

				if (thisRecord instanceof BadRecordText) {
					BadRecordText thisBadText = (BadRecordText) thisRecord;
					Class<?> expectedClass = thisBadText.getExpectedThrowableType();
					Class<?> encounteredClass = errorEncountered.getClass();

					/*
					 * if the encountered exception isn't the same type as the exception
					 * thrown, throw it - it's unexpected and so should fail the test.
					 * Could put fail(...) in here but nicer to allow the whole exception
					 * to be thrown so the stack trace can be easily followed
					 */
					if (expectedClass.isAssignableFrom(encounteredClass) == false) {
						throw new ParseException(errorEncountered);
					}

					if (thisBadText.hasCause()) {
						Class<?> expectedCause = thisBadText.getExpectedCauseType();
						Class<?> encounteredCause = errorEncountered.getCause().getClass();

						if (encounteredCause == null)
							fail("The " + encounteredClass.getName()
									+ "exception encountered was expected to have the cause "
									+ expectedCause.getClass().getName()
									+ ", but no cause was found.  Cannot continue testing");
						else if (expectedCause.isAssignableFrom(encounteredCause) == false)
							throw new ParseException(errorEncountered);
					}

				} else {
					throw new ParseException(errorEncountered);
				}
			}
		}
	}

	

	/*
	 * ##########################################################################
	 * TEST AND ABSTRACT METHODS
	 * ##########################################################################
	 */

	/**
	 * Returns all the bad Records - i.e. the record that the parser should not be
	 * able to parse. The returned collection must not be <code>null</code> but
	 * can be empty.
	 * 
	 * @return All Records that when parsed should throw exceptions
	 */
	public abstract Collection<BadRecordText> getBadRecords();

	/**
	 * @return all Records that the parser should successfully parse. The returned
	 *         collection must not be <code>null</code> but can be empty.
	 */
	public abstract Collection<RecordText> getGoodRecords();

	/*
	 * ##########################################################################
	 * INNER CLASSES
	 * ##########################################################################
	 */

	/**
	 * A container for a single Record. Holds the hash of the record (so it only
	 * needs to be calculated once) as well as the record itself.
	 * 
	 * WARNING: do not put the text for more than one record in this object
	 * 
	 * @author jgreen4
	 * 
	 */
	public static class RecordText {
		private String record;
		private int hash;

		public RecordText(String record) {

			this.record = record;
			this.hash = record.hashCode();
		}

		/**
		 * Compares this Record with another. Two Records are considered equal if
		 * the hashes of their record text are equal
		 * 
		 * @param otherRecord
		 *          The Record to compare to this Record
		 * @return <code>true</code> if the hash of the text of this Record is the
		 *         same as the hash of the text of the other Record;
		 */
		public boolean equals(RecordText otherRecord) {
			return this.hash == otherRecord.hash;
		}

		/**
		 * @return this Record's record text
		 */
		public String getText() {
			return this.record;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return this.hash;
		}
	}

	/**
	 * A container for Records which are malformed in some way. As well as holding
	 * the record text and hash of the record, this object also holds the expected
	 * exception the parser should throw when the malformed record is parsed. The
	 * object optionally holds another throwable which is the cause of the
	 * exception that was thrown. This is useful if the exception thrown is just a
	 * wrapper for the main exception. In this instance, the actual exception can
	 * be checked to make sure it is what is expected
	 * 
	 * @author jgreen4
	 * 
	 */
	public static class BadRecordText extends RecordText {

		/**
		 * The class of exception that should be thrown when the text of this record
		 * is parsed. Will not be null
		 */
		private Class<? extends Throwable> expectedThrowableClass;

		/**
		 * The expected cause of the exception thrown when the text of this record
		 * is parsed. If the cause shouldn't be checked, this will be null.
		 */
		private Class<? extends Throwable> expectedCauseClass;

		/**
		 * Constructs a new <code>BadRecordText</code> with the specified exception
		 * that should be thrown when the text of this record is parsed.
		 * 
		 * @param record
		 *          The malformed record
		 * @param t
		 *          The exception expected when the record's text is parsed
		 */
		public BadRecordText(String record, Class<? extends Throwable> t) {
			this(record, t, null);
		}

		/**
		 * Constructs a new <code>BadRecordText</code> with the specified exception
		 * that should be thrown when the text of this record is parsed, and the
		 * expected exception that caused the exception to be thrown (the cause) If
		 * no cause is expected, this can be null.
		 * 
		 * @param record
		 *          The malformed record
		 * @param t
		 *          The exception expected when the record's text is parsed
		 * @param cause
		 *          The cause of <code>t</code>, or <code>null</code> is a cause
		 *          shouldn't be checked for
		 */
		public BadRecordText(String record, Class<? extends Throwable> t,
				Class<? extends Throwable> cause) {
			super(record);

			if (t == null)
				throw new NullPointerException(
						"The throwable this bad record is expected to cause must be "
								+ "specified");
			this.expectedThrowableClass = t;
			this.expectedCauseClass = cause;
		}

		/**
		 * @return The class of the exception that should be thrown when the text of
		 *         this record is parsed
		 */
		public Class<? extends Throwable> getExpectedThrowableType() {
			return this.expectedThrowableClass;
		}

		/**
		 * @return The class of the cause of the exception that should be thrown
		 *         when the text of this record is parsed - or <code>null</code> if
		 *         the cause should not be checked
		 */
		public Class<? extends Throwable> getExpectedCauseType() {
			return this.expectedCauseClass;
		}

		/**
		 * If the exception thrown when this record is parsed is just a wrapper for
		 * the real reason for failure, this method returns <code>true</code>.
		 * Otherwise, it returns <code>false</code>.
		 * 
		 * @return <code>true</code> if there should be a causing exception for the
		 *         exception thrown when this record is parsed. <code>false</code>
		 *         otherwise
		 */
		public boolean hasCause() {
			return this.expectedCauseClass != null;
		}
		
	}
	public File getFixtureDir(){
		return new File(
				ctx.getInitParameter("test.fixture.dir",
				"tests/uk/ac/ed/epcc/safe/accounting/parsers/ogfur-records"));
	}
}