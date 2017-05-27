//| Copyright - The University of Edinburgh 2011                            |
//|                                                                         |
//| Licensed under the Apache License, Version 2.0 (the "License");         |
//| you may not use this file except in compliance with the License.        |
//| You may obtain a copy of the License at                                 |
//|                                                                         |
//|    http://www.apache.org/licenses/LICENSE-2.0                           |
//|                                                                         |
//| Unless required by applicable law or agreed to in writing, software     |
//| distributed under the License is distributed on an "AS IS" BASIS,       |
//| WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.|
//| See the License for the specific language governing permissions and     |
//| limitations under the License.                                          |
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import uk.ac.ed.epcc.safe.accounting.expr.IntPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropertyCastException;
import uk.ac.ed.epcc.safe.accounting.expr.StringPropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.AutoTable;
import uk.ac.ed.epcc.safe.accounting.update.BatchParser;
import uk.ac.ed.epcc.safe.accounting.update.OptionalTable;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerParser;
import uk.ac.ed.epcc.safe.accounting.update.SkipRecord;
import uk.ac.ed.epcc.safe.accounting.update.StringSplitter;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
import uk.ac.ed.epcc.webapp.jdbc.exception.DataException;
import uk.ac.ed.epcc.webapp.model.data.Duration;

/* 
 * Parsing based on NGS Perl parser for LSF logs.
 * 
 * Assumes the record format complies with the LSF documentation at
 * the following address:
 * 
 * http://wwwuser.gwdg.de/~parallel/lsf_doc/ref_6.0/lsb.acct.5.html
 * 
 */
@SuppressWarnings("unused")


public class LSFParser extends BatchParser implements  Contexed {
	private AppContext c;
	
	
	
	// hardwired event types
	private static final String JOB_FINISH = "JOB_FINISH";
	private static final String EVENT_ADRSV_FINISH = "EVENT_ADRSV_FINISH";
	
	// comparison string for blank string entries
	
	private static final int missingValueInteger = -1;
	private static final long missingValueLong = -1L;
	public LSFParser(AppContext c){
		this.c=c;
	}
	public static  final PropertyRegistry lsf = 
		new PropertyRegistry("lsf","The LSF batch properties");

	
	@AutoTable
	public static final PropertyTag<String> LSF_QUEUE_PROP = new PropertyTag<String>(lsf,"Queue",String.class,"LSF queue");
	public static final PropertyTag<String> LSF_HOSTNAME_PROP = new PropertyTag<String>(lsf,"Hostname",String.class,"LSF hostname");
	@AutoTable
	public static final PropertyTag<String> LSF_USERNAME_PROP = new PropertyTag<String>(lsf,"UserName",String.class,"LSF user name");
	@AutoTable(length=4096)
	public static final PropertyTag<String> LSF_JOBNAME_PROP = new PropertyTag<String>(lsf,"JobName",String.class,"LSF job name");

	public static final PropertyTag<String> LSF_BATCH_COMMAND_PROP = new PropertyTag<String>(lsf,"BatchCommand",String.class,"LSF batch command");

