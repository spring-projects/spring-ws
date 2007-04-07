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

package org.springframework.ws.transport;

import java.io.IOException;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.http.HttpServletConnection;

/**
 * Represents a point-to-point connection that a client can use for sending {@link WebServiceMessage} objects directly
 * to a remote party.
 * <p/>
 * A <code>WebServiceConnection</code> can be obtained using a {@link WebServiceMessageSender}.
 * <p/>
 * On the receiving side, the typical usage scenario for this connection is: <ol> <li>Create concrete connection
 * implementation (eg. {@link HttpServletConnection}) <li>Read request from {@link #getTransportInputStream() the input
 * stream}. <li>{@link TransportInputStream#close() Close} the input stream <li>Write response to {@link
 * #getTransportOutputStream() the output stream}. <li>{@link TransportOutputStream#flush() Flush} the output stream
 * <li>{@link TransportOutputStream#close() Close} the output stream <li>{@link #close() Close the connection} </ol>
 * <p/>
 * On the sending side, the typical usage scenario for this connection is: <ol> <li>Create connection with {@link
 * WebServiceMessageSender#createConnection()} <li>Write request to {@link #getTransportOutputStream() the output
 * stream}. <li>{@link TransportOutputStream#flush() Flush} the output stream <li>{@link TransportOutputStream#close()
 * Close} the output stream <li> <li>Read request from {@link #getTransportInputStream() the input stream}. <li>{@link
 * TransportInputStream#close() Close} the input stream <li>{@link #close() Close the connection} </ol>
 *
 * @author Arjen Poutsma
 * @see WebServiceMessageSender#createConnection()
 * @see #getTransportInputStream()
 * @see #getTransportOutputStream()
 */
public interface WebServiceConnection {

    /**
     * Returns a transport input stream for this connection.
     * <p/>
     * Returns <code>null</code> if no transport input stream is available (eg. a request might not have a response).
     *
     * @return a transport input stream for this connection, or <code>null</code>
     * @throws IOException if an I/O error occurs when creating the input stream, the connection is closed
     */
    TransportInputStream getTransportInputStream() throws IOException;

    /**
     * Returns a transport output stream for this connection.
     * <p/>
     * Returns <code>null</code> if no transport output stream is available (eg. a response might not have a response).
     *
     * @return a transport output stream for this connection
     * @throws IOException if an I/O error occurs when creating the output stream, the connection is closed
     */
    TransportOutputStream getTransportOutputStream() throws IOException;

    /**
     * Closes this connection.
     * <p/>
     * Once a connection has been closed, it is not available for further use. A new connection needs to be created.
     *
     * @throws IOException if an I/O error occurs when closing this socket
     */
    void close() throws IOException;

}
