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

import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.expr.DurationCastPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.DurationSecondsPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropertyCastException;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.update.AutoTable;
import uk.ac.ed.epcc.safe.accounting.update.BatchParser;
import uk.ac.ed.epcc.safe.accounting.update.OptionalTable;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
import uk.ac.ed.epcc.webapp.exceptions.InvalidArgument;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.model.data.Duration;
/**
 * <p>
 * Parser for OGF usage records. This parser is intended to parse documents 
 * based on document GFD.098
 * published by the Open Grid Forum (OGF). The document specifies an XML format
 * for OGF usage records. This parser parses such XML documents and converts
 * them into SAFE's internal format.
 * </p>
 * 
 * 
 * 
 */


public class OGFXMLRecordParser extends XMLRecordParser {
	/**
	 * Name of the registry used to store UR extensionProperties generated from
	 * the config extensionProperties
	 */
	public static final String DEFAULT_PROPERTY_REGISTRY_NAME = "ogfur";
	/**
	 * The property registry that holds all the default property tags used by UR
	 * extensionProperties.
	 */
	public static final PropertyRegistry OGFUR_DEFAULT_REGISTRY = new PropertyRegistry(
			DEFAULT_PROPERTY_REGISTRY_NAME,
			"The default property tags used by UR extension properties");

	// Record identity properties
	@Path("//ur:RecordIdentity")
    public static final PropertyTag<String> OGFUR_RECORD_IDENTITY_PROP = new PropertyTag<>(
			OGFUR_DEFAULT_REGISTRY, "RecordIdentity",String.class);
	@Path("//ur:RecordIdentity/@ur:createTime")
	@OptionalTable
	public static final PropertyTag<Date> OGFUR_CREATE_TIME_PROP = new PropertyTag<>(
			OGFUR_DEFAULT_REGISTRY, "createTime",Date.class);
	@Path("//ur:RecordIdentity/@ur:recordId")
	@AutoTable(unique=true,length=128) public static final PropertyTag<String> OGFUR_RECORD_ID_PROP = new PropertyTag<>(
			OGFUR_DEFAULT_REGISTRY, "recordId",String.class);

	// Job identity properties
	@Path("//ur:GlobalJobId")
	@AutoTable public static final PropertyTag<String> OGFUR_GLOBAL_JOB_ID_PROP = new PropertyTag<>(
			OGFUR_DEFAULT_REGISTRY, "GlobalJobId",String.class);
    @Path("//ur:LocalJobId")
	@AutoTable public static final PropertyTag<String> OGFUR_LOCAL_JOB_ID_PROP = new PropertyTag<>(
			OGFUR_DEFAULT_REGISTRY, "LocalJobId",String.class);
	@Path("//ur:ProcessId")
	@OptionalTable(length=256)
    public static final PropertyTag<String> OGFUR_PROCESS_ID_PROP = new PropertyTag<>(
			OGFUR_DEFAULT_REGISTRY, "ProcessId",String.class);

	// User Identity properties
	
	/** There seems to be some confusion in OGF documents about the capitalisation
	 * of this element as the document gives GlobalUsername but the xsd schema in
	 * common use uses GlobalUserName. Therefore this parser tries to recognise either
	 * but users will have to provide an alternative schema for the GlobalUsername form to validate.
	*/
    @Path("//ur:GlobalUserName|//ur:GlobalUsername")
	@AutoTable(length=512)
    public static final PropertyTag<String> OGFUR_GLOBAL_USERNAME_PROP = new PropertyTag<>(
			OGFUR_DEFAULT_REGISTRY, "GlobalUsername",String.class);
	
    @Path("//ur:LocalUserId")
    @AutoTable public static final PropertyTag<String> OGFUR_LOCAL_USER_ID_PROP = new PropertyTag<>(
			OGFUR_DEFAULT_REGISTRY, "LocalUserId",String.class);

