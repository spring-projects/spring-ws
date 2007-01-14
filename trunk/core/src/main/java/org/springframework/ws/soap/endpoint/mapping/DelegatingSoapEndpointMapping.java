/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.ws.soap.endpoint.mapping;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInvocationChain;
import org.springframework.ws.server.EndpointMapping;
import org.springframework.ws.soap.SoapEndpointMapping;
import org.springframework.ws.soap.server.SoapEndpointInvocationChain;

/**
 * <code>EndpointMapping</code> implement that adds SOAP actors or roles to a delegate endpoint. Delegates to another
 * <code>EndpointMapping</code>, set by <code>delegate</code>, and adds the actors or roles specified by
 * <code>actorsOrRoles</code>.
 * <p/>
 * This endpoint mapping makes it possible to set actors/roles on a specific endpoint, without making the all endpoint
 * mappings depend on SOAP-specific functionality. For normal use, setting an actor or role on an endpoint is not
 * required, the default 'next' role is sufficient.
 * <p/>
 * It is only in a scenario when a certain endpoint act as a SOAP intermediary for another endpoint, as described in the
 * SOAP specificication, this mapping is useful.
 *
 * @author Arjen Poutsma
 * @see org.springframework.ws.soap.SoapHeader#examineMustUnderstandHeaderElements(String)
 * @see org.springframework.ws.soap.SoapVersion#getNextActorOrRoleUri()
 */
public class DelegatingSoapEndpointMapping implements InitializingBean, SoapEndpointMapping {

    private EndpointMapping delegate;

    private String[] actorsOrRoles;

    /**
     * Sets the delegate <code>EndpointMapping</code> to resolve the endpoint with.
     */
    public void setDelegate(EndpointMapping delegate) {
        this.delegate = delegate;
    }

    public final void setActorOrRole(String actorOrRole) {
        Assert.notNull(actorOrRole, "actorOrRole must not be null");
        actorsOrRoles = new String[]{actorOrRole};
    }

    public final void setActorsOrRoles(String[] actorsOrRoles) {
        Assert.notEmpty(actorsOrRoles, "actorsOrRoles must not be empty");
        this.actorsOrRoles = actorsOrRoles;
    }

    /**
     * Creates a new <code>SoapEndpointInvocationChain</code> based on the delegate endpoint, the delegate interceptors,
     * and set actors/roles.
     *
     * @see #setActorsOrRoles(String[])
     */
    public EndpointInvocationChain getEndpoint(MessageContext messageContext) throws Exception {
        EndpointInvocationChain delegateChain = delegate.getEndpoint(messageContext);
        return new SoapEndpointInvocationChain(delegateChain.getEndpoint(), delegateChain.getInterceptors(),
                actorsOrRoles);
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(delegate, "delegate is required");
    }
}
