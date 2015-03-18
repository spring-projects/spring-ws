/*
 * Copyright 2005-2014 the original author or authors.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;

/**
 * Default implementation of the {@code EndpointInterceptor} interface, for simplified implementation of
 * pre-only/post-only interceptors.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public class EndpointInterceptorAdapter implements EndpointInterceptor {

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	/** Returns {@code false}. */
	public boolean understands(Element header) {
		return false;
	}

	/**
	 * Returns {@code true}.
	 *
	 * @return {@code true}
	 */
	@Override
	public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
		return true;
	}

	/**
	 * Returns {@code true}.
	 *
	 * @return {@code true}
	 */
	@Override
	public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
		return true;
	}

	/**
	 * Returns {@code true}.
	 *
	 * @return {@code true}
	 */
	@Override
	public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
		return true;
	}

	/**
	 * Does nothing by default.
	 */
	@Override
	public void afterCompletion(MessageContext messageContext, Object endpoint, Exception ex) throws Exception {
	}
}
