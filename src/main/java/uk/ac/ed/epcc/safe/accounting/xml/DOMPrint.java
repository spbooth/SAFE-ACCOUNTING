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
package uk.ac.ed.epcc.safe.accounting.xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



public class DOMPrint {
   StringBuilder sb = new StringBuilder();
   int depth=0;
   public void add(Node n){
	   prefix();
	   depth++;
	   switch(n.getNodeType()){
	   case Node.ELEMENT_NODE: sb.append("Element: ");sb.append(n.getNodeName()); sb.append(" "); break;
	   case Node.ATTRIBUTE_NODE: sb.append("Attribute: ");sb.append(n.getNodeName()); sb.append("="); sb.append(n.getNodeValue()); break;
	   case Node.COMMENT_NODE: sb.append("Comment: "); sb.append(" "); sb.append(n.getNodeValue()); break;
	   case Node.TEXT_NODE: sb.append("Text: "); sb.append(" "); sb.append(n.getNodeValue()); break;
	   default: sb.append("Other "); sb.append(n.getNodeType());
	   }
	   addList(n.getChildNodes());
	   depth--;
   }
   private void prefix(){
	   sb.append("\n");
	   for(int i=0 ; i < depth ; i++){
		   sb.append(""+i%10);
	   }
   }
   private void addList(NodeList l){
	   int ln = l.getLength();
	   for(int i=0; i< ln; i++){
		   add(l.item(i));
	   }
   }
   @Override
public String toString(){
	   return sb.toString();
   }
}