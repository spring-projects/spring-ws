/*
 * Copyright 2005-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.soap.addressing.server;

import java.lang.reflect.Method;
import java.net.URI;

import org.springframework.aop.support.AopUtils;
import org.springframework.util.Assert;
import org.springframework.ws.server.endpoint.MethodEndpoint;

/**
 * Abstract base class for WS-Addressing {@code Action}-mapped {@link org.springframework.ws.server.EndpointMapping}
 * implementations that map to {@link MethodEndpoint}s. Provides infrastructure for mapping endpoint methods to
 * actions.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public abstract class AbstractActionMethodEndpointMapping extends AbstractActionEndpointMapping {

	/**
	 * Helper method that registers the methods of the given bean. This method iterates over the methods of the bean,
	 * and calls {@link #getActionForMethod(java.lang.reflect.Method)} for each. If this returns a URI, the method is
	 * registered using {@link #registerEndpoint(java.net.URI, Object)}.
	 *
	 * @see #getActionForMethod (java.lang.reflect.Method)
	 */
	protected void registerMethods(Object endpoint) {
		Assert.notNull(endpoint, "'endpoint' must not be null");
		Method[] methods = AopUtils.getTargetClass(endpoint).getMethods();
		for (Method method : methods) {
			if (method.isSynthetic() || method.getDeclaringClass().equals(Object.class)) {
				continue;
			}
			URI action = getActionForMethod(method);
			if (action != null) {
				registerEndpoint(action, new MethodEndpoint(endpoint, method));
			}
		}
	}

	/** Returns the action value for the specified method. */
	protected abstract URI getActionForMethod(Method method);

	/**
	 * Return the class or interface to use for method reflection.
	 *
	 * <p>Default implementation delegates to {@link AopUtils#getTargetClass(Object)}.
	 *
	 * @param endpoint the bean instance (might be an AOP proxy)
	 * @return the bean class to expose
	 */
	protected Class<?> getEndpointClass(Object endpoint) {
		return AopUtils.getTargetClass(endpoint);
	}

}
