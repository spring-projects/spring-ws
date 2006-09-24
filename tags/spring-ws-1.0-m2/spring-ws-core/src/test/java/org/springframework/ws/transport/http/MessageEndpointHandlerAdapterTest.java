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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ws.NoEndpointFoundException;
import org.springframework.ws.context.MessageContextFactory;
import org.springframework.ws.endpoint.MessageEndpoint;
import org.springframework.ws.mock.MockTransportContext;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.context.SoapMessageContext;

public class MessageEndpointHandlerAdapterTest extends TestCase {

    private static final String REQUEST = " <SOAP-ENV:Envelope\n" +
            "  xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
            "  SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" + "   <SOAP-ENV:Body>\n" +
            "       <m:GetLastTradePrice xmlns:m=\"Some-URI\">\n" + "           <symbol>DIS</symbol>\n" +
            "       </m:GetLastTradePrice>\n" + "   </SOAP-ENV:Body>\n" + "</SOAP-ENV:Envelope>";

    private MessageEndpointHandlerAdapter adapter;

    private MockHttpServletRequest httpRequest;

    private MockHttpServletResponse httpResponse;

    private MockControl endpointControl;

    private MessageEndpoint endpointMock;

    private MockControl factoryControl;

    private MessageContextFactory factoryMock;

    private MockControl contextControl;

    private SoapMessageContext contextMock;

    private MockControl messageControl;

    private SoapMessage messageMock;

    private MockControl bodyControl;

    private SoapBody bodyMock;

    protected void setUp() throws Exception {
        adapter = new MessageEndpointHandlerAdapter();
        httpRequest = new MockHttpServletRequest();
        httpResponse = new MockHttpServletResponse();
        endpointControl = MockControl.createControl(MessageEndpoint.class);
        endpointMock = (MessageEndpoint) endpointControl.getMock();
        factoryControl = MockControl.createControl(MessageContextFactory.class);
        factoryMock = (MessageContextFactory) factoryControl.getMock();
        adapter.setMessageContextFactory(factoryMock);
        contextControl = MockControl.createControl(SoapMessageContext.class);
        contextMock = (SoapMessageContext) contextControl.getMock();
        messageControl = MockControl.createControl(SoapMessage.class);
        messageMock = (SoapMessage) messageControl.getMock();
        bodyControl = MockControl.createControl(SoapBody.class);
        bodyMock = (SoapBody) bodyControl.getMock();
    }

    public void testHandleNonPost() throws Exception {
        httpRequest.setMethod("GET");
        endpointControl.replay();
        try {
            adapter.handle(httpRequest, httpResponse, endpointMock);
            fail("ServletException expected");
        }
        catch (ServletException ex) {
            // expected
        }
        endpointControl.verify();
    }

    public void testHandlePostNoResponse() throws Exception {
        httpRequest.setMethod("POST");
        httpRequest.setContent(MessageEndpointHandlerAdapterTest.REQUEST.getBytes("UTF-8"));
        httpRequest.setContentType("text/xml; charset=\"utf-8\"");
        httpRequest.setCharacterEncoding("UTF-8");
        endpointMock.invoke(null);
        endpointControl.setMatcher(MockControl.ALWAYS_MATCHER);
        factoryMock.createContext(new MockTransportContext());
        factoryControl.setMatcher(MockControl.ALWAYS_MATCHER);
        factoryControl.setReturnValue(contextMock);
        contextControl.expectAndReturn(contextMock.hasResponse(), false);

        replayMockControls();

        adapter.handle(httpRequest, httpResponse, endpointMock);

        assertEquals("Invalid status code on response", HttpServletResponse.SC_NO_CONTENT, httpResponse.getStatus());
        assertEquals("Response written", 0, httpResponse.getContentAsString().length());
        verifyMockControls();
    }

    public void testHandlePostResponse() throws Exception {
        httpRequest.setMethod("POST");
        httpRequest.setContent(MessageEndpointHandlerAdapterTest.REQUEST.getBytes("UTF-8"));
        httpRequest.setContentType("text/xml; charset=\"utf-8\"");
        httpRequest.setCharacterEncoding("UTF-8");
        endpointMock.invoke(null);
        endpointControl.setMatcher(MockControl.ALWAYS_MATCHER);
        factoryMock.createContext(new MockTransportContext());
        factoryControl.setMatcher(MockControl.ALWAYS_MATCHER);
        factoryControl.setReturnValue(contextMock);
        contextControl.expectAndReturn(contextMock.hasResponse(), true);
        contextControl.expectAndReturn(contextMock.getResponse(), messageMock);
        messageControl.expectAndReturn(messageMock.getSoapBody(), bodyMock);
        bodyControl.expectAndReturn(bodyMock.hasFault(), false);
        contextMock.sendResponse(new HttpTransportResponse(httpResponse));
        contextControl.setMatcher(MockControl.ALWAYS_MATCHER);

        replayMockControls();

        adapter.handle(httpRequest, httpResponse, endpointMock);

        assertEquals("Invalid status code on response", HttpServletResponse.SC_OK, httpResponse.getStatus());
        verifyMockControls();
    }

    public void testHandlePostFault() throws Exception {
        httpRequest.setMethod("POST");
        httpRequest.setContent(MessageEndpointHandlerAdapterTest.REQUEST.getBytes("UTF-8"));
        httpRequest.setContentType("text/xml; charset=\"utf-8\"");
        httpRequest.setCharacterEncoding("UTF-8");
        endpointMock.invoke(null);
        endpointControl.setMatcher(MockControl.ALWAYS_MATCHER);
        factoryMock.createContext(new MockTransportContext());
        factoryControl.setMatcher(MockControl.ALWAYS_MATCHER);
        factoryControl.setReturnValue(contextMock);
        contextControl.expectAndReturn(contextMock.hasResponse(), true);
        contextControl.expectAndReturn(contextMock.getResponse(), messageMock);
        messageControl.expectAndReturn(messageMock.getSoapBody(), bodyMock);
        bodyControl.expectAndReturn(bodyMock.hasFault(), true);
        contextMock.sendResponse(null);
        contextControl.setMatcher(MockControl.ALWAYS_MATCHER);

        replayMockControls();

        adapter.handle(httpRequest, httpResponse, endpointMock);

        assertEquals("Invalid status code on response",
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                httpResponse.getStatus());
        verifyMockControls();
    }

    public void testHandleNotFound() throws Exception {
        httpRequest.setMethod("POST");
        httpRequest.setContent(MessageEndpointHandlerAdapterTest.REQUEST.getBytes("UTF-8"));
        httpRequest.setContentType("text/xml; charset=\"utf-8\"");
        httpRequest.setCharacterEncoding("UTF-8");
        endpointMock.invoke(null);
        endpointControl.setMatcher(MockControl.ALWAYS_MATCHER);
        endpointControl.setThrowable(new NoEndpointFoundException(null));
        factoryMock.createContext(new MockTransportContext());
        factoryControl.setMatcher(MockControl.ALWAYS_MATCHER);
        factoryControl.setReturnValue(contextMock);

        replayMockControls();

        adapter.handle(httpRequest, httpResponse, endpointMock);
        assertEquals("No 404 returned", HttpServletResponse.SC_NOT_FOUND, httpResponse.getStatus());

        verifyMockControls();

    }

    private void replayMockControls() {
        endpointControl.replay();
        factoryControl.replay();
        contextControl.replay();
        messageControl.replay();
        bodyControl.replay();
    }

    private void verifyMockControls() {
        endpointControl.verify();
        factoryControl.verify();
        contextControl.verify();
        messageControl.verify();
        bodyControl.verify();
    }

}
