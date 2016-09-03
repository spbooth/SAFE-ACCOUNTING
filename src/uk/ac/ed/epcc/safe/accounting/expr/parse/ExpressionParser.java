//| Copyright - The University of Edinburgh 2015                            |
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
/* A Bison parser, made by GNU Bison 2.4.1.  */

/* Skeleton implementation for Bison LALR(1) parsers in Java
   
      Copyright (C) 2007, 2008 Free Software Foundation, Inc.
   
   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
   
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   
   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

/* As a special exception, you may create a larger work that contains
   part or all of the Bison parser skeleton and distribute that work
   under terms of your choice, so long as that work isn't itself a
   parser generator using the skeleton or a modified version thereof
   as a parser skeleton.  Alternatively, if you modify or redistribute
   the parser skeleton itself, you may (at your option) remove this
   special exception, which will cause the skeleton and the resulting
   Bison output files to be licensed under the GNU General Public
   License without this special exception.
   
   This special exception was added by the Free Software Foundation in
   version 2.2 of Bison.  */

package uk.ac.ed.epcc.safe.accounting.expr.parse;
/* First part of user declarations.  */

/* "%code imports" blocks.  */

/* Line 33 of lalr1.java  */
/* Line 5 of "expr.y"  */

import  uk.ac.ed.epcc.safe.accounting.*;
import uk.ac.ed.epcc.safe.accounting.expr.*;
import uk.ac.ed.epcc.safe.accounting.properties.InvalidPropertyException;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyFinder;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTargetFactory;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceExpression;

import java.util.*;
import uk.ac.ed.epcc.webapp.jdbc.expr.Operator;
import uk.ac.ed.epcc.webapp.model.data.expr.*;
import uk.ac.ed.epcc.webapp.*;
import uk.ac.ed.epcc.webapp.jdbc.filter.MatchCondition;
import uk.ac.ed.epcc.webapp.model.data.reference.IndexedReference;

@SuppressWarnings({"unchecked","unused"})



/* Line 33 of lalr1.java  */
/* Line 64 of "expr.java"  */

/**
 * A Bison parser, automatically generated from <tt>expr.y</tt>.
 *
 * @author LALR (1) parser skeleton written by Paolo Bonzini.
 */
public class ExpressionParser
{
    /** Version number for the Bison executable that generated this parser.  */
  public static final String bisonVersion = "2.4.1";

  /** Name of the skeleton that generated this parser.  */
  public static final String bisonSkeleton = "lalr1.java";


  /** True if verbose error messages are enabled.  */
  public boolean errorVerbose = false;



  /** Token returned by the scanner to signal the end of its input.  */
  public static final int EOF = 0;

/* Tokens.  */
  /** Token number, to be returned by the scanner.  */
  public static final int NUMBER = 258;
  /** Token number, to be returned by the scanner.  */
  public static final int MULT = 259;
  /** Token number, to be returned by the scanner.  */
  public static final int DIV = 260;
  /** Token number, to be returned by the scanner.  */
  public static final int PLUS = 261;
  /** Token number, to be returned by the scanner.  */
  public static final int MINUS = 262;
  /** Token number, to be returned by the scanner.  */
  public static final int LPAREN = 263;
  /** Token number, to be returned by the scanner.  */
  public static final int RPAREN = 264;
  /** Token number, to be returned by the scanner.  */
  public static final int LBRACE = 265;
  /** Token number, to be returned by the scanner.  */
  public static final int RBRACE = 266;
  /** Token number, to be returned by the scanner.  */
  public static final int LSQR = 267;
  /** Token number, to be returned by the scanner.  */
  public static final int RSQR = 268;
  /** Token number, to be returned by the scanner.  */
  public static final int PROPTAG = 269;
  /** Token number, to be returned by the scanner.  */
  public static final int STRING = 270;
  /** Token number, to be returned by the scanner.  */
  public static final int COMMA = 271;
  /** Token number, to be returned by the scanner.  */
  public static final int KEYWORD = 272;
  /** Token number, to be returned by the scanner.  */
  public static final int MATCH = 273;
  /** Token number, to be returned by the scanner.  */
  public static final int REFERENCE = 274;
  /** Token number, to be returned by the scanner.  */
  public static final int NEG = 275;



  

  /**
   * Communication interface between the scanner and the Bison-generated
   * parser <tt>ExpressionParser</tt>.
   */
  public interface Lexer {
    

