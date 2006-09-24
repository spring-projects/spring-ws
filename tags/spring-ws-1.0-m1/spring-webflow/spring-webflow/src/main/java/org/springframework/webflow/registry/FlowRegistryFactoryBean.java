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

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A factory bean that produces a populated flow registry using a configured
 * list of {@link FlowRegistrar} objects.
 * <p>
 * This class is also <code>BeanFactoryAware</code> and when used with Spring
 * will automatically create a configured
 * {@link DefaultFlowServiceLocator} for loading Flow artifacts like
 * Actions from the Spring bean factory during the Flow registration process.
 * <p>
 * Usage example:
 * 
 * <pre>
 *     &lt;bean id=&quot;flowLocator&quot; class=&quot;org.springframework.webflow.registry.FlowRegistryFactoryBean&quot;&gt;
 *         &lt;property name=&quot;flowRegistrars&quot;&gt;
 *             &lt;list&gt;
 *                 &lt;bean class=&quot;example.MyFlowRegistrar&quot;/&gt;
 *             &lt;/list&gt;
 *         &lt;/property&gt;
 *     &lt;/bean&gt;
 * </pre>
 * 
 * @author Keith Donald
 */
public class FlowRegistryFactoryBean extends AbstractFlowRegistryFactoryBean {

	/**
	 * The flow registrars that will perform the definition registrations.
	 */
	private List flowRegistrars;

	/**
	 * Sets the list of flow registrars to contain only the single flow
	 * registrar provided. Convenience setter for when registry population is
	 * driven by a single registrar.
	 * @param flowRegistrar the flow registrar
	 */
	public void setFlowRegistrar(FlowRegistrar flowRegistrar) {
		flowRegistrars = Collections.singletonList(flowRegistrar);
	}

	/**
	 * Sets the list of flow registrars that will register flow definitions.
	 * @param flowRegistrars the flow registrars
	 */
	public void setFlowRegistrars(FlowRegistrar[] flowRegistrars) {
		this.flowRegistrars = Arrays.asList(flowRegistrars);
	}

	protected void doPopulate(FlowRegistry registry) {
		if (flowRegistrars != null) {
			Iterator it = flowRegistrars.iterator();
			while (it.hasNext()) {
				FlowRegistrar registrar = (FlowRegistrar)it.next();
				registrar.registerFlows(registry, getFlowServiceLocator());
			}
		}
	}
}