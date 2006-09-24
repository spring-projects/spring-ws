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

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.mapping.AttributeMapper;
import org.springframework.core.CollectionFactory;
import org.springframework.core.style.StylerUtils;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * A single flow definition. A Flow definition represents a reusable,
 * self-contained controller module that provides the blue print for a logical
 * page flow of a web application. A "logical page flow" is defined as a
 * controlled navigation that guides a single user through fulfillment of a
 * business process/goal that takes place over a series of steps, modeled as
 * states.
 * <p>
 * A simple Flow definition could do nothing more than execute an action and
 * display a view, all in one request. A more elaborate Flow definition may be
 * long-lived, executing accross a series of requests, invoking many possible
 * paths, actions, and subflows.
 * <p>
 * Note: A flow is not a welcome page or an index page: don't use flows for
 * those cases, use simple controllers/actions/portlets instead. Don't use flows
 * where your application demands a significant amount of "free browsing": flows
 * force strict navigation. Especially in Intranet applications, there are often
 * "controlled navigations" where the user is not free to do what he or she
 * wants but must follow the guidelines provided by the system to complete a
 * process that is transactional in nature (the quinessential example would be a
 * 'checkout' flow of a shopping cart application). This is a typical use case
 * appropriate for a flow.
 * <p>
 * Structurally, a Flow is composed of a set of states. A {@link State} is a
 * point in a flow where a behavior is executed; for example, showing a view,
 * executing an action, spawning a subflow, or terminating the flow. Different
 * types of states execute different behaviors in a polymorphic fashion.
 * <p>
 * Each {@link TransitionableState} type has one or more transitions that when
 * executed, move a Flow to another state, defining the supported <i>paths</i>
 * through the flow. A state transition is triggered by the occurence of an
 * event. An event is something that happens externally the flow should respond
 * to, for example a user input event like ("submit") or an action execution
 * result event like ("success"). When an event occurs in a state of a Flow,
 * that event drives a state transition that decides what to do next.
 * <p>
 * Each Flow has exactly one start state. A start state is simply a marker
 * noting the state executions of this Flow definition should start in. The
 * first state added to the flow will become the start state by default.
 * <p>
 * Flow definitions may have one or more flow exception handlers. A
 * {@link StateExceptionHandler} can execute custom behavior in response to a
 * specific exception (or set of exceptions) that occur in a state of one of
 * this flow's executions.
 * <p>
 * Instances of this class are typically built by
 * {@link org.springframework.webflow.builder.FlowBuilder} implementations, but
 * may also be directly subclassed.
 * <p>
 * This class, and the rest of the Spring Web Flow (SWF) core, has been designed
 * with minimal dependencies on other libraries, and is usable in a standalone
 * fashion (as well as in the context of other frameworks like Spring MVC,
 * Struts, or JSF, for example). The core system is fully usable outside an HTTP
 * servlet environment, for example in Portlets, tests, or standalone
 * applications. One of the major architectural benefits of Spring Web Flow is
 * the ability to design reusable, high-level controller modules that may be
 * executed in <i>any</i> environment.
 * <p>
 * Note: flows are singleton definition objects so they should be thread-safe!
 * 
 * @see org.springframework.webflow.State
 * @see org.springframework.webflow.TransitionableState
 * @see org.springframework.webflow.ActionState
 * @see org.springframework.webflow.ActionList
 * @see org.springframework.webflow.ViewState
 * @see org.springframework.webflow.SubflowState
 * @see org.springframework.webflow.EndState
 * @see org.springframework.webflow.DecisionState
 * @see org.springframework.webflow.Transition
 * @see org.springframework.webflow.StateExceptionHandler
 * @see org.springframework.webflow.StateExceptionHandlerSet
 * @see org.springframework.webflow.executor.mvc.FlowController
 * @see org.springframework.webflow.executor.mvc.PortletFlowController
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 * @author Colin Sampaleanu
 */
