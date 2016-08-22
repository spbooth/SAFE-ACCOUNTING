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
/**
Classes to encode simple expressions over properties.

Though {@link uk.ac.ed.epcc.safe.accounting.properties.PropertyTag} is itself an
expression this package defines additional more complex expressions such as:
<ul>
<li> Simple mathematical expressions over numerical properties</li>
<li>Date to millisecond conversion</li>
<li> Dereference expressions that access the properties of a referenced {@link uk.ac.ed.epcc.safe.accounting.properties.PropertyTarget}</li>
</ul>

**/
package uk.ac.ed.epcc.safe.accounting.expr;