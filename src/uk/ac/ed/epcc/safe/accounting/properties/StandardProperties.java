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
package uk.ac.ed.epcc.safe.accounting.properties;

import java.util.Date;

import uk.ac.ed.epcc.webapp.model.data.Duration;

/* Standard time property registry
 * 
 */
public class StandardProperties {

	public static final PropertyRegistry time = new PropertyRegistry("time","Time extent of usage records");
	public static final PropertyTag<Date> ENDED_PROP = new PropertyTag<Date>(time,StandardProperties.COMPLETED_TIMESTAMP,Date.class,"The time resource usage ended");
	public static final PropertyTag<Date> STARTED_PROP = new PropertyTag<Date>(time,StandardProperties.STARTED_TIMESTAMP,Date.class,"The time resource usage started");
	public static final PropertyTag<Number> ELAPSED_PROP = new PropertyTag<Number>(time,"Elapsed",Number.class,"Wall-clock run-time in seconds derived from timestamps");
	public static final String COMPLETED_TIMESTAMP = "CompletedTimestamp";
	public static final String STARTED_TIMESTAMP = "StartedTimestamp";

	

	public static final PropertyRegistry base = new PropertyRegistry("base","The common set of accounting properties for all parsers");
	public static final PropertyTag<String> USERNAME_PROP = new PropertyTag<String>(base,"UserName",String.class,"The username using the resource");
	public static final PropertyTag<String> GROUPNAME_PROP = new PropertyTag<String>(base,"GroupName",String.class,"The unix groupname of the primary group using the resource");
	public static final PropertyTag<String> WORKING_DIR = new PropertyTag<String>(base,"CWD",String.class,"Full working directory of resource use");
	public static final PropertyTag<String> COMMAND = new PropertyTag<String>(base,"Command",String.class,"Executed command, optionally with command arguments");
	public static final PropertyTag<String> EXECUTABLE = new PropertyTag<String>(base,"Executable",String.class,"Executed command");
	public static final PropertyTag<Integer> EXIT_PROP = new PropertyTag<Integer>(base,"Exit",Integer.class,"Numerical exit status of job or command");
	public static final PropertyTag<String> ERROR_PROP = new PropertyTag<String>(base,"Error",String.class,"Error string returned from Job");
	public static final PropertyTag<Number> CPU_TIME_PROP = new PropertyTag<Number>(base,"CPUTime",Number.class,"Total CPU time (seconds) used by all processes");
	public static final PropertyTag<Number> RUNTIME_PROP = new PropertyTag<Number>(base,"RuntimeMillis",Number.class,"Wall-clock run-time in milliseconds");
	public static final PropertyTag<Duration> DURATION_PROP = new PropertyTag<Duration>(base,"Duration",Duration.class,"Duration of usage");
	public static final PropertyTag<String> MACHINE_NAME_PROP = new PropertyTag<String>(base,"MachineName",String.class);
	public static final PropertyTag<Date> INSERTED_PROP = new PropertyTag<Date>(time,"Inserted",Date.class,"Time record was inserted in database");
	public static final PropertyTag<String> TEXT_PROP = new PropertyTag<String>(base,"Text",String.class,"Full text of accounting record as submitted");
	public static final PropertyTag<Long> COUNT_PROP = new PropertyTag<Long>(time,"JobCount",Long.class,"Number of Jobs");
	/** Table the record is stored in. This is generally not persisted but some parsers/policies might need it to
	 * look up table specific configuration.
	 */
	public static final PropertyTag<String> TABLE_PROP = new PropertyTag<String>(time,"Table",String.class,"Table record stored in");
	static{
		time.lock();
		base.lock();
	}
}