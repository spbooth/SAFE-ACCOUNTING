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

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ed.epcc.safe.accounting.ErrorSet;
import uk.ac.ed.epcc.safe.accounting.expr.BinaryPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.MilliSecondDatePropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropertyCastException;
import uk.ac.ed.epcc.safe.accounting.parsers.value.IntegerParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.PbsDateParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.StringParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.TimestampParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParseException;
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
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
import uk.ac.ed.epcc.webapp.jdbc.expr.Operator;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;

/**
 * <p>
 * Abstract Parser for variants of the PBS (Portable Batch System) records. 
 * 
 * PBS uses a meta-format which is extensible using name-value pairs of attributes.
 * these attributes can in turn have complex structure that may generate multiple
 * properties so each attribute is processed by a {@link ContainerEntryMaker}.
 *
 * This class defines the subset of common properties that are common to most PBS versions but will need
 * to be extended to parse a usable set of properties.
 *
 * <h5>Record Types Property</h5>
 * <p>
 * An instance of this parser will only parse certain record types. For all
 * other record types, the parse will throw a <code>SkipRecord</code> exception.
 * Which types are parsed and which are skipped is determined by the
 * <em>mode</em> of this parser. The mode takes the form of a comma separated
 * list of record types the parser can parse. This is set as a property in the
 * <code>ConfigService</code> stored in the <code>AppContext</code> used to
 * construct the <code>PbsParser</code>. During initialisation, a
 * <code>PbsParser</code> looks for the following property:
 * </p>
 * <blockquote> pbs.recordTypes.<em>table_name</em> =
 * <em>comma,separated,type,list</em></blockquote>
 * <p>
 * for example
 * </p>
 * <blockquote>pbs.recordTypes.PBSReservation = B </blockquote>
 * <p>
 * All record types in the list will be parsed. All other record types will be
 * skipped. If the property isn't found, record type <em>E</em> is assumed.
 * </p>
 * 
 * @author jgreen4, adrianj, spb
 * 
 */


public abstract class AbstractPbsParser extends BatchParser implements Contexed{
	private static final PBSIdStringEntryMaker pbs_id_maker = new PBSIdStringEntryMaker();
	private AppContext context;
	private String mode;

	/**
	 * Errors reported in this <code>ErrorSet</code> will be returned by the
	 * {@link #endParse()} method
	 */
	private ErrorSet errors;
	/**
	 * Errors reported in this <code>ErrorSet</code> will be reported as
	 * <em>info</em> entries in the logger.
	 */
	private ErrorSet info;
	/**
	 * Errors reported in this <code>ErrorSet</code> will be reported as
	 * <em>warning</em> entries in the logger.
	 */
	private ErrorSet warnings;

	/** Should failed jobs be recorded.
	 * 
	 */
	private boolean record_failed_jobs;
	/**
	 * The record types this parser will parse. All other record types will be
	 * skipped when they are encountered
	 */
	private Collection<String> recordTypes;

	/** Do jobs run in a reservation count as full jobs or should they be marked as a
	 * sub-job
	 * 
	 */
	private boolean charge_reservation_sub_job=false;
	
	static final String PBS_PROPERTY_BASE = "pbs.";
	static final String PBS_RECORD_TYPE_PROPERTY_BASE = PBS_PROPERTY_BASE
	+ "recordTypes.";

	static final Pattern ATTR_PATTERN=Pattern.compile("([\\w_\\.]+)=(([^\\s\"]+)|\"([^\"]*)\")");
	/*
	 * ##########################################################################
	 * PBS REGISTRIES AND PROPERTY TAGS
	 * ##########################################################################
	 */

	private static final String DEFAULT_REGISTRY_NAME = "pbs";
	private static final PropertyRegistry PBS_REGISTRY = new PropertyRegistry(
			DEFAULT_REGISTRY_NAME,"Properties generated by the PBS parser");

	// //////////////////////////////////////////////////////////////////////////
	// PROPERTY TAGS COPPIED FROM BaseParser
	// //////////////////////////////////////////////////////////////////////////

