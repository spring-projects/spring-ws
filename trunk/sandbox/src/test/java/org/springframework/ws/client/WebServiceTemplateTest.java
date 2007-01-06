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

package org.springframework.ws.client;

import java.io.IOException;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.transport.WebServiceMessageSender;

public class WebServiceTemplateTest extends TestCase {

    private WebServiceTemplate template;

    private MockControl factoryControl;

    private WebServiceMessageFactory factoryMock;

    private MockControl messageControl;

    private WebServiceMessage requestMock;

    private WebServiceMessage responseMock;

    protected void setUp() throws Exception {
        template = new WebServiceTemplate();
        factoryControl = MockControl.createControl(WebServiceMessageFactory.class);
        factoryMock = (WebServiceMessageFactory) factoryControl.getMock();
        template.setMessageFactory(factoryMock);
        messageControl = MockControl.createControl(WebServiceMessage.class);
        requestMock = (WebServiceMessage) messageControl.getMock();
        responseMock = (WebServiceMessage) messageControl.getMock();
    }

    public void testMarshalAndSendNoMarshallerSet() throws Exception {
        template.setMarshaller(null);
        replayMockControls();
        try {
            template.marshalAndSend(new Object());
            fail("IllegalStateException expected");
        }
        catch (IllegalStateException ex) {
            // expected behavior
        }
        verifyMockControls();
    }

    public void testMarshalAndSendNoUnmarshallerSet() throws Exception {
        template.setUnmarshaller(null);
        replayMockControls();
        try {
            template.marshalAndSend(new Object());
            fail("IllegalStateException expected");
        }
        catch (IllegalStateException ex) {
            // expected behavior
        }
        verifyMockControls();
    }

    public void testSendAndReceiveMessageResponse() throws Exception {
        factoryControl.expectAndReturn(factoryMock.createWebServiceMessage(), requestMock);
        factoryControl.expectAndReturn(factoryMock.createWebServiceMessage(), responseMock);
        template.setMessageSender(new WebServiceMessageSender() {

            public void sendAndReceive(MessageContext messageContext) throws IOException {
                assertEquals("Invalid request message", requestMock, messageContext.getRequest());
                messageContext.getResponse();
            }
        });
        replayMockControls();
        WebServiceMessage response = template.sendAndReceive(new WebServiceMessageCallback() {
            public void doInMessage(WebServiceMessage message) throws IOException {
                assertEquals("Invalid request message", requestMock, message);
            }
        });
        assertEquals("No response", responseMock, response);
        verifyMockControls();
    }

    private void replayMockControls() {
        factoryControl.replay();
        messageControl.replay();
    }

    private void verifyMockControls() {
        factoryControl.verify();
        messageControl.verify();
    }


}