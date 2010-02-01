/*
 * Copyright 2008 the original author or authors.
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

package org.springframework.ws.transport.tcp.support;

import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.ws.transport.tcp.TcpTransportConstants;

/**
 * Collection of utility methods to work with TCP/IP transports.
 *
 * @author Arjen Poutsma
 */
public abstract class TcpTransportUtils {

    /**
     * Converts the given Socket into a <code>tcp</code> URI.
     *
     * @param socket the socket
     * @return a tcp URI
     */
    public static URI toUri(Socket socket) throws URISyntaxException {
        String host = socket.getInetAddress().getHostName();
        return new URI(TcpTransportConstants.TCP_URI_SCHEME, null, host, socket.getPort(), null, null, null);

    }

}
