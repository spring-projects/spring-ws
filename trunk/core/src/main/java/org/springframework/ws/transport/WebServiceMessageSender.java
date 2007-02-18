package org.springframework.ws.transport;

import java.io.IOException;

/**
 * Defines the methods for classes capable of sending and receiving {@link org.springframework.ws.WebServiceMessage}
 * instances across a transport.
 * <p/>
 * The <code>WebServiceMessageSender</code> is basically a factory for {@link WebServiceConnection} objects.
 *
 * @author Arjen Poutsma
 * @see WebServiceConnection
 */
public interface WebServiceMessageSender {

    /**
     * Create a new <code>WebServiceConnection</code>.
     *
     * @return the new connection
     * @throws IOException in case of I/O errors
     */
    WebServiceConnection createConnection() throws IOException;

}
