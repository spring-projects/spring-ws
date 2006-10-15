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

package org.springframework.ws.soap.saaj.saaj11;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;

import org.custommonkey.xmlunit.XMLTestCase;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.soap.saaj.support.SaajUtils;
import org.springframework.xml.transform.StringResult;
import org.xml.sax.InputSource;

public class Saaj12XmlReaderTest extends XMLTestCase {

    private Transformer transformer;

    private MessageFactory messageFactory;

    protected void setUp() throws Exception {
        messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        transformer = TransformerFactory.newInstance().newTransformer();
    }

    public void testReader() throws Exception {
        SOAPMessage message = messageFactory.createMessage();
        SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
        Saaj12XmlReader reader = new Saaj12XmlReader(envelope);
        DOMSource domSource = new DOMSource(envelope);
        StringResult expected = new StringResult();
        transformer.transform(domSource, expected);

        StringResult result = new StringResult();
        transformer.transform(new SAXSource(reader, new InputSource()), result);
        assertXMLEqual(expected.toString(), result.toString());
    }

    public void testReader2() throws Exception {
        SOAPMessage message =
                SaajUtils.loadMessage(new ClassPathResource("org/springframework/ws/soap/context/soap11.xml"));
        SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
        Saaj12XmlReader reader = new Saaj12XmlReader(envelope);
        DOMSource domSource = new DOMSource(envelope);
        StringResult expected = new StringResult();
        transformer.transform(domSource, expected);

        StringResult result = new StringResult();
        transformer.transform(new SAXSource(reader, new InputSource()), result);
        assertXMLEqual(expected.toString(), result.toString());
    }

}