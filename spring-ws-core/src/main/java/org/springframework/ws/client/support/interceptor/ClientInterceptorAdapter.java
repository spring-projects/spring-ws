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

package org.springframework.ws.client.support.interceptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.context.MessageContext;

/**
 * Default implementation of the {@code ClientInterceptor} interface, for simplified
 * implementation of pre-only/post-only interceptors.
 *
 * @author Marten Deinum
 * @since 2.2.5
 */
public abstract class ClientInterceptorAdapter implements ClientInterceptor {

	/**
	 * Logger available to subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	@Override
	public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {
		return true;
	}

	@Override
	public boolean handleResponse(MessageContext messageContext) throws WebServiceClientException {
		return true;
	}

	@Override
	public boolean handleFault(MessageContext messageContext) throws WebServiceClientException {
		return true;
	}

	/**
	 * Does nothing by default.
	 */
	@Override
	public void afterCompletion(MessageContext messageContext, @Nullable Exception ex)
			throws WebServiceClientException {

	}

}
