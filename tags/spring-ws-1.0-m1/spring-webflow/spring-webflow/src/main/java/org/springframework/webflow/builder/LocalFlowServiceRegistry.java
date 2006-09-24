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

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.webflow.Flow;

/**
 * Simple value object that holds a reference to a local artifact registry
 * of a flow definition that is in the process of being constructed.
 * @author Keith Donald
 */
class LocalFlowServiceRegistry {

	/**
	 * The flow for which this registry is for (and scoped by).
	 */
	private Flow flow;

	/**
	 * The locations of the registry resource definitions. 
	 */
	private Resource[] resources;

	/**
	 * The local registry holding the artifacts local to the flow.
	 */
	private GenericApplicationContext context;

	/**
	 * Create new registry
	 * @param context the local registry
	 */
	public LocalFlowServiceRegistry(Flow flow, Resource[] resources) {
		this.flow = flow;
		this.resources = resources;
	}

	public Flow getFlow() {
		return flow;
	}

	public Resource[] getResources() {
		return resources;
	}

	public ApplicationContext getContext() {
		return context;
	}

	/**
	 * Initialize this registry of the local flow service locator.
	 * @param localFactory the local flow service locator
	 * @param rootFactory the root service locator
	 */
	public void init(LocalFlowServiceLocator localFactory, FlowServiceLocator rootFactory) {
		BeanFactory parent = null;
		if (localFactory.isEmpty()) {
			try {
				parent = rootFactory.getBeanFactory();
			}
			catch (UnsupportedOperationException e) {

			}
		}
		else {
			parent = localFactory.top().context;
		}
		context = createLocalFlowContext(parent, rootFactory);
		new XmlBeanDefinitionReader(context).loadBeanDefinitions(resources);
		context.refresh();
	}

	private GenericApplicationContext createLocalFlowContext(BeanFactory parent, FlowServiceLocator rootFactory) {
		if (parent instanceof WebApplicationContext) {
			GenericWebApplicationContext context = new GenericWebApplicationContext();
			context.setServletContext(((WebApplicationContext)parent).getServletContext());
			context.setParent((WebApplicationContext)parent);
			context.setResourceLoader(rootFactory.getResourceLoader());
			return context;
		}
		else {
			GenericApplicationContext context = new GenericApplicationContext();
			if (parent instanceof ApplicationContext) {
				context.setParent((ApplicationContext)parent);
			}
			else {
				if (parent != null) {
					context.getBeanFactory().setParentBeanFactory(parent);
				}
			}
			context.setResourceLoader(rootFactory.getResourceLoader());
			return context;
		}
	}
}