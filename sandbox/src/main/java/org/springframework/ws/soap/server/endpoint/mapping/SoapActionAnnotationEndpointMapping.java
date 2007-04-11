/*
 * Copyright 2007 the original author or authors.
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

package org.springframework.ws.soap.server.endpoint.mapping;

import java.lang.reflect.Method;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.EndpointInvocationChain;
import org.springframework.ws.server.endpoint.mapping.AbstractAnnotationEndpointMapping;
import org.springframework.ws.soap.SoapEndpointMapping;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.server.SoapEndpointInvocationChain;
import org.springframework.ws.soap.server.endpoint.annotation.SoapAction;

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
public class SoapActionAnnotationEndpointMapping extends AbstractAnnotationEndpointMapping
        implements SoapEndpointMapping {

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
     * @see #setInterceptors(org.springframework.ws.server.EndpointInterceptor[])
     * @see #setActorsOrRoles(String[])
     */
    protected final EndpointInvocationChain createEndpointInvocationChain(MessageContext messageContext,
                                                                          Object endpoint,
                                                                          EndpointInterceptor[] interceptors) {
        return new SoapEndpointInvocationChain(endpoint, interceptors, actorsOrRoles);
    }

    protected String getLookupKeyForMessage(MessageContext messageContext) throws Exception {
        if (messageContext.getRequest() instanceof SoapMessage) {
            SoapMessage request = (SoapMessage) messageContext.getRequest();
            String soapAction = request.getSoapAction();
            if (StringUtils.hasLength(soapAction) && soapAction.charAt(0) == '"' &&
                    soapAction.charAt(soapAction.length() - 1) == '"') {
                return soapAction.substring(1, soapAction.length() - 1);
            }
            else {
                return soapAction;
            }
        }
        else {
            return null;
        }
    }

    protected String getLookupKeyForMethod(Method method) {
        SoapAction soapAction = AnnotationUtils.getAnnotation(method, SoapAction.class);
        return soapAction != null ? soapAction.value() : null;
    }
}
