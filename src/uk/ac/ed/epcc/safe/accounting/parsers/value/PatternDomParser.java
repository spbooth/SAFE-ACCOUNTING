// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers.value;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Node;

import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.Contexed;
/** DomValueParser that extracts part of the target text using a
 * regular expression.
 * The pattern is defined using the property <em><b>pattern.</b>class-tag</em>
 * and the first matching group of the regular expression returned.
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: PatternDomParser.java,v 1.5 2014/09/15 14:32:26 spb Exp $")

public class PatternDomParser implements DomValueParser<String>, Contexed {
    AppContext conn;
    Pattern pat;
    public PatternDomParser(AppContext c, String tag){
    	conn=c;
    	pat = Pattern.compile(c.getInitParameter("pattern."+tag));
    }
	public Class<String> getType() {
		return String.class;
	}

	public String parse(Node valueNode) throws ValueParseException {
		Matcher m = pat.matcher(getText(valueNode));
		if( m.find()){
			return m.group(1);
		}
		return null;
	}

	public AppContext getContext() {
		return conn;
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