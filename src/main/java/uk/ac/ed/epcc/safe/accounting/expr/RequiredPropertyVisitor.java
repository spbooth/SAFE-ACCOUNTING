package uk.ac.ed.epcc.safe.accounting.expr;

import java.util.Set;

import uk.ac.ed.epcc.safe.accounting.properties.MethodPropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropExpression;
import uk.ac.ed.epcc.safe.accounting.properties.PropertyTag;
import uk.ac.ed.epcc.webapp.Indexed;
import uk.ac.ed.epcc.webapp.model.data.DataObject;

/**  A {@link PropExpressionVisitor} to generate the set of leaf properties used by an epression
 * 
 * @author Stephen Booth
 *
 */
public class RequiredPropertyVisitor implements PropExpressionVisitor<Object>{

	/**
	 * @param required
	 */
	public RequiredPropertyVisitor(Set<PropertyTag> required) {
		super();
		this.required = required;
	}

	private final Set<PropertyTag> required;

	@Override
	public Object visitPropertyTag(PropertyTag<?> tag) throws Exception {
		return required.add(tag);
	}

	@Override
	public Object visitStringPropExpression(StringPropExpression<?> stringExpression) throws Exception {
		return stringExpression.getExpression().accept(this);
	}

	@Override
	public Object visitIntPropExpression(IntPropExpression<?> intExpression) throws Exception {
		return intExpression.getExpression().accept(this);
	}

	@Override
	public Object visitLongCastPropExpression(LongCastPropExpression<?> intExpression) throws Exception {
		return intExpression.getExpression().accept(this);
	}

	@Override
	public Object visitDoubleCastPropExpression(DoubleCastPropExpression<?> doubleExpression) throws Exception {
		return doubleExpression.getExpression().accept(this);
	}

	@Override
	public Object visitConstPropExpression(ConstPropExpression<?> constExpression) throws Exception {
		return null;
	}

	@Override
	public Object visitBinaryPropExpression(BinaryPropExpression binaryPropExpression) throws Exception {
		binaryPropExpression.a.accept(this);
		binaryPropExpression.b.accept(this);
		return null;
	}

	@Override
	public Object visitMilliSecondDatePropExpression(MilliSecondDatePropExpression milliSecondDate) throws Exception {
		return milliSecondDate.getDateExpression().accept(this);
	}

	@Override
	public Object visitNamePropExpression(NamePropExpression namePropExpression) throws Exception {
		return namePropExpression.getTargetRef().accept(this);
	}

	@Override
	public <T extends DataObject> Object visitDeRefExpression(DeRefExpression<T, ?> deRefExpression) throws Exception {
		return deRefExpression.getTargetObject().accept(this);
	}

	@Override
	public <T extends DataObject> Object visitDoubleDeRefExpression(DoubleDeRefExpression<T, ?> deRefExpression)
			throws Exception {
		return deRefExpression.getTargetObject().accept(this);
	}

	@Override
	public Object visitSelectPropExpression(SelectPropExpression<?> sel) throws Exception {
		// assume all of a select are optional
		return null;
	}

	@Override
	public Object visitDurationPropExpression(DurationPropExpression sel) throws Exception {
		sel.start.accept(this);
		sel.end.accept(this);
		return null;
	}

	@Override
	public Object visitDurationCastPropExpression(DurationCastPropExpression<?> sel) throws Exception {
		return sel.exp.accept(this);
	}

	@Override
	public Object visitDurationSecondPropExpression(DurationSecondsPropExpression d) throws Exception {
		return d.getDuration().accept(this);
	}

	@Override
	public Object visitLocatePropExpression(LocatePropExpression expr) throws Exception {
		expr.pos.accept(this);
		expr.str.accept(this);
		expr.substr.accept(this);
		return null;
	}

	@Override
	public <T extends Comparable> Object visitArrayFuncPropExpression(ArrayFuncPropExpression<T> expr)
			throws Exception {
		for(PropExpression<T> e : expr) {
			e.accept(this);
		}
		return null;
	}

	@Override
	public <T, D> Object visitTypeConverterPropExpression(TypeConverterPropExpression<T, D> sel) throws Exception {
		return sel.getInnerExpression().accept(this);
	}

	@Override
	public <T, X> Object visitLabelPropExpression(LabelPropExpression<T, X> expr) throws Exception {
		return expr.getExpr().accept(this);
	}

	@Override
	public <T> Object visitCasePropExpression(CasePropExpression<T> expr) throws Exception {
		// assume all optional
		return null;
	}

	@Override
	public Object visitConvetMillisecondToDateExpression(ConvertMillisecondToDatePropExpression expr) throws Exception {
		return expr.getMillisecondExpression().accept(this);
	}

	@Override
	public <C extends Comparable> Object visitCompareExpression(ComparePropExpression<C> expr) throws Exception {
		expr.e1.accept(this);
		expr.e2.accept(this);
		return null;
	}

	@Override
	public <I extends Indexed> Object visitConstReferenceExpression(ConstReferenceExpression<I> expr) throws Exception {
		return null;
	}

	@Override
	public Object visitMethodPropExpression(MethodPropExpression<?> method) throws Exception {
		required.addAll(method.required());
		return null;
	}
}
