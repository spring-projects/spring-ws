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

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageSender;

/** @author Arjen Poutsma */
public class TcpMessageSender implements WebServiceMessageSender {

    private static final String TCP_SCHEME = "tcp://";

    public static final int DEFAULT_PORT = 8081;

    private int timeOut = 1000;

    /** Sets the amount of milliseconds before the tcp connection will timeout. */
    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    public boolean supports(String uri) {
        return StringUtils.hasLength(uri) && uri.startsWith(TCP_SCHEME);
    }

    public WebServiceConnection createConnection(String uri) throws IOException {
        Assert.isTrue(uri.startsWith(TCP_SCHEME), "Invalid uri: " + uri);
        uri = uri.substring(TCP_SCHEME.length());
        int idx = uri.indexOf(':');
        String hostname;
        int port;
        if (idx != -1) {
            hostname = uri.substring(0, idx);
            port = Integer.parseInt(uri.substring(idx + 1));
        } else {
            hostname = uri;
            port = DEFAULT_PORT;
        }
        Socket socket = new Socket();
        SocketAddress socketAddress = new InetSocketAddress(hostname, port);
        socket.connect(socketAddress, timeOut);
        return new TcpSenderConnection(socket);
    }
}