    /**
     * Method to retrieve the semantic value of the last scanned token.
     * @return the semantic value of the last scanned token.  */
    Object getLVal ();

    /**
     * Entry point for the scanner.  Returns the token identifier corresponding
     * to the next token and prepares to return the semantic value
     * of the token. 
     * @return the token identifier corresponding to the next token. */
    int yylex () throws LexException;

    /**
     * Entry point for error reporting.  Emits an error
     * in a user-defined way.
     *
     * 
     * @param s The string for the error message.  */
     void yyerror (String s);
  }

  /** The object doing lexical analysis for us.  */
  private Lexer yylexer;
  
  



  /**
   * Instantiates the Bison-generated parser.
   * @param yylexer The scanner that will supply tokens to the parser.
   */
  public ExpressionParser (Lexer yylexer) {
    this.yylexer = yylexer;
    
  }

  private java.io.PrintStream yyDebugStream = System.err;

  /**
   * Return the <tt>PrintStream</tt> on which the debugging output is
   * printed.
   */
  public final java.io.PrintStream getDebugStream () { return yyDebugStream; }

  /**
   * Set the <tt>PrintStream</tt> on which the debug output is printed.
   * @param s The stream that is used for debugging output.
   */
  public final void setDebugStream(java.io.PrintStream s) { yyDebugStream = s; }

  private int yydebug = 0;

  /**
   * Answer the verbosity of the debugging output; 0 means that all kinds of
   * output from the parser are suppressed.
   */
  public final int getDebugLevel() { return yydebug; }

  /**
   * Set the verbosity of the debugging output; 0 means that all kinds of
   * output from the parser are suppressed.
   * @param level The verbosity level for debugging output.
   */
  public final void setDebugLevel(int level) { yydebug = level; }

  private final int yylex () throws LexException {
    return yylexer.yylex ();
  }
  protected final void yyerror (String s) {
    yylexer.yyerror (s);
  }

  

  protected final void yycdebug (String s) {
    if (yydebug > 0)
      yyDebugStream.println (s);
  }

  private final class YYStack {
    private int[] stateStack = new int[16];
    
    private Object[] valueStack = new Object[16];

    public int size = 16;
    public int height = -1;
    
    public final void push (int state, Object value    	   	      	    ) {
      height++;
      if (size == height) 
        {
	  int[] newStateStack = new int[size * 2];
	  System.arraycopy (stateStack, 0, newStateStack, 0, height);
	  stateStack = newStateStack;
	  
	  
	  Object[] newValueStack = new Object[size * 2];
	  System.arraycopy (valueStack, 0, newValueStack, 0, height);
	  valueStack = newValueStack;

	  size *= 2;
	}

      stateStack[height] = state;
      
      valueStack[height] = value;
    }

    public final void pop () {
      height--;
    }

    public final void pop (int num) {
      // Avoid memory leaks... garbage collection is a white lie!
      if (num > 0) {
	java.util.Arrays.fill (valueStack, height - num + 1, height, null);
        
      }
      height -= num;
    }

    public final int stateAt (int i) {
      return stateStack[height - i];
    }

    public final Object valueAt (int i) {
      return valueStack[height - i];
    }

    // Print the state stack on the debug stream.
    public void print (java.io.PrintStream out)
    {
      out.print ("Stack now");
      
      for (int i = 0; i < height; i++)
        {
	  out.print (' ');
	  out.print (stateStack[i]);
        }
      out.println ();
    }
  }

  /**
   * Returned by a Bison action in order to stop the parsing process and
   * return success (<tt>true</tt>).  */
  public static final int YYACCEPT = 0;

  /**
   * Returned by a Bison action in order to stop the parsing process and
   * return failure (<tt>false</tt>).  */
  public static final int YYABORT = 1;

  /**
   * Returned by a Bison action in order to start error recovery without
   * printing an error message.  */
  public static final int YYERROR = 2;

  /**
   * Returned by a Bison action in order to print an error message and start
   * error recovery.  */
  public static final int YYFAIL = 3;

  private static final int YYNEWSTATE = 4;
  private static final int YYDEFAULT = 5;
  private static final int YYREDUCE = 6;
  private static final int YYERRLAB1 = 7;
  private static final int YYRETURN = 8;

  private int yyerrstatus_ = 0;

  /**
   * Return whether error recovery is being done.  In this state, the parser
   * reads token until it reaches a known state, and then restarts normal
   * operation.  */
  public final boolean recovering ()
  {
    return yyerrstatus_ == 0;
  }

