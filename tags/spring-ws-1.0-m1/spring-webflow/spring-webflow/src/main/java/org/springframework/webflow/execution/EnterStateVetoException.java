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
package org.springframework.webflow.execution;

import org.springframework.webflow.State;
import org.springframework.webflow.StateException;

/**
 * Exception thrown to veto the entering of a state of a flow. Typically thrown
 * by {@link FlowExecutionListener} objects that apply security or other runtime
 * constraint checks to flow executions.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class EnterStateVetoException extends StateException {

	/**
	 * The state whose entering was vetoed.
	 */
	private State vetoedState;

	/**
	 * Create a new enter state veto exception.
	 * @param sourceState the current state when the veto operation occured
	 * @param vetoedState the state for which entering is vetoed
	 * @param message a descriptive message
	 */
	public EnterStateVetoException(State sourceState, State vetoedState, String message) {
		super(sourceState, message);
		this.vetoedState = vetoedState;
	}

	/**
	 * Create a new enter state veto exception.
	 * @param sourceState the current state when the veto operation occured
	 * @param vetoedState the state for which entering is vetoed
	 * @param message a descriptive message
	 * @param cause the underlying cause
	 */
	public EnterStateVetoException(State sourceState, State vetoedState, String message, Throwable cause) {
		super(sourceState, message, cause);
		this.vetoedState = vetoedState;
	}

	/**
	 * Returns the state for which entering was vetoed.
	 */
	public State getVetoedState() {
		return vetoedState;
	}
}