public class Flow extends AnnotatedObject {

	private static final Log logger = LogFactory.getLog(Flow.class);

	/**
	 * An assigned flow identifier uniquely identifying this flow among all
	 * other flows.
	 */
	private String id;

	/**
	 * The set of state definitions for this flow.
	 */
	private Set states = CollectionFactory.createLinkedSetIfPossible(9);

	/**
	 * The default start state for this flow.
	 */
	private State startState;

	/**
	 * The set of flow variables created by this flow.
	 */
	private Set variables = CollectionFactory.createLinkedSetIfPossible(3);

	/**
	 * The mapper to map flow input attributes.
	 */
	private AttributeMapper inputMapper;

	/**
	 * The list of actions to execute when this flow starts.
	 * <p>
	 * Start actions should execute with care as during startup a flow session
	 * has not yet fully initialized and some properties like its "currentState"
	 * have not yet been set.
	 */
	private ActionList startActionList = new ActionList();

	/**
	 * The set of global transitions that are shared by all states of this flow.
	 */
	private TransitionSet globalTransitionSet = new TransitionSet();

	/**
	 * The list of actions to execute when this flow ends.
	 */
	private ActionList endActionList = new ActionList();

	/**
	 * The mapper to map flow output attributes.
	 */
	private AttributeMapper outputMapper;

	/**
	 * The set of exception handlers for this flow.
	 */
	private StateExceptionHandlerSet exceptionHandlerSet = new StateExceptionHandlerSet();

	/**
	 * The set of inline flows contained by this flow.
	 */
	private Set inlineFlows = CollectionFactory.createLinkedSetIfPossible(3);

	/**
	 * Construct a new flow definition with the given id. The id should be
	 * unique among all flows.
	 * @param id the flow identifier
	 */
	public Flow(String id) {
		setId(id);
	}

	/**
	 * Returns the unique id of this flow.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set the unique id of this flow.
	 */
	public void setId(String id) {
		Assert.hasText(id, "This flow must have a unique, non-blank identifier");
		this.id = id;
	}

	/**
	 * Add given state definition to this flow definition. Marked protected, as
	 * this method is to be called by the (privileged) state definition classes
	 * themselves during state construction as part of a FlowBuilder invocation.
	 * @param state the state, if already added nothing happens, if another
	 * instance is added with the same id, an exception is thrown
	 * @throws IllegalArgumentException when the state cannot be added to the
	 * flow; specifically, if another state shares the same id as the one
	 * provided
	 */
	protected void add(State state) throws IllegalArgumentException {
		if (this != state.getFlow() && state.getFlow() != null) {
			throw new IllegalArgumentException("State " + state + " cannot be added to this flow '" + getId()
					+ "' -- it already belongs to a different flow");
		}
		if (states.contains(state)) {
			throw new IllegalArgumentException("This flow '" + getId() + "' already contains a state with id '"
					+ state.getId() + "' -- state ids must be locally unique to the flow definition; "
					+ "existing state-ids of this flow include: " + StylerUtils.style(getStateIds()));
		}
		boolean firstAdd = states.isEmpty();
		states.add(state);
		if (firstAdd) {
			setStartState(state);
		}
	}

	/**
	 * Returns the number of states managed by this flow.
	 * @return the state count
	 */
	public int getStateCount() {
		return states.size();
	}

	/**
	 * Returns the list of states in this flow.
	 */
	public State[] getStates() {
		return (State[])states.toArray(new State[states.size()]);
	}

	/**
	 * Return the start state, throwing an exception if it has not yet been
	 * marked.
	 * @return the start state
	 * @throws IllegalStateException when no start state has been marked
	 */
	public State getStartState() throws IllegalStateException {
		if (startState == null) {
			throw new IllegalStateException("No start state has been set for this flow ('" + getId()
					+ "') -- flow builder configuration error?");
		}
		return startState;
	}

