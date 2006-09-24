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
package org.springframework.webflow.execution.repository.support;

import org.springframework.util.Assert;
import org.springframework.webflow.execution.FlowExecutionListenerLoader;
import org.springframework.webflow.execution.FlowLocator;
import org.springframework.webflow.execution.StaticFlowExecutionListenerLoader;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.util.RandomGuidUidGenerator;

/**
 * A holder for services common to all {@link FlowExecutionRepository}
 * implementations.  Allows for customization of each of the services 
 * to affect FlowExecutionRepository behavior.
 * <p>
 * <b>Configurable properties</b> <br>
 * <table border="1">
 * <tr>
 * <td><b>name</b></td>
 * <td><b>description</b></td>
 * <td><b>default</b></td>
 * </tr>
 * <tr>
 * <td>flowLocator (required)</td>
 * <td>The locator that will load flow definitions as needed for execution</td>
 * <td>None</td>
 * </tr>
 * <tr>
 * <td>listenerLoader</td>
 * <td>The listeners that should be loaded to observe the lifecycle of managed
 * flow executions</td>
 * <td>An empty listener loader</td>
 * </tr>
 * <tr>
 * <td>uidGenerator</td>
 * <td>The strategy to generate unique repository identifiers managed flow
 * executions.</td>
 * <td>{@link RandomGuidUidGenerator A random GUID generator}</td>
 * </tr>
 * </table>
 * @author Keith Donald
 */
public class FlowExecutionRepositoryServices {

	/**
	 * The flow locator strategy for retrieving a flow definition using a flow
	 * id provided by the client.
	 */
	private FlowLocator flowLocator;

	/**
	 * A set of flow execution listeners to a list of flow execution listener
	 * criteria objects. The criteria list determines the conditions in which a
	 * single flow execution listener applies.
	 */
	private FlowExecutionListenerLoader listenerLoader = new StaticFlowExecutionListenerLoader();

	/**
	 * Creates a new flow execution repository service holder.
	 * @param flowLocator the flow locator (required)
	 */
	public FlowExecutionRepositoryServices(FlowLocator flowLocator) {
		Assert.notNull(flowLocator, "The flow locator to load flow definitions to execute is required");
		this.flowLocator = flowLocator;
	}

	/**
	 * Returns the flow locator to use for lookup of flow definitions to
	 * execute.
	 */
	public FlowLocator getFlowLocator() {
		return flowLocator;
	}

	/**
	 * Returns the flow execution listener loader.
	 */
	public FlowExecutionListenerLoader getListenerLoader() {
		return listenerLoader;
	}

	/**
	 * Sets the flow execution listener loader.
	 */
	public void setListenerLoader(FlowExecutionListenerLoader listenerLoader) {
		Assert.notNull(listenerLoader, "The flow execution listener loader is required");
		this.listenerLoader = listenerLoader;
	}
}