package org.springframework.binding.expression;

import java.io.Serializable;
import java.util.Map;

import org.springframework.core.style.ToStringCreator;

/**
 * A simple holder for information about an evaluation attempt.
 * 
 * @author Keith
 */
public class EvaluationAttempt implements Serializable {

	/**
	 * The expression being evaluated.
	 */
	private Expression expression;

	/**
	 * The target object being evaluated on.
	 */
	private Object target;

	/**
	 * The evaluation context.
	 */
	private Map evaluationContext;

	/**
	 * Create an evaluation attempt.
	 * 
	 * @param expression
	 * @param target
	 * @param evaluationContext
	 */
	public EvaluationAttempt(Expression expression, Object target, Map evaluationContext) {
		this.expression = expression;
		this.target = target;
		this.evaluationContext = evaluationContext;
	}

	public Expression getExpression() {
		return expression;
	}

	public Object getTarget() {
		return target;
	}

	public Map getEvaluationContext() {
		return evaluationContext;
	}

	public String toString() {
		return createToString(new ToStringCreator(this)).toString();
	}

	protected ToStringCreator createToString(ToStringCreator creator) {
		return creator.append("expression", expression).append("target", target).append("context", evaluationContext);
	}
}