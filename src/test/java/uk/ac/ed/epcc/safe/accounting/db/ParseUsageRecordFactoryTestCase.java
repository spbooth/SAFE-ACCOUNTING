// Copyright - The University of Edinburgh 2015
/*******************************************************************************
 * Copyright (c) - The Univeristy of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import uk.ac.ed.epcc.safe.accounting.ErrorSet;
import uk.ac.ed.epcc.safe.accounting.ExpressionTargetFactory;
import uk.ac.ed.epcc.safe.accounting.db.AccessorMap.ExpressionTargetProxy;
import uk.ac.ed.epcc.safe.accounting.db.UsageRecordFactory.Use;
import uk.ac.ed.epcc.safe.accounting.expr.DerivedPropertyMap;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionCast;
import uk.ac.ed.epcc.safe.accounting.expr.ExpressionTargetContainer;
import uk.ac.ed.epcc.safe.accounting.expr.ParseException;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.update.IncrementalPropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.PlugInOwner;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerPolicy;
import uk.ac.ed.epcc.safe.accounting.update.SkipRecord;
import uk.ac.ed.epcc.safe.accounting.update.UploadContext;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.model.data.Duration;
/** A superclass for UsageRecordFactory tests.
 * 
 * This implements interface tests with the {@link UploadContext} forwarded onto methods to support
 * legacy sub-classes. 
 * 
 * The {@link #testParser()} and {@link #testBadParser()} methods duplicate the inner workings of the parse flow
 * in order to access greater errors
 * 
 * @author Stephen Booth
 *
 * @param <F>
 * @param <R>
 * @param <I>
 */
