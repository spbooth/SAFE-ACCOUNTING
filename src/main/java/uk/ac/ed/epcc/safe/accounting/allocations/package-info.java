/**
Classes supporting resource allocation.
Note that allocation mechanisms may be purely informational as in a fair-share system,
or they may involve an charging mechanism applied when the records are parsed.
<p>
The key observation here is that Allocations are UsageRecords. Except that they record allocated usage
rather than actual usage. Many of the same types of reports might be made with allocation as with usage.
</p>
 * 
 */
package uk.ac.ed.epcc.safe.accounting.allocations;