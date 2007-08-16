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

package org.springframework.ws.soap.server;

import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.EndpointInvocationChain;

/**
 * SOAP-specific subclass of the <code>EndpointInvocationChain</code>. Adds associated actors (SOAP 1.1) or roles (SOAP
 * 1.2). Used by the <code>SoapMessageDispatcher</code> to determine the MustUnderstand headers for particular
 * endpoint.
 *
 * @author Arjen Poutsma
 * @see #getActorsOrRoles()
 * @see SoapMessageDispatcher
 * @since 1.0
 */
public class SoapEndpointInvocationChain extends EndpointInvocationChain {

    private String[] actorsOrRoles;

    private boolean isUltimateReceiver = true;

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
     * @param endpoint           the endpoint object to invoke
     * @param interceptors       the array of interceptors to apply
     * @param actorsOrRoles      the array of actorsOrRoles to set
     * @param isUltimateReceiver whether this chain fullfils the SOAP 1.2 Ultimate receiver role
     */
    public SoapEndpointInvocationChain(Object endpoint,
                                       EndpointInterceptor[] interceptors,
                                       String[] actorsOrRoles,
                                       boolean isUltimateReceiver) {
        super(endpoint, interceptors);
        this.actorsOrRoles = actorsOrRoles;
        this.isUltimateReceiver = isUltimateReceiver;
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

    /** Indicates whether this chain fulfills the SOAP 1.2 Ultimate Receiver role. Default is <code>true</code>. */
    public boolean isUltimateReceiver() {
        return isUltimateReceiver;
    }
}
