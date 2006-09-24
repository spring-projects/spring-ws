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

import org.springframework.core.style.StylerUtils;

/**
 * Thrown when a state could not be found in a flow on lookup by
 * <code>stateId</code>.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class NoSuchStateException extends FlowArtifactException {

	/**
	 * The flow where the state could not be found.
	 */
	private Flow flow;

	/**
	 * Create a new flow state lookup exception.
	 * @param flow the containing flow
	 * @param stateId the state id that cannot be found
	 */
	public NoSuchStateException(Flow flow, String stateId) {
		this(flow, stateId, null);
	}

	/**
	 * Create a new flow state lookup exception.
	 * @param flow the containing flow
	 * @param stateId the state id that cannot be found
	 * @param cause the underlying cause of this exception
	 */
	public NoSuchStateException(Flow flow, String stateId, Throwable cause) {
		super(stateId, State.class, "No state with state id '" + stateId + "' exists for flow '" + flow.getId()
				+ "' -- valid states are " + StylerUtils.style(flow.getStateIds())
				+ "-- likely programmer error, check your flow configuration", cause);
		this.flow = flow;
	}

	/**
	 * Returns the flow where the state could not be found.
	 * @return the flow
	 */
	public Flow getFlow() {
		return flow;
	}

	/**
	 * Returns the id of the state that was not found.
	 * @return the state id
	 */
	public String getStateId() {
		return getArtifactId();
	}
}