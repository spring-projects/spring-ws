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
 * A runtime object that represents a single client session of a specific
 * <code>Flow</code> definition. This object maintains all the state of the
 * session, including its status within exactly one governing FlowExecution and
 * its current State. This object also acts as the "flow scope" data model. Data
 * in "flow scope" lives for the life of this object, and is cleaned up
 * automatically when this object is destroyed. Destruction happens when this
 * session enters an end state.
 * <p>
 * This object is fully managed by a FlowExecution within a stack-based data
 * structure, where each session in the stack is a spawned flow at a specific
 * state. The session at the top of the stack is the currently active flow. This
 * stack of all flow sessions captures the complete and current state (snapshot)
 * of an executing flow.
 * <p>
 * A flow session will go through several status changes during its lifecycle.
 * Initially it will be {@link FlowSessionStatus#CREATED}. For example, when a
 * new FlowExecution is started to launch a new root Flow definition a new
 * FlowSession is created.
 * <p>
 * When a flow session is activated (about to be manipulated), it's status
 * becomes {@link FlowSessionStatus#ACTIVE}. In the case of a new
 * FlowExecution, session activation happens immediately after creation to put
 * the "root flow" at the top of the stack and transition it to its start state.
 * <p>
 * When control returns to the client for user think time, the status is updated
 * to {@link FlowSessionStatus#PAUSED}. The flow is no longer actively
 * processing: it's stored off somewhere waiting on the user to participate.
 * <p>
 * If a flow session is pushed down in the stack because a subflow is spawned,
 * its status becomes {@link FlowSessionStatus#SUSPENDED} until the subflow
 * returns (ends) and is popped off the stack. The resuming flow session then
 * becomes active once again.
 * <p>
 * When a flow session is terminated because an EndState is reached, its status
 * becomes {@link FlowSessionStatus#ENDED}, ending its lifecycle. The session
 * is popped off the stack and discarded, and any allocated resources in "flow
 * scope" are automatically cleaned up.
 * <p>
 * Note that a flow <i>session</i> is in no way linked to an HTTP session! It
 * just uses the familiar "session" naming convention to denote a stateful
 * interaction.
 * 
 * @see org.springframework.webflow.execution.FlowExecution
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowSession {

	/**
	 * Returns the flow definition associated with this flow session.
	 */
	public Flow getFlow();

	/**
	 * Returns the state of this flow session.
	 */
	public State getState();

	/**
	 * Returns the current status of this flow session.
	 */
	public FlowSessionStatus getStatus();

	/**
	 * Return the session attributes; the basis for "flow scope".
	 * @return the flow scope attributes
	 */
	public AttributeMap getScope();

	/**
	 * Returns the parent flow session in the current flow execution, or
	 * <code>null</code> if there is not parent flow session.
	 */
	public FlowSession getParent();

	/**
	 * Returns whether this flow session is the root flow session in the ongoing
	 * flow execution. The root flow session does not have a parent flow
	 * session.
	 */
	public boolean isRoot();
}