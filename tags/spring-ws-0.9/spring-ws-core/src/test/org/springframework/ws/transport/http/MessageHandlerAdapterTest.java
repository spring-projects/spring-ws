/*
 * Copyright 2005 the original author or authors.
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

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ws.NoEndpointFoundException;
import org.springframework.ws.context.MessageContextFactory;
import org.springframework.ws.endpoint.MessageEndpoint;
import org.springframework.ws.mock.soap.MockSoapMessageContext;

public class MessageHandlerAdapterTest extends TestCase {

    private static final String REQUEST = " <SOAP-ENV:Envelope\n" +
            "  xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
            "  SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" + "   <SOAP-ENV:Body>\n" +
            "       <m:GetLastTradePrice xmlns:m=\"Some-URI\">\n" + "           <symbol>DIS</symbol>\n" +
            "       </m:GetLastTradePrice>\n" + "   </SOAP-ENV:Body>\n" + "</SOAP-ENV:Envelope>";

    private MessageHandlerAdapter adapter;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    private MockControl endpointControl;

    private MessageEndpoint endpointMock;

    private MockControl factoryControl;

    private MessageContextFactory factoryMock;

    protected void setUp() throws Exception {
        adapter = new MessageHandlerAdapter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        endpointControl = MockControl.createControl(MessageEndpoint.class);
        endpointMock = (MessageEndpoint) endpointControl.getMock();
        factoryControl = MockControl.createControl(MessageContextFactory.class);
        factoryMock = (MessageContextFactory) factoryControl.getMock();
        adapter.setMessageContextFactory(factoryMock);
    }

    public void testHandleNonPost() throws Exception {
        request.setMethod("GET");
        endpointControl.replay();
        adapter.handle(request, response, endpointMock);
        endpointControl.verify();
    }

    public void testHandlePostNoResponse() throws Exception {
        request.setMethod("POST");
        request.setContent(REQUEST.getBytes("UTF-8"));
        request.setContentType("text/xml; charset=\"utf-8\"");
        request.setCharacterEncoding("UTF-8");

        endpointMock.invoke(null);
        endpointControl.setMatcher(MockControl.ALWAYS_MATCHER);

        MockSoapMessageContext messageContext = new MockSoapMessageContext();
        factoryControl.expectAndReturn(factoryMock.createContext(request), messageContext);

        endpointControl.replay();
        factoryControl.replay();
        adapter.handle(request, response, endpointMock);
        endpointControl.verify();
        factoryControl.verify();
        assertEquals("Invalid status code on response", HttpServletResponse.SC_OK, response.getStatus());
        assertEquals("Response written", 0, response.getContentAsString().length());
    }

    public void testHandlePostResponse() throws Exception {
        request.setMethod("POST");
        request.setContent(REQUEST.getBytes("UTF-8"));
        request.setContentType("text/xml; charset=\"utf-8\"");
        request.setCharacterEncoding("UTF-8");

        endpointMock.invoke(null);
        endpointControl.setMatcher(MockControl.ALWAYS_MATCHER);

        MockSoapMessageContext messageContext = new MockSoapMessageContext();
        messageContext.createResponse();
        factoryControl.expectAndReturn(factoryMock.createContext(request), messageContext);

        endpointControl.replay();
        factoryControl.replay();
        adapter.handle(request, response, endpointMock);
        endpointControl.verify();
        factoryControl.verify();
        assertEquals("Invalid status code on response", HttpServletResponse.SC_OK, response.getStatus());
        assertTrue("No Response written", response.getContentAsString().length() > 0);
    }

    public void testHandleNotFound() throws Exception {
        request.setMethod("POST");
        request.setContent(REQUEST.getBytes("UTF-8"));
        request.setContentType("text/xml; charset=\"utf-8\"");
        request.setCharacterEncoding("UTF-8");

        endpointMock.invoke(null);
        endpointControl.setMatcher(MockControl.ALWAYS_MATCHER);
        endpointControl.setThrowable(new NoEndpointFoundException(null));

        MockSoapMessageContext messageContext = new MockSoapMessageContext();
        factoryControl.expectAndReturn(factoryMock.createContext(request), messageContext);

        endpointControl.replay();
        factoryControl.replay();
        adapter.handle(request, response, endpointMock);
        endpointControl.verify();
        factoryControl.verify();

        assertEquals("No 404 returned", HttpServletResponse.SC_NOT_FOUND, response.getStatus());

    }

}
