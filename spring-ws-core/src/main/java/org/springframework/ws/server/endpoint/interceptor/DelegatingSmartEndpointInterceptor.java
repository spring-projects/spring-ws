/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.server.endpoint.interceptor;

import org.jspecify.annotations.Nullable;

import org.springframework.core.Ordered;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.SmartEndpointInterceptor;

/**
 * Implementation of the {@link SmartEndpointInterceptor} interface that delegates to a
 * delegate {@link EndpointInterceptor}.
 * <p>
 * If the target {@link EndpointInterceptor} does not implement {@link Ordered}, a default
 * order is applied ({@value #DEFAULT_ORDER}).
 *
 * @author Arjen Poutsma
 * @author Stephane Nicoll
 * @since 2.0
 */
public class DelegatingSmartEndpointInterceptor implements SmartEndpointInterceptor, Ordered {

	/**
	 * Default order for {@link SmartEndpointInterceptor} instances managed by this class
	 * that do not implement {@link Ordered} themselves.
	 * @since 3.1.9
	 */
	public static final int DEFAULT_ORDER = 100;

	private final EndpointInterceptor delegate;

	/**
	 * Creates a new instance of the {@code DelegatingSmartEndpointInterceptor} with the
	 * given delegate.
	 * @param delegate the endpoint interceptor to delegate to.
	 */
	public DelegatingSmartEndpointInterceptor(EndpointInterceptor delegate) {
		Assert.notNull(delegate, "'delegate' must not be null");
		this.delegate = delegate;
	}

	/**
	 * Returns the delegate.
	 * @return the delegate
	 */
	public EndpointInterceptor getDelegate() {
		return this.delegate;
	}

	@Override
	public int getOrder() {
		if (this.delegate instanceof Ordered) {
			return ((Ordered) this.delegate).getOrder();
		}
		return DEFAULT_ORDER;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation delegates to
	 * {@link #shouldIntercept(WebServiceMessage, Object)}.
	 */
	@Override
	public boolean shouldIntercept(MessageContext messageContext, Object endpoint) {
		WebServiceMessage request = messageContext.getRequest();
		return request != null && shouldIntercept(request, endpoint);
	}

	/**
	 * Indicates whether this interceptor should intercept the given request message.
	 * <p>
	 * This implementation always returns {@code true}.
	 * @param request the request message
	 * @param endpoint chosen endpoint to invoke
	 * @return {@code true} to indicate that this interceptor applies; {@code false}
	 * otherwise
	 */
	protected boolean shouldIntercept(WebServiceMessage request, Object endpoint) {
		return true;
	}

	@Override
	public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
		return getDelegate().handleRequest(messageContext, endpoint);
	}

	@Override
	public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
		return getDelegate().handleResponse(messageContext, endpoint);
	}

	@Override
	public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
		return getDelegate().handleFault(messageContext, endpoint);
	}

	@Override
	public void afterCompletion(MessageContext messageContext, Object endpoint, @Nullable Exception ex)
			throws Exception {
		getDelegate().afterCompletion(messageContext, endpoint, ex);
	}

}
