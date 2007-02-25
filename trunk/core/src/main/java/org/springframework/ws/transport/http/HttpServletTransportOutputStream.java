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
import javax.servlet.http.HttpServletResponse;

import org.springframework.ws.transport.TransportOutputStream;

/**
 * HTTP Servlet specific implementation of the <code>TransportOutputStream</code> interface. Exposes the
 * <code>HttpServletResponse</code>.
 *
 * @author Arjen Poutsma
 * @see #getHttpServletResponse()
 */
class HttpServletTransportOutputStream extends TransportOutputStream {

    private final HttpServletResponse httpServletResponse;

    /**
     * Constructs a new instance of the <code>HttpTransportResponse</code> with the given
     * <code>HttpServletResponse</code>.
     */
    public HttpServletTransportOutputStream(HttpServletResponse httpServletResponse) throws IOException {
        this.httpServletResponse = httpServletResponse;
    }

    protected OutputStream createOutputStream() throws IOException {
        return httpServletResponse.getOutputStream();
    }

    /**
     * Returns the wrapped <code>HttpServletResponse</code>.
     */
    public HttpServletResponse getHttpServletResponse() {
        return httpServletResponse;
    }

    public void addHeader(String name, String value) {
        httpServletResponse.addHeader(name, value);
    }
}