	/**
	 * Set the start state for this flow to the state with the provided
	 * <code>stateId</code>; a state must exist by the provided
	 * <code>stateId</code>.
	 * @param stateId the id of the new start state
	 * @throws NoSuchStateException when no state exists with the id you
	 * provided
	 */
	public void setStartState(String stateId) throws NoSuchStateException {
		setStartState(getRequiredState(stateId));
	}

	/**
	 * Set the start state for this flow to the state provided; any state may be
	 * the start state.
	 * @param state the new start state
	 * @throws NoSuchStateException given state has not been added to this flow
	 */
	public void setStartState(State state) throws NoSuchStateException {
		if (!states.contains(state)) {
			throw new NoSuchStateException(this, state.getId());
		}
		startState = state;
	}

	/**
	 * Is a state with the provided id present in this flow?
	 * @param stateId the state id
	 * @return true if yes, false otherwise
	 */
	public boolean containsState(String stateId) {
		return getState(stateId) != null;
	}

	/**
	 * Return the state with the provided id, returning <code>null</code> if
	 * no state exists with that id.
	 * @param stateId the state id
	 * @return the state with that id, or null if none exists
	 */
	public State getState(String stateId) {
		if (!StringUtils.hasText(stateId)) {
			throw new IllegalArgumentException("The specified stateId is invalid: state identifiers must be non-blank");
		}
		Iterator it = states.iterator();
		while (it.hasNext()) {
			State state = (State)it.next();
			if (state.getId().equals(stateId)) {
				return state;
			}
		}
		return null;
	}

	/**
	 * Return the state with the provided id, throwing a exception if no state
	 * exists with that id.
	 * @param stateId the state id
	 * @return the state with that id
	 * @throws NoSuchStateException when no state exists with that id
	 */
	public State getRequiredState(String stateId) throws NoSuchStateException {
		State state = getState(stateId);
		if (state == null) {
			throw new NoSuchStateException(this, stateId);
		}
		return state;
	}

	/**
	 * Return the <code>TransitionableState</code> with given
	 * <code>stateId</code>, or <code>null</code> when not found.
	 * @param stateId id of the state to look up
	 * @return the transitionable state, or null when not found
	 * @throws IllegalStateException when the identified state is not
	 * transitionable
	 */
	public TransitionableState getTransitionableState(String stateId) throws IllegalStateException {
		State state = getState(stateId);
		if (state != null && !(state instanceof TransitionableState)) {
			throw new IllegalStateException("The state '" + stateId + "' of flow '" + getId()
					+ "' must be transitionable");
		}
		return (TransitionableState)state;
	}

	/**
	 * Return the <code>TransitionableState</code> with given
	 * <code>stateId</code>, throwing an exception if not found.
	 * @param stateId id of the state to look up
	 * @return the transitionable state
	 * @throws IllegalStateException when the identified state is not
	 * transitionable
	 * @throws NoSuchStateException when no transitionable state exists by this
	 * id
	 */
	public TransitionableState getRequiredTransitionableState(String stateId) throws IllegalStateException,
			NoSuchStateException {
		TransitionableState state = getTransitionableState(stateId);
		if (state == null) {
			throw new NoSuchStateException(this, stateId);
		}
		return state;
	}

	/**
	 * Convenience accessor that returns an ordered array of the String
	 * <code>ids</code> for the state definitions associated with this flow
	 * definition.
	 * @return the state ids
	 */
	public String[] getStateIds() {
		String[] stateIds = new String[getStateCount()];
		int i = 0;
		Iterator it = states.iterator();
		while (it.hasNext()) {
			stateIds[i++] = ((State)it.next()).getId();
		}
		return stateIds;
	}

	/**
	 * Adds a flow variable.
	 * @param variable the var
	 */
	public void addVariable(FlowVariable variable) {
		variables.add(variable);
	}

