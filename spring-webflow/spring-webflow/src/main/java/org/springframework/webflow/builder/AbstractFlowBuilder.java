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

import java.util.HashMap;
import java.util.Map;

import org.springframework.binding.mapping.AttributeMapper;
import org.springframework.binding.mapping.Mapping;
import org.springframework.binding.mapping.MappingBuilder;
import org.springframework.binding.method.MethodSignature;
import org.springframework.webflow.Action;
import org.springframework.webflow.AnnotatedAction;
import org.springframework.webflow.AttributeCollection;
import org.springframework.webflow.CollectionUtils;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.FlowAttributeMapper;
import org.springframework.webflow.ScopeType;
import org.springframework.webflow.State;
import org.springframework.webflow.StateExceptionHandler;
import org.springframework.webflow.TargetStateResolver;
import org.springframework.webflow.Transition;
import org.springframework.webflow.TransitionCriteria;
import org.springframework.webflow.ViewSelector;
import org.springframework.webflow.action.AbstractBeanInvokingAction;
import org.springframework.webflow.action.MultiAction;
import org.springframework.webflow.action.ResultSpecification;
import org.springframework.webflow.support.ActionTransitionCriteria;
import org.springframework.webflow.support.EventFactorySupport;

/**
 * Base class for flow builders that programmatically build flows in Java
 * configuration code.
 * <p>
 * To give you an example of what a simple Java-based web flow builder
 * definition might look like, the following example defines the 'dynamic' web
 * flow roughly equivalent to the work flow statically implemented in Spring
 * MVC's simple form controller:
 * 
 * <pre>
 * public class CustomerDetailFlowBuilder extends AbstractFlowBuilder {
 *     public void buildStates() {
 *         // get customer information
 *         addActionState(&quot;getDetails&quot;, action(&quot;customerAction&quot;),
 *             on(success(), to(&quot;displayDetails&quot;)));
 *                                                                                 
 *         // view customer information               
 *         addViewState(&quot;displayDetails&quot;, &quot;customerDetails&quot;,
 *             on(submit(), to(&quot;bindAndValidate&quot;));
 *                                                                             
 *         // bind and validate customer information updates 
 *         addActionState(&quot;bindAndValidate&quot;, action(&quot;customerAction&quot;),
 *             new Transition[] {
 *                 on(error(), to(&quot;displayDetails&quot;)),
 *                 on(success(), to(&quot;finish&quot;))
 *             });
 *                                                                                 
 *         // finish
 *         addEndState(&quot;finish&quot;);
 *     }
 * }
 * </pre>
 * 
 * What this Java-based FlowBuilder implementation does is add four states to a
 * flow. These include a "get" <code>ActionState</code> (the start state), a
 * <code>ViewState</code> state, a "bind and validate"
 * <code>ActionState</code>, and an end marker state (<code>EndState</code>).
 * 
 * The first state, an action state, will be assigned the indentifier
 * <code>getDetails</code>. This action state will automatically be
 * configured with the following defaults:
 * <ol>
 * <li>The action instance with id <code>customerAction</code>. This is the
 * <code>Action</code> implementation that will execute when this state is
 * entered. In this example, that <code>Action</code> will go out to the DB,
 * load the Customer, and put it in the Flow's request context.
 * <li>A <code>success</code> transition to a default view state, called
 * <code>displayDetails</code>. This means when the <code>Action</code>
 * returns a <code>success</code> result event (aka outcome), the
 * <code>displayDetails</code> state will be entered.
 * <li>It will act as the start state for this flow (by default, the first
 * state added to a flow during the build process is treated as the start
 * state).
 * </ol>
 * 
 * The second state, a view state, will be identified as
 * <code>displayDetails</code>. This view state will automatically be
 * configured with the following defaults:
 * <ol>
 * <li>A view name called <code>customerDetails</code>. This is the logical
 * name of a view resource. This logical view name gets mapped to a physical
 * view resource (jsp, etc.) by the calling front controller (via a Spring view
 * resolver, or a Struts action forward, for example).
 * <li>A <code>submit</code> transition to a bind and validate action state,
 * indentified by the default id <code>bindAndValidate</code>. This means
 * when a <code>submit</code> event is signaled by the view (for example, on a
 * submit button click), the bindAndValidate action state will be entered and
 * the <code>bindAndValidate</code> method of the
 * <code>customerAction</code> <code>Action</code> implementation will be
 * executed.
 * </ol>
 * 
 * The third state, an action state, will be indentified as <code>
 * bindAndValidate</code>.
 * This action state will automatically be configured with the following
 * defaults:
 * <ol>
 * <li>An action bean named <code>customerAction</code> -- this is the name
 * of the <code>Action</code> implementation exported in the application
 * context that will execute when this state is entered. In this example, the
 * <code>Action</code> has a "bindAndValidate" method that will bind form
 * input in the HTTP request to a backing Customer form object, validate it, and
 * update the DB.
 * <li>A <code>success</code> transition to a default end state, called
 * <code>finish</code>. This means if the <code>Action</code> returns a
 * <code>success</code> result, the <code>finish</code> end state will be
 * transitioned to and the flow will terminate.
 * <li>An <code>error</code> transition back to the form view. This means if
 * the <code>Action</code> returns an <code>error</code> event, the <code>
 * displayDetails</code>
 * view state will be transitioned back to.
 * </ol>
 * 
 * The fourth and last state, an end state, will be indentified with the default
 * end state id <code>finish</code>. This end state is a marker that signals
 * the end of the flow. When entered, the flow session terminates, and if this
 * flow is acting as a root flow in the current flow execution, any
 * flow-allocated resources will be cleaned up. An end state can optionally be
 * configured with a logical view name to forward to when entered. It will also
 * trigger a state transition in a resuming parent flow if this flow was
 * participating as a spawned 'subflow' within a suspended parent flow.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public abstract class AbstractFlowBuilder extends BaseFlowBuilder {

	/**
	 * A helper for creating commonly used event identifiers that drive
	 * transitions created by this builder.
	 */
	private EventFactorySupport eventFactorySupport = new EventFactorySupport();

	/**
	 * Default constructor for subclassing.
	 */
	protected AbstractFlowBuilder() {
		super();
	}

	/**
	 * Create an instance of an abstract flow builder, using the specified
	 * locator to obtain needed flow services at build time.
	 * @param flowServiceLocator the locator for services needed by this builder
	 * to build its Flow
	 */
	protected AbstractFlowBuilder(FlowServiceLocator flowServiceLocator) {
		super(flowServiceLocator);
	}

	/**
	 * Returns the configured event factory support helper for creating commonly
	 * used event identifiers that drive transitions created by this builder.
	 */
	public EventFactorySupport getEventFactorySupport() {
		return eventFactorySupport;
	}

	/**
	 * Sets the event factory support helper to use to create commonly used
	 * event identifiers that drive transitions created by this builder.
	 */
	public void setEventFactorySupport(EventFactorySupport eventFactorySupport) {
		this.eventFactorySupport = eventFactorySupport;
	}

	public void init(String flowId, AttributeCollection attributes) throws FlowBuilderException {
		setFlow(getFlowArtifactFactory().createFlow(flowId, flowAttributes().union(attributes)));
	}

	/**
	 * Hook subclasses may override to provide additional properties for the
	 * flow built by this builder. Returns a empty collection by default.
	 * @return additional properties describing the flow being built, should not
	 * return null
	 */
	protected AttributeCollection flowAttributes() {
		return CollectionUtils.EMPTY_ATTRIBUTE_MAP;
	}

	/**
	 * Adds a view state to the flow built by this builder.
	 * @param stateId the state identifier
	 * @param viewName the string-encoded view selector
	 * @param transition the sole transition (path) out of this state
	 * @return the fully constructed view state instance
	 */
	protected State addViewState(String stateId, String viewName, Transition transition) {
		return getFlowArtifactFactory().createViewState(stateId, getFlow(), null, viewSelector(viewName),
				new Transition[] { transition }, null, null, null);
	}

	/**
	 * Adds a view state to the flow built by this builder.
	 * @param stateId the state identifier
	 * @param viewName the string-encoded view selector
	 * @param transitions the transitions (path) out of this state
	 * @return the fully constructed view state instance
	 */
	protected State addViewState(String stateId, String viewName, Transition[] transitions) {
		return getFlowArtifactFactory().createViewState(stateId, getFlow(), null, viewSelector(viewName), transitions,
				null, null, null);
	}

	/**
	 * Adds a view state to the flow built by this builder.
	 * @param stateId the state identifier
	 * @param entryActions the actions to execute when the state is entered
	 * @param viewSelector the view selector that will make the view selection
	 * when the state is entered
	 * @param transitions the transitions (path) out of this state
	 * @param exceptionHandlers any exception handlers to attach to the state
	 * @param exitActions the actions to execute when the state exits
	 * @param attributes attributes to assign to the state that may be used to
	 * affect state construction and execution
	 * @return the fully constructed view state instance
	 */
	protected State addViewState(String stateId, Action[] entryActions, ViewSelector viewSelector,
			Transition[] transitions, StateExceptionHandler[] exceptionHandlers, Action[] exitActions,
			AttributeCollection attributes) {
		return getFlowArtifactFactory().createViewState(stateId, getFlow(), entryActions, viewSelector, transitions,
				exceptionHandlers, exitActions, attributes);
	}

	/**
	 * Adds an action state to the flow built by this builder.
	 * @param stateId the state identifier
	 * @param action the single action to execute when the state is entered
	 * @param transition the single transition (path) out of this state
	 * @return the fully constructed action state instance
	 */
	protected State addActionState(String stateId, Action action, Transition transition) {
		return getFlowArtifactFactory().createActionState(stateId, getFlow(), null, new Action[] { action },
				new Transition[] { transition }, null, null, null);
	}

	/**
	 * Adds an action state to the flow built by this builder.
	 * @param stateId the state identifier
	 * @param action the single action to execute when the state is entered
	 * @param transitions the transitions (paths) out of this state
	 * @return the fully constructed action state instance
	 */
	protected State addActionState(String stateId, Action action, Transition[] transitions) {
		return getFlowArtifactFactory().createActionState(stateId, getFlow(), null, new Action[] { action },
				transitions, null, null, null);
	}

	/**
	 * Adds an action state to the flow built by this builder.
	 * @param stateId the state identifier
	 * @param action the single action to execute when the state is entered
	 * @param transition the single transition (path) out of this state
	 * @param exceptionHandler the exception handler to handle exceptions thrown
	 * by the action
	 * @return the fully constructed action state instance
	 */
	protected State addActionState(String stateId, Action action, Transition transition,
			StateExceptionHandler exceptionHandler) {
		return getFlowArtifactFactory().createActionState(stateId, getFlow(), null, new Action[] { action },
				new Transition[] { transition }, new StateExceptionHandler[] { exceptionHandler }, null, null);
	}

	/**
	 * Adds an action state to the flow built by this builder.
	 * @param stateId the state identifier
	 * @param entryActions any generic entry actions to add to the state
	 * @param actions the actions to execute in a chain when the state is entered
	 * @param transitions the transitions (paths) out of this state
	 * @param exceptionHandlers the exception handlers to handle exceptions
	 * thrown by the actions
	 * @param exitActions the exit actions to execute when the state exits
	 * @param attributes attributes to assign to the state that may be used to
	 * affect state construction and execution
	 * @return the fully constructed action state instance
	 */
	protected State addActionState(String stateId, Action[] entryActions, Action[] actions, Transition[] transitions,
			StateExceptionHandler[] exceptionHandlers, Action[] exitActions, AttributeCollection attributes) {
		return getFlowArtifactFactory().createActionState(stateId, getFlow(), entryActions, actions, transitions,
				exceptionHandlers, exitActions, attributes);
	}

	/**
	 * Adds a decision state to the flow built by this builder.
	 * @param stateId the state identifier
	 * @param decisionCriteria the criteria that defines the decision
	 * @param trueStateResolver the resolver that will resolve the transition
	 * target state on a "true" decision
	 * @param falseStateResolver the resolver that will resolve the transition
	 * target state on a "false" decision
	 * @return the fully constructed decision state instance
	 */
	protected State addDecisionState(String stateId, TransitionCriteria decisionCriteria,
			TargetStateResolver trueStateResolver, TargetStateResolver falseStateResolver) {
		Transition thenTransition = getFlowArtifactFactory().createTransition(decisionCriteria, null,
				trueStateResolver, null);
		Transition elseTransition = getFlowArtifactFactory().createTransition(null, null, falseStateResolver, null);
		return getFlowArtifactFactory().createDecisionState(stateId, getFlow(), null,
				new Transition[] { thenTransition, elseTransition }, null, null, null);
	}

	/**
	 * Adds a decision state to the flow built by this builder.
	 * @param stateId the state identifier
	 * @param transitions the transitions (paths) out of this state
	 * @return the fully constructed decision state instance
	 */
	protected State addDecisionState(String stateId, Transition[] transitions) {
		return getFlowArtifactFactory().createDecisionState(stateId, getFlow(), null, transitions, null, null, null);
	}

	/**
	 * Adds a decision state to the flow built by this builder.
	 * @param stateId the state identifier
	 * @param entryActions the entry actions to execute when the state enters
	 * @param transitions the transitions (paths) out of this state
	 * @param exceptionHandlers the exception handlers to handle exceptions
	 * thrown by the state
	 * @param exitActions the exit actions to execute when the state exits
	 * @param attributes attributes to assign to the state that may be used to
	 * affect state construction and execution
	 * @return the fully constructed decision state instance
	 */
	protected State addDecisionState(String stateId, Action[] entryActions, Transition[] transitions,
			StateExceptionHandler[] exceptionHandlers, Action[] exitActions, AttributeCollection attributes) {
		return getFlowArtifactFactory().createDecisionState(stateId, getFlow(), entryActions, transitions,
				exceptionHandlers, exitActions, attributes);
	}

	/**
	 * Adds a subflow state to the flow built by this builder.
	 * @param stateId the state identifier
	 * @param subflow the flow that will act as the subflow
	 * @param attributeMapper the mapper to map subflow input and output
	 * attributes
	 * @param transition the single transition (path) out of the state
	 * @return the fully constructed subflow state instance
	 */
	protected State addSubflowState(String stateId, Flow subflow, FlowAttributeMapper attributeMapper,
			Transition transition) {
		return getFlowArtifactFactory().createSubflowState(stateId, getFlow(), null, subflow, attributeMapper,
				new Transition[] { transition }, null, null, null);
	}

	/**
	 * Adds a subflow state to the flow built by this builder.
	 * @param stateId the state identifier
	 * @param subflow the flow that will act as the subflow
	 * @param attributeMapper the mapper to map subflow input and output
	 * attributes
	 * @param transitions the transitions (paths) out of the state
	 * @return the fully constructed subflow state instance
	 */
	protected State addSubflowState(String stateId, Flow subflow, FlowAttributeMapper attributeMapper,
			Transition[] transitions) {
		return getFlowArtifactFactory().createSubflowState(stateId, getFlow(), null, subflow, attributeMapper,
				transitions, null, null, null);
	}

	/**
	 * Adds a subflow state to the flow built by this builder.
	 * @param stateId the state identifier
	 * @param entryActions the entry actions to execute when the state enters
	 * @param subflow the flow that will act as the subflow
	 * @param attributeMapper the mapper to map subflow input and output
	 * attributes
	 * @param transitions the transitions (paths) out of this state
	 * @param exceptionHandlers the exception handlers to handle exceptions
	 * thrown by the state
	 * @param exitActions the exit actions to execute when the state exits
	 * @param attributes attributes to assign to the state that may be used to
	 * affect state construction and execution
	 * @return the fully constructed subflow state instance
	 */
	protected State addSubflowState(String stateId, Action[] entryActions, Flow subflow,
			FlowAttributeMapper attributeMapper, Transition[] transitions, StateExceptionHandler[] exceptionHandlers,
			Action[] exitActions, AttributeCollection attributes) {
		return getFlowArtifactFactory().createSubflowState(stateId, getFlow(), entryActions, subflow, attributeMapper,
				transitions, exceptionHandlers, exitActions, attributes);
	}

	/**
	 * Adds an end state to the flow built by this builder.
	 * @param stateId the state identifier
	 * @return the fully constructed end state instance
	 */
	protected State addEndState(String stateId) {
		return getFlowArtifactFactory().createEndState(stateId, getFlow(), null, null, null, null, null);
	}

	/**
	 * Adds an end state to the flow built by this builder.
	 * @param stateId the state identifier
	 * @param viewName the string-encoded view selector
	 * @return the fully constructed end state instance
	 */
	protected State addEndState(String stateId, String viewName) {
		return getFlowArtifactFactory().createEndState(stateId, getFlow(), null, endingViewSelector(viewName), null,
				null, null);
	}

	/**
	 * Adds an end state to the flow built by this builder.
	 * @param stateId the state identifier
	 * @param viewName the string-encoded view selector
	 * @param outputMapper the output mapper to map output attributes for the
	 * end state (a flow outcome)
	 * @return the fully constructed end state instance
	 */
	protected State addEndState(String stateId, String viewName, AttributeMapper outputMapper) {
		return getFlowArtifactFactory().createEndState(stateId, getFlow(), null, endingViewSelector(viewName),
				outputMapper, null, null);
	}

	/**
	 * Adds an end state to the flow built by this builder.
	 * @param stateId the state identifier
	 * @param entryActions the actions to execute when the state is entered
	 * @param viewSelector the view selector that will make the view selection
	 * when the state is entered
	 * @param outputMapper the output mapper to map output attributes for the
	 * end state (a flow outcome)
	 * @param exceptionHandlers any exception handlers to attach to the state
	 * @param attributes attributes to assign to the state that may be used to
	 * affect state construction and execution
	 * @return the fully constructed end state instance
	 */
	protected State addEndState(String stateId, Action[] entryActions, ViewSelector viewSelector,
			AttributeMapper outputMapper, StateExceptionHandler[] exceptionHandlers, AttributeCollection attributes) {
		return getFlowArtifactFactory().createEndState(stateId, getFlow(), entryActions, viewSelector, outputMapper,
				exceptionHandlers, attributes);
	}

	/**
	 * Factory method that creates a view state view selector from an encoded
	 * view name. See {@link TextToViewSelector} for information on the
	 * conversion rules.
	 * @param viewName the encoded view selector
	 * @return the view selector
	 */
	protected ViewSelector viewSelector(String viewName) {
		return viewSelector(TextToViewSelector.VIEW_STATE_TYPE, viewName);
	}

	/**
	 * Factory method that creates a end state view selector from an encoded
	 * view name. See {@link TextToViewSelector} for information on the
	 * conversion rules.
	 * @param viewName the encoded view selector
	 * @return the view selector
	 */
	protected ViewSelector endingViewSelector(String viewName) {
		return viewSelector(TextToViewSelector.END_STATE_TYPE, viewName);
	}

	private ViewSelector viewSelector(String stateType, String viewName) {
		Map context = new HashMap(1, 1);
		context.put(TextToViewSelector.STATE_TYPE_CONTEXT_PARAMETER, stateType);
		return (ViewSelector)fromStringTo(ViewSelector.class).execute(viewName, context);
	}

	/**
	 * Resolves the action with the specified id. Simply looks the action up by
	 * id and returns it.
	 * @param id the action id
	 * @return the action
	 * @throws FlowArtifactException the action could not be resolved
	 */
	protected Action action(String id) throws FlowArtifactException {
		return getFlowServiceLocator().getAction(id);
	}

	/**
	 * Creates a bean invoking action that invokes the method identified by the
	 * signature on the bean associated with the action identifier.
	 * @param id the action id identifying a arbitrary
	 * <code>java.lang.Object</code> to be used as an action
	 * @param methodSignature the signature of the method to invoke on the POJO
	 * @return the adapted bean invoking action
	 * @throws FlowArtifactException the action could not be resolved
	 */
	protected Action action(String id, MethodSignature methodSignature) throws FlowArtifactException {
		return getBeanInvokingActionFactory().createBeanInvokingAction(id, getFlowServiceLocator().getBeanFactory(),
				methodSignature, null, getFlowServiceLocator().getConversionService(), null);
	}

	/**
	 * Creates a bean invoking action that invokes the method identified by the
	 * signature on the bean associated with the action identifier.
	 * @param id the action id identifying a arbitrary
	 * <code>java.lang.Object</code> to be used as an action
	 * @param methodSignature the signature of the method to invoke on the POJO
	 * @return the adapted bean invoking action
	 * @throws FlowArtifactException the action could not be resolved
	 */
	protected Action action(String id, MethodSignature methodSignature, ResultSpecification resultSpecification)
			throws FlowArtifactException {
		return getBeanInvokingActionFactory().createBeanInvokingAction(id, getFlowServiceLocator().getBeanFactory(),
				methodSignature, resultSpecification, getFlowServiceLocator().getConversionService(), null);
	}

	/**
	 * Convert the encoded method signature string to a {@link MethodSignature}
	 * object. Method signatures are used to match methods on POJO services to
	 * invoke on a {@link AbstractBeanInvokingAction bean invoking action}.
	 * <p>
	 * Encoded Method signature format: Method without arguments:
	 * 
	 * <pre>
	 *     ${methodName}
	 * </pre>
	 * 
	 * Method with arguments:
	 * 
	 * <pre>
	 *     ${methodName}(${arg1}, ${arg2}, ${arg n})
	 * </pre>
	 * 
	 * @param method the encoded method signature
	 * @return the method signature
	 * @see #action(String, MethodSignature, ResultSpecification)
	 * 
	 */
	protected MethodSignature method(String method) {
		return (MethodSignature)fromStringTo(MethodSignature.class).execute(method);
	}

	/**
	 * Factory method for a {@link {@link MethodSignature} method signature}
	 * {@link {@link ResultSpecification} result specification}.
	 * A result specification is used to expose a return value of a POJO invoked 
	 * as part of a {@link AbstractBeanInvokingAction bean invoking action}.
	 * @param resultName the result name
	 * @return the result specification
	 * @see #action(String, MethodSignature, ResultSpecification)
	 */
	protected ResultSpecification result(String resultName) {
		return result(resultName, null);
	}

	/**
	 * Factory method for a {@link {@link MethodSignature} method signature}
	 * {@link {@link ResultSpecification} result specification}.
	 * A result specification is used to expose a return value of a POJO invoked 
	 * as part of a {@link AbstractBeanInvokingAction bean invoking action}.
	 * @param resultName the result name attribute
	 * @param resultScope the scope of the result
	 * @return the result specification
	 * @see #action(String, MethodSignature, ResultSpecification)
	 */
	protected ResultSpecification result(String resultName, ScopeType resultScope) {
		return new ResultSpecification(resultName, resultScope);
	}

	/**
	 * Creates an annotated action decorator that instructs the specified method
	 * be invoked on the multi action when it is executed. Use this when working
	 * with MultiActions to specify the method on the MultiAction to invoke for
	 * a particular usage scenario. Use the {@link #method(String)} factory
	 * method when working with
	 * {@link AbstractBeanInvokingAction bean invoking actions}.
	 * @param methodName the name of the method on the multi action instance
	 * @param multiAction the multi action
	 * @return the annotated action that when invoked sets up a context property
	 * used by the multi action to instruct it with what method to invoke
	 */
	protected AnnotatedAction invoke(String methodName, MultiAction multiAction) throws FlowArtifactException {
		AnnotatedAction action = new AnnotatedAction(multiAction);
		action.getAttributeMap().put(AnnotatedAction.METHOD_ATTRIBUTE, methodName);
		return action;
	}

	/**
	 * Request that the attribute mapper with the specified name be used to map
	 * attributes between a parent flow and a spawning subflow when the subflow
	 * state being constructed is entered.
	 * @param id the id of the attribute mapper that will map attributes between
	 * the flow built by this builder and the subflow
	 * @return the attribute mapper
	 * @throws FlowArtifactException no FlowAttributeMapper implementation was
	 * exported with the specified id
	 */
	protected FlowAttributeMapper attributeMapper(String id) throws FlowArtifactException {
		return getFlowServiceLocator().getAttributeMapper(id);
	}

	/**
	 * Request that the <code>Flow</code> with the specified flowId be spawned
	 * as a subflow when the subflow state being built is entered. Simply
	 * resolves the subflow definition by id and returns it; throwing a
	 * fail-fast exception if it does not exist.
	 * @param id the flow definition id
	 * @return the flow to be used as a subflow, this should be passed to a
	 * addSubflowState call
	 * @throws FlowArtifactException when the flow cannot be resolved
	 */
	protected Flow flow(String id) throws FlowArtifactException {
		return getFlowServiceLocator().getSubflow(id);
	}

	/**
	 * Creates a transition criteria that is used to match a Transition. The
	 * criteria is based on the provided expression string.
	 * @param transitionCriteriaExpression the transition criteria expression,
	 * typically simply a static event identifier (e.g. "submit")
	 * @return the transition criteria
	 */
	protected TransitionCriteria on(String transitionCriteriaExpression) {
		return (TransitionCriteria)fromStringTo(TransitionCriteria.class).execute(transitionCriteriaExpression);
	}

	/**
	 * Creates a transition target state resolver that is used to take a
	 * Transition to its target state during execution.
	 * @param targetStateExpression the target state expression, typically
	 * simply the targetStateId
	 * @return the target state resolver
	 */
	protected TargetStateResolver to(String targetStateExpression) {
		return (TargetStateResolver)fromStringTo(TargetStateResolver.class).execute(targetStateExpression);
	}

	/**
	 * Creates a new transition.
	 * @param matchingCriteria the criteria that determines when the transition matches
	 * @param targetStateResolver the resolver that calculates the target state of the transition
	 * @return the transition
	 */
	protected Transition transition(TransitionCriteria matchingCriteria, TargetStateResolver targetStateResolver) {
		return getFlowArtifactFactory().createTransition(matchingCriteria, null, targetStateResolver, null);
	}

	/**
	 * Creates a new transition.
	 * @param matchingCriteria the criteria that determines when the transition matches
	 * @param targetStateResolver the resolver that calculates the target state of the transition
	 * @param executionCriteria the criteria that determines if a matched transition is allowed to execute
	 * @return the transition
	 */
	protected Transition transition(TransitionCriteria matchingCriteria, TargetStateResolver targetStateResolver,
			TransitionCriteria executionCriteria) {
		return getFlowArtifactFactory()
				.createTransition(matchingCriteria, executionCriteria, targetStateResolver, null);
	}

	/**
	 * Creates a new transition.
	 * @param matchingCriteria
	 * @param executionCriteria
	 * @param targetStateResolver
	 * @param attributes
	 * @return the transition
	 */
	protected Transition transition(TransitionCriteria matchingCriteria, TargetStateResolver targetStateResolver,
			TransitionCriteria executionCriteria, AttributeCollection attributes) {
		return getFlowArtifactFactory().createTransition(matchingCriteria, executionCriteria, targetStateResolver,
				attributes);
	}

	/**
	 * Creates a <code>TransitionCriteria</code> that will execute the
	 * specified action when the Transition is executed but before the
	 * transition's target state is entered.
	 * <p>
	 * This criteria will only allow the Transition to complete execution if the
	 * Action completes successfully.
	 * @param action the action to execute after a transition is matched but
	 * before it transitions to its target state
	 * @return the transition execution criteria
	 */
	protected TransitionCriteria ifReturnedSuccess(Action action) {
		return new ActionTransitionCriteria(action);
	}

	/**
	 * Creates the <code>success</code> event id. "Success" indicates that an
	 * action completed successfuly.
	 * @return the event id
	 */
	protected String success() {
		return eventFactorySupport.getSuccessEventId();
	}

	/**
	 * Creates the <code>error</code> event id. "Error" indicates that an
	 * action completed with an error status.
	 * @return the event id
	 */
	protected String error() {
		return eventFactorySupport.getErrorEventId();
	}

	/**
	 * Creates the <code>submit</code> event id. "Submit" indicates the user
	 * submitted a request (form) for processing.
	 * @return the event id
	 */
	protected String submit() {
		return "submit";
	}

	/**
	 * Creates the <code>back</code> event id. "Back" indicates the user wants
	 * to go to the previous step in the flow.
	 * @return the event id
	 */
	protected String back() {
		return "back";
	}

	/**
	 * Creates the <code>cancel</code> event id. "Cancel" indicates the flow
	 * was aborted because the user changed their mind.
	 * @return the event id
	 */
	protected String cancel() {
		return "cancel";
	}

	/**
	 * Creates the <code>finish</code> event id. "Finish" indicates the flow
	 * has finished processing.
	 * @return the event id
	 */
	protected String finish() {
		return "finish";
	}

	/**
	 * Creates the <code>select</code> event id. "Select" indicates an object
	 * was selected for processing or display.
	 * @return the event id
	 */
	protected String select() {
		return "select";
	}

	/**
	 * Creates the <code>edit</code> event id. "Edit" indicates an object was
	 * selected for creation or updating.
	 * @return the event id
	 */
	protected String edit() {
		return "edit";
	}

	/**
	 * Creates the <code>add</code> event id. "Add" indicates a child object
	 * is being added to a parent collection.
	 * @return the event id
	 */
	protected String add() {
		return "add";
	}

	/**
	 * Creates the <code>delete</code> event id. "Delete" indicates a object
	 * is being removed.
	 * @return the event id
	 */
	protected String delete() {
		return "delete";
	}

	/**
	 * Creates the <code>yes</code> event id. "Yes" indicates a true result
	 * was returned.
	 * @return the event id
	 */
	protected String yes() {
		return eventFactorySupport.getYesEventId();
	}

	/**
	 * Creates the <code>no</code> event id. "False" indicates a false result
	 * was returned.
	 * @return the event id
	 */
	protected String no() {
		return eventFactorySupport.getNoEventId();
	}

	/**
	 * Factory method that returns a new, fully configured mapping builder to
	 * assist with building {@link Mapping} objects used by a
	 * {@link FlowAttributeMapper} to map attributes.
	 * @return the mapping builder
	 */
	protected MappingBuilder mapping() {
		MappingBuilder mapping = new MappingBuilder(getFlowServiceLocator().getExpressionParser());
		mapping.setConversionService(getFlowServiceLocator().getConversionService());
		return mapping;
	}

	private FlowArtifactFactory getFlowArtifactFactory() {
		return getFlowServiceLocator().getFlowArtifactFactory();
	}

	private BeanInvokingActionFactory getBeanInvokingActionFactory() {
		return getFlowServiceLocator().getBeanInvokingActionFactory();
	}
}