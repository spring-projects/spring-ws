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
package org.springframework.webflow.builder;

import org.springframework.binding.mapping.AttributeMapper;
import org.springframework.webflow.Action;
import org.springframework.webflow.ActionState;
import org.springframework.webflow.AttributeCollection;
import org.springframework.webflow.DecisionState;
import org.springframework.webflow.EndState;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.State;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.SubflowState;
import org.springframework.webflow.TargetStateResolver;
import org.springframework.webflow.Transition;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.TransitionableState;
import org.springframework.webflow.ViewSelector;
import org.springframework.webflow.ViewState;

/**
 * A factory for core web flow elements such as {@link Flow flows},
 * {@link State states}, and {@link Transition transitions}.
 * <p>
 * This factory encapsulates the construct of each Flow implementation as well
 * as each core state type. Subclasses may customize how the core elements are
 * created, useful for plugging in custom implementations.
 * 
 * @author Keith Donald
 */
public class FlowArtifactFactory {

	/**
	 * Factory method that creates a new {@link Flow} definition object.
	 * <p>
	 * Note this method does not return a fully configured Flow instance, it
	 * only encapsulates the selection of implementation. A
	 * {@link FlowAssembler} delegating to a calling {@link FlowBuilder} is
	 * expected to assemble the Flow fully before returning it to external
	 * clients.
	 * @param id the flow id the flow identifier, should be unique to all flows
	 * in an application (required)
	 * @param attributes attributes to assign to the Flow, which may also be
	 * used to affect flow construction; may be null
	 * @return the initial flow instance, ready for assembly by a FlowBuilder
	 * @throws FlowArtifactException an exception occured creating the Flow
	 * instance
	 */
	public Flow createFlow(String id, AttributeCollection attributes) throws FlowArtifactException {
		Flow flow = new Flow(id);
		flow.getAttributeMap().putAll(attributes);
		return flow;
	}

	/**
	 * Factory method that creates a new view state, a state where a user is
	 * allowed to participate in the flow. This method is an atomic operation
	 * that returns a fully initialized state. It encapsulates the selection of
	 * the view state implementation as well as the state assembly.
	 * @param id the identifier to assign to the state, must be unique to its
	 * owning flow (required)
	 * @param flow the flow that will own (contain) this state (required)
	 * @param entryActions any state entry actions; may be null
	 * @param viewSelector the state view selector strategy; may be null
	 * @param transitions any transitions (paths) out of this state; may be null
	 * @param exceptionHandlers any exception handlers; may be null
	 * @param exitActions any state exit actions; may be null
	 * @param attributes attributes to assign to the State; which may also be
	 * used to affect state construction. May be null.
	 * @return the fully initialized view state instance
	 * @throws FlowArtifactException an exception occured creating the state
	 */
	public State createViewState(String id, Flow flow, Action[] entryActions, ViewSelector viewSelector,
			Transition[] transitions, StateExceptionHandler[] exceptionHandlers, Action[] exitActions,
			AttributeCollection attributes) throws FlowArtifactException {
		ViewState viewState = new ViewState(flow, id);
		if (viewSelector != null) {
			viewState.setViewSelector(viewSelector);
		}
		configureCommonProperties(viewState, entryActions, transitions, exceptionHandlers, exitActions, attributes);
		return viewState;
	}

	/**
	 * Factory method that creates a new action state, a state where a system
	 * action is executed. This method is an atomic operation that returns a
	 * fully initialized state. It encapsulates the selection of the action
	 * state implementation as well as the state assembly.
	 * @param id the identifier to assign to the state, must be unique to its
	 * owning flow (required)
	 * @param flow the flow that will own (contain) this state (required)
	 * @param entryActions any state entry actions; may be null
	 * @param actions the actions to execute when the state is entered
	 * (required)
	 * @param transitions any transitions (paths) out of this state; may be null
	 * @param exceptionHandlers any exception handlers; may be null
	 * @param exitActions any state exit actions; may be null
	 * @param attributes attributes to assign to the State; which may also be
	 * used to affect state construction. May be null.
	 * @return the fully initialized action state instance
	 * @throws FlowArtifactException an exception occured creating the state
	 */
	public State createActionState(String id, Flow flow, Action[] entryActions, Action[] actions,
			Transition[] transitions, StateExceptionHandler[] exceptionHandlers, Action[] exitActions,
			AttributeCollection attributes) throws FlowArtifactException {
		ActionState actionState = new ActionState(flow, id);
		actionState.getActionList().addAll(actions);
		configureCommonProperties(actionState, entryActions, transitions, exceptionHandlers, exitActions, attributes);
		return actionState;
	}

	/**
	 * Factory method that creates a new decision state, a state where a flow
	 * routing decision is made. This method is an atomic operation that returns
	 * a fully initialized state. It encapsulates the selection of the decision
	 * state implementation as well as the state assembly.
	 * @param id the identifier to assign to the state, must be unique to its
	 * owning flow (required)
	 * @param flow the flow that will own (contain) this state (required)
	 * @param entryActions any state entry actions; may be null
	 * @param transitions any transitions (paths) out of this state
	 * @param exceptionHandlers any exception handlers; may be null
	 * @param exitActions any state exit actions; may be null
	 * @param attributes attributes to assign to the State; which may also be
	 * used to affect state construction. May be null.
	 * @return the fully initialized decision state instance
	 * @throws FlowArtifactException an exception occured creating the state
	 */
	public State createDecisionState(String id, Flow flow, Action[] entryActions, Transition[] transitions,
			StateExceptionHandler[] exceptionHandlers, Action[] exitActions, AttributeCollection attributes)
			throws FlowArtifactException {
		DecisionState decisionState = new DecisionState(flow, id);
		configureCommonProperties(decisionState, entryActions, transitions, exceptionHandlers, exitActions, attributes);
		return decisionState;
	}

