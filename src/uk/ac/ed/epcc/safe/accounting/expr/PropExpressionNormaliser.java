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
package uk.ac.ed.epcc.safe.accounting.expr;

import java.util.Date;

import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.safe.accounting.reference.ReferenceExpression;
import uk.ac.ed.epcc.webapp.Indexed;
import uk.ac.ed.epcc.webapp.jdbc.expr.Operator;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
import uk.ac.ed.epcc.webapp.model.data.Duration;

/** A PropExpressionVisitor that re-writes a PropExpression to normalised form.
 * Mostly it copies through the input expression.
 * 
 * This is intended to perform simple optimisations. 
 * Note it will not expand PropertyTags as these may have different expansions on different
 * objects and PropExpressions may dereference to other objects.
 * 
 * @author spb
 *
 */


public class PropExpressionNormaliser implements PropExpressionVisitor<PropExpression> {

	
	public PropExpression visitStringPropExpression(
			StringPropExpression<?> stringExpression) throws Exception {
		return stringExpression;
	}

	
	public PropExpression visitIntPropExpression(
			IntPropExpression<?> intExpression) throws Exception {
		return intExpression;
	}
	public PropExpression visitLongCastPropExpression(
			LongCastPropExpression<?> intExpression) throws Exception {
		return intExpression;
	}

	
	public PropExpression visitDoubleCastPropExpression(
			DoubleCastPropExpression<?> doubleExpression) throws Exception {
		return doubleExpression;
	}

	
	public PropExpression visitConstPropExpression(
			ConstPropExpression<?> constExpression) throws Exception {
		return constExpression;
	}

	
	public PropExpression visitBinaryPropExpression(
			BinaryPropExpression binaryPropExpression) throws Exception {
		return binaryPropExpression;
	}

	
	public PropExpression<? extends Number> visitMilliSecondDatePropExpression(
			MilliSecondDatePropExpression milliSecondDate) throws Exception {
		PropExpression<Date> date_expr=milliSecondDate.getDateExpression();
		if( date_expr instanceof ConvertMillisecondToDatePropExpression){
			ConvertMillisecondToDatePropExpression d = (ConvertMillisecondToDatePropExpression)date_expr;
			return d.getMillisecondExpression();
		}
		return milliSecondDate;
	}

	
	public PropExpression visitNamePropExpression(
			NamePropExpression namePropExpression) throws Exception {
		return namePropExpression;
	}
	public <T extends DataObject & ExpressionTarget> PropExpression visitDoubleDeRefExpression(
			DoubleDeRefExpression<T, ?> deRefExpression) throws Exception {
		return visitDeRefExpression(deRefExpression);
	}
	@SuppressWarnings("unchecked")
	public <T extends DataObject & ExpressionTarget> PropExpression visitDeRefExpression(
			DeRefExpression<T, ?> deRefExpression) throws Exception {
		
		
		PropExpression nested = deRefExpression.getExpression().accept(this);
		if( nested instanceof ReferenceExpression ){
			if( deRefExpression instanceof DoubleDeRefExpression){
				return deRefExpression;
			}
			// convert to DoubleDeRefExpression if we can.
			return new DoubleDeRefExpression((ReferenceExpression) deRefExpression.getTargetObject().accept(this),(ReferenceExpression) nested);
		}else if( nested instanceof NamePropExpression){
			NamePropExpression npe = (NamePropExpression)nested;
			return new NamePropExpression(new DoubleDeRefExpression((ReferenceExpression) deRefExpression.getTargetObject().accept(this), npe.getTargetRef()));
		}else if( nested instanceof StringPropExpression){
			StringPropExpression npe = (StringPropExpression)nested;
			return new StringPropExpression(new DeRefExpression((ReferenceExpression) deRefExpression.getTargetObject().accept(this), npe.getExpression()));
		}else if( nested instanceof IntPropExpression){
			IntPropExpression npe = (IntPropExpression)nested;
			return new IntPropExpression(new DeRefExpression((ReferenceExpression) deRefExpression.getTargetObject().accept(this), npe.getExpression()));
		}else if( nested instanceof TypeConverterPropExpression){
			TypeConverterPropExpression npe = (TypeConverterPropExpression)nested;
			return new TypeConverterPropExpression(npe.getConverter(),new DeRefExpression((ReferenceExpression) deRefExpression.getTargetObject().accept(this), npe.getInnerExpression()));
		}
		return deRefExpression;
	}

	
	public PropExpression visitPropertyTag(PropertyTag<?> tag) throws Exception {
		return tag;
	}

	
	public PropExpression visitSelectPropExpression(SelectPropExpression<?> sel)
			throws Exception {
		return sel;
	}

	
	public PropExpression visitDurationPropExpression(DurationPropExpression sel)
			throws Exception {
		return sel;
	}

	
	public PropExpression visitDurationCastPropExpression(
			DurationCastPropExpression<?> sel) throws Exception {
		if(sel.exp instanceof DurationSecondsPropExpression){
			return ((DurationSecondsPropExpression)sel.exp).getDuration();
		}
		return sel;
	}

	
	public <T, D> PropExpression visitTypeConverterPropExpression(
			TypeConverterPropExpression<T, D> sel) throws Exception {
		return sel;
	}


	public <T,R> PropExpression visitLabelPropExpression(
			LabelPropExpression<T,R> expr) throws Exception {
		return expr;
	}


	public PropExpression visitDurationSecondPropExpression(
			DurationSecondsPropExpression d) throws Exception {
		PropExpression<Duration> nest = d.getDuration();
		if( nest != null && nest instanceof DurationCastPropExpression){
			
			DurationCastPropExpression durexp = (DurationCastPropExpression)nest;
			PropExpression expression = durexp.exp;
			long resolution = durexp.resolution;
			if( resolution == 1000L){
				// underlying expression already in seconds.
				return expression;
			}
			// convert numerically as this may allow SQLExpression optimisation
			return new BinaryPropExpression(new BinaryPropExpression(expression, Operator.MUL, new ConstPropExpression<Number>(Number.class, resolution)),
				Operator.DIV, new ConstPropExpression<Number>(Number.class, 1000L));
		}
			
		return d;
	}


	public <T> PropExpression visitCasePropExpression(CasePropExpression<T> expr)
			throws Exception {
		return expr;
	}


	public PropExpression<? extends Date> visitConvetMillisecondToDateExpression(
			ConvertMillisecondToDatePropExpression expr) throws Exception {
		if( expr.milli_expr instanceof MilliSecondDatePropExpression){
			MilliSecondDatePropExpression d = (MilliSecondDatePropExpression) expr.milli_expr;
			return d.getDateExpression();
		}
		return expr;
	}


	@Override
	public <C extends Comparable> PropExpression visitCompareExpression(
			ComparePropExpression<C> expr) throws Exception {
		return expr;
	}


	@Override
	public <I extends Indexed> PropExpression visitConstReferenceExpression(ConstReferenceExpression<I> expr)
			throws Exception {
		return expr;
	}

}