	@AutoTable(unique=true)
	public static final PropertyTag<Integer> LSF_JOBID_PROP = new PropertyTag<Integer>(lsf,"JobID",Integer.class,"LSF job ID");
	@AutoTable(unique=true)
	public static final PropertyTag<Integer> LSF_JOB_ARRAY_INDEX = new PropertyTag<Integer>(lsf, "JobArrayIndex", Integer.class,"Job array index");
	@AutoTable
	public static final PropertyTag<Integer> LSF_JOBEXIT_PROP = new PropertyTag<Integer>(lsf,"JobExit",Integer.class,"LSF job exit status");
	@AutoTable
	public static final PropertyTag<Date> LSF_SUBMITTED_PROP = new PropertyTag<Date>(lsf,"SubmittedTimestamp",Date.class,"Time job was submitted");
	@AutoTable(unique=true)
	public static final PropertyTag<Date> LSF_LOGDATE_PROP = new PropertyTag<Date>(lsf,"LogDate",Date.class,"Log date");
	@AutoTable
	public static final PropertyTag<Date> LSF_STARTED_PROP = new PropertyTag<Date>(lsf,"StartedTimestamp",Date.class,"Time job was started");
	@AutoTable
	public static final PropertyTag<Number> LSF_WALLCLOCK_PROP = new PropertyTag<Number>(lsf,"Wall",Number.class,"Wallclock in seconds as reported directly by LSF");
	@AutoTable
	public static final PropertyTag<String> LSF_PROJECT_PROP = new PropertyTag<String>(lsf,"Project",String.class,"LSF Project");
	@AutoTable
	public static final PropertyTag<Number> LSF_CPU_TIME_PROP = new PropertyTag<Number>(lsf,"CPUTime",Number.class,"Total CPU time (seconds) used by all processes");
	@OptionalTable(target=Long.class)
	public static final PropertyTag<Number> LSF_MEMORYUSED_PROP = new PropertyTag<Number>(lsf,"MemUsed",Number.class,"LSF max memory usage in KB");
	@OptionalTable(target=Long.class)
	public static final PropertyTag<Number> LSF_VIRTUALMEMORYUSED_PROP = new PropertyTag<Number>(lsf,"VirtualMemUsed",Number.class,"LSF max virtual memory usage in KB");
	@AutoTable(target=Integer.class)
	public static final PropertyTag<Integer> LSF_MAXPROCESSORS_PROP = new PropertyTag<Integer>(lsf,"MaxProcessors",Integer.class,"LSF maximum number of processors used");
	@AutoTable(target=Integer.class)
	public static final PropertyTag<Integer> LSF_NUMPROCESSORS_PROP = new PropertyTag<Integer>(lsf,"NumProcessors",Integer.class,"LSF number of processors used");
	
	@OptionalTable(target=Integer.class)
	public static final PropertyTag<Integer> LSF_NUM_EXECHOSTS_PROP = new PropertyTag<Integer>(lsf,"NumExecHosts",Integer.class,"LSF number of exec hosts");
	@OptionalTable(length=4096)
	public static final PropertyTag<String> LSF_EXEC_HOSTS_PROP = new PropertyTag<String>(lsf,"ExecHosts",String.class,"LSF Exec Hosts");

	
	// Define indices into the record as a convenience.
	// Not necessary but hopefully makes the code more maintainable.
	// We dont use all these indicices, but defining them all
	// helps to keep track of where everything is and hopefully reduces errors.
	
	
	private static final int EVENT_TYPE_INDEX = 0;
	private static final int VERSION_NUMBER_INDEX = 1;
	private static final int EVENT_TIME_INDEX = 2;
	private static final int JOB_ID_INDEX = 3;
	private static final int USER_ID_INDEX = 4;
	private static final int OPTIONS_INDEX = 5;
	private static final int NUM_PROCESSORS_INDEX = 6;
	private static final int SUBMIT_TIME_INDEX = 7;
	private static final int BEGIN_TIME_INDEX = 8;
	private static final int TERM_TIME_INDEX = 9;
	private static final int START_TIME_INDEX = 10;
	private static final int USERNAME_INDEX = 11;
	private static final int QUEUE_INDEX = 12;
	private static final int RESOURCE_REQ_INDEX = 13;
	private static final int DEP_COND_INDEX = 14;
	private static final int PRE_EXECCMD_INDEX = 15;
	private static final int FROMHOST_INDEX = 16;
	private static final int CWD_INDEX = 17;
	private static final int INFILE_INDEX = 18;
	private static final int OUTFILE_INDEX = 19;
	private static final int ERRFILE_INDEX = 20;
	private static final int JOBFILE_INDEX = 21;
	private static final int NUM_ASKED_HOSTS_INDEX = 22;
	
	/*
	 * Things get tricky here. If NUM_ASKED_HOSTS is > 0, the next lot of data
	 * that will be returned from our split is the set of host names.
	 * Subsequent indices therefore have to be offset from the end of the list of hosts!
	 * 
	 */
	
	
	private static final int JOB_STATUS_INDEX_OFFSET = 0;
	private static final int HOST_FACTOR_INDEX_OFFSET = 1;
	private static final int JOB_NAME_INDEX_OFFSET = 2;
	private static final int BATCH_COMMAND_INDEX_OFFSET = 3;
	
