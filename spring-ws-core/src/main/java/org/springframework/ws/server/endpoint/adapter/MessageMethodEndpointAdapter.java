/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.server.endpoint.adapter;

import java.lang.reflect.Method;

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.MessageDispatcher;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.soap.server.SoapMessageDispatcher;

/**
 * Adapter that supports endpoint methods with message contexts. Supports methods with the following signature:
 * 
 * <pre>
 * void handleMyMessage(MessageContext request);
 * </pre>
 * 
 * I.e. methods that take a single {@link MessageContext} parameter, and return {@code void}. The method can have any
 * name, as long as it is mapped by an {@link org.springframework.ws.server.EndpointMapping}.
 * <p>
 * This adapter is registered by default by the {@link MessageDispatcher} and {@link SoapMessageDispatcher}.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 * @deprecated as of Spring Web Services 2.0, in favor of {@link DefaultMethodEndpointAdapter} and
 *             {@link org.springframework.ws.server.endpoint.adapter.method.MessageContextMethodArgumentResolver
 *             MessageContextMethodArgumentResolver}.
 */
@Deprecated
public class MessageMethodEndpointAdapter extends AbstractMethodEndpointAdapter {

	@Override
	protected boolean supportsInternal(MethodEndpoint methodEndpoint) {
		Method method = methodEndpoint.getMethod();
		return Void.TYPE.isAssignableFrom(method.getReturnType()) && method.getParameterTypes().length == 1
				&& MessageContext.class.isAssignableFrom(method.getParameterTypes()[0]);
	}

	@Override
	protected void invokeInternal(MessageContext messageContext, MethodEndpoint methodEndpoint) throws Exception {
		methodEndpoint.invoke(messageContext);
	}

}