	// maps to BaseParser.ENDED_PROP
	@AutoTable(unique=true)
	public static final PropertyTag<Date> PBS_ENDED_PROP = new PropertyTag<Date>(
			PBS_REGISTRY, "end", Date.class,"Time when the job ended execution");

	// maps to BaseParser.EXIT_PROP
	@AutoTable
	public static final PropertyTag<Integer> PBS_EXIT_PROP = new PropertyTag<Integer>(
			PBS_REGISTRY, "Exit_status", Integer.class,
	"Numerical exit status of job");

	// maps to BaseParser.GROUPNAME_PROP
	@AutoTable(length=128)
	public static final PropertyTag<String> PBS_GROUP_PROP = new PropertyTag<String>(
			PBS_REGISTRY, "group",String.class,
	"The name of the group under which the job executed");

	// maps to BaseParser.STARTED_PROP
	@AutoTable
	public static final PropertyTag<Date> PBS_STARTED_PROP = new PropertyTag<Date>(
			PBS_REGISTRY, "start", Date.class,"Time when job execution started");

	// maps to BaseParser.USERNAME_PROP
	@AutoTable
	public static final PropertyTag<String> PBS_USER_PROP = new PropertyTag<String>(
			PBS_REGISTRY, "user",String.class,
	"The name of the user under which the job executed");

	// //////////////////////////////////////////////////////////////////////////
	// PROPERTY TAGS COPPIED FROM BatchParser
	// //////////////////////////////////////////////////////////////////////////

	// maps to BatchParser.ACCOUNT_PROP
	@AutoTable(length=128)
	public static final PropertyTag<String> PBS_ACCOUNT_PROP = new PropertyTag<String>(
			PBS_REGISTRY, "account",String.class,
	"If submitter supplied a string to be recorded in accounting");

	// maps to BatchParser.JOB_ID_PROP
	@AutoTable(unique=true,length=128)
	public static final PropertyTag<String> PBS_ID_STRING_PROP = new PropertyTag<String>(
			PBS_REGISTRY,
			"id_string",String.class,
	"Identifies the job, reservation or reservation-job identifier.  This is not the job's name");

	// maps to BatchParser.JOB_NAME_PROP
	@AutoTable(length=128)
	public static final PropertyTag<String> PBS_JOB_NAME_PROP = new PropertyTag<String>(
			PBS_REGISTRY, "jobname", String.class,"The name of the job");

	// maps to BatchParser.QUEUE_PROP
	@AutoTable
	public static final PropertyTag<String> PBS_QUEUE_PROP = new PropertyTag<String>(
			PBS_REGISTRY, "queue", String.class,"The name of the queue in which the job executed");

	// maps to BatchParser.SUBMITTED_PROP
	@AutoTable
	public static final PropertyTag<Date> PBS_SUBMITTED_PROP = new PropertyTag<Date>(
			PBS_REGISTRY, "ctime", Date.class,"Time when the job was first submitted");

