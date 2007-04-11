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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.WebServiceMessageSender;

/** @author Arjen Poutsma */
public class TcpMessageSender implements WebServiceMessageSender, InitializingBean {

    private InetAddress address;

    private int port = -1;

    private int timeOut = 1000;

    /** Sets the port the sender will connect to. */
    public void setPort(int port) {
        this.port = port;
    }

    /** Sets the amount of milliseconds before the tcp connection will timeout. */
    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    /**
     * Sets the internet address the client will connect to.
     *
     * @throws java.net.UnknownHostException when the given address is not known
     */
    public void setAddress(String address) throws UnknownHostException {
        this.address = InetAddress.getByName(address);
    }

    public WebServiceConnection createConnection() throws IOException {
        Socket socket = new Socket();
        SocketAddress socketAddress = new InetSocketAddress(address, port);
        socket.connect(socketAddress, timeOut);
        return new TcpSendingWebServiceConnection(socket);
    }

    public void afterPropertiesSet() throws Exception {
        if (port == -1) {
            throw new IllegalArgumentException("port is required");
        }
        Assert.notNull(address, "address is required");

    }
}
