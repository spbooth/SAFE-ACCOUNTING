
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

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropertyCastException;
import uk.ac.ed.epcc.safe.accounting.parsers.value.IntegerParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParserService;
import uk.ac.ed.epcc.safe.accounting.policy.DerivedPropertyPolicy;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.update.AccountingParseException;
import uk.ac.ed.epcc.safe.accounting.update.AutoTable;
import uk.ac.ed.epcc.safe.accounting.update.BatchParser;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.config.ConfigService;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;

/**
 * <p>
 * Parser for PBS (Portable Batch System) records. These records can have a
 * different format depending on their <em>record type</em> which is specified
 * in argument two of the record. The different attributes record types can have
 * are hard coded into the {@link PbsRecordType} object this parser uses to
 * parse records. Additionally, <em>extension</em> attributes may be specified
 * using properties set in the {@link ConfigService} stored in the
 * {@link AppContext} used to construct a <code>PbsParser</code>. Strictly
 * speaking, the specification only allows extensions of the form
 * <code>Resource_List.<em>resource_name</em></code> and
 * <code>resources_used.<em>resource_name</em></code>, however this parser will
 * allow extensions with any name.
 * </p>
 * <h5>Declaring New PBS Attributes (Extension Attributes)</h5>
 * <p>
 * The PBS specification allows extra extensions to be used in it's record
 * types. Most of these are of the form <em>Resource_List.<em>resource_name</em>
 * </em> and <em>resources_used.<em>resource_name</em></em>. To create new ones,
 * the property format is:
 * </p>
 * <blockquote> pbs.<em>table_name</em>.attribute_type.<em>attribute_name</em> =
 * <em>parser</em> </blockquote>
 * <p>
 * Where <em>table_name</em> is the name of the database table in which the PBS
 * records are being stored, <em>attribute_name</em> is the name of the
 * attribute to parse (the name that will appear in the PBS record) and
 * <em>parser</em> is the parser to use to parse the value. There are several
 * parsers already defined that can be used to parse values. It is also possible
 * to write custom parsers and use them too. See
 * {@link uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParserService
 * ValueParserService} for more information on what parsers are provided by
 * default, as well as how to add custom parsers.
 * </p>
 * <p>
 * <b>Note:</b> Some pbs attribute extensions allow dots in their name
 * (Resource_List.<em>resource_name</em>). This parser will accept attribute
 * names with dots in them but change them to underscores in the accounting
 * properties. For example, the configuration property
 * <code>pbs.PBSTable.attribute_type.resources_used.mem</code> will parse an
 * attribute called <code>resources_used.mem</code> from a PBS record, but will
 * store the value in an accounting property called
 * <code>resources_used_mem</code>. If a different name is preferred, make use
 * of {@linkplain DerivedPropertyPolicy} to make a new accounting property with
 * a more appropriate name.
 * </p>
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
 * @author jgreen4, adrianj
 * 
 */


public class PbsParser extends AbstractPbsParser implements Contexed{
	
	private static MakerMap additionalAttributes= new MakerMap();
	private MakerMap extensionAttributes;
	private PropertyRegistry pbsExtensionRegistry;
	
	private ContainerEntryMaker exec_host_maker=null;

	//static final String PBS_PROPERTY_BASE = "pbs.";
	//static final String PBS_RECORD_TYPE_PROPERTY_BASE = PBS_PROPERTY_BASE
	//+ "recordTypes.";

	/*
	 * ##########################################################################
	 * PBS REGISTRIES AND PROPERTY TAGS
	 * ##########################################################################
	 */


	private static final String EXTENSION_REGISTRY_NAME = "pbsExtension";
	public static final PropertyRegistry ADDITIONAL_REGISTRY = new PropertyRegistry(
			"pbs_add","Additional Properties generated by the PBS parser");

	
	// Not part of PBS specification - used as part of 'requester' which is
	// represented as user@host
    @AutoTable(length=64)
	public static final PropertyTag<String> PBS_USER_HOST_PROP = new PropertyTag<>(
			ADDITIONAL_REGISTRY, "host", String.class,"The name of the host a user is associated with");

  