	private static final int RU_UTIME_INDEX_OFFSET = 4;
	private static final int RU_STIME_INDEX_OFFSET = 5;
	private static final int RU_MAXRSS_INDEX_OFFSET = 6;
	private static final int RU_IXRSS_INDEX_OFFSET = 7;
	private static final int RU_ISMRSS_INDEX_OFFSET = 8;
	private static final int RU_IDRSS_INDEX_OFFSET = 9;
	private static final int RU_ISRSS_INDEX_OFFSET = 10;
	private static final int RU_MINFLT_INDEX_OFFSET = 11;
	private static final int RU_MAGFLT_INDEX_OFFSET = 12;
	private static final int RU_NSWAP_INDEX_OFFSET = 13;
	private static final int RU_INBLOCK_INDEX_OFFSET = 14;
	private static final int RU_OUBLOCK_INDEX_OFFSET = 15;
	private static final int RU_IOCH_INDEX_OFFSET = 16;
	private static final int RU_MSGSND_INDEX_OFFSET = 17;
	private static final int RU_MSGRCV_INDEX_OFFSET = 18;
	private static final int RU_NSIGNALS_INDEX_OFFSET = 19;
	private static final int RU_NVCSW_INDEX_OFFSET = 20;
	private static final int RU_NIVCSW_INDEX_OFFSET = 21;
	private static final int RU_EXUTIME_INDEX_OFFSET = 22;
	
	private static final int MAILUSER_INDEX_OFFSET = 23;
	private static final int PROJECTNAME_INDEX_OFFSET = 24;
	private static final int EXITSTATUS_INDEX_OFFSET = 25;
	private static final int MAXNUM_PROCESSORS_INDEX_OFFSET = 26;
	private static final int LOGIN_SHELL_INDEX_OFFSET = 27;
	private static final int JOBSCHEDULER_TIMEEVENT_INDEX_OFFSET = 28;
	private static final int JOBARRAY_IDX_INDEX_OFFSET = 29;
	private static final int MAX_RMEM_INDEX_OFFSET = 30;
	private static final int MAX_RSWAP_INDEX_OFFSET = 31;
	private static final int INFILE_SPOOL_INDEX_OFFSET = 32;
	private static final int COMMAND_SPOOL_INDEX_OFFSET = 33;
	private static final int RSVLD_INDEX_OFFSET = 34;
	private static final int ADDITIONAL_INFO_INDEX_OFFSET = 34;
	private static final int EXIT_INFO_INDEX_OFFSET = 36;
	private static final int WARNING_ACTION_INDEX_OFFSET = 37;
	private static final int WARNING_TIMEPERIOD_INDEX_OFFSET = 38;
	private static final int CHARGED_SAAP_INDEX_OFFSET = 39;
	private static final int SLA_INDEX_OFFSET = 40;
	
	
	private class Tokenizer {
		
		private char[] values;
		int valuesIndex = 0;
		
		
		private boolean hasNext()
		{
			return values.length > valuesIndex;
		}
		
