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
package org.springframework.webflow;

import java.io.Serializable;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;

/**
 * A value object that defines a specification for a flow variable. Encapsulates
 * information about the variable (name and type), and the behavior necessary
 * to create a new variable instance in a flow execution scope.
 * @author Keith Donald
 */
public abstract class FlowVariable implements Serializable {

	/**
	 * The variable name.
	 */
	private String name;

	/**
	 * The variable scope.
	 */
	private ScopeType scope;

	/**
	 * Creates a new flow variable.
	 * @param name the variable name
	 */
	public FlowVariable(String name) {
		this(name, null);
	}

	/**
	 * Creates a new flow variable.
	 * @param name the variable name
	 * @param scope the variable scope type
	 */
	public FlowVariable(String name, ScopeType scope) {
		Assert.hasText(name, "The variable name is required");
		this.name = name;
		this.scope = (scope != null ? scope : ScopeType.FLOW);
	}

	/**
	 * Returns the name of this variable.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the scope of this variable.
	 */
	public ScopeType getScope() {
		return scope;
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof FlowVariable)) {
			return false;
		}
		FlowVariable other = (FlowVariable)o;
		return name.equals(other.name) && scope.equals(other.scope);
	}

	public int hashCode() {
		return name.hashCode() + scope.hashCode();
	}

	/**
	 * Creates a new instance of this flow variable in flow scope.
	 * @param context the flow execution request context
	 */
	public final void create(RequestContext context) {
		scope.getScope(context).put(name, createVariableValue(context));
	}

	protected abstract Object createVariableValue(RequestContext context);
	
	public String toString() {
		return new ToStringCreator(this).append("name", name).append("scope", scope).toString();
	}
}