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
 * Central interface that allows callers to access contextual information about
 * an ongoing flow execution within the context of a single client request. The
 * term <i>request</i> is used to describe a single call (thread) into the flow
 * system by an external actor to manipulate exactly one flow execution.
 * <p>
 * A new instance of this object is created when one of the core operations
 * supported by a <code>FlowExecution</code> is invoked, either <i>start</i>
 * to launch the flow execution, <i>signalEvent</i> to resume the flow
 * execution, or <i>refresh</i> to reconstitute the flow execution's last view
 * selection for purposes of reissuing a user response.
 * <p>
 * Once created this context object is passed around throughout request
 * processing where it may be accessed and reasoned upon, typically by
 * user-implemented action code and/or state transition criteria.
 * <p>
 * When a call into a flow execution returns, this object goes out of scope and
 * is disposed of automatically. Thus this object is an internal artifact used
 * within a FlowExecution: this object is NOT directly exposed to external
 * client code, e.g. a view implementation (JSP).
 * <p>
 * Note: the "requestScope" property may be used as a store for arbitrary data
 * that should exist for the life of this object.
 * <p>
 * Request-local data, along with all data in flow scope, is available for
 * exposing to view templates via a
 * {@link org.springframework.webflow.ViewSelection}'s "model" property,
 * returned when a {@link org.springframework.webflow.ViewState} or
 * {@link org.springframework.webflow.EndState} is entered.
 * <p>
 * This interface does not allow direct manipulation of the flow execution. That
 * is only possible via the
 * {@link org.springframework.webflow.FlowExecutionControlContext} sub
 * interface, which is used by privelged state types to manipulate the flow.
 * <p>
 * The web flow system will ensure that a RequestContext object is local to the
 * current thread. It can be safely manipulated without needing to worry about
 * concurrent access.
 * <p>
 * Note: the <i>request</i> context is in no way linked to an HTTP or Portlet
 * request! It uses the familiar "request" naming convention to indicate a
 * single call to manipulate a runtime execution of a flow.
 * 
 * @see org.springframework.webflow.execution.FlowExecution
 * @see org.springframework.webflow.FlowExecutionControlContext
 * @see org.springframework.webflow.Action
 * @see org.springframework.webflow.TransitionCriteria
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface RequestContext {

	/**
	 * Returns the definition of the flow that is currently executing.
	 * @return the flow definition for the active session
	 * @throws IllegalStateException if this flow execution has not been started
	 * at all, or if this execution has ended and is no longer actively
	 * executing
	 */
	public Flow getActiveFlow() throws IllegalStateException;

	/**
	 * Returns the current state of the executing flow. May return
	 * <code>null</code> if this flow execution is in the process of starting
	 * and has not yet entered its start state.
	 * @return the current state, or <code>null</code> if in the process of
	 * starting.
	 * @throws IllegalStateException if this flow execution has not been started
	 * at all, or if this execution has ended and is no longer actively
	 * executing
	 */
	public State getCurrentState() throws IllegalStateException;

	/**
	 * Returns a mutable accessor for accessing and/or setting attributes in
	 * request scope. <b>Request scoped attributes exist for the duration of
	 * this request only.</b>
	 * @return the request scope
	 */
	public AttributeMap getRequestScope();

	/**
	 * Returns a mutable accessor for accessing and/or setting attributes in
	 * flow scope. <b>Flow scoped attributes exist for the life of the active
	 * flow session.</b>
	 * @return the flow scope
	 * @see FlowExecutionContext#getActiveSession()
	 */
	public AttributeMap getFlowScope();

	/**
	 * Returns a mutable accessor for accessing and/or setting attributes in
	 * conversation scope. <b>Conversation scoped attributes exist for the life
	 * of the executing flow and are shared accross all flow sessions.</b>
	 * @return the conversation scope
	 */
	public AttributeMap getConversationScope();

	/**
	 * Returns the immutable input parameters associated with this request into
	 * Spring Web Flow. The map returned is immutable and cannot be changed.
	 * <p>
	 * This is typically a convenient shortcut for accessing the
	 * {@link ExternalContext#getRequestParameterMap()} directly.
	 */
	public ParameterMap getRequestParameters();

	/**
	 * Returns the external client context that originated (or triggered) this
	 * request. Also known as the "user context".
	 * <p>
	 * Acting as a facade, the returned context object provides a single point
	 * of access to the calling client's environment. It provides normalized
	 * access to attributes of the client environment without tying you to
	 * specific constructs within that environment.
	 * <p>
	 * In addition, this context may be downcastable to a specific context type
	 * for a specific client environment, such as a
	 * {@link org.springframework.webflow.context.servlet.ServletExternalContext}
	 * for servlets or a
	 * {@link org.springframework.webflow.context.portlet.PortletExternalContext}
	 * for portlets. Such downcasting will give you full access to a native
	 * HttpServletRequest, for example. With that said, for portability reasons
	 * you should avoid coupling your flow artifacts to a specific deployment
	 * environment when possible.
	 * @return the originating external context, the one that triggered the
	 * current execution request
	 */
	public ExternalContext getExternalContext();

	/**
	 * Returns additional contextual information about the executing flow.
	 * @return the flow execution context
	 */
	public FlowExecutionContext getFlowExecutionContext();

	/**
	 * Returns the last event signaled during this request. The event may or may
	 * not have caused a state transition to happen.
	 * @return the last signaled event
	 */
	public Event getLastEvent();

	/**
	 * Returns the last state transition that executed in this request.
	 * @return the last transition, or <code>null</code> if no transition has
	 * occured yet
	 */
	public Transition getLastTransition();

	/**
	 * Returns a context map for accessing arbitrary attributes about the state
	 * of the current request.
	 * <p>
	 * Attributes provisioned within this map are often used by {@link Action}
	 * implementations to influence their behavior.
	 * @return the current attributes of this request, or empty if not set
	 */
	public UnmodifiableAttributeMap getAttributes();

	/**
	 * Set the contextual attributes describing the state of this request.
	 * Overwrites any pre-existing collection.
	 * @param attributes the attributes
	 */
	public void setAttributes(AttributeCollection attributes);

	/**
	 * Returns the data model for this context, suitable for exposing to clients
	 * (mostly web views). Typically the model will contain the union of the
	 * data available in request scope and flow scope.
	 * @return the model that can be exposed to a client view for rendering
	 * purposes
	 */
	public UnmodifiableAttributeMap getModel();
}