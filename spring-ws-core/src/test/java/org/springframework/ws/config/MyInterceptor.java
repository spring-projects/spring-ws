/*
 * Copyright 2005-2014 the original author or authors.
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

package org.springframework.ws.config;

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;

/**
 * @author Arjen Poutsma
 */
public class MyInterceptor implements EndpointInterceptor {

	private int order;

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
		return true;
	}

	@Override
	public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
		return true;
	}

	@Override
	public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
		return true;
	}

	@Override
	public void afterCompletion(MessageContext messageContext, Object endpoint, Exception ex) {
	}
}