	@AutoTable
	public static final PropertyTag<String> PBS_NODES_PROP = new PropertyTag<>(
			ADDITIONAL_REGISTRY, "nodes",String.class, "The list of node numbers and types used in the execution");

	@AutoTable
	public static final PropertyTag<Integer> PBS_NUM_NODES_PROP = new PropertyTag<>(
			ADDITIONAL_REGISTRY, "num_nodes", Integer.class, "The number of nodes used, derived from the nodes list.");

	@AutoTable
	public static final PropertyTag<Integer> PBS_NUM_CPUS_PROP = new PropertyTag<>(
			ADDITIONAL_REGISTRY, "num_cpus", Integer.class, "The number of cpus used, set explicitly");

	public static final PropertyTag<Integer> PBS_PROC_PER_NODE_PROP = new PropertyTag<>(
			ADDITIONAL_REGISTRY, "ppn", Integer.class, "The number of cpus used per node, derived from the nodes list");

	@AutoTable(target=String.class,length=16)
	public static final PropertyTag<String> PBS_PLACEMENT = new PropertyTag<>(ADDITIONAL_REGISTRY,
			"place",String.class,"Requested placement");

	// //////////////////////////////////////////////////////////////////////////
	// Standard attributes
	// //////////////////////////////////////////////////////////////////////////

	
	static {
		// sometimes this is an explicit field or it could be a resource list
		additionalAttributes.addParser(PBS_NUM_CPUS_PROP, IntegerParser.PARSER);
		additionalAttributes.addParser("Resource_List.ncpus", PBS_NUM_CPUS_PROP, IntegerParser.PARSER);
		// D, K, S
		additionalAttributes.put("requester", new RequesterEntryMaker());

		
		

		additionalAttributes.put("Resource_List.nodes", new PBSNodesCPUEntryMaker());
		
		additionalAttributes.put("Resource_List.place", new PlacementEntryMaker());
	}

	

	// //////////////////////////////////////////////////////////////////////////
	// Derived Properties
	// //////////////////////////////////////////////////////////////////////////


	

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
	public PbsParser(AppContext context) {
		super(context);
	}

	/**
	 * Adds a new extension to the parser. When the parser next parses a file, an
	 * attribute with the same name as the one specified here will use the
	 * <code>maker</code> to parse the value of the attribute and populate a
	 * <code>PropertyContainer</code> accordingly.
	 * 
	 * @param attributeName
	 *          The name of the attribute this maker should be used for
	 * @param maker
	 *          The object that will parse the attribute's value and generate
	 *          entries in a <code>PropertyContainer</code>
	 * @return The previous <code>ContainerEntryMaker</code> that as being used
	 *         for this attribute, or <code>null</code> if there was no
	 *         <code>ContainerEntryMaker</code> associated with attributes of this
	 *         name.
	 */
	public ContainerEntryMaker addExtension(String attributeName,
			ContainerEntryMaker maker) {
		return this.extensionAttributes.put(attributeName, maker);
	}

	
	

	

