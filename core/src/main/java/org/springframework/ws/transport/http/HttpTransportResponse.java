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

import org.springframework.ws.transport.TransportResponse;

/**
 * HTTP-specific implementation of the <code>TransportResponse</code> interface. Exposes the
 * <code>HttpServletResponse</code>
 *
 * @author Arjen Poutsma
 */
public class HttpTransportResponse implements TransportResponse {

    private final HttpServletResponse response;

    /**
     * Constructs a new instance of the <code>HttpTransportResponse</code> with the given
     * <code>HttpServletResponse</code>.
     */
    public HttpTransportResponse(HttpServletResponse response) {
        this.response = response;
    }

    /**
     * Returns the wrapped <code>HttpServletResponse</code>.
     */
    public HttpServletResponse getHttpServletResponse() {
        return response;
    }

    public void addHeader(String name, String value) {
        response.addHeader(name, value);
    }

    public OutputStream getOutputStream() throws IOException {
        return response.getOutputStream();
    }
}
