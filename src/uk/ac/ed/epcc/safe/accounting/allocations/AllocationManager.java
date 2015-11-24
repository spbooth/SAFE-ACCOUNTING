// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.allocations;

import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.UsageProducer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTarget;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceTag;
import uk.ac.ed.epcc.safe.accounting.selector.RecordSelector;
import uk.ac.ed.epcc.webapp.content.Table;
import uk.ac.ed.epcc.webapp.forms.Form;
import uk.ac.ed.epcc.webapp.forms.exceptions.TransitionException;
import uk.ac.ed.epcc.webapp.forms.transition.ViewTransitionProvider;
import uk.ac.ed.epcc.webapp.model.period.SplitManager;
import uk.ac.ed.epcc.webapp.time.Period;
/** AllocationManagers are the UsageProducer classes for Allocations and
 * provide transitions to edit the allocations
 * 
 * @author spb
 *
 * @param <K> transition key
 * @param <T> type of {@link Allocation}
 */
public interface AllocationManager<K,T extends Allocation> extends UsageProducer<T>, ViewTransitionProvider<K,T>,SplitManager<T> {
 public static final String ALLOCATION_ADMIN = "AllocationAdmin";

 /** Add record to a table of allocations.
  * 
  * @param tab Table to add to
  * @param allocation {@link Allocation} to format (also used as row key)
  * @param template A set of filter properties used to generate the table. may be null.
 * @return modified table
  */
 public Table<String,T>  addIndexTable(Table<String,T>  tab, T allocation, PropertyTarget template);
 
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

 /** build a creation form that takes default index values.
  * 
  * @param f
  * @param p
  * @param defaults
 * @throws TransitionException 
  */
 public void buildCreationForm(Form f, Period p, PropertyMap defaults) throws TransitionException;
}