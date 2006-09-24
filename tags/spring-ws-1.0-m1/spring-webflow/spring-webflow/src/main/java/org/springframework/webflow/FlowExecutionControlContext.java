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
 * Mutable control interface used to manipulate an ongoing flow execution in the
 * context of one client request. Primarily used internally by the various flow
 * artifacts when they are invoked.
 * <p>
 * This interface acts as a facade for core definition constructs such as the
 * central <code>Flow</code> and <code>State</code> classes, abstracting
 * away details about the runtime <i>execution</i> subsystem defined in the
 * {@link org.springframework.webflow.execution} package.
 * <p>
 * Note this type is not the same as the {@link FlowExecutionContext}! Objects
 * of this type are <i>request specific</i>: they provide a control interface
 * for manipulating exactly one flow execution locally from exactly one request.
 * A <code>FlowExecutionContext</code> provides information about a single
 * flow execution (conversation), and it's scope is not local to a specific
 * request (or thread).
 * 
 * @see org.springframework.webflow.Flow
 * @see org.springframework.webflow.State
 * @see org.springframework.webflow.execution.FlowExecution
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowExecutionControlContext extends RequestContext {

	/**
	 * Record the last event signaled in the executing flow. This method will be
	 * called as part of signaling an event in a flow to indicate the
	 * 'lastEvent' that was signaled.
	 * @param lastEvent the last event signaled
	 * @see Flow#onEvent(Event, FlowExecutionControlContext)
	 */
	public void setLastEvent(Event lastEvent);

	/**
	 * Record the last transition that executed in the executing flow. This
	 * method will be called as part of executing a transition from one state to
	 * another.
	 * @param lastTransition the last transition that executed
	 * @see Transition#execute(TransitionableState, FlowExecutionControlContext)
	 */
	public void setLastTransition(Transition lastTransition);

	/**
	 * Record the current state that has entered in the executing flow. This
	 * method will be called as part of entering a new state by the State type
	 * itself.
	 * @param state the current state
	 * @see State#enter(FlowExecutionControlContext)
	 */
	public void setCurrentState(State state);

	/**
	 * Spawn a new flow session and activate it in the currently executing flow.
	 * Also transitions the spawned flow to its start state, which may be
	 * overridden by providing the optional state parameter. This method should
	 * be called by clients that wish to spawn new flows, such as subflow
	 * states.
	 * @param flow the flow to start, its <code>start()</code> method will be
	 * called
	 * @param input initial contents of the newly created flow session (may be
	 * <code>null</code>, e.g. empty)
	 * @return the selected starting view, which returns control to the client
	 * and requests that a view be rendered with model data
	 * @throws StateException if an exception was thrown within a state of the
	 * flow during execution of this start operation
	 * @see Flow#start(FlowExecutionControlContext, AttributeMap)
	 */
	public ViewSelection start(Flow flow, AttributeMap input) throws StateException;

	/**
	 * Signals the occurence of an event in the current state of this flow
	 * execution request context. This method should be called by clients that
	 * report internal event occurences, such as action states. The
	 * <code>onEvent()</code> method of the flow involved in the flow
	 * execution will be called.
	 * @param event the event that occured
	 * @return the next selected view, which returns control to the client and
	 * requests that a view be rendered with model data
	 * @throws StateException if an exception was thrown within a state of the
	 * flow during execution of this signalEvent operation
	 * @see Flow#onEvent(Event, FlowExecutionControlContext)
	 */
	public ViewSelection signalEvent(Event event) throws StateException;

	/**
	 * End the active flow session of the current flow execution. This method
	 * should be called by clients that terminate flows, such as end states. The
	 * <code>end()</code> method of the flow involved in the flow execution
	 * will be called.
	 * @param output output produced by the session that is eligible for
	 * mapping by a resuming parent flow.
	 * @return the ended session
	 * @throws IllegalStateException when the flow execution is not active
	 * @see Flow#end(FlowExecutionControlContext, AttributeMap)
	 */
	public FlowSession endActiveFlowSession(AttributeMap output) throws IllegalStateException;

}