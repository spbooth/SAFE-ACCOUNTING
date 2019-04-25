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
package uk.ac.ed.epcc.safe.accounting.parsers.value;

import org.w3c.dom.Node;
/** Adapter that converts a ValueParser into a DomValueParser
 * 
 * @author spb
 *
 * @param <T> target type
 */


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