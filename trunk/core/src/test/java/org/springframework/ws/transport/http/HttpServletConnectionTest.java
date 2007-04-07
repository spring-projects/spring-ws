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

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import junit.framework.TestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.FileCopyUtils;
import org.springframework.ws.transport.TransportInputStream;

public class HttpServletConnectionTest extends TestCase {

    private HttpServletConnection connection;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    protected void setUp() throws Exception {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        connection = new HttpServletConnection(request, response);
    }

    public void testReadInputStream() throws Exception {
        byte[] content = "content".getBytes("UTF-8");
        request.setContent(content);
        byte[] result = FileCopyUtils.copyToByteArray(connection.getTransportInputStream());
        assertTrue("Invalid contents", Arrays.equals(content, result));
    }

    public void testGetHeaders() throws Exception {
        String headerName = "Header";
        String headerValue = "Value";
        request.addHeader(headerName, headerValue);
        TransportInputStream tis = connection.getTransportInputStream();
        Iterator iterator = tis.getHeaderNames();
        assertTrue("No headers found", iterator.hasNext());
        assertEquals("Invalid header", headerName, iterator.next());
        iterator = tis.getHeaders(headerName);
        assertTrue("No header values found", iterator.hasNext());
        assertEquals("Invalid header value", headerValue, iterator.next());
    }

    public void testWriteOutputStream() throws Exception {
        byte[] content = "content".getBytes("UTF-8");
        FileCopyUtils.copy(content, connection.getTransportOutputStream());
        assertTrue("Invalid contents", Arrays.equals(content, response.getContentAsByteArray()));
    }

    public void testAddHeaders() throws Exception {
        String headerName = "Header";
        String headerValue = "Value";
        connection.getTransportOutputStream().addHeader(headerName, headerValue);
        assertTrue("No header set", response.getHeaderNames().contains(headerName));
        assertEquals("Invalid header value set", Collections.singletonList(headerValue),
                response.getHeaders(headerName));
    }
}