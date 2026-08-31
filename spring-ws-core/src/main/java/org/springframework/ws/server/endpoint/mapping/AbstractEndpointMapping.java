/*
 * Copyright 2005-present the original author or authors.
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

package org.springframework.ws.server.endpoint.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.EndpointInvocationChain;
import org.springframework.ws.server.EndpointMapping;
import org.springframework.ws.server.SmartEndpointInterceptor;

/**
 * Abstract base class for EndpointMapping implementations. Supports a default endpoint,
 * and endpoint interceptors.
 * <p>
 * Interceptors configured through {@link #setInterceptors(EndpointInterceptor[])} run
 * first, in the order they have been configured, followed by any auto-discovered
 * {@link SmartEndpointInterceptor} beans sorted using
 * {@link AnnotationAwareOrderComparator}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @see #getEndpointInternal(org.springframework.ws.context.MessageContext)
 * @see org.springframework.ws.server.EndpointInterceptor
 */
public abstract class AbstractEndpointMapping extends ApplicationObjectSupport implements EndpointMapping, Ordered {

	/**
	 * Name of the {@link MessageContext#getProperty(String) message context property}
	 * that holds the key that this mapping used to look up the endpoint for the current
	 * request.
	 * <p>
	 * The property is only set if the lookup succeeded, so that consumers such as
	 * observability instrumentation can safely derive metadata from a key that is known
	 * to match the service contract. The type of the value depends on the mapping
	 * implementation; it is a {@link javax.xml.namespace.QName} for payload root based
	 * mappings and a {@code String} for SOAP action or WS-Addressing based mappings.
	 * @since 5.1.0
	 */
	public static final String LOOKUP_KEY_PROPERTY = AbstractEndpointMapping.class.getName() + ".lookupKey";

	private int order = Integer.MAX_VALUE; // default: same as non-Ordered

	private @Nullable Object defaultEndpoint;

	private EndpointInterceptor @Nullable [] interceptors;

	private SmartEndpointInterceptor @Nullable [] smartInterceptors;

	/**
	 * Returns the endpoint interceptors to apply to all endpoints mapped by this endpoint
	 * mapping.
	 * @return array of endpoint interceptors, or {@code null} if none
	 */
	public EndpointInterceptor @Nullable [] getInterceptors() {
		return this.interceptors;
	}

	/**
	 * Sets the endpoint interceptors to apply to all endpoints mapped by this endpoint
	 * mapping.
	 * @param interceptors array of endpoint interceptors, or {@code null} if none
	 */
	public final void setInterceptors(EndpointInterceptor[] interceptors) {
		this.interceptors = interceptors;
	}

	@Override
	public final int getOrder() {
		return this.order;
	}

	/**
	 * Specify the order value for this mapping.
	 * <p>
	 * Default value is {@link Integer#MAX_VALUE}, meaning that it's non-ordered.
	 * @see org.springframework.core.Ordered#getOrder()
	 */
	public final void setOrder(int order) {
		this.order = order;
	}

	/**
	 * Initializes the interceptors.
	 * @see #initInterceptors()
	 */
	@Override
	protected void initApplicationContext() throws BeansException {
		initInterceptors();
	}

	/**
	 * Initialize the specified interceptors, adapting them where necessary.
	 * <p>
	 * Auto-discovered {@link SmartEndpointInterceptor} beans are sorted using
	 * {@link AnnotationAwareOrderComparator}.
	 * @see #setInterceptors
	 */
	protected void initInterceptors() {
		Map<String, SmartEndpointInterceptor> smartInterceptors = BeanFactoryUtils
			.beansOfTypeIncludingAncestors(obtainApplicationContext(), SmartEndpointInterceptor.class, true, false);
		if (!smartInterceptors.isEmpty()) {
			List<SmartEndpointInterceptor> orderedInterceptors = new ArrayList<>(smartInterceptors.values());
			AnnotationAwareOrderComparator.sort(orderedInterceptors);
			this.smartInterceptors = orderedInterceptors.toArray(new SmartEndpointInterceptor[0]);
		}
	}

