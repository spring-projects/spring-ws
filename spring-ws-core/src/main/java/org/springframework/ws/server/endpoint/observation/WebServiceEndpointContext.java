package org.springframework.ws.server.endpoint.observation;

import io.micrometer.observation.transport.Propagator;
import io.micrometer.observation.transport.RequestReplyReceiverContext;
import org.springframework.ws.transport.HeadersAwareReceiverWebServiceConnection;
import org.springframework.ws.transport.TransportInputStream;

import java.io.IOException;
import java.util.Iterator;

public class WebServiceEndpointContext extends RequestReplyReceiverContext<HeadersAwareReceiverWebServiceConnection, TransportInputStream> {


    public static final String UNKNOWN = "unknown";
    private String outcome = UNKNOWN;
    private String localname = UNKNOWN;
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

    public String getSoapAction() {
        return soapAction;
    }

    public void setSoapAction(String soapAction) {
        this.soapAction = soapAction;
    }
}
