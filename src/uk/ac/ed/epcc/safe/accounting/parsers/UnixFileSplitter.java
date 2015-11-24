// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers;

import uk.ac.ed.epcc.safe.accounting.update.StringSplitter;

@uk.ac.ed.epcc.webapp.Version("$Id: UnixFileSplitter.java,v 1.6 2014/09/15 14:32:25 spb Exp $")


public class UnixFileSplitter extends StringSplitter {

	public UnixFileSplitter(String u) {
		// any number of line ends with optional whitespace and
		// trailing comment
		super(u,"(?:\\s*(?:#.*)?\\n)+");
	}

}