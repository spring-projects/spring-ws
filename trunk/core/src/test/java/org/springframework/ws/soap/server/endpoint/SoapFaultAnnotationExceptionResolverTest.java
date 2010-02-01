/*
 * Copyright 2007 the original author or authors.
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
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;

import junit.framework.TestCase;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;
import org.springframework.ws.soap.soap11.Soap11Fault;
import org.springframework.ws.soap.soap12.Soap12Fault;

public class SoapFaultAnnotationExceptionResolverTest extends TestCase {

    private SoapFaultAnnotationExceptionResolver resolver;

    protected void setUp() throws Exception {
        resolver = new SoapFaultAnnotationExceptionResolver();
    }

    public void testResolveExceptionClientSoap11() throws Exception {
        MessageFactory saajFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SoapMessageFactory factory = new SaajSoapMessageFactory(saajFactory);
        MessageContext context = new DefaultMessageContext(factory);

        boolean result = resolver.resolveException(context, null, new MyClientException());
        assertTrue("resolveException returns false", result);
        assertTrue("Context has no response", context.hasResponse());
        SoapMessage response = (SoapMessage) context.getResponse();
        assertTrue("Resonse has no fault", response.getSoapBody().hasFault());
        Soap11Fault fault = (Soap11Fault) response.getSoapBody().getFault();
        assertEquals("Invalid fault code on fault", SoapVersion.SOAP_11.getClientOrSenderFaultName(),
                fault.getFaultCode());
        assertEquals("Invalid fault string on fault", "Client error", fault.getFaultStringOrReason());
        assertNull("Detail on fault", fault.getFaultDetail());
    }

    public void testResolveExceptionSenderSoap12() throws Exception {
        MessageFactory saajFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SoapMessageFactory factory = new SaajSoapMessageFactory(saajFactory);
        MessageContext context = new DefaultMessageContext(factory);

        boolean result = resolver.resolveException(context, null, new MySenderException());
        assertTrue("resolveException returns false", result);
        assertTrue("Context has no response", context.hasResponse());
        SoapMessage response = (SoapMessage) context.getResponse();
        assertTrue("Resonse has no fault", response.getSoapBody().hasFault());
        Soap12Fault fault = (Soap12Fault) response.getSoapBody().getFault();
        assertEquals("Invalid fault code on fault", SoapVersion.SOAP_12.getClientOrSenderFaultName(),
                fault.getFaultCode());
        assertEquals("Invalid fault string on fault", "Sender error", fault.getFaultReasonText(Locale.ENGLISH));
        assertNull("Detail on fault", fault.getFaultDetail());
    }

    public void testResolveExceptionServerSoap11() throws Exception {
        MessageFactory saajFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SoapMessageFactory factory = new SaajSoapMessageFactory(saajFactory);
        MessageContext context = new DefaultMessageContext(factory);

        boolean result = resolver.resolveException(context, null, new MyServerException());
        assertTrue("resolveException returns false", result);
        assertTrue("Context has no response", context.hasResponse());
        SoapMessage response = (SoapMessage) context.getResponse();
        assertTrue("Resonse has no fault", response.getSoapBody().hasFault());
        Soap11Fault fault = (Soap11Fault) response.getSoapBody().getFault();
        assertEquals("Invalid fault code on fault", SoapVersion.SOAP_11.getServerOrReceiverFaultName(),
                fault.getFaultCode());
        assertEquals("Invalid fault string on fault", "Server error", fault.getFaultStringOrReason());
        assertNull("Detail on fault", fault.getFaultDetail());
    }

    public void testResolveExceptionReceiverSoap12() throws Exception {
        MessageFactory saajFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage message = saajFactory.createMessage();
        SoapMessageFactory factory = new SaajSoapMessageFactory(saajFactory);
        MessageContext context = new DefaultMessageContext(new SaajSoapMessage(message), factory);

        boolean result = resolver.resolveException(context, null, new MyReceiverException());
        assertTrue("resolveException returns false", result);
        assertTrue("Context has no response", context.hasResponse());
        SoapMessage response = (SoapMessage) context.getResponse();
        assertTrue("Resonse has no fault", response.getSoapBody().hasFault());
        Soap12Fault fault = (Soap12Fault) response.getSoapBody().getFault();
        assertEquals("Invalid fault code on fault", SoapVersion.SOAP_12.getServerOrReceiverFaultName(),
                fault.getFaultCode());
        assertEquals("Invalid fault string on fault", "Receiver error", fault.getFaultReasonText(Locale.ENGLISH));
        assertNull("Detail on fault", fault.getFaultDetail());
    }

    public void testResolveExceptionDefault() throws Exception {
        SoapFaultDefinition defaultFault = new SoapFaultDefinition();
        defaultFault.setFaultCode(SoapFaultDefinition.CLIENT);
        defaultFault.setFaultStringOrReason("faultstring");
        resolver.setDefaultFault(defaultFault);
        MessageFactory saajFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SoapMessageFactory factory = new SaajSoapMessageFactory(saajFactory);
        MessageContext context = new DefaultMessageContext(factory);

        boolean result = resolver.resolveException(context, null, new NonAnnotatedException());
        assertTrue("resolveException returns false", result);
        assertTrue("Context has no response", context.hasResponse());
        SoapMessage response = (SoapMessage) context.getResponse();
        assertTrue("Resonse has no fault", response.getSoapBody().hasFault());
        Soap11Fault fault = (Soap11Fault) response.getSoapBody().getFault();
        assertEquals("Invalid fault code on fault", SoapVersion.SOAP_11.getClientOrSenderFaultName(),
                fault.getFaultCode());
        assertEquals("Invalid fault string on fault", "faultstring", fault.getFaultStringOrReason());
        assertNull("Detail on fault", fault.getFaultDetail());
    }

    public void testResolveExceptionCustom() throws Exception {
        MessageFactory saajFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SoapMessageFactory factory = new SaajSoapMessageFactory(saajFactory);
        MessageContext context = new DefaultMessageContext(factory);

        boolean result = resolver.resolveException(context, null, new MyCustomException());
        assertTrue("resolveException returns false", result);
        assertTrue("Context has no response", context.hasResponse());
        SoapMessage response = (SoapMessage) context.getResponse();
        assertTrue("Resonse has no fault", response.getSoapBody().hasFault());
        Soap11Fault fault = (Soap11Fault) response.getSoapBody().getFault();
        assertEquals("Invalid fault code on fault", new QName("http://springframework.org/spring-ws", "Fault"),
                fault.getFaultCode());
        assertEquals("Invalid fault string on fault", "MyCustomException thrown", fault.getFaultStringOrReason());
        assertEquals("Invalid fault locale on fault", new Locale("nl"), fault.getFaultStringLocale());
    }

    public void testResolveExceptionInheritance() throws Exception {
        MessageFactory saajFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SoapMessageFactory factory = new SaajSoapMessageFactory(saajFactory);
        MessageContext context = new DefaultMessageContext(factory);

        boolean result = resolver.resolveException(context, null, new MySubClientException());
        assertTrue("resolveException returns false", result);
        assertTrue("Context has no response", context.hasResponse());
        SoapMessage response = (SoapMessage) context.getResponse();
        assertTrue("Resonse has no fault", response.getSoapBody().hasFault());
        Soap11Fault fault = (Soap11Fault) response.getSoapBody().getFault();
        assertEquals("Invalid fault code on fault", SoapVersion.SOAP_11.getClientOrSenderFaultName(),
                fault.getFaultCode());
        assertEquals("Invalid fault string on fault", "Client error", fault.getFaultStringOrReason());
        assertNull("Detail on fault", fault.getFaultDetail());
    }

    public void testResolveExceptionExceptionMessage() throws Exception {
        MessageFactory saajFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SoapMessageFactory factory = new SaajSoapMessageFactory(saajFactory);
        MessageContext context = new DefaultMessageContext(factory);

        boolean result = resolver.resolveException(context, null, new NoStringOrReasonException("Exception message"));
        assertTrue("resolveException returns false", result);
        assertTrue("Context has no response", context.hasResponse());
        SoapMessage response = (SoapMessage) context.getResponse();
        assertTrue("Resonse has no fault", response.getSoapBody().hasFault());
        Soap11Fault fault = (Soap11Fault) response.getSoapBody().getFault();
        assertEquals("Invalid fault code on fault", SoapVersion.SOAP_11.getClientOrSenderFaultName(),
                fault.getFaultCode());
        assertEquals("Invalid fault string on fault", "Exception message", fault.getFaultStringOrReason());
        assertNull("Detail on fault", fault.getFaultDetail());
    }

    @SoapFault(faultCode = FaultCode.CLIENT, faultStringOrReason = "Client error")
    public class MyClientException extends Exception {

    }

    public class MySubClientException extends MyClientException {

    }

    @SoapFault(faultCode = FaultCode.CLIENT)
    public class NoStringOrReasonException extends Exception {

        public NoStringOrReasonException(String message) {
            super(message);
        }
    }

    @SoapFault(faultCode = FaultCode.SENDER, faultStringOrReason = "Sender error")
    public class MySenderException extends Exception {

    }

    @SoapFault(faultCode = FaultCode.SERVER, faultStringOrReason = "Server error")
    public class MyServerException extends Exception {

    }

    @SoapFault(faultCode = FaultCode.RECEIVER, faultStringOrReason = "Receiver error")
    public class MyReceiverException extends Exception {

    }

    @SoapFault(faultCode = FaultCode.CUSTOM, customFaultCode = "{http://springframework.org/spring-ws}Fault",
            faultStringOrReason = "MyCustomException thrown", locale = "nl")
    public class MyCustomException extends Exception {

    }

    public class NonAnnotatedException extends Exception {

    }


}