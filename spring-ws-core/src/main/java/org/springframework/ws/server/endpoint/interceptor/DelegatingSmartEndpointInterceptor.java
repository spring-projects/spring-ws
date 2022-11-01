/*
 * Copyright 2005-2022 the original author or authors.
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

package org.springframework.ws.server.endpoint.interceptor;

import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.SmartEndpointInterceptor;

/**
 * Implementation of the {@link SmartEndpointInterceptor} interface that delegates to a delegate
 * {@link EndpointInterceptor}.
 *
 * @author Arjen Poutsma
 * @since 2.0
 */
public class DelegatingSmartEndpointInterceptor implements SmartEndpointInterceptor {

	private final EndpointInterceptor delegate;

	/**
	 * Creates a new instance of the {@code DelegatingSmartEndpointInterceptor} with the given delegate.
	 *
	 * @param delegate the endpoint interceptor to delegate to.
	 */
	public DelegatingSmartEndpointInterceptor(EndpointInterceptor delegate) {
		Assert.notNull(delegate, "'delegate' must not be null");
		this.delegate = delegate;
	}

	/**
	 * Returns the delegate.
	 *
	 * @return the delegate
	 */
	public EndpointInterceptor getDelegate() {
		return delegate;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation delegates to {@link #shouldIntercept(WebServiceMessage, Object)}.
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
	 *
	 * @param request the request message
	 * @param endpoint chosen endpoint to invoke
	 * @return {@code true} to indicate that this interceptor applies; {@code false} otherwise
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
	public void afterCompletion(MessageContext messageContext, Object endpoint, Exception ex) throws Exception {
		getDelegate().afterCompletion(messageContext, endpoint, ex);
	}
}
