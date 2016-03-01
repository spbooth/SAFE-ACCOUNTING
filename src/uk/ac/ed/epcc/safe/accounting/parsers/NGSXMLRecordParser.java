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
import java.util.Properties;

import uk.ac.ed.epcc.safe.accounting.expr.BinaryPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ConstPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.IntPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropertyCastException;
import uk.ac.ed.epcc.safe.accounting.parsers.value.IntervallicVolumeParser;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.jdbc.expr.Operator;

/** OGFUR parser extended to include standard NGS additions
 * Note that this class uses a different version of the schema
 * (with different schema namespace) from the parent class.
 * 
 * @author spb
 *
 */


public class NGSXMLRecordParser extends OGFXMLRecordParser {
	public static final PropertyRegistry NGS_REGISTRY = new PropertyRegistry("ngs", "NGS additional properties");
	@Path("//ur:Memory[@ur:type=\"memoryUsed\"]")
    @ParseClass(parser=IntervallicVolumeParser.class)
    public static final PropertyTag<Number> NGS_MEMORY_USED_PROP = new PropertyTag<Number>(
			NGS_REGISTRY, "MemoryUsed", Number.class, "Memory used converted to bit-seconds");
	public static final PropertyTag<Integer> NGS_KB_USED_PROP = new PropertyTag<Integer>(
			NGS_REGISTRY, "KBUsed", Integer.class, "Memory used converted to KB");

	@Path("//ur:Memory[@ur:type=\"virtualMemoryUsed\"]")
    @ParseClass(parser=IntervallicVolumeParser.class)
    public static final PropertyTag<Number> NGS_VIRTUAL_MEMORY_USED_PROP = new PropertyTag<Number>(
			NGS_REGISTRY, "VirtualMemoryUsed", Number.class, "Virtual Memory used converted to bit-seconds");
	 public static final PropertyTag<Integer> NGS_VIRTUAL_KB_USED_PROP = new PropertyTag<Integer>(
				NGS_REGISTRY, "VirtualKBUsed", Integer.class, "Virtual Memory used converted to KB");
		
	@Path("//ur:TimeInstant[@ur:type=\"timeGlobusSubmitted\"]")
	public static final PropertyTag<Date> GLOBUS_SUBMITTED_PROP = new PropertyTag<Date>(
			NGS_REGISTRY, "GlobusSubmittedTime",Date.class);
	@Path("//ur:TimeInstant[@ur:type=\"timeGlobusExecutable\"]")
	public static final PropertyTag<Date> GLOBUS_EXECUTABLE_PROP = new PropertyTag<Date>(
			NGS_REGISTRY, "GlobusExecutableTime",Date.class);
	@Path("//ur:TimeInstant[@ur:type=\"timeGlobusFinished\"]")
	public static final PropertyTag<Date> GLOBUS_FINISHED_PROP = new PropertyTag<Date>(
			NGS_REGISTRY, "GlobusFinishedTime",Date.class);
	@Path("Resource[@ur:description=\"VOinfo\"]")
	public static final PropertyTag<String> VO_PROP = new PropertyTag<String>(
			NGS_REGISTRY, "Vo",String.class);

	
	static{
		NGS_REGISTRY.lock();
	}
	public NGSXMLRecordParser(AppContext context) {
		super(context);
	}
	

	@Override
	protected PropertyFinder initFinder(AppContext context, 
			PropertyFinder prev) {
		MultiFinder mf = new MultiFinder();
		mf.addFinder(super.initFinder(context,  prev));
		mf.addFinder(NGS_REGISTRY);
		return mf;
	}


	@Override
	public PropExpressionMap getDerivedProperties(PropExpressionMap previous) {
		PropExpressionMap map= super.getDerivedProperties(previous);
		try {
			map.put(NGSXMLRecordParser.NGS_KB_USED_PROP,new IntPropExpression<Number>( new BinaryPropExpression(NGSXMLRecordParser.NGS_MEMORY_USED_PROP, Operator.DIV, new ConstPropExpression<Integer>(Integer.class,8192))));
			map.put(NGSXMLRecordParser.NGS_VIRTUAL_KB_USED_PROP, new IntPropExpression<Number>(new BinaryPropExpression(NGSXMLRecordParser.NGS_VIRTUAL_MEMORY_USED_PROP, Operator.DIV, new ConstPropExpression<Integer>(Integer.class,8192))));
			
		} catch (PropertyCastException e) {
			getLogger().error("Error making derived prop",e);
		}
		return map;
	}
	@Override
	protected String defaultSchemaName() {
		return "urwg-schema.11.xsd";
	}
	@Override
	protected ParsernameSpaceContext makeNameSpaceContext() {

		ParsernameSpaceContext res = new ParsernameSpaceContext(getContext());
		res.addNamespace("ur","http://www.gridforum.org/2003/ur-wg");
		res.addNamespace("xd","http://www.w3.org/2000/09/xmldsig#");
		return res;
	}
	
}