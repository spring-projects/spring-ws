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
 * A <code>EndpointInterceptor</code> gets called before the appropriate {@link EndpointAdapter} triggers the invocation
 * of the endpoint itself. This mechanism can be used for a large field of preprocessing aspects, e.g. for authorization
 * checks, or message header checks. Its main purpose is to allow for factoring out repetitive endpoint code.
 * <p/>
 * Typically an interceptor chain is defined per {@link EndpointMapping} bean, sharing its granularity. To be able to
 * apply a certain interceptor chain to a group of handlers, one needs to map the desired handlers via one
 * <code>EndpointMapping</code> bean.The interceptors themselves are defined as beans in the application context,
 * referenced by the mapping bean definition via its <code>interceptors</code> property (in XML: a &lt;list&gt; of
 * &lt;ref&gt;).
 *
 * @author Arjen Poutsma
 * @see EndpointInvocationChain#getInterceptors()
 * @see org.springframework.ws.server.endpoint.interceptor.EndpointInterceptorAdapter
 * @see org.springframework.ws.server.endpoint.mapping.AbstractEndpointMapping#setInterceptors(EndpointInterceptor[])
 */
public interface EndpointInterceptor {

    /**
     * Processes the incoming request message. Called after {@link EndpointMapping} determined an appropriate endpoint
     * object, but before {@link EndpointAdapter} invokes the endpoint.
     * <p/>
     * {@link MessageDispatcher} processes an endpoint in an invocation chain, consisting of any number of interceptors,
     * with the endpoint itself at the end. With this method, each interceptor can decide to abort the chain, typically
     * creating a custom response.
     *
     * @param messageContext contains the incoming request message
     * @param endpoint       chosen endpoint to invoke
     * @return <code>true</code> to continue processing of the request interceptor chain; <code>false</code> to indicate
     *         blocking of the request endpoint chain, <em>without invoking the endpoint</em>
     * @throws Exception in case of errors
     * @see org.springframework.ws.context.MessageContext#getRequest()
     */
    boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception;

    /**
     * Processes the outgoing response message. Called after {@link EndpointAdapter} actually invoked the endpoint. Can
     * manipulate the response, if any, by adding new headers, etc.
     * <p/>
     * {@link MessageDispatcher} processes an endpoint in an invocation chain, consisting of any number of interceptors,
     * with the endpoint itself at the end. With this method, each interceptor can post-process an invocation, getting
     * applied in inverse order of the execution chain.
     * <p/>
     * Note: Will only be called if this interceptor's {@link #handleRequest}  method has successfully completed.
     *
     * @param messageContext contains both request and response messages
     * @param endpoint       chosen endpoint to invoke
     * @return <code>true</code> to continue processing of the reponse interceptor chain; <code>false</code> to indicate
     *         blocking of the response endpoint chain.
     * @throws Exception in case of errors
     * @see org.springframework.ws.context.MessageContext#getRequest()
     * @see org.springframework.ws.context.MessageContext#hasResponse()
     * @see org.springframework.ws.context.MessageContext#getResponse()
     */
    boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception;

    /**
     * Processes the outgoing response fault. Called after {@link EndpointAdapter} actually invoked the endpoint. Can
     * manipulate the response, if any, by adding new headers, etc.
     * <p/>
     * {@link MessageDispatcher} processes an endpoint in an invocation chain, consisting of any number of interceptors,
     * with the endpoint itself at the end. With this method, each interceptor can post-process an invocation, getting
     * applied in inverse order of the execution chain.
     * <p/>
     * Note: Will only be called if this interceptor's {@link #handleRequest}  method has successfully completed.
     *
     * @param messageContext contains both request and response messages, the response should contains a Fault
     * @param endpoint       chosen endpoint to invoke
     * @return <code>true</code> to continue processing of the reponse interceptor chain; <code>false</code> to indicate
     *         blocking of the response handler chain.
     */
    boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception;
}
