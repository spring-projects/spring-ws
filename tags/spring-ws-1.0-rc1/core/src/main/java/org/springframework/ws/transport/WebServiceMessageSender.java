package org.springframework.ws.transport;

import java.io.IOException;

import org.springframework.ws.WebServiceMessage;

/**
 * Defines the methods for classes capable of sending and receiving {@link WebServiceMessage} instances across a
 * transport.
 * <p/>
 * The <code>WebServiceMessageSender</code> is basically a factory for {@link WebServiceConnection} objects.
 *
 * @author Arjen Poutsma
 * @see WebServiceConnection
 */
public interface WebServiceMessageSender {

    /**
     * Create a new {@link WebServiceConnection} to the specified URI.
     *
     * @param uri the URI to open a connection to
     * @return the new connection
     * @throws IOException in case of I/O errors
     */
    WebServiceConnection createConnection(String uri) throws IOException;

    /**
     * Indicates whether the message sender supports the given URI.
     *
     * @param uri the URI
     * @return true
     */
    boolean supports(String uri);

}
