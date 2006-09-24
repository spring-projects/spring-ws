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
package org.springframework.webflow.test;

import org.springframework.core.io.Resource;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.builder.FlowAssembler;
import org.springframework.webflow.builder.FlowBuilder;
import org.springframework.webflow.builder.FlowServiceLocator;
import org.springframework.webflow.registry.ExternalizedFlowDefinition;

/**
 * Base class for flow integration tests that verify an externalized flow
 * definition executes as expected.
 * 
 * @author Keith Donald
 */
public abstract class AbstractExternalizedFlowExecutionTests extends AbstractFlowExecutionTests {

	/**
	 * The cached flow definition
	 */
	private static Flow cachedFlowDefinition;

	/**
	 * The flag indicating if the the flow definition built from an externalized
	 * resource as part of this test should be cached.
	 */
	private boolean cacheFlowDefinition;

	/**
	 * Returns if flow definition caching is turned on.
	 */
	public boolean isCacheFlowDefinition() {
		return cacheFlowDefinition;
	}

	/**
	 * Sets the flag indicating if the the flow definition built from an
	 * externalized resource as part of this test should be cached.
	 */
	public void setCacheFlowDefinition(boolean cacheFlowDefinition) {
		this.cacheFlowDefinition = cacheFlowDefinition;
	}

	protected Flow getFlow() throws FlowArtifactException {
		if (isCacheFlowDefinition() && cachedFlowDefinition != null) {
			return cachedFlowDefinition;
		}
		FlowServiceLocator flowServiceLocator = createFlowServiceLocator();
		ExternalizedFlowDefinition flowDefinition = getFlowDefinition();
		FlowBuilder builder = createFlowBuilder(flowDefinition.getLocation(), flowServiceLocator);
		new FlowAssembler(flowDefinition.getId(), flowDefinition.getAttributes(), builder).assembleFlow();
		Flow flow = builder.getFlow();
		if (isCacheFlowDefinition()) {
			cachedFlowDefinition = flow;
		}
		return flow;
	}

	/**
	 * Returns the flow artifact factory to use during flow definition
	 * construction time for accessing externally managed flow artifacts such as
	 * actions and flows to be used as subflows.
	 * <p>
	 * Subclasses should override to return a specific flow artifact factory
	 * implementation to support their flow execution test scenarios.
	 * 
	 * @return the flow artifact factory
	 */
	protected FlowServiceLocator createFlowServiceLocator() {
		return new MockFlowServiceLocator();
	}

	/**
	 * Create the builder that will build the flow whose execution will be
	 * tested.
	 * @param resource the externalized flow definition resource location
	 * @param flowServiceLocator the flow artifact factory
	 * @return the flow builder
	 */
	protected abstract FlowBuilder createFlowBuilder(Resource resource, FlowServiceLocator flowServiceLocator);

	/**
	 * Returns the definition of the externalized flow needed by this flow
	 * execution test: subclasses must override.
	 * @return the externalize flow definition to test
	 */
	protected abstract ExternalizedFlowDefinition getFlowDefinition();
}