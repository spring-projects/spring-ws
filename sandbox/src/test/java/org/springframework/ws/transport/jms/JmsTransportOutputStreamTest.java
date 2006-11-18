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

import javax.jms.BytesMessage;
import javax.jms.Session;

import junit.framework.TestCase;
import org.easymock.MockControl;

public class JmsTransportOutputStreamTest extends TestCase {

    private JmsTransportOutputStream tos;

    private MockControl messageControl;

    private BytesMessage messageMock;

    private MockControl sessionControl;

    private Session sessionMock;

    protected void setUp() throws Exception {
        messageControl = MockControl.createControl(BytesMessage.class);
        messageMock = (BytesMessage) messageControl.getMock();
        sessionControl = MockControl.createControl(Session.class);
        sessionMock = (Session) sessionControl.getMock();
        tos = new JmsTransportOutputStream(sessionMock);
    }

    public void testHeaders() throws Exception {
        sessionControl.expectAndReturn(sessionMock.createBytesMessage(), messageMock);
        String headerName = "Header";
        String headerValue = "Value";
        messageMock.setStringProperty(headerName, headerValue);
        sessionControl.replay();
        messageControl.replay();
        tos.addHeader(headerName, headerValue);
        sessionControl.verify();
        messageControl.verify();
    }

}