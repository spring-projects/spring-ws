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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

/**
 * Abstract base class for {@link WebServiceConnection} implementations used for sending requests.
 *
 * @author Arjen Poutsma
 * @since 1.0.0
 */
public abstract class AbstractSenderConnection extends AbstractWebServiceConnection {

    private TransportOutputStream requestOutputStream;

    private TransportInputStream responseInputStream;

    protected final TransportOutputStream createTransportOutputStream() throws IOException {
        if (requestOutputStream == null) {
            requestOutputStream = new RequestTransportOutputStream();
        }
        return requestOutputStream;
    }

    protected final TransportInputStream createTransportInputStream() throws IOException {
        if (hasResponse()) {
            if (responseInputStream == null) {
                responseInputStream = new ResponseTransportInputStream();
            }
            return responseInputStream;
        }
        else {
            return null;
        }
    }

    /** Indicates whether this connection has a response. */
    protected abstract boolean hasResponse() throws IOException;

    /**
     * Adds a request header with the given name and value. This method can be called multiple times, to allow for
     * headers with multiple values.
     *
     * @param name  the name of the header
     * @param value the value of the header
     */
    protected abstract void addRequestHeader(String name, String value) throws IOException;

    /** Returns the output stream to write the request to. */
    protected abstract OutputStream getRequestOutputStream() throws IOException;

    /**
     * Returns an iteration over all the header names this request contains. Returns an empty <code>Iterator</code> if
     * there areno headers.
     */
    protected abstract Iterator getResponseHeaderNames() throws IOException;

    /**
     * Returns an iteration over all the string values of the specified header. Returns an empty <code>Iterator</code>
     * if there are no headers of the specified name.
     */
    protected abstract Iterator getResponseHeaders(String name) throws IOException;

    /** Returns the input stream to read the response from. */
    protected abstract InputStream getResponseInputStream() throws IOException;

    /** Implementation of <code>TransportInputStream</code> for receiving-side connections. */
    class RequestTransportOutputStream extends TransportOutputStream {

        public void addHeader(String name, String value) throws IOException {
            addRequestHeader(name, value);
        }

        protected OutputStream createOutputStream() throws IOException {
            return getRequestOutputStream();
        }
    }

    /** Implementation of {@link TransportInputStream} for client-side HTTP. */
    class ResponseTransportInputStream extends TransportInputStream {

        protected InputStream createInputStream() throws IOException {
            return getResponseInputStream();
        }

        public Iterator getHeaderNames() throws IOException {
            return getResponseHeaderNames();
        }

        public Iterator getHeaders(String name) throws IOException {
            return getResponseHeaders(name);
        }

    }

}
