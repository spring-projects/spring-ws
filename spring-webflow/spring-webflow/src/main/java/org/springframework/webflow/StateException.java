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

/**
 * Core base class for exceptions that occur in a flow state. Provides a
 * reference to the State definition where the exception occured. Can be used
 * directly, but you are encouraged to create a specific subclass for a
 * particular use case.
 * <p>
 * State exceptions occur at runtime, when the flow is executing requests on
 * behalf of a client. They signal that an execution problem occured: e.g.
 * action execution failed or no transition was would matching a particular
 * request context. A state exception does not indicate a flow definition
 * problem, a FlowArtifactLookupException is used for that.
 * 
 * @see org.springframework.webflow.State
 * @see org.springframework.webflow.FlowArtifactException
 * 
 * @author Keith Donald
 */
public class StateException extends FlowException {

	/**
	 * The state where the exception occured.
	 */
	private State state;

	/**
	 * Creates a new state exception.
	 * @param state the state where the exception occured
	 * @param message a descriptive message
	 */
	public StateException(State state, String message) {
		super(message);
		this.state = state;
	}

	/**
	 * Creates a new state exception.
	 * @param state the state where the exception occured
	 * @param message a descriptive message
	 * @param cause the root cause
	 */
	public StateException(State state, String message, Throwable cause) {
		super(message, cause);
		this.state = state;
	}

	/**
	 * Returns the state where the exception occured.
	 */
	public State getState() {
		return state;
	}

	/**
	 * Returns the flow that was executing when this exception occured.
	 */
	public Flow getFlow() {
		if (state == null) {
			throw new IllegalStateException("The state is null, cannot access the flow");
		}
		return state.getFlow();
	}
}