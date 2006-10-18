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
 *  * limitations under the License.
 */

package org.springframework.ws.soap.saaj;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.custommonkey.xmlunit.XMLTestCase;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

public class Saaj11ImplementationStrategyTest extends XMLTestCase {

    private Saaj11ImplementationStrategy strategy;

    private SOAPMessage message;

    private SOAPEnvelope envelope;

    private Transformer transformer;

    protected void setUp() throws Exception {
        strategy = new Saaj11ImplementationStrategy();
        MessageFactory messageFactory = MessageFactory.newInstance();
        message = messageFactory.createMessage();
        envelope = message.getSOAPPart().getEnvelope();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();
    }

    public void testGetName() throws Exception {
        QName name = strategy.getName(envelope);
        assertEquals("Invalid name", SoapVersion.SOAP_11.getEnvelopeName(), name);
    }

    public void testGetSource() throws Exception {
        StringResult result = new StringResult();
        transformer.transform(strategy.getSource(envelope), result);

        assertXMLEqual("Invalid source",
                "<Envelope xmlns='http://schemas.xmlsoap.org/soap/envelope/'><Header/><Body/></Envelope>",
                result.toString());
    }

    public void testGetResult() throws Exception {
        SOAPBody body = envelope.getBody();
        StringSource content = new StringSource("<content xmlns='http://springframework.org/spring-ws'/>");
        transformer.transform(content, strategy.getResult(body));

        StringResult result = new StringResult();
        transformer.transform(strategy.getSource(envelope), result);
        assertXMLEqual("Invalid source",
                "<Envelope xmlns='http://schemas.xmlsoap.org/soap/envelope/'><Header/><Body><content xmlns='http://springframework.org/spring-ws'/></Body></Envelope>",
                result.toString());
    }

    public void testGetFaultCode() throws Exception {
        SOAPBody body = envelope.getBody();
        SOAPFault fault = body.addFault();
        fault.setFaultCode(fault.getElementName().getPrefix() + ":Client");
        QName faultCode = strategy.getFaultCode(fault);
        assertEquals("Invalid fault code", SoapVersion.SOAP_11.getClientOrSenderFaultName(), faultCode);
    }

    public void testAddDetailEntry() throws Exception {
        SOAPBody body = envelope.getBody();
        SOAPFault fault = body.addFault();
        QName name = new QName("http://springframework.org/spring-ws", "name", "spring-ws");

        Detail detail = fault.addDetail();
        DetailEntry result = strategy.addDetailEntry(detail, name);
        assertNotNull("No result returned", result);
        assertEquals("Invalid namespace", result.getElementName().getURI(), name.getNamespaceURI());
        assertEquals("Invalid namespace", result.getElementName().getLocalName(), name.getLocalPart());
    }

    public void testRemoveContents() throws Exception {
        SOAPBody body = envelope.getBody();
        body.addTextNode("bla");
        body.addChildElement("bla");
        strategy.removeContents(body);
        assertFalse("Children not removed", body.getChildElements().hasNext());
    }

    public void testAddHeaderElement() throws Exception {
        SOAPHeader header = envelope.getHeader();

        fail("Test is not implemented");
    }
}