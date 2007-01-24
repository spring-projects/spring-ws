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
 * Workflow interface that allows for customized endpoint invocation chains. Applications can register any number of
 * existing or custom interceptors for certain groups of endpoints, to add common preprocessing behavior without needing
 * to modify each endpoint implementation.
 * <p/>
 * A <code>EndpointInterceptor</code> gets called before the appropriate <code>EndpointAdapter</code> triggers the
 * invocation of the endpoint itself. This mechanism can be used for a large field of preprocessing aspects, e.g. for
 * authorization checks, or message header checks. Its main purpose is to allow for factoring out repetitive endpoint
 * code.
 * <p/>
 * Typically an interceptor chain is defined per <code>EndpointMapping</code> bean, sharing its granularity. To be able
 * to apply a certain interceptor chain to a group of handlers, one needs to map the desired handlers via one
 * <code>EndpointMapping</code> bean.
 *
 * @author Arjen Poutsma
 * @see EndpointInvocationChain#getInterceptors()
 * @see org.springframework.ws.server.endpoint.interceptor.EndpointInterceptorAdapter
 * @see org.springframework.ws.server.endpoint.mapping.AbstractEndpointMapping#setInterceptors(EndpointInterceptor[])
 */
public interface EndpointInterceptor {

    /**
     * Processes the incoming request message.
     *
     * @param messageContext contains the incoming request message
     * @param endpoint       chosen endpoint to invoke
     * @return <code>true</code> to continue processing of the request interceptor chain; <code>false</code> to indicate
     *         blocking of the request handler chain, <em>without invoking the endpoint</em>
     */
    boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception;

    /**
     * Processes the outgoing response message.
     *
     * @param messageContext contains both request and response messages
     * @param endpoint       chosen endpoint to invoke
     * @return <code>true</code> to continue processing of the reponse interceptor chain; <code>false</code> to indicate
     *         blocking of the response handler chain.
     */
    boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception;

}