	/**
	 * Returns the non-standard attributes that this parser uses. These are
	 * attributes not explicitly and completely defined in the PBS specification
	 * but may be used by PBS records. These can be attributes of the form
	 * <code>Resource_List.RESOURCE_NAME</code> or
	 * <code>resources_used.RESOURCE_NAME</code> as allowed by the PBS
	 * specification, however they may be other types of attributes.
	 * 
	 * @return The extension attributes used by this PBS parser in a name-to
	 *         attribute maker map.
	 */
	public MakerMap getExtensions() {
		return this.extensionAttributes;
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
	public PropertyFinder initFinder( PropertyFinder prev,
			String mode) {
		MultiFinder finder = (MultiFinder) super.initFinder(prev,mode);

		// Overidding the default handler
		boolean inc = getContext().getBooleanParameter("include_slot_in_node."+mode, false);
		exec_host_maker= new ExecHostEntryMaker(inc);
		
		this.pbsExtensionRegistry = new PropertyRegistry(EXTENSION_REGISTRY_NAME+mode,"Extension properties generated by the PBS batch parser");
		this.extensionAttributes = new MakerMap();
		
		Properties ctxProperties = getContext().getProperties();

		// Get all the extension attributes
		Enumeration propNames = ctxProperties.propertyNames();
		String base = PBS_PROPERTY_BASE + mode + ".attribute_type.";
		final int baseLength = base.length();
		while (propNames.hasMoreElements()) {
			String propName = propNames.nextElement().toString();

			if (propName.startsWith(base)) {
				String attributeName = propName.substring(baseLength);
				String attributeType = ctxProperties.getProperty(propName);
				this.addAttribute(attributeName, attributeType);
			}
		}
		finder.addFinder(ADDITIONAL_REGISTRY);
		finder.addFinder(this.pbsExtensionRegistry);
		return finder;
	}

	/**


	

	/**
	 * Adds an extension attribute to the map of extension attributes
	 * 
	 * @param name
	 *          The name of the extension attribute
	 * @param typeDeclaration
	 *          The type of the extension attribute, possibly with an explicit
	 *          parser to use for the type
	 * @throws IllegalArgumentException
	 *           If the datatype is not known or the name is an empty string
	 */
	@SuppressWarnings("unchecked")
	private void addAttribute(final String name, String typeDeclaration)
	throws IllegalArgumentException {
		assert name != null : "name cannot be null";
		assert typeDeclaration != null : "type cannot be null";

		if (name.equals(""))
			throw new IllegalArgumentException(
			"cannot use a blank string as an attribute name");

		ValueParserService valueParserService = getContext().getService(ValueParserService.class);
	
		ValueParser parser = valueParserService.getValueParser(typeDeclaration);

		if( parser == null ){
			throw new IllegalArgumentException("Parser Type "+typeDeclaration+" did not resolve");
		}else{
				String tagName = name.replace('.', '_');

				PropertyTag tag;
				try {
					tag = new PropertyTag(pbsExtensionRegistry, tagName, parser
							.getType());
				} catch (ConsistencyError err) {
					throw new IllegalArgumentException("Cannot use '" + tagName
							+ "' as an attribute name. It does not conform "
							+ "the PbsParser attribute naming convensions");
				}

				PropertyEntryMaker maker = new PropertyEntryMaker(tag, parser);
				PbsParser.this.extensionAttributes.put(name, maker);
		}
	}

	/*
	 * ##########################################################################
	 * PRIVATE PARSER CLASSES
	 * ##########################################################################
	 */

	/**
	 * Specific class for parsing the <em>requester</em> attribute. Required
	 * because the attribute needs more than one property tag to store all it's
	 * information
	 */
	private static class RequesterEntryMaker implements ContainerEntryMaker {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * uk.ac.ed.epcc.safe.accounting.parsers.ContainerEntryMaker#setValue(uk
		 * .ac.ed.epcc.safe.accounting.PropertyContainer, java.lang.String)
		 */
		public void setValue(PropertyContainer container, String valueString)
		throws IllegalArgumentException, InvalidPropertyException {
			String userAndHost[] = this.getUserAndHost(valueString);

			container.setProperty(PBS_USER_PROP, userAndHost[0]);
			container.setProperty(PBS_USER_HOST_PROP, userAndHost[1]);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * uk.ac.ed.epcc.safe.accounting.parsers.ContainerEntryMaker#setValue(uk
		 * .ac.ed.epcc.safe.accounting.PropertyMap, java.lang.String)
		 */
		public void setValue(PropertyMap map, String valueString)
		throws IllegalArgumentException {
			String userAndHost[] = this.getUserAndHost(valueString);

			map.setProperty(PBS_USER_PROP, userAndHost[0]);
			map.setProperty(PBS_USER_HOST_PROP, userAndHost[1]);
		}

		private String[] getUserAndHost(String requester)
		throws IllegalArgumentException {
			String userAndHost[] = requester.split("@");

			if (userAndHost.length != 2)
				throw new IllegalArgumentException(
						"bad requester string.  Expected format = user@host.  Value recieved = "
						+ requester);

			return userAndHost;
		}
	}


