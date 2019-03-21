/*
 * Copyright 2005-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.server.endpoint.mapping;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContextException;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MethodEndpoint;

/**
 * Abstract base class for {@link MethodEndpoint} mappings.
 *
 * <p>Subclasses typically implement {@link org.springframework.beans.factory.config.BeanPostProcessor} to look for beans
 * that qualify as endpoint. The methods of this bean are then registered under a specific key with {@link
 * #registerEndpoint(Object, MethodEndpoint)}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public abstract class AbstractMethodEndpointMapping<T> extends AbstractEndpointMapping {

	private final Map<T, MethodEndpoint> endpointMap = new HashMap<T, MethodEndpoint>();

	/**
	 * Lookup an endpoint for the given message. The extraction of the endpoint key is delegated to the concrete
	 * subclass.
	 *
	 * @return the looked up endpoint, or {@code null}
	 * @see #getLookupKeyForMessage(MessageContext)
	 */
	@Override
	protected Object getEndpointInternal(MessageContext messageContext) throws Exception {
		T key = getLookupKeyForMessage(messageContext);
		if (key == null) {
			return null;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Looking up endpoint for [" + key + "]");
		}
		return lookupEndpoint(key);
	}

	/**
	 * Returns the endpoint keys for the given message context.
	 *
	 * @return the registration keys
	 */
	protected abstract T getLookupKeyForMessage(MessageContext messageContext) throws Exception;

	/**
	 * Looks up an endpoint instance for the given keys. All keys are tried in order.
	 *
	 * @param key key the beans are mapped to
	 * @return the associated endpoint instance, or {@code null} if not found
	 */
	protected MethodEndpoint lookupEndpoint(T key) {
		return endpointMap.get(key);
	}

	/**
	 * Register the given endpoint instance under the key.
	 *
	 * @param key	   the lookup key
	 * @param endpoint the method endpoint instance
	 * @throws BeansException if the endpoint could not be registered
	 */
	protected void registerEndpoint(T key, MethodEndpoint endpoint) throws BeansException {
		Object mappedEndpoint = endpointMap.get(key);
		if (mappedEndpoint != null) {
			throw new ApplicationContextException("Cannot map endpoint [" + endpoint + "] on registration key [" + key +
					"]: there's already endpoint [" + mappedEndpoint + "] mapped");
		}
		if (endpoint == null) {
			throw new ApplicationContextException("Could not find endpoint for key [" + key + "]");
		}
		endpointMap.put(key, endpoint);
		if (logger.isDebugEnabled()) {
			logger.debug("Mapped [" + key + "] onto endpoint [" + endpoint + "]");
		}
	}

	/**
	 * Helper method that registers the methods of the given bean. This method iterates over the methods of the bean,
	 * and calls {@link #getLookupKeyForMethod(Method)} for each. If this returns a string, the method is registered
	 * using {@link #registerEndpoint(Object, MethodEndpoint)}.
	 *
	 * @see #getLookupKeyForMethod(Method)
	 */
	protected void registerMethods(final Object endpoint) {
		Assert.notNull(endpoint, "'endpoint' must not be null");
		Class<?> endpointClass = getEndpointClass(endpoint);
		ReflectionUtils.doWithMethods(endpointClass, new ReflectionUtils.MethodCallback() {

			public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
				List<T> keys = getLookupKeysForMethod(method);
				for (T key : keys) {
					registerEndpoint(key, new MethodEndpoint(endpoint, method));
				}
			}
		});
	}

	/**
	 * Helper method that registers the methods of the given class. This method iterates over the methods of the class,
	 * and calls {@link #getLookupKeyForMethod(Method)} for each. If this returns a string, the method is registered
	 * using {@link #registerEndpoint(Object, MethodEndpoint)}.
	 *
	 * @see #getLookupKeyForMethod(Method)
	 * @see #getLookupKeysForMethod(Method)
	 */
	protected void registerMethods(String beanName) {
		Assert.hasText(beanName, "'beanName' must not be empty");
		Class<?> endpointType = getApplicationContext().getType(beanName);
		endpointType = ClassUtils.getUserClass(endpointType);
		
		Set<Method> methods = findEndpointMethods(endpointType, new ReflectionUtils.MethodFilter() {
			public boolean matches(Method method) {
				return !getLookupKeysForMethod(method).isEmpty();
			}
		});

		for (Method method : methods) {
			List<T> keys = getLookupKeysForMethod(method);
			for (T key : keys) {
				registerEndpoint(key, new MethodEndpoint(beanName, getApplicationContext(), method));
			}
		}

	}

	private Set<Method> findEndpointMethods(Class<?> endpointType,
											final ReflectionUtils.MethodFilter endpointMethodFilter) {
		final Set<Method> endpointMethods = new LinkedHashSet<Method>();
		Set<Class<?>> endpointTypes = new LinkedHashSet<Class<?>>();
		Class<?> specificEndpointType = null;
		if (!Proxy.isProxyClass(endpointType)) {
			endpointTypes.add(endpointType);
			specificEndpointType = endpointType;
		}
		endpointTypes.addAll(Arrays.asList(endpointType.getInterfaces()));
		for (Class<?> currentEndpointType : endpointTypes) {
			final Class<?> targetClass = (specificEndpointType != null ? specificEndpointType : currentEndpointType);
			ReflectionUtils.doWithMethods(currentEndpointType, new ReflectionUtils.MethodCallback() {
				public void doWith(Method method) {
					Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
					Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);
					if (endpointMethodFilter.matches(specificMethod) &&
							(bridgedMethod == specificMethod || !endpointMethodFilter.matches(bridgedMethod))) {
						endpointMethods.add(specificMethod);
					}
				}
			}, ReflectionUtils.USER_DECLARED_METHODS);
		}
		return endpointMethods;
	}

	/**
	 * Returns the endpoint key for the given method. Returns {@code null} if the method is not to be
	 * registered, which is the default.
	 *
	 * @param method the method
	 * @return a registration key, or {@code null} if the method is not to be registered
	 * @see #getLookupKeysForMethod(Method)
	 */
	protected T getLookupKeyForMethod(Method method) {
		return null;
	}

	/**
	 * Returns the endpoint keys for the given method. Should return an empty array if the method is not to be
	 * registered. The default delegates to {@link #getLookupKeysForMethod(Method)}.
	 *
	 * @param method the method
	 * @return a list of registration keys
	 * @since 2.2
	 */
	protected List<T> getLookupKeysForMethod(Method method) {
		T key = getLookupKeyForMethod(method);
		return key != null ? Collections.singletonList(key) : Collections.<T>emptyList();
	}

	/**
	 * Return the class or interface to use for method reflection.
	 *
	 * <p>Default implementation delegates to {@link AopUtils#getTargetClass(Object)}.
	 *
	 * @param endpoint the bean instance (might be an AOP proxy)
	 * @return the bean class to expose
	 */
	protected Class<?> getEndpointClass(Object endpoint) {
		if (AopUtils.isJdkDynamicProxy(endpoint)) {
			throw new IllegalArgumentException(ClassUtils.getShortName(getClass()) +
					" does not work with JDK Dynamic Proxies. " +
					"Please use CGLIB proxies, by setting proxy-target-class=\"true\" on the aop:aspectj-autoproxy " +
					"or aop:config element.");
		}
		return AopUtils.getTargetClass(endpoint);
	}
}
