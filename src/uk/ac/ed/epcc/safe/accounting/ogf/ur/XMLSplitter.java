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
package uk.ac.ed.epcc.safe.accounting.ogf.ur;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;


/**
 * <p>
 * This content handler expects a single OGF <code>UsageRecords</code> XML
 * element as specified by the OGF Usage Record specification. The XML format
 * of this element can be found in the OGF specification.
 * </p>
 * <p>
 * This content handler is slightly more liberal than the specification
 * requires. The specification states that multiple <code>UsageRecord</code>
 * elements should be contained in a single <code>UsageRecords</code> element.
 * This parser does not enforce that restriction. In fact, this parser ignores
 * anything outside of a <code>UsageRecord</code> element. Each element
 * returned by this parser's iterator is simply all text in a
 * <code>UsageRecord</code> element including the start and end
 * &lt;UsageRecord&gt; tags. If any namespaces were declared in the parents of
 * a <code>UsageRecord</code> element, they are added to the
 * <code>UsageRecord</code> element's opening tag. As a result, each element
 * returned by the iterator should be a valid XML document in itself, although
 * it will be missing the optional XML header (the <code>&lt;?xml
 * version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;<code> bit)
 * </p>
 * 
 * @author jgreen4
 * 
 */


public class XMLSplitter implements ContentHandler {
	/**
	 * The recorder that will record the text of each OGF Usage Record element
	 * appropriately
	 */
	private XMLTextRecorder recorder;
	/**
	 * A collection of all the usage records which have been extracted
	 */
	Queue<String> usageRecordQueue;
	/**
	 * Marker noting if the content handler is currently working
	 */
	boolean running;
	String targets[]= {"UsageRecord","JobUsageRecord","Usage"};

	/**
	 * Constructs a new {@link XMLSplitter}.
	 * 
	 */
	public XMLSplitter() {
		/*
		 * We know we're going to be dealing with large elements, so make sure the
		 * text buffer doesn't have to be resized often by setting a good minimum
		 * size
		 */
		this.recorder = new XMLTextRecorder(1024);
		this.usageRecordQueue = new LinkedList<String>();
	}
	public XMLSplitter(String targets[]){
		this();
		this.targets=targets;
	}

	/**
	 * 
	 * @return An iterator that will iterate over all the usage records this
	 *         content handler obtained by splitting the contents it parsed.
	 *         This iterator may be fetched before parsing but it will not
	 *         return any elements until after parsing.
	 */
	public Iterator<String> iterator() {
		return this.usageRecordQueue.iterator();
	}

	// ////////////////////////////////////////////////////////////////////////
	// ContentHandler START AND END DOCUMENT METHODS
	// ////////////////////////////////////////////////////////////////////////

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#startDocument()
	 */
	public void startDocument() throws IllegalStateException {
		if (this.running)
			throw new IllegalStateException(
			"Cannot start parsing.  Parsing has already begun");

		this.running = true;
		this.usageRecordQueue.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#endDocument()
	 */
	public void endDocument() throws SAXException {
		this.running = false;

		if (this.recorder.isRecording())
			throw new SAXException(new IllegalStateException(
					"Parsing ended while the content handler was recording a usage record "
					+ "element"));
	}

	// ////////////////////////////////////////////////////////////////////////
	// ContentHandler METHODS
	// ////////////////////////////////////////////////////////////////////////

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length) {
		this.recorder.characters(ch, start, length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String localName, String name) {
		boolean recordedEndElement = this.recorder.endElement(name);
		boolean stoppedRecording = !this.recorder.isRecording();

		/*
		 * If the recorder recorded the end of the element but is now no longer
		 * recording, we have reached the end of a record. As such, it should be
		 * added to the list
		 */
		if (recordedEndElement && stoppedRecording) {
			boolean accepted = this.usageRecordQueue.offer(this.recorder.getText());

			assert accepted : "unable to add the next usage record to the "
				+ "buffer of read usage records for some reason";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
	 * java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String name,
			Attributes atts) {
		if (this.isUsageRecordElement(localName)){
			this.recorder.startRecording(name, localName, atts);
		}else{
			this.recorder.startElement(name, atts);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String,
	 * java.lang.String)
	 */
	public void startPrefixMapping(String prefix, String uri) {
		this.recorder.startPrefixMapping(prefix, uri);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
	 */
	public void endPrefixMapping(String prefix) {
		this.recorder.endPrefixMapping(prefix);

	}

	// ////////////////////////////////////////////////////////////////////////
	// Content Handler METHODS NOT USED
	// ////////////////////////////////////////////////////////////////////////

	/**
	 * Not used
	 */
	public void ignorableWhitespace(char[] ch, int start, int length) {
		// Not used
	}

	/**
	 * Not used
	 */
	public void processingInstruction(String target, String data) {
		// Not used
	}

	/**
	 * Not used
	 */
	public void skippedEntity(String name) {
		// Not used
	}

	/**
	 * Not used
	 */
	public void setDocumentLocator(Locator locator) {
		// Not used
	}

	// ////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	// ////////////////////////////////////////////////////////////////////////

	/**
	 * Convenience method for checking if an element is a Usage Record element -
	 * an operation performed many times in this class
	 * 
	 * @param name
	 *          The element name to test to see if it represents a usage record
	 *          element
	 * @return <code>true</code> if <code>name</code> is a Usage Record element
	 *         name. <code>false</code> otherwise.
	 */
	private boolean isUsageRecordElement(String name) {
		for(String s: targets){
			if( s.equals(name) ){
				return true;
			}
		}
		return false;
	}
}