  private int yyaction (int yyn, YYStack yystack, int yylen) throws Exception
  {
    Object yyval;
    

    /* If YYLEN is nonzero, implement the default value of the action:
       `$$ = $1'.  Otherwise, use the top of the stack.
    
       Otherwise, the following line sets YYVAL to garbage.
       This behavior is undocumented and Bison
       users should not rely upon it.  */
    if (yylen > 0)
      yyval = yystack.valueAt (yylen - 1);
    else
      yyval = yystack.valueAt (0);
    
    yy_reduce_print (yyn, yystack);

    switch (yyn)
      {
	  case 2:
  if (yyn == 2)
    
/* Line 353 of lalr1.java  */
/* Line 91 of "expr.y"  */
    { result = ((PropExpression)(yystack.valueAt (1-(1)))); };
  break;
    

  case 3:
  if (yyn == 3)
    
/* Line 353 of lalr1.java  */
/* Line 94 of "expr.y"  */
    {
  ReferenceExpression tag = (ReferenceExpression)  ((PropExpression)(yystack.valueAt (3-(1))));
  PropExpression inner = ((PropExpression)(yystack.valueAt (3-(2))));
  if( inner instanceof ReferenceExpression){
    yyval =new DoubleDeRefExpression(tag,(ReferenceExpression) inner);
  }else{
    yyval = new DeRefExpression(tag,inner);
  }
   
};
  break;
    

  case 4:
  if (yyn == 4)
    
/* Line 353 of lalr1.java  */
/* Line 104 of "expr.y"  */
    {
  yyval = find(((String)(yystack.valueAt (1-(1))))); 
};
  break;
    

  case 5:
  if (yyn == 5)
    
/* Line 353 of lalr1.java  */
/* Line 107 of "expr.y"  */
    {
  yyval = new ConstPropExpression(String.class,((String)(yystack.valueAt (1-(1))))); 
};
  break;
    

  case 6:
  if (yyn == 6)
    
/* Line 353 of lalr1.java  */
/* Line 110 of "expr.y"  */
    { 
  LinkedList<PropExpression> list =((LinkedList)(yystack.valueAt (3-(2))));
  yyval = SelectPropExpression.makeSelect(list.toArray(new PropExpression[list.size()])); 
};
  break;
    

  case 7:
  if (yyn == 7)
    
/* Line 353 of lalr1.java  */
/* Line 114 of "expr.y"  */
    {
  Keywords op = ((Keywords)(yystack.valueAt (4-(1))));
  yyval = op.getExpression(((LinkedList)(yystack.valueAt (4-(3)))));
};
  break;
    

  case 8:
  if (yyn == 8)
    
/* Line 353 of lalr1.java  */
/* Line 118 of "expr.y"  */
    { yyval = castNumber(((PropExpression)(yystack.valueAt (3-(2))))); };
  break;
    

  case 9:
  if (yyn == 9)
    
/* Line 353 of lalr1.java  */
/* Line 119 of "expr.y"  */
    {
  yyval = new BinaryPropExpression(castNumber(((PropExpression)(yystack.valueAt (3-(1))))),Operator.ADD,castNumber(((PropExpression)(yystack.valueAt (3-(3)))))); 
};
  break;
    

  case 10:
  if (yyn == 10)
    
/* Line 353 of lalr1.java  */
/* Line 122 of "expr.y"  */
    {
  yyval = new BinaryPropExpression(castNumber(((PropExpression)(yystack.valueAt (3-(1))))),Operator.SUB,castNumber(((PropExpression)(yystack.valueAt (3-(3)))))); 
};
  break;
    

  case 11:
  if (yyn == 11)
    
/* Line 353 of lalr1.java  */
/* Line 125 of "expr.y"  */
    {
  yyval = new BinaryPropExpression(castNumber(((PropExpression)(yystack.valueAt (3-(1))))),Operator.MUL, castNumber(((PropExpression)(yystack.valueAt (3-(3)))))); 
};
  break;
    

  case 12:
  if (yyn == 12)
    
/* Line 353 of lalr1.java  */
/* Line 128 of "expr.y"  */
    { 
  yyval = new BinaryPropExpression(castNumber(((PropExpression)(yystack.valueAt (3-(1))))),Operator.DIV, castNumber(((PropExpression)(yystack.valueAt (3-(3)))))); 
};
  break;
    

  case 13:
  if (yyn == 13)
    
/* Line 353 of lalr1.java  */
/* Line 131 of "expr.y"  */
    {
  yyval = new NegatePropExpression(castNumber(((PropExpression)(yystack.valueAt (2-(2))))));    
};
  break;
    

  case 14:
  if (yyn == 14)
    
/* Line 353 of lalr1.java  */
/* Line 134 of "expr.y"  */
    {
  yyval=new ConstPropExpression(Number.class,((Number)(yystack.valueAt (1-(1)))));    
};
  break;
    

  case 15:
  if (yyn == 15)
    
/* Line 353 of lalr1.java  */
/* Line 137 of "expr.y"  */
    {
  yyval=new ConstReferenceExpression(((IndexedReference)(yystack.valueAt (1-(1)))));    
};
  break;
    

  case 16:
  if (yyn == 16)
    
/* Line 353 of lalr1.java  */
/* Line 140 of "expr.y"  */
    {
  yyval= new ComparePropExpression(((PropExpression)(yystack.valueAt (3-(1)))),((MatchCondition)(yystack.valueAt (3-(2)))),((PropExpression)(yystack.valueAt (3-(3)))));
};
  break;
    

  case 17:
  if (yyn == 17)
    
/* Line 353 of lalr1.java  */
/* Line 145 of "expr.y"  */
    {
  if( ((PropExpression)(yystack.valueAt (2-(1)))) instanceof ReferenceExpression ){
  ReferenceExpression tag = (ReferenceExpression)  ((PropExpression)(yystack.valueAt (2-(1))));
  if( PropertyTargetFactory.class.isAssignableFrom(tag.getFactoryClass())){
    PropertyTargetFactory fac = (PropertyTargetFactory) conn.makeObject(tag.getFactoryClass(), tag.getTable());
    push(fac.getFinder());
  }
  yyval=tag;
  }else{
     throw new ParseException("Illegal dereference of "+((PropExpression)(yystack.valueAt (2-(1)))).toString()); 
  }
};
  break;
    

  case 18:
  if (yyn == 18)
    
/* Line 353 of lalr1.java  */
/* Line 160 of "expr.y"  */
    { pop(); };
  break;
    

  case 19:
  if (yyn == 19)
    
/* Line 353 of lalr1.java  */
/* Line 163 of "expr.y"  */
    {
  LinkedList list = new LinkedList();
  list.add(((PropExpression)(yystack.valueAt (1-(1)))));
  yyval=list;
};
  break;
    

  case 20:
  if (yyn == 20)
    
/* Line 353 of lalr1.java  */
/* Line 168 of "expr.y"  */
    {
  LinkedList list = ((LinkedList)(yystack.valueAt (3-(1))));
  list.add(((PropExpression)(yystack.valueAt (3-(3)))));
  yyval=list;
};
  break;
    



/* Line 353 of lalr1.java  */
/* Line 566 of "expr.java"  */
	default: break;
      }

    yy_symbol_print ("-> $$ =", yyr1_[yyn], yyval);

    yystack.pop (yylen);
    yylen = 0;

    /* Shift the result of the reduction.  */
    yyn = yyr1_[yyn];
    int yystate = yypgoto_[yyn - yyntokens_] + yystack.stateAt (0);
    if (0 <= yystate && yystate <= yylast_
	&& yycheck_[yystate] == yystack.stateAt (0))
      yystate = yytable_[yystate];
    else
      yystate = yydefgoto_[yyn - yyntokens_];

    yystack.push (yystate, yyval);
    return YYNEWSTATE;
  }

