// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers;

import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParseException;
import uk.ac.ed.epcc.safe.accounting.parsers.value.ValueParser;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyContainer;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyMap;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;

/**
 * Links a <code>PropertyTag</code> with a <code>ValueParser</code>. The the
 * purpose of the <code>PropertyEntryMaker</code> it to parse a string and
 * construct an object using the data contained in the string. The object will
 * have the same datatype as the one supported by the <code>PropertyTag</code>
 * and thus the <code>PropertyTag</code> and generated object may be used as a
 * key-value pair in a <code>PropertyContainer</code>.
 * 
 * 
 * @author jgreen4
 * 
 * @param <T>
 *          Type of the <code>PropertyTag</code> and the type of the generated
 *          object
 */
@uk.ac.ed.epcc.webapp.Version("$Id: PropertyEntryMaker.java,v 1.15 2014/09/15 14:32:25 spb Exp $")

public class PropertyEntryMaker<T> implements ContainerEntryMaker {
	/**
	 * The <code>PropertyTag</code> this maker is associated with
	 */
	PropertyTag<T> tag;

	/**
	 * The parser used to generate the object of type T from a given string
	 */
	ValueParser<? extends T> parser;

	/**
	 * 
	 * Constructs a new <code>PropertyEntryMaker</code> around an existing
	 * <code>PropertyTag</code>.
	 * 
	 * @param tag
	 *          The <code>PropertyTag</code> this maker may parse values for
	 * @param parser
	 *          The parser used to generate the object of type T from a given
	 *          string
	 * @throws NullPointerException
	 *           If <code>tag</code> or <code>parser</code> are <code>null</code>
	 */
	public PropertyEntryMaker(PropertyTag<T> tag, ValueParser<? extends T> parser)
			throws NullPointerException {
		if (tag == null)
			throw new NullPointerException("tag cannot be null");
		if (parser == null)
			throw new NullPointerException("parser cannot be null");

		this.tag = tag;
		this.parser = parser;
	}

	/**
	 * Returns the name of this <code>PropertyEntryMaker</code>, which will be the
	 * same as the name of it's <code>PropertyTag</code>.
	 * 
	 * @return The name of this <code>PropertyEntryMaker</code>
	 */
	public String getName() {
		return this.tag.getName();
	}

	/**
	 * Returns the parser this <code>PropertyEntryMaker</code> uses to generate
	 * objects of type T from string values.
	 * 
	 * @return This <code>PropertyEntryMaker</code>'s parser
	 */
	public ValueParser<? extends T> getParser() {
		return this.parser;
	}

	/**
	 * Returns the <code>PropertyTag</code> this maker parses values for.
	 * 
	 * @return This <code>PropertyEntryMaker</code>'s <code>PropertyTag</code>
	 */
	public PropertyTag<T> getPropertyTag() {
		return this.tag;
	}

	/**
	 * The type of class this <code>PropertyEntryMaker</code> parses string to
	 * 
	 * @return This <code>PropertyEntryMaker</code>'s datatype
	 */
	public Class<? super T> getTarget() {
		return this.tag.getTarget();
	}

	/**
	 * Parses the specified string and adds the generated value to
	 * <code>container</code> using this <code>PropertyEntryMaker</code>'s
	 * <code>PropertyTag</code> as the key
	 * 
	 * @param contanier
	 *          The container to add this parser's <code>PropertyTag</code> and
	 *          parsed value to
	 * @param valueString
	 *          The string to parse and convert into an object of type T
	 * @throws IllegalArgumentException
	 *           If <code>valueString</code> is not of a format that allows this
	 *           maker to convert it into an object of type T
	 * @throws InvalidPropertyException
	 *           If the container cannot contain this parser's
	 *           <code>PropertyTag</code>
	 * @throws NullPointerException
	 *           If <code>container</code> is <code>null</code> or valueString is
	 *           null and this the parser does not support <code>null</code>
	 *           strings
	 */
	public void setValue(PropertyContainer contanier, String valueString)
			throws IllegalArgumentException, InvalidPropertyException,
			NullPointerException {
		try {
			T value = this.parser.parse(valueString);
			if( value == null ){
				return;
			}
			contanier.setProperty(this.tag, value);
		} catch (ValueParseException e) {
			throw new IllegalArgumentException("Can't assign value to property '"
					+ this.tag.getName() + "': " + e.getMessage(), e);
		}
	}

	/**
	 * Parses the specified string and adds the generated value to
	 * PropertyContainer using this <code>PropertyEntryMaker</code>'s
	 * <code>PropertyTag</code> as the key. This method differs from
	 * {@link #setValue(PropertyContainer, String)} in that it can't throw an
	 * <code>InvalidPropertyException</code> (<code>PropertyMap</code>s don't when
	 * properties are set in them, unlike <code>PropertyContainer</code>s)
	 * 
	 * @param map
	 *          The container to add this parser's <code>PropertyTag</code> and
	 *          parsed value to
	 * @param valueString
	 *          The string to parse and convert into an object of type T
	 * @throws IllegalArgumentException
	 *           If <code>valueString</code> is not of a format that allows this
	 *           maker to convert it into an object of type T
	 * @throws NullPointerException
	 *           If valueString is <code>null</code> and this maker does not
	 *           support <code>null</code> strings
	 */
	public void setValue(PropertyMap map, String valueString)
			throws IllegalArgumentException, NullPointerException {
		try {
			map.setProperty(this.tag, this.parser.parse(valueString));
		} catch (ValueParseException e) {
			throw new IllegalArgumentException("Can't assign value ["+valueString+"] to property '"
					+ this.tag.getName() + "': " + e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getName();
	}
}