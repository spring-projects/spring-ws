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
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.transport.TransportInputStream;

/**
 * Implementation of the <code>TransportInputStream</code> interface based on {@link java.net.HttpURLConnection}.
 * Exposes the <code>HttpURLConnection</code>.
 *
 * @author Arjen Poutsma
 */
public class HttpUrlConnectionTransportInputStream extends TransportInputStream {

    private final HttpURLConnection connection;

    /**
     * Constructs a new instance of the <code>HttpUrlConnectionTransportInputStream</code> based on the given
     * <code>HttpURLConnection</code>.
     */
    public HttpUrlConnectionTransportInputStream(HttpURLConnection connection) throws IOException {
        Assert.notNull(connection, "connection must not be null");
        this.connection = connection;
    }

    /**
     * Returns the wrapped <code>HttpURLConnection</code>.
     */
    public HttpURLConnection getConnection() {
        return connection;
    }

    protected InputStream createInputStream() throws IOException {
        if (connection.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
            return connection.getErrorStream();
        }
        else {
            return connection.getInputStream();
        }
    }

    public Iterator getHeaderNames() throws IOException {
        List headerNames = new ArrayList();
        // Header field 0 is the status line, so we start at 1
        int i = 1;
        while (true) {
            String headerName = connection.getHeaderField(i);
            if (!StringUtils.hasLength(headerName)) {
                break;
            }
            headerNames.add(headerName);
            i++;
        }
        return headerNames.iterator();
    }

    public Iterator getHeaders(String name) throws IOException {
        String headerField = connection.getHeaderField(name);
        if (headerField == null) {
            return Collections.EMPTY_LIST.iterator();
        }
        else {
            Set tokens = StringUtils.commaDelimitedListToSet(headerField);
            return tokens.iterator();
        }
    }
}
