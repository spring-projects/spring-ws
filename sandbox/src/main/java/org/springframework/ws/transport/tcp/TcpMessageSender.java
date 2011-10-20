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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageSender;

/** @author Arjen Poutsma */
public class TcpMessageSender implements WebServiceMessageSender {

    public static final int DEFAULT_PORT = 8081;

    private int timeOut = 1000;

    /** Sets the amount of milliseconds before the tcp connection will timeout. */
    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    public WebServiceConnection createConnection(URI theUri) throws IOException {
        int port = theUri.getPort();
        if (port == -1) {
            port = DEFAULT_PORT;
        }
        Socket socket = new Socket();
        SocketAddress socketAddress = new InetSocketAddress(theUri.getHost(), port);
        socket.connect(socketAddress, timeOut);
        return new TcpSenderConnection(socket);
    }

    public boolean supports(URI uri) {
        return uri.getScheme().equals(TcpTransportConstants.TCP_URI_SCHEME);
    }
}
