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

package org.springframework.ws.soap.axiom.support;

import java.io.StringWriter;
import java.util.Locale;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPMessage;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axiom.soap.impl.llom.soap11.SOAP11Factory;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.xml.sax.SaxUtils;

public class AxiomUtilsTest extends XMLTestCase {

    private OMElement element;

    protected void setUp() throws Exception {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace namespace = factory.createOMNamespace("http://www.springframework.org", "prefix");
        element = factory.createOMElement("element", namespace);
        XMLUnit.setIgnoreWhitespace(true);
    }

    public void testToNamespaceDeclared() throws Exception {
        QName qName = new QName(element.getNamespace().getNamespaceURI(), "localPart");
        OMNamespace namespace = AxiomUtils.toNamespace(qName, element);
        assertNotNull("Invalid namespace", namespace);
        assertEquals("Invalid namespace", qName.getNamespaceURI(), namespace.getNamespaceURI());
    }

    public void testToNamespaceUndeclared() throws Exception {
        QName qName = new QName("http://www.example.com", "localPart");
        OMNamespace namespace = AxiomUtils.toNamespace(qName, element);
        assertNotNull("Invalid namespace", namespace);
        assertEquals("Invalid namespace", qName.getNamespaceURI(), namespace.getNamespaceURI());
        assertFalse("Invalid prefix", "prefix".equals(namespace.getPrefix()));
    }

    public void testToNamespacePrefixDeclared() throws Exception {
        QName qName = new QName(element.getNamespace().getNamespaceURI(), "localPart", "prefix");
        OMNamespace namespace = AxiomUtils.toNamespace(qName, element);
        assertNotNull("Invalid namespace", namespace);
        assertEquals("Invalid namespace", qName.getNamespaceURI(), namespace.getNamespaceURI());
        assertEquals("Invalid prefix", "prefix", namespace.getPrefix());
    }

    public void testToNamespacePrefixUndeclared() throws Exception {
        QName qName = new QName("http://www.example.com", "localPart", "otherPrefix");
        OMNamespace namespace = AxiomUtils.toNamespace(qName, element);
        assertNotNull("Invalid namespace", namespace);
        assertEquals("Invalid namespace", qName.getNamespaceURI(), namespace.getNamespaceURI());
        assertEquals("Invalid prefix", qName.getPrefix(), namespace.getPrefix());
    }

    public void testToLanguage() throws Exception {
        assertEquals("Invalid conversion", "fr-CA", AxiomUtils.toLanguage(Locale.CANADA_FRENCH));
        assertEquals("Invalid conversion", "en", AxiomUtils.toLanguage(Locale.ENGLISH));
    }

    public void testToLocale() throws Exception {
        assertEquals("Invalid conversion", Locale.CANADA_FRENCH, AxiomUtils.toLocale("fr-CA"));
        assertEquals("Invalid conversion", Locale.ENGLISH, AxiomUtils.toLocale("en"));
    }

    public void testToDocument() throws Exception {
        Resource resource = new ClassPathResource("org/springframework/ws/soap/soap11/soap11.xml");

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document expected = documentBuilder.parse(SaxUtils.createInputSource(resource));

        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLStreamReader reader = inputFactory.createXMLStreamReader(resource.getInputStream());
        StAXSOAPModelBuilder builder =
                new StAXSOAPModelBuilder(reader, new SOAP11Factory(), SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        SOAPMessage soapMessage = builder.getSoapMessage();

        Document result = AxiomUtils.toDocument(soapMessage.getSOAPEnvelope());

        assertXMLEqual("Invalid document generated from SOAPEnvelope", expected, result);
    }

    public void testToEnvelope() throws Exception {
        Resource resource = new ClassPathResource("org/springframework/ws/soap/soap11/soap11.xml");

        byte[] buf = FileCopyUtils.copyToByteArray(resource.getFile());
        String expected = new String(buf, "UTF-8");

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(SaxUtils.createInputSource(resource));

        SOAPEnvelope envelope = AxiomUtils.toEnvelope(document);
        StringWriter writer = new StringWriter();
        envelope.serialize(writer);
        String result = writer.toString();

        assertXMLEqual("Invalid SOAPEnvelope generated from document", expected, result);
    }
}