	// //////////////////////////////////////////////////////////////////////////
	// PROPERTY TAGS SPECIFIC TO PBS RECORDS
	// //////////////////////////////////////////////////////////////////////////
	@AutoTable	
	public static final PropertyTag<String> PBS_ACCOUNTING_ID_PROP = new PropertyTag<String>(
			PBS_REGISTRY,
			"accounting_id",String.class,
	"Accounting identifier associated with system-generated accounting data (CSA JID, job container ID)");
	@OptionalTable
	public static final PropertyTag<String> PBS_ALTERNATE_ID_PROP = new PropertyTag<String>(
			PBS_REGISTRY, "alt_id",String.class, "An optional alternate job identifier");
	@OptionalTable(length=512)
	public static final PropertyTag<String> PBS_AUTHORIZED_USERS_PROP = new PropertyTag<String>(
			PBS_REGISTRY, "authorized_users",String.class,
	"List of authorised acl users of the queue that services the reservation");
	@OptionalTable(length=512)
	public static final PropertyTag<String> PBS_AUTHORIZED_GROUPS_PROP = new PropertyTag<String>(
			PBS_REGISTRY, "authorized_groups",String.class,
	"List of authorised acl groups of the queue that services the reservation");
	@OptionalTable(length=1024)
	public static final PropertyTag<String> PBS_AUTHORIZED_HOSTS_PROP = new PropertyTag<String>(
			PBS_REGISTRY, "authorized_hosts",String.class,
	"List of authorised acl hosts of the queue that services the reservation");
	@OptionalTable
	public static final PropertyTag<Date> PBS_ELIGIBLE_TIME_PROP = new PropertyTag<Date>(
			PBS_REGISTRY, "etime", Date.class,"The time the job became eligible to run");
	@OptionalTable
	public static final PropertyTag<Date> PBS_ENTERED_QUEUE_PROP = new PropertyTag<Date>(
			PBS_REGISTRY, "qtime",Date.class,
	"Time when the job was queued into the current queue");
	@OptionalTable(length=1024)
	public static final PropertyTag<String> PBS_EXEC_HOST_PROP = new PropertyTag<String>(
			PBS_REGISTRY,
			"exec_host",String.class,
	"A list of vnodes (delimiter = plus sign (+)) with the resources used in them.  Format varies depending on the version of PBS used");
	@OptionalTable
	public static final PropertyTag<Date> PBS_JOB_TIMESTAMP_PROP = new PropertyTag<Date>
	(
			PBS_REGISTRY, "date_time",Date.class,
	"Time supplied at the beginning of the record (first argument)");

	@OptionalTable(length=128)
	public static final PropertyTag<String> PBS_OWNER_PROP = new PropertyTag<String>(
			PBS_REGISTRY, "owner",String.class,
	"The name of the party who submitted the resource reservation request");
	@OptionalTable(length=1)
	public static final PropertyTag<String> PBS_RECORD_TYPE_PROP = new PropertyTag<String>(
			PBS_REGISTRY, "record_type",String.class,
	"Single character indicating the type of record");
	@OptionalTable(target=Long.class)
	public static final PropertyTag<Number> PBS_RESERVATION_DURATION_PROP = new PropertyTag<Number>(
			PBS_REGISTRY, "duration", Number.class,
	"The duration specified or computed for the resource reservation (in seconds)");
	@OptionalTable
	public static final PropertyTag<String> PBS_RESERVATION_NAME_PROP = new PropertyTag<String>(
			PBS_REGISTRY, "resvname", String.class,"The name of the reservation period");

	// Not sure if this is the same as PBS_RESERVATION_NAME - used in a different
	// record type but may mean the same thing

	public static final PropertyTag<String> PBS_RESERVATION_ID_PROP = new PropertyTag<String>(
			PBS_REGISTRY, "resvID", String.class,"The id of the reservation period");
	@OptionalTable
	public static final PropertyTag<Number> PBS_SESSION_PROP = new PropertyTag<Number>(
			PBS_REGISTRY, "session", Number.class, "The session number of the job");

    @AutoTable
	public static final PropertyTag<Number> PBS_TIME_USED_PROP = new PropertyTag<Number>(
			PBS_REGISTRY, "runtime", Number.class, "The runtime of the job");

    @OptionalTable
    public static final PropertyTag<Integer> PBS_SEQUENCE_PROP = new PropertyTag<Integer>(PBS_REGISTRY, "sequence", Integer.class,"Numerical PBS sequence number");
    @OptionalTable
	public static final PropertyTag<Integer> PBS_ARRAY_INDEX_PROP = new PropertyTag<Integer>(PBS_REGISTRY, "array_index", Integer.class,"Numerical PBS array index");
	@AutoTable
	public static final PropertyTag<Boolean> PBS_IS_ARRAY_PROP = new PropertyTag<Boolean>(PBS_REGISTRY, "is_array", Boolean.class,"Is this an array job");
	
