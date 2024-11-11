/*
 * Copyright 2005-2024 the original author or authors.
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
package org.springframework.ws.server.endpoint.observation;

import io.micrometer.observation.transport.RequestReplyReceiverContext;
import org.springframework.ws.transport.HeadersAwareReceiverWebServiceConnection;
import org.springframework.ws.transport.TransportInputStream;

import java.io.IOException;
import java.util.Iterator;

/**
 * ObservationContext that describes how a WebService Endpoint is observed.
 * @author Johan Kindgren
 */
public class WebServiceEndpointContext extends RequestReplyReceiverContext<HeadersAwareReceiverWebServiceConnection, TransportInputStream> {

    public static final String UNKNOWN = "unknown";
    private String outcome = UNKNOWN;
    private String localPart = UNKNOWN;
    private String namespace = UNKNOWN;
    private String soapAction = UNKNOWN;

    public WebServiceEndpointContext(HeadersAwareReceiverWebServiceConnection connection) {
        super((carrier, key) -> {
            try {
                Iterator<String> headers = carrier.getRequestHeaders(key);
                if (headers.hasNext()) {
                    return headers.next();
                } else {
                    return null;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        setCarrier(connection);
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public String getLocalPart() {
        return localPart;
    }

    public void setLocalPart(String localPart) {
        this.localPart = localPart;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getSoapAction() {
        return soapAction;
    }

    public void setSoapAction(String soapAction) {
        this.soapAction = soapAction;
    }
}
