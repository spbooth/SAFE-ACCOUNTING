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
package uk.ac.ed.epcc.safe.accounting.reports;

import uk.ac.ed.epcc.safe.accounting.reports.exceptions.ReportException;

/** Converter interface used by the {@link ParameterExtension} to convert a single parameter object into an array of values.
 * 
 * Returning a null value from {@link #split(Object)} indicates the
 * output should be skipped without error. For example if an optional parameter
 * is being expanded (the splitter sees this as a null input). however the splitter can
 * descide to provide a default expansion instead.
 * @author spb
 *
 * @param <I> input type
 * @param <O> output type
 */
public interface Splitter<I,O> {

	/**
	 * @param input input parameter (may be null)
	 * @return  array of values. 
	 * 
	 */
	public O[] split(I input) throws ReportException;
}