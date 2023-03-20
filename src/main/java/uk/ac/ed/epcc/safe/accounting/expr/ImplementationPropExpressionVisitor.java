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
package uk.ac.ed.epcc.safe.accounting.expr;

import uk.ac.ed.epcc.safe.accounting.properties.MethodPropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.webapp.Indexed;
import uk.ac.ed.epcc.webapp.model.data.DataObject;
/** Class that expands derived properties to generate an implementation string.
 * This is essentially the same as the {@link #toString()} method on the expression
 * except that nested expressions are expanded to leaf values.
 * @author spb
 *
 */
public abstract class ImplementationPropExpressionVisitor implements
		PropExpressionVisitor<String> {
	

	public String visitStringPropExpression(
			StringPropExpression<?> stringExpression) throws Exception {
		return "String("+stringExpression.getExpression().accept(this)+")";
	}

	public String visitIntPropExpression(IntPropExpression<?> intExpression)
			throws Exception {
		return "Int("+intExpression.getExpression().accept(this)+")";
	}
	public String visitFloorPropExpression(FloorPropExpression<?> intExpression)
			throws Exception {
		return "Floor("+intExpression.getExpression().accept(this)+")";
	}
	public String visitCeilPropExpression(CeilPropExpression<?> intExpression)
			throws Exception {
		return "Ceil("+intExpression.getExpression().accept(this)+")";
	}
	public String visitLongCastPropExpression(LongCastPropExpression<?> intExpression)
			throws Exception {
		return "Long("+intExpression.getExpression().accept(this)+")";
	}
	public String visitDoubleCastPropExpression(
			DoubleCastPropExpression<?> doubleExpression) throws Exception {
		return "Double("+doubleExpression.getExpression().accept(this)+")";
	}

	public String visitDurationSecondPropExpression(
			DurationSecondsPropExpression e) throws Exception {
		return "Seconds("+e.getDuration().accept(this)+")";
	}
	public String visitConstPropExpression(
			ConstPropExpression<?> constExpression) throws Exception {
		return constExpression.toString();
	}

	public String visitBinaryPropExpression(
			BinaryPropExpression binaryPropExpression) throws Exception {
		//TODO suppress unnecessary brackets
		return "("+binaryPropExpression.a.accept(this)+binaryPropExpression.op.text()+binaryPropExpression.b.accept(this)+")";
	}

	public String visitMilliSecondDatePropExpression(
			MilliSecondDatePropExpression milliSecondDate) throws Exception {
		
		return milliSecondDate.toString();
	}

	public String visitNamePropExpression(NamePropExpression namePropExpression)
			throws Exception {
		return namePropExpression.toString();
	}
	public <T extends DataObject> String visitDoubleDeRefExpression(
			DoubleDeRefExpression<T, ?> deRefExpression) throws Exception {
		return deRefExpression.toString();
	}
	public <T extends DataObject> String visitDeRefExpression(
			DeRefExpression<T, ?> deRefExpression) throws Exception {
		return deRefExpression.toString();
	}

	public String visitSelectPropExpression(SelectPropExpression<?> sel)
			throws Exception {
		return sel.toString();
	}

	public String visitDurationPropExpression(DurationPropExpression sel)
			throws Exception {
		return "Duration("+sel.start.accept(this)+","+sel.end.accept(this)+")";
	}

	public String visitDurationCastPropExpression(
			DurationCastPropExpression<?> sel) throws Exception {
		return "Duration("+sel.exp.accept(this)+","+sel.resolution+")";
	}

	public <T, D> String visitTypeConverterPropExpression(
			TypeConverterPropExpression<T, D> sel) throws Exception {
		return sel.getConverter().getClass().getSimpleName()+"("+sel.getInnerExpression().accept(this)+")";
	}

	public <T, X> String visitLabelPropExpression(LabelPropExpression<T, X> expr)
			throws Exception {
		
		return "Label("+expr.getLabeller().getClass().getSimpleName()+","+expr.getExpr().accept(this)+")";
	}

	public <T> String visitCasePropExpression(CasePropExpression<T> expr)
			throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("CASE(");
		for(CasePropExpression.Case<T> c: expr.getCases()){
			sb.append(c.sel.toString());
			sb.append(":");
			sb.append(c.expr.accept(this));
			sb.append(";");
		}
		PropExpression<? extends T> def = expr.getDefaultExpression();
		if( def != null ){
			sb.append("default:");
			sb.append(def.accept(this));
		}
		sb.append(")");
		return sb.toString();
	}

	public String visitConvetMillisecondToDateExpression(
			ConvertMillisecondToDatePropExpression expr) throws Exception {
		return expr.toString();
	}

	@Override
	public <C extends Comparable> String visitCompareExpression(
			ComparePropExpression<C> expr) throws Exception {

		return "("+expr.e1.accept(this)+(expr.m==null?"==":expr.m.toString())+expr.e2.accept(this)+")";
	}

	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.expr.PropExpressionVisitor#visitConstReferenceExpression(uk.ac.ed.epcc.safe.accounting.expr.ConstReferenceExpression)
	 */
	@Override
	public <I extends Indexed> String visitConstReferenceExpression(ConstReferenceExpression<I> expr) throws Exception {
		return visitConstPropExpression(expr);
	}

	public String visitLocatePropExpression(
			LocatePropExpression loc) throws Exception {
		return "Locate("+loc.substr.accept(this)+","+loc.str.accept(this)+","+loc.pos.accept(this)+")";
	}

	/* (non-Javadoc)
	 * @see uk.ac.ed.epcc.safe.accounting.expr.PropExpressionVisitor#visitArrayFuncPropExpression(uk.ac.ed.epcc.safe.accounting.expr.ArrayFuncPropExpression)
	 */
	@Override
	public <T extends Comparable> String visitArrayFuncPropExpression(ArrayFuncPropExpression<T> expr)
			throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(expr.getFunc().name());
		sb.append("(");
		boolean seen=false;
		for(PropExpression e: expr){
			if(seen){
				sb.append(",");
			}
			sb.append(e.accept(this));
			seen=true;
		}
		sb.append(")");
		return sb.toString();
	}

	@Override
	public String visitMethodPropExpression(MethodPropExpression<?> method) throws Exception {
		return method.toString();
	}
}