	// //////////////////////////////////////////////////////////////////////////
	// Standard attributes
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * All attributes declared in the PBS manuals
	 */
	private static final MakerMap STANDARD_ATTRIBUTES = new MakerMap();
	static {
		/* The comments list the record types the properties are contained in */
		// S
		STANDARD_ATTRIBUTES.addParser(PBS_ACCOUNTING_ID_PROP,StringParser.PARSER);
		// B, E
		STANDARD_ATTRIBUTES.addParser(PBS_ACCOUNT_PROP,StringParser.PARSER);
		// E
		STANDARD_ATTRIBUTES.addParser(PBS_ALTERNATE_ID_PROP,StringParser.PARSER);
		// B
		STANDARD_ATTRIBUTES.addParser(PBS_AUTHORIZED_GROUPS_PROP,StringParser.PARSER);
		// B
		STANDARD_ATTRIBUTES.addParser(PBS_AUTHORIZED_HOSTS_PROP,StringParser.PARSER);
		// B
		STANDARD_ATTRIBUTES.addParser(PBS_AUTHORIZED_USERS_PROP,StringParser.PARSER);
		// E, S
		STANDARD_ATTRIBUTES.addParser(PBS_ELIGIBLE_TIME_PROP,TimestampParser.PARSER);
		// B, E
		STANDARD_ATTRIBUTES.addParser(PBS_ENDED_PROP,TimestampParser.PARSER);
		// E, S
		STANDARD_ATTRIBUTES.addParser(PBS_ENTERED_QUEUE_PROP,TimestampParser.PARSER);
		// B, S
		STANDARD_ATTRIBUTES.addParser(PBS_EXEC_HOST_PROP,StringParser.PARSER);
		// E
		STANDARD_ATTRIBUTES.addParser(PBS_EXIT_PROP,IntegerParser.PARSER);
		// E, S
		STANDARD_ATTRIBUTES.addParser(PBS_GROUP_PROP,StringParser.PARSER);
		// All record types
		// Actually this is parsed directly not via the MakerMap see parse method
		STANDARD_ATTRIBUTES.put(PBS_ID_STRING_PROP.getName(),pbs_id_maker);
		// E, S
		STANDARD_ATTRIBUTES.addParser(PBS_JOB_NAME_PROP,StringParser.PARSER);
		// All record types
		STANDARD_ATTRIBUTES.addParser(PBS_JOB_TIMESTAMP_PROP,PbsDateParser.PARSER);
		// B
		STANDARD_ATTRIBUTES.addParser(PBS_OWNER_PROP,StringParser.PARSER);
		// B, E, Q, S
		STANDARD_ATTRIBUTES.addParser(PBS_QUEUE_PROP,StringParser.PARSER);
		// All record types
		STANDARD_ATTRIBUTES.addParser(PBS_RECORD_TYPE_PROP,StringParser.PARSER);
		// B
		STANDARD_ATTRIBUTES.addParser(PBS_RESERVATION_DURATION_PROP,IntegerParser.PARSER);
		// B, E
		STANDARD_ATTRIBUTES.addParser(PBS_RESERVATION_ID_PROP,StringParser.PARSER);
		// B, E
		STANDARD_ATTRIBUTES.addParser(PBS_RESERVATION_NAME_PROP,StringParser.PARSER);
		// S
		STANDARD_ATTRIBUTES.addParser(PBS_SESSION_PROP,IntegerParser.PARSER);
		// B, E, S
		STANDARD_ATTRIBUTES.addParser(PBS_STARTED_PROP,TimestampParser.PARSER);
		// B, E, S
		STANDARD_ATTRIBUTES.addParser(PBS_SUBMITTED_PROP,TimestampParser.PARSER);
		// E, S
		STANDARD_ATTRIBUTES.addParser(PBS_USER_PROP,StringParser.PARSER);

	}

	
	// //////////////////////////////////////////////////////////////////////////
	// Derived Properties
	// //////////////////////////////////////////////////////////////////////////

