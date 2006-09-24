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
import java.util.Enumeration;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;

import org.springframework.util.Assert;
import org.springframework.ws.transport.TransportRequest;

/**
 * HTTP-specific implementation of the <code>TransportRequest</code> interface. Exposes the
 * <code>HttpServletRequest</code>
 *
 * @author Arjen Poutsma
 */
public class HttpTransportRequest implements TransportRequest {

    private final HttpServletRequest request;

    /**
     * Constructs a new instance of the <code>HttpTransportRequest</code> with the given
     * <code>HttpServletRequest</code>.
     */
    public HttpTransportRequest(HttpServletRequest request) {
        Assert.notNull(request, "request is required");
        this.request = request;
    }

    /**
     * Returns the wrapped <code>HttpServletRequest</code>.
     */
    public HttpServletRequest getHttpServletRequest() {
        return request;
    }

    public Iterator getHeaders(String name) {
        return new EnumerationIterator(request.getHeaders(name));
    }

    public InputStream getInputStream() throws IOException {
        return request.getInputStream();
    }

    public Iterator getHeaderNames() {
        return new EnumerationIterator(request.getHeaderNames());
    }

    public String getUrl() {
        StringBuffer url = new StringBuffer(request.getScheme());
        url.append("://").append(request.getServerName()).append(':').append(request.getServerPort());
        url.append(request.getRequestURI());
        return url.toString();
    }

    /**
     * Private static class that adapts a header enumeration provided by the HttpServletRequest and provides it as an
     * iterator.
     */
    private static class EnumerationIterator implements Iterator {

        private final Enumeration enumeration;

        public EnumerationIterator(Enumeration enumeration) {
            this.enumeration = enumeration;
        }

        public boolean hasNext() {
            return enumeration.hasMoreElements();
        }

        public Object next() {
            return enumeration.nextElement();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
