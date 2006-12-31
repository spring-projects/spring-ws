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

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.soap11.AbstractSoap11MessageTestCase;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

public class SaajSoap11MessageTest extends AbstractSoap11MessageTestCase {

    private SOAPMessage saajMessage;

    protected final SoapMessage createSoapMessage() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        saajMessage = messageFactory.createMessage();
        return new SaajSoapMessage(saajMessage);
    }

    public void testGetPayloadSource() throws Exception {
        saajMessage.getSOAPBody().addChildElement("child");
        Source source = soapMessage.getPayloadSource();
        StringResult result = new StringResult();
        transformer.transform(source, result);
        assertXMLEqual("Invalid source", "<child/>", result.toString());
    }

    public void testGetPayloadSourceText() throws Exception {
        saajMessage.getSOAPBody().addTextNode(" ");
        saajMessage.getSOAPBody().addChildElement("child");
        Source source = soapMessage.getPayloadSource();
        StringResult result = new StringResult();
        transformer.transform(source, result);
        assertXMLEqual("Invalid source", "<child/>", result.toString());
    }

    public void testGetPayloadResult() throws Exception {
        StringSource source = new StringSource("<child/>");
        Result result = soapMessage.getPayloadResult();
        transformer.transform(source, result);
        assertTrue("No child nodes created", saajMessage.getSOAPBody().hasChildNodes());
        assertEquals("Invalid child node created", "child", saajMessage.getSOAPBody().getFirstChild().getLocalName());
    }

}
