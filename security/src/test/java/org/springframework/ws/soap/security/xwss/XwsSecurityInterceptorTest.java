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

package org.springframework.ws.soap.security.xwss;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;

import junit.framework.TestCase;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.security.WsSecurityValidationException;

public class XwsSecurityInterceptorTest extends TestCase {

    private MessageFactory messageFactory;

    protected void setUp() throws Exception {
        messageFactory = MessageFactory.newInstance();
    }

    public void testhandleRequest() throws Exception {
        final SOAPMessage request = messageFactory.createMessage();
        final SOAPMessage validatedRequest = messageFactory.createMessage();
        XwsSecurityInterceptor interceptor = new XwsSecurityInterceptor() {

            protected void secureMessage(SoapMessage soapMessage) throws XwsSecuritySecurementException {
                fail("secure not expected");
            }

            protected void validateMessage(SoapMessage message) throws WsSecurityValidationException {
                SaajSoapMessage saajSoapMessage = (SaajSoapMessage) message;
                assertEquals("Invalid message", request, saajSoapMessage.getSaajMessage());
                saajSoapMessage.setSaajMessage(validatedRequest);
            }

        };
        MessageContext context =
                new DefaultMessageContext(new SaajSoapMessage(request), new SaajSoapMessageFactory(messageFactory));
        interceptor.handleRequest(context, null);
        assertEquals("Invalid request", validatedRequest, ((SaajSoapMessage) context.getRequest()).getSaajMessage());
    }

    public void testhandleResponse() throws Exception {
        final SOAPMessage securedResponse = messageFactory.createMessage();
        XwsSecurityInterceptor interceptor = new XwsSecurityInterceptor() {

            protected void secureMessage(SoapMessage message) throws XwsSecuritySecurementException {
                SaajSoapMessage saajSoapMessage = (SaajSoapMessage) message;
                saajSoapMessage.setSaajMessage(securedResponse);
            }

            protected void validateMessage(SoapMessage soapMessage) throws WsSecurityValidationException {
                fail("validate not expected");
            }

        };
        SOAPMessage request = messageFactory.createMessage();
        MessageContext context =
                new DefaultMessageContext(new SaajSoapMessage(request), new SaajSoapMessageFactory(messageFactory));
        context.getResponse();
        interceptor.handleResponse(context, null);
        assertEquals("Invalid response", securedResponse, ((SaajSoapMessage) context.getResponse()).getSaajMessage());
    }

}
