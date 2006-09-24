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

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.custommonkey.xmlunit.XMLTestCase;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapFault;
import org.springframework.xml.transform.StringResult;

public class SaajSoapBodyTest extends XMLTestCase {

    private SOAPBody saajBody;

    private SoapBody body;

    protected void setUp() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage saajMessage = messageFactory.createMessage();
        this.saajBody = saajMessage.getSOAPBody();
        SaajSoapMessage saajSoapMessage = new SaajSoapMessage(saajMessage);
        this.body = saajSoapMessage.getSoapBody();
    }

    public void testAddMustUnderstandFault() {
        SoapFault fault = body.addMustUnderstandFault(null);
        assertNotNull("Null returned", fault);
        assertTrue("SoapBody has no fault", body.hasFault());
        assertNotNull("SoapBody has no fault", body.getFault());
        assertEquals("Invalid fault code", new QName("http://schemas.xmlsoap.org/soap/envelope/", "MustUnderstand"),
                fault.getFaultCode());
        assertTrue("Fault not a MustUnderstand fault", fault.isMustUnderstandFault());
        assertFalse("Fault is a Sender fault", fault.isSenderFault());
        assertFalse("Fault is a Receiver fault", fault.isReceiverFault());
        assertNotNull("Fault source is null", fault.getSource());
        assertTrue("SAAJ SOAPBody has no fault", saajBody.hasFault());
        SOAPFault saajFault = saajBody.getFault();
        assertNotNull("Null returned", saajFault);
        assertEquals("Invalid fault code", saajBody.getPrefix() + ":MustUnderstand", saajFault.getFaultCode());
    }

    public void testAddSenderFault() {
        SoapFault fault = body.addSenderFault("faultString");
        assertNotNull("Null returned", fault);
        assertTrue("SoapBody has no fault", body.hasFault());
        assertNotNull("SoapBody has no fault", body.getFault());
        assertEquals("Invalid fault code", new QName("http://schemas.xmlsoap.org/soap/envelope/", "Client"),
                fault.getFaultCode());
        assertEquals("Invalid fault string ", "faultString", fault.getFaultString());
        assertTrue("Fault not a Sender fault", fault.isSenderFault());
        assertFalse("Fault is a MustUnderstand fault", fault.isMustUnderstandFault());
        assertFalse("Fault is a Receiver fault", fault.isReceiverFault());
        assertNotNull("Fault source is null", fault.getSource());
        assertTrue("SAAJ SOAPBody has no fault", saajBody.hasFault());
        SOAPFault saajFault = saajBody.getFault();
        assertNotNull("Null returned", saajFault);
        assertEquals("Invalid fault code", saajBody.getPrefix() + ":Client", saajFault.getFaultCode());
    }

    public void testAddReceiverFault() {
        SoapFault fault = body.addReceiverFault("faultString");
        assertNotNull("Null returned", fault);
        assertTrue("SoapBody has no fault", body.hasFault());
        assertNotNull("SoapBody has no fault", body.getFault());
        assertEquals("Invalid fault code", new QName("http://schemas.xmlsoap.org/soap/envelope/", "Server"),
                fault.getFaultCode());
        assertEquals("Invalid fault string ", "faultString", fault.getFaultString());
        assertTrue("Fault not a Receiver fault", fault.isReceiverFault());
        assertFalse("Fault is a MustUnderstand fault", fault.isMustUnderstandFault());
        assertFalse("Fault is a Sender fault", fault.isSenderFault());
        assertNotNull("Fault source is null", fault.getSource());
        assertTrue("SAAJ SOAPBody has no fault", saajBody.hasFault());
        SOAPFault saajFault = saajBody.getFault();
        assertNotNull("Null returned", saajFault);
        assertEquals("Invalid fault code", saajBody.getPrefix() + ":Server", saajFault.getFaultCode());
    }

    public void testAddFault() {
        QName faultCode = new QName("namespace", "localPart", "prefix");
        SoapFault fault = body.addFault(faultCode, "Fault");
        assertNotNull("Null returned", fault);
        assertTrue("SoapBody has no fault", body.hasFault());
        assertNotNull("SoapBody has no fault", body.getFault());
        assertEquals("Invalid fault code", faultCode, fault.getFaultCode());
        assertEquals("Invalid fault string", "Fault", fault.getFaultString());
        assertFalse("Fault is a MustUnderstand fault", fault.isMustUnderstandFault());
        assertFalse("Fault is a Sender fault", fault.isSenderFault());
        assertFalse("Fault is a Receiver fault", fault.isReceiverFault());
        assertNotNull("Fault source is null", fault.getSource());
        assertTrue("SAAJ SOAPBody has no fault", saajBody.hasFault());
        SOAPFault saajFault = saajBody.getFault();
        assertNotNull("Null returned", saajFault);
        assertEquals("Invalid fault code", "prefix:localPart", saajFault.getFaultCode());
    }

    public void testGetName() throws Exception {
        assertEquals("Invalid name", new QName(SOAPConstants.URI_NS_SOAP_ENVELOPE, "Body"), body.getName());
    }

    public void testGetSource() throws Exception {
        Name name = ((SOAPEnvelope) saajBody.getParentElement()).createName("localName", "prefix", "namespace");
        saajBody.addBodyElement(name);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringResult result = new StringResult();
        transformer.transform(body.getSource(), result);
        assertXMLEqual("Invalid source for body",
                "<Body xmlns='http://schemas.xmlsoap.org/soap/envelope/'><prefix:localName xmlns:prefix='namespace'/></Body>",
                result.toString());
    }

    public void testAddFaultWithExistingPayload() throws Exception {
        Name payloadName = ((SOAPEnvelope) saajBody.getParentElement()).createName("localName", "prefix", "namespace");
        saajBody.addBodyElement(payloadName);
        body.addReceiverFault("faultString");
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringResult result = new StringResult();
        transformer.transform(body.getSource(), result);
        assertXMLEqual("Invalid source for body",
                "<SOAP-ENV:Body xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'>" +
                        "<SOAP-ENV:Fault><faultstring>faultString</faultstring><faultcode>SOAP-ENV:Server</faultcode></SOAP-ENV:Fault>" +
                        "</SOAP-ENV:Body>", result.toString());

    }


}
