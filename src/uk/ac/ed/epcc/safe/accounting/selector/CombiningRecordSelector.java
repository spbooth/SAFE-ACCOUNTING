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
package uk.ac.ed.epcc.safe.accounting.selector;

import java.util.Iterator;
import java.util.LinkedHashSet;

import uk.ac.ed.epcc.webapp.exceptions.ConsistencyError;
/** A class that encodes a selection expression for a set of UsageRecords.
 * 
 * Essentially this is an ordered set of SelectClause objects. We use an ordered set so that
 * different instances of the same selector don't randomly permute the order of the clauses in SQL 
 * statements improving the chance of query caching working.
 * 
 * The hash and equals methods compare the contents for equality so the combination becomes immutable once
 * the {@link #hashCode()} function is called to prevent the hash changing.
 * 
 * @author spb
 *
 */
public abstract class CombiningRecordSelector implements Iterable<RecordSelector>, RecordSelector {
	

	public Iterator<RecordSelector> iterator() {
		return new Iterator<RecordSelector>() {
			private Iterator<RecordSelector> nested = contents.iterator();

			public boolean hasNext() {
				return nested.hasNext();
			}

			public RecordSelector next() {
				return nested.next();
			}

			public void remove() {
				throw new UnsupportedOperationException("remove called on CombiningRecordSelector iterator");
				
			}
		};
	}


	private final LinkedHashSet<RecordSelector> contents;
	private boolean locked=false;
	private int hash=1;
	private final String combine_tag;
	/**
	 * 
	 */


	protected CombiningRecordSelector(String tag) {
		contents=new LinkedHashSet<>();
		combine_tag=tag;
	}

	protected CombiningRecordSelector(String tag,RecordSelector sel){
		this(tag);
		if( sel != null){
		   add(sel);
		}
	}
	
	public void lock(){
		this.locked=true;
		hash=contents.size();
		for(RecordSelector sel : contents){
			// combine with XOR so order of contents does not affect hash.
			hash ^= sel.hashCode();
		}
	}
	public boolean isLocked(){
		return locked;
	}
	/* (non-Javadoc)
	 * @see java.util.HashSet#add(java.lang.Object)
	 */
	public boolean add(RecordSelector arg0) {
		if(arg0 == null){
			throw new ConsistencyError("null selector added to RecordSelector");
		}
		
		if( locked ){
			throw new ConsistencyError("add called on locked CombiningSelector");
		}
		if( getClass().isAssignableFrom(arg0.getClass())){
			// merge contents if we are the same type or adding a sub-type
			LinkedHashSet<RecordSelector> more = ((CombiningRecordSelector)arg0).contents;
			boolean added=false;
			for(RecordSelector s : more){
				if( add(s)){
					added=true;
				}
			}
			return added;
		}
		return contents.add(arg0.copy());
	}
	public int size(){
		return contents.size();
	}

	

	public abstract CombiningRecordSelector copy();

	@Override
	public final int hashCode() {
		if( ! locked ){
			// we don't want the hash to change between calls so lock
			// on first use
			// the lock function calculates the hash value.
			lock();
			
		}
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if( this == obj ){
			return true;
		}
		if( obj==null || ! getClass().isAssignableFrom(obj.getClass()) || hashCode() != obj.hashCode()){
			return false;
		}
		CombiningRecordSelector peer = (CombiningRecordSelector) obj;
		if( size() != peer.size()){
			return false;
		}
		for(RecordSelector sel : contents){
			if( ! peer.contents.contains(sel)){
				return false;
			}
		}
		return true;
	}
	
	public String toString(){
		if( contents.size() == 1){
			return "("+contents.iterator().next().toString()+")";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for( Iterator<RecordSelector> it = contents.iterator(); it.hasNext();){
			sb.append(it.next().toString());
			if( it.hasNext()){
				sb.append(" ");
				sb.append(combine_tag);
				sb.append(" ");
			}
		}
		sb.append(")");
		return sb.toString();
	}

	
}