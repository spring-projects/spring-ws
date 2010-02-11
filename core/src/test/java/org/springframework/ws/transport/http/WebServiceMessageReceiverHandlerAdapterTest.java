/*
 * Copyright 2005-2010 the original author or authors.
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

import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.http.HttpServletResponse;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ws.FaultAwareWebServiceMessage;
import org.springframework.ws.NoEndpointFoundException;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.transport.WebServiceMessageReceiver;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;

public class WebServiceMessageReceiverHandlerAdapterTest {

    private static final String REQUEST = " <SOAP-ENV:Envelope\n" +
            "  xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
            "  SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" + "   <SOAP-ENV:Body>\n" +
            "       <m:GetLastTradePrice xmlns:m=\"Some-URI\">\n" + "           <symbol>DIS</symbol>\n" +
            "       </m:GetLastTradePrice>\n" + "   </SOAP-ENV:Body>\n" + "</SOAP-ENV:Envelope>";

    private WebServiceMessageReceiverHandlerAdapter adapter;

    private MockHttpServletRequest httpRequest;

    private MockHttpServletResponse httpResponse;

    private WebServiceMessageFactory factoryMock;

    private FaultAwareWebServiceMessage responseMock;

    private FaultAwareWebServiceMessage requestMock;

    @Before
    public void setUp() throws Exception {
        adapter = new WebServiceMessageReceiverHandlerAdapter();
        httpRequest = new MockHttpServletRequest();
        httpResponse = new MockHttpServletResponse();
        factoryMock = createMock(WebServiceMessageFactory.class);
        adapter.setMessageFactory(factoryMock);
        requestMock = createMock("request", FaultAwareWebServiceMessage.class);
        responseMock = createMock("response", FaultAwareWebServiceMessage.class);
    }

    @Test
    public void testHandleNonPost() throws Exception {
        httpRequest.setMethod(HttpTransportConstants.METHOD_GET);
        replayMockControls();
        WebServiceMessageReceiver endpoint = new WebServiceMessageReceiver() {

            public void receive(MessageContext messageContext) throws Exception {
            }
        };
        adapter.handle(httpRequest, httpResponse, endpoint);
        Assert.assertEquals("METHOD_NOT_ALLOWED expected", HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                httpResponse.getStatus());
        verifyMockControls();
    }

    @Test
    public void testHandlePostNoResponse() throws Exception {
        httpRequest.setMethod(HttpTransportConstants.METHOD_POST);
        httpRequest.setContent(REQUEST.getBytes("UTF-8"));
        httpRequest.setContentType("text/xml; charset=\"utf-8\"");
        httpRequest.setCharacterEncoding("UTF-8");
        expect(factoryMock.createWebServiceMessage(isA(InputStream.class))).andReturn(responseMock);

        replayMockControls();
        WebServiceMessageReceiver endpoint = new WebServiceMessageReceiver() {

            public void receive(MessageContext messageContext) throws Exception {
            }
        };

        adapter.handle(httpRequest, httpResponse, endpoint);

        Assert.assertEquals("Invalid status code on response", HttpServletResponse.SC_ACCEPTED,
                httpResponse.getStatus());
        Assert.assertEquals("Response written", 0, httpResponse.getContentAsString().length());
        verifyMockControls();
    }

    @Test
    public void testHandlePostResponse() throws Exception {
        httpRequest.setMethod(HttpTransportConstants.METHOD_POST);
        httpRequest.setContent(REQUEST.getBytes("UTF-8"));
        httpRequest.setContentType("text/xml; charset=\"utf-8\"");
        httpRequest.setCharacterEncoding("UTF-8");
        expect(factoryMock.createWebServiceMessage(isA(InputStream.class))).andReturn(requestMock);
        expect(factoryMock.createWebServiceMessage()).andReturn(responseMock);
        expect(responseMock.hasFault()).andReturn(false);
        responseMock.writeTo(isA(OutputStream.class));

        replayMockControls();
        WebServiceMessageReceiver endpoint = new WebServiceMessageReceiver() {

            public void receive(MessageContext messageContext) throws Exception {
                messageContext.getResponse();
            }
        };

        adapter.handle(httpRequest, httpResponse, endpoint);

        Assert.assertEquals("Invalid status code on response", HttpServletResponse.SC_OK, httpResponse.getStatus());
        verifyMockControls();
    }

    @Test
    public void testHandlePostFault() throws Exception {
        httpRequest.setMethod(HttpTransportConstants.METHOD_POST);
        httpRequest.setContent(REQUEST.getBytes("UTF-8"));
        httpRequest.setContentType("text/xml; charset=\"utf-8\"");
        httpRequest.setCharacterEncoding("UTF-8");
        expect(factoryMock.createWebServiceMessage(isA(InputStream.class))).andReturn(requestMock);
        expect(factoryMock.createWebServiceMessage()).andReturn(responseMock);
        expect(responseMock.hasFault()).andReturn(true);
        responseMock.writeTo(isA(OutputStream.class));

        replayMockControls();
        WebServiceMessageReceiver endpoint = new WebServiceMessageReceiver() {

            public void receive(MessageContext messageContext) throws Exception {
                messageContext.getResponse();
            }
        };

        adapter.handle(httpRequest, httpResponse, endpoint);

        Assert.assertEquals("Invalid status code on response", HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                httpResponse.getStatus());
        verifyMockControls();
    }

    @Test
    public void testHandleNotFound() throws Exception {
        httpRequest.setMethod(HttpTransportConstants.METHOD_POST);
        httpRequest.setContent(REQUEST.getBytes("UTF-8"));
        httpRequest.setContentType("text/xml; charset=\"utf-8\"");
        httpRequest.setCharacterEncoding("UTF-8");
        expect(factoryMock.createWebServiceMessage(isA(InputStream.class))).andReturn(requestMock);

        replayMockControls();

        WebServiceMessageReceiver endpoint = new WebServiceMessageReceiver() {

            public void receive(MessageContext messageContext) throws Exception {
                throw new NoEndpointFoundException(messageContext.getRequest());
            }
        };

        adapter.handle(httpRequest, httpResponse, endpoint);
        Assert.assertEquals("No 404 returned", HttpServletResponse.SC_NOT_FOUND, httpResponse.getStatus());

        verifyMockControls();

    }

    private void replayMockControls() {
        replay(factoryMock, requestMock, responseMock);
    }

    private void verifyMockControls() {
        verify(factoryMock, requestMock, responseMock);
    }

}
