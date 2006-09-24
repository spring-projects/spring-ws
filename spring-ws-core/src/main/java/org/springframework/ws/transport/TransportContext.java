package org.springframework.ws.transport;

/**
 * Defines the contract for Web service request that come in via a transport. Exposes headers and the inputstream to
 * read from.
 *
 * @author Arjen Poutsma
 */
public interface TransportContext {

    TransportRequest getTransportRequest() throws TransportException;

    TransportResponse getTransportResponse() throws TransportException;

}
