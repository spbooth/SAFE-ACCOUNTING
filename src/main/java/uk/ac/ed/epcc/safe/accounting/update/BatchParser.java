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
package uk.ac.ed.epcc.safe.accounting.update;

import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.expr.BinaryPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ConstPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.ConvertMillisecondToDatePropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.DurationPropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.MilliSecondDatePropExpression;
import uk.ac.ed.epcc.safe.accounting.expr.PropExpressionMap;
import uk.ac.ed.epcc.safe.accounting.expr.PropertyCastException;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyRegistry;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.StandardProperties;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
import uk.ac.ed.epcc.webapp.jdbc.expr.Operator;
import uk.ac.ed.epcc.webapp.model.data.Duration;

/** extends {@link BaseParser} to add properties specific to batch system records.
 * 
 * @author spb
 *
 */
public abstract class BatchParser extends BaseParser {
    public BatchParser(AppContext conn) {
		super(conn);
	}
	public static final String SUBMITTED_TIMESTAMP = "SubmittedTimestamp";


	public static final PropertyRegistry batch= new PropertyRegistry("batch","The common set of accounting properties for all batch parsers");
		
	public static final PropertyTag<String> QUEUE_PROP = new PropertyTag<>(batch,"Queue",String.class,"Name of queue or job class");
    
    public static final PropertyTag<Date> SUBMITTED_PROP = new PropertyTag<>(batch,SUBMITTED_TIMESTAMP,Date.class,"Time job was submitted");
    
   
    public static final PropertyTag<String> JOB_ID_PROP = new PropertyTag<>(batch,"JobID",String.class,"Job is string as seen by the batch job, fairly unique but may recycle over long periods");
 
    public static final PropertyTag<String> JOB_NAME_PROP = new PropertyTag<>(batch,"JobName",String.class,"Job name as provided by the user");
   
    public static final PropertyTag<Integer> REQUESTED_CPUS_PROP = new PropertyTag<>(batch,"RequestedCpus",Integer.class,"Number of cpus requested by the job");
    
	public static final PropertyTag<Integer> PROC_COUNT_PROP = new PropertyTag<>(batch,"CPUs",Integer.class,"Number of cpus allocated to the job");
   
	public static final PropertyTag<Integer> NODE_COUNT_PROP = new PropertyTag<>(batch,"Nodes",Integer.class,"Number of distributed memory nodes allocated to the job");

    public static final PropertyTag<Integer> TASK_COUNT_PROP = new PropertyTag<>(batch,"Tasks",Integer.class,"Number of tasks (mpi) used by the job");
    
    public static final PropertyTag<String> PARTITION_PROP = new PropertyTag<>(batch,"Partition",String.class,"Named allocated partition of resource");
  
    public static final PropertyTag<String> ACCOUNT_PROP = new PropertyTag<>(batch,"Account",String.class,"Account string as specified for Batch job");
    
    public static final PropertyTag<Number> WALLCLOCK_PROP = new PropertyTag<>(batch,"Wall",Number.class,"Wallclock in seconds as reported directly by batch system");
    public static final PropertyTag<Duration> REQUESTED_WALLCLOCK_PROP = new PropertyTag<>(batch,"RequestedWall",Duration.class,"Requested Wallclock time");
    public static final PropertyTag<Date> EXPECTED_FINISH = new PropertyTag<>(batch,"ExpectedFinish",Date.class,"Expected finish time based on start time and requested wall-clock");
    public static final PropertyTag<Number> RESIDENCY_PROP = new PropertyTag<>(batch,"Residency",Number.class,"Residency milliseconds proportional to associated CPUS");
    
    public static final PropertyTag<Number> TIME_PROP = new PropertyTag<>(batch,"Time",Number.class,"Residency seconds proportional to associated CPUS");
    public static final PropertyTag<Number> HOURS_PROP = new PropertyTag<>(batch,"CPUHours",Number.class,"Residency hours proportional to associated CPUS");
        
    public static final PropertyTag<Number> WAIT_PROP = new PropertyTag<>(batch,"Wait",Number.class,"Wait time in milliseconds");
   
