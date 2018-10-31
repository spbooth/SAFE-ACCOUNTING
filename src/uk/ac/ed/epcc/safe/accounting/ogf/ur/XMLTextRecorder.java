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

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.xml.sax.Attributes;

/**
 * <p>
 * This is essentially a {@link StringBuilder} for XML SAX parsers. The
 * <code>XMLTextRecorder</code> is used to record the contents of an XML
 * element. From now on, the element being recorded is referred to as the
 * <em>enclosing element</em>. All information contained within the XML document
 * should be forwarded to the recorder with the appropriate methods regardless
 * of whether or not it is recording. The recorder will determine if the
 * information should be recorded or not. This object is <b>not</b> thread safe
 * and should only be used by one SAX parser at a time.
 * </p>
 * <p>
 * At some point, a parser can switch this object on. The forwarded character,
 * namespace mappings and element start and end information is then recorded.
 * This object will save the information as text - essentially an XML excerpt -
 * until it is told to stop recording. Text is only saved when the
 * <code>XMLTextRecorder</code> has been told to record.
 * </p>
 * <p>
 * The <code>XMLTextRecorder</code> provides methods with the same name as
 * methods in the SAX API. These are the methods information should be forwarded
 * to. Unlike the SAX API methods, these methods have a boolean return type. The
 * boolean represents whether something was actually recorded during the method
 * call. If the <code>XMLTextRecorder</code> is recording, it will return
 * <code>true</code>, otherwise, it will return <code>false</code>. Note that
 * the {@link #endElement(String)} method can return <code>true</code> even if
 * recording stops after the method returns. This is because the end element tag
 * was recorded and the return value reflects this. A call to
 * {@link #isRecording()} would be required to tell if the
 * <code>XMLTextRecorder</code> will record something on the next record method
 * invocation.
 * </p>
 * <p>
 * A <code>XMLTextRecorder</code> is started when a particular element is
 * encountered. It records until the element is exited. A SAX content handler
 * must explicitly start the <code>XMLTextRecorder</code> with the
 * {@link #startRecording(String, String, Attributes) startRecording} method but
 * it does not have to stop it; the recorder will count the number of elements
 * it enters and leaves to determine when it should stop. Even when the recorder
 * is not recording, it notes prefix declarations which it may have to include
 * when it is started. This is necessary to ensure the recorded text is itself a
 * valid XML document with all prefixes declared. It is therefore important to
 * forward all information to the <code>XMLTextRecorder</code>.
 * </p>
 * <p>
 * The start and end tag of the enclosing element may, or may not be recorded.
 * The appropriate behaviour may be set at construction time with the
 * appropriate constructor (see the constructors for more detail). This setting
 * can greatly alter the text which is recorded and does not just determine if
 * the text is wrapped in start and end tags. To make sure namespaces are
 * declared properly, the recorder makes a note of namespace prefix starts and
 * ends regardless of whether or not it is recording. If the enclosing element
 * is included, all namespace declarations made before recording was started are
 * added to the enclosing element's start tag, along with any that were there
 * already. If, however the enclosing element is not recorded, these namespace
 * declarations, along with those made within the enclosing element tag will be
 * added to all children (but not other descendants) of the enclosing element.
 * This could make the recorded text considerably longer but may be a desirable
 * result. If the enclosing element is not recorded, it's local name, qualified
 * name and attributes are still stored by the recorder and can be retrieved for
 * further processing by various getter methods. However, they will not present
 * in the recorded text.
 * </p>
 * 
 * @author jgreen4
 * 
 */


public class XMLTextRecorder {
	/**
	 * The string builder to hold the text
	 */
	private StringBuilder text;
	/**
	 * The attributes of the element this recorder was started on
	 */
	private Attributes attributes;
	/**
	 * The qualified name of the element this recorder was started on
	 */
	private String elementName;
	/**
	 * The local name of the element this recorder was started on
	 */
	private String elementLocalName;

	/**
	 * A counter to count how many elements have been entered since the recorder
	 * started. When it is at 0, recording doesn't take place
	 */
	private int elementCounter;

	/**
	 * A list of namespaces that were added before recording began. If the
	 * enclosing element is recorded, these will needed to be added to that.
	 * Otherwise, they will have to be added to each direct child of the recorded
	 * element
	 */
	private Map<String, String> externalNamespaces;

	/**
	 * A list of namespaces that have been registered since recording began and
	 * need to be added to the next element
	 */
	private Map<String, String> internalNamespaces;

	private boolean recordEnclosingElement = true;

