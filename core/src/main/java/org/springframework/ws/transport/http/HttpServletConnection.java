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

package org.springframework.ws.transport.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.ws.transport.TransportInputStream;
import org.springframework.ws.transport.TransportOutputStream;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.support.EnumerationIterator;

/**
 * Implementation of {@link WebServiceConnection} that is based on the Servlet API. Exposes a {@link HttpServletRequest}
 * and {@link HttpServletResponse}.
 *
 * @author Arjen Poutsma
 * @author Arjen Poutsma
 * @see #getHttpServletRequest()
 * @see #getHttpServletResponse()
 */
public class HttpServletConnection implements WebServiceConnection {

    private final HttpServletRequest httpServletRequest;

    private final HttpServletResponse httpServletResponse;

    /**
     * Constructs a new servlet connection with the given <code>HttpServletRequest</code> and
     * <code>HttpServletResponse</code>.
     */
    public HttpServletConnection(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        this.httpServletRequest = httpServletRequest;
        this.httpServletResponse = httpServletResponse;
    }

    /** Returns the <code>HttpServletRequest</code> for this connection. */
    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    /** Returns the <code>HttpServletResponse</code> for this connection. */
    public HttpServletResponse getHttpServletResponse() {
        return httpServletResponse;
    }

    public TransportInputStream getTransportInputStream() {
        return new HttpServletTransportInputStream();
    }

    public TransportOutputStream getTransportOutputStream() {
        return new HttpServletTransportOutputStream();
    }

    public void close() throws IOException {
        // no op
    }

    /**
     * Implementation of {@link TransportInputStream} based on the {@link HttpServletRequest} field.
     *
     * @see HttpServletConnection#httpServletRequest
     */
    private class HttpServletTransportInputStream extends TransportInputStream {

        protected InputStream createInputStream() throws IOException {
            return httpServletRequest.getInputStream();
        }

        public Iterator getHeaderNames() {
            return new EnumerationIterator(httpServletRequest.getHeaderNames());
        }

        public Iterator getHeaders(String name) {
            return new EnumerationIterator(httpServletRequest.getHeaders(name));
        }
    }

    /**
     * Implementation of {@link TransportOutputStream} based on the {@link HttpServletResponse} field.
     *
     * @see HttpServletConnection#httpServletResponse
     */
    private class HttpServletTransportOutputStream extends TransportOutputStream {

        protected OutputStream createOutputStream() throws IOException {
            return httpServletResponse.getOutputStream();
        }

        public void addHeader(String name, String value) {
            httpServletResponse.addHeader(name, value);
        }
    }


}
