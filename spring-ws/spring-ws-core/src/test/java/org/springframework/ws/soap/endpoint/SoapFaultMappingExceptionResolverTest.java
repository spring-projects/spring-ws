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

package org.springframework.ws.soap.endpoint;

import java.util.Properties;

import javax.xml.namespace.QName;

import org.custommonkey.xmlunit.XMLTestCase;
import org.easymock.MockControl;

import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapMessageException;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.context.SoapMessageContext;

public class SoapFaultMappingExceptionResolverTest extends XMLTestCase {

    private SoapFaultMappingExceptionResolver resolver;

    private MockControl contextControl;

    private SoapMessageContext contextMock;

    private MockControl messageControl;

    private SoapMessage messageMock;

    private MockControl bodyControl;

    private SoapBody bodyMock;

    private MockControl faultControl;

    private SoapFault faultMock;

    protected void setUp() throws Exception {
        resolver = new SoapFaultMappingExceptionResolver();
        contextControl = MockControl.createControl(SoapMessageContext.class);
        contextMock = (SoapMessageContext) contextControl.getMock();
        messageControl = MockControl.createControl(SoapMessage.class);
        messageMock = (SoapMessage) messageControl.getMock();
        bodyControl = MockControl.createControl(SoapBody.class);
        bodyMock = (SoapBody) bodyControl.getMock();
        faultControl = MockControl.createControl(SoapFault.class);
        faultMock = (SoapFault) faultControl.getMock();

    }

    public void testGetDepth() throws Exception {
        assertEquals("Invalid depth for Exception", 0, resolver.getDepth("java.lang.Exception", new Exception()));
        assertEquals("Invalid depth for IllegalArgumentException", 2,
                resolver.getDepth("java.lang.Exception", new IllegalArgumentException()));
        assertEquals("Invalid depth for IllegalStateException", -1,
                resolver.getDepth("IllegalArgumentException", new IllegalStateException()));
    }

    public void testResolveExceptionSender() throws Exception {
        Properties mappings = new Properties();
        mappings.setProperty(Exception.class.getName(), "RECEIVER,Receiver error");
        mappings.setProperty(RuntimeException.class.getName(), "SENDER, Sender error");
        resolver.setExceptionMappings(mappings);
        contextControl.expectAndReturn(contextMock.createSoapResponse(), messageMock);
        messageControl.expectAndReturn(messageMock.getSoapBody(), bodyMock);
        messageControl.expectAndReturn(messageMock.getVersion(), SoapVersion.SOAP_11);
        bodyControl.expectAndReturn(bodyMock.addFault(SoapVersion.SOAP_11.getSenderFaultName(), "Sender error"),
                faultMock);

        replayMockControls();

        boolean result = resolver.resolveException(contextMock, null, new IllegalArgumentException("bla"));
        assertTrue("resolveException returns false", result);

        verifyMockControls();
    }

    public void testResolveExceptionReceiver() throws Exception {
        Properties mappings = new Properties();
        mappings.setProperty(Exception.class.getName(), "SENDER,Sender error");
        mappings.setProperty(RuntimeException.class.getName(), "RECEIVER, Receiver error, en");
        resolver.setExceptionMappings(mappings);
        contextControl.expectAndReturn(contextMock.createSoapResponse(), messageMock);
        messageControl.expectAndReturn(messageMock.getSoapBody(), bodyMock);
        messageControl.expectAndReturn(messageMock.getVersion(), SoapVersion.SOAP_11);
        bodyControl.expectAndReturn(bodyMock.addFault(SoapVersion.SOAP_11.getReceiverFaultName(), "Receiver error"),
                faultMock);

        replayMockControls();

        boolean result = resolver.resolveException(contextMock, null, new IllegalArgumentException("bla"));
        assertTrue("resolveException returns false", result);

        verifyMockControls();
    }

    public void testResolveExceptionDefault() throws Exception {
        Properties mappings = new Properties();
        mappings.setProperty(SoapMessageException.class.getName(), "RECEIVER,Receiver error");
        resolver.setExceptionMappings(mappings);
        SoapFaultDefinition defaultFault = new SoapFaultDefinition();
        QName faultCode = new QName("namespace", "faultcode", "prefix");
        defaultFault.setFaultCode(faultCode);
        defaultFault.setFaultString("faultstring");
        resolver.setDefaultFault(defaultFault);
        contextControl.expectAndReturn(contextMock.createSoapResponse(), messageMock);
        messageControl.expectAndReturn(messageMock.getSoapBody(), bodyMock);
        bodyControl.expectAndReturn(bodyMock.addFault(faultCode, "faultstring"), faultMock);

        replayMockControls();

        boolean result = resolver.resolveException(contextMock, null, new IllegalArgumentException("bla"));
        assertTrue("resolveException returns false", result);

        verifyMockControls();
    }

    private void replayMockControls() {
        contextControl.replay();
        messageControl.replay();
        bodyControl.replay();
        faultControl.replay();
    }

    private void verifyMockControls() {
        contextControl.verify();
        messageControl.verify();
        bodyControl.verify();
        faultControl.verify();
    }


}