// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers.value;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
import uk.ac.ed.epcc.webapp.Description;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;
/** ValueParser for IndexedReference types.
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: IndexedReferenceValueParser.java,v 1.8 2014/09/15 14:32:25 spb Exp $")
@Description("Parse a type-safe reference")
public class IndexedReferenceValueParser implements ValueParser<IndexedReference>, Contexed {

	private AppContext c;
	
	public IndexedReferenceValueParser(AppContext c){
		this.c=c;
	}
	
	public Class<IndexedReference> getType() {
		
		return IndexedReference.class;
	}

	public IndexedReference parse(String valueString)
			throws ValueParseException {
		return IndexedReference.parseIndexedReference(c, valueString);
	}

	public String format(IndexedReference value) {
		return value.toString();
	}

	public AppContext getContext() {
		return c;
	}

}