// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
/**
 * 
 */
package uk.ac.ed.epcc.safe.accounting.update;

import java.util.Iterator;
@uk.ac.ed.epcc.webapp.Version("$Id: StringSplitter.java,v 1.3 2014/09/15 14:32:29 spb Exp $")

/** Utility class to iterate over strings split using a pattern 
 * 
 * @author spb
 *
 */
public class StringSplitter implements Iterator<String>{
	final String res[];
	int pos=0;
	public StringSplitter(String u){
		//this(u,"(?:\\s*\\n)+");  // strip trailing whitespace and merge empty lines
		this(u,"\\n+");
	}
	public StringSplitter(String u, String split_pattern){
		if( u != null ){
		   res = u.split(split_pattern);
		}else{
			res = new String[0];
		}
	}
	public boolean hasNext() {
		return pos < res.length;
	}
	public String next() {
		return res[pos++];
	}
	public void remove() {
		throw new UnsupportedOperationException();
	}
}