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

package org.springframework.ws.soap.saaj;

import junit.framework.TestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;

public class SaajSoapMessageContextFactoryTest extends TestCase {

    private SaajSoapMessageContextFactory messageContextFactory;

    private static final String REQUEST =
            "<SOAP-ENV:Envelope xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/' SOAP-ENV:encodingStyle='http://schemas.xmlsoap.org/soap/encoding/'><SOAP-ENV:Body><m:GetLastTradePrice xmlns:m='Some-URI'><symbol>DIS</symbol></m:GetLastTradePrice></SOAP-ENV:Body></SOAP-ENV:Envelope>";

    protected void setUp() throws Exception {
        messageContextFactory = new SaajSoapMessageContextFactory();
    }

    public void testCreateMessageFromHttpServletRequest() throws Exception {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setMethod("POST");
        servletRequest.setContent(REQUEST.getBytes("UTF-8"));
        servletRequest.setContentType("text/xml; charset=\"utf-8\"");
        servletRequest.setCharacterEncoding("UTF-8");
        servletRequest.addHeader("SOAPAction", "\"Some-URI\"");

        MessageContext messageContext = messageContextFactory.createContext(servletRequest);
        SoapMessage requestMessage = (SoapMessage) messageContext.getRequest();
        assertNotNull("Request null", requestMessage);
        assertEquals("Invalid soap action", "\"Some-URI\"", requestMessage.getSoapAction());
    }

}