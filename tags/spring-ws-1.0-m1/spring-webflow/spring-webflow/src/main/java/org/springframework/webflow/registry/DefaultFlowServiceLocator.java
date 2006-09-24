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
package org.springframework.webflow.registry;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.Assert;
import org.springframework.webflow.Flow;
import org.springframework.webflow.FlowArtifactException;
import org.springframework.webflow.builder.BaseFlowServiceLocator;
import org.springframework.webflow.builder.FlowServiceLocator;

/**
 * The default flow service locator implementation that obtains subflow
 * definitions from a dedicated {@link FlowRegistry} and obtains the remaining
 * services from a generic Spring {@link BeanFactory}.
 * 
 * @see FlowRegistry
 * @see FlowServiceLocator#getSubflow(String)
 * 
 * @author Keith Donald
 */
public class DefaultFlowServiceLocator extends BaseFlowServiceLocator {

	/**
	 * The registry for locating subflows.
	 */
	private FlowRegistry subflowRegistry;

	/**
	 * The Spring bean factory that manages configured flow artifacts.
	 */
	private BeanFactory beanFactory;

	/**
	 * Creates a flow artifact factory that retrieves subflows from the provided
	 * registry and additional artifacts from the provided bean factory.
	 * @param subflowRegistry The registry for loading subflows
	 * @param beanFactory The spring bean factory
	 */
	public DefaultFlowServiceLocator(FlowRegistry subflowRegistry, BeanFactory beanFactory) {
		Assert.notNull(subflowRegistry, "The subflow registry is required");
		Assert.notNull(beanFactory, "The bean factory is required");
		this.subflowRegistry = subflowRegistry;
		this.beanFactory = beanFactory;
	}

	protected void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	/**
	 * Returns the flow registry used by this flow artifact factory to manage
	 * subflow definitions.
	 * @return the flow registry
	 */
	public FlowRegistry getSubflowRegistry() {
		return subflowRegistry;
	}

	public Flow getSubflow(String id) throws FlowArtifactException {
		return subflowRegistry.getFlow(id);
	}

	public BeanFactory getBeanFactory() {
		return beanFactory;
	}	
}