		private char getNext() throws IndexOutOfBoundsException
		{
			if ( hasNext())
			{
				return values[valuesIndex++];
			} else
			{
				throw new IndexOutOfBoundsException();
			}
		}
		
		
		public Tokenizer(String data)
		{
			values = data.toCharArray();
		}
//		List<String> tokenize()
//		{
//			
//			// we can be inside a string, or we are processing values
//			boolean readingString = false;
//		
//			
//			List<String> tokens = new ArrayList<String>();
//			StringBuffer buffer = new StringBuffer();
//			
//			while(hasNext())
//			{
//				char nextChar = getNext();
//				if ( readingString == true)
//				{
//					// check if its a quote
//					if ( nextChar == '\"')
//					{
//						// so we are in a string and we've got a quote char
//						// if the next char is a quote as well, assume we've hit a double escaped string
//	
//						try
//						{
//							nextChar = getNext();
//						} catch (IndexOutOfBoundsException e)
//						{
//							// we've hit the end of the line
//							// output the contents of the buffer
//							System.out.println("pushing token " + buffer.toString());
//							tokens.add(buffer.toString());
//							break;
//						}
//	
//						if ( nextChar == '\"')
//						{
//							// push both characters onto our buffer and continue
//							buffer.append("\"\"");
//						} else
//						{
//							// we are in a string, but have a quote char followed by something
//							// which isnt a quote
//							
//							readingString = false;
//							tokens.add(buffer.toString());
//							buffer.setLength(0);
//						}
//					} else
//					{
//						buffer.append(nextChar);
//					}
//				} else
//				{
//					if ( nextChar == '\"') // found a quote char so start a string
//					{
//						readingString = true;
//					} else if ( nextChar == ' ')
//					{
//							tokens.add(buffer.toString());
//							buffer.setLength(0);
//						
//					} else
//					{
//						buffer.append(nextChar);
//					}
//				}
//			}
//			
//			return tokens;
//		}
		List<String> tokenize()
		{
			
		
		
			
			List<String> tokens = new ArrayList<String>();
			StringBuilder buffer = new StringBuilder();
			
			if( ! hasNext() ){
				// empty string
				return tokens;
			}
			char nextChar = getNext(); // this is always the look ahead token 
		
			while(true ){
				// first skip any whitespace
				while( Character.isWhitespace(nextChar)){
					if( ! hasNext() ){
						// whitespace at end of line
						return tokens;
					}
					nextChar = getNext();
				}
				// char must be non-whitespace
				// clear buffer ready to build next token
				buffer.setLength(0);
				if( nextChar == '\"' ){
					// start to parse a quoted string
					
					nextChar = getNext(); // drop first quote and go to start of value
					
					boolean scanning = true;
					while(scanning){
						if( nextChar == '\"'  ){
							if( ! hasNext() ){
					    		// end of string was end of line
					    		tokens.add(buffer.toString());
					    		return tokens;
					    	}
							nextChar = getNext(); // drop the first quote seen
							scanning = (nextChar == '\"'); // keep scanning on repeated quote
							                               // and pass second quote to consume step
						}
						if( scanning ){
					    	buffer.append(nextChar);
					    	if( ! hasNext() ){
					    		// bit odd this as the string is not terminated
					    		tokens.add(buffer.toString());
					    		return tokens;
					    	}
					    	nextChar = getNext();
						}
					}
					tokens.add(buffer.toString());
				}else{
					// parse a bare token
					while( ! Character.isWhitespace(nextChar)){
						buffer.append(nextChar);
						if( ! hasNext() ){
							tokens.add(buffer.toString());
							return tokens;
						}
						nextChar = getNext();
					}
					tokens.add(buffer.toString());
				}
				
				
				
			}
			
			
			
		}
	}
	
	
	// missing integer values are represented by a specific value
	private boolean isDefined(int val)
	{
		return missingValueInteger != val;
	}
	private boolean isDefined(long val)
	{
		return missingValueLong != val;
	}
	private boolean isDefined(Integer val)
	{
		return val != null && missingValueInteger != val.intValue();
	}
	
	private boolean isDefined(String val)
	{
		if (val == null)
		{
			return false;
		}
		
		return val.trim().length() != 0;
	}
	
	
	static{
		lsf.lock();
	}
	
	
	
	
	
	@Override
	public Iterator<String> splitRecords(String update)
	throws AccountingParseException 
	{
		
		
		// merge continuation lines before splitting.
		// seems to add space at beginning of continuation line
		return new StringSplitter(update.replaceAll("!\n ", ""));
	}
	
	// debug method to show tokenised values
	private void printValues(List<String> values)
	{
		Iterator<String> it = values.iterator();
		
		int i=0;
		while(it.hasNext())
		{
			String value = it.next();
			//System.out.println(i + " " + value);
			i++;
		}
	}
	private Integer getInteger(List<String> values, int idx){
		String s = values.get(idx);
		if( s == null || s.trim().length() == 0){
			return null;
		}
		return Integer.parseInt(s);
	}
	private Date getDate(List<String> values, int idx){
		String s = values.get(idx);
		if( s == null || s.trim().length() == 0){
			return null;
		}
		return readTime(s);
	}
	