	/**
	 * Adds the flow variables.
	 * @param variables the vars
	 */
	public void addVariables(FlowVariable[] variables) {
		if (variables == null) {
			return;
		}
		for (int i = 0; i < variables.length; i++) {
			addVariable(variables[i]);
		}
	}

	/**
	 * Returns the flow variables.
	 */
	public FlowVariable[] getVariables() {
		return (FlowVariable[])variables.toArray(new FlowVariable[variables.size()]);
	}

	/**
	 * Returns the configured flow input mapper
	 * @return the input mapper
	 */
	public AttributeMapper getInputMapper() {
		return inputMapper;
	}

	/**
	 * Sets the mapper to map flow input attributes.
	 * @param inputMapper the input mapper
	 */
	public void setInputMapper(AttributeMapper inputMapper) {
		this.inputMapper = inputMapper;
	}

	/**
	 * Returns the list of actions executed by this flow when an execution of
	 * the flow <i>starts</i>.
	 * @return the start action list
	 */
	public ActionList getStartActionList() {
		return startActionList;
	}

	/**
	 * Returns the list of actions executed by this flow when an execution of
	 * the flow <i>ends</i>.
	 * @return the end action list
	 */
	public ActionList getEndActionList() {
		return endActionList;
	}

	/**
	 * Returns the configured flow output mapper
	 * @return the output mapper
	 */
	public AttributeMapper getOutputMapper() {
		return outputMapper;
	}

	/**
	 * Sets the mapper to map flow output attributes.
	 * @param outputMapper the output mapper
	 */
	public void setOutputMapper(AttributeMapper outputMapper) {
		this.outputMapper = outputMapper;
	}

	/**
	 * Returns the set of exception handlers, allowing manipulation of how state
	 * exceptions are handled when thrown during flow execution. <p/> Exception
	 * handlers are invoked when an exception occurs when this state is entered,
	 * and can execute custom exception handling logic as well as select an
	 * error view to display. <p/> State exception handlers attached at the flow
	 * level have a opportunity to handle exceptions that aren't handled at the
	 * state level.
	 * @return the state exception handler set
	 */
	public StateExceptionHandlerSet getExceptionHandlerSet() {
		return exceptionHandlerSet;
	}

	/**
	 * Adds an inline flow to this flow.
	 * @param flow the inline flow to add
	 */
	public void addInlineFlow(Flow flow) {
		inlineFlows.add(flow);
	}

	/**
	 * Returns the list of inline flow ids.
	 * @return a string array of inline flow identifiers
	 */
	public String[] getInlineFlowIds() {
		String[] flowIds = new String[getInlineFlowCount()];
		int i = 0;
		Iterator it = inlineFlows.iterator();
		while (it.hasNext()) {
			flowIds[i++] = ((Flow)it.next()).getId();
		}
		return flowIds;
	}

	/**
	 * Returns the list of inline flows.
	 * @return the list of inline flows
	 */
	public Flow[] getInlineFlows() {
		return (Flow[])inlineFlows.toArray(new Flow[inlineFlows.size()]);
	}

	/**
	 * Returns the count of registered inline flows.
	 * @return the count
	 */
	public int getInlineFlowCount() {
		return inlineFlows.size();
	}

	/**
	 * Tests if this flow contains an in-line flow with the specified id.
	 * @param id the inline flow id
	 * @return true if this flow contains a inline flow with that id, false
	 * otherwise
	 */
	public boolean containsInlineFlow(String id) {
		return getInlineFlow(id) != null;
	}

	/**
	 * Returns the inline flow with the provided id, or <code>null</code> if
	 * no such inline flow exists.
	 * @param id the inline flow id
	 * @return the inline flow
	 */
	public Flow getInlineFlow(String id) {
		if (!StringUtils.hasText(id)) {
			throw new IllegalArgumentException(
					"The specified inline flowId is invalid: flow identifiers must be non-blank");
		}
		Iterator it = inlineFlows.iterator();
		while (it.hasNext()) {
			Flow flow = (Flow)it.next();
			if (flow.getId().equals(id)) {
				return flow;
			}
		}
		return null;
	}

