package org.springframework.binding.expression;

import java.util.Map;

import org.springframework.core.style.ToStringCreator;

/**
 * Records an attempt to set a property.
 * 
 * @author Keith Donald
 */
public class SetPropertyAttempt extends EvaluationAttempt {

	private Object value;

	/**
	 * Creates a new set property attempt.
	 * @param expression
	 * @param target
	 * @param value
	 * @param setContext
	 */
	public SetPropertyAttempt(Expression expression, Object target, Object value, Map setContext) {
		super(expression, target, setContext);
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	protected ToStringCreator createToString(ToStringCreator creator) {
		return super.createToString(creator).append("value", value);
	}
}