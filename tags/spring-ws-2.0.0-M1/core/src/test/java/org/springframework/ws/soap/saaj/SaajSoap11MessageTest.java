/*
 * Copyright 2005-2010 the original author or authors.
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
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.soap11.AbstractSoap11MessageTestCase;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import org.junit.Test;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SaajSoap11MessageTest extends AbstractSoap11MessageTestCase {

    private SOAPMessage saajMessage;

    @Override
    protected final SoapMessage createSoapMessage() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        saajMessage = messageFactory.createMessage();
        saajMessage.getSOAPHeader().detachNode();
        return new SaajSoapMessage(saajMessage);
    }

    @Test
    public void testGetPayloadSource() throws Exception {
        saajMessage.getSOAPPart().getEnvelope().getBody().addChildElement("child");
        Source source = soapMessage.getPayloadSource();
        StringResult result = new StringResult();
        transformer.transform(source, result);
        assertXMLEqual("Invalid source", "<child/>", result.toString());
    }

    @Test
    public void testGetPayloadSourceText() throws Exception {
        SOAPBody body = saajMessage.getSOAPPart().getEnvelope().getBody();
        body.addTextNode(" ");
        body.addChildElement("child");
        Source source = soapMessage.getPayloadSource();
        StringResult result = new StringResult();
        transformer.transform(source, result);
        assertXMLEqual("Invalid source", "<child/>", result.toString());
    }

    @Test
    public void testGetPayloadResult() throws Exception {
        StringSource source = new StringSource("<child/>");
        Result result = soapMessage.getPayloadResult();
        transformer.transform(source, result);
        SOAPBody body = saajMessage.getSOAPPart().getEnvelope().getBody();
        Iterator<?> iterator = body.getChildElements();
        assertTrue("No child nodes created", iterator.hasNext());
        SOAPBodyElement bodyElement = (SOAPBodyElement) iterator.next();
        assertEquals("Invalid child node created", "child", bodyElement.getElementName().getLocalName());
    }

}