    public static final PropertyTag<Number> CPU_WAIT_PROP = new PropertyTag<>(batch,"CPUWait",Number.class,"Wait time in milliseconds proportional to CPUS");
    public static final PropertyTag<Boolean> FAULT_PROP = new FailProperty(batch,"Fault","Did a hardware fault terminate this job");
    public static final PropertyTag<Boolean> SUCCESS_PROP = new PropertyTag<>(batch,"Success",Boolean.class,"Did job succeed");
    public static final PropertyTag<Boolean> SUBJOB_PROP = new PropertyTag<>(batch,"SubJob",Boolean.class,"Is this a sub-job and should be recorded not charged");
    
    public static final PropertyTag<Boolean> SERIAL_PROP = new PropertyTag<>(batch,"Serial",Boolean.class,"Is this a serial rather than a parallel job");
    
    public static final PropertyTag<Number> SLOWDOWN_PROP = new PropertyTag<>(batch,"Slowdown",Number.class);
    public static final PropertyTag<Number> REQUEST_ACCURACY_PROP = new PropertyTag<>(batch,"RequestAccuracy",Number.class);
    public static final PropertyTag<String> BATCH_SCRIPT_PROP = new PropertyTag<>(batch,"Script",String.class,"The text of the batch script");
    @OptionalTable(target=Boolean.class)
    public static final PropertyTag<Boolean> EXCLUSIVE = new PropertyTag<>(batch,"Exclusive",Boolean.class,"Did the job have exclusive use of the nodes");
    static{
    	batch.lock();
    }
    @Override
	public PropExpressionMap getDerivedProperties(PropExpressionMap previous) {
		PropExpressionMap res = super.getDerivedProperties(previous);
		try{
		res.put(RESIDENCY_PROP,
				new BinaryPropExpression(PROC_COUNT_PROP,Operator.MUL,StandardProperties.RUNTIME_PROP)
		);
		// derive from residency as an aggregate table may have persisted it directly 
		res.put(TIME_PROP,
				new BinaryPropExpression(RESIDENCY_PROP, Operator.DIV, new ConstPropExpression<>(Integer.class,Integer.valueOf(1000))));
		res.put(HOURS_PROP,
				new BinaryPropExpression(RESIDENCY_PROP, Operator.DIV, new ConstPropExpression<>(Integer.class,Integer.valueOf(3600*1000))));

		// use requested value if we don't have actual value and vice versa
		res.put(PROC_COUNT_PROP, REQUESTED_CPUS_PROP);
		res.put(REQUESTED_CPUS_PROP,PROC_COUNT_PROP);
		
		// fall back to requested cpus if task count not explicit
		//res.put(TASK_COUNT_PROP, REQUESTED_CPUS_PROP);
		
		// default cputime to residency seconds
		res.put(StandardProperties.CPU_TIME_PROP, TIME_PROP);
		res.put(WAIT_PROP, new BinaryPropExpression(
				new MilliSecondDatePropExpression(StandardProperties.STARTED_PROP),
				Operator.SUB,
				new MilliSecondDatePropExpression(SUBMITTED_PROP)
				));
		res.put(CPU_WAIT_PROP,
				new BinaryPropExpression(PROC_COUNT_PROP,Operator.MUL,WAIT_PROP)
		);
		res.put(SLOWDOWN_PROP, new BinaryPropExpression(
				new BinaryPropExpression(new MilliSecondDatePropExpression(StandardProperties.ENDED_PROP), Operator.SUB, new MilliSecondDatePropExpression(StandardProperties.STARTED_PROP)), 
				Operator.DIV, 
				new BinaryPropExpression(new MilliSecondDatePropExpression(StandardProperties.ENDED_PROP), Operator.SUB, new MilliSecondDatePropExpression(SUBMITTED_PROP))));
		res.put(REQUEST_ACCURACY_PROP, new BinaryPropExpression(new DurationPropExpression(StandardProperties.STARTED_PROP, StandardProperties.ENDED_PROP), Operator.DIV, REQUESTED_WALLCLOCK_PROP));
		res.put(EXPECTED_FINISH, new ConvertMillisecondToDatePropExpression(
				new BinaryPropExpression(new MilliSecondDatePropExpression(StandardProperties.STARTED_PROP), Operator.ADD, REQUESTED_WALLCLOCK_PROP)));
		// If no definition assume false
		res.put(SERIAL_PROP, new ConstPropExpression<Boolean>(Boolean.class, Boolean.FALSE));
		res.put(SUBJOB_PROP, new ConstPropExpression<Boolean>(Boolean.class, Boolean.FALSE));
		}catch(PropertyCastException e){
			throw new ConsistencyError("cast check failed for built in propexpression ",e);
		} 
		return res;
	}
  
}