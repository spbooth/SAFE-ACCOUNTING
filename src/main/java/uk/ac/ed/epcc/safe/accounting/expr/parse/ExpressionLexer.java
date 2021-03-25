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
import uk.ac.ed.epcc.webapp.Indexed;
import uk.ac.ed.epcc.webapp.jdbc.expr.Operator;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.logging.LoggerService;
import uk.ac.ed.epcc.webapp.model.NameFinder;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedProducer;



public class ExpressionLexer implements ExpressionParser.Lexer{
	/** An {@link ExpressionLexTarget} for an in-line object reference.
	 * 
	 * @author spb
	 *
	 */
	private static final class ReferenceLiteralLexTarget implements ExpressionLexTarget {
		@Override
		public Object make(AppContext conn,String pattern) throws LexException {
			int a = pattern.indexOf('(');
			int b = pattern.indexOf(',');
			int c = pattern.indexOf(')');
			String tag = pattern.substring(a+1, b).trim();
			String id = pattern.substring(b+1,c).trim();
			IndexedProducer  producer = conn.makeObject(IndexedProducer.class, tag);
			if( producer == null ){
				throw new LexException(tag+" does not resolve to IndexProducer");
			}
			try{
				Integer int_id = Integer.parseInt(id);
				return producer.makeReference(int_id);
			}catch(NumberFormatException e){
				if( producer instanceof NameFinder){
					Indexed idx = ((NameFinder)producer).findFromString(id);
					return producer.makeReference(idx);
				}
			}
			throw new LexException(id+" does not resolve in "+tag);
		}

		@Override
		public String getRegexp() {
			return "@REF\\(\\s*\\w+\\s*,\\s*[\\w-]+\\s*\\)";
		}

		@Override
		public int getToken(String pattern) {
			return ExpressionParser.Lexer.REFERENCE;
		}
	}

	/** An {@link ExpressionLexTarget} for {@link MatchCondition}s
	 * 
	 * @author spb
	 *
	 */
	private static final class MatchConditionLexTarget implements ExpressionLexTarget {
		@Override
		public String getRegexp() {
			return "(?:"+MatchCondition.getRegexp()+"|(?:==))";
		}

		@Override
		public Object make(AppContext conn,String pattern) throws LexException {
			if( pattern.equals("==")){
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
			return ExpressionParser.Lexer.MATCH;
		}
	}

	/** An {@link ExpressionLexTarget} for keyword names
	 * 
	 * @author spb
	 *
	 */
	private static final class KeywordLexTarget implements ExpressionLexTarget {
		public String getRegexp() {
			return "@(?:"+Keywords.getRegexp()+")";
		}

		public Object make(AppContext conn,String pattern) throws LexException {
			return Keywords.valueOf(pattern.substring(1));
		}

		public int getToken(String pattern) {
			return ExpressionParser.Lexer.KEYWORD;
		}
	}

	/** An {@link ExpressionLexTarget} for string literals
	 * 
	 * @author spb
	 *
	 */
	private static final class StringLiteralLexTarget implements ExpressionLexTarget {
		public Object make(AppContext conn,String pattern) throws LexException {
			return pattern.subSequence(1, pattern.length()-1);
		}

		public String getRegexp() {
			return "\"(?:[^\"\\n\\r])*\"";
		}

		public int getToken(String pattern) {
			
			return ExpressionParser.Lexer.STRING;
		}
	}

