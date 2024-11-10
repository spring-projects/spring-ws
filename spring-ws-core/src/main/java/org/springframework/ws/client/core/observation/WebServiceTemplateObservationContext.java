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
package org.springframework.ws.client.core.observation;

import io.micrometer.observation.transport.RequestReplySenderContext;
import org.springframework.ws.transport.HeadersAwareSenderWebServiceConnection;
import org.springframework.ws.transport.TransportInputStream;

import java.io.IOException;
/**
 * ObservationContext used to instrument a WebServiceTemplate operation.
 * @author Johan Kindgren
 */
class WebServiceTemplateObservationContext extends RequestReplySenderContext<HeadersAwareSenderWebServiceConnection, TransportInputStream> {


    public static final String UNKNOWN = "unknown";
    private String outcome = UNKNOWN;
    private String localname = UNKNOWN;
    private String namespace = UNKNOWN;
    private String host = UNKNOWN;
    private String soapAction = UNKNOWN;

    public WebServiceTemplateObservationContext(HeadersAwareSenderWebServiceConnection connection) {
        super((carrier, key, value) -> {

            if (carrier != null) {
                try {
                    carrier.addRequestHeader(key, value);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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

    public String getLocalname() {
        return localname;
    }

    public void setLocalname(String localname) {
        this.localname = localname;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }


    public String getSoapAction() {
        return soapAction;
    }

    public void setSoapAction(String soapAction) {
        this.soapAction = soapAction;
    }
}
