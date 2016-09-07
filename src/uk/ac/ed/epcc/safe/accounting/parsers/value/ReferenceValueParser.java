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
package uk.ac.ed.epcc.safe.accounting.parsers.value;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Indexed;
import uk.ac.ed.epcc.webapp.logging.Logger;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.Classification;
import uk.ac.ed.epcc.webapp.model.ClassificationFactory;
import uk.ac.ed.epcc.webapp.model.NameFinder;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedProducer;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;

/** A format tolerant ValueParser for IndexedReference.
 * 
 * This class should be capable of handling text formatted by the {@link DefaultFormatter}
 * 
 * @author spb
 *
 */


public class ReferenceValueParser<I extends Indexed> implements ValueParser<IndexedReference> {

	IndexedProducer<I> producer;
	AppContext c;
	public ReferenceValueParser(AppContext c, IndexedProducer<I> producer){
		this.c=c;
		this.producer=producer;
		assert(producer != null );
	}
	public Class<IndexedReference> getType() {
	
		return IndexedReference.class;
	}

	@SuppressWarnings("unchecked")
	public IndexedReference parse(String valueString) throws ValueParseException {
		Logger log = c.getService(LoggerService.class).getLogger(getClass());
		if( valueString == null ){
			return null;
		}
		valueString=valueString.trim();
		if( valueString.length() == 0){
			return null;
		}
		IndexedReference ref = IndexedReference.parseIndexedReference(c, valueString);
		if( ref != null ){
			if( ref.isNull()){
				// This is probably as a result of parameter expansion using the expression format. Treat null references as wild-cards same as
				// missing parameters.
				return null;
			}
			if( producer.isMyReference(ref)){
				return ref;
			}
			throw new ValueParseException("Reference parsed to wrong type "+valueString+" expecting "+producer.getTarget().getName()+" got "+ref.toString());
		}
		// Try a simple integer parse first as we can determine if not
		// an int without doing a database lookup.
		try {
			return producer.makeReference(producer.find(Integer.parseInt(valueString)));
		}catch(NumberFormatException nf){
			// not a simple integer value then
		} catch (Exception e) {
			// An integer but not a valid one.
			log.debug("Error parsing as int "+valueString, e);
			
		}
		// Attempt a simple name lookup
		if( producer instanceof NameFinder){
			NameFinder cf = (NameFinder) producer;
			Indexed c = cf.findFromString(valueString);
			if( c != null ){
			  return producer.makeReference((I) c);
			}
		}
		throw new ValueParseException("Cannot parse to IndexedReference: "+valueString);
		
	}

	public String format(IndexedReference value) {
		return value.toString();
	}

}