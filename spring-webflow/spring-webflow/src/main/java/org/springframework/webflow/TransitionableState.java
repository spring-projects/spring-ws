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

import org.springframework.core.style.ToStringCreator;

/**
 * Abstract superclass for states that have one or more transitions. State
 * transitions are typically triggered by events.
 * 
 * @see org.springframework.webflow.Transition
 * @see org.springframework.webflow.TransitionCriteria
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public abstract class TransitionableState extends State {

	/**
	 * The set of possible transitions out of this state.
	 */
	private TransitionSet transitions = new TransitionSet();

	/**
	 * An actions to execute when exiting this state.
	 */
	private ActionList exitActionList = new ActionList();

	/**
	 * Create a new transitionable state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @throws IllegalArgumentException when this state cannot be added to given
	 * flow
	 * @see State#State(Flow, String)
	 * @see #getTransitionSet()
	 */
	protected TransitionableState(Flow flow, String id) throws IllegalArgumentException {
		super(flow, id);
	}

	/**
	 * Returns the set of transitions that define the possible paths out of this
	 * state.
	 * @return the state transition set
	 */
	public TransitionSet getTransitionSet() {
		return transitions;
	}

	/**
	 * Returns the list of actions executed by this state when it is exited.
	 * @return the state exit action list
	 */
	public ActionList getExitActionList() {
		return exitActionList;
	}

	/**
	 * Inform this state definition that an event was signaled in it.
	 * @param context the flow execution control context
	 * @return the selected view
	 * @throws NoMatchingTransitionException when a matching transition cannot
	 * be found
	 */
	public ViewSelection onEvent(Event event, FlowExecutionControlContext context) throws NoMatchingTransitionException {
		return getRequiredTransition(context).execute(this, context);
	}

	/**
	 * Get a transition in this state for given flow execution request context.
	 * Throws and exception when there is no corresponding transition.
	 * @throws NoMatchingTransitionException when a matching transition cannot
	 * be found
	 */
	public Transition getRequiredTransition(RequestContext context) throws NoMatchingTransitionException {
		Transition transition = getTransitionSet().getTransition(context);
		if (transition == null) {
			throw new NoMatchingTransitionException(this, context.getLastEvent());
		}
		return transition;
	}

	/**
	 * Re-enter this state. This is typically called when a transition out of
	 * this state is selected, but transition execution rolls back and as a
	 * result the flow reenters the source state.
	 * <p>
	 * By default, this just calls <code>enter()</code>.
	 * @param context the flow control context in an executing flow (a client
	 * instance of a flow)
	 * @return a view selection containing model and view information needed to
	 * render the results of the state processing
	 */
	public ViewSelection reenter(FlowExecutionControlContext context) {
		return enter(context);
	}

	/**
	 * Exit this state. This is typically called when a transition takes the
	 * flow out of this state into another state. By default just executes the
	 * exit action, if one is registered.
	 * @param context the flow control context
	 */
	public void exit(FlowExecutionControlContext context) {
		exitActionList.execute(context);
	}

	protected void appendToString(ToStringCreator creator) {
		creator.append("transitions", transitions).append("exitActionList", exitActionList);
	}
}