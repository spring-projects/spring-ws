/*
 * Copyright 2005-2010 the original author or authors.
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

package org.springframework.ws.server.endpoint.mapping;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.axiom.AxiomSoapMessageFactory;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;

/**
 * Implementation of the <code>EndpointMapping</code> interface to map from the full request URI to endpoint beans.
 * Supports both mapping to bean instances and mapping to bean names: the latter is required for prototype handlers.
 * <p/>
 * The <code>endpointMap</code> property is suitable for populating the endpoint map with bean references, e.g. via the
 * map element in XML bean definitions.
 * <p/>
 * Mappings to bean names can be set via the <code>mappings</code> property, in a form accepted by the
 * <code>java.util.Properties</code> class, like as follows:
 * <pre>
 * http://example.com:8080/services/bookFlight=bookFlightEndpoint
 * jms://exampleQueue=getFlightsEndpoint
 * </pre>
 * The syntax is URI=ENDPOINT_BEAN_NAME.
 * <p/>
 * This endpoint mapping does not read from the request message, and therefore is more suitable for message factories
 * which directly read from the transport request (such as the {@link AxiomSoapMessageFactory} with the
 * <code>payloadCaching</code> disabled). However, this endpoint mapping obviously is transport specific.
 *
 * @author Arjen Poutsma
 * @since 1.5.0
 */
public class UriEndpointMapping extends AbstractMapBasedEndpointMapping {

    @Override
    protected boolean validateLookupKey(String key) {
        try {
            new URI(key);
            return true;
        }
        catch (URISyntaxException e) {
            return false;
        }
    }

    @Override
    protected String getLookupKeyForMessage(MessageContext messageContext) throws Exception {
        TransportContext transportContext = TransportContextHolder.getTransportContext();
        if (transportContext != null) {
            WebServiceConnection connection = transportContext.getConnection();
            if (connection != null) {
                return connection.getUri().toString();
            }
        }
        return null;
    }
}
