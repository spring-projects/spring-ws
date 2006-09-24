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

package org.springframework.ws;

/**
 * Endpoint invocation chain, consisting of an endpoint object and any preprocessing interceptors.
 *
 * @author Arjen Poutsma
 * @see EndpointInterceptor
 */
public class EndpointInvocationChain {

    private Object endpoint;

    private EndpointInterceptor[] interceptors;

    /**
     * Create new <code>EndpointInvocationChain</code>.
     *
     * @param endpoint the endpoint object to invoke
     */
    public EndpointInvocationChain(Object endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Create new <code>EndpointInvocationChain</code>.
     *
     * @param endpoint     the endpoint object to invoke
     * @param interceptors the array of interceptors to apply
     */
    public EndpointInvocationChain(Object endpoint, EndpointInterceptor[] interceptors) {
        this.endpoint = endpoint;
        this.interceptors = interceptors;
    }

    /**
     * Returns the endpoint object to invoke.
     *
     * @return the endpoint object
     */
    public Object getEndpoint() {
        return endpoint;
    }

    /**
     * Returns the array of interceptors to apply before the handler executes.
     *
     * @return the array of interceptors
     */
    public EndpointInterceptor[] getInterceptors() {
        return interceptors;
    }

}
