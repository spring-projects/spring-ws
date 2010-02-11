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

package org.springframework.ws.soap.security.xwss;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;

import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.security.WsSecurityValidationException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class XwsSecurityInterceptorTest {

    private MessageFactory messageFactory;

    @Before
    public void setUp() throws Exception {
        messageFactory = MessageFactory.newInstance();
    }

    @Test
    public void testHandleServerRequest() throws Exception {
        final SOAPMessage request = messageFactory.createMessage();
        final SOAPMessage validatedRequest = messageFactory.createMessage();
        XwsSecurityInterceptor interceptor = new XwsSecurityInterceptor() {

            @Override
            protected void secureMessage(SoapMessage soapMessage, MessageContext messageContext)
                    throws XwsSecuritySecurementException {
                Assert.fail("secure not expected");
            }

            @Override
            protected void validateMessage(SoapMessage message, MessageContext messageContext)
                    throws WsSecurityValidationException {
                SaajSoapMessage saajSoapMessage = (SaajSoapMessage) message;
                Assert.assertEquals("Invalid message", request, saajSoapMessage.getSaajMessage());
                saajSoapMessage.setSaajMessage(validatedRequest);
            }

        };
        MessageContext context =
                new DefaultMessageContext(new SaajSoapMessage(request), new SaajSoapMessageFactory(messageFactory));
        interceptor.handleRequest(context, null);
        Assert.assertEquals("Invalid request", validatedRequest,
                ((SaajSoapMessage) context.getRequest()).getSaajMessage());
    }

    @Test
    public void testHandleServerResponse() throws Exception {
        final SOAPMessage securedResponse = messageFactory.createMessage();
        XwsSecurityInterceptor interceptor = new XwsSecurityInterceptor() {

            @Override
            protected void secureMessage(SoapMessage message, MessageContext messageContext)
                    throws XwsSecuritySecurementException {
                SaajSoapMessage saajSoapMessage = (SaajSoapMessage) message;
                saajSoapMessage.setSaajMessage(securedResponse);
            }

            @Override
            protected void validateMessage(SoapMessage soapMessage, MessageContext messageContext)
                    throws WsSecurityValidationException {
                Assert.fail("validate not expected");
            }

        };
        SOAPMessage request = messageFactory.createMessage();
        MessageContext context =
                new DefaultMessageContext(new SaajSoapMessage(request), new SaajSoapMessageFactory(messageFactory));
        context.getResponse();
        interceptor.handleResponse(context, null);
        Assert.assertEquals("Invalid response", securedResponse,
                ((SaajSoapMessage) context.getResponse()).getSaajMessage());
    }

    @Test
    public void testhandleClientRequest() throws Exception {
        final SOAPMessage request = messageFactory.createMessage();
        final SOAPMessage securedRequest = messageFactory.createMessage();
        XwsSecurityInterceptor interceptor = new XwsSecurityInterceptor() {

            @Override
            protected void secureMessage(SoapMessage soapMessage, MessageContext messageContext)
                    throws XwsSecuritySecurementException {
                SaajSoapMessage saajSoapMessage = (SaajSoapMessage) soapMessage;
                Assert.assertEquals("Invalid message", request, saajSoapMessage.getSaajMessage());
                saajSoapMessage.setSaajMessage(securedRequest);
            }

            @Override
            protected void validateMessage(SoapMessage message, MessageContext messageContext)
                    throws WsSecurityValidationException {
                Assert.fail("validate not expected");
            }

        };
        MessageContext context =
                new DefaultMessageContext(new SaajSoapMessage(request), new SaajSoapMessageFactory(messageFactory));
        interceptor.handleRequest(context);
        Assert.assertEquals("Invalid request", securedRequest,
                ((SaajSoapMessage) context.getRequest()).getSaajMessage());
    }

    @Test
    public void testHandleClientResponse() throws Exception {
        final SOAPMessage validatedResponse = messageFactory.createMessage();
        XwsSecurityInterceptor interceptor = new XwsSecurityInterceptor() {

            @Override
            protected void secureMessage(SoapMessage message, MessageContext messageContext)
                    throws XwsSecuritySecurementException {
                Assert.fail("secure not expected");
            }

            @Override
            protected void validateMessage(SoapMessage soapMessage, MessageContext messageContext)
                    throws WsSecurityValidationException {
                SaajSoapMessage saajSoapMessage = (SaajSoapMessage) soapMessage;
                saajSoapMessage.setSaajMessage(validatedResponse);
            }

        };
        SOAPMessage request = messageFactory.createMessage();
        MessageContext context =
                new DefaultMessageContext(new SaajSoapMessage(request), new SaajSoapMessageFactory(messageFactory));
        context.getResponse();
        interceptor.handleResponse(context);
        Assert.assertEquals("Invalid response", validatedResponse,
                ((SaajSoapMessage) context.getResponse()).getSaajMessage());
    }

}