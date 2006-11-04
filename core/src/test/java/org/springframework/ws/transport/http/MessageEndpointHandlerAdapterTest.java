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
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.endpoint.MessageEndpoint;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapMessage;

public class MessageEndpointHandlerAdapterTest extends TestCase {

    private static final String REQUEST = " <SOAP-ENV:Envelope\n" +
            "  xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
            "  SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" + "   <SOAP-ENV:Body>\n" +
            "       <m:GetLastTradePrice xmlns:m=\"Some-URI\">\n" + "           <symbol>DIS</symbol>\n" +
            "       </m:GetLastTradePrice>\n" + "   </SOAP-ENV:Body>\n" + "</SOAP-ENV:Envelope>";

    private MessageEndpointHandlerAdapter adapter;

    private MockHttpServletRequest httpRequest;

    private MockHttpServletResponse httpResponse;

    private MockControl factoryControl;

    private WebServiceMessageFactory factoryMock;

    private MockControl messageControl;

    private SoapMessage responseMock;

    private MockControl bodyControl;

    private SoapBody bodyMock;

    private SoapMessage requestMock;

    protected void setUp() throws Exception {
        adapter = new MessageEndpointHandlerAdapter();
        httpRequest = new MockHttpServletRequest();
        httpResponse = new MockHttpServletResponse();
        factoryControl = MockControl.createControl(WebServiceMessageFactory.class);
        factoryMock = (WebServiceMessageFactory) factoryControl.getMock();
        adapter.setMessageFactory(factoryMock);
        messageControl = MockControl.createControl(SoapMessage.class);
        requestMock = (SoapMessage) messageControl.getMock();
        responseMock = (SoapMessage) messageControl.getMock();
        bodyControl = MockControl.createControl(SoapBody.class);
        bodyMock = (SoapBody) bodyControl.getMock();
    }

    public void testHandleNonPost() throws Exception {
        httpRequest.setMethod("GET");
        replayMockControls();
        MessageEndpoint endpoint = new MessageEndpoint() {

            public void invoke(MessageContext messageContext) throws Exception {
            }
        };
        try {
            adapter.handle(httpRequest, httpResponse, endpoint);
            fail("ServletException expected");
        }
        catch (ServletException ex) {
            // expected
        }
        verifyMockControls();
    }

    public void testHandlePostNoResponse() throws Exception {
        httpRequest.setMethod("POST");
        httpRequest.setContent(MessageEndpointHandlerAdapterTest.REQUEST.getBytes("UTF-8"));
        httpRequest.setContentType("text/xml; charset=\"utf-8\"");
        httpRequest.setCharacterEncoding("UTF-8");
        factoryMock.createWebServiceMessage(new HttpServletTransportInputStream(httpRequest));
        factoryControl.setMatcher(MockControl.ALWAYS_MATCHER);
        factoryControl.setReturnValue(responseMock);

        replayMockControls();
        MessageEndpoint endpoint = new MessageEndpoint() {

            public void invoke(MessageContext messageContext) throws Exception {
            }
        };

        adapter.handle(httpRequest, httpResponse, endpoint);

        assertEquals("Invalid status code on response", HttpServletResponse.SC_NO_CONTENT, httpResponse.getStatus());
        assertEquals("Response written", 0, httpResponse.getContentAsString().length());
        verifyMockControls();
    }

    public void testHandlePostResponse() throws Exception {
        httpRequest.setMethod("POST");
        httpRequest.setContent(MessageEndpointHandlerAdapterTest.REQUEST.getBytes("UTF-8"));
        httpRequest.setContentType("text/xml; charset=\"utf-8\"");
        httpRequest.setCharacterEncoding("UTF-8");
        factoryMock.createWebServiceMessage(new HttpServletTransportInputStream(httpRequest));
        factoryControl.setMatcher(MockControl.ALWAYS_MATCHER);
        factoryControl.setReturnValue(requestMock);
        factoryControl.expectAndReturn(factoryMock.createWebServiceMessage(), responseMock);
        messageControl.expectAndReturn(responseMock.getSoapBody(), bodyMock);
        bodyControl.expectAndReturn(bodyMock.hasFault(), false);
        responseMock.writeTo(new HttpServletTransportOutputStream(httpResponse));
        messageControl.setMatcher(MockControl.ALWAYS_MATCHER);

        replayMockControls();
        MessageEndpoint endpoint = new MessageEndpoint() {

            public void invoke(MessageContext messageContext) throws Exception {
                messageContext.getResponse();
            }
        };

        adapter.handle(httpRequest, httpResponse, endpoint);

        assertEquals("Invalid status code on response", HttpServletResponse.SC_OK, httpResponse.getStatus());
        verifyMockControls();
    }

    public void testHandlePostFault() throws Exception {
        httpRequest.setMethod("POST");
        httpRequest.setContent(MessageEndpointHandlerAdapterTest.REQUEST.getBytes("UTF-8"));
        httpRequest.setContentType("text/xml; charset=\"utf-8\"");
        httpRequest.setCharacterEncoding("UTF-8");
        factoryMock.createWebServiceMessage(new HttpServletTransportInputStream(httpRequest));
        factoryControl.setMatcher(MockControl.ALWAYS_MATCHER);
        factoryControl.setReturnValue(requestMock);
        factoryControl.expectAndReturn(factoryMock.createWebServiceMessage(), responseMock);
        messageControl.expectAndReturn(responseMock.getSoapBody(), bodyMock);
        bodyControl.expectAndReturn(bodyMock.hasFault(), true);
        responseMock.writeTo(new HttpServletTransportOutputStream(httpResponse));
        messageControl.setMatcher(MockControl.ALWAYS_MATCHER);

        replayMockControls();
        MessageEndpoint endpoint = new MessageEndpoint() {

            public void invoke(MessageContext messageContext) throws Exception {
                messageContext.getResponse();
            }
        };

        adapter.handle(httpRequest, httpResponse, endpoint);

        assertEquals("Invalid status code on response", HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                httpResponse.getStatus());
        verifyMockControls();
    }

    public void testHandleNotFound() throws Exception {
        httpRequest.setMethod("POST");
        httpRequest.setContent(MessageEndpointHandlerAdapterTest.REQUEST.getBytes("UTF-8"));
        httpRequest.setContentType("text/xml; charset=\"utf-8\"");
        httpRequest.setCharacterEncoding("UTF-8");
        factoryMock.createWebServiceMessage(new HttpServletTransportInputStream(httpRequest));
        factoryControl.setMatcher(MockControl.ALWAYS_MATCHER);
        factoryControl.setReturnValue(requestMock);

        replayMockControls();

        MessageEndpoint endpoint = new MessageEndpoint() {

            public void invoke(MessageContext messageContext) throws Exception {
                throw new NoEndpointFoundException(messageContext.getRequest());
            }
        };

        adapter.handle(httpRequest, httpResponse, endpoint);
        assertEquals("No 404 returned", HttpServletResponse.SC_NOT_FOUND, httpResponse.getStatus());

        verifyMockControls();

    }

    private void replayMockControls() {
        factoryControl.replay();
        messageControl.replay();
        bodyControl.replay();
    }

    private void verifyMockControls() {
        factoryControl.verify();
        messageControl.verify();
        bodyControl.verify();
    }

}
