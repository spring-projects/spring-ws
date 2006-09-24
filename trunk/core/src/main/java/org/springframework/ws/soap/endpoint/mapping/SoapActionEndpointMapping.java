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

package org.springframework.ws.soap.endpoint.mapping;

import java.util.Iterator;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.EndpointInterceptor;
import org.springframework.ws.EndpointInvocationChain;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.endpoint.mapping.AbstractMapBasedEndpointMapping;
import org.springframework.ws.soap.SoapEndpointInvocationChain;
import org.springframework.ws.soap.SoapEndpointMapping;

/**
 * Implementation of the <code>EndpointMapping</code> interface to map from <code>SOAPAction</code> headers to endpoint
 * beans. Supports both mapping to bean instances and mapping to bean names: the latter is required for prototype
 * handlers.
 * <p/>
 * The <code>endpointMap</code> property is suitable for populating the endpoint map with bean references, e.g. via the
 * map element in XML bean definitions.
 * <p/>
 * Mappings to bean names can be set via the <code>mappings</code> property, in a form accepted by the
 * <code>java.util.Properties</code> class, like as follows:
 * <pre>
 * http://www.springframework.org/spring-ws/samples/airline/BookFlight=bookFlightEndpoint
 * http://www.springframework.org/spring-ws/samples/airline/GetFlights=getFlightsEndpoint
 * </pre>
 * The syntax is SOAP_ACTION=ENDPOINT_BEAN_NAME.
 * <p/>
 * This endpoint mapping does not read from the request message, and therefore is more suitable for message contexts
 * which directly read from the transport request (such as the <code>AxiomSoapMessageContextFactory</code> with the
 * <code>payloadCaching</code> disabled).
 *
 * @author Arjen Poutsma
 */
public class SoapActionEndpointMapping extends AbstractMapBasedEndpointMapping implements SoapEndpointMapping {

    /**
     * The name of the SOAPAction <code>TransportRequest</code> header.
     */
    public static final String SOAP_ACTION_HEADER = "SOAPAction";

    private String[] actorsOrRoles;

    public final void setActorOrRole(String actorOrRole) {
        Assert.notNull(actorOrRole, "actorOrRole must not be null");
        actorsOrRoles = new String[]{actorOrRole};
    }

    public final void setActorsOrRoles(String[] actorsOrRoles) {
        Assert.notEmpty(actorsOrRoles, "actorsOrRoles must not be empty");
        this.actorsOrRoles = actorsOrRoles;
    }

    /**
     * Creates a new <code>SoapEndpointInvocationChain</code> based on the given endpoint, and the set interceptors, and
     * actors/roles.
     *
     * @param endpoint     the endpoint
     * @param interceptors the endpoint interceptors
     * @return the created invocation chain
     * @see #setInterceptors(org.springframework.ws.EndpointInterceptor[])
     * @see #setActorsOrRoles(String[])
     */
    protected final EndpointInvocationChain createEndpointInvocationChain(MessageContext messageContext,
                                                                          Object endpoint,
                                                                          EndpointInterceptor[] interceptors) {
        return new SoapEndpointInvocationChain(endpoint, interceptors, actorsOrRoles);
    }

    protected String getLookupKeyForMessage(MessageContext messageContext) throws Exception {
        Iterator iterator = messageContext.getTransportRequest().getHeaders(SOAP_ACTION_HEADER);
        String soapAction = "";
        if (iterator.hasNext()) {
            soapAction = (String) iterator.next();
        }
        if (StringUtils.hasLength(soapAction) && soapAction.charAt(0) == '"' &&
                soapAction.charAt(soapAction.length() - 1) == '"') {
            return soapAction.substring(1, soapAction.length() - 1);
        }
        else {
            return soapAction;
        }
    }

    protected boolean validateLookupKey(String key) {
        return StringUtils.hasLength(key);
    }
}
