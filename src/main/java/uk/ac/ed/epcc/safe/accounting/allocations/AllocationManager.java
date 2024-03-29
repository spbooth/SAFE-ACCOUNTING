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
package uk.ac.ed.epcc.safe.accounting.allocations;

import java.util.Calendar;
import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTarget;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceTag;
import uk.ac.ed.epcc.safe.accounting.selector.PropertyTargetGenerator;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.Tagged;
import uk.ac.ed.epcc.webapp.content.Table;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.exceptions.TransitionException;
import uk.ac.ed.epcc.webapp.forms.transition.ViewTransitionProvider;
import uk.ac.ed.epcc.webapp.jdbc.filter.BaseFilter;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedProducer;
import uk.ac.ed.epcc.webapp.model.period.SplitManager;
import uk.ac.ed.epcc.webapp.session.SessionService;
import uk.ac.ed.epcc.webapp.time.Period;
import uk.ac.ed.epcc.webapp.time.ViewPeriod;
/** AllocationManagers are the factory classes for Allocations and
 * provide transitions to edit the allocations.
 * 
 * To allow common interface code to work with different types of allocation this extends
 * {@link PropertyTargetGenerator} 
 * 
 * 
 * @author spb
 *
 * @param <K> transition key
 * @param <T> type of {@link Allocation}
 */
public interface AllocationManager<K,T extends Allocation> extends  ViewTransitionProvider<K,T>,SplitManager<T>,Tagged,PropertyTargetGenerator<T>,IndexedProducer<T> {

/** Role needed to create allocations.
 *    Can restrict which index properties a user can set by defining a
 *    relationship {@link AllocationPeriodTransitionProvider#ALLOCATION_ADMIN_RELATIONSHIP} on the index prop.
 */
public static final String ALLOCATION_ADMIN_ROLE = "AllocationAdmin";

/** Role needed to only view allocations index. Additional roles are needed to access specific allocation types
 */
public static final String ALLOCATION_VIEW_ROLE = "AllocationView";

/** Relationship to edit an allocation.
 * If this is not defined on a table falls back to using the AllocationAdmin role
 * 
 */
public static final String EDIT_ALLOCATION_RELATIONSHIP = "AllocationEdit";
/** Relationship to view an allocation.
 * If this is not defined on a table falls back to using the AllocationAdmin role
 * 
 */
public static final String VIEW_ALLOCATION_RELATIONSHIP = "AllocationView";
 /** Add record to a table of allocations.
  * 
  * @param tab Table to add to
  * @param allocation {@link Allocation} to format (also used as row key)
  * @param template A set of filter properties used to generate the table. may be null.
 * @return modified table
  */
 public Table<String,T>  addIndexTable(Table<String,T>  tab, T allocation, PropertyTarget template);
 
 /** Apply final formatting to index table
  * 
  * @param tab
  * @param template
  */
 public void finishIndexTable(Table<String,T>  tab,  PropertyTarget template);
 /** Build a form for filtering the Allocations in an index page 
  * only used by view_allocations.jsp
  * @param f
  * @deprecated
  */
 public void buildFilterForm(Form f);
 
 /** Convert the filter form into a RecordSelector
  * only used by view_allocations.jsp
  * @param f
  * @return RecrodSelector
  * @deprecated
  */
 public RecordSelector getFilterSelector(Form f); 
 /** get the set of properties used to classify allocations
	 * 
	 * @return Set<ReferenceTag>
	 */
 public Set<ReferenceTag> getIndexProperties();

 /** Get a filter representing which allocations 
  * the current user is allowed to see.
  * 
  * @param sess {@link SessionService}
  * @return {@link BaseFilter} on the allocations
  */
 public BaseFilter<T> getViewFilter(SessionService sess);
 /** build a creation form that takes default index values.
  * 
  * @param f
  * @param p
  * @param defaults
 * @throws TransitionException 
  */
 public void buildCreationForm(Form f, Period p, PropertyMap defaults) throws TransitionException;
 
 public AllocationPeriodTransitionProvider getTransitionProvider();
 
 /** Get the name for the type to use in messages etc.
  * 
  * @return
  */
 public String getTypeName();
 
 /** get a default {@link ViewPeriod} appropriate to this type of allocation
  * 
  * @return
  */
 public ViewPeriod getDefaultViewPeriod();
 
 /** get the finest resolution {@link Calendar} field to use in date inpus
  * 
  * @return
  */
 public int getMinDateField();
}