  /* Return YYSTR after stripping away unnecessary quotes and
     backslashes, so that it's suitable for yyerror.  The heuristic is
     that double-quoting is unnecessary unless the string contains an
     apostrophe, a comma, or backslash (other than backslash-backslash).
     YYSTR is taken from yytname.  */
  private final String yytnamerr_ (String yystr)
  {
    if (yystr.charAt (0) == '"')
      {
        StringBuffer yyr = new StringBuffer ();
        strip_quotes: for (int i = 1; i < yystr.length (); i++)
          switch (yystr.charAt (i))
            {
            case '\'':
            case ',':
              break strip_quotes;

            case '\\':
	      if (yystr.charAt(++i) != '\\')
                break strip_quotes;
              /* Fall through.  */
            default:
              yyr.append (yystr.charAt (i));
              break;

            case '"':
              return yyr.toString ();
            }
      }
    else if (yystr.equals ("$end"))
      return "end of input";

    return yystr;
  }

  /*--------------------------------.
  | Print this symbol on YYOUTPUT.  |
  `--------------------------------*/

  private void yy_symbol_print (String s, int yytype,
			         Object yyvaluep				 )
  {
    if (yydebug > 0)
    yycdebug (s + (yytype < yyntokens_ ? " token " : " nterm ")
	      + yytname_[yytype] + " ("
	      + (yyvaluep == null ? "(null)" : yyvaluep.toString ()) + ")");
  }