	@Path("//ur:UserIdentity/xd:KeyInfo/xd:X509Data/xd:X509SubjectName")
	@AutoTable(length=512)
	public static final PropertyTag<String> OGFUR_SUBJECT_NAME_PROP = new PropertyTag<>(OGFUR_DEFAULT_REGISTRY, "X509SubjectName",String.class);
	// Base Properties
	@Path("//ur:Charge")
	@OptionalTable
	public static final PropertyTag<Number> OGFUR_CHARGE_PROP = new PropertyTag<>(
			OGFUR_DEFAULT_REGISTRY, "Charge", Number.class);
	@Path("//ur:Charge/@ur:unit")
	@OptionalTable
	public static final PropertyTag<String> OGFUR_CHARGE_UNIT_PROP = new PropertyTag<>(
			OGFUR_DEFAULT_REGISTRY, "Charge_unit",String.class);
	@Path("//ur:EndTime")
	@AutoTable(unique=true) public static final PropertyTag<Date> OGFUR_END_TIME_PROP = new PropertyTag<>(
			OGFUR_DEFAULT_REGISTRY, "EndTime",Date.class);
	@Path("//ur:Host")
	@OptionalTable(length=512)
	public static final PropertyTag<String> OGFUR_HOST_PROP = new PropertyTag<>(
			OGFUR_DEFAULT_REGISTRY, "Host",String.class);
	@Path("//ur:JobName")
	@AutoTable(length=128) public static final PropertyTag<String> OGFUR_JOB_NAME_PROP = new PropertyTag<>(
			OGFUR_DEFAULT_REGISTRY, "JobName",String.class);
    @Path("//ur:MachineName")
    @OptionalTable(length=128)
	public static final PropertyTag<String> OGFUR_MACHINE_NAME_PROP = new PropertyTag<>(
			OGFUR_DEFAULT_REGISTRY, "MachineName",String.class);
    @Path("//ur:ProjectName")
    @OptionalTable(length=128)
    public static final PropertyTag<String> OGFUR_PROJECT_NAME_PROP = new PropertyTag<>(
			OGFUR_DEFAULT_REGISTRY, "ProjectName",String.class);
    @Path("//ur:Queue")
    @AutoTable public static final PropertyTag<String> OGFUR_QUEUE_PROP = new PropertyTag<>(
			OGFUR_DEFAULT_REGISTRY, "Queue",String.class);
    @Path("//ur:StartTime")
    @AutoTable public static final PropertyTag<Date> OGFUR_START_TIME_PROP = new PropertyTag<>(
			OGFUR_DEFAULT_REGISTRY, "StartTime",Date.class);
    @Path("//ur:Status")
    @AutoTable public static final PropertyTag<String> OGFUR_STATUS_PROP = new PropertyTag<>(
			OGFUR_DEFAULT_REGISTRY, "Status",String.class);
    @Path("//ur:SubmitHost")
    @OptionalTable(length=128)
    public static final PropertyTag<String> OGFUR_SUBMIT_HOST_PROP = new PropertyTag<>(
			OGFUR_DEFAULT_REGISTRY, "SubmitHost",String.class);
	@Path("//ur:WallDuration")
	@OptionalTable(target=Long.class)
    public static final PropertyTag<Duration> OGFUR_WALL_DURATION_PROP = new PropertyTag<>(
			OGFUR_DEFAULT_REGISTRY, "WallDuration", Duration.class);

