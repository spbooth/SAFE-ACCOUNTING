// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.parsers.value;

import org.w3c.dom.Node;

import uk.ac.ed.epcc.webapp.model.data.Duration;

/** parse a numeric value. if storageUnit or phaseUnit values are specified then 
 * convert to bits. If a phase unit exists the result is scaled by the number of seconds
 * 
 * @author spb
 *
 */
@uk.ac.ed.epcc.webapp.Version("$Id: IntervallicVolumeParser.java,v 1.7 2014/09/15 14:32:25 spb Exp $")

public class IntervallicVolumeParser implements DomValueParser<Number> {

	public Class<Number> getType() {
		return Number.class;
	}

	public Number parse(Node valueNode) throws ValueParseException {
		String unit = getText(valueNode.getAttributes().getNamedItem("storageUnit"));
		String duration = getText(valueNode.getAttributes().getNamedItem("phaseUnit"));
		double raw = Double.parseDouble(getText(valueNode));
		double mult = 1.0;
		if( unit != null ){
			
			if( unit.endsWith("B")){
				mult = 8.0; 
			}
			switch( unit.charAt(0)){
			case 'E': mult *= 1024.0;
			case 'P': mult *= 1024.0;
			case 'T': mult *= 1024.0;
			case 'G': mult *= 1024.0;
			case 'M': mult *= 1024.0;
			case 'K': mult *= 1024.0;
			}
		}
		if( duration != null){
			Duration d = XMLDurationParser.PARSER.parse(duration);
			mult *= d.doubleValue();
		}
		return new Double(raw*mult);
	}
	private String getText(Node valueNode)throws ValueParseException{
		if( valueNode == null ){
			return null;
		}
		if( valueNode.getNodeType() == Node.TEXT_NODE || valueNode.getNodeType() == Node.ATTRIBUTE_NODE){
			return valueNode.getNodeValue();
		}else if( valueNode.getNodeType() == Node.ELEMENT_NODE){
			return getText(valueNode.getFirstChild());
		}
		throw new ValueParseException("Unexpected node type "+valueNode.getNodeType());
	}

}