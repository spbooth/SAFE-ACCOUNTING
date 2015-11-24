// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers.value;

import org.w3c.dom.Node;
/** Adapter that converts a ValueParser into a DomValueParser
 * 
 * @author spb
 *
 * @param <T> target type
 */
@uk.ac.ed.epcc.webapp.Version("$Id: DomParserAdapter.java,v 1.6 2014/09/15 14:32:25 spb Exp $")

public class DomParserAdapter<T> implements DomValueParser<T> {

	private final ValueParser<T> value_parser;
	public DomParserAdapter(ValueParser<T> vp){
		value_parser=vp;
	}
	public Class<T> getType() {
		return value_parser.getType();
	}

	public T parse(Node valueNode) throws ValueParseException {
		
		return value_parser.parse(getText(valueNode));
	}
	private String getText(Node valueNode)throws ValueParseException{
		if( valueNode ==  null){
			return "";
		}
		if( valueNode.getNodeType() == Node.TEXT_NODE || valueNode.getNodeType() == Node.ATTRIBUTE_NODE){
			String res = valueNode.getNodeValue();
			if( res == null ){
				return "";
			}
			return res;
		}else if( valueNode.getNodeType() == Node.ELEMENT_NODE){
			return getText(valueNode.getFirstChild());
		}
		throw new ValueParseException("Unexpected node type "+valueNode.getNodeType());
	}

}