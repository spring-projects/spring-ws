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
import org.springframework.util.Assert;

/**
 * A transitionable state that spawns a subflow when executed. When the subflow
 * this state spawns ends, the ending result is used as grounds for a state
 * transition out of this state.
 * <p>
 * A subflow state may be configured to map input data from its flow -- acting
 * as the parent flow -- down to the subflow when the subflow is spawned. In
 * addition, output data produced by the subflow may be mapped up to the parent
 * flow when the subflow ends and the parent flow resumes. See the
 * {@link FlowAttributeMapper} interface definition for more information on how
 * to do this. The logic for ending a subflow is located in the {@link EndState}
 * implementation.
 * 
 * @see org.springframework.webflow.FlowAttributeMapper
 * @see org.springframework.webflow.EndState
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class SubflowState extends TransitionableState {

	/**
	 * The subflow that should be spawned when this subflow state is entered.
	 */
	private Flow subflow;

	/**
	 * The attribute mapper that should map attributes from the parent flow down
	 * to the spawned subflow and visa versa.
	 */
	private FlowAttributeMapper attributeMapper;

	/**
	 * Create a new subflow state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param subflow the subflow to spawn
	 * @throws IllegalArgumentException when this state cannot be added to given
	 * flow
	 * @see TransitionableState#TransitionableState(Flow, String)
	 * @see #setAttributeMapper(FlowAttributeMapper)
	 */
	public SubflowState(Flow flow, String id, Flow subflow) throws IllegalArgumentException {
		super(flow, id);
		setSubflow(subflow);
	}

	/**
	 * Set the subflow that will be spawned by this state.
	 * @param subflow the subflow to spawn
	 */
	private void setSubflow(Flow subflow) {
		Assert.notNull(subflow, "A subflow state must have a subflow; the subflow is required");
		this.subflow = subflow;
	}

	/**
	 * Returns the subflow spawned by this state.
	 */
	public Flow getSubflow() {
		return subflow;
	}

	/**
	 * Set the attribute mapper to use to map model data between parent and
	 * child subflow model. Can be null if no mapping is needed.
	 */
	public void setAttributeMapper(FlowAttributeMapper attributeMapper) {
		this.attributeMapper = attributeMapper;
	}

	/**
	 * Returns the attribute mapper used to map data between parent and child
	 * subflow model, or null if no mapping is needed.
	 */
	public FlowAttributeMapper getAttributeMapper() {
		return attributeMapper;
	}

	/**
	 * Specialization of State's <code>doEnter</code> template method that
	 * executes behaviour specific to this state type in polymorphic fashion.
	 * <p>
	 * Entering this state, creates the subflow input map and spawns the subflow
	 * in the current flow execution.
	 * @param context the control context for the currently executing flow, used
	 * by this state to manipulate the flow execution
	 * @return a view selection containing model and view information needed to
	 * render the results of the state execution
	 * @throws StateException if an exception occurs in this state
	 */
	protected ViewSelection doEnter(FlowExecutionControlContext context) throws StateException {
		if (logger.isDebugEnabled()) {
			logger.debug("Spawning subflow '" + getSubflow().getId() + "' within flow '" + getFlow().getId() + "'");
		}
		return context.start(getSubflow(), createSubflowInput(context));
	}

	private AttributeMap createSubflowInput(RequestContext context) {
		if (getAttributeMapper() != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Messaging the configured attribute mapper to map attributes "
						+ "down to the spawned subflow for access within the subflow");
			}
			return getAttributeMapper().createSubflowInput(context);
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("No attribute mapper configured for this subflow state '" + getId()
						+ "' -- as a result, no attributes in flow scope will be passed to the spawned subflow '"
						+ subflow.getId() + "'");
			}
			return null;
		}
	}

	public ViewSelection onEvent(Event event, FlowExecutionControlContext context) {
		mapSubflowOutput(event.getAttributes(), context);
		return super.onEvent(event, context);
	}

	private void mapSubflowOutput(UnmodifiableAttributeMap subflowOutput, RequestContext context) {
		if (getAttributeMapper() != null) {
			if (logger.isDebugEnabled()) {
				logger
						.debug("Messaging the configured attribute mapper to map subflow result attributes to the "
								+ "resuming parent flow -- It will have access to attributes passed up by the completed subflow");
			}
			attributeMapper.mapSubflowOutput(subflowOutput, context);
		}
		else {
			if (logger.isDebugEnabled()) {
				logger
						.debug("No attribute mapper is configured for the resuming state '"
								+ getId()
								+ "' -- as a result, no attributes in the ending subflow scope will be passed to the resuming flow");
			}
		}
	}

	protected void appendToString(ToStringCreator creator) {
		creator.append("subflow", subflow.getId()).append("attributeMapper", attributeMapper);
		super.appendToString(creator);
	}
}