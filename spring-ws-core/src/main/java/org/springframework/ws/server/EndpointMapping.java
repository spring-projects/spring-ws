/*
 * Copyright 2005 the original author or authors.
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

package org.springframework.ws.server;

import org.springframework.ws.context.MessageContext;

/**
 * Defines a mapping between message requests and endpoint objects.
 *
 * <p>This class can be implemented by application developers, although this is not always necessary, as
 * {@code PayloadRootQNameEndpointMapping} and {@code SoapActionEndpointMapping} are included.
 *
 * <p>HandlerMapping implementations can support mapped interceptors but do not have to. An endpoint will always be wrapped
 * in a {@code EndpointExecutionChain} instance, optionally accompanied by some {@code EndpointInterceptor}
 * instances. The {@code MessageDispacher} will first call each {@code EndpointInterceptor}'s
 * {@code handlerRequest} method in the given order, finally invoking the endpoint itself if all
 * {@code handlerRequest} methods have returned {@code true}.
 *
 * @author Arjen Poutsma
 * @see org.springframework.ws.server.endpoint.mapping.AbstractEndpointMapping
 * @see org.springframework.ws.server.endpoint.mapping.PayloadRootQNameEndpointMapping
 * @see org.springframework.ws.soap.server.endpoint.mapping.SoapActionEndpointMapping
 * @since 1.0.0
 */
public interface EndpointMapping {

	/**
	 * Returns an endpoint and any interceptors for this message context. The choice may be made on message contents,
	 * transport request url, a routing table, or any factor the implementing class chooses.
	 *
	 * <p>The returned {@code EndpointExecutionChain} contains an endpoint Object, rather than even a tag interface,
	 * so that endpoints are not constrained in any way. For example, a {@code EndpointAdapter} could be written to
	 * allow another framework's endpoint objects to be used.
	 *
	 * <p>Returns {@code null} if no match was found. This is by design. The {@code MessageDispatcher} will query
	 * all registered {@code EndpointMapping} beans to find a match, and only decide there is an error if none can
	 * find an endpoint.
	 *
	 * @return a HandlerExecutionChain instance containing endpoint object and any interceptors, or {@code null} if
	 *		   no mapping is found
	 * @throws Exception if there is an internal error
	 */
	EndpointInvocationChain getEndpoint(MessageContext messageContext) throws Exception;

}