  /**
   * Parse input from the scanner that was specified at object construction
   * time.  Return whether the end of the input was reached successfully.
   *
   * @return <tt>true</tt> if the parsing succeeds.  Note that this does not
   *          imply that there were no syntax errors.
   */
  public boolean parse () throws LexException, Exception
  {
    /// Lookahead and lookahead in internal form.
    int yychar = yyempty_;
    int yytoken = 0;

    /* State.  */
    int yyn = 0;
    int yylen = 0;
    int yystate = 0;

    YYStack yystack = new YYStack ();

    /* Error handling.  */
    int yynerrs_ = 0;
    

    /// Semantic value of the lookahead.
    Object yylval = null;

    int yyresult;

    yycdebug ("Starting parse\n");
    yyerrstatus_ = 0;


    /* Initialize the stack.  */
    yystack.push (yystate, yylval);

    int label = YYNEWSTATE;
    for (;;)
      switch (label)
      {
        /* New state.  Unlike in the C/C++ skeletons, the state is already
	   pushed when we come here.  */
      case YYNEWSTATE:
        yycdebug ("Entering state " + yystate + "\n");
        if (yydebug > 0)
          yystack.print (yyDebugStream);
    
        /* Accept?  */
        if (yystate == yyfinal_)
          return true;
    
        /* Take a decision.  First try without lookahead.  */
        yyn = yypact_[yystate];
        if (yyn == yypact_ninf_)
          {
            label = YYDEFAULT;
	    break;
          }
    
        /* Read a lookahead token.  */
        if (yychar == yyempty_)
          {
	    yycdebug ("Reading a token: ");
	    yychar = yylex ();
            
            yylval = yylexer.getLVal ();
          }
    
        /* Convert token to internal form.  */
        if (yychar <= EOF)
          {
	    yychar = yytoken = EOF;
	    yycdebug ("Now at end of input.\n");
          }
        else
          {
	    yytoken = yytranslate_ (yychar);
	    yy_symbol_print ("Next token is", yytoken,
	    		     yylval);
          }
    
        /* If the proper action on seeing token YYTOKEN is to reduce or to
           detect an error, take that action.  */
        yyn += yytoken;
        if (yyn < 0 || yylast_ < yyn || yycheck_[yyn] != yytoken)
          label = YYDEFAULT;
    
        /* <= 0 means reduce or error.  */
        else if ((yyn = yytable_[yyn]) <= 0)
          {
	    if (yyn == 0 || yyn == yytable_ninf_)
	      label = YYFAIL;
	    else
	      {
	        yyn = -yyn;
	        label = YYREDUCE;
	      }
          }
    
        else
          {
            /* Shift the lookahead token.  */
	    yy_symbol_print ("Shifting", yytoken,
	    		     yylval);
    
            /* Discard the token being shifted.  */
            yychar = yyempty_;
    
            /* Count tokens shifted since error; after three, turn off error
               status.  */
            if (yyerrstatus_ > 0)
              --yyerrstatus_;
    
            yystate = yyn;
            yystack.push (yystate, yylval);
            label = YYNEWSTATE;
          }
        break;
    
      /*-----------------------------------------------------------.
      | yydefault -- do the default action for the current state.  |
      `-----------------------------------------------------------*/
      case YYDEFAULT:
        yyn = yydefact_[yystate];
        if (yyn == 0)
          label = YYFAIL;
        else
          label = YYREDUCE;
        break;
    
      /*-----------------------------.
      | yyreduce -- Do a reduction.  |
      `-----------------------------*/
      case YYREDUCE:
        yylen = yyr2_[yyn];
        label = yyaction (yyn, yystack, yylen);
	yystate = yystack.stateAt (0);
        break;
    
      /*------------------------------------.
      | yyerrlab -- here on detecting error |
      `------------------------------------*/
      case YYFAIL:
        /* If not already recovering from an error, report this error.  */
        if (yyerrstatus_ == 0)
          {
	    ++yynerrs_;
	    yyerror (yysyntax_error (yystate, yytoken));
          }
    
        
        if (yyerrstatus_ == 3)
          {
	    /* If just tried and failed to reuse lookahead token after an
	     error, discard it.  */
    
	    if (yychar <= EOF)
	      {
	      /* Return failure if at end of input.  */
	      if (yychar == EOF)
	        return false;
	      }
	    else
	      yychar = yyempty_;
          }
    
        /* Else will try to reuse lookahead token after shifting the error
           token.  */
        label = YYERRLAB1;
        break;
    
      /*---------------------------------------------------.
      | errorlab -- error raised explicitly by YYERROR.  |
      `---------------------------------------------------*/
      case YYERROR:
    
        
        /* Do not reclaim the symbols of the rule which action triggered
           this YYERROR.  */
        yystack.pop (yylen);
        yylen = 0;
        yystate = yystack.stateAt (0);
        label = YYERRLAB1;
        break;
    
      /*-------------------------------------------------------------.
      | yyerrlab1 -- common code for both syntax error and YYERROR.  |
      `-------------------------------------------------------------*/
      case YYERRLAB1:
        yyerrstatus_ = 3;	/* Each real token shifted decrements this.  */
    
        for (;;)
          {
	    yyn = yypact_[yystate];
	    if (yyn != yypact_ninf_)
	      {
	        yyn += yyterror_;
	        if (0 <= yyn && yyn <= yylast_ && yycheck_[yyn] == yyterror_)
	          {
	            yyn = yytable_[yyn];
	            if (0 < yyn)
		      break;
	          }
	      }
    
	    /* Pop the current state because it cannot handle the error token.  */
	    if (yystack.height == 1)
	      return false;
    
	    
	    yystack.pop ();
	    yystate = yystack.stateAt (0);
	    if (yydebug > 0)
	      yystack.print (yyDebugStream);
          }
    
	

        /* Shift the error token.  */
        yy_symbol_print ("Shifting", yystos_[yyn],
			 yylval);
    
        yystate = yyn;
	yystack.push (yyn, yylval);
        label = YYNEWSTATE;
        break;
    
        /* Accept.  */
      case YYACCEPT:
        return true;
    
        /* Abort.  */
      case YYABORT:
        return false;
      }
  }

