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
import java.util.Iterator;

import junit.framework.TestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.FileCopyUtils;

public class HttpServletTransportInputStreamTest extends TestCase {

    private HttpServletTransportInputStream tis;

    private MockHttpServletRequest request;

    private byte[] content;

    protected void setUp() throws Exception {
        request = new MockHttpServletRequest();
        content = "content".getBytes("UTF-8");
        request.setContent(content);
        tis = new HttpServletTransportInputStream(request);
    }

    public void testReadInputStream() throws Exception {
        byte[] result = FileCopyUtils.copyToByteArray(tis);
        assertTrue("Invalid contents", Arrays.equals(content, result));
    }

    public void testHeaders() throws Exception {
        String headerName = "Header";
        String headerValue = "Value";
        request.addHeader(headerName, headerValue);
        Iterator iterator = tis.getHeaderNames();
        assertTrue("No headers found", iterator.hasNext());
        assertEquals("Invalid header", headerName, iterator.next());
        iterator = tis.getHeaders(headerName);
        assertTrue("No header values found", iterator.hasNext());
        assertEquals("Invalid header value", headerValue, iterator.next());
    }

}