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

package org.springframework.ws.soap.saaj;

import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import junit.framework.TestCase;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.xml.transform.StringSource;

public class SaajSoapHeaderTest extends TestCase {

    private SOAPHeader saajHeader;

    private SoapHeader header;

    private SOAPEnvelope saajEnvelope;

    protected void setUp() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage saajMessage = messageFactory.createMessage();
        saajEnvelope = saajMessage.getSOAPPart().getEnvelope();
        this.saajHeader = saajMessage.getSOAPHeader();
        SaajSoapMessage saajSoapMessage = new SaajSoapMessage(saajMessage);
        this.header = saajSoapMessage.getSoapHeader();
    }

    public void testAddHeaderElement() throws TransformerException, SOAPException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        QName qName = new QName("namespace", "localName", "prefix");
        SoapHeaderElement headerElement = header.addHeaderElement(qName);
        transformer.transform(new StringSource("<content/>"), headerElement.getResult());
        assertNotNull("No SoapHeaderElement returned", headerElement);
        assertEquals("Invalid qName for element", qName, headerElement.getName());
        Name name = saajEnvelope.createName("localName", "prefix", "namespace");
        Iterator iterator = saajHeader.getChildElements(name);
        SOAPHeaderElement saajHeaderElement = (SOAPHeaderElement) iterator.next();
        assertEquals("Invalid localName", qName.getLocalPart(), saajHeaderElement.getElementName().getLocalName());
        assertEquals("Invalid prefix", qName.getPrefix(), saajHeaderElement.getElementName().getPrefix());
        assertEquals("Invalid namespace", qName.getNamespaceURI(), saajHeaderElement.getElementName().getURI());
        assertTrue("SAAJ Header has no children", saajHeaderElement.hasChildNodes());
        iterator = saajHeaderElement.getChildElements();
        SOAPElement child = (SOAPElement) iterator.next();
        assertEquals("Invalid localName", "content", child.getElementName().getLocalName());
    }

    public void testExamineMustUnderstandHeaderElements() throws SOAPException {
        Name name1 = saajEnvelope.createName("localName1", "prefix", "namespace");
        SOAPHeaderElement saajHeaderElement1 = saajHeader.addHeaderElement(name1);
        saajHeaderElement1.setMustUnderstand(true);
        saajHeaderElement1.setActor("role1");
        Name name2 = saajEnvelope.createName("localName2", "prefix", "namespace");
        SOAPHeaderElement saajHeaderElement2 = saajHeader.addHeaderElement(name2);
        saajHeaderElement2.setMustUnderstand(true);
        saajHeaderElement2.setActor("role2");
        Iterator iterator = header.examineMustUnderstandHeaderElements("role1");
        assertNotNull("Iterator is null", iterator);
        assertTrue("Iterator is empty", iterator.hasNext());
        SoapHeaderElement headerElement = (SoapHeaderElement) iterator.next();
        assertEquals("Invalid name on header element", new QName("namespace", "localName1"), headerElement.getName());
        assertTrue("MustUnderstand not set on header element", headerElement.getMustUnderstand());
        assertEquals("Invalid role on header element", "role1", headerElement.getRole());
        assertFalse("Iterator contains too much element", iterator.hasNext());
    }
}