	public boolean parse(PropertyMap r,String record) throws AccountingParseException {

		
		List<String> values = null;
		try
		{
			Tokenizer tokenizer = new Tokenizer(record);
			values = tokenizer.tokenize();
			
		} catch (Exception e)
		{
			throw new AccountingParseException("Error processing record",  e);
		}
		
		
		// sanity check the returned tokens
		if(values == null || values.size() == 0 )
		{
			throw new SkipRecord("No data found");
		}
		
		// check the event type, reject if advance reservation
		String eventType = values.get(0);
		if(EVENT_ADRSV_FINISH.compareTo(eventType)==0)
		{
			throw new SkipRecord("Advance reservation");
		}
		// reject the job if we dont recognise the event type
		
		
		if(JOB_FINISH.compareTo(eventType)!=0)
		{
			throw new AccountingParseException("Unrecognised job event " + eventType);
		}
		
		// should probably check version number in case the format varies between versions
		String version = values.get(VERSION_NUMBER_INDEX);
		
		
		// check if we have a host list, in which case we need to offset
		// some of our indices
		
		/*
		 * ..
		 * [numAskedHosts]
		 * [askedHost1]
		 * ..
		 * [askedHostN]
		 * [numExecHosts]
		 * [execHost1]
		 * ..
		 * [execHostN]	all subsequent indices are offset from this point
		 * 
		 */
		
		// numAskedHosts is an absolute index, everything beyond it must be based on
		// a calculated offset.
		
		int numAskedHosts = Integer.valueOf(values.get(NUM_ASKED_HOSTS_INDEX));
		int dataOffset = NUM_ASKED_HOSTS_INDEX + numAskedHosts + 1;
		
		// check how many exec hosts we have
		int indexOfExecHosts = dataOffset;
		int numExecHosts = Integer.valueOf(values.get(indexOfExecHosts));
		
		// increment the index beyond the set of exec hosts
		dataOffset += numExecHosts + 1;
		
		// gather the information we need
		
		String localUserId = values.get(USERNAME_INDEX);
		Integer jobID = getInteger(values,JOB_ID_INDEX);
		Integer arrayID = getInteger(values,JOBARRAY_IDX_INDEX_OFFSET + dataOffset);
		Date eventTime = getDate(values,EVENT_TIME_INDEX);
		
		String queue = values.get(QUEUE_INDEX);
		
		String jobName = values.get(JOB_NAME_INDEX_OFFSET + dataOffset);
		String batchCommand = values.get(BATCH_COMMAND_INDEX_OFFSET + dataOffset);
		String projectName = values.get(PROJECTNAME_INDEX_OFFSET + dataOffset);
		int jobStatus = getInteger(values, JOB_STATUS_INDEX_OFFSET + dataOffset);
		
		try {
			Long.valueOf(values.get(MAX_RMEM_INDEX_OFFSET + dataOffset));
			Long.valueOf(values.get(MAX_RSWAP_INDEX_OFFSET + dataOffset));
		} catch (Exception e)
		{
			throw new AccountingParseException(e.getMessage());
		}
		
		long maxRMem = Long.valueOf(values.get(MAX_RMEM_INDEX_OFFSET + dataOffset));
//		if( maxRMem < 0L){
//			System.out.println(values.get(MAX_RMEM_INDEX_OFFSET + dataOffset));
//		}
		assert(maxRMem > 0L);
		long maxRSwap = Long.valueOf(values.get(MAX_RSWAP_INDEX_OFFSET + dataOffset));
		assert(maxRSwap > 0L);
		int num_processors = Integer.valueOf(values.get(NUM_PROCESSORS_INDEX));
		Date submitTime = getDate(values,SUBMIT_TIME_INDEX);
		Date startTime = getDate(values,START_TIME_INDEX);
		
		double ru_time = Double.valueOf(values.get(RU_UTIME_INDEX_OFFSET + dataOffset));
		double rs_time = Double.valueOf(values.get(RU_STIME_INDEX_OFFSET + dataOffset));
		
		int maxNumProcessors = Integer.valueOf(values.get(MAXNUM_PROCESSORS_INDEX_OFFSET + dataOffset));
		
		
		// add the properties
		
		if(eventTime != null)
		{
			r.setProperty(LSF_LOGDATE_PROP, eventTime);
		}
		
		if(isDefined(localUserId))
		{
			r.setProperty(LSF_USERNAME_PROP, localUserId);
		}
		if( isDefined(projectName)){
			r.setProperty(LSF_PROJECT_PROP, projectName);
		}
		if( isDefined(num_processors)){
			r.setProperty(LSF_NUMPROCESSORS_PROP, num_processors);
		}
		if (isDefined(jobStatus) )
		{
			r.setProperty(LSF_JOBEXIT_PROP, jobStatus);
			r.setProperty(BatchParser.SUCCESS_PROP, jobStatus == 64);
			
		} else
		{
			r.setProperty(BatchParser.SUCCESS_PROP, false);
		}
		
		if ( isDefined(maxRMem))
		{
			r.setProperty(LSF_MEMORYUSED_PROP, maxRMem);
		}
		
		if ( isDefined(maxRSwap))
		{
			r.setProperty(LSF_VIRTUALMEMORYUSED_PROP, maxRSwap);
		}
		
		if ( submitTime != null)
		{
			r.setProperty(LSF_SUBMITTED_PROP, submitTime);
		}
		
		if ( startTime != null)
		{
			r.setProperty(LSF_STARTED_PROP, startTime);
		}
		
		Duration wallDuration=null;
		if ( eventTime != null  && startTime != null)
		{
			wallDuration = new Duration(startTime, eventTime);
			r.setProperty(LSF_WALLCLOCK_PROP, wallDuration.getSeconds());
		}
		
		
		
		
		r.setProperty(LSF_CPU_TIME_PROP, ru_time+rs_time);
		
		
		if ( isDefined(maxNumProcessors))
		{
			r.setProperty(LSF_MAXPROCESSORS_PROP, maxNumProcessors);
		}
		
		
		if ( isDefined(queue))
		{
			r.setProperty(LSF_QUEUE_PROP, queue);
		}
		
		if( isDefined(jobName))
		{
			r.setProperty(LSF_JOBNAME_PROP, jobName);
		}
		if( isDefined(batchCommand))
		{
			r.setProperty(LSF_BATCH_COMMAND_PROP, batchCommand);
		}
		
		if ( isDefined(jobID))
		{
			r.setProperty(LSF_JOBID_PROP, jobID);
		}
		if( isDefined(arrayID)){
			r.setProperty(LSF_JOB_ARRAY_INDEX, arrayID);
		}
		
		// add list of exec hosts if present
		if ( numExecHosts > 0)
		{
			
			r.setProperty(LSF_NUM_EXECHOSTS_PROP, numExecHosts);
			
			// extract the list of hosts and build a colon-seperated list
			
			StringBuilder buffer = new StringBuilder();
			
			for(int i=0; i<numExecHosts; i++)
			{
				buffer.append(values.get(indexOfExecHosts+1+i) + ":");
			}
			
			r.setProperty(LSF_EXEC_HOSTS_PROP, buffer.toString());
		}
		
		return true;
	}

