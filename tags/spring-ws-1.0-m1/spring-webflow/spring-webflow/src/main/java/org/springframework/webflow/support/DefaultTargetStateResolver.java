/*
 * Copyright 2002-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.webflow.support;

import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.support.StaticExpression;
import org.springframework.util.Assert;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.State;
import org.springframework.webflow.TargetStateResolver;
import org.springframework.webflow.Transition;
import org.springframework.webflow.TransitionableState;

/**
 * A transition target state resolver that always resolves to the same target
 * state.
 * 
 * @author Keith Donald
 */
public class DefaultTargetStateResolver implements TargetStateResolver {

	/**
	 * The expression for the target state identifier.
	 */
	private Expression targetStateId;

	/**
	 * Creates a new static target state resolver that always returns the same
	 * target state.
	 * @param targetStateId the id of the target state
	 */
	public DefaultTargetStateResolver(String targetStateId) {
		this(new StaticExpression(targetStateId));
	}

	/**
	 * Creates a new target state resolver.
	 * @param targetStateIdExpression the target state id expression
	 */
	public DefaultTargetStateResolver(Expression targetStateIdExpression) {
		Assert.notNull(targetStateIdExpression, "The target state id expression is required");
		this.targetStateId = targetStateIdExpression;
	}

	/**
	 * Returns the id expression of the target state resolved by this resolver.
	 */
	public Expression getTargetStateId() {
		return targetStateId;
	}

	public State resolveTargetState(Transition transition, TransitionableState sourceState, RequestContext context) {
		String stateId = String.valueOf(targetStateId.evaluateAgainst(context, null));
		return sourceState.getFlow().getRequiredState(stateId);
	}

	public String toString() {
		return "[targetStateId = '" + targetStateId + "']";
	}
}