  // Generate an error message.
  private String yysyntax_error (int yystate, int tok)
  {
    if (errorVerbose)
      {
        int yyn = yypact_[yystate];
        if (yypact_ninf_ < yyn && yyn <= yylast_)
          {
	    StringBuffer res;

	    /* Start YYX at -YYN if negative to avoid negative indexes in
	       YYCHECK.  */
	    int yyxbegin = yyn < 0 ? -yyn : 0;

	    /* Stay within bounds of both yycheck and yytname.  */
	    int yychecklim = yylast_ - yyn + 1;
	    int yyxend = yychecklim < yyntokens_ ? yychecklim : yyntokens_;
	    int count = 0;
	    for (int x = yyxbegin; x < yyxend; ++x)
	      if (yycheck_[x + yyn] == x && x != yyterror_)
	        ++count;

	    // FIXME: This method of building the message is not compatible
	    // with internationalization.
	    res = new StringBuffer ("syntax error, unexpected ");
	    res.append (yytnamerr_ (yytname_[tok]));
	    if (count < 5)
	      {
	        count = 0;
	        for (int x = yyxbegin; x < yyxend; ++x)
	          if (yycheck_[x + yyn] == x && x != yyterror_)
		    {
		      res.append (count++ == 0 ? ", expecting " : " or ");
		      res.append (yytnamerr_ (yytname_[x]));
		    }
	      }
	    return res.toString ();
          }
      }

    return "syntax error";
  }


  /* YYPACT[STATE-NUM] -- Index in YYTABLE of the portion describing
     STATE-NUM.  */
  private static final byte yypact_ninf_ = -9;
  private static final byte yypact_[] =
  {
        -2,    -9,    -2,    -2,    -2,    -9,    -9,    -1,    -9,     9,
      43,    -2,     2,    24,    43,    46,    -2,    -9,    -2,    -2,
      -2,    -2,    -9,    -2,    33,    -9,    -9,    -2,    16,    -8,
      -8,    48,    48,     2,    -9,    -9,    43,    -9
  };

