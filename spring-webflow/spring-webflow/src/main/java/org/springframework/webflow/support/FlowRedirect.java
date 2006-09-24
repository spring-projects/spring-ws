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

import java.util.Collections;
import java.util.Map;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.ViewSelection;

/**
 * Concrete response type that requests that a <i>new</u> flow execution
 * (representing the start of a new conversation) be launched.
 * <p>
 * This allows "redirect to new flow" semantics; useful for restarting a flow
 * after completion, or starting an entirely new flow from within the end state
 * of another flow definition.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public final class FlowRedirect extends ViewSelection {

	/**
	 * The id of the flow definition to launch.
	 */
	private final String flowId;

	/**
	 * A map of input attributes to pass to the flow.
	 */
	private final Map input;

	/**
	 * Creates a new flow redirect.
	 * @param flowId the id of the flow to launch
	 * @param input the input data to pass to the flow.
	 */
	public FlowRedirect(String flowId, Map input) {
		Assert.hasText(flowId, "The flow id is required");
		this.flowId = flowId;
		if (input == null) {
			input = Collections.EMPTY_MAP;
		}
		this.input = input;
	}

	/**
	 * Return the id of the flow definition to launch.
	 */
	public String getFlowId() {
		return flowId;
	}

	/**
	 * Return the flow input map as an unmodifiable map. Never returns null.
	 */
	public Map getInput() {
		return Collections.unmodifiableMap(input);
	}

	public boolean equals(Object o) {
		if (!(o instanceof FlowRedirect)) {
			return false;
		}
		FlowRedirect other = (FlowRedirect)o;
		return flowId.equals(other.flowId) && input.equals(other.input);
	}

	public int hashCode() {
		return flowId.hashCode() + input.hashCode();
	}

	public String toString() {
		return new ToStringCreator(this).append("flowId", flowId).append("input", input).toString();
	}
}