	/**
	 * Look up an endpoint for the given message context, falling back to the default
	 * endpoint if no specific one is found.
	 * @return the looked up endpoint instance, or the default endpoint
	 * @see #getEndpointInternal(org.springframework.ws.context.MessageContext)
	 */
	@Override
	public final @Nullable EndpointInvocationChain getEndpoint(MessageContext messageContext) throws Exception {
		Object endpoint = resoleEndpoint(messageContext);
		if (endpoint == null) {
			return null;
		}
		List<EndpointInterceptor> interceptors = new ArrayList<>();
		if (this.interceptors != null) {
			interceptors.addAll(Arrays.stream(this.interceptors)
				.filter(interceptor -> shouldIntercept(interceptor, messageContext, endpoint))
				.toList());
		}
		if (this.smartInterceptors != null) {
			interceptors.addAll(Arrays.stream(this.smartInterceptors)
				.filter(interceptor -> shouldIntercept(interceptor, messageContext, endpoint))
				.toList());
		}
		return createEndpointInvocationChain(messageContext, endpoint,
				interceptors.toArray(new EndpointInterceptor[0]));
	}

	private @Nullable Object resoleEndpoint(MessageContext messageContext) throws Exception {
		Object endpoint = getEndpointInternal(messageContext);
		if (endpoint == null) {
			endpoint = this.defaultEndpoint;
		}
		if (endpoint == null) {
			return null;
		}
		if (endpoint instanceof String endpointName) {
			endpoint = resolveStringEndpoint(endpointName);
		}
		return endpoint;
	}

	private boolean shouldIntercept(EndpointInterceptor interceptor, MessageContext messageContext, Object endpoint) {
		if (interceptor instanceof SmartEndpointInterceptor smartEndpointInterceptor) {
			return smartEndpointInterceptor.shouldIntercept(messageContext, endpoint);
		}
		return true;
	}

	/**
	 * Creates a new {@code EndpointInvocationChain} based on the given message context,
	 * endpoint, and interceptors. Default implementation creates a simple
	 * {@code EndpointInvocationChain} based on the set interceptors.
	 * @param endpoint the endpoint
	 * @param interceptors the endpoint interceptors
	 * @return the created invocation chain
	 * @see #setInterceptors(org.springframework.ws.server.EndpointInterceptor[])
	 */
	protected EndpointInvocationChain createEndpointInvocationChain(MessageContext messageContext, Object endpoint,
			EndpointInterceptor[] interceptors) {
		return new EndpointInvocationChain(endpoint, interceptors);
	}

	/**
	 * Returns the default endpoint for this endpoint mapping.
	 * @return the default endpoint mapping, or null if none
	 */
	protected final @Nullable Object getDefaultEndpoint() {
		return this.defaultEndpoint;
	}

	/**
	 * Sets the default endpoint for this endpoint mapping. This endpoint will be returned
	 * if no specific mapping was found.
	 * <p>
	 * Default is {@code null}, indicating no default endpoint.
	 * @param defaultEndpoint the default endpoint, or null if none
	 */
	public final void setDefaultEndpoint(Object defaultEndpoint) {
		this.defaultEndpoint = defaultEndpoint;
	}

	/**
	 * Resolves an endpoint string. If the given string can is a bean name, it is resolved
	 * using the application context.
	 * @param endpointName the endpoint name
	 * @return the resolved endpoint, or {@code null} if the name could not be resolved
	 */
	protected @Nullable Object resolveStringEndpoint(String endpointName) {
		ApplicationContext applicationContext = obtainApplicationContext();
		if (applicationContext.containsBean(endpointName)) {
			return applicationContext.getBean(endpointName);
		}
		else {
			return null;
		}
	}

	/**
	 * Lookup an endpoint for the given request, returning {@code null} if no specific one
	 * is found. This template method is called by getEndpoint, a {@code null} return
	 * value will lead to the default handler, if one is set.
	 * <p>
	 * The returned endpoint can be a string, in which case it is resolved as a bean name.
	 * Also, it can take the form {@code beanName#method}, in which case the method is
	 * resolved.
	 * @return the looked up endpoint instance, or null
	 * @throws Exception if there is an error
	 */
	protected abstract @Nullable Object getEndpointInternal(MessageContext messageContext) throws Exception;

}