	/*
	 * This is a static declaration to save it having to be constructed each time.
	 * In the future, it may be more appropriate to determine derivations
	 * dynamically in the getDerivedProperties method but until then, derivations
	 * are done in a static block for efficiency.
	 */
	private static PropExpressionMap DERIVATIONS = new PropExpressionMap();
	static {
		try {
			// Base Parser derivations
			DERIVATIONS.peer(StandardProperties.ENDED_PROP, PBS_ENDED_PROP);
			DERIVATIONS.peer(StandardProperties.EXIT_PROP, PBS_EXIT_PROP);
			DERIVATIONS.peer(StandardProperties.GROUPNAME_PROP, PBS_GROUP_PROP);
			DERIVATIONS.peer(StandardProperties.STARTED_PROP, PBS_STARTED_PROP);
			DERIVATIONS.peer(StandardProperties.USERNAME_PROP, PBS_USER_PROP);

			// Batch parser derivations
			DERIVATIONS.peer(BatchParser.ACCOUNT_PROP, PBS_ACCOUNT_PROP);
			DERIVATIONS.peer(BatchParser.JOB_NAME_PROP, PBS_JOB_NAME_PROP);
			DERIVATIONS.peer(BatchParser.QUEUE_PROP, PBS_QUEUE_PROP);
			DERIVATIONS.peer(BatchParser.SUBMITTED_PROP, PBS_SUBMITTED_PROP);
			DERIVATIONS.peer(BatchParser.JOB_ID_PROP, PBS_ID_STRING_PROP);
		} catch (PropertyCastException e) {
			throw new ConsistencyError(
					"Unable to construct default derivations for PBS parsers.  "
					+ "Class cannot be loaded", e);
		}
	}
	static{
		PBS_REGISTRY.lock();
	}
	
	/** class that attempts to extract a PBS sequence number and array-id 
	 * from the id_string. The unprocessed string is also parses
	 * 
	 * @author spb
	 *
	 */
	public static class PBSIdStringEntryMaker implements ContainerEntryMaker{
		public static final Pattern array_patt = Pattern.compile("(\\d+)\\[(\\d+)\\]\\..*");
		@Override
		public void setValue(PropertyContainer contanier, String valueString)
				throws IllegalArgumentException, InvalidPropertyException,
				NullPointerException, AccountingParseException {
			contanier.setProperty(PBS_ID_STRING_PROP, valueString);
			Matcher m = array_patt.matcher(valueString);
			if( m.matches()){
				contanier.setProperty(PBS_SEQUENCE_PROP, Integer.parseInt(m.group(1)));
				contanier.setProperty(PBS_ARRAY_INDEX_PROP, Integer.parseInt(m.group(2)));
				contanier.setProperty(PBS_IS_ARRAY_PROP, true);
			}else{
				contanier.setProperty(PBS_IS_ARRAY_PROP, false);
			}
		}

		@Override
		public void setValue(PropertyMap map, String valueString)
				throws IllegalArgumentException, NullPointerException,
				AccountingParseException {
			map.setProperty(PBS_ID_STRING_PROP, valueString);
			Matcher m = array_patt.matcher(valueString);
			if( m.matches()){
				map.setProperty(PBS_SEQUENCE_PROP, Integer.parseInt(m.group(1)));
				map.setProperty(PBS_ARRAY_INDEX_PROP, Integer.parseInt(m.group(2)));
				map.setProperty(PBS_IS_ARRAY_PROP, true);
			}else{
				map.setProperty(PBS_IS_ARRAY_PROP, false);
			}
			
		}
		
	}
	/*
	 * ##########################################################################
	 * CONSTRUCTORS & IMPLEMENTED METHODS
	 * ##########################################################################
	 */

	/**
	 * Constructs a new <code>PbsParser</code>. The parser should not be used
	 * until it's {@link #initFinder(AppContext, PropertyFinder, String)} method
	 * has been called.
	 * 
	 * @param context
	 *          The context in which this parser is operating
	 */
	public AbstractPbsParser(AppContext context) {
		if (context == null)
			throw new NullPointerException("AppContext cannot be null");

		this.context = context;
	}

	

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ed.epcc.webapp.Contexed#getContext()
	 */
	public AppContext getContext() {
		return context;
	}