  /* YYDEFACT[S] -- default rule to reduce with in state S when YYTABLE
     doesn't specify something else to do.  Zero means the default is an
     error.  */
  private static final byte yydefact_[] =
  {
         0,    14,     0,     0,     0,     4,     5,     0,    15,     0,
       2,     0,    13,     0,    19,     0,     0,     1,     0,     0,
       0,     0,    17,     0,     0,     8,     6,     0,     0,    11,
      12,     9,    10,    16,    18,     3,    20,     7
  };

  /* YYPGOTO[NTERM-NUM].  */
  private static final byte yypgoto_[] =
  {
        -9,    -9,     0,    -9,    -9,     6
  };

  /* YYDEFGOTO[NTERM-NUM].  */
  private static final byte
  yydefgoto_[] =
  {
        -1,     9,    14,    11,    35,    15
  };

  /* YYTABLE[YYPACT[STATE-NUM]].  What to do in state STATE-NUM.  If
     positive, shift that token.  If negative, reduce the rule which
     number is the opposite.  If zero, do what YYDEFACT says.  */
  private static final byte yytable_ninf_ = -1;
  private static final byte
  yytable_[] =
  {
        10,     1,    12,    13,    22,     2,     3,    16,     4,    17,
      23,    24,     5,     6,    22,     7,     0,     8,    29,    30,
      31,    32,    28,    33,     0,    37,     0,    36,    18,    19,
      20,    21,    27,    25,     0,     0,    22,    18,    19,    20,
      21,     0,    23,     0,     0,    22,    34,    18,    19,    20,
      21,    23,    18,    19,     0,    22,     0,    26,     0,     0,
      22,    23,    27,     0,     0,     0,    23
  };

  /* YYCHECK.  */
  private static final byte
  yycheck_[] =
  {
         0,     3,     2,     3,    12,     7,     8,     8,    10,     0,
      18,    11,    14,    15,    12,    17,    -1,    19,    18,    19,
      20,    21,    16,    23,    -1,     9,    -1,    27,     4,     5,
       6,     7,    16,     9,    -1,    -1,    12,     4,     5,     6,
       7,    -1,    18,    -1,    -1,    12,    13,     4,     5,     6,
       7,    18,     4,     5,    -1,    12,    -1,    11,    -1,    -1,
      12,    18,    16,    -1,    -1,    -1,    18
  };

  /* STOS_[STATE-NUM] -- The (internal number of the) accessing
     symbol of state STATE-NUM.  */
  private static final byte
  yystos_[] =
  {
         0,     3,     7,     8,    10,    14,    15,    17,    19,    22,
      23,    24,    23,    23,    23,    26,     8,     0,     4,     5,
       6,     7,    12,    18,    23,     9,    11,    16,    26,    23,
      23,    23,    23,    23,    13,    25,    23,     9
  };

  /* TOKEN_NUMBER_[YYLEX-NUM] -- Internal symbol number corresponding
     to YYLEX-NUM.  */
  private static final short
  yytoken_number_[] =
  {
         0,   256,   257,   258,   259,   260,   261,   262,   263,   264,
     265,   266,   267,   268,   269,   270,   271,   272,   273,   274,
     275
  };

  /* YYR1[YYN] -- Symbol number of symbol that rule YYN derives.  */
  private static final byte
  yyr1_[] =
  {
         0,    21,    22,    23,    23,    23,    23,    23,    23,    23,
      23,    23,    23,    23,    23,    23,    23,    24,    25,    26,
      26
  };

  /* YYR2[YYN] -- Number of symbols composing right hand side of rule YYN.  */
  private static final byte
  yyr2_[] =
  {
         0,     2,     1,     3,     1,     1,     3,     4,     3,     3,
       3,     3,     3,     2,     1,     1,     3,     2,     1,     1,
       3
  };

  /* YYTNAME[SYMBOL-NUM] -- String name of the symbol SYMBOL-NUM.
     First, the terminals, then, starting at \a yyntokens_, nonterminals.  */
  private static final String yytname_[] =
  {
    "$end", "error", "$undefined", "NUMBER", "MULT", "DIV", "PLUS", "MINUS",
  "LPAREN", "RPAREN", "LBRACE", "RBRACE", "LSQR", "RSQR", "PROPTAG",
  "STRING", "COMMA", "KEYWORD", "MATCH", "REFERENCE", "NEG", "$accept",
  "full_expr", "expr", "start_deref", "end_deref", "expr_list", null
  };

