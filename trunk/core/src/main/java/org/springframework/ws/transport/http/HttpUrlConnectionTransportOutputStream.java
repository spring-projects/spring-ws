/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.ws.transport.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import org.springframework.util.Assert;
import org.springframework.ws.transport.TransportOutputStream;

/**
 * Implementation of the <code>TransportOutputStream</code> interface based on {@link java.net.HttpURLConnection}.
 * Exposes the <code>HttpURLConnection</code>.
 *
 * @author Arjen Poutsma
 */
class HttpUrlConnectionTransportOutputStream extends TransportOutputStream {

    private final HttpURLConnection connection;

    /**
     * Constructs a new instance of the <code>HttpUrlConnectionTransportOutputStream</code> based on the given
     * <code>HttpURLConnection</code>.
     */
    public HttpUrlConnectionTransportOutputStream(HttpURLConnection connection) throws IOException {
        Assert.notNull(connection, "connection must not be null");
        this.connection = connection;
    }

    public void addHeader(String name, String value) throws IOException {
        connection.setRequestProperty(name, value);
    }

    protected OutputStream createOutputStream() throws IOException {
        return connection.getOutputStream();
    }
}