	/**
	 * {@link java.lang.StringBuilder StringBuilders} start with an initial
	 * capacity of 16. It is possible that this recorder will need more than that
	 * so this field sets a value can be set so the recorder doesn't hog too much
	 * memory and is a better reflection on the minimum size of text this recorder
	 * records. This will improve efficiency slightly as less array copies occur
	 * as the text {@link java.lang.StringBuilder StringBuilder} will run out of
	 * space less often
	 */
	private final int STRING_BUILDER_OPTIMISE_SIZE;
	/**
	 * Default value for the string builder size if none is specified
	 */
	private static final int STRING_BUILDER_OPTIMISE_SIZE_DEFAULT = 16;

	/**
	 * Constructs a new <code>XMLTextRecorder</code>. The
	 * <code>XMLTextRecorder</code> is not set to record upon construction.
	 * {@link #startRecording(String, String, Attributes)} must be explicitly
	 * called for that. The enclosing element will be recorded and text buffer
	 * size will be set to {@value #STRING_BUILDER_OPTIMISE_SIZE_DEFAULT}.
	 * 
	 * @see #XMLTextRecorder(boolean, int)
	 * @see #startRecording(String, String, Attributes)
	 */
	public XMLTextRecorder() {
		this(true, STRING_BUILDER_OPTIMISE_SIZE_DEFAULT);
	}

	/**
	 * Constructs a new <code>XMLTextRecorder</code>. The
	 * <code>XMLTextRecorder</code> is not set to record upon construction.
	 * {@link #startRecording(String, String, Attributes)} must be explicitly
	 * called for that.
	 * 
	 * @param textBufferSize
	 *          The size the the text buffer should be initialised to when
	 *          recording. If large elements will often be recorded, this object
	 *          will be more efficient if <code>textBufferSize</code> is roughly
	 *          about the size of the number of characters the the element will
	 *          hold.
	 * @throws NegativeArraySizeException
	 *           if <code>textBufferSize</code> is negative
	 * @see #XMLTextRecorder(boolean, int)
	 * @see #startRecording(String, String, Attributes)
	 */
	public XMLTextRecorder(int textBufferSize) throws NegativeArraySizeException {
		this(true, textBufferSize);
	}

	/**
	 * Constructs a new <code>XMLTextRecorder</code>. The
	 * <code>XMLTextRecorder</code> is not set to record upon construction.
	 * {@link #startRecording(String, String, Attributes)} must be explicitly
	 * called for that.
	 * 
	 * @param recordEnclosingElement
	 *          If <code>true</code> the tag of the enclosing element will be
	 *          included with the recorded text. If set to <code>false</code> it
	 *          won't.
	 * @throws NegativeArraySizeException 
	 *          
	 * @see #XMLTextRecorder(boolean, int)
	 * @see #startRecording(String, String, Attributes)
	 */
	public XMLTextRecorder(boolean recordEnclosingElement)
			throws NegativeArraySizeException {
		this(recordEnclosingElement, STRING_BUILDER_OPTIMISE_SIZE_DEFAULT);
	}

	/**
	 * Constructs a new <code>XMLTextRecorder</code>. The
	 * <code>XMLTextRecorder</code> is not set to record upon construction.
	 * {@link #startRecording(String, String, Attributes)} must be explicitly
	 * called for that. Using this constructor, one can decide whether or not to
	 * include the enclosing element's tags in the recorded text. See the
	 * documentation for this class for a description of the implications of (not)
	 * recording the enclosing element's tags.
	 * 
	 * @param recordEnclosingElement
	 *          If <code>true</code> the tag of the enclosing element will be
	 *          included with the recorded text. If set to <code>false</code> it
	 *          won't.
	 * 
	 * @param textBufferSize
	 *          The size the the text buffer should be initialised to when
	 *          recording. If large elements will often be recorded, this object
	 *          will be more efficient if <code>textBufferSize</code> is roughly
	 *          about the size of the number of characters the the element will
	 *          hold.
	 * 
	 * @throws NegativeArraySizeException
	 *           if <code>textBufferSize</code> is negative
	 * @see #XMLTextRecorder()
	 * @see #startRecording(String, String, Attributes)
	 */
	public XMLTextRecorder(boolean recordEnclosingElement, int textBufferSize)
			throws NegativeArraySizeException {
		this.STRING_BUILDER_OPTIMISE_SIZE = textBufferSize;
		this.text = new StringBuilder(textBufferSize);
		this.attributes = new BlankAttributes();
		this.elementCounter = 0;
		this.externalNamespaces = new TreeMap<>();
		this.internalNamespaces = new TreeMap<>();
		this.recordEnclosingElement = recordEnclosingElement;
	}

