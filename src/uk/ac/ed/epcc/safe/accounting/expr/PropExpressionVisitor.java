// Copyright - The University of Edinburgh 2011
/*******************************************************************************
 * Copyright (c) - The University of Edinburgh 2010
 *******************************************************************************/
package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.properties.BasePropExpressionVisitor;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
/** A visitor interface for PropExpressions. 
 * 
 * This class handles the extended set of expressions.
 * 
 
 * @author spb
 *
 * @param <R> type returned by visitor
 */
public interface  PropExpressionVisitor<R> extends BasePropExpressionVisitor<R>{
  public R visitStringPropExpression(StringPropExpression<?> stringExpression) throws Exception;
  public R visitIntPropExpression(IntPropExpression<?> intExpression) throws Exception;
  public R visitLongCastPropExpression(LongCastPropExpression<?> intExpression) throws Exception;
  public R visitDoubleCastPropExpression(DoubleCastPropExpression<?> doubleExpression) throws Exception;
  public R visitConstPropExpression(ConstPropExpression<?> constExpression) throws Exception;
  public R visitBinaryPropExpression(BinaryPropExpression binaryPropExpression) throws Exception;
  public R visitMilliSecondDatePropExpression(MilliSecondDatePropExpression milliSecondDate) throws Exception;
  public R visitNamePropExpression(NamePropExpression namePropExpression) throws Exception;
  public <T extends DataObject & ExpressionTarget> R visitDeRefExpression(DeRefExpression<T,?> deRefExpression) throws Exception;
 
  public R visitSelectPropExpression(SelectPropExpression<?> sel) throws Exception;
  public R visitDurationPropExpression(DurationPropExpression sel) throws Exception;
  public R visitDurationCastPropExpression(DurationCastPropExpression<?> sel) throws Exception;
  public R visitDurationSecondPropExpression(DurationSecondsPropExpression d)throws Exception;
  public <T,D> R visitTypeConverterPropExpression(TypeConverterPropExpression<T,D> sel) throws Exception;
  public <T,X> R visitLabelPropExpression(LabelPropExpression<T,X> expr) throws Exception;
  public <T> R visitCasePropExpression(CasePropExpression<T> expr)  throws Exception;
  public R visitConvetMillisecondToDateExpression(ConvertMillisecondToDatePropExpression expr)throws Exception;
  public <C extends Comparable> R visitCompareExpression(ComparePropExpression<C> expr) throws Exception;
}