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
import java.util.Collections;
import java.util.Iterator;

import org.springframework.util.Assert;
import org.springframework.ws.transport.AbstractReceivingWebServiceConnection;

/** @author Arjen Poutsma */
public class TcpReceivingWebServiceConnection extends AbstractReceivingWebServiceConnection {

    private final Socket socket;

    public TcpReceivingWebServiceConnection(Socket socket) {
        Assert.notNull(socket, "socket must not be null");
        this.socket = socket;
    }

    public void close() throws IOException {
        socket.close();
    }

    protected Iterator getRequestHeaderNames() throws IOException {
        return Collections.EMPTY_LIST.iterator();
    }

    protected Iterator getRequestHeaders(String name) throws IOException {
        return Collections.EMPTY_LIST.iterator();
    }

    protected InputStream getRequestInputStream() throws IOException {
        return new FilterInputStream(socket.getInputStream()) {

            public void close() throws IOException {
                // don't close the socket
                socket.shutdownInput();
            }
        };
    }

    protected void addResponseHeader(String name, String value) throws IOException {
    }

    protected OutputStream getResponseOutputStream() throws IOException {
        return new FilterOutputStream(socket.getOutputStream()) {

            public void close() throws IOException {
                // don't close the socket
                socket.shutdownOutput();
            }
        };
    }

    protected void sendResponse() throws IOException {
    }


}
