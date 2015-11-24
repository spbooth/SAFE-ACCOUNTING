// Copyright - The University of Edinburgh 2011
/**
 * This package handles the accounting records.
<p>
The aim of these classes is to support general accounting and report generation with a variety of 
deployment  and 
accounting models.
</p>
<p>
The key item here is an accounting record. This contains a combination of different kinds of data:
</p>
<ul>
<li>Raw factual information e.g. the accounting output of a batch system.</li>
<li>Derived information due to local policy/infrastructure e.g. the charge applied to a job or 
the department the user belongs to.</li>
</ul>
<p>
This naturally leads us towards having implementation classes for different providers of factual 
information and augmenting them 
(by extension or composition) with the local policy.
In practice some of the derived information needs to be stored and cannot just be generated on 
the fly from the factual data. Charging data
needs to reflect the actual charge made in case charging parameters are changed. 
Also any derived quantities that will be used for
record selection may need to be recorded with the record for performance reasons. 
In some cases raw data from different data sources needs to be combined. For example to add a 
log of which executables are running to the
basic information in a batch accounting log.
</p>
<p>
There are different types of resource quantity we need to consider:
</p>
<ul>
<li>An <b>Annotation</b> quantity that records something about the context of the record. for Example: the User or the Queue.
</li>
<li>An <b>Point</b> quantity measures the use of resources being consumed at a single point 
in time. For example: number of nodes in use, number of Kb occupied. As our data model only allows a single value
for a property Point quantities that vary over time need to be represented by an indicative value such as the average. 
</li>
<li>An <b>Cumulative</b> quantity that increases over time for example node-hours
 used by a job or Kb-days of disk usage, total page-faults.
Charging is usually based on Cumulative quantities.
</li>
</ul>

Where the period covered by a usage record is defined a mapping exists between Point and Cumulative properties.
Point properties can be integrated (or representative values multiplied by the period length) to give a corresponding
Cumulative property. Cumulative properties can be divided by the period length to give an indicative rate.
The correct mapping of Point and Cumulative properties onto a reporting period tends to be different depending on the 
property in question but many common mappings can be performed by 
{@link uk.ac.ed.epcc.safe.accounting.OverlapHandler}.

<p>
The lowest common denominator of accounting record is something like:
</p>
<ul>
<li>Period over which resource was used</li>
<li>A set of properties corresponding to consumed resources (e.g. CPU time or charge)</li>
<li>A set of properties giving information about the nature of the job (e.g. cpu-count or queue-name)</li>
<li>A set of properties identifying responsibility for the job (e.g. user or project)</li> 
</ul>
<p>
However this could represent many different things depending on the system.
E.g.
</p>
<ul>
<li>A single process</li>
<li> A batch job</li>
<li> The output of a days accounting summary</li>
</ul>
<p>
Many accounting operations require that some subset of the accounting records is selected.   
Usually by date-range and some record properties and a chart or table produced of one of the 
Extrinsic properties often divided into 
classes based on additional job properties. For example a table of CPU-time grouped by project.
</p>
<p>
We need to abstract out these different concepts to allow common code that can cope with the 
different quantities and properties supported
by different accounting schemes. In addition we sometimes want to support aggregate reports 
over multiple types of accounting
scheme. In which case we need a combining object that implements the same basic interface 
but only exposes those quantities and
properties that are common to all the underlying schemes. 
</p>
<p>
In order to support multiple accounting schemes at the same time we need a {@link uk.ac.ed.epcc.safe.accounting.UsageManager} 
class to present a unified view of the tables from the different schemes.
</p>
<p>
Standard accounting only uses the functionality of {@link  uk.ac.ed.epcc.safe.accounting.UsageRecord}. 
Accounting data is queried via the {@link uk.ac.ed.epcc.safe.accounting.UsageProducer} interface. 
 {@link uk.ac.ed.epcc.safe.accounting.UsageManager}  has to implement this interface.
The Factory classes for the various sub-classes of {@link uk.ac.ed.epcc.safe.accounting.UsageRecord} also need to present 
a standard interface for the {@link uk.ac.ed.epcc.safe.accounting.UsageManager} to use. This is also {@link uk.ac.ed.epcc.safe.accounting.UsageProducer} 
interface. 
</p>
<p>
Because we wish to support aggregate operations over multiple tables we introduce an additional level of data 
abstraction over that used by Repository/Record. Instead of identifying quantities by their database field name
we use {@link uk.ac.ed.epcc.safe.accounting.properties.PropertyTag}s. These are translated into objects from <b>uk.ac.epcc.model.data.expr</b> by the underlying classes. This allows properties 
to be represented by different field names in different tables. Properties can also be used to reference complex expressions {@link uk.ac.ed.epcc.safe.accounting.properties.PropExpression}s involving multiple
table fields.
</p>
 */
package uk.ac.ed.epcc.safe.accounting;