	/**
	 * Record the specified text if this <code>XMLTextRecorder</code> is currently
	 * recording
	 * 
	 * @param text
	 *          The text to record
	 * @return <code>true</code> if any text was recorded. <code>false</code>
	 *         otherwise
	 */
	public boolean characters(String text) {
		if (this.isRecording()) {
			String txt = text.replace("&","&amp;");
			// Next two are legal xml but not html below 4
			// not strictly illegal 
			//txt=txt.replace("\"", "&quot;");
			//txt=txt.replace("\'","&apos;");
			txt=txt.replace("<","&lt;");
			txt=txt.replace(">", "&gt;");
			this.text.append(txt);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Record the specified text if this <code>XMLTextRecorder</code> is currently
	 * recording
	 * 
	 * @param ch
	 *          The <code>char</code> array to read characters from
	 * @param start
	 *          The starting element in <code>ch</code> to read text from.
	 * @param length
	 *          The number of characters to read from <code>ch</code>
	 * @return <code>true</code> if any text was recorded. <code>false</code>
	 *         otherwise
	 */
	public boolean characters(char[] ch, int start, int length) {
		if (this.isRecording()) {
			String text = new String(ch, start, length);
			characters(text);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Records the ending of an element. Note, that calling this method may result
	 * in the recorder stopping it's recording. In such a situation the method
	 * will return <code>true</code>. This behaviour occurs because the event of
	 * an element ending was recorded. However, all subsequent methods called to
	 * record information will return <code>false</code>, as will the
	 * {@link #isRecording()}, until the
	 * {@link #startRecording(String, String, Attributes)} method is called again.
	 * The <code>true</code> return value is appropriate for the reasons described
	 * above but may not be what one would expect when first considering the
	 * problem.
	 * 
	 * @param elementName
	 *          The name of the element that has ended
	 * @return <code>true</code> if the ending of the element was recorded.
	 *         <code>false</code> otherwise
	 */
	public boolean endElement(String elementName) {
		if (this.isNotRecording())
			return false;

		this.elementCounter--;

		// If we're still recording, we need to record the closing tag
		if (this.isRecording() || this.recordEnclosingElement)
			text.append("</").append(elementName).append(">");

		// Something was recorded so we return true
		return true;
	}

	/**
	 * Record the ending of a prefix string mapping to a particular namespace
	 * 
	 * @param prefix
	 *          The prefix that no longer maps to a namespace
	 * @return <code>true</code>. This method will always remove namespace
	 *         mappings from this namespace mapping list if they exist, regardless
	 *         of whether or not the recorder is recording
	 */
	public boolean endPrefixMapping(String prefix) {
		if (this.isRecording())
			this.internalNamespaces.remove(prefix);
		else
			this.externalNamespaces.remove(prefix);

		return true;
	}

	/**
	 * <p>
	 * Gets the map of attributes recorded when the recorder started. The map is a
	 * key value pair of attribute name to attribute value. If the names have
	 * prefixes, they are removed.
	 * </p>
	 * 
	 * <p>
	 * The map returned is a copy of the attributes recorded. It is fully
	 * modifiable and modifying it's contents will not alter the set of attributes
	 * stored in this recorder
	 * </p>
	 * 
	 * <p>
	 * The returned map will never be null, even if the <code>Attributes</code>
	 * object specified when recording started was null. In that situation, an
	 * empty map will be returned.
	 * </p>
	 * 
	 * @return A map of attribute names to values. Will never return null (see
	 *         above)
	 */
	public Map<String, String> getAttributes() {
		Map<String, String> attributesMap = new HashMap<>();
		int length = attributes.getLength();
		for (int i = 0; i < length; i++) {
			// Strip of any prefixes.
			String name = attributes.getQName(i);
			if (name.contains(":"))
				name = name.substring(name.indexOf(":") + 1);

			attributesMap.put(name, attributes.getValue(i));
		}

		return attributesMap;
	}

	/**
	 * returns the local name of the element in which recording was started. This
	 * value is not reset when recording stops. It keeps it's old value until the
	 * recorder is restarted with the
	 * {@link #startRecording(String, String, Attributes)} method.
	 * 
	 * @return The local name of the first element
	 */
	public String getElementLocalName() {
		return this.elementLocalName;
	}

	/**
	 * returns the qualified name of the element in which recording was started.
	 * This value is not reset when recording stops. It keeps it's old value until
	 * the recorder is restarted with the
	 * {@link #startRecording(String, String, Attributes)} method.
	 * 
	 * @return The name of the first element
	 */
	public String getElementName() {
		return this.elementName;
	}

	/**
	 * <p>
	 * The text that has been recorded while this recorder has been recording.
	 * This value is not reset when recording stops. It keeps it's old value until
	 * the recorder is restarted with the
	 * {@link #startRecording(String, String, Attributes)} method.
	 * </p>
	 * 
	 * <p>
	 * The style of text returned depends on how this recorder was constructed. If
	 * the enclosing element is being recorded, the starting and end tag of that
	 * element will enclose the text. The attributes of the enclosing element and
	 * the namespaces declared before recording started will be declared in this
	 * starting tag. If the enclosing element is not being recorded, only
	 * sub-elements will be returned. Each direct descendant of the enclosing
	 * element will contain all namespace declarations that were declared before
	 * recording started and in the enclosing element itself.
	 * </p>
	 * 
	 * <p>
	 * One can call this method while the recorder is recording, however, the
	 * caller should note that the text returned may not be the complete contents
	 * of the element currently being recorded
	 * </p>
	 * 
	 * @return String
	 */
	public String getText() {
		return this.text.toString();
	}

	/**
	 * Mimics the <code>ignorableWhitespace</code> method from the
	 * {@link org.xml.sax.ContentHandler ContentHandler} class. This
	 * implementation currently calls the {@link #characters(char[], int, int)}
	 * method.
	 * 
	 * @param ch
	 *          the characters from the XML document
	 * @param start
	 *          the start position in the array
	 * @param length
	 *          the number of characters to read from the array
	 * @return <code>true</code> if any text was recorded. <code>false</code>
	 *         otherwise.
	 */
	public boolean ignorableWhitespace(char[] ch, int start, int length) {
		return this.characters(ch, start, length);
	}

	/**
	 * A convenience method. Returns the opposite of {@link #isRecording()}.
	 * 
	 * @return <code>true</code> if this recorder is not currently recording text,
	 *         element names, attributes and internalNamespaces.
	 *         <code>false</code> otherwise
	 */
	public boolean isNotRecording() {
		return this.elementCounter <= 0;
	}

	/**
	 * Determines whether or not this recorder is still recording. Recording is
	 * started explicitly by calling the
	 * {@link #startRecording(String, String, Attributes)} method. However, the
	 * recorder is not explicitly stopped. It counts the number of elements it
	 * enters and leaves and determines when it has left the element it started
	 * recording in. At this point, it stops itself. This method is useful for
	 * checking if the recorder has indeed stopped which indicated the parser has
	 * left the element in which the recording started.
	 * 
	 * @return <code>true</code> if this recorder is currently recording text,
	 *         element names, attributes and internalNamespaces.
	 *         <code>false</code> otherwise
	 */
	public boolean isRecording() {
		return this.elementCounter > 0;
	}

	

	/**
	 * Records the starting of an element. This method does not start the
	 * recorder. The recorder is started explicitly by calling one of the
	 * <code>startRecording</code> methods. This method should <em>not</em> be
	 * called for the same element one of the <code>startRecording</code> methods
	 * was called for. This will lead to incorrect behaviour. Subsequent arrivals
	 * at elements should use this method to note they have entered a new element.
	 * Calling this method while the recorder is not recording is not
	 * inappropriate, however in this situation, this method will do nothing and
	 * return <code>false</code>.
	 * 
	 * @param elementName
	 *          The qualified name of the element that was started
	 * @param atts
	 *          The attributes the element contained
	 * @return <code>true</code> if this recorder is currently recording.
	 *         <code>false</code> otherwise
	 */
	public boolean startElement(String elementName, Attributes atts) {
		if (this.isNotRecording())
			return false;

		text.append("<").append(elementName);

		// if we didn't record the external namespaces in the enclosing element
		if (this.recordEnclosingElement == false)
			// if this element is a direct descendant of the recorded element
			if (this.elementCounter == 1)
				// If there are external namespaces to record
				if (this.externalNamespaces.isEmpty() == false)
					// record the namespaces in this element
					text.append(getNamespaceString(this.externalNamespaces));

		if (this.internalNamespaces.isEmpty() == false) {
			text.append(getNamespaceString(this.internalNamespaces));
			// internalNamespaces have been recorded now so they don't need to be
			// stored any
			// more
			this.internalNamespaces.clear();
		}

		text.append(getAttributesString(atts)).append(">");

		this.elementCounter++;

		// Something was recorded so we return true
		return true;
	}

	/**
	 * Record the starting of a prefix string mapping to a particular namespace.
	 * Note: mappings are only recorded and used within the element that is being
	 * recorded. This method can be used to add prefix mappings when the recorder
	 * is not recording. The result in this instance is that more
	 * internalNamespaces will be declared in the starting element
	 * 
	 * @param prefix
	 *          The prefix that now maps to the specified namespace
	 * @param uri
	 *          The namespace <code>prefix<code> now maps to.
	 * @return <code>true</code>. This method will always add namespace mappings
	 *         and apply them to the first element it can regardless of whether or
	 *         not the recorder is recording. Of cause, the recorder cannot add
	 *         the namespaces to an element until it starts recording so
	 *         internalNamespaces added and removed before recording starts will
	 *         not show up on the first element when recording does start.
	 */
	public boolean startPrefixMapping(String prefix, String uri) {
		if (this.isRecording())
			this.internalNamespaces.put(prefix, uri);
		else
			this.externalNamespaces.put(prefix, uri);

		return true;
	}

	/**
	 * Starts this recorder recording the contents of the specified element. Note:
	 * the start and end tag of the element specified are not recorded in this
	 * recorders text recorder. The text recorded is the contents of the element
	 * only. The starting element's local name, qualified name and attributes are
	 * recorded by this recorder separately from the text and can be retrieved
	 * with the appropriate getter methods.
	 * 
	 * @see #getAttributes()
	 * @see #getElementLocalName()
	 * @see #getElementName()
	 * @param name
	 *          The qualified name of the element whose contents are being
	 *          recorded
	 * @param localName
	 *          The local name of the element whose contents are being recorded
	 * @param atts
	 *          Any attributes the element has. Can be <code>null</code>
	 */
	public void startRecording(String name, String localName, Attributes atts) {
		if (atts == null)
			atts = new BlankAttributes();

		if (this.isRecording())
			throw new IllegalStateException("Recording already in progress");

		this.elementCounter = 1;
		this.text = new StringBuilder(STRING_BUILDER_OPTIMISE_SIZE);
		this.attributes = atts;
		this.elementName = name;
		this.elementLocalName = localName;

		if (this.recordEnclosingElement) {
			this.text.append("<").append(name);
			if (this.externalNamespaces.isEmpty() == false)
				text.append(getNamespaceString(this.externalNamespaces));

			this.text.append(getAttributesString(atts)).append(">");
		}
	}

	/*
	 * ##########################################################################
	 * PRIVATE METHODS & CLASSES
	 * ##########################################################################
	 */

	/**
	 * This is a utility method. It generates a <code>String</code> of prefix to
	 * namespace mappings to be used as attributes in a starting element tag and
	 * returns it. The returned string will begin with a space character.
	 * 
	 * @param namespace
	 *          The namespaces (name to value map) to be converted to a
	 *          <code>String</code>
	 * @return The string representation of <code>namespace</code>
	 */
	private static String getNamespaceString(Map<String, String> namespace) {
		StringBuilder sb = new StringBuilder();

		for (Map.Entry<String, String> entry : namespace.entrySet()) {
			String prefix = entry.getKey();
			String uri = entry.getValue();

			if (prefix.equals(""))
				sb.append(" xmlns='").append(uri).append("'");
			else
				sb.append(" xmlns:").append(prefix).append("='").append(uri)
						.append("'");
		}

		return sb.toString();
	}

	/**
	 * This is a utility method. It generates a <code>String</code> of attribute
	 * name to value mappings to be used as attributes in a starting element tag
	 * and returns it. The returned string will begin with a space character.
	 * 
	 * @param atts
	 *          The attributes to convert to <code>String</code> form
	 * @return The string representation of <code>atts</code>
	 */
	private static String getAttributesString(Attributes atts) {
		StringBuilder sb = new StringBuilder();

		int attsLen = atts.getLength();
		for (int a = 0; a < attsLen; a++)
			sb.append(" ").append(atts.getQName(a)).append("='").append(
					atts.getValue(a)).append("'");

		return sb.toString();
	}

	/**
	 * A basic implementation of the {@link org.xml.sax.Attributes Attributes}
	 * interface that represents an empty set of attributes
	 * 
	 * @author jgreen4
	 * 
	 */
	private static class BlankAttributes implements Attributes {

		public int getIndex(String name) {
			return -1;
		}

		public int getIndex(String uri, String localName) {
			return -1;
		}

		public int getLength() {
			return 0;
		}

		public String getLocalName(int index) {
			return null;
		}

		public String getQName(int index) {
			return null;
		}

		public String getType(int index) {
			return null;
		}

		public String getType(String name) {
			return null;
		}

		public String getType(String uri, String localName) {
			return null;
		}

		public String getURI(int index) {
			return null;
		}

		public String getValue(int index) {
			return null;
		}

		public String getValue(String name) {
			return null;
		}

		public String getValue(String uri, String localName) {
			return null;
		}
	}

}