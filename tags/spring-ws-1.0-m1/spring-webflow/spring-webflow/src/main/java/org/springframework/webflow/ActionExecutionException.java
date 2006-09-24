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
 * Thrown if an unhandled exception occurs when an action is executed. Typically
 * wraps another exception noting the root cause failure, which may be checked
 * or unchecked.
 * <p>
 * Is a StateException, recording information about what state a FlowExecution
 * was in when this exception was thrown. Also provides a reference to the
 * Action instance itself and the execution properties that may have affected
 * its execution.
 * <p>
 * Note: if the flow execution was in the process of starting the
 * {@link #getState()} accessor will return null. In this case, the flow
 * definition that was starting when this exception was thrown can be obtained
 * by calling {@link #getFlow()}.
 * 
 * @see org.springframework.webflow.Action
 * @see org.springframework.webflow.ActionState
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class ActionExecutionException extends StateException {

	/**
	 * The flow that was executing when the exception occured.
	 */
	private Flow flow;

	/**
	 * The action that threw an exception while executing.
	 */
	private Action action;

	/**
	 * The action's execution properties, which may have affected its execution
	 * and possibly contributed to this exception being thrown.
	 */
	private UnmodifiableAttributeMap executionProperties;

	/**
	 * Create a new action execution exception that occured while a flow
	 * execution was starting and before the start state was entered.
	 * @param flow the flow
	 * @param action the action that generated an unrecoverable exception
	 * @param cause the underlying cause
	 */
	public ActionExecutionException(Flow flow, Action action, UnmodifiableAttributeMap executionProperties,
			Throwable cause) {
		this(null, action, executionProperties, "Exception thrown executing start " + action + " of flow '"
				+ flow.getId() + "'", cause);
		this.flow = flow;
	}

	/**
	 * Create a new action execution exception that occured in a state of a flow
	 * execution.
	 * @param state the active state
	 * @param action the action that generated an unrecoverable exception
	 * @param executionProperties action execution properties
	 * @param cause the underlying cause
	 */
	public ActionExecutionException(State state, Action action, UnmodifiableAttributeMap executionProperties,
			Throwable cause) {
		this(state, action, executionProperties, "Exception thrown executing " + action + " in state '" + state.getId()
				+ "' of flow '" + state.getFlow().getId() + "'", cause);
	}

	/**
	 * Create a new action execution exception that occured in the state of a
	 * flow execution.
	 * @param state the active state
	 * @param action the action that generated an unrecoverable exception
	 * @param executionProperties action execution properties
	 * @param message a descriptive message
	 * @param cause the underlying cause
	 */
	public ActionExecutionException(State state, Action action, UnmodifiableAttributeMap executionProperties,
			String message, Throwable cause) {
		super(state, message, cause);
		this.action = action;
		this.executionProperties = executionProperties;
	}

	/**
	 * Returns the flow that was executing when this exception occured.
	 * @return the flow
	 */
	public Flow getFlow() {
		if (getState() != null) {
			return getState().getFlow();
		}
		else {
			return flow;
		}
	}

	/**
	 * Returns the action that threw an exception when executed.
	 * @return the failing action
	 */
	public Action getAction() {
		return action;
	}

	/**
	 * Returns the properties (attributes) associated with the action during
	 * execution.
	 */
	public UnmodifiableAttributeMap getExecutionAttributes() {
		return executionProperties;
	}
}