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

package org.springframework.ws.soap.server.endpoint;

import java.util.Locale;
import java.util.Properties;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;

import org.custommonkey.xmlunit.XMLTestCase;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapMessageException;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.soap11.Soap11Fault;
import org.springframework.ws.soap.soap12.Soap12Fault;

public class SoapFaultMappingExceptionResolverTest extends XMLTestCase {

    private SoapFaultMappingExceptionResolver resolver;

    protected void setUp() throws Exception {
        resolver = new SoapFaultMappingExceptionResolver();
    }

    public void testGetDepth() throws Exception {
        assertEquals("Invalid depth for Exception", 0, resolver.getDepth("java.lang.Exception", new Exception()));
        assertEquals("Invalid depth for IllegalArgumentException", 2,
                resolver.getDepth("java.lang.Exception", new IllegalArgumentException()));
        assertEquals("Invalid depth for IllegalStateException", -1,
                resolver.getDepth("IllegalArgumentException", new IllegalStateException()));
    }

    public void testResolveExceptionClientSoap11() throws Exception {
        Properties mappings = new Properties();
        mappings.setProperty(Exception.class.getName(), "SERVER, Server error");
        mappings.setProperty(RuntimeException.class.getName(), "CLIENT, Client error");
        resolver.setExceptionMappings(mappings);

        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SOAPMessage message = messageFactory.createMessage();
        SoapMessageFactory factory = new SaajSoapMessageFactory(messageFactory);
        MessageContext context = new DefaultMessageContext(new SaajSoapMessage(message), factory);

        boolean result = resolver.resolveException(context, null, new IllegalArgumentException("bla"));
        assertTrue("resolveException returns false", result);
        assertTrue("Context has no response", context.hasResponse());
        SoapMessage response = (SoapMessage) context.getResponse();
        assertTrue("Response has no fault", response.getSoapBody().hasFault());
        Soap11Fault fault = (Soap11Fault) response.getSoapBody().getFault();
        assertEquals("Invalid fault code on fault", SoapVersion.SOAP_11.getClientOrSenderFaultName(),
                fault.getFaultCode());
        assertEquals("Invalid fault string on fault", "Client error", fault.getFaultStringOrReason());
        assertNull("Detail on fault", fault.getFaultDetail());
    }

    public void testResolveExceptionSenderSoap12() throws Exception {
        Properties mappings = new Properties();
        mappings.setProperty(Exception.class.getName(), "RECEIVER, Receiver error, en");
        mappings.setProperty(RuntimeException.class.getName(), "SENDER, Sender error, en");
        resolver.setExceptionMappings(mappings);

        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage message = messageFactory.createMessage();
        SoapMessageFactory factory = new SaajSoapMessageFactory(messageFactory);
        MessageContext context = new DefaultMessageContext(new SaajSoapMessage(message), factory);

        boolean result = resolver.resolveException(context, null, new IllegalArgumentException("bla"));
        assertTrue("resolveException returns false", result);
        assertTrue("Context has no response", context.hasResponse());
        SoapMessage response = (SoapMessage) context.getResponse();
        assertTrue("Response has no fault", response.getSoapBody().hasFault());
        Soap12Fault fault = (Soap12Fault) response.getSoapBody().getFault();
        assertEquals("Invalid fault code on fault", SoapVersion.SOAP_12.getClientOrSenderFaultName(),
                fault.getFaultCode());
        assertEquals("Invalid fault string on fault", "Sender error", fault.getFaultReasonText(Locale.ENGLISH));
        assertNull("Detail on fault", fault.getFaultDetail());
    }

    public void testResolveExceptionServerSoap11() throws Exception {
        Properties mappings = new Properties();
        mappings.setProperty(Exception.class.getName(), "CLIENT, Client error");
        mappings.setProperty(RuntimeException.class.getName(), "SERVER, Server error");
        resolver.setExceptionMappings(mappings);

        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SOAPMessage message = messageFactory.createMessage();
        SoapMessageFactory factory = new SaajSoapMessageFactory(messageFactory);
        MessageContext context = new DefaultMessageContext(new SaajSoapMessage(message), factory);

        boolean result = resolver.resolveException(context, null, new IllegalArgumentException("bla"));
        assertTrue("resolveException returns false", result);
        assertTrue("Context has no response", context.hasResponse());
        SoapMessage response = (SoapMessage) context.getResponse();
        assertTrue("Response has no fault", response.getSoapBody().hasFault());
        Soap11Fault fault = (Soap11Fault) response.getSoapBody().getFault();
        assertEquals("Invalid fault code on fault", SoapVersion.SOAP_11.getServerOrReceiverFaultName(),
                fault.getFaultCode());
        assertEquals("Invalid fault string on fault", "Server error", fault.getFaultStringOrReason());
        assertNull("Detail on fault", fault.getFaultDetail());
    }

    public void testResolveExceptionReceiverSoap12() throws Exception {
        Properties mappings = new Properties();
        mappings.setProperty(Exception.class.getName(), "SENDER, Sender error");
        mappings.setProperty(RuntimeException.class.getName(), "RECEIVER, Receiver error");
        resolver.setExceptionMappings(mappings);

        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage message = messageFactory.createMessage();
        SoapMessageFactory factory = new SaajSoapMessageFactory(messageFactory);
        MessageContext context = new DefaultMessageContext(new SaajSoapMessage(message), factory);

        boolean result = resolver.resolveException(context, null, new IllegalArgumentException("bla"));
        assertTrue("resolveException returns false", result);
        assertTrue("Context has no response", context.hasResponse());
        SoapMessage response = (SoapMessage) context.getResponse();
        assertTrue("Response has no fault", response.getSoapBody().hasFault());
        Soap12Fault fault = (Soap12Fault) response.getSoapBody().getFault();
        assertEquals("Invalid fault code on fault", SoapVersion.SOAP_12.getServerOrReceiverFaultName(),
                fault.getFaultCode());
        assertEquals("Invalid fault string on fault", "Receiver error", fault.getFaultReasonText(Locale.ENGLISH));
        assertNull("Detail on fault", fault.getFaultDetail());
    }

    public void testResolveExceptionDefault() throws Exception {
        Properties mappings = new Properties();
        mappings.setProperty(SoapMessageException.class.getName(), "SERVER,Server error");
        resolver.setExceptionMappings(mappings);
        SoapFaultDefinition defaultFault = new SoapFaultDefinition();
        defaultFault.setFaultCode(SoapFaultDefinition.CLIENT);
        defaultFault.setFaultStringOrReason("faultstring");
        resolver.setDefaultFault(defaultFault);
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SOAPMessage message = messageFactory.createMessage();
        SoapMessageFactory factory = new SaajSoapMessageFactory(messageFactory);
        MessageContext context = new DefaultMessageContext(new SaajSoapMessage(message), factory);

        boolean result = resolver.resolveException(context, null, new IllegalArgumentException("bla"));
        assertTrue("resolveException returns false", result);
        assertTrue("Context has no response", context.hasResponse());
        SoapMessage response = (SoapMessage) context.getResponse();
        assertTrue("Response has no fault", response.getSoapBody().hasFault());
        Soap11Fault fault = (Soap11Fault) response.getSoapBody().getFault();
        assertEquals("Invalid fault code on fault", SoapVersion.SOAP_11.getClientOrSenderFaultName(),
                fault.getFaultCode());
        assertEquals("Invalid fault string on fault", "faultstring", fault.getFaultStringOrReason());
        assertNull("Detail on fault", fault.getFaultDetail());
    }


}