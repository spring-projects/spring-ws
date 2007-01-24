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

import java.util.Arrays;
import java.util.Collections;

import junit.framework.TestCase;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.FileCopyUtils;

public class HttpServletTransportOutputStreamTest extends TestCase {

    private HttpServletTransportOutputStream tos;

    private MockHttpServletResponse response;

    protected void setUp() throws Exception {
        response = new MockHttpServletResponse();
        tos = new HttpServletTransportOutputStream(response);
    }

    public void testWriteOutputStream() throws Exception {
        byte[] content = "content".getBytes("UTF-8");
        FileCopyUtils.copy(content, tos);
        assertTrue("Invalid contents", Arrays.equals(content, response.getContentAsByteArray()));
    }

    public void testHeaders() throws Exception {
        String headerName = "Header";
        String headerValue = "Value";
        tos.addHeader(headerName, headerValue);
        assertTrue("No header set", response.getHeaderNames().contains(headerName));
        assertEquals("Invalid header value set", Collections.singletonList(headerValue),
                response.getHeaders(headerName));
    }

}