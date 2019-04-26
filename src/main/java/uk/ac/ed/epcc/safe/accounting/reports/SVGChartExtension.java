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
package uk.ac.ed.epcc.safe.accounting.reports;

import java.awt.Dimension;
import java.text.NumberFormat;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

import uk.ac.ed.epcc.webapp.AppContext;


/** A {@link ChartExtension} that generates an in-line SVG graphic 
 * 
 * @author spb
 *
 */



public class SVGChartExtension extends ChartExtension {

	public SVGChartExtension(AppContext c,NumberFormat nf) throws ParserConfigurationException {
		super(c,nf);
	}

	@Override
	public DocumentFragment addChart( Chart chart,String caption) {
		try {
		//Note the createSVG generates the figure element
		// itself so it can annotate with graphic sizes.
		// Get a DOMImplementation
		DOMImplementation domImpl = SVGDOMImplementation.getDOMImplementation();
		// Create an instance of org.w3c.dom.Document
		String svgNS = "http://www.w3.org/2000/svg";
		Document SVGdoc = domImpl.createDocument(svgNS, "svg", null);
		DocumentFragment frag=SVGdoc.createDocumentFragment();
		// Create an instance of the SVG Generator
		SVGGraphics2D svgGenerator = new SVGGraphics2D(SVGdoc);
		Dimension d = chart.chart.getChartData().getSize();
		svgGenerator.setSVGCanvasSize(d);
		  		//JComponent jcomp = (JComponent) chart; 
		chart.chart.getChartData().writeGraphics(svgGenerator);
		Element figure=SVGdoc.createElement("figure");
		frag.appendChild(figure);
		figure.setAttribute("height", Integer.toString(d.height));
		figure.setAttribute("width", Integer.toString(d.width));
		figure.appendChild(svgGenerator.getRoot());
		if( caption != null && caption.trim().length() > 0){
			Element c=SVGdoc.createElement("caption");
			c.appendChild(SVGdoc.createTextNode(caption));
			figure.appendChild(c);
		}
		return frag;
		}catch(Exception e) {
			addError("Bad Plot","Error addingsvg graphic",e);
			return getDocument().createDocumentFragment();
		}
	}

	@Override
	public boolean graphOutput() {
		return true;
	}

}