	/**
	 * Returns the set of transitions eligible for execution by this flow if no
	 * state-level transition is matched.
	 * @return the global transition set
	 */
	public TransitionSet getGlobalTransitionSet() {
		return globalTransitionSet;
	}

	public boolean equals(Object o) {
		if (!(o instanceof Flow)) {
			return false;
		}
		Flow other = (Flow)o;
		return id.equals(other.id);
	}

	public int hashCode() {
		return id.hashCode();
	}

	/**
	 * Start a new session for this flow in its stat state.
	 * @param context the flow execution control context
	 * @param input eligible input into the session
	 * @throws StateException when an exception occurs entering the start state
	 */
	public ViewSelection start(FlowExecutionControlContext context, AttributeMap input) throws StateException {
		createVariables(context);
		if (inputMapper != null) {
			inputMapper.map(input, context, Collections.EMPTY_MAP);
		}
		startActionList.execute(context);
		return startState.enter(context);
	}

	/**
	 * Inform this flow definition that an event was signaled in the current
	 * state of an active flow execution.
	 * @param context the flow execution control context
	 * @return the selected view
	 * @throws StateException when an exception occurs processing the event
	 */
	public ViewSelection onEvent(Event event, FlowExecutionControlContext context) throws StateException {
		TransitionableState currentState = getCurrentTransitionableState(context);
		try {
			return currentState.onEvent(event, context);
		}
		catch (NoMatchingTransitionException e) {
			// try the flow level transition set for a match
			Transition transition = globalTransitionSet.getTransition(context);
			if (transition != null) {
				return transition.execute(currentState, context);
			}
			else {
				throw e;
			}
		}
	}

	/**
	 * Inform this flow definition that a execution session of itself has ended.
	 * @param context the flow execution control context
	 * @param output initial output produced by the session that is eligible for
	 * modification by this method.
	 * @throws StateException when an exception occurs ending this flow
	 */
	public void end(FlowExecutionControlContext context, AttributeMap output) throws StateException {
		endActionList.execute(context);
		if (outputMapper != null) {
			outputMapper.map(context, output, Collections.EMPTY_MAP);
		}
	}

	/**
	 * Handle an exception that occured during an execution of this flow.
	 * @param exception the exception that occured
	 * @param context the flow execution control context
	 * @return the selected error view, or <code>null</code> if no handler
	 * matched or returned a non-null view selection
	 * @throws StateException passed in, if it was not handled
	 */
	public ViewSelection handleException(StateException exception, FlowExecutionControlContext context)
			throws StateException {
		return getExceptionHandlerSet().handleException(exception, context);
	}

	private void createVariables(RequestContext context) {
		Iterator it = variables.iterator();
		while (it.hasNext()) {
			FlowVariable variable = (FlowVariable)it.next();
			if (logger.isDebugEnabled()) {
				logger.debug("Creating " + variable);
			}
			variable.create(context);
		}
	}

	private TransitionableState getCurrentTransitionableState(FlowExecutionControlContext context) {
		State currentState = context.getCurrentState();
		if (!(currentState instanceof TransitionableState)) {
			throw new IllegalStateException("You can only signal events in transitionable states, and state "
					+ context.getCurrentState() + " is not transitionable - programmer error");
		}
		return (TransitionableState)currentState;
	}

	public String toString() {
		return new ToStringCreator(this).append("id", id).append("states", states).append("startState", startState)
				.append("variables", variables).append("inputMapper", inputMapper).append("startActionList",
						startActionList).append("exceptionHandlerSet", exceptionHandlerSet).append(
						"globalTransitionSet", globalTransitionSet).append("endActionList", endActionList).append(
						"outputMapper", outputMapper).append("inlineFlows", inlineFlows).toString();
	}
}