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
package uk.ac.ed.epcc.safe.accounting.policy;

import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.expr.DeRefExpression;
import uk.ac.ed.epcc.safe.accounting.expr.DurationCastPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropertyCastException;
import uk.ac.ed.epcc.safe.accounting.expr.SelectPropExpression;
import uk.ac.ed.epcc.safe.accounting.parsers.GlobusJobManagerParser;
import uk.ac.ed.epcc.safe.accounting.parsers.NGSXMLRecordParser;
import uk.ac.ed.epcc.safe.accounting.parsers.OGFXMLRecordParser;
import uk.ac.ed.epcc.safe.accounting.properties.MultiFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceTag;
import uk.ac.ed.epcc.safe.accounting.update.BatchParser;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
import uk.ac.ed.epcc.webapp.jdbc.table.ReferenceFieldType;
import uk.ac.ed.epcc.webapp.jdbc.table.TableSpecification;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
/** Policy to define standard properties used to 
 * generate OGF-Usage records
 * This policy provides some standard mappings which can be
 * overidden/extended by using an {@link ExpressionPropertyPolicy}
 * @author spb
 *
 */


public class GridPropertyPolicy extends BaseUsageRecordPolicy {

	private AppContext conn;
	private static final String GLOBUS_FIELD="GlobusID";
	private ReferenceTag globus_tag=null;
	public PropertyFinder initFinder(AppContext ctx, PropertyFinder prev,
			String table) {
		conn=ctx;
		MultiFinder mf = new MultiFinder();
		mf.addFinder(OGFXMLRecordParser.OGFUR_DEFAULT_REGISTRY);
		mf.addFinder(NGSXMLRecordParser.NGS_REGISTRY);
		String globus_table = ctx.getInitParameter("GridPropertyPolicy."+table+".globus");
		if( globus_table != null ){
			globus_tag=(ReferenceTag) prev.find(IndexedReference.class, globus_table);
		}
		return mf;
	}

	
	protected <T> void ADD_DERIVATON(PropExpressionMap map, PropertyTag<T> p2, PropExpression<T> p1)
	{
		try{
			map.put(p2, p1);
		}catch(PropertyCastException t){
			getLogger().error("Error adding Derivations in OGFUsageRecordParser",t);
			throw new ConsistencyError("Bad derivation", t);
		}
	}
	@SuppressWarnings("unchecked")
	protected <T> void ADD_REMOTE(PropExpressionMap map, PropertyTag<T> local, PropertyTag<T> remote){
		try{
		    map.put(local, new DeRefExpression(globus_tag, remote));
		}catch(PropertyCastException e){
			getLogger().error("Error adding expression for globus property",e);
		}
	}
	@SuppressWarnings("unchecked")
	@Override
	public PropExpressionMap getDerivedProperties(PropExpressionMap previous) {
		PropExpressionMap map = super.getDerivedProperties(previous);
			ADD_DERIVATON(map,OGFXMLRecordParser.OGFUR_END_TIME_PROP, StandardProperties.ENDED_PROP);
			ADD_DERIVATON(map,OGFXMLRecordParser.OGFUR_MACHINE_NAME_PROP, StandardProperties.MACHINE_NAME_PROP);
			ADD_DERIVATON(map,OGFXMLRecordParser.OGFUR_START_TIME_PROP, StandardProperties.STARTED_PROP);
			ADD_DERIVATON(map,OGFXMLRecordParser.OGFUR_LOCAL_USER_ID_PROP, StandardProperties.USERNAME_PROP);
			ADD_DERIVATON(map, OGFXMLRecordParser.OGFUR_WALL_DURATION_PROP, StandardProperties.DURATION_PROP);
			try {
				map.put(OGFXMLRecordParser.OGFUR_CPU_DURATION_PROP, new DurationCastPropExpression<Number>(StandardProperties.CPU_TIME_PROP, 1000L));
			} catch (PropertyCastException e) {
				getLogger().error("Error making CPU_DURATION expression",e);
			}
			try {
				
				map.put(OGFXMLRecordParser.OGFUR_CREATE_TIME_PROP, new SelectPropExpression<Date>(Date.class, new PropExpression[]{StandardProperties.INSERTED_PROP,StandardProperties.ENDED_PROP}));
			} catch (PropertyCastException e) {
				getLogger().error("Error making create CREATE_TIME expression",e);
			}

			ADD_DERIVATON(map,OGFXMLRecordParser.OGFUR_QUEUE_PROP, BatchParser.QUEUE_PROP);
			ADD_DERIVATON(map,OGFXMLRecordParser.OGFUR_JOB_NAME_PROP, BatchParser.JOB_NAME_PROP);
			ADD_DERIVATON(map, OGFXMLRecordParser.OGFUR_LOCAL_JOB_ID_PROP, BatchParser.JOB_ID_PROP);
			ADD_DERIVATON(map,OGFXMLRecordParser.OGFUR_NODE_COUNT_PROP, BatchParser.NODE_COUNT_PROP);
			ADD_DERIVATON(map,OGFXMLRecordParser.OGFUR_PROCESSORS_PROP, BatchParser.PROC_COUNT_PROP);

			if( globus_tag != null ){
				ADD_REMOTE(map,NGSXMLRecordParser.GLOBUS_SUBMITTED_PROP,GlobusJobManagerParser.GLOBUS_START_DATE);
				ADD_REMOTE(map,NGSXMLRecordParser.GLOBUS_EXECUTABLE_PROP,GlobusJobManagerParser.GLOBUS_EXEC_DATE);
				ADD_REMOTE(map,NGSXMLRecordParser.GLOBUS_FINISHED_PROP,GlobusJobManagerParser.GLOBUS_END_DATE);
				ADD_REMOTE(map,OGFXMLRecordParser.OGFUR_SUBJECT_NAME_PROP,GlobusJobManagerParser.GLOBUS_DN);
				ADD_REMOTE(map,OGFXMLRecordParser.OGFUR_SUBMIT_HOST_PROP,GlobusJobManagerParser.GLOBUS_SUBMIT_HOST);
			}
			
			
			return map;
	}


	@Override
	public TableSpecification modifyDefaultTableSpecification(AppContext conn,TableSpecification t,PropExpressionMap map,String tagName) {
		if( t != null ){
			String globus_table = conn.getInitParameter("GridPropertyPolicy."+tagName+".globus");
			if( globus_table != null){
				t.setField(GLOBUS_FIELD, new ReferenceFieldType(globus_table));
			}
		}
		return t;
	}
	protected final Logger getLogger(){
		return conn.getService(LoggerService.class).getLogger(getClass());
	}
}