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
import java.util.Properties;
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
import uk.ac.ed.epcc.safe.accounting.update.AutoTable;
import uk.ac.ed.epcc.safe.accounting.update.BatchParser;
import uk.ac.ed.epcc.safe.accounting.update.PropertyContainerParser;
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


public class PbsParser extends AbstractPbsParser implements Contexed,
PropertyContainerParser {
	
	private static MakerMap additionalAttributes= new MakerMap();
	private MakerMap extensionAttributes;
	private PropertyRegistry pbsExtensionRegistry;
	

	static final String PBS_PROPERTY_BASE = "pbs.";
	static final String PBS_RECORD_TYPE_PROPERTY_BASE = PBS_PROPERTY_BASE
	+ "recordTypes.";

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
	public static final PropertyTag<String> PBS_USER_HOST_PROP = new PropertyTag<String>(
			ADDITIONAL_REGISTRY, "host", String.class,"The name of the host a user is associated with");

  

	@AutoTable
	public static final PropertyTag<String> PBS_NODES_PROP = new PropertyTag<String>(
			ADDITIONAL_REGISTRY, "nodes",String.class, "The list of node numbers and types used in the execution");

	@AutoTable
	public static final PropertyTag<Integer> PBS_NUM_NODES_PROP = new PropertyTag<Integer>(
			ADDITIONAL_REGISTRY, "num_nodes", Integer.class, "The number of nodes used, derived from the nodes list.");

	@AutoTable
	public static final PropertyTag<Integer> PBS_NUM_CPUS_PROP = new PropertyTag<Integer>(
			ADDITIONAL_REGISTRY, "num_cpus", Integer.class, "The number of cpus used, derived from the nodes list");

	public static final PropertyTag<Integer> PBS_PROC_PER_NODE_PROP = new PropertyTag<Integer>(
			ADDITIONAL_REGISTRY, "ppn", Integer.class, "The number of cpus used per node, derived from the nodes list");


	// //////////////////////////////////////////////////////////////////////////
	// Standard attributes
	// //////////////////////////////////////////////////////////////////////////

	
	static {
		additionalAttributes.addParser(PBS_NUM_CPUS_PROP, IntegerParser.PARSER);
		// D, K, S
		additionalAttributes.put("requester", new RequesterEntryMaker());

		additionalAttributes.put("exec_host", new ExecHostEntryMaker());

		additionalAttributes.put("Resource_List.nodes", new PBSNodesCPUEntryMaker());
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
	 * <code>Resorce_List.RESOURCE_NAME</code> or
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
	public PropertyFinder initFinder(AppContext ctx, PropertyFinder prev,
			String mode) {
		MultiFinder finder = (MultiFinder) super.initFinder(ctx,prev,mode);

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
	 * the "+" character.  This class separates the exec_host anmd counts the number 
	 * of separate entries.  It also stores the original exec_host string back into 
	 * the logs.
	 * 
	 * @author AdrianJ
	 *
	 */
	private static class ExecHostEntryMaker implements ContainerEntryMaker {

		ExecHostEntryMaker(){
			super();
			
		}

		
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * uk.ac.ed.epcc.safe.accounting.parsers.ContainerEntryMaker#setValue(uk
		 * .ac.ed.epcc.safe.accounting.PropertyContainer, java.lang.String)
		 */
		public void setValue(PropertyContainer container, String valueString)
		throws IllegalArgumentException, InvalidPropertyException {

			int numCPUs = this.getNumCPUs(valueString);

			container.setProperty(PBS_EXEC_HOST_PROP, valueString);
			container.setProperty(PBS_NUM_CPUS_PROP, numCPUs);
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

			int numCPUs = this.getNumCPUs(valueString);

			map.setProperty(PBS_EXEC_HOST_PROP, valueString);
			map.setProperty(PBS_NUM_CPUS_PROP, numCPUs);
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
	/**
	 * Nodes cpu EntryMaker
	 * 
	 * Some varients of PBS use a modes=<n>:ppn=<n> syntax for the nodes
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
	
	@Override
	protected ContainerEntryMaker getEntryMaker(String attr) {
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
			getContext().error(e,"Error adding derivations");
		}
		
		return derv;
	}


}