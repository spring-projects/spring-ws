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
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

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
            template.sendAndReceive(new Object());
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
            template.sendAndReceive(new Object());
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
        template.setMessageSender(new ResponseMessageSender());
        replayMockControls();
        WebServiceMessage response = template.sendAndReceive(new WebServiceMessageCallback() {
            public void doInMessage(WebServiceMessage message) throws IOException {
                assertEquals("Invalid request message", requestMock, message);
            }
        });
        assertEquals("Invalid response", responseMock, response);
        verifyMockControls();
    }

    public void testSendAndReceiveMessageNoResponse() throws Exception {
        factoryControl.expectAndReturn(factoryMock.createWebServiceMessage(), requestMock);
        template.setMessageSender(new NoResponseMessageSender());
        replayMockControls();
        WebServiceMessage response = template.sendAndReceive(new WebServiceMessageCallback() {
            public void doInMessage(WebServiceMessage message) throws IOException {
                assertEquals("Invalid request message", requestMock, message);
            }
        });
        assertNull("No response", response);
        verifyMockControls();
    }

    public void testSendAndReceiveSourceResponse() throws Exception {
        factoryControl.expectAndReturn(factoryMock.createWebServiceMessage(), requestMock);
        factoryControl.expectAndReturn(factoryMock.createWebServiceMessage(), responseMock);
        messageControl.expectAndReturn(requestMock.getPayloadResult(), new StringResult());
        Source expected = new StringSource("<response/>");
        messageControl.expectAndReturn(responseMock.getPayloadSource(), expected);
        template.setMessageSender(new ResponseMessageSender());
        replayMockControls();
        Source response = template.sendAndReceive(new StringSource("<request />"));
        assertEquals("Invalid response", expected, response);
    }

    public void testSendAndReceiveSourceNoResponse() throws Exception {
        factoryControl.expectAndReturn(factoryMock.createWebServiceMessage(), requestMock);
        messageControl.expectAndReturn(requestMock.getPayloadResult(), new StringResult());
        template.setMessageSender(new NoResponseMessageSender());
        replayMockControls();
        Source response = template.sendAndReceive(new StringSource("<request />"));
        assertNull("Invalid response", response);
    }

    public void testSendAndReceiveMarshallResponse() throws Exception {
        template.setMessageSender(new ResponseMessageSender());
        MockControl marshallerControl = MockControl.createControl(Marshaller.class);
        Marshaller marshallerMock = (Marshaller) marshallerControl.getMock();
        template.setMarshaller(marshallerMock);
        MockControl unmarshallerControl = MockControl.createControl(Unmarshaller.class);
        Unmarshaller unmarshallerMock = (Unmarshaller) unmarshallerControl.getMock();
        template.setUnmarshaller(unmarshallerMock);
        Object request = new Object();
        Object expected = new Object();
        Result requestResult = new StringResult();
        Source responseSource = new StringSource("");

        factoryControl.expectAndReturn(factoryMock.createWebServiceMessage(), requestMock);
        messageControl.expectAndReturn(requestMock.getPayloadResult(), requestResult);
        marshallerMock.marshal(request, requestResult);
        factoryControl.expectAndReturn(factoryMock.createWebServiceMessage(), responseMock);
        messageControl.expectAndReturn(responseMock.getPayloadSource(), responseSource);
        unmarshallerControl.expectAndReturn(unmarshallerMock.unmarshal(responseSource), expected);

        replayMockControls();
        marshallerControl.replay();
        unmarshallerControl.replay();

        Object response = template.sendAndReceive(request);

        assertEquals("Invalid response", expected, response);

        verifyMockControls();
        marshallerControl.verify();
        unmarshallerControl.verify();
    }

    public void testSendAndReceiveMarshallNoResponse() throws Exception {
        template.setMessageSender(new NoResponseMessageSender());
        MockControl marshallerControl = MockControl.createControl(Marshaller.class);
        Marshaller marshallerMock = (Marshaller) marshallerControl.getMock();
        template.setMarshaller(marshallerMock);
        MockControl unmarshallerControl = MockControl.createControl(Unmarshaller.class);
        Unmarshaller unmarshallerMock = (Unmarshaller) unmarshallerControl.getMock();
        template.setUnmarshaller(unmarshallerMock);
        Object request = new Object();
        Result requestResult = new StringResult();

        factoryControl.expectAndReturn(factoryMock.createWebServiceMessage(), requestMock);
        messageControl.expectAndReturn(requestMock.getPayloadResult(), requestResult);
        marshallerMock.marshal(request, requestResult);

        replayMockControls();
        marshallerControl.replay();

        Object response = template.sendAndReceive(request);

        assertNull("Invalid response", response);

        verifyMockControls();
        marshallerControl.verify();
    }

    private void replayMockControls() {
        factoryControl.replay();
        messageControl.replay();
    }

    private void verifyMockControls() {
        factoryControl.verify();
        messageControl.verify();
    }

    private class NoResponseMessageSender implements WebServiceMessageSender {

        public void sendAndReceive(MessageContext messageContext) throws IOException {
            assertEquals("Invalid request message", requestMock, messageContext.getRequest());
        }
    }

    private class ResponseMessageSender implements WebServiceMessageSender {

        public void sendAndReceive(MessageContext messageContext) throws IOException {
            assertEquals("Invalid request message", requestMock, messageContext.getRequest());
            messageContext.getResponse();
        }
    }
}