	@Override
	public void startParse(PropertyContainer defaults) throws DataException,
	InvalidPropertyException {
	}

	

	public AppContext getContext() {
		return c;
	}

	@Override
	public PropertyFinder initFinder(AppContext conn, PropertyFinder prev, String table){
		MultiFinder finder=new MultiFinder();
		finder.addFinder(StandardProperties.time);
		finder.addFinder(StandardProperties.base);
		finder.addFinder(batch);
		finder.addFinder(lsf);
		return finder;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.parsers.BatchParser#getDerivedProperties()
	 */
	@Override
	public PropExpressionMap getDerivedProperties(PropExpressionMap previous) {
		PropExpressionMap result = super.getDerivedProperties(previous);
		try {
			
			result.put(LSF_JOBID_PROP,new IntPropExpression<String>( JOB_ID_PROP));
			result.put(JOB_ID_PROP, new StringPropExpression<Integer>(LSF_JOBID_PROP));
			addAlias(result,LSF_QUEUE_PROP,QUEUE_PROP);
			addAlias(result, LSF_USERNAME_PROP, StandardProperties.USERNAME_PROP);
			addAlias(result, LSF_JOBNAME_PROP, BatchParser.JOB_NAME_PROP);
			addAlias(result, LSF_SUBMITTED_PROP, BatchParser.SUBMITTED_PROP);
			addAlias(result,LSF_STARTED_PROP,StandardProperties.STARTED_PROP);
			addAlias(result, LSF_LOGDATE_PROP, StandardProperties.ENDED_PROP);
			addAlias(result, LSF_WALLCLOCK_PROP, BatchParser.WALLCLOCK_PROP);
			addAlias(result, LSF_CPU_TIME_PROP,StandardProperties.CPU_TIME_PROP);
			addAlias(result, LSF_MAXPROCESSORS_PROP, BatchParser.REQUESTED_CPUS_PROP);
			addAlias(result,LSF_NUMPROCESSORS_PROP,BatchParser.PROC_COUNT_PROP);
			addAlias(result, LSF_NUM_EXECHOSTS_PROP, BatchParser.NODE_COUNT_PROP);
			addAlias(result, LSF_PROJECT_PROP, BatchParser.ACCOUNT_PROP);
			addAlias(result,LSF_BATCH_COMMAND_PROP,BatchParser.BATCH_SCRIPT_PROP);
			
		} catch (PropertyCastException e) {
			throw new ConsistencyError("Error generated derived properties",e);
		}
		return result;
	}

	private <T> void addAlias(PropExpressionMap result,PropertyTag<T> a, PropertyTag<T> b)
	throws PropertyCastException {
		result.put(a,b);
		result.put(b,a);
	}
	

}