	/** An {@link ExpressionLexTarget} for numeric operators
	 * 
	 * @author spb
	 *
	 */
	private static final class OperatorLexTarget implements ExpressionLexTarget {
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
				return ExpressionParser.Lexer.PLUS;
			}
			else if( pattern.equals("-")){
				return ExpressionParser.Lexer.MINUS;
			}
			else if( pattern.equals("/")){
				return ExpressionParser.Lexer.DIV;
			}
			else{
				return ExpressionParser.Lexer.MULT;
			}
			
		}
	}

	/** An {@link ExpressionLexTarget} for a property tag name
	 * 
	 * @author spb
	 *
	 */
	private static final class PropertyTagLexTarget implements ExpressionLexTarget {
		public String getRegexp() {
			return "(?:"+FixedPropertyFinder.PROPERTY_FINDER_PREFIX_REGEXP+")?"+PropertyTag.PROPERT_TAG_NAME_PATTERN;
		}

		public Object make(AppContext conn,String pattern) throws LexException{
			return pattern;
		}

		public int getToken(String pattern) {
			return ExpressionParser.Lexer.PROPTAG;
		}
	}

	/** an {@link ExpressionLexTarget} for a literal numeric value
	 * 
	 * @author spb
	 *
	 */
	private static final class NumberLexTarget implements ExpressionLexTarget {
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
			
			return ExpressionParser.Lexer.NUMBER;
		}
	}
	
	/** an {@link ExpressionLexTarget} for a literal numeric value
	 * 
	 * @author spb
	 *
	 */
	private static final class BooleanLexTarget implements ExpressionLexTarget {
		public String getRegexp(){
			  return "(?i:true)|(?i:false)";
		  }

		public Object make(AppContext conn,String pattern){
				  return Boolean.parseBoolean(pattern);
		  }

		public int getToken(String pattern) {
			
			return ExpressionParser.Lexer.BOOLEAN;
		}
	}
	/** a generic {@link ExpressionLexTarget} 
	 * 
	 * @author spb
	 *
	 */
	private static final class LiteralExpressionLexTarget implements ExpressionLexTarget{
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
	  private static ExpressionLexTarget lex_targets[];
	   static Pattern makePattern(){
		   List<ExpressionLexTarget> targets=new LinkedList<>();
			
		   // Number target
			  targets.add(new NumberLexTarget());
			  // Booleans
			  targets.add(new BooleanLexTarget());
			  // Property target
			  targets.add(new PropertyTagLexTarget());
			  // operator target
			  targets.add(new OperatorLexTarget());
			  // Bracket literal target
			  targets.add(new LiteralExpressionLexTarget(ExpressionParser.Lexer.COMMA, ","));
			  targets.add(new LiteralExpressionLexTarget(ExpressionParser.Lexer.LPAREN, "\\("));
			  targets.add(new LiteralExpressionLexTarget(ExpressionParser.Lexer.RPAREN, "\\)"));
			  targets.add(new LiteralExpressionLexTarget(ExpressionParser.Lexer.LSQR, "\\["));
			  targets.add(new LiteralExpressionLexTarget(ExpressionParser.Lexer.RSQR, "\\]"));
			  targets.add(new LiteralExpressionLexTarget(ExpressionParser.Lexer.LBRACE, "\\{"));
			  targets.add(new LiteralExpressionLexTarget(ExpressionParser.Lexer.RBRACE, "\\}"));
			  
			  // String literal target
			  targets.add(new StringLiteralLexTarget());
		      // keyword
			  targets.add(new KeywordLexTarget());
			  // matchcondition
			  targets.add(new MatchConditionLexTarget());
			  targets.add(new ReferenceLiteralLexTarget());
			lex_targets=targets.toArray(new ExpressionLexTarget[targets.size()]);
			StringBuilder pattern_text = new StringBuilder();
			pattern_text.append("\\s*");
			boolean seen=false;
			for(ExpressionLexTarget t : lex_targets){
				if( seen ){
					pattern_text.append("|");
				}
				seen=true;
				pattern_text.append("(");
				pattern_text.append(t.getRegexp());
				pattern_text.append(")");
			}
			return Pattern.compile(pattern_text.toString());
	   }
	  static Pattern lex_pattern = makePattern();
	  
	  private Object val=null;
	  private AppContext conn;
	  private String orig;
	  private Matcher m;
	  int end;
	  public ExpressionLexer(AppContext conn,String text){
		  this.conn=conn;
		  this.orig=text.trim();
		  this.m = lex_pattern.matcher(orig);
		 
	  }
	 
	
	
	
	public Object getLVal() {
		return val;
	}

	public void yyerror(String s) {
		LoggerService serv = conn.getService(LoggerService.class);
		if( serv != null ){
			serv.getLogger(getClass()).warn(s);
		}
		
	}

	public int yylex() throws  LexException {
		 if( m.regionStart() >= m.regionEnd()){
			  return ExpressionParser.Lexer.EOF;
		  }
		 if( m.lookingAt() ){
			  for(int i=0 ; i< lex_targets.length ; i++){
				  ExpressionLexTarget t = lex_targets[i];
				  String text = m.group(i+1);
				  if( text != null ){
					  orig=orig.substring(text.length()).trim();
					  m.reset(orig);
					  //m.region(m.regionStart()+text.length(), m.regionEnd());
					  val= t.make(conn,text);
					  return t.getToken(text);
				  }
			  }
		  }
		
		  throw new LexException(orig.substring(m.regionStart()));
	}

}