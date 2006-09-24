package org.springframework.binding.expression;

import java.util.Map;

/**
 * An evaluator that is capable of setting a property on a target object at the
 * value path defined by an expression.
 * @author Keith Donald
 */
public interface PropertyExpression extends Expression {

	/**
	 * Set the value of this property expression on the target object to the
	 * value provided.
	 * @param target the target object
	 * @param value the value
	 * @param setContext the evaluation and setter context
	 */
	public void setValue(Object target, Object value, Map setContext) throws EvaluationException;
}