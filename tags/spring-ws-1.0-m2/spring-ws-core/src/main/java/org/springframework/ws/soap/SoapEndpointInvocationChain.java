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

package org.springframework.ws.soap;

import org.springframework.ws.EndpointInterceptor;
import org.springframework.ws.EndpointInvocationChain;

/**
 * SOAP-specific subclass of the <code>EndpointInvocationChain</code>. Adds associated actors (SOAP 1.1) or roles (SOAP
 * 1.2). Used by the <code>SoapMessageDispatcher</code> to determine the MustUnderstand headers for particular
 * endpoint.
 *
 * @author Arjen Poutsma
 * @see #getActorsOrRoles()
 * @see SoapMessageDispatcher
 */
public class SoapEndpointInvocationChain extends EndpointInvocationChain {

    private String[] actorsOrRoles;

    /**
     * Create new <code>SoapEndpointInvocationChain</code>.
     *
     * @param endpoint the endpoint object to invoke
     */
    public SoapEndpointInvocationChain(Object endpoint) {
        super(endpoint);
    }

    /**
     * Create new <code>SoapEndpointInvocationChain</code>.
     *
     * @param endpoint     the endpoint object to invoke
     * @param interceptors the array of interceptors to apply
     */
    public SoapEndpointInvocationChain(Object endpoint, EndpointInterceptor[] interceptors) {
        super(endpoint, interceptors);
    }

    /**
     * Create new <code>EndpointInvocationChain</code>.
     *
     * @param endpoint      the endpoint object to invoke
     * @param interceptors  the array of interceptors to apply
     * @param actorsOrRoles the array of actorsOrRoles to set
     */
    public SoapEndpointInvocationChain(Object endpoint, EndpointInterceptor[] interceptors, String[] actorsOrRoles) {
        super(endpoint, interceptors);
        this.actorsOrRoles = actorsOrRoles;
    }

    /**
     * Gets the actors (SOAP 1.1) or roles (SOAP 1.2) associated with an invocation of this chain and its contained
     * interceptors and endpoint.
     *
     * @return a string array of URIs for SOAP actors/roles
     */
    public String[] getActorsOrRoles() {
        return actorsOrRoles;
    }
}
