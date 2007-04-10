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

import org.springframework.ws.transport.AbstractReceivingWebServiceConnection;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.support.EnumerationIterator;

/**
 * Implementation of {@link WebServiceConnection} that is based on the Servlet API.
 *
 * @author Arjen Poutsma
 */
public class HttpServletConnection extends AbstractReceivingWebServiceConnection {

    private final HttpServletRequest request;

    private final HttpServletResponse response;

    /**
     * Constructs a new servlet connection with the given <code>HttpServletRequest</code> and
     * <code>HttpServletResponse</code>.
     */
    public HttpServletConnection(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        request = httpServletRequest;
        response = httpServletResponse;
    }

    /** Returns the <code>HttpServletRequest</code> for this connection. */
    public HttpServletRequest getHttpServletRequest() {
        return request;
    }

    /** Returns the <code>HttpServletResponse</code> for this connection. */
    public HttpServletResponse getHttpServletResponse() {
        return response;
    }

    protected Iterator getRequestHeaderNames() throws IOException {
        return new EnumerationIterator(request.getHeaderNames());
    }

    protected Iterator getRequestHeaders(String name) throws IOException {
        return new EnumerationIterator(request.getHeaders(name));
    }

    protected InputStream getRequestInputStream() throws IOException {
        return request.getInputStream();
    }

    protected void addResponseHeader(String name, String value) throws IOException {
        response.addHeader(name, value);
    }

    protected OutputStream getResponseOutputStream() throws IOException {
        return response.getOutputStream();
    }

    protected void sendResponse() throws IOException {
        // no op
    }

    public void close() throws IOException {
        // no op
    }

}
