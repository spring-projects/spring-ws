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
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;

import org.springframework.ws.transport.TransportInputStream;
import org.springframework.ws.transport.support.EnumerationIterator;

/**
 * HTTP Servlet specific implementation of the <code>TransportInputStream</code> interface. Exposes the
 * <code>HttpServletRequest</code>.
 *
 * @author Arjen Poutsma
 * @see #getHttpServletRequest()
 */
public class HttpServletTransportInputStream extends TransportInputStream {

    private final HttpServletRequest httpServletRequest;

    /**
     * Constructs a new instance of the <code>HttpTransportRequest</code> with the given
     * <code>HttpServletRequest</code>.
     */
    public HttpServletTransportInputStream(HttpServletRequest httpServletRequest) throws IOException {
        this.httpServletRequest = httpServletRequest;
    }

    protected InputStream createInputStream() throws IOException {
        return httpServletRequest.getInputStream();
    }

    /**
     * Returns the wrapped <code>HttpServletRequest</code>.
     */
    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    public Iterator getHeaderNames() {
        return new EnumerationIterator(httpServletRequest.getHeaderNames());
    }

    public Iterator getHeaders(String name) {
        return new EnumerationIterator(httpServletRequest.getHeaders(name));
    }
}
