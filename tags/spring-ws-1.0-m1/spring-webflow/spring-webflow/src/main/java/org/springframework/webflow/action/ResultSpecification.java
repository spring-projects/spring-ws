package org.springframework.webflow.action;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ScopeType;

/**
 * Object encapsulating the data and behavior about how a result value should be
 * exposed to a flow execution context.
 * @see AbstractBeanInvokingAction
 * @author Keith Donald
 */
public class ResultSpecification {

	/**
	 * The name of the attribute to index the return value of the invoked bean
	 * method. Optional; the default is <code>null</code> which simply results
	 * in ignoring the return value.
	 */
	private String resultName;

	/**
	 * The scope the attribute indexing the return value of the invoked bean
	 * method. Default is {@link ScopeType#REQUEST).
	 */
	private ScopeType resultScope;

	/**
	 * Creates a new result specification.
	 * @param resultName the result name
	 * @param resultScope the result scope
	 */
	public ResultSpecification(String resultName, ScopeType resultScope) {
		Assert.notNull(resultName, "The result name is required");
		Assert.notNull(resultScope, "The result scope is required");
		this.resultName = resultName;
		this.resultScope = resultScope;
	}

	/**
	 * Returns name of the attribute to index the return value of the invoked
	 * bean method.
	 */
	public String getResultName() {
		return resultName;
	}

	/**
	 * Returns the scope the attribute indexing the return value of the invoked
	 * bean method.
	 */
	public ScopeType getResultScope() {
		return resultScope;
	}

	/**
	 * Exposes the return value as an attribute in a configured scope, if
	 * necessary. Subclasses may override.
	 * @param returnValue the return value
	 * @param context the request context
	 */
	public void exposeResult(Object returnValue, RequestContext context) {
		resultScope.getScope(context).put(resultName, returnValue);
	}

	public String toString() {
		return new ToStringCreator(this).append("resultName", resultName).append("resultScope", resultScope).toString();
	}
}