	// Differentiated Properties
    @Path("//ur:CpuDuration")
    @OptionalTable(target=Long.class)
	@AutoTable public static final PropertyTag<Duration> OGFUR_CPU_DURATION_PROP = new PropertyTag<>(
			OGFUR_DEFAULT_REGISTRY, "CpuDuration", Duration.class);
//   @Path("//ur:Disk[metric='total'")
//    @ParseClass(parser=IntervallicVolumeParser.class)
//    public static final PropertyTag<Number> OGFUR_DISK_PROP = new PropertyTag<Number>(
//			OGFUR_DEFAULT_REGISTRY, "Disk", Number.class);
//    @Path("//ur:Memory")
//    @ParseClass(parser=IntervallicVolumeParser.class)
//    public static final PropertyTag<Number> OGFUR_MEMORY_PROP = new PropertyTag<Number>(
//			OGFUR_DEFAULT_REGISTRY, "Memory", Number.class);
//    @Path("//ur:Network")
//    @ParseClass(parser=IntervallicVolumeParser.class)
//    public static final PropertyTag<Number> OGFUR_NETWORK_PROP = new PropertyTag<Number>(
//			OGFUR_DEFAULT_REGISTRY, "Network", Number.class);
    @Path("//ur:NodeCount")
    @AutoTable(target=Integer.class) 
	public static final PropertyTag<Integer> OGFUR_NODE_COUNT_PROP = new PropertyTag<>(
			OGFUR_DEFAULT_REGISTRY, "NodeCount", Integer.class);
    @Path("//ur:Processors")
    @AutoTable(target=Integer.class) 
	public static final PropertyTag<Integer> OGFUR_PROCESSORS_PROP = new PropertyTag<>(
			OGFUR_DEFAULT_REGISTRY, "Processors", Integer.class);
    @Path("//ur:ServiceLevel")
    @OptionalTable
	public static final PropertyTag<String> OGFUR_SERVICE_LEVEL_PROP = new PropertyTag<>(
			OGFUR_DEFAULT_REGISTRY, "ServiceLevel",String.class);
//    @Path("//ur:Swap")
//  @ParseClass(parser=IntervallicVolumeParser.class)
//  public static final PropertyTag<Number> OGFUR_SWAP_PROP = new PropertyTag<Number>(
//	OGFUR_DEFAULT_REGISTRY, "Swap", Number.class);
//	@Path("//ur:TimeDuration")
//	public static final PropertyTag<Duration> OGFUR_TIME_DURATION_PROP = new PropertyTag<Duration>(
//			OGFUR_DEFAULT_REGISTRY, "TimeDuration", Duration.class);
//	@Path("//ur:TimeInstant")
//	public static final PropertyTag<Date> OGFUR_TIME_INSTANT_PROP = new PropertyTag<Date>(
//			OGFUR_DEFAULT_REGISTRY, "TimeInstant");

    // Extension properties defined in the OGF-UR specification
//    @Path("Resource")
//	public static final PropertyTag<String> OGFUR_RESOURCE_PROP = new PropertyTag<String>(
//			OGFUR_DEFAULT_REGISTRY, "Resource");
//    @Path("ConsumableResource")
//    public static final PropertyTag<Number> OGFUR_CONSUMABLE_RESOURCE_PROP = new PropertyTag<Number>(
//			OGFUR_DEFAULT_REGISTRY, "ConsumableResource", Number.class);
//    @Path("PhaseResource")
//    public static final PropertyTag<Number> OGFUR_PHASE_RESOURCE_PROP = new PropertyTag<Number>(
//			OGFUR_DEFAULT_REGISTRY, "PhaseResource", Number.class);
//    @Path("VolumeResource")
//	public static final PropertyTag<Number> OGFUR_VOLUME_RESOURCE_PROP = new PropertyTag<Number>(
//			OGFUR_DEFAULT_REGISTRY, "VolumeResource", Number.class);
    @AutoTable(length=1024)
    public static final PropertyTag OGF_TEXT=StandardProperties.TEXT_PROP;
    static{
    	OGFUR_DEFAULT_REGISTRY.lock();
    }
	public OGFXMLRecordParser(AppContext context) {
		super(context);
	}

	