public abstract class ParseUsageRecordFactoryTestCase<F extends UsageRecordFactory<R>,R extends UsageRecordFactory.Use,I>
		extends UsageRecordFactoryTestCase<F,R>  implements UsageRecordParseTargetInterfaceTest<I, UsageRecordParseTarget<I>>{
	
	private UsageRecordParseTargetInterfaceTestImp<I, UsageRecordParseTarget<I>> plugin_owner_test = new UsageRecordParseTargetInterfaceTestImp<>(this,()->getPluginOwner() , ()->getUploadContext(), ()->ctx);
			//new PluginOwnerTestCaseImpl<>(()->getPluginOwner());

	
	private UploadContext upload_context = new UploadContext() {
		
		@Override
		public String getUpdateText() throws Exception {
			
			return ParseUsageRecordFactoryTestCase.this.getUpdateText();
		}
		
		@Override
		public String getSkipText() {
			
			return ParseUsageRecordFactoryTestCase.this.getSkipUpdateText();
		}
		
		@Override
		public String getExpectedResource() {
			return ParseUsageRecordFactoryTestCase.this.getReceiveAccountingExpected();
		}
		
		@Override
		public String getExceptionText() {
			return ParseUsageRecordFactoryTestCase.this.getBadUpdateText();
		}
		
		@Override
		public PropertyMap getDefaults() {
			return ParseUsageRecordFactoryTestCase.this.getDefaults();
		}
		
		@Override
		public Map<String, Object> getDefaultParams() {
			return new HashMap<>();
		}
	};
	public String getUpdateText() throws Exception{
		return "";
	}

	public String getBadUpdateText() {
		return "";
	}
	public String getSkipUpdateText() {
		return "";
	}
	public final UsageRecordParseTarget<I> getPluginOwner() {
		F factory = getFactory();
		if( factory instanceof UsageRecordParseTarget) {
			return (UsageRecordParseTarget<I>) factory;
		}
		return (UsageRecordParseTarget<I>) factory.getComposite(PropertyContainerParseTargetComposite.class);
	}
	
	public final UsageRecordParseTarget<I> getParseTarget(){
		F factory = getFactory();
		if( factory instanceof UsageRecordParseTarget) {
			return (UsageRecordParseTarget<I>) factory;
		}
		return (UsageRecordParseTarget<I>) factory.getComposite(PropertyContainerParseTargetComposite.class);
	}
	public abstract PropertyMap getDefaults();

	
	@SuppressWarnings("unchecked")
	public Set getIgnore() {
		Set res = new HashSet();
		res.add(UsageRecordFactory.INSERTED_TIMESTAMP);
		res.add(UsageRecordParseTargetPlugIn.TEXT);
		return res;
	}

	/**
	 * Any exceptions thrown during {@link #testBadParser} are collected together
	 * in an error set. At the end of the parse, this method is called. It is
	 * expected to act no the exceptions that arose. The default behaviour of this
	 * method is to ignore SkipRecord exceptions (prints a message to the screen
	 * saying it happened but doesn't fail the test) and throw any other
	 * exceptions. Parsers that wish to test the parser with bad data can override
	 * this implementation and do more detailed checks on the errors to make sure
	 * the correct exception was thrown when expected and complain if it wasn't.
	 * 
	 * @param successfulRecords
	 *          a collection of all the records which were parsed without any
	 *          problems arising. Can be <code>null</code> if there were no
	 *          successful records.
	 * @param failedRecords
	 *          The errors that occurred during parsing of each record. The detail
	 *          of each error in the <code>ErrorSet</code> should be the erroneous
	 *          record. Can be <code>null</code> if there were no failed records.
	 * @param errors
	 *          Any general errors that occurred which are not related to a
	 *          particular record (starting or ending the parse for example). Can
	 *          be <code>null</code> if there were no general errors.
	 * @throws Exception
	 *           The first error that occurred during parsing that was not just a
	 *           SkipRecord exception
	 */
	protected void processBadParseErrors(Collection<String> successfulRecords,
			ErrorSet failedRecords, ErrorSet errors) throws Exception {
		int skipped = 0;
		Throwable firstError = null;

		if (errors != null && errors.size() > 0) {
			/*
			 * If errors.size() > 0, both iterators should never throw
			 * NoSuchElementException
			 */
			ErrorSet.Entry firstEntry = errors.getEntries().iterator().next();
			ErrorSet.Detail firstDetail = firstEntry.getDetails().iterator().next();
			firstError = firstDetail.getThrowable();
			if (firstError == null)
				fail(firstEntry.getText() + "\n\tDetails: " + firstDetail.getText());
			else
				throw new ParseException(firstError);

		} else if (successfulRecords.isEmpty() == false) {
			// Bad records are supposed to fail so complain about records that passed
			fail(successfulRecords.size() + "records parsed without a problem, "
					+ "however they are marked as being invalid.  First record was: "
					+ successfulRecords.iterator().next());
		} else if (failedRecords != null) {
			for (ErrorSet.Detail details : failedRecords.getAllErrorDetails()) {
				if (details.getThrowable() instanceof SkipRecord)
					skipped++;
				else if (firstError == null)
					firstError = details.getThrowable();
			}
		}

		System.out.println("Skipped records: " + skipped);
		if (firstError != null) {
			firstError.printStackTrace();
			throw new ParseException(firstError);
		}
	}
	@Test
	public void testGetParser() {
		plugin_owner_test.testGetParser();
	}

	public String info(Object o) {
		if (o == null) {
			return "null";
		}
		return "<"+o.toString() + "> [" + o.getClass().getCanonicalName() + "]";
	}

	@Test
	public void testParser() throws Exception {
		Calendar c = Calendar.getInstance();
		c.set(1990, Calendar.JANUARY, 01);
		Date min = c.getTime();
		c.set(2500, Calendar.JANUARY, 01);
		Date max = c.getTime();
		UsageRecordFactory<R> fac = getFactory();
		if (!fac.isValid()) {
			return;
		}
		PlugInOwner<I> owner = getPluginOwner();
		PropertyContainerParser<I> parser = owner.getParser();
		PropExpressionMap dmap = new PropExpressionMap();
		dmap = parser.getDerivedProperties(dmap);
		for(PropertyContainerPolicy pol : owner.getPolicies()){
			dmap = pol.getDerivedProperties(dmap);
		}
		TableSpecification spec = parser.modifyDefaultTableSpecification(new TableSpecification(),dmap,fac.getTag());
		for(PropertyContainerPolicy pol : owner.getPolicies()){
			pol.modifyDefaultTableSpecification(spec,dmap,fac.getTag());
		}
		//assert(spec != null);
		// Make sure update text is provided by the subclasses
		String updateText = getUpdateText();
		if (updateText == null || updateText.length() == 0) {
			fail("No update text specified");
		}

		PropertyMap defaults = getDefaults();
		DerivedPropertyMap meta = new DerivedPropertyMap(getContext());
		meta.setAll(defaults);
		PropExpressionMap expr = owner.getDerivedProperties();
		meta.addDerived(expr);
		Iterator<I> lines = parser.splitRecords(new ByteArrayInputStream(  updateText.getBytes()));
		
		UsageRecordParseTarget<I> target = getParseTarget();
		target.startParse(meta);

		Set ignore = getIgnore();

		while (lines.hasNext()) {
			I current_line = lines.next();
			//System.out.println(parser.formatRecord(current_line));
			try {
				//System.out.println("parse "
				//		+ fac.getParser().getClass().getCanonicalName());
				DerivedPropertyMap map = new DerivedPropertyMap(ctx);
				if (defaults != null) {
					defaults.setContainer(map);
				}
				if (target.parse(map, current_line)) {
					target.lateParse(map);
					R record = fac.makeBDO();
					ExpressionTargetContainer proxy = fac.getExpressionTarget(record);
					map.setContainer(proxy);

					Number runtime = proxy.getProperty(StandardProperties.RUNTIME_PROP, Long
							.valueOf(0L));
					assertNotNull(runtime);
					Date begin = record.getStart();
					Date end = record.getEnd();
					
					Date map_begin = map.getProperty(StandardProperties.STARTED_PROP);
					Date map_end = map.getProperty(StandardProperties.ENDED_PROP);

					
					long res = record.getRecord().getRepository().getResolution();
					if (begin != null && end != null) {

						assertTrue(begin.after(min));
						assertTrue(begin.before(max));
						assertTrue(end.after(min));
						assertTrue(end.before(max));
						// record will only be accurate to resolution of repository
						assertEquals("Starts" ,map_begin.getTime()/res ,begin.getTime()/res);
						assertEquals("Ends" ,map_end.getTime()/res ,end.getTime()/res);
						// assume map is the more accurate
						assertEquals("Runtime check", map_end.getTime() - map_begin.getTime(),
								runtime.longValue());
					}
					ExpressionTargetContainer old =  target.findDuplicate(proxy);

					/*
					 * If there is an identical record already in the database, make sure
					 * it and the new one are identical. Useful for making sure the parser
					 * parsed everything correctly
					 */
					if (old != null) {
						Use u = null;
						if( old instanceof Use) {
							u = (Use) old;
						}else if( old instanceof AccessorMap.ExpressionTargetProxy) {
							u = (Use) ((ExpressionTargetProxy)old).getRecord();
						}
						assertNotNull("DataObject not found", u);
						//System.out.println("Found old record");
						// System.out.println("Parser Charge "+record.getProperty(SafePolicy.SU_PROP,0L)+" "+old.getProperty(SafePolicy.SU_PROP,0L));
						// assertEquals("Charge mismatch ",old.getProperty(SafePolicy.SU_PROP,0L).longValue(),
						// record.getProperty(SafePolicy.SU_PROP,0L).longValue());
						// assertEquals("Raw mis-match",old.getProperty(SafePolicy.RAW_SU_PROP,0L).longValue(),record.getProperty(SafePolicy.RAW_SU_PROP,0L).longValue());
						Map old_map = u.getRecord().getValues();
						assertTrue(old_map.size() > 0);
						// old_map.remove(UsageRecordFactory.INSERTED_TIMESTAMP);
						old_map.remove("Text");
						old_map.remove("Inserted");
						Map record_map = record.getRecord().getValues();

						assertTrue(record_map.size() > 0);
						// assertEquals("map size",record_map.size(), old_map.size());

						boolean incremental = target.getParser() instanceof IncrementalPropertyContainerParser;
						for (Object key : old_map.keySet()) {
							if (old_map.get(key) != null)
								assertTrue("Quant " + key.toString(), record_map
										.containsKey(key));
						}
						boolean ok=true;
						StringBuilder prob = new StringBuilder();
						for (Object key : old_map.keySet()) {
							if (!ignore.contains(key)) {
								System.out.println(key.toString()+" "+record_map.get(key).toString()+" "+old_map.get(key).toString());
								if (old_map.get(key) != null) {
									// incremental are allowed to have null values
									// as each input only specifies part of the input
									if( ! incremental || record_map.get(key) != null){
									String tag = "map mismatch " + key.toString() + " old="
											+ info(old_map.get(key)) + " new="
											+ info(record_map.get(key));
									//assertTrue(tag,

									//compare(old_map.get(key), record_map.get(key)));
									
									if( ! compare(old_map.get(key),record_map.get(key))){
										ok=false;
										prob.append(tag);
										prob.append("\n");
										
										System.err.println(tag+current_line);
									}
									}
								}
							}
						}
						assertTrue(prob.toString(),ok);
					} else {
						System.out.println("Duplicate Record not found\n");
					}
				}
			} catch (SkipRecord e) {
				System.out.println("skipping Record\n");
			}
		}

		String errors = target.endParse().toString();
		if(errors.length() > 0)
			System.err.println(errors);
	}
@Test
	public void testBadParser() throws Exception {
		String updateText = this.getBadUpdateText();

		// If there's no bad records to test, this test is complete
		if (updateText == null || updateText.length() == 0) {
			System.out.println("no bad records to test");
			return;
		}

		UsageRecordFactory<R> fac = getFactory();
		if (!fac.isValid()) {
			return;
		}
		UsageRecordParseTarget<I> target = getParseTarget();
		Collection<String> sucessfulRecords = new ArrayList<>();
		ErrorSet failedRecords = new ErrorSet();
		ErrorSet errors = new ErrorSet();

		PropertyMap defaults = getDefaults();
		PropertyContainerParser<I> parser = target.getParser();
		Iterator<I> lines = parser.splitRecords(new ByteArrayInputStream(  updateText.getBytes()));
		
		
		try {
			target.startParse(defaults);
		} catch (Exception e) {
			errors.add("start parse error", e.getMessage(), e);
			this.processBadParseErrors(sucessfulRecords, failedRecords, errors);
			return;
		}

		

		while (lines.hasNext()) {
			I rec = lines.next();
			String current_line = parser.formatRecord(rec);
			//System.out.println(current_line);
			//System.out.println("parse "
			//		+ parser.getClass().getCanonicalName());
			DerivedPropertyMap map = new DerivedPropertyMap(ctx);
			if (defaults != null) {
				defaults.setContainer(map);
			}
			try {
				target.parse(map, rec);
				target.lateParse(map);
				sucessfulRecords.add(current_line);
			} catch (Exception e) {
				/*
				 * subclasses may need the exception to see if it was expected or not.
				 * As a result, e is included in the error
				 */
				failedRecords.add("parse error", current_line, e);
			}
		}

		try {
			System.out.println(target.endParse());
		} catch (Exception e) {
			errors.add("end parse error", e.getMessage(), e);
		}

		/*
		 * Check for any exceptions that should have been thrown and throw them if
		 * they are not expected
		 */
		this.processBadParseErrors(sucessfulRecords, failedRecords, errors);
	}
@Test
	public final void testReceiveAccounting() throws Exception {
		plugin_owner_test.testReceiveAccounting();
	}

    public String getReceiveAccountingExpected() {
    	return null;
    }

public final void receiveAccounting(String updateText) throws Exception {
	plugin_owner_test.receiveAccounting(updateText);
}
@Test
	public void testGetPolicies() {
		plugin_owner_test.testGetPolicies();
	}
@Test
	public void testGetMapper() {
		UsageRecordFactory<R> fac = getFactory();
		if (!fac.isValid()) {
			return;
		}
		ExpressionTargetFactory etf = ExpressionCast.getExpressionTargetFactory(fac);
		AccessorMap map = etf.getAccessorMap();

		assertNotNull(map);

	}

	public boolean compare(Object a, Object b) {
		if (a instanceof Duration) {
			a = Long.valueOf(((Duration) a).getMilliseconds());
		}
		if (b instanceof Duration) {
			b = Long.valueOf(((Duration) b).getMilliseconds());
		}
		// System.out.println(" "+a.getClass().getCanonicalName()+" "+b.getClass().getCanonicalName()+" "+a.toString()+" "+b.toString());
		
		if( (a instanceof Integer || a instanceof Long) &&( b instanceof Integer || b instanceof Long)){
			// compare as integer
			return ((Number) a).longValue() == ((Number) b).longValue();
		}else if ( a instanceof Number && b instanceof Number ){
			// compare as floating
			return ((Number) a).floatValue() == ((Number) b).floatValue();
		}
		return a.equals(b);
	}

	@Override
	public void testGetFinder() {
		plugin_owner_test.testGetFinder();
	}

	@Override
	public void testGetDerivedProperties() {
		plugin_owner_test.testGetDerivedProperties();
		
	}

	@Override
	public UploadContext getUploadContext() {
		return upload_context;
	}

	@Override
	public void testSkipLines() throws Exception {
		plugin_owner_test.testSkipLines();
		
	}

	@Override
	public void testExceptionLines() throws Exception {
		plugin_owner_test.testExceptionLines();
	}

	@Override
	public void testGoodLines() throws Exception {
		plugin_owner_test.testGoodLines();
		
	}
   
	
	
}