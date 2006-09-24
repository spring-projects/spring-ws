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

import org.springframework.util.Assert;
import org.springframework.webflow.AttributeCollection;
import org.springframework.webflow.CollectionUtils;
import org.springframework.webflow.UnmodifiableAttributeMap;

/**
 * A director for assembling flows, delegating to a {@link FlowBuilder} to
 * construct a flow. This class encapsulates the algorithm for using a
 * FlowBuilder to assemble a Flow properly. It acts as the director in the
 * classic GoF builder pattern.
 * <p>
 * Flow assemblers may be used in a standalone, programmatic fashion as follows:
 * 
 * <pre>
 *     FlowBuilder builder = ...;
 *     new FlowAssembler(&quot;myFlow&quot;, builder).assembleFlow();
 *     Flow flow = builder.getFlow();
 * </pre>
 * 
 * @see org.springframework.webflow.builder.FlowBuilder
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowAssembler {

	/**
	 * The identifier to assign to the flow.
	 */
	private String flowId;

	/**
	 * Attributes that can be used to affect flow construction.
	 */
	private UnmodifiableAttributeMap flowAttributes;

	/**
	 * The flow builder strategy used to construct the flow from its component
	 * parts.
	 */
	private FlowBuilder flowBuilder;

	/**
	 * Create a new flow assembler that will direct Flow assembly using the
	 * specified builder strategy.
	 * @param flowId the assigned flow id
	 * @param flowBuilder the builder the factory will use to build flows
	 */
	public FlowAssembler(String flowId, FlowBuilder flowBuilder) {
		this(flowId, null, flowBuilder);
	}

	/**
	 * Create a new flow assembler that will direct Flow assembly using the
	 * specified builder strategy.
	 * @param flowId the assigned flow id
	 * @param flowAttributes externally assigned flow attributes that can affect
	 * flow construction
	 * @param flowBuilder the builder the factory will use to build flows
	 */
	public FlowAssembler(String flowId, AttributeCollection flowAttributes, FlowBuilder flowBuilder) {
		Assert.hasText(flowId, "The flow id is required");
		Assert.notNull(flowBuilder, "The flow builder is required");
		this.flowId = flowId;
		this.flowAttributes = (flowAttributes != null ? flowAttributes.unmodifiable()
				: CollectionUtils.EMPTY_ATTRIBUTE_MAP);
		this.flowBuilder = flowBuilder;
	}

	/**
	 * Returns the identifier to assign to the flow.
	 */
	public String getFlowId() {
		return flowId;
	}

	/**
	 * Returns externally assigned attributes that can be used to affect flow
	 * construction.
	 */
	public UnmodifiableAttributeMap getFlowAttributes() {
		return flowAttributes;
	}

	/**
	 * Returns the flow builder strategy used to construct the flow from its
	 * component parts.
	 */
	public FlowBuilder getFlowBuilder() {
		return flowBuilder;
	}

	/**
	 * Assembles the flow, directing the construction process by delegating to
	 * the configured FlowBuilder. While the assembly process is ongoing the
	 * "assembling" flag is set to true.
	 */
	public void assembleFlow() {
		flowBuilder.init(flowId, flowAttributes);
		flowBuilder.buildVariables();
		flowBuilder.buildStartActions();
		flowBuilder.buildInputMapper();
		flowBuilder.buildInlineFlows();
		flowBuilder.buildStates();
		flowBuilder.buildGlobalTransitions();
		flowBuilder.buildEndActions();
		flowBuilder.buildOutputMapper();
		flowBuilder.buildExceptionHandlers();
		flowBuilder.dispose();
	}
}