	/**
	 * ExecHostEntryMaker
	 * 
	 * This class is used to parse the exec_host field in the PBS logs.
	 * The field includes an entry for each host/cpu used in the job separated by 
	 * the "+" character. 
	 * 
	 *  Each entry represents an MPi task and is in the form <em>vnodeName/ID[*C]</em>
	 *  where <em>vnodeName</em> identifies the node, <em>ID</em> is a unique index and <em>C</em>
	 *  is an optional CPU count.
	 *  This class separates the exec_host and calculates the number of nodes and cpus.
	 *    It also stores the original exec_host string back into the container.
	 * 
	 * Some systems (Isambard) have a single vnodeName and only distinguish between
	 * nodes using the ID field
	 * 
	 * @author AdrianJ
	 *
	 */
	private static class ExecHostEntryMaker implements ContainerEntryMaker {
       
		ExecHostEntryMaker(boolean inc_slot_in_node){
			super();
			if( inc_slot_in_node ) {
				patt = Pattern.compile("(?<VNODE>[A-Za-z0-9\\.\\-]+/\\d+)(?<COUNT>\\*\\d+)?");
			}else {
				patt = Pattern.compile("(?<VNODE>[A-Za-z0-9\\.\\-]+)/\\d+(?<COUNT>\\*\\d+)?");
			}
		}
        private int nodes;
        private int cpus;
        private Set<String> node_set = new HashSet<>();
        
        // Its up to the system config whether "slots" are different nodes
        // or sockets within a node
        private final Pattern patt;
       
