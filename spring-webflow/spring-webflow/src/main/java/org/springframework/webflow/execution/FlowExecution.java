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

import org.springframework.webflow.AttributeMap;
import org.springframework.webflow.ExternalContext;
import org.springframework.webflow.FlowExecutionContext;
import org.springframework.webflow.StateException;
import org.springframework.webflow.ViewSelection;

/**
 * A <i>client instance</i> of an executing top-level flow, representing a
 * single instance of a web conversation <i>at a specific point in time</i>.
 * <p>
 * This is the central facade interface for managing one runtime execution of a
 * Flow. Implementations of this interface are the finite state machine that is
 * the heart of Spring Web Flow.
 * <p>
 * Typically, when a browser wants to launch a new execution of a Flow at
 * runtime, it passes in the id of the Flow definition to launch to a governing
 * {@link org.springframework.webflow.executor.FlowExecutor}. This manager then
 * creates an instance of an object implementing this interface, initializing it
 * with the requested Flow definition which becomes the execution's "root", or
 * top-level flow. After creation, the {@link #start(AttributeMap, ExternalContext)} operation
 * is called, which causes the execution to activate a new session for its root
 * flow definition. That session is then pushed onto a stack and its definition
 * becomes the <i>active flow</i>. A local, internal
 * {@link org.springframework.webflow.FlowExecutionControlContext} object (which
 * extends ({@link org.springframework.webflow.RequestContext}) is then
 * created and the Flow's start {@link org.springframework.webflow.State} is
 * entered.
 * <p>
 * In a distributed environment such as HTTP, after a call into this object has
 * completed and control returns to the caller (manager), this execution object
 * (if still active) is typically saved out to a repository before the server
 * request ends. For example it might be saved out to the HttpSession, a
 * Database, or a client-side hidden form field for later restoration and
 * manipulation. This is the responsibility of the
 * {@link org.springframework.webflow.execution.repository.FlowExecutionRepository}
 * subsystem.
 * <p>
 * Subsequent requests from the client to manipuate this flow execution trigger
 * restoration and rehydration of this object, followed by an invocation of the
 * {@link #signalEvent(EventId, ExternalContext)} operation. The signalEvent
 * operation tells this state machine what action the user took from within the
 * context of the current state; for example, the user may have pressed pressed
 * the "submit" button, or pressed "cancel". After the user event is processed,
 * control again goes back to the caller and if this execution is still active,
 * it is saved out to storage. This continues until a client event causes this
 * flow execution to end (by the root flow reaching an EndState). At that time,
 * this object is removed from the repository and discarded.
 * 
 * @see org.springframework.webflow.executor.FlowExecutor
 * @see org.springframework.webflow.execution.repository.FlowExecutionRepository
 * @see org.springframework.webflow.Flow
 * @see org.springframework.webflow.State
 * @see org.springframework.webflow.FlowSession
 * @see org.springframework.webflow.FlowExecutionControlContext
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowExecution extends FlowExecutionContext {

	/**
	 * Start this flow execution, transitioning it to the root flow's start
	 * state and returning the starting model and view selection. Typically
	 * called by a flow execution manager, but also from test code.
	 * @param input input attributes to pass to the flow, which the flow may
	 * choose to map into its scope
	 * @param context the context in which the event occured
	 * @return the starting view selection, which requests that the calling
	 * client render a view with configured model data (so the user may
	 * participate in this flow execution)
	 * @throws StateException if an exception was thrown within a state of the
	 * flow execution during request processing
	 * @see FlowExecutionContext#getFlow()
	 */
	public ViewSelection start(AttributeMap input, ExternalContext context) throws StateException;

	/**
	 * Signal an occurence of the specified user event in the current state of
	 * this executing flow. The event will be processed in full and control will
	 * be returned once event processing is complete.
	 * @param eventId the identifier of the user event that occured
	 * @param context the context in which the event occured
	 * @return the next view selection to display for this flow execution, which
	 * requests that the calling client render a view with configured model data
	 * (so the user may participate in this flow execution)
	 * @throws StateException if an exception was thrown within a state of the
	 * resumed flow execution during event processing
	 */
	public ViewSelection signalEvent(EventId eventId, ExternalContext context) throws StateException;

	/**
	 * Refresh this flow execution, asking the current view selection to be reconstituted to 
	 * support a reissuing the response.  This is idempotent operation that may be safely called 
	 * on a paused execution.
	 * @param context the context in which the event occured
	 * @return the current view selection for this flow execution
	 * @throws StateException if an exception was thrown within a state of the
	 * resumed flow execution during event processing
	 */
	public ViewSelection refresh(ExternalContext context) throws StateException;
}