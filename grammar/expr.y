%language {Java}
%define parser_class_name {ExpressionParser}
%define package {uk.ac.ed.epcc.safe.accounting.expr.parse}

%code imports{
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
}

%define lex_throws {LexException}
%define throws {Exception}

%token NUMBER MULT DIV PLUS MINUS LPAREN RPAREN LBRACE RBRACE LSQR RSQR PROPTAG STRING COMMA KEYWORD MATCH REFERENCE

%left  RPAREN
%left  RSQR
%left  PLUS MINUS
%left  MULT DIV
%left MATCH
%left NEG


%type<Number> NUMBER
%type<String> STRING
%type<String> PROPTAG
%type<Keywords> KEYWORD
%type<MatchCondition> MATCH
%type<IndexedReference> REFERENCE
%type<PropExpression> expr start_deref
%type<LinkedList> expr_list

%code {  
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

}



%%

full_expr: expr { result = $1; }
;

expr : start_deref expr end_deref	{
  ReferenceExpression tag = (ReferenceExpression)  $1;
  PropExpression inner = $2;
  if( inner instanceof ReferenceExpression){
    $$ =new DoubleDeRefExpression(tag,(ReferenceExpression) inner);
  }else{
    $$ = new DeRefExpression(tag,inner);
  }
   
} 
| PROPTAG			{
  $$ = find($1); 
}
| STRING			{
  $$ = new ConstPropExpression(String.class,$1); 
}
| LBRACE expr_list RBRACE { 
  LinkedList<PropExpression> list =$2;
  $$ = SelectPropExpression.makeSelect(list.toArray(new PropExpression[list.size()])); 
}
| KEYWORD LPAREN expr_list RPAREN {
  Keywords op = $1;
  $$ = op.getExpression($3);
}
|  LPAREN expr RPAREN { $$ = castNumber($2); }
| expr PLUS  expr           {
  $$ = new BinaryPropExpression(castNumber($1),Operator.ADD,castNumber($3)); 
}
| expr MINUS expr           {
  $$ = new BinaryPropExpression(castNumber($1),Operator.SUB,castNumber($3)); 
}
| expr MULT  expr           {
  $$ = new BinaryPropExpression(castNumber($1),Operator.MUL, castNumber($3)); 
}
| expr DIV   expr           { 
  $$ = new BinaryPropExpression(castNumber($1),Operator.DIV, castNumber($3)); 
}
| MINUS expr   %prec NEG  {
  $$ = new NegatePropExpression(castNumber($2));    
}
| NUMBER                      {
  $$=new ConstPropExpression(Number.class,$1);    
}
| REFERENCE                      {
  $$=new ConstReferenceExpression($1);    
}
| expr MATCH expr {
  $$= new ComparePropExpression($1,$2,$3);
}
;

start_deref : expr  LSQR	{
  if( $1 instanceof ReferenceExpression ){
    ReferenceExpression tag = (ReferenceExpression)  $1;
    ExpressionTargetFactory etf = ExpressionCast.getExpressionTargetFactory(tag.getFactory(conn));
    if( etf != null){
      push(etf.getFinder());
    }else{
      throw new ParseException("Illegal target of dereference "+$1.toString());
    }
    $$=tag;
  }else{
     throw new ParseException("Illegal dereference of "+$1.toString()); 
  }
}
;


end_deref	: RSQR		{ pop(); }
;

expr_list :  expr {
  LinkedList list = new LinkedList();
  list.add($1);
  $$=list;
}
| expr_list COMMA expr {
  LinkedList list = $1;
  list.add($3);
  $$=list;
}
; 


