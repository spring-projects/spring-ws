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
package org.springframework.webflow.action;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.binding.method.MethodSignature;
import org.springframework.util.Assert;
import org.springframework.webflow.RequestContext;

/**
 * Thin action proxy that delegates to an arbitrary bean managed in a Spring
 * bean factory. The bean does not have to implement any special interface to be
 * invoked.
 * <p>
 * To use this class, you configure the name of the bean you wish to invoke and
 * what method on that bean should be invoked with what arguments, typically
 * using flow request context attributes.
 * <p>
 * Example configuration and usage:
 * 
 * <pre>
 *     MethodSignature method = new MethodSignature("myMethod");
 *     BeanFactory factory = ...
 *     BeanFactoryInvokingAction action = new BeanFactoryBeanInvokingAction(method, "myBean", factory);
 *     MockRequestContext context = new MockRequestContext();
 *     action.execute(context);
 * </pre>
 * 
 * @author Keith Donald
 */
public class BeanFactoryBeanInvokingAction extends AbstractBeanInvokingAction {

	/**
	 * The name of the bean in the factory to invoke. Required.
	 */
	private String beanName;

	/**
	 * The bean factory managing the bean to invoke. Required.
	 */
	private BeanFactory beanFactory;

	/**
	 * Creates a bean factory bean invoking action.
	 * @param methodSignature the signature of the method on the bean to invoke
	 * @param beanName the id of the bean in the bean factory
	 * @param beanFactory the bean factory
	 */
	public BeanFactoryBeanInvokingAction(MethodSignature methodSignature, String beanName, BeanFactory beanFactory) {
		super(methodSignature);
		Assert.hasText(beanName, "The bean name is required");
		Assert.notNull(beanFactory, "The bean factory is required");
		this.beanName = beanName;
		this.beanFactory = beanFactory;
	}

	/**
	 * Returns the configured bean name.
	 */
	public String getBeanName() {
		return beanName;
	}

	/**
	 * Returns the configured bean factory member.
	 */
	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	/**
	 * Looks up the bean to invoke. This implementation loads the bean by name
	 * from the BeanFactory.
	 */
	protected Object getBean(RequestContext context) {
		return getBeanFactory().getBean(getBeanName());
	}
}