	protected Logger getLogger(){
		return getContext().getService(LoggerService.class).getLogger(getClass());
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ed.epcc.safe.accounting.BaseParser#endParse()
	 */
	@Override
	public String endParse() {
		LoggerService loggerService = this.context.getService(LoggerService.class);
		Logger logger = loggerService.getLogger(AbstractPbsParser.class);

		/*
		 * Info and warnings are sent to the logger. Errors are returned - they are
		 * more important and so are passed back to be shown directly to the caller
		 * of the parse
		 */
		String info = this.info.toString();
		if (info.length() > 0)
			logger.info(info);

		String warnings = this.warnings.toString();
		if (warnings.length() > 0)
			logger.warn(warnings);

		return this.errors.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ed.epcc.safe.accounting.BatchParser#getDerivedProperties()
	 */
	@Override
	public PropExpressionMap getDerivedProperties(PropExpressionMap previous) {
		PropExpressionMap derivedProps = super.getDerivedProperties(previous);

		try{
			derivedProps.put(PBS_TIME_USED_PROP,
					new BinaryPropExpression(new MilliSecondDatePropExpression(PBS_ENDED_PROP),
							Operator.SUB,
							new MilliSecondDatePropExpression(PBS_STARTED_PROP))
			);
		}catch(PropertyCastException e){
			throw new ConsistencyError("cast check failed for pbs parser property expression used to get the runtime ",e);
		} 

		derivedProps.getAllFrom(DERIVATIONS);

		return derivedProps;
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.epcc.safe.accounting.BaseParser#getFinder(uk.ac.ed.epcc.webapp
	 * .AppContext, uk.ac.ed.epcc.safe.accounting.PropertyFinder,
	 * java.lang.String)
	 */
	@Override
	public PropertyFinder initFinder(AppContext ctx, PropertyFinder prev,
			String mode) {
		this.context = ctx;
		this.mode=mode;
		// Note the contents of the registry depend on the mode so the name should as well
		
		String recordTypeList = ctx.getInitParameter(PBS_RECORD_TYPE_PROPERTY_BASE + mode);
		recordTypes = new HashSet<String>();
		if (recordTypeList == null || mode == null) {
			this.recordTypes.add("E");
		} else {
			for (String type : recordTypeList.split(",")) {
				try {
					this.recordTypes.add(type);
				} catch (IllegalArgumentException e) {
					getLogger().error("Unable to identify record type '" + type
							+ "'.  It is not a recognised PBS record type");
				}
			}
		}
		this.charge_reservation_sub_job = ctx.getBooleanParameter("charge_reservation_sub_job."+mode, true);
		this.record_failed_jobs = ctx.getBooleanParameter("record_failed_jobs."+mode, false);
		MultiFinder finder = new MultiFinder();
		finder.addFinder(StandardProperties.time);
		finder.addFinder(StandardProperties.base);
		finder.addFinder(BatchParser.batch);
		finder.addFinder(PBS_REGISTRY);
		return finder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.epcc.safe.accounting.BaseParser#startParse(uk.ac.ed.epcc.safe.
	 * accounting.PropertyContainer)
	 */
	@Override
	public void startParse(PropertyContainer defaults) {
		this.errors = new ErrorSet();
		this.info = new ErrorSet();
		this.warnings = new ErrorSet();
	}

	

	protected ContainerEntryMaker getEntryMaker(String attr){
		return STANDARD_ATTRIBUTES.get(attr);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ed.epcc.safe.accounting.UsageRecordParser#parse(uk.ac.ed.epcc.safe
	 * .accounting.PropertyMap, java.lang.String)
	 */
	public boolean parse(PropertyMap pbsMap, String record)
	throws AccountingParseException {
		String args[] = record.split(";");
		if (args.length < 3)
			throw new AccountingParseException("Too few fields: " + args.length);
	
		/*
		 * First, extract the standard PBS information
		 */
		Date stamp;
		String recordTypeCode;
		String job_id;
		try {
			recordTypeCode = args[1];
	
			// Make sure we should parse records of this type
			if (this.recordTypes.contains(recordTypeCode) == false && this.recordTypes.contains((String)recordTypeCode) == false) {
				throw new SkipRecord("Not parsing records of type " + recordTypeCode);
			}
	
			stamp = PbsDateParser.PARSER.parse(args[0]);
			job_id = args[2];
	
		} catch (ValueParseException e) {
			this.errors.add("Unable to parse values of mandatory initial fields",
					record, e);
			return false;
		}
	
		pbsMap.setProperty(PBS_JOB_TIMESTAMP_PROP, stamp);
		pbsMap.setProperty(PBS_RECORD_TYPE_PROP, recordTypeCode);
		pbs_id_maker.setValue(pbsMap, job_id);
	
		// Use the fetched record type to parse the rest of the record
//		StringTokenizer st = new StringTokenizer(args[3]);
//		while (st.hasMoreElements()) {
//			String attribute = st.nextToken();
//			int assignmentLocation = attribute.indexOf("=");
//			if (assignmentLocation < 0) {
//				String errorMessage = "Bad attribute string '" + attribute
//				+ "': it doesn't have an '=' sign for assigning a value to it.  "
//				+ "Ignoring the attribute.";
//				this.errors.add(errorMessage, record);
//				continue;
//			}
//	
//			String attrName = attribute.substring(0, assignmentLocation);
//			String attrValue = attribute.substring(assignmentLocation + 1);
	    Matcher m = ATTR_PATTERN.matcher(args[3]);
	    while(m.find()){
	    	String attrName=m.group(1);
	    	String attrValue=m.group(3);
	    	if( attrValue == null ){
	    		attrValue=m.group(4);
	    	}
			ContainerEntryMaker maker = getEntryMaker(attrName);
			
	
			if (maker == null) {
				String errorMessage = "unrecognised attribute '" + attrName + "'";
				this.warnings.add(errorMessage, record);
			} else {
				try {
					maker.setValue(pbsMap, attrValue);
				} catch (IllegalArgumentException e) {
					this.errors.add("Problem with attribute '" + attrValue
							+ "': Unable to parse value '" + attrValue + "'", record, e);
				}
			}
		}
	
		// This code address the issue of jobs that report their 
		// end time as being before their start time, or there 
		// submitted or eligible times are after there start time (usually because 
		// the clocks of the machines that record the different properties 
		// have drifted).  Here we just set the end time to the start time 
		// if this situation exists so the jobs will be counted by have 
		// zero runtime, and we set submitted and/or eligible to start to 
		// so they effectively have zero wait time as well.
		// AdrianJ 2010
		Date ended = pbsMap.getProperty(PBS_ENDED_PROP);
		Date started = pbsMap.getProperty(PBS_STARTED_PROP);
		Date ctime = pbsMap.getProperty(PBS_SUBMITTED_PROP);
		Date etime = pbsMap.getProperty(PBS_ELIGIBLE_TIME_PROP);
		if(started != null){
			if( started.getTime() == 0L){
				// This is the completion record for a set of array jobs
				// the array jobs have already been charged.
				throw new SkipRecord("StartTime zero (array completion record)");
			}
			if(ended != null){
				if(ended.getTime() < started.getTime()){
					pbsMap.setProperty(PBS_ENDED_PROP, started);
				}
			}else{
				throw new SkipRecord("No end time");
			}
			if(ctime != null){
				if(ctime.getTime() > started.getTime()){
					pbsMap.setProperty(PBS_SUBMITTED_PROP, started);
				}
			}
			if(etime != null){
				if(etime.getTime() > started.getTime()){
					pbsMap.setProperty(PBS_ELIGIBLE_TIME_PROP, started);
				}
			}
		}else{
			throw new SkipRecord("No start time");
		}
	
		// mark jobs from a reservation as sub-jobs.
		if( ! charge_reservation_sub_job && pbsMap.getProperty(PBS_RESERVATION_ID_PROP) != null){
			pbsMap.setProperty(BatchParser.SUBJOB_PROP, Boolean.TRUE);
		}
		Number exit = pbsMap.getProperty(PBS_EXIT_PROP, null);
		if( exit != null ){
			boolean success = exit.intValue() >= 0;
			pbsMap.setProperty(BatchParser.SUCCESS_PROP, success);
			if( ! success && ! record_failed_jobs){
				throw new SkipRecord("Job failed "+exit);
			}
		}
	
	
		return true;
	}

	

	


}