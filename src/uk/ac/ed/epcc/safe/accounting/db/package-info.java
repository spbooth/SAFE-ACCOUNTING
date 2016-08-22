/**

This package contains classes that implement the Accounting interfaces from 
uk.ac.ed.epcc.safe.accounting as database persisted objects using the classes from
uk.ac.ed.epcc.webapp.model.data
</p>
<p>
The database meta-data is an additional possible source of property-tag configuration.
A foreign key may be used to indicate a classification tag (though a policy will still be
needed to generate the values from the key property). 
Classification tables and manual allocation records will have some properties that 
are only modified by forms code. In this case we want to automatically generate propertyTags 
from the database schema.
</p>
**/
package uk.ac.ed.epcc.safe.accounting.db;