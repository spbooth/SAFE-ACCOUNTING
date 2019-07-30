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
/**
 * 
 */
package uk.ac.ed.epcc.safe.accounting.update;

import java.util.Iterator;


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
		this(u,"(?:\\r?\\n)+");  // Any number or CR-LF or LF
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