//| Copyright - The University of Edinburgh 2015                            |
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
package uk.ac.ed.epcc.safe.accounting.parsers;

import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.ogf.ur.XMLSplitter;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.update.AutoTable;
import uk.ac.ed.epcc.webapp.AppContext;
/** The LoadLeveler global-history file is a structured binary file. The data access API
 * can be used to pull out property values using key values defined in "llapi.h"
 * This allows the set of supported properties to be extended without breaking backwards compatibility.
 * 
 * Here we assume that the data has been converted to an XML form by an auxiliary C program
 * with elements tags matching the
 * key values in llapi.h. This gives us an extensible format which can code with additional/missing
 * values.
 * 
 * @author spb
 *
 */
public class LoadLevelerParser extends XMLRecordParser {
	public static final PropertyRegistry loadleveler_reg = new PropertyRegistry("loadl", "Properties from loadlevler");
	
	@AutoTable
	@Path("//LL_JobName")
	public static final PropertyTag<String> LL_JobName = new PropertyTag<String>(loadleveler_reg, "LL_JobName", String.class, "Loadleveler job name");
	@AutoTable
	@Path("//LL_JobSubmitTime")
	public static final PropertyTag<Date> LL_JobSubmitTime = new PropertyTag<Date>(loadleveler_reg,"LL_JobSubmitTime",Date.class,"Loadleveler submit time");
	
	@Path("//LL_JobSubmittingUser")
	public static final PropertyTag<String> LL_JobSubmittingUser = new PropertyTag<String>(loadleveler_reg,"LL_JobSubmittingUser",String.class,"Loadleveler user submitting job");
	
	@Path("//LL_StepAccountNumber")
	public static final PropertyTag<String> LL_StepAccountNumber = new PropertyTag<String>(loadleveler_reg,"LL_StepAccountNumber",String.class,"Loadlevler account string");
	
	@AutoTable
	@Path("//LL_StepCompletionCode")
	public static final PropertyTag<Integer> LL_StepCompletionCode = new PropertyTag<Integer>(loadleveler_reg,"LL_StepCompletionCode",Integer.class,"Loadleveler completion code");
	
	@AutoTable
	@Path("//LL_StepCompletionDate")
	public static final PropertyTag<Date> LL_StepCompletionDate = new PropertyTag<Date>(loadleveler_reg,"LL_StepCompletionDate",Date.class,"Loadleveler completion date");
	
	
	@AutoTable
	@Path("//LL_StepID")
	public static final PropertyTag<String> LL_StepID = new PropertyTag<String>(loadleveler_reg,"LL_StepID",String.class,"Loadleveler step id");
	
	@AutoTable
	@Path("//LL_StepStartTime")
	public static final PropertyTag<Date> LL_StepStartTime = new PropertyTag<Date>(loadleveler_reg,"LL_StepStartTime",Date.class,"Loadleveler start time");
	
	@AutoTable
	@Path("//LLStepBgJobId")
	public static final PropertyTag<Integer> LL_StepBgJobId = new PropertyTag<Integer>(loadleveler_reg,"LL_StepBgJobId",Integer.class);
	@AutoTable
	@Path("//LLStepBgSizeRequested")
	public static final PropertyTag<Integer> LL_StepBgSizeRequested = new PropertyTag<Integer>(loadleveler_reg,"LL_StepBgSizeRequested",Integer.class);
	@AutoTable
	@Path("//LLStepBgSizAllocated")
	public static final PropertyTag<Integer> LL_StepBgSizeAllocated = new PropertyTag<Integer>(loadleveler_reg,"LL_StepBgSizeAllocated",Integer.class);
	
	@AutoTable
	@Path("//LL_StepNodeCount")
	public static final PropertyTag<Integer> LL_StepNodeCount = new PropertyTag<Integer>(loadleveler_reg,"LL_StepNodeCount",Integer.class,"Number of nodes in step");
	
	@AutoTable
	@Path("//LL_StepTaskInstanceCount")
	public static final PropertyTag<Integer> LL_StepTaskInstanceCount = new PropertyTag<Integer>(loadleveler_reg,"LL_StepTaskInstanceCount",Integer.class,"number of task instances");
	
	@AutoTable
	@Path("//LL_StepJobClass")
	public static final PropertyTag<String> LL_StepJobClass = new PropertyTag<String>(loadleveler_reg,"LL_StepJobClass",String.class,"defined class for step");
	
	@AutoTable
	@Path("//LL_StepPowerConsumption")
	public static final PropertyTag<Double> LL_StepPowerConsumption = new PropertyTag<Double>(loadleveler_reg, "LL_StepPowerConsumption", Double.class, "Power consumed in kwh");
	
	
	
	
	@Override
	protected XMLSplitter getSplitter() {
		return new XMLSplitter(new String[] {"JobStep"});
	}

	public LoadLevelerParser(AppContext context) {
		super(context);
	}

}