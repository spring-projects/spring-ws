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

package org.springframework.ws.transport.tcp;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Iterator;

import org.springframework.util.Assert;
import org.springframework.ws.transport.AbstractSenderConnection;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.tcp.support.TcpTransportUtils;

/**
 * Implementation of {@link WebServiceConnection} that is used for client-side TCP/IP access. Exposes a {@link Socket}.
 *
 * @author Arjen Poutsma
 */
public class TcpSenderConnection extends AbstractSenderConnection {

    private final Socket socket;

    /** Constructs a new TCP/IP connection with the given socket. */
    protected TcpSenderConnection(Socket socket) {
        Assert.notNull(socket, "socket must not be null");
        this.socket = socket;
    }

    /** Returns the socket for this connection. */
    public Socket getSocket() {
        return socket;
    }

    public URI getUri() throws URISyntaxException {
        return TcpTransportUtils.toUri(socket);
    }

    public void onClose() throws IOException {
        socket.close();
    }

    /*
     * Errors
     */

    public boolean hasError() throws IOException {
        return false;
    }

    public String getErrorMessage() throws IOException {
        return null;
    }

    protected void addRequestHeader(String name, String value) throws IOException {
    }

    protected OutputStream getRequestOutputStream() throws IOException {
        return new FilterOutputStream(socket.getOutputStream()) {

            public void close() throws IOException {
                // don't close the socket
                socket.shutdownOutput();
            }
        };
    }

    protected void sendRequest() throws IOException {
    }

    protected boolean hasResponse() throws IOException {
        return true;
    }

    protected Iterator getResponseHeaderNames() throws IOException {
        return Collections.EMPTY_LIST.iterator();
    }

    protected Iterator getResponseHeaders(String name) throws IOException {
        return Collections.EMPTY_LIST.iterator();
    }

    protected InputStream getResponseInputStream() throws IOException {
        return new FilterInputStream(socket.getInputStream()) {

            public void close() throws IOException {
                // don't close the socket
                socket.shutdownInput();
            }
        };
    }
}
