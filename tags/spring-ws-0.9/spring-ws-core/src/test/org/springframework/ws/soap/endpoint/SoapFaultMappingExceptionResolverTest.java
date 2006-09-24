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
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.mock.soap.MockSoapMessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.SoapMessageException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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

    public void testResolveExceptionNoDefault() throws Exception {
        Properties mappings = new Properties();
        mappings.setProperty(Exception.class.getName(), "Server,Server error");
        mappings.setProperty(RuntimeException.class.getName(), "Client, Client error");
        resolver.setExceptionMappings(mappings);
        MessageContext messageContext = new MockSoapMessageContext();
        boolean result = resolver.resolveException(messageContext, null, new IllegalArgumentException("bla"));
        assertTrue("resolveException returns false", result);
        SoapMessage response = (SoapMessage) messageContext.getResponse();
        assertNotNull("Response not set", response);
        Element fault = response.getFault();
        assertNotNull("Returned message does not contain fault", fault);
        testFault(fault, "Client", "Client error");
    }

    public void testResolveExceptionDefault() throws Exception {
        Properties mappings = new Properties();
        mappings.setProperty(SoapMessageException.class.getName(), "Server,Server error");
        resolver.setExceptionMappings(mappings);
        SoapFaultDefinition defaultFault = new SoapFaultDefinition();
        defaultFault.setCode(new QName("MustUnderstand"));
        defaultFault.setString("Header not understood");
        resolver.setDefaultFault(defaultFault);
        MessageContext messageContext = new MockSoapMessageContext();
        boolean result = resolver.resolveException(messageContext, null, new IllegalArgumentException("bla"));
        assertTrue("resolveException returns false", result);
        SoapMessage response = (SoapMessage) messageContext.getResponse();
        assertNotNull("Response not set", response);
        Element fault = response.getFault();
        assertNotNull("Returned message does not contain fault", fault);
        testFault(fault, "MustUnderstand", "Header not understood");
    }

    private void testFault(Element fault, String code, String string) throws Exception {
        assertEquals("Invalid localName", "Fault", fault.getLocalName());
        assertEquals("Invalid uri", "http://schemas.xmlsoap.org/soap/envelope/", fault.getNamespaceURI());
        NodeList children = fault.getChildNodes();
        assertTrue("Invalid amount of children", children.getLength() >= 2);
        assertEquals("Invalid localName", "faultcode", children.item(0).getLocalName());
        assertEquals("Invalid faultCode", code, children.item(0).getTextContent());
        assertEquals("Invalid localName", "faultstring", children.item(1).getLocalName());
        assertEquals("Invalid faultCode", string, children.item(1).getTextContent());
    }

}