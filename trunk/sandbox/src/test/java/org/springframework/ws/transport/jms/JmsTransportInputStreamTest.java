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

package org.springframework.ws.transport.jms;

import java.util.Collections;
import java.util.Iterator;
import javax.jms.BytesMessage;

import junit.framework.TestCase;
import org.easymock.MockControl;

public class JmsTransportInputStreamTest extends TestCase {

    private JmsTransportInputStream tis;

    private MockControl messageControl;

    private BytesMessage messageMock;

    protected void setUp() throws Exception {
        messageControl = MockControl.createControl(BytesMessage.class);
        messageMock = (BytesMessage) messageControl.getMock();
        tis = new JmsTransportInputStream(messageMock);
    }

    public void testHeaders() throws Exception {
        String headerName = "Header";
        messageControl.expectAndReturn(messageMock.getPropertyNames(),
                Collections.enumeration(Collections.singleton(headerName)));
        String headerValue = "Value";
        messageControl.expectAndReturn(messageMock.getStringProperty(headerName), headerValue);
        messageControl.replay();
        Iterator iterator = tis.getHeaderNames();
        assertTrue("No headers found", iterator.hasNext());
        assertEquals("Invalid header", headerName, iterator.next());
        iterator = tis.getHeaders(headerName);
        assertTrue("No header values found", iterator.hasNext());
        assertEquals("Invalid header value", headerValue, iterator.next());
        messageControl.verify();
    }

}