	/**
	 * Private method used to declare two <code>PropertyTag</code>s to be equal to
	 * each other. Each of the two tags specified is added to the map with one as
	 * an expression of the other.
	 * 
	 * @param <T>
	 *          The type of <code>PropertyTag</code> each tag is
	 * @param p1
	 *          The <code>PropertyTag</code> that is equivalent to <code>p2</code>
	 * @param p2
	 *          The <code>PropertyTag</code> that is equivalent to <code>p1</code>
	 * @throws PropertyCastException
	 */
	protected <T> void ADD_DERIVATON(PropExpressionMap map, PropertyTag<T> p1, PropertyTag<T> p2)
			{
		try{
			map.put(p1, p2);
			map.put(p2, p1);
		}catch(PropertyCastException t){
			getLogger().error("Error adding Derivations in OGFUsageRecordParser",t);
			throw new ConsistencyError("Bad derivation", t);
		}
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ed.epcc.safe.accounting.BatchParser#getDerivedProperties()
	 */
	@Override
	public PropExpressionMap getDerivedProperties(PropExpressionMap previous) {
		PropExpressionMap map = super.getDerivedProperties(previous);
			ADD_DERIVATON(map,StandardProperties.ENDED_PROP, OGFUR_END_TIME_PROP);
			ADD_DERIVATON(map,StandardProperties.MACHINE_NAME_PROP, OGFUR_MACHINE_NAME_PROP);
			ADD_DERIVATON(map,StandardProperties.STARTED_PROP, OGFUR_START_TIME_PROP);
			ADD_DERIVATON(map,StandardProperties.USERNAME_PROP, OGFUR_LOCAL_USER_ID_PROP);
			
			ADD_DERIVATON(map,StandardProperties.INSERTED_PROP, OGFUR_CREATE_TIME_PROP);

			ADD_DERIVATON(map,BatchParser.QUEUE_PROP, OGFUR_QUEUE_PROP);
			ADD_DERIVATON(map,BatchParser.JOB_NAME_PROP, OGFUR_JOB_NAME_PROP);
			ADD_DERIVATON(map,BatchParser.JOB_ID_PROP, OGFUR_LOCAL_JOB_ID_PROP);
			ADD_DERIVATON(map,BatchParser.NODE_COUNT_PROP, OGFUR_NODE_COUNT_PROP);
			ADD_DERIVATON(map,BatchParser.PROC_COUNT_PROP, OGFUR_PROCESSORS_PROP);
		
			//ADD_DERIVATON(map,BatchParser.WALLCLOCK_PROP, OGFUR_WALL_DURATION_PROP);
			try{
			map.put(OGFXMLRecordParser.OGFUR_WALL_DURATION_PROP, new DurationCastPropExpression<>(BatchParser.WALLCLOCK_PROP,1000L));
			map.put(BatchParser.WALLCLOCK_PROP, new DurationSecondsPropExpression(OGFXMLRecordParser.OGFUR_WALL_DURATION_PROP));
			}catch(PropertyCastException e){
				getLogger().error("Error adding Derivations in OGFUsageRecordParser",e);
				throw new ConsistencyError("Bad derivation", e);
			}
		return map;
	}
	@Override
	protected ParsernameSpaceContext makeNameSpaceContext() {

		ParsernameSpaceContext res = new ParsernameSpaceContext(getContext());
		res.addNamespace("ur","http://schema.ogf.org/urf/2003/09/urf");
		res.addNamespace("xd","http://www.w3.org/2000/09/xmldsig#");
		return res;
	}
	@Override
	protected PropertyFinder initFinder(PropertyFinder prev) {
		MultiFinder mf = new MultiFinder();
		mf.addFinder(super.initFinder(prev));
		mf.addFinder(OGFUR_DEFAULT_REGISTRY);
		return mf;
	}
	@Override
	protected String defaultSchemaName() {
		return "ur.xsd";
	}



	@Override
	public TableSpecification modifyDefaultTableSpecification(TableSpecification initial,PropExpressionMap map,String table_name) {
		
		TableSpecification spec = super.modifyDefaultTableSpecification(initial,map,table_name);
		if( spec != null){
		try {
			// Want an index on the EndTime The one in BaseParser won't
			// match our field names
			spec.new Index("EndIndex",false,OGFUR_END_TIME_PROP.getName());
		} catch (InvalidArgument e) {
			getLogger().error("Failed to make EndIndex",e);
		}
		}
		return spec;
	}



	@Override
	protected String[] getTargets() {
		return new String[]{"UsageRecord","JobUsageRecord","Usage"};
	}
	
}