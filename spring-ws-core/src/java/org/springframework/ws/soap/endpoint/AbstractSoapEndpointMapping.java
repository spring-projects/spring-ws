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

package org.springframework.ws.soap.endpoint;

import org.springframework.ws.EndpointInvocationChain;
import org.springframework.ws.endpoint.AbstractEndpointMapping;
import org.springframework.ws.soap.SoapEndpointInvocationChain;

/**
 * Abstract base class for SOAP <code>EndpointMapping</code> implementations. Adds support to SOAP actor roles, in
 * addition to the default endpoint, and endpoint interceptors offered by the base
 * <code>AbstractEndpointMapping</code>.
 * <p/>
 * By default, the SOAP actor role is set to the special 'next actor' role, identifying the next application processing
 * a SOAP request as the intended actor.
 *
 * @author Arjen Poutsma
 */
public abstract class AbstractSoapEndpointMapping extends AbstractEndpointMapping {

    private String[] roles = new String[]{SoapEndpointInvocationChain.DEFAULT_ROLE};

    /**
     * Returns the array of SOAP actor roles to apply to all endpoints mapped by this endpoint mapping. By default, this
     * is set to the 'next actor' role.
     *
     * @return the SOAP actor roles
     * @see SoapEndpointInvocationChain#DEFAULT_ROLE
     */
    protected final String[] getRoles() {
        return roles;
    }

    /**
     * Sets the array of SOAP Actor roles to apply to all endpoints mapped by this endpoint mapping.
     *
     * @param roles the SOAP actor roles
     */
    public final void setRoles(String[] roles) {
        this.roles = roles;
    }

    /**
     * Creates a new <code>SoapEndpointInvocationChain</code> based on the given endpoint, and the set interceptors, and
     * roles.
     *
     * @param endpoint the endpoint
     * @return the created invocation chain
     * @see #setInterceptors(org.springframework.ws.EndpointInterceptor[])
     * @see #setRoles(String[])
     */
    protected EndpointInvocationChain createEndpointInvocationChain(Object endpoint) {
        return new SoapEndpointInvocationChain(endpoint, getInterceptors(), this.roles);
    }
}
