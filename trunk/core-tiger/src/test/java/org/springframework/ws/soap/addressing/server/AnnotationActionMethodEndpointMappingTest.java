/*
 * Copyright ${YEAR} the original author or authors.
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

package org.springframework.ws.soap.addressing.server;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;

import org.springframework.context.support.StaticApplicationContext;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInvocationChain;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.soap.addressing.server.annotation.Action;
import org.springframework.ws.soap.addressing.server.annotation.Address;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

public class AnnotationActionMethodEndpointMappingTest extends XMLTestCase {

    private StaticApplicationContext applicationContext;

    private AnnotationActionEndpointMapping mapping;

    private MessageFactory messageFactory;

    protected void setUp() throws Exception {
        messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        XMLUnit.setIgnoreWhitespace(true);
        applicationContext = new StaticApplicationContext();
        applicationContext.registerSingleton("mapping", AnnotationActionEndpointMapping.class);
        mapping = (AnnotationActionEndpointMapping) applicationContext.getBean("mapping");
    }

    public void testNoAddress() throws Exception {
        applicationContext.registerSingleton("endpoint", Endpoint1.class);
        applicationContext.refresh();
        MessageContext messageContext = createMessageContext();

        EndpointInvocationChain chain = mapping.getEndpoint(messageContext);
        assertNotNull("MethodEndpoint not registered", chain);
        MethodEndpoint expected = new MethodEndpoint(applicationContext.getBean("endpoint"), "doIt", new Class[0]);
        assertEquals("Invalid endpoint registered", expected, chain.getEndpoint());
    }

    public void testAddress() throws Exception {
        applicationContext.registerSingleton("endpoint", Endpoint2.class);
        applicationContext.refresh();
        MessageContext messageContext = createMessageContext();

        EndpointInvocationChain chain = mapping.getEndpoint(messageContext);
        assertNotNull("MethodEndpoint not registered", chain);
        MethodEndpoint expected = new MethodEndpoint(applicationContext.getBean("endpoint"), "doIt", new Class[0]);
        assertEquals("Invalid endpoint registered", expected, chain.getEndpoint());
    }

    private MessageContext createMessageContext() throws SOAPException, IOException {
        MimeHeaders mimeHeaders = new MimeHeaders();
        mimeHeaders.addHeader("Content-Type", " application/soap+xml");
        InputStream is = getClass().getResourceAsStream("valid.xml");
        assertNotNull("Could not load valid.xml", is);
        try {
            SaajSoapMessage message = new SaajSoapMessage(messageFactory.createMessage(mimeHeaders, is));
            return new DefaultMessageContext(message, new SaajSoapMessageFactory(messageFactory));
        }
        finally {
            is.close();
        }
    }

    @Endpoint
    private static class Endpoint1 {

        @Action("http://fabrikam123.example/mail/Delete")
        public void doIt() {

        }
    }

    @Endpoint
    @Address("mailto:joe@fabrikam123.example")
    private static class Endpoint2 {

        @Action("http://fabrikam123.example/mail/Delete")
        public void doIt() {

        }
    }
}