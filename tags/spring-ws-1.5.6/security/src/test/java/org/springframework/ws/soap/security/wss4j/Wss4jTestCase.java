/*
 * Copyright 2008 the original author or authors.
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
package org.springframework.ws.soap.security.wss4j;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.context.DefaultMessageContext;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.axiom.AxiomSoapMessage;
import org.springframework.ws.soap.axiom.AxiomSoapMessageFactory;
import org.springframework.ws.soap.axiom.support.AxiomUtils;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.xml.xpath.XPathExpression;
import org.springframework.xml.xpath.XPathExpressionFactory;

public abstract class Wss4jTestCase extends TestCase {

    protected MessageFactory messageFactory;

    protected final boolean axiomTest = this.getClass().getSimpleName()
            .startsWith("Axiom");

    protected final boolean saajTest = this.getClass().getSimpleName()
            .startsWith("Saaj");

    protected Map namespaces;

    protected final void setUp() throws Exception {
        if (!axiomTest && !saajTest) {
            throw new IllegalArgumentException("test class name must statrt with either Axiom or Saaj");
        }
        messageFactory = MessageFactory.newInstance();
        namespaces = new HashMap();
        namespaces.put("SOAP-ENV", "http://schemas.xmlsoap.org/soap/envelope/");
        namespaces.put("wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
        namespaces.put("ds", "http://www.w3.org/2000/09/xmldsig#");
        namespaces.put("xenc", "http://www.w3.org/2001/04/xmlenc#");
//        namespaces.put("wsse11", "http://docs.oasis-open.org/wss/2005/xx/oasis-2005xx-wss-wssecurity-secext-1.1.xsd");
        namespaces.put("wsse11", "http://docs.oasis-open.org/wss/oasis-wss-wssecurity-secext-1.1.xsd");
        namespaces.put("echo", "http://www.springframework.org/spring-ws/samples/echo");
        namespaces.put("wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
        onSetup();
    }

    protected void assertXpathEvaluatesTo(String message,
                                          String expectedValue,
                                          String xpathExpression,
                                          Document document) {
        XPathExpression expression = XPathExpressionFactory
                .createXPathExpression(xpathExpression, namespaces);
        String actualValue = expression.evaluateAsString(document);
        assertEquals(message, expectedValue, actualValue);
    }

    protected void assertXpathExists(String message, String xpathExpression, Document document) {
        XPathExpression expression = XPathExpressionFactory
                .createXPathExpression(xpathExpression, namespaces);
        Node node = expression.evaluateAsNode(document);
        assertNotNull(message, node);
    }

    protected void assertXpathNotExists(String message, String xpathExpression, Document document) {
        XPathExpression expression = XPathExpressionFactory
                .createXPathExpression(xpathExpression, namespaces);
        Node node = expression.evaluateAsNode(document);
        assertNull(message, node);
    }

    protected SaajSoapMessage loadSaajMessage(String fileName) throws Exception {
        MimeHeaders mimeHeaders = new MimeHeaders();
        mimeHeaders.addHeader("Content-Type", "text/xml");
        Resource resource = new ClassPathResource(fileName, getClass());
        InputStream is = resource.getInputStream();
        try {
            assertTrue("Could not load SAAJ message [" + resource + "]", resource.exists());
            is = resource.getInputStream();
            return new SaajSoapMessage(messageFactory.createMessage(mimeHeaders, is));
        }
        finally {
            is.close();
        }
    }

    protected AxiomSoapMessage loadAxiomMessage(String fileName) throws Exception {
        Resource resource = new ClassPathResource(fileName, getClass());
        InputStream is = resource.getInputStream();
        try {
            assertTrue("Could not load Axiom message [" + resource + "]", resource.exists());
            is = resource.getInputStream();

            XMLStreamReader parser = XMLInputFactory.newInstance()
                    .createXMLStreamReader(is);
            StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(parser, null);
            org.apache.axiom.soap.SOAPMessage soapMessage = builder
                    .getSoapMessage();
            return new AxiomSoapMessage(soapMessage, "", true);
        }
        finally {
            is.close();
        }
    }

    protected Object getMessage(SoapMessage soapMessage) {
        if (soapMessage instanceof SaajSoapMessage) {
            return ((SaajSoapMessage) soapMessage).getSaajMessage();
        }
        if (soapMessage instanceof AxiomSoapMessage) {
            return ((AxiomSoapMessage) soapMessage).getAxiomMessage();

        }
        throw new IllegalArgumentException("Illegal message: " + soapMessage);
    }

    protected void setMessage(SoapMessage soapMessage, Object message) {
        if (soapMessage instanceof SaajSoapMessage) {
            ((SaajSoapMessage) soapMessage)
                    .setSaajMessage((SOAPMessage) message);
            return;
        }
        if (soapMessage instanceof AxiomSoapMessage) {
            ((AxiomSoapMessage) soapMessage)
                    .setAxiomMessage((org.apache.axiom.soap.SOAPMessage) message);
            return;
        }
        throw new IllegalArgumentException("Illegal message: " + message);
    }

    protected void onSetup() throws Exception {
    }

    protected SoapMessage loadMessage(String fileName) throws Exception {
        if (axiomTest) {
            return loadAxiomMessage(fileName);
        }
        if (saajTest) {
            return loadSaajMessage(fileName);
        }
        throw new IllegalArgumentException();
    }

    protected WebServiceMessageFactory getMessageFactory() throws Exception {
        if (axiomTest) {
            return new AxiomSoapMessageFactory();
        }
        if (saajTest) {
            return new SaajSoapMessageFactory(messageFactory);
        }
        throw new IllegalArgumentException();
    }

    protected Document getDocument(SoapMessage message) throws Exception {
        if (axiomTest) {
            return AxiomUtils.toDocument(((AxiomSoapMessage) message).getAxiomMessage().getSOAPEnvelope());
        }
        if (saajTest) {
            return ((SaajSoapMessage) message).getSaajMessage().getSOAPPart();
        }
        throw new IllegalArgumentException();
    }

    protected MessageContext getMessageContext(final SoapMessage response) throws Exception {
        return new DefaultMessageContext(response, getMessageFactory()) {
            public WebServiceMessage getResponse() {
                return response;
            }
        };
    }

}
