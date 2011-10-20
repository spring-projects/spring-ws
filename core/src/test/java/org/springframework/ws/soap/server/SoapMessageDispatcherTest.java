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

package org.springframework.ws.soap.server;

import java.util.Iterator;
import java.util.Locale;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;

import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.soap11.Soap11Fault;
import org.springframework.ws.soap.soap12.Soap12Fault;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;

public class SoapMessageDispatcherTest {

    private SoapMessageDispatcher dispatcher;

    private SoapEndpointInterceptor interceptorMock;

    @Before
    public void setUp() throws Exception {
        interceptorMock = createMock(SoapEndpointInterceptor.class);
        dispatcher = new SoapMessageDispatcher();
    }

    @Test
    public void testProcessMustUnderstandHeadersUnderstoodSoap11() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SOAPMessage request = messageFactory.createMessage();
        SOAPHeaderElement header =
                request.getSOAPHeader().addHeaderElement(new QName("http://www.springframework.org", "Header"));
        header.setActor(SOAPConstants.URI_SOAP_ACTOR_NEXT);
        header.setMustUnderstand(true);
        SoapMessageFactory factory = new SaajSoapMessageFactory(messageFactory);
        MessageContext context = new DefaultMessageContext(new SaajSoapMessage(request), factory);
        expect(interceptorMock.understands(isA(SoapHeaderElement.class))).andReturn(true);

        replay(interceptorMock);

        SoapEndpointInvocationChain chain =
                new SoapEndpointInvocationChain(new Object(), new SoapEndpointInterceptor[]{interceptorMock});

        boolean result = dispatcher.handleRequest(chain, context);
        Assert.assertTrue("Header not understood", result);

