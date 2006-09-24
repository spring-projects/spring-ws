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

package org.springframework.ws.soap.saaj;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.custommonkey.xmlunit.XMLTestCase;
import org.w3c.dom.Element;

public class SaajSoapMessageTest extends XMLTestCase {

    private SaajSoapMessage message;

    private SOAPMessage saajMessage;

    protected void setUp() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        saajMessage = messageFactory.createMessage();
        this.message = new SaajSoapMessage(saajMessage);
    }

    public void testGetFault() throws Exception {
        assertNull("Message has fault", message.getFault());
        saajMessage.getSOAPBody().addFault();
        assertNotNull("Message has no fault", message.getFault());
    }

    public void testGetPayloadSource() throws Exception {
        saajMessage.getSOAPBody().addChildElement("child");
        Source source = message.getPayloadSource();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(source, new StreamResult(writer));
        assertXMLEqual("Invalid source", "<child/>", writer.toString());
    }

    public void testGetPayloadResult() throws Exception {
        StringReader reader = new StringReader("<child/>");
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        Result result = message.getPayloadResult();
        transformer.transform(new StreamSource(reader), result);
        assertTrue("No child nodes created", saajMessage.getSOAPBody().hasChildNodes());
        assertEquals("Invalid child node created", "child", saajMessage.getSOAPBody().getFirstChild().getLocalName());
    }

    public void testGetHeaderElements() throws Exception {
        Name headerName = saajMessage.getSOAPPart().getEnvelope().createName("header", "prefix", "namespace");
        saajMessage.getSOAPHeader().addChildElement(headerName);
        Element[] headers = message.getHeaderElements();
        assertEquals("Invalid amount of headers", 1, headers.length);
        assertEquals("Invalid header", "header", headers[0].getLocalName());
        assertEquals("Invalid header", "prefix", headers[0].getPrefix());
        assertEquals("Invalid header", "namespace", headers[0].getNamespaceURI());
    }

    public void testGetHeaderElementsQName() throws Exception {
        Name header1Name = saajMessage.getSOAPPart().getEnvelope().createName("header1", "prefix", "namespace");
        saajMessage.getSOAPHeader().addChildElement(header1Name);
        Name header2Name = saajMessage.getSOAPPart().getEnvelope().createName("header2", "prefix", "namespace");
        saajMessage.getSOAPHeader().addChildElement(header2Name);
        QName qName = new QName("namespace", "header1", "prefix");
        Element[] headers = message.getHeaderElements(qName);
        assertEquals("Invalid amount of headers", 1, headers.length);
        assertEquals("Invalid header", "header1", headers[0].getLocalName());
        assertEquals("Invalid header", "prefix", headers[0].getPrefix());
        assertEquals("Invalid header", "namespace", headers[0].getNamespaceURI());
    }

    public void testGetMimeHeader() throws Exception {
        saajMessage.getMimeHeaders().addHeader("SOAPAction", "value");
        assertEquals("Invalid mime header value", "value", message.getSoapAction());
    }

    public void testAddFault() throws Exception {
        Element fault = message.addFault(new QName("Server"), "string", null);
        assertNotNull("Fault is null", fault);
        assertTrue("Message has not fault", saajMessage.getSOAPBody().hasFault());
        SOAPFault soapFault = saajMessage.getSOAPBody().getFault();
        assertEquals("Invalid Fault code", "SOAP-ENV:Server", soapFault.getFaultCode());
    }

    public void testAddHeaderElement() throws Exception {
        Element header = message.addHeaderElement(new QName("namespace", "localpart", "prefix"), true, "actor");
        assertNotNull("Header is null", header);
        SOAPHeaderElement saajHeader = (SOAPHeaderElement) saajMessage.getSOAPHeader().getFirstChild();
        assertEquals("Invalid header localname", saajHeader.getLocalName(), "localpart");
        assertEquals("Invalid header prefix", saajHeader.getPrefix(), "prefix");
        assertEquals("Invalid header namespace", saajHeader.getNamespaceURI(), "namespace");
        assertTrue("Invalid header mustUnderstand", saajHeader.getMustUnderstand());
        assertEquals("Invalid header actor", "actor", saajHeader.getActor());
    }

}