  /* YYRHS -- A `-1'-separated list of the rules' RHS.  */
  private static final byte yyrhs_[] =
  {
        22,     0,    -1,    23,    -1,    24,    23,    25,    -1,    14,
      -1,    15,    -1,    10,    26,    11,    -1,    17,     8,    26,
       9,    -1,     8,    23,     9,    -1,    23,     6,    23,    -1,
      23,     7,    23,    -1,    23,     4,    23,    -1,    23,     5,
      23,    -1,     7,    23,    -1,     3,    -1,    19,    -1,    23,
      18,    23,    -1,    23,    12,    -1,    13,    -1,    23,    -1,
      26,    16,    23,    -1
  };

  /* YYPRHS[YYN] -- Index of the first RHS symbol of rule number YYN in
     YYRHS.  */
  private static final byte yyprhs_[] =
  {
         0,     0,     3,     5,     9,    11,    13,    17,    22,    26,
      30,    34,    38,    42,    45,    47,    49,    53,    56,    58,
      60
  };

  /* YYRLINE[YYN] -- Source line where rule number YYN was defined.  */
  private static final short yyrline_[] =
  {
         0,    91,    91,    94,   104,   107,   110,   114,   118,   119,
     122,   125,   128,   131,   134,   137,   140,   145,   160,   163,
     168
  };

  // Report on the debug stream that the rule yyrule is going to be reduced.
  private void yy_reduce_print (int yyrule, YYStack yystack)
  {
    if (yydebug == 0)
      return;

    int yylno = yyrline_[yyrule];
    int yynrhs = yyr2_[yyrule];
    /* Print the symbols being reduced, and their result.  */
    yycdebug ("Reducing stack by rule " + (yyrule - 1)
	      + " (line " + yylno + "), ");

    /* The symbols being reduced.  */
    for (int yyi = 0; yyi < yynrhs; yyi++)
      yy_symbol_print ("   $" + (yyi + 1) + " =",
		       yyrhs_[yyprhs_[yyrule] + yyi],
		       ((yystack.valueAt (yynrhs-(yyi + 1)))));
  }

  /* YYTRANSLATE(YYLEX) -- Bison symbol number corresponding to YYLEX.  */
  private static final byte yytranslate_table_[] =
  {
         0,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     1,     2,     3,     4,
       5,     6,     7,     8,     9,    10,    11,    12,    13,    14,
      15,    16,    17,    18,    19,    20
  };

  private static final byte yytranslate_ (int t)
  {
    if (t >= 0 && t <= yyuser_token_number_max_)
      return yytranslate_table_[t];
    else
      return yyundef_token_;
  }

  private static final int yylast_ = 66;
  private static final int yynnts_ = 6;
  private static final int yyempty_ = -2;
  private static final int yyfinal_ = 17;
  private static final int yyterror_ = 1;
  private static final int yyerrcode_ = 256;
  private static final int yyntokens_ = 21;

  private static final int yyuser_token_number_max_ = 275;
  private static final int yyundef_token_ = 2;

/* User implementation code.  */
/* Unqualified %code blocks.  */

/* Line 875 of lalr1.java  */
/* Line 47 of "expr.y"  */
  
  private final Stack<PropertyFinder> registry=new Stack<PropertyFinder>();
  private AppContext conn;

  private PropExpression result=null;

  public void init(AppContext conn, PropertyFinder finder){
    this.conn=conn;
    push(finder);
  }
  public PropExpression getExpression(){
    return result;
  }
  private void push(PropertyFinder finder){
	  registry.push(finder);
  }
  private void pop(){
	  registry.pop();
  }
  
  private PropertyTag find(String name) throws InvalidPropertyException{
       return registry.peek().make(name);
  }

  private PropExpression<? extends Number> castNumber(PropExpression<?> exp) throws ParseException{
    if( Number.class.isAssignableFrom(exp.getTarget())){
      return (PropExpression<Number>) exp;
    }
    if( Date.class.isAssignableFrom(exp.getTarget())){
      try {
	return new MilliSecondDatePropExpression((PropExpression<java.util.Date>) exp);
      } catch (PropertyCastException e) {
	throw new ParseException(e);
      }
    }
    throw new ParseException("Cannot convert expression to number :"+exp+" Type:"+exp.getTarget().getSimpleName());
  }




/* Line 875 of lalr1.java  */
/* Line 1177 of "expr.java"  */

}