        verify(interceptorMock);
    }

    @Test
    public void testProcessMustUnderstandHeadersUnderstoodSoap12() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage request = messageFactory.createMessage();
        SOAPHeaderElement header =
                request.getSOAPHeader().addHeaderElement(new QName("http://www.springframework.org", "Header"));
        header.setMustUnderstand(true);
        header.setRole(SOAPConstants.URI_SOAP_1_2_ROLE_NEXT);
        SoapMessageFactory factory = new SaajSoapMessageFactory(messageFactory);
        MessageContext context = new DefaultMessageContext(new SaajSoapMessage(request), factory);
        expect(interceptorMock.understands(isA(SoapHeaderElement.class))).andReturn(true);

        replay(interceptorMock);

        SoapEndpointInvocationChain chain =
                new SoapEndpointInvocationChain(new Object(), new SoapEndpointInterceptor[]{interceptorMock});

        boolean result = dispatcher.handleRequest(chain, context);
        Assert.assertTrue("Header not understood", result);

        verify(interceptorMock);
    }

    @Test
    public void testProcessMustUnderstandHeadersNotUnderstoodSoap11() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SOAPMessage request = messageFactory.createMessage();
        SOAPHeaderElement header = request.getSOAPHeader()
                .addHeaderElement(new QName("http://www.springframework.org", "Header", "spring-ws"));
        header.setActor(SOAPConstants.URI_SOAP_ACTOR_NEXT);
        header.setMustUnderstand(true);
        SoapMessageFactory factory = new SaajSoapMessageFactory(messageFactory);
        MessageContext context = new DefaultMessageContext(new SaajSoapMessage(request), factory);
        expect(interceptorMock.understands(isA(SoapHeaderElement.class))).andReturn(false);

        replay(interceptorMock);

        SoapEndpointInvocationChain chain =
                new SoapEndpointInvocationChain(new Object(), new SoapEndpointInterceptor[]{interceptorMock});

        boolean result = dispatcher.handleRequest(chain, context);
        Assert.assertFalse("Header understood", result);
        Assert.assertTrue("Context has no response", context.hasResponse());
        SoapBody responseBody = ((SoapMessage) context.getResponse()).getSoapBody();
        Assert.assertTrue("Response body has no fault", responseBody.hasFault());
        Soap11Fault fault = (Soap11Fault) responseBody.getFault();
        Assert.assertEquals("Invalid fault code", new QName(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, "MustUnderstand"),
                fault.getFaultCode());
        Assert.assertEquals("Invalid fault string", SoapMessageDispatcher.DEFAULT_MUST_UNDERSTAND_FAULT_STRING,
                fault.getFaultStringOrReason());
        Assert.assertEquals("Invalid fault string locale", Locale.ENGLISH, fault.getFaultStringLocale());

        verify(interceptorMock);
    }

    @Test
    public void testProcessMustUnderstandHeadersNotUnderstoodSoap12() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage request = messageFactory.createMessage();
        SOAPHeaderElement header = request.getSOAPHeader()
                .addHeaderElement(new QName("http://www.springframework.org", "Header", "spring-ws"));
        header.setMustUnderstand(true);
        header.setRole(SOAPConstants.URI_SOAP_1_2_ROLE_NEXT);
        SoapMessageFactory factory = new SaajSoapMessageFactory(messageFactory);
        MessageContext context = new DefaultMessageContext(new SaajSoapMessage(request), factory);
        expect(interceptorMock.understands(isA(SoapHeaderElement.class))).andReturn(false);

        replay(interceptorMock);

        SoapEndpointInvocationChain chain =
                new SoapEndpointInvocationChain(new Object(), new SoapEndpointInterceptor[]{interceptorMock});

        boolean result = dispatcher.handleRequest(chain, context);
        Assert.assertFalse("Header understood", result);
        Assert.assertTrue("Context has no response", context.hasResponse());
        SoapMessage response = (SoapMessage) context.getResponse();
        SoapBody responseBody = response.getSoapBody();
        Assert.assertTrue("Response body has no fault", responseBody.hasFault());
        Soap12Fault fault = (Soap12Fault) responseBody.getFault();
        Assert.assertEquals("Invalid fault code", new QName(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE, "MustUnderstand"),
                fault.getFaultCode());
        Assert.assertEquals("Invalid fault string", SoapMessageDispatcher.DEFAULT_MUST_UNDERSTAND_FAULT_STRING,
                fault.getFaultReasonText(Locale.ENGLISH));
        SoapHeader responseHeader = response.getSoapHeader();
        Iterator<SoapHeaderElement> iterator = responseHeader.examineAllHeaderElements();
        Assert.assertTrue("Response header has no elements", iterator.hasNext());
        SoapHeaderElement headerElement = iterator.next();
        Assert.assertEquals("No NotUnderstood header",
                new QName(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE, "NotUnderstood"), headerElement.getName());

        verify(interceptorMock);
    }

    @Test
    public void testProcessMustUnderstandHeadersForActorSoap11() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SOAPMessage request = messageFactory.createMessage();
        SOAPHeaderElement header = request.getSOAPHeader()
                .addHeaderElement(new QName("http://www.springframework.org", "Header", "spring-ws"));
        String headerActor = "http://www/springframework.org/role";
        header.setActor(headerActor);
        header.setMustUnderstand(true);
        SoapMessageFactory factory = new SaajSoapMessageFactory(messageFactory);
        MessageContext context = new DefaultMessageContext(new SaajSoapMessage(request), factory);
        expect(interceptorMock.understands(isA(SoapHeaderElement.class))).andReturn(true);

        replay(interceptorMock);

        SoapEndpointInvocationChain chain = new SoapEndpointInvocationChain(new Object(),
                new SoapEndpointInterceptor[]{interceptorMock}, new String[]{headerActor}, true);

        boolean result = dispatcher.handleRequest(chain, context);
        Assert.assertTrue("actor-specific header not understood", result);

        verify(interceptorMock);
    }

    @Test
    public void testProcessMustUnderstandHeadersForRoleSoap12() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage request = messageFactory.createMessage();
        SOAPHeaderElement header = request.getSOAPHeader()
                .addHeaderElement(new QName("http://www.springframework.org", "Header", "spring-ws"));
        String headerRole = "http://www/springframework.org/role";
        header.setRole(headerRole);
        header.setMustUnderstand(true);
        SoapMessageFactory factory = new SaajSoapMessageFactory(messageFactory);
        MessageContext context = new DefaultMessageContext(new SaajSoapMessage(request), factory);
        expect(interceptorMock.understands(isA(SoapHeaderElement.class))).andReturn(true);

        replay(interceptorMock);

        SoapEndpointInvocationChain chain = new SoapEndpointInvocationChain(new Object(),
                new SoapEndpointInterceptor[]{interceptorMock}, new String[]{headerRole}, true);

        boolean result = dispatcher.handleRequest(chain, context);
        Assert.assertTrue("role-specific header not understood", result);

        verify(interceptorMock);
    }

    @Test
    public void testProcessNoHeader() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SOAPMessage request = messageFactory.createMessage();
        request.getSOAPHeader().detachNode();
        SoapMessageFactory factory = new SaajSoapMessageFactory(messageFactory);
        MessageContext context = new DefaultMessageContext(new SaajSoapMessage(request), factory);
        replay(interceptorMock);

        SoapEndpointInvocationChain chain = new SoapEndpointInvocationChain(new Object(),
                new SoapEndpointInterceptor[]{interceptorMock}, new String[]{"role"}, true);

        boolean result = dispatcher.handleRequest(chain, context);
        Assert.assertTrue("Invalid result", result);
        verify(interceptorMock);
    }

    @Test
    public void testProcessMustUnderstandHeadersNoInterceptors() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SOAPMessage request = messageFactory.createMessage();
        SOAPHeaderElement header =
                request.getSOAPHeader().addHeaderElement(new QName("http://www.springframework.org", "Header"));
        header.setActor(SOAPConstants.URI_SOAP_ACTOR_NEXT);
        header.setMustUnderstand(true);
        SoapMessageFactory factory = new SaajSoapMessageFactory(messageFactory);
        MessageContext context = new DefaultMessageContext(new SaajSoapMessage(request), factory);
        replay(interceptorMock);

        SoapEndpointInvocationChain chain = new SoapEndpointInvocationChain(new Object(), null);

        boolean result = dispatcher.handleRequest(chain, context);
        Assert.assertFalse("Header understood", result);
        verify(interceptorMock);
    }

}