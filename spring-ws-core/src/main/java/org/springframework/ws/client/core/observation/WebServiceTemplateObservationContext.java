package org.springframework.ws.client.core.observation;

import io.micrometer.observation.transport.RequestReplySenderContext;
import org.springframework.ws.transport.HeadersAwareSenderWebServiceConnection;
import org.springframework.ws.transport.TransportInputStream;

import java.io.IOException;

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