        // This is not documented in the manual but has been seen in practice
        private static Pattern patt2 = Pattern.compile("(?<VNODE>[A-Za-z0-9\\.\\-]+)/(?<MIN>\\d+)\\-(?<MAX>\\d+)?");
        private void parse(String execHost) throws AccountingParseException{
        	node_set.clear();
        	nodes=0;
        	cpus=0;
        	for(String vnode : execHost.split("\\+")){
        		Matcher m = patt.matcher(vnode);
        		if( m.matches()){
        			node_set.add(m.group("VNODE"));
        			String count = m.group("COUNT");
        			if( count != null && ! count.isEmpty() ){
        				cpus += Integer.parseInt(count.substring(1));
        			}else{
        				cpus++;
        			}
        		}else{
        			Matcher m2 = patt2.matcher(vnode);
        			if( m2.matches()) {
        				node_set.add(m2.group("VNODE"));
        				int min = Integer.parseInt(m2.group("MIN"));
        				int max = Integer.parseInt(m2.group("MAX"));
        				if( max > min) {
        					cpus += max-min+1;
        				}else {
        					cpus++;
        				}
        			}else {
        				throw new IllegalArgumentException("Bad vnode "+vnode);
        			}
        		}
        	}
        	nodes=node_set.size();
        }
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * uk.ac.ed.epcc.safe.accounting.parsers.ContainerEntryMaker#setValue(uk
		 * .ac.ed.epcc.safe.accounting.PropertyContainer, java.lang.String)
		 */
		public void setValue(PropertyContainer container, String valueString)
		throws IllegalArgumentException, InvalidPropertyException, AccountingParseException {

			synchronized (this) {
				parse(valueString);
				container.setProperty(PBS_EXEC_HOST_PROP, valueString);
				container.setProperty(PBS_NUM_CPUS_PROP, cpus);
				container.setProperty(PBS_NUM_NODES_PROP, nodes);
				node_set.clear();
			}

			
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * uk.ac.ed.epcc.safe.accounting.parsers.ContainerEntryMaker#setValue(uk
		 * .ac.ed.epcc.safe.accounting.PropertyMap, java.lang.String)
		 */
		public void setValue(PropertyMap map, String valueString)
		throws IllegalArgumentException, AccountingParseException {

			synchronized (this) {
				parse(valueString);
				map.setProperty(PBS_EXEC_HOST_PROP, valueString);
				map.setProperty(PBS_NUM_CPUS_PROP, cpus);
				map.setProperty(PBS_NUM_NODES_PROP, nodes);
				node_set.clear();
			}
		}

		

	}
	
	
	/**
	 * Nodes cpu EntryMaker
	 * 
	 * Some varients of PBS use a nodes=<n>:ppn=<n> syntax for the nodes
	 * 
	 * @author spb
	 *
	 */
	public static class PBSNodesCPUEntryMaker implements ContainerEntryMaker {


		private final Pattern node_patt = Pattern.compile("(?:nodes=)?(\\d+):ppn=(\\d+)(?::.*)?");
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * uk.ac.ed.epcc.safe.accounting.parsers.ContainerEntryMaker#setValue(uk
		 * .ac.ed.epcc.safe.accounting.PropertyContainer, java.lang.String)
		 */
		public void setValue(PropertyContainer container, String valueString)
		throws IllegalArgumentException, InvalidPropertyException {
			container.setProperty(PBS_NODES_PROP, valueString);
			int total_nodes=0;
			int total_cpus=0;
			for(String phrase : valueString.split("\\+")){

				Matcher m = node_patt.matcher(phrase);
				try{
					if( m.matches()){
						int nodes = Integer.parseInt(m.group(1));
						total_nodes += nodes;
						total_cpus += nodes * Integer.parseInt(m.group(2));

					}else{

						int nodes = Integer.parseInt(valueString);
						total_nodes += nodes;
						// assume 1 cpu per node unless specified otherwise
						total_cpus += nodes;
					}
				}catch( NumberFormatException e){
					throw new IllegalArgumentException(e);
				}
			}
			container.setProperty(PBS_NUM_NODES_PROP, total_nodes);
			container.setProperty(PBS_NUM_CPUS_PROP, total_cpus);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * uk.ac.ed.epcc.safe.accounting.parsers.ContainerEntryMaker#setValue(uk
		 * .ac.ed.epcc.safe.accounting.PropertyMap, java.lang.String)
		 */
		public void setValue(PropertyMap map, String valueString)
		throws IllegalArgumentException {

			try {
				setValue((PropertyContainer)map, valueString);
			} catch (InvalidPropertyException e) {
				// won't happen
			}
		}

		private int getNumCPUs(String execHost) {

			String hosts[] = null;
			hosts = execHost.split("\\+");
			if(hosts != null && hosts.length != 0){
				return hosts.length;
			}else{
				return 1;
			}
		}

	}
	private static class PlacementEntryMaker implements ContainerEntryMaker{

		private static final String EXCL = "excl";

		@Override
		public void setValue(PropertyContainer container, String valueString) throws IllegalArgumentException,
				InvalidPropertyException, NullPointerException, AccountingParseException {
			container.setProperty(PBS_PLACEMENT, valueString);
			if( valueString.contains(EXCL)){
				container.setProperty(BatchParser.EXCLUSIVE, Boolean.TRUE);
			}else{
				container.setProperty(BatchParser.EXCLUSIVE, Boolean.FALSE);
			}
			
		}

		@Override
		public void setValue(PropertyMap map, String valueString)
				throws IllegalArgumentException, NullPointerException, AccountingParseException {
			map.setProperty(PBS_PLACEMENT, valueString);
			if( valueString.equals(EXCL)){
				map.setProperty(BatchParser.EXCLUSIVE, Boolean.TRUE);
			}else{
				map.setProperty(BatchParser.EXCLUSIVE, Boolean.FALSE);
			}
			
		}
		
	}
	@Override
	protected ContainerEntryMaker getEntryMaker(String attr) {
		if( attr.equals("exec_host")) {
			// override the default maker
			return exec_host_maker;
		}
		ContainerEntryMaker e = super.getEntryMaker(attr);
		if( e == null ){
			e = additionalAttributes.get(attr);
		}
		if( e == null){
			e = extensionAttributes.get(attr);
		}
		return e;
	}

	@Override
	public PropExpressionMap getDerivedProperties(PropExpressionMap previous) {
		// TODO Auto-generated method stub
		PropExpressionMap derv = super.getDerivedProperties(previous);
		try {
			derv.put(BatchParser.NODE_COUNT_PROP, PBS_NUM_NODES_PROP);
			derv.put(BatchParser.PROC_COUNT_PROP, PBS_NUM_CPUS_PROP);
		} catch (PropertyCastException e) {
			getLogger().error("Error adding derivations",e);
		}
		
		return derv;
	}


}