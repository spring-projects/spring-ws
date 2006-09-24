package org.springframework.binding.expression.support;

import java.util.Map;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.binding.expression.EvaluationAttempt;
import org.springframework.binding.expression.EvaluationException;
import org.springframework.binding.expression.PropertyExpression;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;

/**
 * An expression evaluator that uses the spring bean wrapper.
 * 
 * @author Keith Donald
 */
public class BeanWrapperExpression implements PropertyExpression {

	/**
	 * The expression.
	 */
	private String expression;

	public BeanWrapperExpression(String expression) {
		this.expression = expression;
	}

	public int hashCode() {
		return expression.hashCode();
	}

	public boolean equals(Object o) {
		if (!(o instanceof BeanWrapperExpression)) {
			return false;
		}
		BeanWrapperExpression other = (BeanWrapperExpression)o;
		return expression.equals(other.expression);
	}

	public Object evaluateAgainst(Object target, Map evaluationContext) throws EvaluationException {
		try {
			return new BeanWrapperImpl(target).getPropertyValue(expression);
		}
		catch (BeansException e) {
			throw new EvaluationException(new EvaluationAttempt(this, target, evaluationContext), e);
		}
	}

	public void setValue(Object target, Object value, Map setContext) throws EvaluationException {
		try {
			Assert.notNull(target, "The target object to evaluate is required");
			new BeanWrapperImpl(target).setPropertyValue(expression, value);
		}
		catch (BeansException e) {
			throw new EvaluationException(new EvaluationAttempt(this, target, setContext), e);
		}
	}

	public String toString() {
		return new ToStringCreator(this).append("expression", expression).toString();
	}
}