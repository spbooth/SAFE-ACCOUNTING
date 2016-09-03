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
package uk.ac.ed.epcc.safe.accounting.expr.parse;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ed.epcc.safe.accounting.properties.FixedPropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.AppContext;
import uk.ac.ed.epcc.webapp.jdbc.expr.Operator;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;



public class ExpressionLexer implements ExpressionParser.Lexer{
	public static class LiteralExpressionLexTarget implements ExpressionLexTarget{
        private final String regexp;
        private final int token;
        public LiteralExpressionLexTarget(int code, String regexp){
        	this.token=code;
        	this.regexp=regexp;
        }
		public int getToken(String pattern) {
			return token;
		}

		public String getRegexp() {
			return regexp;
		}

		public Object make(AppContext conn,String pattern) throws LexException {
			return pattern;
		}
		
	}
	 private CharSequence text=null;
	  private CharSequence prev=null;
	  private List<ExpressionLexTarget> targets=new LinkedList<ExpressionLexTarget>();
	 
	  private Object val=null;
	  private AppContext conn;
	  public ExpressionLexer(AppContext conn,String text){
		  this.conn=conn;
		  this.text=text;
		  // Number target
		  targets.add(new ExpressionLexTarget(){
			  public String getRegexp(){
				  return "\\d+(?:\\.\\d*)?(?:e[+|-]?\\d+)?";
			  }
			  public Object make(AppContext conn,String pattern){
				  try{
					  return Long.parseLong(pattern);
				  }catch(NumberFormatException e){
				    return Double.parseDouble(pattern);
				  }
			  }
			public int getToken(String pattern) {
				
				return ExpressionParser.NUMBER;
			}
		  });
		  // Property target
		  targets.add(new ExpressionLexTarget(){

			public String getRegexp() {
				return "(?:"+FixedPropertyFinder.PROPERTY_FINDER_PREFIX_REGEXP+")?"+PropertyTag.PROPERT_TAG_NAME_PATTERN;
			}

			public Object make(AppContext conn,String pattern) throws LexException{
				return pattern;
			}

			public int getToken(String pattern) {
				return ExpressionParser.PROPTAG;
			}
			  
		  });
		  // operator target
		  targets.add(new ExpressionLexTarget(){

				public String getRegexp() {
					return "\\+|-|\\*|/";
				}

				public Object make(AppContext conn,String pattern) throws LexException{
					for( Operator o : EnumSet.allOf(Operator.class)){
						if( pattern.equals(o.text())){
							return o;
						}
							
					}
					throw new LexException("illegal operator "+pattern);
				}

				public int getToken(String pattern) {
					if( pattern.equals("+")){
						return ExpressionParser.PLUS;
					}
					else if( pattern.equals("-")){
						return ExpressionParser.MINUS;
					}
					else if( pattern.equals("/")){
						return ExpressionParser.DIV;
					}
					else{
						return ExpressionParser.MULT;
					}
					
				}
				  
			  });
		  // Bracket literal target
		  targets.add(new LiteralExpressionLexTarget(ExpressionParser.COMMA, ","));
		  targets.add(new LiteralExpressionLexTarget(ExpressionParser.LPAREN, "\\("));
		  targets.add(new LiteralExpressionLexTarget(ExpressionParser.RPAREN, "\\)"));
		  targets.add(new LiteralExpressionLexTarget(ExpressionParser.LSQR, "\\["));
		  targets.add(new LiteralExpressionLexTarget(ExpressionParser.RSQR, "\\]"));
		  targets.add(new LiteralExpressionLexTarget(ExpressionParser.LBRACE, "\\{"));
		  targets.add(new LiteralExpressionLexTarget(ExpressionParser.RBRACE, "\\}"));
	
		  // String literal target
		  targets.add(new ExpressionLexTarget() {
			
			public Object make(AppContext conn,String pattern) throws LexException {
				return pattern.subSequence(1, pattern.length()-1);
			}
			
			public String getRegexp() {
				return "\"(?:[^\"\\n\\r])*\"";
			}

			public int getToken(String pattern) {
				
				return ExpressionParser.STRING;
			}
		});
	      // keyword
		  targets.add(new ExpressionLexTarget(){

			public String getRegexp() {
				return "@(?:"+Keywords.getRegexp()+")";
			}

			public Object make(AppContext conn,String pattern) throws LexException {
				return Keywords.valueOf(pattern.substring(1));
			}

			public int getToken(String pattern) {
				return ExpressionParser.KEYWORD;
			}
			  
		  });
		  // matchcondition
		  targets.add(new ExpressionLexTarget(){

			@Override
			public String getRegexp() {
				return "(?:"+MatchCondition.getRegexp()+"|(?:==))";
			}

			@Override
			public Object make(AppContext conn,String pattern) throws LexException {
				if( pattern == "=="){
					return null;
				}
				for( MatchCondition m : MatchCondition.values()){
					if( m.match().equals(pattern)){
						return m;
					}
				}
				return MatchCondition.valueOf(pattern);
			}

			@Override
			public int getToken(String pattern) {
				return ExpressionParser.MATCH;
			}
			  
		  });
		  targets.add(new ExpressionLexTarget(){

			@Override
			public String getRegexp() {
				
				return IndexedReference.INDEXED_REFERENCE_NAME_REGEXP;
			}

			@Override
			public Object make(AppContext conn,String pattern) throws LexException {
				return IndexedReference.parseIndexedReference(conn, pattern);
			}

			@Override
			public int getToken(String pattern) {
				// TODO Auto-generated method stub
				return ExpressionParser.REFERENCE;
			}
			  
		  });
	  }
	 
	  public void back(){
		  text=prev;
		  prev=null;
	  }
	  Object next()throws LexException{
		  if( text == null || text.length() == 0){
			  return null;
		  }
		  for( LexTarget lt : targets){
			  String pattern="^\\s*("+lt.getRegexp()+")\\s*(.*)";
			  //System.out.println("Pattern<"+pattern+"> text<"+text+">");
			  Pattern p =  Pattern.compile(pattern);
			  Matcher m = p.matcher(text);
			  if( m.matches()){
				  Object o = lt.make(conn,m.group(1));
				  prev = text;
				  text = m.group(2);
				  return o;
			  }
		  }
		  throw new LexException(text);
	  }
	  
	
	
	public Object getLVal() {
		return val;
	}

	public void yyerror(String s) {
		LoggerService serv = conn.getService(LoggerService.class);
		if( serv != null ){
			serv.getLogger(getClass()).error(s);
		}
		
	}

	public int yylex() throws  LexException {
		 if( text == null || text.length() == 0){
			  return ExpressionParser.EOF;
		  }
		  for( ExpressionLexTarget lt : targets){
			  String pattern="^\\s*("+lt.getRegexp()+")\\s*(.*)";
			  //System.out.println("Pattern<"+pattern+"> text<"+text+">");
			  Pattern p =  Pattern.compile(pattern);
			  Matcher m = p.matcher(text);
			  if( m.matches()){
				  val = lt.make(conn,m.group(1));
				  prev = text;
				  text = m.group(2);
				  return lt.getToken(m.group(1));
			  }
		  }
		  throw new LexException(text);
	}

}