	/**
	 * Factory method that creates a new subflow state, a state where a parent
	 * flow spawns another flow as a subflow. This method is an atomic operation
	 * that returns a fully initialized state. It encapsulates the selection of
	 * the subflow state implementation as well as the state assembly.
	 * @param id the identifier to assign to the state, must be unique to its
	 * owning flow (required)
	 * @param flow the flow that will own (contain) this state (required)
	 * @param entryActions any state entry actions; may be null
	 * @param subflow the subflow definition (required)
	 * @param attributeMapper the subflow input and output attribute mapper; may
	 * be null
	 * @param transitions any transitions (paths) out of this state
	 * @param exceptionHandlers any exception handlers; may be null
	 * @param exitActions any state exit actions; may be null
	 * @param attributes attributes to assign to the State; which may also be
	 * used to affect state construction. May be null.
	 * @return the fully initialized subflow state instance
	 * @throws FlowArtifactException an exception occured creating the state
	 */
	public State createSubflowState(String id, Flow flow, Action[] entryActions, Flow subflow,
			FlowAttributeMapper attributeMapper, Transition[] transitions, StateExceptionHandler[] exceptionHandlers,
			Action[] exitActions, AttributeCollection attributes) throws FlowArtifactException {
		SubflowState subflowState = new SubflowState(flow, id, subflow);
		if (attributeMapper != null) {
			subflowState.setAttributeMapper(attributeMapper);
		}
		configureCommonProperties(subflowState, entryActions, transitions, exceptionHandlers, exitActions, attributes);
		return subflowState;
	}

	/**
	 * Factory method that creates a new end state, a state where an executing
	 * flow session terminates. This method is an atomic operation that returns
	 * a fully initialized state. It encapsulates the selection of the end state
	 * implementation as well as the state assembly.
	 * @param id the identifier to assign to the state, must be unique to its
	 * owning flow (required)
	 * @param flow the flow that will own (contain) this state (required)
	 * @param entryActions any state entry actions; may be null
	 * @param viewSelector the state confirmation view selector strategy; may be
	 * null
	 * @param outputMapper the state output mapper; may be null
	 * @param exceptionHandlers any exception handlers; may be null
	 * @param attributes attributes to assign to the State; which may also be
	 * used to affect state construction. May be null.
	 * @return the fully initialized subflow state instance
	 * @throws FlowArtifactException an exception occured creating the state
	 */
	public State createEndState(String id, Flow flow, Action[] entryActions, ViewSelector viewSelector,
			AttributeMapper outputMapper, StateExceptionHandler[] exceptionHandlers, AttributeCollection attributes)
			throws FlowArtifactException {
		EndState endState = new EndState(flow, id);
		if (viewSelector != null) {
			endState.setViewSelector(viewSelector);
		}
		if (outputMapper != null) {
			endState.setOutputMapper(outputMapper);
		}
		configureCommonProperties(endState, entryActions, exceptionHandlers, attributes);
		return endState;
	}

	/**
	 * Factory method that creates a new transition, a path from one step in a
	 * flow to another. This method is an atomic operation that returns a fully
	 * initialized transition. It encapsulates the selection of the transition
	 * implementation as well as the transition assembly.
	 * @param matchingCriteria the criteria that matches the transition; may be
	 * null
	 * @param executionCriteria the criteria that governs execution of the
	 * transition after match; may be null
	 * @param targetStateResolver the resolver for calculating the target state
	 * of the transition (required)
	 * @param attributes attributes to assign to the transition, which may also
	 * be used to affect transition construction. May be null.
	 * @return the fully initialized transition instance
	 * @throws FlowArtifactException an exception occured creating the
	 * transition
	 */
	public Transition createTransition(TransitionCriteria matchingCriteria, TransitionCriteria executionCriteria,
			TargetStateResolver targetStateResolver, AttributeCollection attributes) throws FlowArtifactException {
		Transition transition = new Transition(targetStateResolver);
		if (matchingCriteria != null) {
			transition.setMatchingCriteria(matchingCriteria);
		}
		if (executionCriteria != null) {
			transition.setExecutionCriteria(executionCriteria);
		}
		transition.getAttributeMap().putAll(attributes);
		return transition;
	}

	private void configureCommonProperties(TransitionableState state, Action[] entryActions, Transition[] transitions,
			StateExceptionHandler[] exceptionHandlers, Action[] exitActions, AttributeCollection attributes) {
		configureCommonProperties(state, entryActions, exceptionHandlers, attributes);
		state.getTransitionSet().addAll(transitions);
		state.getExitActionList().addAll(exitActions);
	}

	private void configureCommonProperties(State state, Action[] entryActions,
			StateExceptionHandler[] exceptionHandlers, AttributeCollection attributes) {
		state.getEntryActionList().addAll(entryActions);
		state.getExceptionHandlerSet().addAll(exceptionHandlers);
		state.getAttributeMap().putAll(attributes);
	}
}