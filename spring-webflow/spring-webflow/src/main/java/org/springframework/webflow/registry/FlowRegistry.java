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

import org.springframework.webflow.Flow;
import org.springframework.webflow.execution.FlowLocator;

/**
 * A interface for registering Flow definitions. Extends the FlowRegistryMBean
 * management interface exposing monitoring and management operations. Extends
 * FlowLocator for accessing registered Flow definitions for execution at
 * runtime.
 * <p>
 * Is a <code>FlowLocator</code>, allowing for execution of flow definitions
 * managed in this registry.
 * 
 * @see org.springframework.webflow.execution.FlowLocator
 * @see org.springframework.webflow.executor.FlowExecutorImpl
 * 
 * @author Keith Donald
 */
public interface FlowRegistry extends FlowRegistryMBean, FlowLocator {

	/**
	 * Queries this registry to determine if a specific flow is contained within
	 * it.
	 * 
	 * @param id the flow id
	 * @return true if a flow is contained in this registry with the id provided
	 */
	public boolean containsFlow(String id);

	/**
	 * Return an unmodifiable collection of the flow definitions registered in this registry.
	 * @return the flow collection
	 */
	public Flow[] getFlows();
	
	/**
	 * Register the flow definition in this registry. Registers a "holder", not
	 * the Flow definition itself. This allows the actual Flow definition to be
	 * loaded lazily only when needed, and also rebuilt at runtime when its
	 * underlying resource changes without redeploy.
	 * @param flowHolder a holder holding the flow definition to register
	 */
	public void registerFlow(FlowHolder flowHolder);

}